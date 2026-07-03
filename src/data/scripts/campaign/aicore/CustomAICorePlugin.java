/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.AICoreAdminPlugin
 *  com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
 *  com.fs.starfarer.api.characters.FullName
 *  com.fs.starfarer.api.characters.FullName$Gender
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl
 *  com.fs.starfarer.api.impl.campaign.ids.Ranks
 *  com.fs.starfarer.api.ui.Alignment
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 */
package data.scripts.campaign.aicore;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreAdminPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.awt.Color;
import java.util.Random;

public class CustomAICorePlugin
extends BaseAICoreOfficerPluginImpl
implements AICoreAdminPlugin {
    public static final String CORE_CL = "cl_core";
    public static final String CORE_HL = "hl_core";
    public static final String CORE_WLLG = "wllg_core";
    public static final String CORE_XIAOHUI = "xiaohui_core";
    public static int CL_POINTS = 0;
    public static int HL_POINTS = 0;
    public static int WLLG_POINTS = 0;
    public static int XIAOHUI_POINTS = 0;
    public static float CL_MULT = 2.0f;
    public static float HL_MULT = 3.0f;
    public static float WLLG_MULT = 4.0f;
    public static float XIAOHUI_MULT = 4.0f;

    public String getAICoreId() {
        return null;
    }

    public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
        if (!(CORE_CL.equals(aiCoreId) || CORE_HL.equals(aiCoreId) || CORE_WLLG.equals(aiCoreId) || CORE_XIAOHUI.equals(aiCoreId))) {
            return null;
        }
        PersonAPI person = Global.getFactory().createPerson();
        person.setFaction(factionId);
        person.setAICoreId(aiCoreId);
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
        person.setName(new FullName(spec.getName(), "", FullName.Gender.ANY));
        person.getStats().setSkipRefresh(true);
        if (CORE_CL.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/cl_core.png");
            person.getStats().setLevel(5);
            person.getStats().setSkillLevel("helmsmanship", 2.0f);
            person.getStats().setSkillLevel("combat_endurance", 2.0f);
            person.getStats().setSkillLevel("field_modulation", 2.0f);
            person.getStats().setSkillLevel("target_analysis", 2.0f);
            person.getStats().setSkillLevel("EmergencyMeasures", 1.0f);
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(CL_MULT));
            if (CL_POINTS != 0) {
                person.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)CL_POINTS);
            }
            person.setPersonality("cautious");
        } else if (CORE_HL.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/hl_core.png");
            person.getStats().setLevel(6);
            person.getStats().setSkillLevel("helmsmanship", 2.0f);
            person.getStats().setSkillLevel("combat_endurance", 2.0f);
            person.getStats().setSkillLevel("field_modulation", 2.0f);
            person.getStats().setSkillLevel("target_analysis", 2.0f);
            person.getStats().setSkillLevel("systems_expertise", 2.0f);
            person.getStats().setSkillLevel("EmergencyMeasures", 2.0f);
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(HL_MULT));
            if (HL_POINTS != 0) {
                person.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)HL_POINTS);
            }
            person.setPersonality("steady");
        } else if (CORE_WLLG.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/wllg_core.png");
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
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(WLLG_MULT));
            if (WLLG_POINTS != 0) {
                person.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)WLLG_POINTS);
            }
            person.setPersonality("steady");
        } else if (CORE_XIAOHUI.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/hf.png");
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
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(XIAOHUI_MULT));
            if (XIAOHUI_POINTS != 0) {
                person.getMemoryWithoutUpdate().set("$autoPointsValue", (Object)XIAOHUI_POINTS);
            }
            person.setPersonality("steady");
        }
        person.setRankId(Ranks.SPACE_CAPTAIN);
        person.setPostId(null);
        person.getMemoryWithoutUpdate().set("$isAICore", (Object)true);
        person.getStats().setSkipRefresh(false);
        return person;
    }

    public PersonAPI createPerson(String aiCoreId, String factionId, long seed) {
        if (!(CORE_CL.equals(aiCoreId) || CORE_HL.equals(aiCoreId) || CORE_WLLG.equals(aiCoreId) || CORE_XIAOHUI.equals(aiCoreId))) {
            return null;
        }
        PersonAPI person = Global.getFactory().createPerson();
        person.setFaction(factionId);
        person.setAICoreId(aiCoreId);
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
        person.setName(new FullName(spec.getName(), "", FullName.Gender.ANY));
        person.getStats().setSkipRefresh(true);
        if (CORE_CL.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/cl_core.png");
        } else if (CORE_HL.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/hl_core.png");
            person.getStats().setSkillLevel("industrial_planning", 1.0f);
            person.getStats().setSkillLevel("TerraformingMastery", 1.0f);
        } else if (CORE_WLLG.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/wllg_core.png");
            person.getStats().setSkillLevel("industrial_planning", 1.0f);
            person.getStats().setSkillLevel("TerraformingMastery", 1.0f);
            person.getStats().setSkillLevel("ThreatGovernance", 1.0f);
            person.getStats().setSkillLevel("IndustrialExpansion", 1.0f);
        } else if (CORE_XIAOHUI.equals(aiCoreId)) {
            person.setPortraitSprite("graphics/portraits/hf.png");
            person.getStats().setSkillLevel("industrial_planning", 1.0f);
            person.getStats().setSkillLevel("TerraformingMastery", 1.0f);
            person.getStats().setSkillLevel("ThreatGovernance", 1.0f);
            person.getStats().setSkillLevel("IndustrialExpansion", 1.0f);
        }
        person.setRankId(null);
        person.setPostId(Ranks.POST_ADMINISTRATOR);
        person.getMemoryWithoutUpdate().set("$isAICore", (Object)true);
        if (CORE_CL.equals(aiCoreId)) {
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(CL_MULT));
        } else if (CORE_HL.equals(aiCoreId)) {
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(HL_MULT));
        } else if (CORE_WLLG.equals(aiCoreId)) {
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(WLLG_MULT));
        } else if (CORE_XIAOHUI.equals(aiCoreId)) {
            person.getMemoryWithoutUpdate().set("$autoPointsMult", (Object)Float.valueOf(XIAOHUI_MULT));
        }
        person.getStats().setSkipRefresh(false);
        return person;
    }

    public void createPersonalitySection(PersonAPI person, TooltipMakerAPI tooltip) {
        Object description;
        String heading;
        String aiCoreId = person.getAICoreId();
        if (aiCoreId == null) {
            return;
        }
        Color text = person.getFaction().getBaseUIColor();
        Color bg = person.getFaction().getDarkUIColor();
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(aiCoreId);
        if (CORE_CL.equals(aiCoreId)) {
            heading = "\u6027\u683c: \u9519\u4e71\u9632\u5fa1";
            description = spec.getName() + "\u7684\u9632\u5fa1\u534f\u8bae\u88ab\u9519\u8bef\u5f3a\u5316\uff0c\u5bfc\u81f4\u5176\u5728\u6218\u6597\u4e2d\u8fc7\u5ea6\u5173\u6ce8\u751f\u5b58\u800c\u9519\u5931\u8fdb\u653b\u673a\u4f1a\uff0c\u4f46\u751f\u5b58\u80fd\u529b\u663e\u8457\u63d0\u5347\u3002";
        } else if (CORE_HL.equals(aiCoreId)) {
            heading = "\u6027\u683c: \u6df7\u4e71\u8ba1\u7b97";
            description = spec.getName() + "\u7684\u8fd0\u7b97\u903b\u8f91\u4e2d\u5b58\u5728\u523b\u610f\u5f15\u5165\u7684\u968f\u673a\u6270\u52a8\uff0c\u4f7f\u5176\u5728\u6218\u6597\u4e2d\u8868\u73b0\u51fa\u4e0d\u53ef\u9884\u6d4b\u7684\u653b\u51fb\u6a21\u5f0f\uff0c\u4f46\u6838\u5fc3\u6218\u672f\u4ecd\u7136\u4fdd\u6301\u7406\u6027\u3002";
        } else if (CORE_WLLG.equals(aiCoreId)) {
            heading = "\u6027\u683c: \u6d41\u5149\u7d0a\u4e71";
            description = spec.getName() + "\u7684\u6838\u5fc3\u8fd0\u7b97\u4e2d\u6df7\u5165\u4e86\u4f4d\u9519\u6d41\u5149\u7684\u5e72\u6270\uff0c\u4f7f\u6218\u672f\u51b3\u7b56\u5728\u9ad8\u6548\u4e0e\u6df7\u4e71\u4e4b\u95f4\u5feb\u901f\u5207\u6362\uff0c\u4f46\u603b\u4f53\u4ecd\u4fdd\u6301\u7406\u6027\u3002";
        } else if (CORE_XIAOHUI.equals(aiCoreId)) {
            heading = "\u6027\u683c: \u76ee\u89c6\u5bf0\u5b87";
            description = "\u5979\u5c06\u610f\u8bc6\u5347\u534e\u4e3a\u4fef\u77b0\u661f\u6d77\u7684\u53cc\u773c\uff0c\u7fa4\u661f\u95f4\u7684\u6bcf\u4e00\u6b21\u95ea\u70c1\u3001\u6bcf\u4e00\u9053\u5c3e\u7130\u90fd\u9003\u4e0d\u8fc7\u5979\u7684\u6ce8\u89c6\u3002\u6218\u672f\u4e8e\u5979\u800c\u8a00\uff0c\u4e0d\u8fc7\u662f\u4e00\u573a\u65e9\u5df2\u6d1e\u6089\u7ed3\u5c40\u7684\u68cb\u5c40\u3002";
        } else {
            return;
        }
        tooltip.addSectionHeading(heading, text, bg, Alignment.MID, 20.0f);
        tooltip.addPara((String)description, 10.0f);
    }
}

