/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.BaseModPlugin
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CampaignPlugin
 *  com.fs.starfarer.api.campaign.FactionAPI
 *  com.fs.starfarer.api.campaign.GenericPluginManagerAPI$GenericPlugin
 *  com.fs.starfarer.api.campaign.RepLevel
 *  com.fs.starfarer.api.campaign.SectorAPI
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.characters.FullName
 *  com.fs.starfarer.api.characters.FullName$Gender
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition
 *  com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
 */
package data;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import data.scripts.campaign.aicore.CustomAICorePlugin;
import data.scripts.campaign.aicore.ThreatAICoreCampaignPlugin;
import data.scripts.campaign.fleets.Threat_qr_FleetGenPlugin;
import data.scripts.effects.NaniteReconstructorForgeEffect;
import data.scripts.effects.NanoLifeSupportEnhancerEffect;
import data.world.threat_NEXGenerate;
import data.world.threat_NormalGenerate;

public class HdbyunmModPlugin
extends BaseModPlugin {
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
        this.setupNanitesResource();
        this.registerNaniteReconstructorForge();
        this.registerNanoLifeSupportEnhancer();
    }

    public void onGameLoad(boolean newGame) {
        Global.getSector().registerPlugin((CampaignPlugin)new ThreatAICoreCampaignPlugin());
        Global.getSector().getGenericPlugins().addPlugin((GenericPluginManagerAPI.GenericPlugin)new Threat_qr_FleetGenPlugin(), true);
        this.adjustThreatQrRelations(Global.getSector());
        if (Global.getSector().getStarSystem("NOVA_REGULA") == null) {
            new threat_NormalGenerate().generate(Global.getSector());
        }
    }

    public void onNewGame() {
        Global.getSector().registerPlugin((CampaignPlugin)new ThreatAICoreCampaignPlugin());
        Global.getSector().getGenericPlugins().addPlugin((GenericPluginManagerAPI.GenericPlugin)new Threat_qr_FleetGenPlugin(), true);
        if (HdbyunmModPlugin.isNexerelinEnabled()) {
            new threat_NEXGenerate().generate(Global.getSector());
        } else {
            new threat_NormalGenerate().generate(Global.getSector());
        }
    }

    public void onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad();
        this.addThreatQrLiaison();
    }

    private void addThreatQrLiaison() {
        String targetMarketId = "nova_regula_4";
        MarketAPI market = Global.getSector().getEconomy().getMarket(targetMarketId);
        if (market == null) {
            Global.getLogger(HdbyunmModPlugin.class).warn((Object)("\u672a\u627e\u5230\u5e02\u573a " + targetMarketId + "\uff0c\u8054\u7edc\u4eba\u5c0f\u7070\u521b\u5efa\u5931\u8d25"));
            return;
        }
        PersonAPI xiaoHui = Global.getFactory().createPerson();
        xiaoHui.setId("threat_qr_liaison_xiaohui");
        xiaoHui.setFaction("threat_qr");
        xiaoHui.setName(new FullName("\u5c0f\u7070", "", FullName.Gender.FEMALE));
        xiaoHui.setPortraitSprite("graphics/portraits/hf.png");
        xiaoHui.setRankId("threat_liaison01");
        xiaoHui.setPostId("threat_liaison02");
        xiaoHui.setAICoreId("wllg_core");
        xiaoHui.getStats().setLevel(9);
        xiaoHui.getStats().setSkillLevel("helmsmanship", 2.0f);
        xiaoHui.getStats().setSkillLevel("combat_endurance", 2.0f);
        xiaoHui.getStats().setSkillLevel("field_modulation", 2.0f);
        xiaoHui.getStats().setSkillLevel("target_analysis", 2.0f);
        xiaoHui.getStats().setSkillLevel("systems_expertise", 2.0f);
        xiaoHui.getStats().setSkillLevel("damage_control", 2.0f);
        xiaoHui.getStats().setSkillLevel("gunnery_implants", 2.0f);
        xiaoHui.getStats().setSkillLevel("impact_mitigation", 2.0f);
        xiaoHui.getStats().setSkillLevel("EmergencyMeasures", 2.0f);
        xiaoHui.getStats().setSkillLevel("industrial_planning", 1.0f);
        xiaoHui.getStats().setSkillLevel("TerraformingMastery", 1.0f);
        xiaoHui.getStats().setSkillLevel("ThreatGovernance", 1.0f);
        xiaoHui.getStats().setSkillLevel("IndustrialExpansion", 1.0f);
        xiaoHui.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(CustomAICorePlugin.WLLG_MULT));
        if (CustomAICorePlugin.WLLG_POINTS != 0) {
            xiaoHui.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)CustomAICorePlugin.WLLG_POINTS);
        }
        xiaoHui.setPersonality("steady");
        xiaoHui.getMemoryWithoutUpdate().set("$important", (Object)true);
        market.addPerson(xiaoHui);
        market.getCommDirectory().addPerson(xiaoHui);
        Global.getSector().getImportantPeople().addPerson(xiaoHui);
        Global.getLogger(HdbyunmModPlugin.class).info((Object)("\u7279\u6b8a\u8054\u7edc\u4eba\u201c\u5c0f\u7070\u201d\u5df2\u6210\u529f\u6dfb\u52a0\u5230\u5e02\u573a " + market.getName()));
    }

    private static boolean isNexerelinEnabled() {
        return Global.getSettings().getModManager().isModEnabled("nexerelin");
    }

    private void adjustThreatQrRelations(SectorAPI sector) {
        String[] hostileFactions;
        FactionAPI threatFaction = sector.getFaction("threat_qr");
        if (threatFaction == null) {
            Global.getLogger(HdbyunmModPlugin.class).warn((Object)"threat_qr \u52bf\u529b\u4e0d\u5b58\u5728\uff0c\u65e0\u6cd5\u8bbe\u7f6e\u5173\u7cfb");
            return;
        }
        for (String factionId : hostileFactions = new String[]{"tutorial", "remnant", "pirates", "neutral", "independent", "hegemony", "tritachyon", "luddic_church", "luddic_path", "persean", "sindrian_diktat"}) {
            FactionAPI other = sector.getFaction(factionId);
            if (other == null) continue;
            threatFaction.setRelationship(factionId, RepLevel.HOSTILE);
        }
        Global.getLogger(HdbyunmModPlugin.class).info((Object)"threat_qr \u521d\u59cb\u5173\u7cfb\u5df2\u8bbe\u7f6e\u4e3a\u654c\u5bf9");
    }

    private void setupNanitesResource() {
        ResourceDepositsCondition.COMMODITY.put("nanites_0", "nanites");
        ResourceDepositsCondition.COMMODITY.put("nanites_1", "nanites");
        ResourceDepositsCondition.COMMODITY.put("nanites_2", "nanites");
        ResourceDepositsCondition.BASE_MODIFIER.put("nanites", 0);
        ResourceDepositsCondition.MODIFIER.put("nanites_0", -3);
        ResourceDepositsCondition.MODIFIER.put("nanites_1", 1);
        ResourceDepositsCondition.MODIFIER.put("nanites_2", 3);
        ResourceDepositsCondition.INDUSTRY.put("nanites", "mining");
    }

    private void registerNaniteReconstructorForge() {
        ItemEffectsRepo.ITEM_EFFECTS.put("nanite_reconstructor_forge", new NaniteReconstructorForgeEffect());
    }

    private void registerNanoLifeSupportEnhancer() {
        ItemEffectsRepo.ITEM_EFFECTS.put("nano_life_support_enhancer", new NanoLifeSupportEnhancerEffect());
    }
}

