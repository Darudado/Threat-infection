/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class HeavyArmorTurrets
extends BaseHullMod {
    public static float ARMOR_EFF_MULT = 1.05f;
    public static float ARMOR_BONUS_PERCENT = 25.0f;
    public static float TURN_PENALTY_PERCENT = 25.0f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_EFF_MULT);
        stats.getArmorBonus().modifyPercent(id, ARMOR_BONUS_PERCENT);
        stats.getWeaponTurnRateBonus().modifyMult(id, 1.0f - TURN_PENALTY_PERCENT * 0.01f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "5%";
        }
        if (index == 1) {
            return (int)ARMOR_BONUS_PERCENT + "%";
        }
        if (index == 2) {
            return (int)TURN_PENALTY_PERCENT + "%";
        }
        return null;
    }
}

