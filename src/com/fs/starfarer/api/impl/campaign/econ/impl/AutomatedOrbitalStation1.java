/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.econ.Industry$IndustryTooltipMode
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Pair
 */
package com.fs.starfarer.api.impl.campaign.econ.impl;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.Color;
import java.util.ArrayList;

public class AutomatedOrbitalStation1
extends OrbitalStation {
    public static float NANITES_MULTIPLIER = 1.0f;

    public void apply() {
        super.apply();
        if (this.isFunctional()) {
            int size = 3;
            boolean battlestation = this.getSpec().hasTag("battlestation");
            boolean starfortress = this.getSpec().hasTag("starfortress");
            if (battlestation) {
                size = 5;
            } else if (starfortress) {
                size = 7;
            }
            int nanitesDemand = Math.max(1, (int)((float)size * NANITES_MULTIPLIER));
            this.demand("nanites", nanitesDemand);
            this.recalculateCRWithNanites();
            this.updateDefenseBonusTooltip();
        }
    }

    protected float getCR() {
        float cr;
        float d;
        float q;
        ArrayList<String> criticalResources = new ArrayList<String>();
        criticalResources.add("crew");
        criticalResources.add("supplies");
        criticalResources.add("nanites");
        int maxDeficit = 0;
        float totalDemand = 0.0f;
        for (String resource : criticalResources) {
            Pair deficitPair = this.getMaxDeficit(new String[]{resource});
            int deficit = (Integer)deficitPair.two;
            if (deficit < 0) {
                deficit = 0;
            }
            int demand = this.getDemand(resource).getQuantity().getModifiedInt();
            totalDemand += (float)demand;
            if (deficit <= maxDeficit) continue;
            maxDeficit = deficit;
        }
        float demand = totalDemand;
        float deficit = maxDeficit;
        if (deficit < 0.0f) {
            deficit = 0.0f;
        }
        if (demand < 1.0f) {
            demand = 1.0f;
            deficit = 0.0f;
        }
        if ((q = Misc.getShipQuality((MarketAPI)this.market)) < 0.0f) {
            q = 0.0f;
        }
        if (q > 1.0f) {
            q = 1.0f;
        }
        if ((d = (demand - deficit) / demand) < 0.0f) {
            d = 0.0f;
        }
        if (d > 1.0f) {
            d = 1.0f;
        }
        if ((cr = 0.5f + 0.5f * Math.min(d, q)) > 1.0f) {
            cr = 1.0f;
        }
        return cr;
    }

    private void recalculateCRWithNanites() {
        this.applyCRToStation();
    }

    private void updateDefenseBonusTooltip() {
        float mult = this.getDeficitMult(new String[]{"supplies", "nanites"});
        if (mult != 1.0f) {
            Pair maxDeficitPair = this.getMaxDeficit(new String[]{"supplies", "nanites"});
            String com = (String)maxDeficitPair.one;
            this.market.getStats().getDynamic().getMod("ground_defenses_mod").unmodifyMult(this.getModId());
            float bonus = this.getDefenseBonus();
            String extra = " (" + AutomatedOrbitalStation1.getDeficitText((String)com).toLowerCase() + ")";
            this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(this.getModId(), 1.0f + bonus * mult, this.getNameForModifier() + extra);
        }
    }

    private float getDefenseBonus() {
        boolean battlestation = this.getSpec().hasTag("battlestation");
        boolean starfortress = this.getSpec().hasTag("starfortress");
        if (battlestation) {
            return DEFENSE_BONUS_BATTLESTATION;
        }
        if (starfortress) {
            return DEFENSE_BONUS_FORTRESS;
        }
        return DEFENSE_BONUS_BASE;
    }

    public void unapply() {
        this.supply.remove("nanites");
        this.demand.remove("nanites");
        super.unapply();
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        super.addPostDemandSection(tooltip, hasDemand, mode);
        if (mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional()) {
            Color h = Misc.getHighlightColor();
            float opad = 10.0f;
            Pair nanitesDeficit = this.getMaxDeficit(new String[]{"nanites"});
            if ((Integer)nanitesDeficit.two > 0) {
                tooltip.addPara("\u7531\u4e8e\u7eb3\u7c73\u673a\u5668\u4eba\u77ed\u7f3a\uff0c\u7a7a\u95f4\u7ad9\u7684\u81ea\u52a8\u5316\u7cfb\u7edf\u8fd0\u884c\u6548\u7387\u964d\u4f4e\u3002", Misc.getNegativeHighlightColor(), opad);
            }
            tooltip.addPara("\u8be5\u8f68\u9053\u7ad9\u4f7f\u7528\u7eb3\u7c73\u673a\u5668\u4eba\u8fdb\u884c\u81ea\u52a8\u5316\u7ef4\u62a4\u548c\u4fee\u590d\uff0c\u63d0\u9ad8\u4e86\u6574\u4f53\u6548\u7387\u3002", Misc.getPositiveHighlightColor(), opad);
        }
    }

    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return this.getMaxDeficit(new String[]{"supplies", "crew", "nanites"});
    }
}

