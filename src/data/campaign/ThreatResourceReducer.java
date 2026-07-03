/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.econ.Industry
 *  com.fs.starfarer.api.campaign.econ.MarketConditionPlugin
 *  com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin
 *  com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCells
 */
package data.campaign;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bases.LuddicPathCells;

public class ThreatResourceReducer
extends BaseMarketConditionPlugin {
    private static final String THREAT_FACTION_ID = "threat_qr";
    public static final float FLEET_SIZE_BONUS = 0.5f;
    private static final String FLEET_SIZE_MOD_ID = "threat_resource_reducer_fleet_size";

    public void apply(String id) {
        this.market.getAccessibilityMod().modifyFlat(id, 1.0f, "\u5a01\u80c1\u6587\u660e\u79e9\u5e8f");
        this.market.getStats().getDynamic().getMod("combat_fleet_size_mult").modifyFlat(FLEET_SIZE_MOD_ID, 0.5f, "\u5a01\u80c1\u6587\u660e\u79e9\u5e8f");
        this.modifyAllIndustries(id);
    }

    public void unapply(String id) {
        this.market.getAccessibilityMod().unmodify(id);
        this.market.getStats().getDynamic().getMod("combat_fleet_size_mult").unmodifyFlat(FLEET_SIZE_MOD_ID);
        this.unmodifyAllIndustries(id);
    }

    public void advance(float amount) {
        if (!THREAT_FACTION_ID.equals(this.market.getFactionId())) {
            this.market.removeCondition(this.getModId());
            return;
        }
        this.removeHostileConditions();
    }

    private void modifyAllIndustries(String id) {
        String desc = "\u5a01\u80c1\u6587\u660e\u79e9\u5e8f";
        for (Industry industry : this.market.getIndustries()) {
            this.setDemandZero(industry, "food", id, desc);
            this.setDemandZero(industry, "drugs", id, desc);
            this.setDemandZero(industry, "organs", id, desc);
            this.setDemandZero(industry, "crew", id, desc);
            this.setDemandZero(industry, "marines", id, desc);
            this.setSupplyMult(industry, "crew", id, desc, 0.1f);
            this.setSupplyZero(industry, "marines", id, desc);
        }
    }

    private void unmodifyAllIndustries(String id) {
        String[] commodities = new String[]{"food", "drugs", "organs", "crew", "marines"};
        for (Industry industry : this.market.getIndustries()) {
            for (String commodity : commodities) {
                try {
                    if (industry.getDemand(commodity) != null) {
                        industry.getDemand(commodity).getQuantity().unmodify(id);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    if (industry.getSupply(commodity) == null) continue;
                    industry.getSupply(commodity).getQuantity().unmodify(id);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    private void setDemandZero(Industry industry, String commodity, String id, String desc) {
        try {
            if (industry.getDemand(commodity) != null) {
                industry.getDemand(commodity).getQuantity().modifyMult(id, 0.0f, desc);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void setSupplyZero(Industry industry, String commodity, String id, String desc) {
        try {
            if (industry.getSupply(commodity) != null) {
                industry.getSupply(commodity).getQuantity().modifyMult(id, 0.0f, desc);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void setSupplyMult(Industry industry, String commodity, String id, String desc, float mult) {
        try {
            if (industry.getSupply(commodity) != null) {
                industry.getSupply(commodity).getQuantity().modifyMult(id, mult, desc);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void removeHostileConditions() {
        String[] hostileConditions;
        if (this.market.hasCondition("pather_cells")) {
            try {
                LuddicPathCells plugin;
                MarketConditionPlugin condition = this.market.getCondition("pather_cells").getPlugin();
                if (condition instanceof LuddicPathCells && (plugin = (LuddicPathCells)condition).getIntel() != null) {
                    plugin.getIntel().endImmediately();
                }
            }
            catch (Exception condition) {
                // empty catch block
            }
            this.market.removeCondition("pather_cells");
        }
        for (String conditionId : hostileConditions = new String[]{"rogue_ai_core", "dissident", "decivilized"}) {
            if (!this.market.hasCondition(conditionId)) continue;
            this.market.removeCondition(conditionId);
        }
    }

    public boolean runWhilePaused() {
        return true;
    }
}

