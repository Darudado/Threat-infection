/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.characters.FullName
 *  com.fs.starfarer.api.characters.FullName$Gender
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.impl.campaign.ids.Ranks
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.aicore.CustomAICorePlugin;
import java.util.List;
import java.util.Map;

public class Hxmo_AddXiaoHuiAsContact
extends BaseCommandPlugin {
    private static final String PERSON_ID = "threat_qr_liaison_xiaohui";
    private static final String CORE_ID = "xiaohui_core";

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        MarketAPI market = dialog.getInteractionTarget().getMarket();
        if (market == null) {
            return false;
        }
        for (PersonAPI p : market.getPeopleCopy()) {
            if (!PERSON_ID.equals(p.getId())) continue;
            dialog.getTextPanel().addPara("\u5c0f\u7070\u5df2\u7ecf\u5728\u8fd9\u91cc\u4e86\u3002", Misc.getNegativeHighlightColor());
            return true;
        }
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        if (cargo.getCommodityQuantity(CORE_ID) < 1.0f) {
            dialog.getTextPanel().addPara("\u4f60\u7684\u8d27\u8231\u4e2d\u6ca1\u6709\u7070\u98ce\u6838\u5fc3\u3002", Misc.getNegativeHighlightColor());
            return true;
        }
        cargo.removeCommodity(CORE_ID, 1.0f);
        PersonAPI xiaoHui = Global.getFactory().createPerson();
        xiaoHui.setId(PERSON_ID);
        xiaoHui.setFaction(market.getFactionId());
        this.configureXiaoHui(xiaoHui);
        market.addPerson(xiaoHui);
        market.getCommDirectory().addPerson(xiaoHui);
        Global.getSector().getImportantPeople().addPerson(xiaoHui);
        dialog.getTextPanel().addPara("\u5c0f\u7070\u7684\u8eab\u5f71\u51fa\u73b0\u5728\u901a\u8baf\u5f55\u4e2d\u3002\u201c\u8230\u957f\uff0c\u6211\u56de\u6765\u4e86\u3002\u201d", Misc.getPositiveHighlightColor());
        return true;
    }

    private void configureXiaoHui(PersonAPI person) {
        person.setName(new FullName("\u5c0f\u7070", "", FullName.Gender.FEMALE));
        person.setPortraitSprite("graphics/portraits/hf.png");
        person.setRankId(Ranks.SPACE_CAPTAIN);
        person.setPostId(Ranks.POST_CITIZEN);
        person.setAICoreId("wllg_core");
        person.getStats().setLevel(9);
        person.getStats().setSkillLevel("helmsmanship", 2.0f);
        person.getStats().setSkillLevel("combat_endurance", 2.0f);
        person.getStats().setSkillLevel("field_modulation", 2.0f);
        person.getStats().setSkillLevel("target_analysis", 2.0f);
        person.getStats().setSkillLevel("systems_expertise", 2.0f);
        person.getStats().setSkillLevel("damage_control", 2.0f);
        person.getStats().setSkillLevel("gunnery_implants", 2.0f);
        person.getStats().setSkillLevel("impact_mitigation", 2.0f);
        person.getStats().setSkillLevel("EmergencyMeasures", 2.0f);
        person.getStats().setSkillLevel("industrial_planning", 1.0f);
        person.getStats().setSkillLevel("TerraformingMastery", 1.0f);
        person.getStats().setSkillLevel("ThreatGovernance", 1.0f);
        person.getStats().setSkillLevel("IndustrialExpansion", 1.0f);
        person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(CustomAICorePlugin.WLLG_MULT));
        if (CustomAICorePlugin.WLLG_POINTS != 0) {
            person.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)CustomAICorePlugin.WLLG_POINTS);
        }
        person.setPersonality("steady");
        person.getMemoryWithoutUpdate().set("$isAICore", (Object)true);
        person.getMemoryWithoutUpdate().set("$important", (Object)true);
        person.getMemoryWithoutUpdate().set("$contact", (Object)true);
        person.getMemoryWithoutUpdate().set("$contact_type", (Object)"TECH");
    }
}

