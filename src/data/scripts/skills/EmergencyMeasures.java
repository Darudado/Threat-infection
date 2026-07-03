/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.characters.AfterShipCreationSkillEffect
 *  com.fs.starfarer.api.characters.CharacterStatsSkillEffect
 *  com.fs.starfarer.api.characters.LevelBasedEffect$ScopeDescription
 *  com.fs.starfarer.api.characters.MutableCharacterStatsAPI
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.characters.ShipSkillEffect
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.listeners.AdvanceableListener
 *  com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription
 */
package data.scripts.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.AfterShipCreationSkillEffect;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

public class EmergencyMeasures
extends BaseSkillEffectDescription {
    public static final float DAMAGE_REDUCTION = 0.05f;
    public static final float INVINCIBLE_DURATION = 5.0f;
    public static final float ARMOR_REPAIR_RATE = 0.05f;
    public static final float ARMOR_REPAIR_LIMIT_FRACTION = 0.5f;
    public static final float INVINCIBLE_TRIGGER_HULL_FRACTION = 0.1f;
    private static final float EXTRA_AUTO_POINTS = 50.0f;
    private static final float ARMOR_PERCENT_BONUS = 0.1f;
    private static final boolean DEBUG = false;

    public static class EliteInvincibleListener
    implements AdvanceableListener {
        private final ShipAPI ship;
        private boolean invincible = false;
        private boolean hasTriggered = false;
        private float invincibleTimer = 0.0f;
        private float armorRepairTimer = 0.0f;
        private float totalArmorRepaired = 0.0f;
        private float maxArmorTotal = 0.0f;
        private float armorRepairLimit = 0.0f;
        private boolean initArmorData = false;
        private final String invincibleId = "emergency_measures_invincible";

        public EliteInvincibleListener(ShipAPI ship) {
            this.ship = ship;
        }

        public void advance(float amount) {
            float maxHull;
            float threshold;
            float currHull;
            if (this.ship == null || !this.ship.isAlive()) {
                return;
            }
            if (Global.getCombatEngine().isPaused()) {
                return;
            }
            if (!this.initArmorData) {
                this.maxArmorTotal = this.getTotalArmorMax(this.ship);
                this.armorRepairLimit = this.maxArmorTotal * 0.5f;
                this.initArmorData = true;
            }
            if (!this.hasTriggered && !this.invincible && (currHull = this.ship.getHitpoints()) <= (threshold = (maxHull = this.ship.getMaxHitpoints()) * 0.1f)) {
                this.hasTriggered = true;
                this.invincible = true;
                this.invincibleTimer = 5.0f;
                this.applyInvincibility();
                if (currHull < threshold) {
                    this.ship.setHitpoints(threshold);
                }
                Global.getCombatEngine().addFloatingText(this.ship.getLocation(), "\u5e94\u6025\u7cfb\u7edf\u542f\u52a8\uff1a\u65e0\u654c 5 \u79d2", 22.0f, new Color(100, 255, 100), (CombatEntityAPI)this.ship, 0.5f, 1.5f);
                Global.getSoundPlayer().playSound("system_phase_skimmer", 1.5f, 0.6f, this.ship.getLocation(), this.ship.getVelocity());
            }
            if (this.invincible) {
                this.invincibleTimer -= amount;
                if (this.invincibleTimer <= 0.0f) {
                    this.removeInvincibility();
                    this.invincible = false;
                    Global.getCombatEngine().addFloatingText(this.ship.getLocation(), "\u5e94\u6025\u62a4\u76fe\u5931\u6548", 16.0f, new Color(255, 100, 100), (CombatEntityAPI)this.ship, 0.5f, 1.0f);
                } else {
                    this.armorRepairTimer += amount;
                    if (this.armorRepairTimer >= 1.0f) {
                        float repairAmount = this.maxArmorTotal * 0.05f;
                        float remaining = this.armorRepairLimit - this.totalArmorRepaired;
                        if (remaining > 0.0f) {
                            float actualRepair = Math.min(repairAmount, remaining);
                            this.repairArmor(this.ship, actualRepair);
                            this.totalArmorRepaired += actualRepair;
                            if (actualRepair > 0.0f) {
                                Global.getCombatEngine().addFloatingText(this.ship.getLocation(), String.format("\u88c5\u7532\u4fee\u590d +%.1f", Float.valueOf(actualRepair)), 14.0f, new Color(150, 255, 150), (CombatEntityAPI)this.ship, 0.3f, 0.8f);
                            }
                        }
                        this.armorRepairTimer -= 1.0f;
                    }
                    this.ship.setJitter((Object)this.ship, new Color(100, 255, 100, 200), 1.2f, 4, 0.0f, 12.0f);
                    this.ship.setJitterUnder((Object)this.ship, new Color(100, 255, 100, 100), 1.0f, 5, 0.0f, 18.0f);
                }
            }
        }

        private void applyInvincibility() {
            this.ship.getMutableStats().getHullDamageTakenMult().modifyMult("emergency_measures_invincible", 0.0f);
            this.ship.getMutableStats().getArmorDamageTakenMult().modifyMult("emergency_measures_invincible", 0.0f);
            this.ship.getMutableStats().getShieldDamageTakenMult().modifyMult("emergency_measures_invincible", 0.0f);
            this.ship.getMutableStats().getEmpDamageTakenMult().modifyMult("emergency_measures_invincible", 0.0f);
            if (this.ship.getShield() != null) {
                this.ship.getShield().toggleOn();
                this.ship.getMutableStats().getShieldUpkeepMult().modifyMult("emergency_measures_invincible", 0.0f);
            }
        }

        private void removeInvincibility() {
            this.ship.getMutableStats().getHullDamageTakenMult().unmodify("emergency_measures_invincible");
            this.ship.getMutableStats().getArmorDamageTakenMult().unmodify("emergency_measures_invincible");
            this.ship.getMutableStats().getShieldDamageTakenMult().unmodify("emergency_measures_invincible");
            this.ship.getMutableStats().getEmpDamageTakenMult().unmodify("emergency_measures_invincible");
            if (this.ship.getShield() != null) {
                this.ship.getMutableStats().getShieldUpkeepMult().unmodify("emergency_measures_invincible");
            }
        }

        private float getTotalArmorMax(ShipAPI ship) {
            float[][] grid = ship.getArmorGrid().getGrid();
            if (grid == null || grid.length == 0) {
                return 0.0f;
            }
            float maxPerCell = ship.getArmorGrid().getMaxArmorInCell();
            return (float)(grid.length * grid[0].length) * maxPerCell;
        }

        private void repairArmor(ShipAPI ship, float amount) {
            float[][] armorGrid = ship.getArmorGrid().getGrid();
            if (armorGrid == null) {
                return;
            }
            float maxPerCell = ship.getArmorGrid().getMaxArmorInCell();
            ArrayList<Cell> damagedCells = new ArrayList<Cell>();
            for (int i = 0; i < armorGrid.length; ++i) {
                for (int j = 0; j < armorGrid[i].length; ++j) {
                    if (!(armorGrid[i][j] < maxPerCell)) continue;
                    damagedCells.add(new Cell(i, j));
                }
            }
            if (damagedCells.isEmpty()) {
                return;
            }
            float perCell = amount / (float)damagedCells.size();
            for (Cell cell : damagedCells) {
                float newVal;
                float oldVal = armorGrid[cell.x][cell.y];
                armorGrid[cell.x][cell.y] = newVal = Math.min(maxPerCell, oldVal + perCell);
            }
        }

        private static class Cell {
            int x;
            int y;

            Cell(int x, int y) {
                this.x = x;
                this.y = y;
            }
        }
    }

    public static class Level5
    implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getArmorBonus().modifyPercent(id, 10.0f);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getArmorBonus().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "\u989d\u5916\u589e\u52a0 10% \u5f53\u524d\u88c5\u7532\u503c";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level4
    implements AfterShipCreationSkillEffect,
    ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
        }

        public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.addListener((Object)new EliteInvincibleListener(ship));
        }

        public void unapplyEffectsAfterShipCreation(ShipAPI ship, String id) {
            ship.removeListenerOfClass(EliteInvincibleListener.class);
        }

        public String getEffectDescription(float level) {
            return "\u5f53\u8230\u8239\u7ed3\u6784\u503c\u4f4e\u4e8e\u6700\u5927\u503c\u7684 10% \u65f6\uff0c\u83b7\u5f97 5 \u79d2\u65e0\u654c\uff0c\u5e76\u5728\u65e0\u654c\u671f\u95f4\u6bcf\u79d2\u4fee\u590d 5% \u6700\u5927\u88c5\u7532\u503c\uff0c\u603b\u4fee\u590d\u91cf\u4e0d\u8d85\u8fc7\u6700\u5927\u88c5\u7532\u503c\u7684 50%\uff08\u4ec5\u89e6\u53d1\u4e00\u6b21\uff09";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
        }
    }

    public static class Level3
    implements ShipSkillEffect {
        private static final HashMap<ShipAPI.HullSize, Float> FLAT_ARMOR_BONUS = new HashMap();

        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if (hullSize == null) {
                return;
            }
            Float flatBonus = FLAT_ARMOR_BONUS.get(hullSize);
            if (flatBonus != null && flatBonus.floatValue() > 0.0f) {
                stats.getArmorBonus().modifyFlat(id, flatBonus.floatValue());
            }
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getArmorBonus().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "\u6839\u636e\u8230\u8239\u89c4\u6a21\u589e\u52a0\u62a4\u7532\uff1a\u62a4\u536b\u8230+75\uff0c\u9a71\u9010\u8230+125\uff0c\u5de1\u6d0b\u8230+150\uff0c\u4e3b\u529b\u8230+200";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
        }

        static {
            FLAT_ARMOR_BONUS.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(75.0f));
            FLAT_ARMOR_BONUS.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(125.0f));
            FLAT_ARMOR_BONUS.put(ShipAPI.HullSize.CRUISER, Float.valueOf(150.0f));
            FLAT_ARMOR_BONUS.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(200.0f));
            FLAT_ARMOR_BONUS.put(ShipAPI.HullSize.DEFAULT, Float.valueOf(0.0f));
        }
    }

    public static class Level2
    extends BaseSkillEffectDescription
    implements CharacterStatsSkillEffect {
        public void apply(MutableCharacterStatsAPI stats, String id, float level) {
            if (stats.isPlayerStats()) {
                PersonAPI player = Global.getSector().getPlayerPerson();
                float current = player.getMemoryWithoutUpdate().getFloat("$autoPointsValue");
                float newValue = current + 50.0f;
                player.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)Float.valueOf(newValue));
            }
        }

        public void unapply(MutableCharacterStatsAPI stats, String id) {
            if (stats.isPlayerStats()) {
                PersonAPI player = Global.getSector().getPlayerPerson();
                float current = player.getMemoryWithoutUpdate().getFloat("$autoPointsValue");
                float newValue = Math.max(0.0f, current - 50.0f);
                player.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)Float.valueOf(newValue));
            }
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.FLEET;
        }
    }

    public static class Level1
    implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            stats.getHullDamageTakenMult().modifyMult(id, 0.95f);
            stats.getArmorDamageTakenMult().modifyMult(id, 0.95f);
            stats.getShieldDamageTakenMult().modifyMult(id, 0.95f);
        }

        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getHullDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getShieldDamageTakenMult().unmodify(id);
        }

        public String getEffectDescription(float level) {
            return "-5% \u6240\u53d7\u4f24\u5bb3";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.PILOTED_SHIP;
        }
    }
}

