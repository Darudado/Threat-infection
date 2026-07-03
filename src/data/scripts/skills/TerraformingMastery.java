/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.characters.LevelBasedEffect$ScopeDescription
 *  com.fs.starfarer.api.characters.MarketSkillEffect
 */
package data.scripts.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.LevelBasedEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerraformingMastery
implements MarketSkillEffect {
    private static final float FLEET_SIZE_BONUS = 0.25f;
    private static final float GROUND_DEFENSE_BONUS = 0.25f;
    private static final List<String> NEGATIVE_CONDITIONS = Arrays.asList("high_gravity", "tectonic_activity", "hot", "cold", "thin_atmosphere", "pollution", "extreme_weather");
    private static final String MEM_REMOVED_CONDITIONS = "$terraforming_removed_conditions";
    private static final String MEM_PATROL_APPLIED = "$terraforming_patrol_applied";

    public void apply(MarketAPI market, String id, float level) {
        Global.getLogger(this.getClass()).info((Object)("TerraformingMastery: Applying to " + market.getName()));
        market.getStats().getDynamic().getMod("combat_fleet_size_mult").modifyFlat(id, 0.25f, "\u5730\u5f62\u6539\u9020\u4e13\u7cbe");
        market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(id, 1.25f, "\u5730\u5f62\u6539\u9020\u4e13\u7cbe");
        ArrayList<String> removed = new ArrayList<String>();
        for (String condId : NEGATIVE_CONDITIONS) {
            if (!market.hasCondition(condId)) continue;
            market.removeCondition(condId);
            removed.add(condId);
            Global.getLogger(this.getClass()).info((Object)("TerraformingMastery: Removed condition " + condId));
        }
        if (!removed.isEmpty()) {
            market.getMemoryWithoutUpdate().set(MEM_REMOVED_CONDITIONS, removed);
        }
        if (!market.getMemoryWithoutUpdate().getBoolean(MEM_PATROL_APPLIED)) {
            this.applyPatrolBonus(market, id);
            market.getMemoryWithoutUpdate().set(MEM_PATROL_APPLIED, (Object)true);
        }
    }

    public void unapply(MarketAPI market, String id) {
        Global.getLogger(this.getClass()).info((Object)("TerraformingMastery: Unapplying from " + market.getName()));
        market.getStats().getDynamic().getMod("combat_fleet_size_mult").unmodifyFlat(id);
        market.getStats().getDynamic().getMod("ground_defenses_mod").unmodifyMult(id);
        List<String> removed = (List<String>)market.getMemoryWithoutUpdate().get(MEM_REMOVED_CONDITIONS);
        if (removed != null) {
            for (String condId : removed) {
                if (market.hasCondition(condId)) continue;
                market.addCondition(condId);
                Global.getLogger(this.getClass()).info((Object)("TerraformingMastery: Restored condition " + condId));
            }
            market.getMemoryWithoutUpdate().unset(MEM_REMOVED_CONDITIONS);
        }
        if (market.getMemoryWithoutUpdate().getBoolean(MEM_PATROL_APPLIED)) {
            this.removePatrolBonus(market, id);
            market.getMemoryWithoutUpdate().unset(MEM_PATROL_APPLIED);
        }
    }

    private void applyPatrolBonus(MarketAPI market, String id) {
        boolean hasMilitary;
        boolean bl = hasMilitary = market.hasIndustry("militarybase") || market.hasIndustry("highcommand");
        if (hasMilitary) {
            market.getStats().getDynamic().getMod("patrol_num_heavy_mod").modifyFlat(id, 1.0f, "\u5730\u5f62\u6539\u9020\u4e13\u7cbe");
            Global.getLogger(this.getClass()).info((Object)"TerraformingMastery: Added +1 heavy patrol");
        } else {
            market.getStats().getDynamic().getMod("patrol_num_medium_mod").modifyFlat(id, 1.0f, "\u5730\u5f62\u6539\u9020\u4e13\u7cbe");
            Global.getLogger(this.getClass()).info((Object)"TerraformingMastery: Added +1 medium patrol");
        }
    }

    private void removePatrolBonus(MarketAPI market, String id) {
        boolean hasMilitary;
        boolean bl = hasMilitary = market.hasIndustry("militarybase") || market.hasIndustry("highcommand");
        if (hasMilitary) {
            market.getStats().getDynamic().getMod("patrol_num_heavy_mod").unmodifyFlat(id);
        } else {
            market.getStats().getDynamic().getMod("patrol_num_medium_mod").unmodifyFlat(id);
        }
        Global.getLogger(this.getClass()).info((Object)"TerraformingMastery: Removed patrol bonus");
    }

    public String getEffectDescription(float level) {
        return "\u8230\u961f\u89c4\u6a21 +25%\uff0c\u5730\u9762\u9632\u5fa1 +25%\uff0c\u6c38\u4e45\u62b5\u6d88\u4e03\u79cd\u8d1f\u9762\u884c\u661f\u6761\u4ef6\uff0c\u5e76\u6839\u636e\u662f\u5426\u6709\u519b\u4e8b\u8bbe\u65bd\u589e\u52a0 1 \u652f\u91cd\u578b\u6216\u4e2d\u578b\u5de1\u903b\u961f\u3002";
    }

    public String getEffectPerLevelDescription() {
        return null;
    }

    public LevelBasedEffect.ScopeDescription getScopeDescription() {
        return LevelBasedEffect.ScopeDescription.GOVERNED_OUTPOST;
    }
}

