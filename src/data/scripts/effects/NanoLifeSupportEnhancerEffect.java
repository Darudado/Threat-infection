/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.campaign.econ.Industry
 *  com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin$InstallableItemDescriptionMode
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableItemEffect
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 */
package data.scripts.effects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableItemEffect;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NanoLifeSupportEnhancerEffect
extends BaseInstallableItemEffect {
    private static final String ID = "nano_life_support_enhancer";
    private static final String REQUIRED_INDUSTRY_ID = "nanolifesupportfacility";
    private static final float EXTRA_HAZARD_REDUCTION = -0.5f;
    private static final float ACCESSIBILITY_BONUS = 0.25f;

    public NanoLifeSupportEnhancerEffect() {
        super(ID);
    }

    public void apply(Industry industry) {
        if (industry != null && industry.getMarket() != null) {
            industry.getMarket().getHazard().modifyFlat("nano_life_support_enhancer_hazard", -0.5f, "\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668");
            industry.getMarket().getAccessibilityMod().modifyFlat("nano_life_support_enhancer_access", 0.25f, "\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668");
            if (Global.getSettings().isDevMode()) {
                Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("Applied NanoLifeSupportEnhancer: -%.1f%% hazard, +%.1f%% accessibility", Float.valueOf(-50.0f), Float.valueOf(25.0f)));
            }
        }
    }

    public void unapply(Industry industry) {
        if (industry != null && industry.getMarket() != null) {
            industry.getMarket().getHazard().unmodifyFlat("nano_life_support_enhancer_hazard");
            industry.getMarket().getAccessibilityMod().unmodifyFlat("nano_life_support_enhancer_access");
        }
    }

    protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data, InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
        if (text == null) {
            return;
        }
        if (mode != null && mode.equals((Object)InstallableIndustryItemPlugin.InstallableItemDescriptionMode.CARGO_TOOLTIP)) {
            text.addPara(pre + "\u4e3a\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u989d\u5916\u51cf\u5c11}%s}\u5371\u9669\u5ea6\uff0c\u5e76\u589e\u52a0}%s}\u6d41\u901a\u6027", pad, Misc.getPositiveHighlightColor(), new String[]{"50%", "25%"});
        } else {
            text.addPara("\u4e3a\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u989d\u5916\u51cf\u5c11}%s}\u5371\u9669\u5ea6\uff0c\u5e76\u589e\u52a0}%s}\u6d41\u901a\u6027", pad, Misc.getPositiveHighlightColor(), new String[]{"50%", "25%"});
        }
    }

    public String[] getSimpleReqs(Industry industry) {
        if (industry != null && !REQUIRED_INDUSTRY_ID.equals(industry.getId())) {
            return new String[]{"\u53ea\u80fd\u5b89\u88c5\u5728\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u4e0a"};
        }
        return super.getSimpleReqs(industry);
    }

    public boolean canBeInstalledIn(Industry industry) {
        if (industry == null) {
            return false;
        }
        String industryId = industry.getId();
        return industryId != null && industryId.equals(REQUIRED_INDUSTRY_ID);
    }
}

