/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class IntegratedSystemStats
extends BaseShipSystemScript {
    public static float SPEED_BONUS = 125.0f;
    public static float TURN_BONUS = 20.0f;
    private Color color = new Color(100, 50, 120, 255);
    private static Map<ShipAPI.HullSize, Float> damageReductionMap = new HashMap<ShipAPI.HullSize, Float>();
    protected Object STATUSKEY1 = new Object();
    public static float ROF_BONUS = 1.0f;
    public static float FLUX_REDUCTION = 50.0f;

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        Float reduction;
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(id + "_plasma");
            stats.getMaxTurnRate().unmodify(id + "_plasma");
            stats.getTurnAcceleration().unmodify(id + "_plasma");
            stats.getAcceleration().unmodify(id + "_plasma");
            stats.getDeceleration().unmodify(id + "_plasma");
        } else {
            stats.getMaxSpeed().modifyFlat(id + "_plasma", SPEED_BONUS * effectLevel);
            stats.getAcceleration().modifyPercent(id + "_plasma", SPEED_BONUS * 3.0f * effectLevel);
            stats.getDeceleration().modifyPercent(id + "_plasma", SPEED_BONUS * 3.0f * effectLevel);
            stats.getTurnAcceleration().modifyFlat(id + "_plasma", TURN_BONUS * effectLevel);
            stats.getTurnAcceleration().modifyPercent(id + "_plasma", TURN_BONUS * 5.0f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id + "_plasma", 15.0f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id + "_plasma", 100.0f * effectLevel);
        }
        float damageMult = 1.0f;
        if (stats.getVariant() != null && (reduction = damageReductionMap.get(stats.getVariant().getHullSize())) != null) {
            damageMult = 1.0f - (1.0f - reduction.floatValue()) * effectLevel;
        }
        stats.getHullDamageTakenMult().modifyMult(id + "_damper", damageMult);
        stats.getArmorDamageTakenMult().modifyMult(id + "_damper", damageMult);
        stats.getEmpDamageTakenMult().modifyMult(id + "_damper", damageMult);
        float rofMult = 1.0f + ROF_BONUS * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id + "_ammo", rofMult);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id + "_ammo", 1.0f - FLUX_REDUCTION * 0.01f * effectLevel);
        if (stats.getEntity() instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI)stats.getEntity();
            ship.getEngineController().fadeToOtherColor((Object)this, this.color, new Color(0, 0, 0, 0), effectLevel, 0.67f);
            ship.getEngineController().extendFlame((Object)this, 2.0f * effectLevel, 0.0f * effectLevel, 0.0f * effectLevel);
            ship.getEngineController().extendFlame((Object)this, 1.5f * effectLevel, 0.5f * effectLevel, 0.3f * effectLevel);
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                float percent = (1.0f - damageMult) * 100.0f;
                Global.getCombatEngine().maintainStatusForPlayerShip(this.STATUSKEY1, "graphics/icons/hullsys/damper_field.png", "\u7efc\u5408\u7cfb\u7edf", String.format("\u4f24\u5bb3\u51cf\u514d: %.0f%%", Float.valueOf(percent)), false);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id + "_plasma");
        stats.getMaxTurnRate().unmodify(id + "_plasma");
        stats.getTurnAcceleration().unmodify(id + "_plasma");
        stats.getAcceleration().unmodify(id + "_plasma");
        stats.getDeceleration().unmodify(id + "_plasma");
        stats.getHullDamageTakenMult().unmodify(id + "_damper");
        stats.getArmorDamageTakenMult().unmodify(id + "_damper");
        stats.getEmpDamageTakenMult().unmodify(id + "_damper");
        stats.getBallisticRoFMult().unmodify(id + "_ammo");
        stats.getBallisticWeaponFluxCostMod().unmodify(id + "_ammo");
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        switch (index) {
            case 0: {
                return new ShipSystemStatsScript.StatusData("+" + (int)(SPEED_BONUS * effectLevel) + " \u6700\u5927\u901f\u5ea6", false);
            }
            case 1: {
                return new ShipSystemStatsScript.StatusData("+" + (int)(TURN_BONUS * 5.0f * effectLevel) + "% \u8f6c\u5411", false);
            }
            case 2: {
                float rofPercent = (int)(ROF_BONUS * effectLevel * 100.0f);
                return new ShipSystemStatsScript.StatusData("\u5f39\u9053\u5c04\u901f +" + (int)rofPercent + "%", false);
            }
            case 3: {
                return new ShipSystemStatsScript.StatusData("\u5f39\u9053\u6b66\u5668\u901a\u91cf -" + (int)(FLUX_REDUCTION * effectLevel) + "%", false);
            }
        }
        return null;
    }

    public String getSystemName() {
        return "\u7efc\u5408\u589e\u5f3a\u7cfb\u7edf";
    }

    public String getSystemDescription() {
        return "\u7ed3\u5408\u4e86\u63a8\u8fdb\u3001\u9632\u5fa1\u548c\u6b66\u5668\u589e\u5f3a\u529f\u80fd\u7684\u591a\u7528\u9014\u7cfb\u7edf\u3002";
    }

    static {
        damageReductionMap.put(ShipAPI.HullSize.FIGHTER, Float.valueOf(0.33f));
        damageReductionMap.put(ShipAPI.HullSize.FRIGATE, Float.valueOf(0.33f));
        damageReductionMap.put(ShipAPI.HullSize.DESTROYER, Float.valueOf(0.33f));
        damageReductionMap.put(ShipAPI.HullSize.CRUISER, Float.valueOf(0.5f));
        damageReductionMap.put(ShipAPI.HullSize.CAPITAL_SHIP, Float.valueOf(0.5f));
    }
}

