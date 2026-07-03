/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.campaign.econ.Industry
 *  com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin$InstallableItemDescriptionMode
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BoostIndustryInstallableItemEffect
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 */
package data.scripts.effects;

import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BoostIndustryInstallableItemEffect;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class NaniteReconstructorForgeEffect
extends BoostIndustryInstallableItemEffect {
    private static final String ID = "nanite_reconstructor_forge";
    private static final int PRODUCTION_BONUS = 4;
    private static final float QUALITY_BONUS = 0.0f;

    public NaniteReconstructorForgeEffect() {
        super(ID, 4, 0);
    }

    public void apply(Industry industry) {
        super.apply(industry);
        if (industry == null || industry.getMarket() == null) {
            return;
        }
        String[] outputs = this.getOutputsForIndustry(industry.getId());
        if (outputs != null) {
            for (String commodity : outputs) {
                if (industry.getSupply(commodity) == null) continue;
                industry.getSupply(commodity).getQuantity().modifyFlat("nanite_reconstructor_forge_extra_" + commodity, 4.0f, "\u7eb3\u7c73\u91cd\u6784\u953b\u7089\u6548\u7387\u63d0\u5347");
            }
        }
        try {
            String planetType;
            if (industry.getMarket().getPlanetEntity() != null && this.isHabitablePlanet(planetType = industry.getMarket().getPlanetEntity().getSpec().getPlanetType()) && !industry.getMarket().hasCondition("pollution")) {
                industry.getMarket().addCondition("pollution");
                industry.getMarket().getMemoryWithoutUpdate().set("$nanite_forge_pollution_added", (Object)true);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void unapply(Industry industry) {
        if (industry != null && industry.getMarket() != null) {
            String[] outputs;
            if (industry.getMarket().getMemoryWithoutUpdate().getBoolean("$nanite_forge_pollution_added")) {
                industry.getMarket().removeCondition("pollution");
                industry.getMarket().getMemoryWithoutUpdate().unset("$nanite_forge_pollution_added");
            }
            if ((outputs = this.getOutputsForIndustry(industry.getId())) != null) {
                for (String commodity : outputs) {
                    if (industry.getSupply(commodity) == null) continue;
                    industry.getSupply(commodity).getQuantity().unmodify("nanite_reconstructor_forge_extra_" + commodity);
                }
            }
        }
        super.unapply(industry);
    }

    public void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data, InstallableIndustryItemPlugin.InstallableItemDescriptionMode mode, String pre, float pad) {
        if (text == null) {
            return;
        }
        text.addPara("\u5728\u5b9c\u5c45\u661f\u7403\u4e0a\u4f7f\u7528\u65f6\u4f1a\u5bfc\u81f4 %s\u3002", pad, Misc.getNegativeHighlightColor(), new String[]{"\u5de5\u4e1a\u6c61\u67d3"});
        text.addPara("\u53ef\u5b89\u88c5\u4ea7\u4e1a\uff1a\u7eb3\u7c73\u91cd\u6784\u9635\u5217\u3001\u865a\u7a7a\u67a2\u7ebd", pad, Misc.getHighlightColor(), new String[]{"\u7eb3\u7c73\u91cd\u6784\u9635\u5217", "\u865a\u7a7a\u67a2\u7ebd"});
        text.addPara("\u63d0\u9ad8\u5efa\u7b514\u70b9\u4ea7\u80fd", pad, Misc.getHighlightColor(), new String[]{"\u63d0\u9ad8\u5efa\u7b514\u70b9\u4ea7\u80fd"});
    }

    public String[] getSimpleReqs(Industry industry) {
        if (industry != null && !this.isAllowed(industry.getId())) {
            return new String[]{"\u53ea\u80fd\u5b89\u88c5\u5728\u7eb3\u7c73\u91cd\u6784\u9635\u5217\u6216\u865a\u7a7a\u67a2\u7ebd\u4e0a"};
        }
        return super.getSimpleReqs(industry);
    }

    public boolean canBeInstalledIn(Industry industry) {
        return industry != null && this.isAllowed(industry.getId());
    }

    private boolean isAllowed(String industryId) {
        return "nano_reconstruction_array".equals(industryId) || "heavy_production_hub".equals(industryId);
    }

    private String[] getOutputsForIndustry(String industryId) {
        if ("nano_reconstruction_array".equals(industryId)) {
            return new String[]{"heavy_machinery", "hand_weapons", "rare_metals", "metals", "ships", "supplies"};
        }
        if ("heavy_production_hub".equals(industryId)) {
            return new String[]{"heavy_machinery", "supplies", "fuel"};
        }
        return null;
    }

    private boolean isHabitablePlanet(String planetType) {
        if (planetType == null) {
            return false;
        }
        String lower = planetType.toLowerCase();
        return lower.contains("terran") || lower.contains("arid") || lower.contains("jungle") || lower.contains("tundra") || lower.contains("desert") || lower.contains("water") || lower.contains("arctic");
    }
}

