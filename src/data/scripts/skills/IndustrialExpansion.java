/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier
 *  com.fs.starfarer.api.characters.LevelBasedEffect$ScopeDescription
 *  com.fs.starfarer.api.characters.MarketSkillEffect
 *  com.fs.starfarer.api.impl.campaign.population.PopulationComposition
 */
package data.scripts.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;

public class IndustrialExpansion
implements MarketImmigrationModifier {
    public static int SIZE_BONUS = 2;
    public static float INCOME_BONUS_PERCENT = 25.0f;

    public void modifyIncoming(MarketAPI marketAPI, PopulationComposition populationComposition) {
    }

    public static class Level1
    implements MarketSkillEffect,
    MarketImmigrationModifier {
        public void apply(MarketAPI market, String id, float level) {
            market.getIncomeMult().modifyPercent(id, INCOME_BONUS_PERCENT);
            market.getStats().getDynamic().getMod("max_market_size").modifyFlat(id, (float)SIZE_BONUS);
            market.addImmigrationModifier((MarketImmigrationModifier)this);
        }

        public void unapply(MarketAPI market, String id) {
            market.getIncomeMult().unmodifyPercent(id);
            market.getStats().getDynamic().getMod("max_market_size").unmodifyFlat(id);
            market.removeImmigrationModifier((MarketImmigrationModifier)this);
        }

        public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
            int size = market.getSize();
            float bonus = this.calculateGrowthBonus(size);
            if (bonus > 0.0f) {
                incoming.getWeight().modifyFlat(this.getModId(), bonus, "\u5de5\u4e1a\u6269\u5f20\uff1a\u4eba\u53e3\u589e\u957f\u52a0\u6210");
            } else {
                incoming.getWeight().unmodifyFlat(this.getModId());
            }
        }

        private float calculateGrowthBonus(int size) {
            return 2.0f * (float)size - 0.2f * (float)(size - 1);
        }

        private String getModId() {
            return "IndustrialExpansion_growth";
        }

        public String getEffectDescription(float level) {
            return "\u6b96\u6c11\u5730\u6536\u5165\u589e\u52a0" + (int)INCOME_BONUS_PERCENT + "%\uff0c\u6b96\u6c11\u5730\u89c4\u6a21\u4e0a\u9650\u63d0\u5347" + SIZE_BONUS + "\uff0c\u4eba\u53e3\u589e\u957f\u6839\u636e\u6b96\u6c11\u5730\u89c4\u6a21\u989d\u5916\u589e\u52a0\uff08\u89c4\u6a21\u6bcf\u7ea7+2\uff0c\u6bcf\u7ea7\u8870\u51cf0.2\uff09";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public LevelBasedEffect.ScopeDescription getScopeDescription() {
            return LevelBasedEffect.ScopeDescription.GOVERNED_OUTPOST;
        }
    }
}

