/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class Hxmo_CanAccessNaniteMenu
extends BaseCommandPlugin {
    private static final String CORE_ID = "xiaohui_core";
    private static final String SHIP_HULL_ID = "bltt";

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (cargo.getCommodityQuantity(CORE_ID) >= 1.0f) {
            return true;
        }
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (!SHIP_HULL_ID.equals(member.getHullId())) continue;
            return true;
        }
        return false;
    }
}

