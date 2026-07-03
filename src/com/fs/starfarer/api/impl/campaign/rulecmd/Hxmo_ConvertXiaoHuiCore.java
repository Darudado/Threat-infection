/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.fleet.FleetMemberType
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class Hxmo_ConvertXiaoHuiCore
extends BaseCommandPlugin {
    private static final String CORE_ID = "xiaohui_core";
    private static final String SHIP_HULL_ID = "bltt";

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        String action = params.get(0).getString(memoryMap);
        if ("core_to_ship".equals(action)) {
            this.convertCoreToShip(dialog);
        } else if ("ship_to_core".equals(action)) {
            this.convertShipToCore(dialog);
        }
        return true;
    }

    private void convertCoreToShip(InteractionDialogAPI dialog) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (cargo.getCommodityQuantity(CORE_ID) < 1.0f) {
            dialog.getTextPanel().addPara("\u4f60\u7684\u8d27\u8231\u4e2d\u6ca1\u6709\u7070\u98ce\u6838\u5fc3\u3002", Misc.getNegativeHighlightColor());
            return;
        }
        if (Global.getSettings().getHullSpec(SHIP_HULL_ID) == null) {
            dialog.getTextPanel().addPara("\u8230\u8239\u6570\u636e\u9519\u8bef\uff0c\u65e0\u6cd5\u8f6c\u6362\u3002", Misc.getNegativeHighlightColor());
            return;
        }
        cargo.removeCommodity(CORE_ID, 1.0f);
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, SHIP_HULL_ID);
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
        dialog.getTextPanel().addPara("\u7070\u98ce\u6838\u5fc3\u53d1\u51fa\u67d4\u548c\u7684\u5149\u8292\uff0c\u7eb3\u7c73\u7c92\u5b50\u8fc5\u901f\u91cd\u7ec4\uff0c\u5316\u4f5c\u4e00\u8258 " + ship.getVariant().getFullDesignationWithHullNameForShip() + " \u52a0\u5165\u4e86\u4f60\u7684\u8230\u961f\u3002", Misc.getPositiveHighlightColor());
    }

    private void convertShipToCore(InteractionDialogAPI dialog) {
        FleetMemberAPI target = null;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (!SHIP_HULL_ID.equals(member.getHullId())) continue;
            target = member;
            break;
        }
        if (target == null) {
            dialog.getTextPanel().addPara("\u4f60\u7684\u8230\u961f\u4e2d\u6ca1\u6709\u53ef\u8f6c\u6362\u7684\u7eb3\u7c73\u8230\u8239\u3002", Misc.getNegativeHighlightColor());
            return;
        }
        Global.getSector().getPlayerFleet().getFleetData().removeFleetMember(target);
        Global.getSector().getPlayerFleet().getCargo().addCommodity(CORE_ID, 1.0f);
        dialog.getTextPanel().addPara(target.getShipName() + " \u5206\u89e3\u4e3a\u7eb3\u7c73\u7c92\u5b50\uff0c\u91cd\u65b0\u51dd\u805a\u6210\u4e00\u679a\u7070\u98ce\u6838\u5fc3\u3002", Misc.getPositiveHighlightColor());
    }
}

