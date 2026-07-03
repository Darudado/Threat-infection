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
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 */
package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class FragmentCoordinatorHullmod233
extends BaseHullMod {
    public static float SIZE_INCREASE = 120.0f;
    public static float SMOD_SIZE_INCREASE = 100.0f;

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod("fragment_swarm_size_mod").modifyPercent(id, SIZE_INCREASE);
        boolean sMod = this.isSMod(stats);
        if (sMod) {
            stats.getDynamic().getMod("fragment_swarm_size_mod").modifyPercent(id, SIZE_INCREASE + SMOD_SIZE_INCREASE);
        }
    }

    public CargoStackAPI getRequiredItem() {
        return Global.getSettings().createCargoStack(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData("threat_processing_unit", (String)null), (CargoAPI)null);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return index == 0 ? (int)SIZE_INCREASE + "%" : null;
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        return index == 0 ? (int)SMOD_SIZE_INCREASE + "%" : null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        boolean hasOriginal = ship.getVariant().getHullMods().contains("fragment_coordinator");
        return !hasOriginal && super.isApplicableToShip(ship);
    }

    public String getUnapplicableReason(ShipAPI ship) {
        boolean hasOriginal = ship.getVariant().getHullMods().contains("fragment_coordinator");
        return hasOriginal ? "\u4e0e\u788e\u7247\u534f\u8c03\u5668\u63d2\u4ef6\u4e0d\u517c\u5bb9" : super.getUnapplicableReason(ship);
    }
}

