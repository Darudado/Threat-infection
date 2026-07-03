/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class StabilizedShieldArray
extends BaseHullMod {
    private static final float SHIELD_DAMAGE_REDUCTION = 5.0f;
    private static final float HARD_FLUX_DISSIPATION = 10.0f;
    private static final float SHIELD_UPKEEP_REDUCTION = 50.0f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldDamageTakenMult().modifyMult(id, 0.95f);
        stats.getHardFluxDissipationFraction().modifyFlat(id, 0.099999994f);
        stats.getShieldUpkeepMult().modifyMult(id, 0.5f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        switch (index) {
            case 0: {
                return "5%";
            }
            case 1: {
                return "10%";
            }
            case 2: {
                return "50%";
            }
        }
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getShield() != null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return "\u8230\u8239\u6ca1\u6709\u62a4\u76fe\u7cfb\u7edf";
    }
}

