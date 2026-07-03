/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class Hxmo_CheckCoreInstalled
extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (market == null) {
            return false;
        }
        return market.getAdmin() != null && market.getAdmin().getAICoreId() != null && market.getAdmin().getAICoreId().equals("xiaohui_core");
    }
}

