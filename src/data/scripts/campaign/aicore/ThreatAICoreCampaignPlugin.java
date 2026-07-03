/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.PluginPick
 *  com.fs.starfarer.api.campaign.AICoreAdminPlugin
 *  com.fs.starfarer.api.campaign.AICoreOfficerPlugin
 *  com.fs.starfarer.api.campaign.BaseCampaignPlugin
 *  com.fs.starfarer.api.campaign.CampaignPlugin$PickPriority
 */
package data.scripts.campaign.aicore;

import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import data.scripts.campaign.aicore.CustomAICorePlugin;

public class ThreatAICoreCampaignPlugin
extends BaseCampaignPlugin {
    public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
        if ("cl_core".equals(commodityId) || "hl_core".equals(commodityId) || "wllg_core".equals(commodityId) || "xiaohui_core".equals(commodityId)) {
            return new PluginPick((Object)new CustomAICorePlugin(), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }

    public PluginPick<AICoreAdminPlugin> pickAICoreAdminPlugin(String commodityId) {
        if ("cl_core".equals(commodityId) || "hl_core".equals(commodityId) || "wllg_core".equals(commodityId) || "xiaohui_core".equals(commodityId)) {
            return new PluginPick((Object)new CustomAICorePlugin(), CampaignPlugin.PickPriority.MOD_SET);
        }
        return null;
    }
}

