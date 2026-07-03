/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class Hxmo_HasCoreForContact
extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity("xiaohui_core") >= 1.0f;
    }
}

