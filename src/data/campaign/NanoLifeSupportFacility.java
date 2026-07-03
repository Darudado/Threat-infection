/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
 *  com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
 *  com.fs.starfarer.api.campaign.econ.Industry$AICoreDescriptionMode
 *  com.fs.starfarer.api.campaign.econ.Industry$IndustryTooltipMode
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Pair
 */
package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;

public class NanoLifeSupportFacility
extends BaseIndustry {
    private static final String NANITES = "nanites";
    private static final float HAZARD_REDUCTION = -0.25f;
    private static final float ALPHA_CORE_EXTRA_REDUCTION = -0.25f;
    private static final int NANITES_DEMAND_MOD = 2;
    private static final int CREW_DEMAND_MOD = -1;
    private static final String HAZARD_MOD_ID_PREFIX = "nano_life_support_hazard";

    public boolean isHidden() {
        return false;
    }

    public boolean isFunctional() {
        if (!super.isFunctional()) {
            return false;
        }
        Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
        return nanitesDeficit == null || (Integer)nanitesDeficit.two <= 0;
    }

    public void apply() {
        Pair nanitesDeficit;
        super.apply(true);
        int size = this.market.getSize();
        this.demand(NANITES, size + 2);
        this.demand("crew", size + -1);
        this.demand("volatiles", Math.max(1, size / 2));
        float mult = this.getDeficitMult(new String[]{NANITES, "crew", "volatiles"});
        Object extra = "";
        if (mult != 1.0f) {
            String com = (String)this.getMaxDeficit((String[])new String[]{NANITES, "crew", "volatiles"}).one;
            extra = " (" + NanoLifeSupportFacility.getDeficitText((String)com).toLowerCase() + ")";
        }
        float actualReduction = -0.25f * mult;
        String hazardModId = "nano_life_support_hazard_" + this.getModId();
        this.market.getHazard().modifyFlat(hazardModId, actualReduction, this.getNameForModifier() + (String)extra);
        if (Global.getSettings().isDevMode()) {
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("NanoLifeSupportFacility: Applied hazard reduction for market %s. ModId: %s, Base Reduction: %.1f%%, Actual Reduction: %.1f%%", this.market.getName(), hazardModId, Float.valueOf(-25.0f), Float.valueOf(actualReduction * 100.0f)));
        }
        if ((nanitesDeficit = this.getMaxDeficit(new String[]{NANITES})) != null && (Integer)nanitesDeficit.two > 0 && !this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        }
        if (!this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        }
    }

    public void unapply() {
        super.unapply();
        String hazardModId = "nano_life_support_hazard_" + this.getModId();
        this.market.getHazard().unmodifyFlat(hazardModId);
        if (Global.getSettings().isDevMode()) {
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("NanoLifeSupportFacility: Removed hazard reduction for market %s. ModId: %s", this.market.getName(), hazardModId));
        }
    }

    protected void applyAlphaCoreModifiers() {
        String alphaCoreModId = "nano_life_support_hazard_alpha_" + this.getModId();
        this.market.getHazard().modifyFlat(alphaCoreModId, -0.25f, "\u963f\u5c14\u6cd5\u6838\u5fc3 (" + this.getNameForModifier() + ")");
        if (Global.getSettings().isDevMode()) {
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("NanoLifeSupportFacility: Applied Alpha Core extra reduction for market %s. Extra Reduction: %.1f%%", this.market.getName(), Float.valueOf(-25.0f)));
        }
    }

    protected void applyNoAICoreModifiers() {
        String alphaCoreModId = "nano_life_support_hazard_alpha_" + this.getModId();
        this.market.getHazard().unmodifyFlat(alphaCoreModId);
    }

    protected void applyBetaCoreModifiers() {
    }

    protected void applyGammaCoreModifiers() {
    }

    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        float opad = 10.0f;
        Color highlight = Misc.getHighlightColor();
        String pre = "\u76ee\u524d\u5df2\u88ab\u5206\u914d\u7684 \u963f\u5c14\u6cd5\u7ea7 AI \u6838\u5fc3\u3002";
        if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "\u963f\u5c14\u6cd5\u7ea7 AI \u6838\u5fc3\u3002";
        }
        float a = Math.abs(-0.25f);
        String str = (int)(a * 100.0f) + "%";
        if (mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0f);
            text.addPara(pre + "\u989d\u5916\u51cf\u5c11 %s \u5371\u9669\u5ea6\u3002", 0.0f, highlight, new String[]{str});
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "\u989d\u5916\u51cf\u5c11 %s \u5371\u9669\u5ea6\u3002", opad, highlight, new String[]{str});
        }
    }

    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional();
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        if (mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional()) {
            this.addHazardImpactSection(tooltip);
        }
    }

    protected void addHazardImpactSection(TooltipMakerAPI tooltip) {
        String[] commodities = new String[]{NANITES, "crew", "volatiles"};
        float mult = this.getDeficitMult(commodities);
        float actualReduction = -0.25f * mult;
        if (this.aiCoreId != null && this.aiCoreId.equals("alpha_core")) {
            actualReduction += -0.25f;
        }
        Object extra = "";
        if (mult != 1.0f) {
            String com = (String)this.getMaxDeficit((String[])commodities).one;
            extra = " (" + NanoLifeSupportFacility.getDeficitText((String)com).toLowerCase() + ")";
        }
        Color h = Misc.getHighlightColor();
        if (mult != 1.0f) {
            h = Misc.getNegativeHighlightColor();
        }
        int reductionPercent = (int)(Math.abs(actualReduction) * 100.0f);
        tooltip.addPara("\u5371\u9669\u5ea6: -%s", 3.0f, h, new String[]{reductionPercent + "%" + (String)extra});
    }

    protected int getBaseStabilityMod() {
        return 1;
    }

    public String getNameForModifier() {
        return this.getSpec().getName().contains("\u8bbe\u65bd") ? this.getSpec().getName() : Misc.ucFirst((String)this.getSpec().getName());
    }

    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return this.getMaxDeficit(new String[]{NANITES, "crew", "volatiles"});
    }

    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    private boolean isNonHabitablePlanet() {
        String[] habitableTypes;
        if (this.market.getPlanetEntity() == null) {
            return true;
        }
        String planetType = this.market.getPlanetEntity().getTypeId();
        for (String habitableType : habitableTypes = new String[]{"terran", "arid", "jungle", "tundra", "desert", "water", "arctic"}) {
            if (!planetType.contains(habitableType)) continue;
            return false;
        }
        return true;
    }

    public boolean isAvailableToBuild() {
        if (!Global.getSector().getPlayerFaction().knowsIndustry(this.getId())) {
            return false;
        }
        if (!this.market.getFaction().isPlayerFaction()) {
            return false;
        }
        return this.isNonHabitablePlanet();
    }

    public String getUnavailableReason() {
        if (!Global.getSector().getPlayerFaction().knowsIndustry(this.getId())) {
            return "\u9700\u8981\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u84dd\u56fe";
        }
        if (!this.market.getFaction().isPlayerFaction()) {
            return "\u53ea\u80fd\u5efa\u9020\u5728\u73a9\u5bb6\u63a7\u5236\u7684\u5e02\u573a";
        }
        if (!this.isNonHabitablePlanet()) {
            return "\u53ea\u80fd\u5efa\u9020\u5728\u975e\u5b9c\u5c45\u661f\u7403\u4e0a";
        }
        Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
        if (nanitesDeficit != null && (Integer)nanitesDeficit.two > 0) {
            return "\u7eb3\u7c73\u788e\u7247\u4f9b\u5e94\u4e0d\u8db3\uff08\u77ed\u7f3a" + String.valueOf(nanitesDeficit.two) + "\u5355\u4f4d\uff09";
        }
        return super.getUnavailableReason();
    }

    public boolean showWhenUnavailable() {
        return Global.getSector().getPlayerFaction().knowsIndustry(this.getId());
    }

    public boolean canImprove() {
        return false;
    }

    public boolean hasEffects() {
        return true;
    }

    public boolean isBeneficial() {
        return true;
    }

    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        super.addPostDescriptionSection(tooltip, mode);
        if (mode == Industry.IndustryTooltipMode.NORMAL && this.isFunctional()) {
            float opad = 10.0f;
            tooltip.addPara("\u8be5\u5efa\u7b51\u901a\u8fc7\u5148\u8fdb\u7684\u7eb3\u7c73\u6280\u672f\u6539\u5584\u6b96\u6c11\u5730\u73af\u5883\uff0c\u63d0\u4f9b25%%\u5371\u9669\u5ea6\u51cf\u5c11\u3002", opad, Misc.getHighlightColor(), new String[]{"25%"});
            tooltip.addPara("\u53ea\u80fd\u5efa\u9020\u5728\u975e\u5b9c\u5c45\u661f\u7403\u4e0a\uff0c\u7528\u4e8e\u6539\u5584\u6076\u52a3\u73af\u5883\u7684\u6b96\u6c11\u5730\u6761\u4ef6\u3002", Misc.getGrayColor(), opad);
        }
    }
}

