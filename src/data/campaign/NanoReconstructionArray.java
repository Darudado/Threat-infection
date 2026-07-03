/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
 *  com.fs.starfarer.api.util.Pair
 */
package data.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;

public class NanoReconstructionArray
extends BaseIndustry {
    private static final String NANITES = "nanites";
    private static final float HEAVY_MACHINERY_PER_POP = 0.45f;
    private static final float HAND_WEAPONS_PER_POP = 0.4f;
    private static final float RARE_METALS_PER_POP = 0.25f;
    private static final float METALS_PER_POP = 0.5f;
    private static final float SHIPS_PER_POP = 0.35f;
    private static final float SUPPLIES_PER_POP = 0.4f;
    private static final float HEAVY_MACHINERY_BASE = 1.5f;
    private static final float HAND_WEAPONS_BASE = 1.5f;
    private static final float RARE_METALS_BASE = 0.8f;
    private static final float METALS_BASE = 2.0f;
    private static final float SHIPS_BASE = 1.0f;
    private static final float SUPPLIES_BASE = 1.5f;
    private static final int NANITES_DEMAND_MOD = 2;
    private static final int CREW_DEMAND_MOD = -1;

    public void apply() {
        super.apply(true);
        int size = this.market.getSize();
        this.demand(NANITES, size + 2);
        this.demand("crew", size + -1);
        this.supply("heavy_machinery", (int)(1.5 + Math.floor((float)size * 0.45f)));
        this.supply("hand_weapons", (int)(1.5 + Math.floor((float)size * 0.4f)));
        this.supply("rare_metals", (int)((double)0.8f + Math.floor((float)size * 0.25f)) + 2);
        this.supply("metals", (int)(2.0 + Math.floor((float)size * 0.5f)) + 2);
        this.supply("ships", (int)(1.0 + Math.floor((float)size * 0.35f)));
        this.supply("supplies", (int)(1.5 + Math.floor((float)size * 0.4f)));
        Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
        this.applyDeficitToProduction(1, nanitesDeficit, new String[]{"heavy_machinery", "hand_weapons", "rare_metals", "metals", "ships", "supplies"});
        if (!this.isFunctional()) {
            this.supply.clear();
        }
    }

    public void unapply() {
        super.unapply();
    }

    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isAvailableToBuild() {
        return Global.getSector().getPlayerFaction().knowsIndustry(this.getId());
    }

    public String getUnavailableReason() {
        if (!Global.getSector().getPlayerFaction().knowsIndustry(this.getId())) {
            return "\u9700\u8981\u7eb3\u7c73\u91cd\u6784\u9635\u5217\u84dd\u56fe";
        }
        return super.getUnavailableReason();
    }

    public boolean showWhenUnavailable() {
        return Global.getSector().getPlayerFaction().knowsIndustry(this.getId());
    }
}

