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

public class FluxCapacitorOvercharge
extends BaseHullMod {
    private static final float FLUX_CAPACITY_BONUS = 10.0f;
    private static final float FLUX_DISSIPATION_BONUS = 10.0f;
    private static final float VENT_RATE_BONUS = 25.0f;
    private static final float EMP_RESISTANCE = 25.0f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().modifyPercent(id, 10.0f);
        stats.getFluxDissipation().modifyPercent(id, 10.0f);
        stats.getVentRateMult().modifyPercent(id, 25.0f);
        stats.getEmpDamageTakenMult().modifyMult(id, 0.75f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        switch (index) {
            case 0: {
                return "10%";
            }
            case 1: {
                return "10%";
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
}

