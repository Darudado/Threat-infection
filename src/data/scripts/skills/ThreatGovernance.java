/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.econ.Industry
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.characters.LevelBasedEffect$ScopeDescription
 *  com.fs.starfarer.api.characters.MarketSkillEffect
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
 */
package data.scripts.skills;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;

public class ThreatGovernance {
    private static final float ACCESS_BONUS = 0.2f;
    private static final int STABILITY_BONUS = 2;
    private static final int DEMAND_REDUCTION = 1;
    private static final float UPKEEP_MULT = 0.85f;

    public static class Level1
    implements MarketSkillEffect {
        public void apply(MarketAPI market, String id, float level) {
            market.getAccessibilityMod().modifyFlat(id, 0.2f);
            market.getStability().modifyFlat(id, 2.0f);
            for (Industry ind : market.getIndustries()) {
                if (!(ind instanceof BaseIndustry)) continue;
                BaseIndustry baseInd = (BaseIndustry)ind;
                baseInd.getDemandReductionFromOther().modifyFlat(id, 1.0f, "\u5a01\u80c1\u6cbb\u7406");
            }
            market.getUpkeepMult().modifyMult(id, 0.85f, "\u5a01\u80c1\u6cbb\u7406");
        }

        public void unapply(MarketAPI market, String id) {
            market.getAccessibilityMod().unmodifyFlat(id);
            market.getStability().unmodifyFlat(id);
            for (Industry ind : market.getIndustries()) {
                if (!(ind instanceof BaseIndustry)) continue;
                BaseIndustry baseInd = (BaseIndustry)ind;
                baseInd.getDemandReductionFromOther().unmodifyFlat(id);
            }
            market.getUpkeepMult().unmodifyMult(id);
        }

        public String getEffectDescription(float level) {
            int accessPercent = Math.round(20.0f);
            int upkeepPercent = Math.round(14.999998f);
            return "+" + accessPercent + "% \u6d41\u901a\u6027\uff0c+2 \u7a33\u5b9a\u6027\uff0c\u6240\u6709\u5de5\u4e1a\u8bbe\u65bd\u9700\u6c42 -1\uff0c\u5e02\u573a\u7ef4\u62a4\u6210\u672c\u964d\u4f4e " + upkeepPercent + "%";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.GOVERNED_OUTPOST;
        }
    }
}

