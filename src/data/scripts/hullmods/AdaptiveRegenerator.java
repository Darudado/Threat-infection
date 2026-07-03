/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.CargoAPI$CargoItemType
 *  com.fs.starfarer.api.campaign.CargoStackAPI
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.vector.Vector2f;

public class AdaptiveRegenerator
extends BaseHullMod {
    private static final float HULL_REPAIR_PERCENT = 1.0f;
    private static final float ARMOR_REPAIR_PERCENT = 0.5f;
    private static final String HULLMOD_ID = "fragment_fabricator";
    private static final float SMOD_HULL_REPAIR_BONUS = 1.0f;
    private static final float SMOD_ARMOR_REPAIR_BONUS = 0.5f;
    private static final float BASE_REPAIR_LIMIT = 0.3f;
    private static final float BUILT_IN_REPAIR_LIMIT_BONUS = 0.2f;
    private static final Color REPAIR_COLOR = new Color(220, 220, 220, 200);
    private static final Color SMOD_REPAIR_COLOR = new Color(220, 255, 220, 220);
    private static final Color STATUS_COLOR = new Color(180, 255, 180, 255);
    private static final float REPAIR_PARTICLE_CHANCE = 0.4f;
    private static final int MAX_PARTICLES_PER_FRAME = 6;
    private Map<ShipAPI, ShipState> shipStates = new HashMap<ShipAPI, ShipState>();

    private void showStatusText(ShipAPI ship, String text, Color color) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        float currentTime = engine.getTotalElapsedTime(false);
        ShipState state = this.getShipState(ship);
        if (state.canShowStatus(currentTime)) {
            engine.addFloatingText(ship.getLocation(), text, 16.0f, color, (CombatEntityAPI)ship, 0.5f, 0.8f);
            state.setLastStatusDisplayTime(currentTime);
        }
    }

    private void showRepairProgress(ShipAPI ship, float previousHull, float currentHull, float maxHull, boolean isSMod) {
        CombatEngineAPI engine;
        float threshold;
        float repairAmount = currentHull - previousHull;
        float f = threshold = isSMod ? 0.02f : 0.03f;
        if (repairAmount > maxHull * threshold && (engine = Global.getCombatEngine()) != null) {
            float currentTime = engine.getTotalElapsedTime(false);
            ShipState state = this.getShipState(ship);
            if (currentTime - state.lastStatusDisplayTime > 5.0f) {
                Object progressText = String.format("+%.1f%%", Float.valueOf(repairAmount / maxHull * 100.0f));
                if (isSMod) {
                    progressText = (String)progressText + " (S-Mod)";
                }
                engine.addFloatingText(ship.getLocation(), (String)progressText, 14.0f, isSMod ? new Color(150, 255, 150, 255) : STATUS_COLOR, (CombatEntityAPI)ship, 0.5f, 0.6f);
                state.lastStatusDisplayTime = currentTime;
            }
        }
    }

    private ShipState getShipState(ShipAPI ship) {
        if (!this.shipStates.containsKey(ship)) {
            this.shipStates.put(ship, new ShipState());
        }
        return this.shipStates.get(ship);
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        float[][] armorGrid;
        boolean isSMod;
        if (!ship.isAlive()) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) {
            return;
        }
        ShipState state = this.getShipState(ship);
        state.isSMod = isSMod = this.isSMod(ship);
        if (!state.limitsInitialized) {
            boolean builtIn = super.isBuiltIn(ship);
            float limitMultiplier = 0.3f + (builtIn ? 0.2f : 0.0f);
            state.maxHullLimit = ship.getMaxHitpoints() * limitMultiplier;
            try {
                armorGrid = ship.getArmorGrid().getGrid();
                float maxArmorPerCell = ship.getArmorGrid().getMaxArmorInCell();
                int totalCells = armorGrid.length * armorGrid[0].length;
                state.maxArmorLimit = (float)totalCells * maxArmorPerCell * limitMultiplier;
            }
            catch (Exception e) {
                state.maxArmorLimit = 0.0f;
            }
            state.limitsInitialized = true;
        }
        float previousHull = ship.getHitpoints();
        boolean needsRepair = false;
        if (ship.getHitpoints() < ship.getMaxHitpoints() * 0.99f && state.hullRepaired < state.maxHullLimit) {
            needsRepair = true;
            state.isRepairing = true;
        } else if (state.armorRepaired < state.maxArmorLimit) {
            try {
                armorGrid = ship.getArmorGrid().getGrid();
                float maxArmor = ship.getArmorGrid().getMaxArmorInCell();
                block4: for (int x = 0; x < armorGrid.length; ++x) {
                    for (int y = 0; y < armorGrid[0].length; ++y) {
                        if (!(armorGrid[x][y] < maxArmor * 0.99f)) continue;
                        needsRepair = true;
                        state.isRepairing = true;
                        break block4;
                    }
                }
                if (!needsRepair) {
                    state.isRepairing = false;
                }
            }
            catch (Exception e) {
                state.isRepairing = false;
            }
        } else {
            state.isRepairing = false;
        }
        if (state.isRepairing) {
            this.repairShip(ship, amount, previousHull, isSMod, state);
            this.showRepairEffects(ship, state, amount, isSMod);
            if (engine.getTotalElapsedTime(false) - state.lastStatusDisplayTime > 10.0f) {
                String statusText = isSMod ? "S-Mod\u589e\u5f3a\u4fee\u590d\u4e2d" : "\u6b63\u5728\u81ea\u52a8\u4fee\u590d";
                this.showStatusText(ship, statusText, isSMod ? new Color(150, 255, 150, 255) : STATUS_COLOR);
            }
        } else if (state.hullRepaired >= state.maxHullLimit && state.armorRepaired >= state.maxArmorLimit && engine.getTotalElapsedTime(false) - state.lastStatusDisplayTime > 10.0f) {
            this.showStatusText(ship, "\u4fee\u590d\u4e0a\u9650\u5df2\u8fbe", Misc.getGrayColor());
        }
        state.effectInterval.advance(amount);
        this.cleanupDestroyedShips();
    }

    private void cleanupDestroyedShips() {
        this.shipStates.keySet().removeIf(ship -> !ship.isAlive());
    }

    private void repairShip(ShipAPI ship, float amount, float previousHull, boolean isSMod, ShipState state) {
        float maxHull;
        float currentHull = ship.getHitpoints();
        if (currentHull < (maxHull = ship.getMaxHitpoints()) && state.hullRepaired < state.maxHullLimit) {
            float hullRepairPercent = 1.0f;
            if (isSMod) {
                hullRepairPercent += 1.0f;
            }
            float hullRepair = maxHull * (hullRepairPercent / 100.0f) * amount;
            float remainingHullLimit = state.maxHullLimit - state.hullRepaired;
            hullRepair = Math.min(hullRepair, remainingHullLimit);
            float maxRepair = maxHull - currentHull;
            if ((hullRepair = Math.min(hullRepair, maxRepair)) > 0.0f) {
                float newHull = currentHull + hullRepair;
                ship.setHitpoints(newHull);
                state.hullRepaired += hullRepair;
                this.showRepairProgress(ship, previousHull, newHull, maxHull, isSMod);
            }
        }
        this.repairArmor(ship, amount, isSMod, state);
    }

    private void repairArmor(ShipAPI ship, float amount, boolean isSMod, ShipState state) {
        try {
            float[][] armorGrid = ship.getArmorGrid().getGrid();
            float maxArmorPerCell = ship.getArmorGrid().getMaxArmorInCell();
            int gridWidth = armorGrid.length;
            int gridHeight = armorGrid[0].length;
            float armorRepairPercent = 0.5f;
            if (isSMod) {
                armorRepairPercent += 0.5f;
            }
            float repairPerFrame = maxArmorPerCell * (armorRepairPercent / 100.0f) * amount;
            float remainingArmorLimit = state.maxArmorLimit - state.armorRepaired;
            if (remainingArmorLimit <= 0.0f) {
                return;
            }
            for (int x = 0; x < gridWidth; ++x) {
                for (int y = 0; y < gridHeight; ++y) {
                    float currentArmor;
                    float maxRepairForCell;
                    if (!(armorGrid[x][y] < maxArmorPerCell * 0.99f) || (maxRepairForCell = maxArmorPerCell - (currentArmor = armorGrid[x][y])) <= 0.0f) continue;
                    float repairAmount = Math.min(repairPerFrame, maxRepairForCell);
                    if ((repairAmount = Math.min(repairAmount, remainingArmorLimit)) <= 0.0f) break;
                    armorGrid[x][y] = currentArmor + repairAmount;
                    state.armorRepaired += repairAmount;
                    if ((remainingArmorLimit -= repairAmount) <= 0.0f) break;
                }
                if (!(remainingArmorLimit <= 0.0f)) {
                    continue;
                }
                break;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void showRepairEffects(ShipAPI ship, ShipState state, float amount, boolean isSMod) {
        if (!state.effectInterval.intervalElapsed()) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        int particlesToCreate = (int)(Math.random() * 6.0) + 1;
        for (int i = 0; i < particlesToCreate; ++i) {
            if (!(Math.random() < (double)0.4f)) continue;
            float size = 6.0f + (float)Math.random() * 12.0f;
            int alpha = 120 + (int)(Math.random() * 80.0);
            Color color = isSMod ? new Color(SMOD_REPAIR_COLOR.getRed(), SMOD_REPAIR_COLOR.getGreen(), SMOD_REPAIR_COLOR.getBlue(), alpha) : new Color(REPAIR_COLOR.getRed(), REPAIR_COLOR.getGreen(), REPAIR_COLOR.getBlue(), alpha);
            float radius = ship.getCollisionRadius() * 0.8f;
            float angle = (float)Math.random() * 360.0f;
            float distance = (float)Math.random() * radius;
            float x = (float)Math.cos(Math.toRadians(angle)) * distance;
            float y = (float)Math.sin(Math.toRadians(angle)) * distance;
            float velX = (float)Math.random() * 15.0f - 7.5f;
            float velY = (float)Math.random() * 15.0f - 7.5f;
            Vector2f location = new Vector2f(ship.getLocation().x + x, ship.getLocation().y + y);
            engine.addHitParticle(location, new Vector2f(velX, velY), size, 0.8f, 0.5f + (float)Math.random() * 0.3f, color);
        }
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    public CargoStackAPI getRequiredItem() {
        return Global.getSettings().createCargoStack(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData(HULLMOD_ID, (String)null), (CargoAPI)null);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship == null || !ship.getHullSpec().isPhase();
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && ship.getHullSpec().isPhase()) {
            return "\u65e0\u6cd5\u5b89\u88c5\u5728\u76f8\u4f4d\u8230\u8239\u4e0a";
        }
        return super.getUnapplicableReason(ship);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        switch (index) {
            case 0: {
                return String.format("%.1f%%", Float.valueOf(1.0f));
            }
            case 1: {
                return String.format("%.1f%%", Float.valueOf(0.5f));
            }
        }
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10.0f;
        float small = 5.0f;
        Color highlight = Misc.getHighlightColor();
        Color sModColor = new Color(150, 255, 150, 255);
        tooltip.addPara("\u81ea\u9002\u5e94\u518d\u751f\u7cfb\u7edf", pad, highlight, new String[]{"\u81ea\u9002\u5e94\u518d\u751f\u7cfb\u7edf"});
        tooltip.addPara("\u81ea\u52a8\u4fee\u590d\u53d7\u635f\u7684\u8230\u8239\uff1a", small);
        tooltip.setBulletedListMode("  - ");
        tooltip.addPara("\u6bcf\u79d2\u4fee\u590d %s \u7ed3\u6784\u503c", 0.0f, highlight, new String[]{String.format("%.1f%%", Float.valueOf(1.0f))});
        tooltip.addPara("\u6bcf\u79d2\u4fee\u590d %s \u88c5\u7532\u503c", 0.0f, highlight, new String[]{String.format("%.1f%%", Float.valueOf(0.5f))});
        tooltip.addPara("\u6700\u591a\u4fee\u590d\u8230\u8239\u6700\u5927\u7ed3\u6784/\u88c5\u7532\u503c\u7684 %s", 0.0f, highlight, new String[]{String.format("%.0f%%", Float.valueOf(30.000002f))});
        tooltip.addPara("\u5185\u7f6e\u65f6\u63d0\u5347\u81f3 %s", 0.0f, highlight, new String[]{String.format("%.0f%%", Float.valueOf(50.0f))});
        tooltip.setBulletedListMode(null);
        if (ship != null && ship.isAlive() && !isForModSpec) {
            Color statusColor;
            tooltip.addPara("", pad);
            ShipState state = this.getShipState(ship);
            String statusText = (String)state.getStatusText();
            if (state.isRepairing) {
                statusColor = state.isSMod ? sModColor : Misc.getPositiveHighlightColor();
                statusText = "\u7cfb\u7edf\u5de5\u4f5c\u4e2d - " + statusText;
                if (state.isSMod) {
                    statusText = statusText + " (S-Mod\u589e\u5f3a)";
                }
            } else {
                statusColor = Misc.getGrayColor();
                statusText = "\u7cfb\u7edf\u5f85\u673a - " + statusText;
            }
            tooltip.addPara("\u5f53\u524d\u72b6\u6001: " + statusText, small, statusColor, new String[]{statusText});
            if (state.limitsInitialized) {
                float hullPercent = state.hullRepaired / state.maxHullLimit * 100.0f;
                float armorPercent = state.armorRepaired / state.maxArmorLimit * 100.0f;
                tooltip.addPara("\u7ed3\u6784\u4fee\u590d\u8fdb\u5ea6: %s / 100%%", small, highlight, new String[]{String.format("%.1f%%", Float.valueOf(hullPercent))});
                tooltip.addPara("\u88c5\u7532\u4fee\u590d\u8fdb\u5ea6: %s / 100%%", small, highlight, new String[]{String.format("%.1f%%", Float.valueOf(armorPercent))});
            }
        }
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        switch (index) {
            case 0: {
                return String.format("%.1f%%", Float.valueOf(1.0f));
            }
            case 1: {
                return String.format("%.1f%%", Float.valueOf(0.5f));
            }
        }
        return null;
    }

    public boolean isSModEffectAPenalty() {
        return false;
    }

    private static class ShipState {
        boolean isRepairing = false;
        boolean isSMod = false;
        float lastStatusDisplayTime = 0.0f;
        IntervalUtil effectInterval = new IntervalUtil(0.15f, 0.2f);
        float hullRepaired = 0.0f;
        float armorRepaired = 0.0f;
        float maxHullLimit = 0.0f;
        float maxArmorLimit = 0.0f;
        boolean limitsInitialized = false;

        private ShipState() {
        }

        String getStatusText() {
            if (this.isRepairing) {
                return "\u4fee\u590d\u4e2d";
            }
            if (this.maxHullLimit > 0.0f && this.hullRepaired >= this.maxHullLimit && this.maxArmorLimit > 0.0f && this.armorRepaired >= this.maxArmorLimit) {
                return "\u5df2\u8fbe\u4fee\u590d\u4e0a\u9650";
            }
            return "\u5c31\u7eea";
        }

        boolean canShowStatus(float currentTime) {
            return currentTime - this.lastStatusDisplayTime > 5.0f;
        }

        void setLastStatusDisplayTime(float time) {
            this.lastStatusDisplayTime = time;
        }
    }
}

