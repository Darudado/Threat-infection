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

public class applyEffectsBeforeShipCreation
extends BaseHullMod {
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getOverloadTimeMod().modifyMult(id, 0.75f);
        stats.getVentRateMult().modifyPercent(id, 200.0f);
        stats.getEmpDamageTakenMult().modifyMult(id, 0.75f);
        stats.getBeamDamageTakenMult().modifyMult(id, 0.75f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        switch (index) {
            case 0: {
                return "25%";
            }
            case 1: {
                return "200%";
            }
            case 2: {
                return "25%";
            }
            case 3: {
                return "25%";
            }
        }
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
}

