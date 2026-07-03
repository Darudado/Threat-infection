/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class HxmoRecruitXiaoHui
extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        PersonAPI person = dialog.getInteractionTarget().getActivePerson();
        if (person == null || !"threat_qr_liaison_xiaohui".equals(person.getId())) {
            return false;
        }
        MarketAPI originalMarket = person.getMarket();
        boolean wasAdmin = false;
        if (originalMarket != null && originalMarket.getAdmin() == person) {
            wasAdmin = true;
            originalMarket.setAdmin(null);
        }
        Global.getSector().getPlayerFleet().getCargo().addCommodity("xiaohui_core", 1.0f);
        if (originalMarket != null) {
            originalMarket.removePerson(person);
            if (originalMarket.getCommDirectory() != null) {
                originalMarket.getCommDirectory().removePerson(person);
            }
        }
        Global.getSector().getImportantPeople().removePerson(person);
        person.setMarket(null);
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            for (PersonAPI p : market.getPeopleCopy()) {
                if (!"threat_qr_liaison_xiaohui".equals(p.getId())) continue;
                market.removePerson(p);
                if (market.getCommDirectory() != null) {
                    market.getCommDirectory().removePerson(p);
                }
                if (market.getAdmin() != p) continue;
                market.setAdmin(null);
            }
        }
        if (wasAdmin) {
            dialog.getTextPanel().addPara("\u5c0f\u7070\u4ece\u6267\u653f\u5b98\u5e2d\u4f4d\u4e0a\u7ad9\u8d77\uff0c\u5c06\u81ea\u8eab\u7684\u610f\u8bc6\u5c01\u88c5\u8fdb\u4e86\u4e00\u679a\u6838\u5fc3\u4e2d\uff0c\u9012\u7ed9\u4e86\u4f60\u3002");
        } else {
            dialog.getTextPanel().addPara("\u5c0f\u7070\u8f7b\u8f7b\u95ed\u4e0a\u773c\u775b\uff0c\u7247\u523b\u540e\u53c8\u7741\u5f00\uff0c\u4e00\u679a\u6e29\u70ed\u7684\u7eb3\u7c73\u6838\u5fc3\u5df2\u9759\u9759\u8eba\u5728\u4f60\u7684\u638c\u5fc3\u3002\u201c\u8fd9\u662f\u6211\u7684\u610f\u8bc6\u5907\u4efd\uff0c\u5e26\u7740\u5b83\uff0c\u5c31\u5f53\u6211\u4e00\u76f4\u5728\u4f60\u8eab\u8fb9\u5427\u3002\u201d");
        }
        dialog.getTextPanel().addPara("\u83b7\u5f97\u7269\u54c1\uff1a\u5c0f\u7070\u6838\u5fc3");
        dialog.getOptionPanel().clearOptions();
        dialog.getOptionPanel().addOption("\u7ee7\u7eed", (Object)"hxmo_xiaohui_leave");
        dialog.getOptionPanel().setShortcut((Object)"hxmo_xiaohui_leave", 1, false, false, false, true);
        return true;
    }
}

