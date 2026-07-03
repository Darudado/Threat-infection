/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CampaignUIAPI$CoreUITradeMode
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.CargoAPI$CargoItemType
 *  com.fs.starfarer.api.campaign.CargoStackAPI
 *  com.fs.starfarer.api.campaign.CoreUIAPI
 *  com.fs.starfarer.api.campaign.PlayerMarketTransaction
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.campaign.SpecialItemPlugin
 *  com.fs.starfarer.api.campaign.SubmarketPlugin$PlayerEconomyImpactMode
 *  com.fs.starfarer.api.campaign.SubmarketPlugin$TransferAction
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.econ.SubmarketAPI
 *  com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin
 *  com.fs.starfarer.api.util.Highlights
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 */
package data.scripts.campaign.submarkets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemPlugin;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.impl.items.BlueprintProviderItem;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreatSubmarketPlugin
extends BaseSubmarketPlugin {
    private static final List<String> THREAT_INDUSTRIES = Arrays.asList("ThreatControlHQ", "NanoReconstructionArray", "wxkjz_c01", "NanoLifeSupportFacility");
    private static final String NANITE_RECONSTRUCTOR_FORGE = "nanite_reconstructor_forge";
    private static final String NANO_LIFE_SUPPORT_ENHANCER = "nano_life_support_enhancer";
    private static final String FRAGMENT_FABRICATOR = "fragment_fabricator";
    private static final String THREAT_PROCESSING_UNIT = "threat_processing_unit";

    public void init(SubmarketAPI submarket) {
        super.init(submarket);
    }

    public void updateCargoPrePlayerInteraction() {
        float seconds = Global.getSector().getClock().convertToSeconds(this.sinceLastCargoUpdate);
        this.addAndRemoveStockpiledResources(seconds, false, true, true);
        this.sinceLastCargoUpdate = 0.0f;
        if (this.okToUpdateShipsAndWeapons()) {
            this.sinceSWUpdate = 0.0f;
            float stability = this.market.getStabilityValue();
            this.pruneWeapons(0.0f);
            boolean military = Misc.isMilitary((MarketAPI)this.market);
            int weapons = 6 + Math.max(0, this.market.getSize() - 1);
            int fighters = 2 + Math.max(0, (this.market.getSize() - 3) / 2);
            this.addWeapons(weapons, weapons + 2, 3, this.submarket.getFaction().getId(), false);
            this.addFighters(fighters, fighters + 2, 3, this.submarket.getFaction().getId());
            String independentFaction = "independent";
            this.addWeapons(weapons / 2, weapons / 2 + 1, 3, independentFaction, false);
            this.addFighters(fighters / 2, fighters / 2 + 1, 3, independentFaction);
            this.addHullMods(3, 1 + this.itemGenRandom.nextInt(3));
            this.addThreatIndustrialBlueprints();
            this.refreshSpecialItems();
            this.refreshStackableSpecialItems();
        }
        this.getCargo().sort();
    }

    private void refreshSpecialItems() {
        boolean shouldRefresh;
        CargoAPI cargo = this.getCargo();
        ArrayList<CargoStackAPI> specialStacksToRemove = new ArrayList<CargoStackAPI>();
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            SpecialItemData data;
            if (!stack.isSpecialStack() || (data = stack.getSpecialDataIfSpecial()) == null || !NANITE_RECONSTRUCTOR_FORGE.equals(data.getId()) && !NANO_LIFE_SUPPORT_ENHANCER.equals(data.getId())) continue;
            specialStacksToRemove.add(stack);
        }
        for (CargoStackAPI stack : specialStacksToRemove) {
            cargo.removeStack(stack);
        }
        boolean playerHasForge = Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().getBoolean("$threat_forge_owned");
        boolean playerHasEnhancer = Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().getBoolean("$threat_enhancer_owned");
        float refreshChance = 0.7f;
        boolean bl = shouldRefresh = this.itemGenRandom.nextFloat() < refreshChance;
        if (shouldRefresh) {
            boolean enhancerEverAdded;
            boolean forgeEverAdded;
            if (!playerHasForge && !(forgeEverAdded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$threat_forge_ever_added"))) {
                SpecialItemData forgeItem = new SpecialItemData(NANITE_RECONSTRUCTOR_FORGE, null);
                cargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)forgeItem, 1.0f);
                Global.getSector().getMemoryWithoutUpdate().set("$threat_forge_ever_added", (Object)true);
                Global.getLogger(((Object)((Object)this)).getClass()).info((Object)"Refreshed nanite_reconstructor_forge in slot 1");
            }
            if (!playerHasEnhancer && !(enhancerEverAdded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$threat_enhancer_ever_added"))) {
                SpecialItemData enhancerItem = new SpecialItemData(NANO_LIFE_SUPPORT_ENHANCER, null);
                cargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)enhancerItem, 1.0f);
                Global.getSector().getMemoryWithoutUpdate().set("$threat_enhancer_ever_added", (Object)true);
                Global.getLogger(((Object)((Object)this)).getClass()).info((Object)"Refreshed nano_life_support_enhancer in slot 2");
            }
        }
    }

    private void refreshStackableSpecialItems() {
        int processorQty;
        CargoAPI cargo = this.getCargo();
        ArrayList<CargoStackAPI> toRemove = new ArrayList<CargoStackAPI>();
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            SpecialItemData data;
            if (!stack.isSpecialStack() || (data = stack.getSpecialDataIfSpecial()) == null || !FRAGMENT_FABRICATOR.equals(data.getId()) && !THREAT_PROCESSING_UNIT.equals(data.getId())) continue;
            toRemove.add(stack);
        }
        for (CargoStackAPI stack : toRemove) {
            cargo.removeStack(stack);
        }
        int minQty = 1;
        int marketSize = this.market.getSize();
        int maxQty = marketSize < 4 ? 3 : (marketSize < 6 ? 4 : 5);
        int fabricatorQty = minQty + this.itemGenRandom.nextInt(maxQty - minQty + 1);
        if (fabricatorQty > 0) {
            cargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData(FRAGMENT_FABRICATOR, null), (float)fabricatorQty);
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)("Added " + fabricatorQty + " fragment_fabricator(s) to submarket."));
        }
        if ((processorQty = minQty + this.itemGenRandom.nextInt(maxQty - minQty + 1)) > 0) {
            cargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData(THREAT_PROCESSING_UNIT, null), (float)processorQty);
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)("Added " + processorQty + " threat_processing_unit(s) to submarket."));
        }
    }

    private void addThreatIndustrialBlueprints() {
        CargoAPI ourCargo = this.getCargo();
        for (CargoStackAPI stack : ourCargo.getStacksCopy()) {
            SpecialItemPlugin plugin = stack.getPlugin();
            if (!(plugin instanceof BlueprintProviderItem)) continue;
            ourCargo.removeStack(stack);
        }
        ArrayList<String> availableBlueprints = new ArrayList<String>();
        for (String industryId : THREAT_INDUSTRIES) {
            if (Global.getSector().getPlayerFaction().knowsIndustry(industryId)) continue;
            availableBlueprints.add(industryId);
        }
        if (availableBlueprints.isEmpty()) {
            return;
        }
        int blueprintCount = this.getThreatBlueprintCount();
        blueprintCount = Math.min(blueprintCount, availableBlueprints.size());
        WeightedRandomPicker picker = new WeightedRandomPicker(this.itemGenRandom);
        for (String blueprint : availableBlueprints) {
            picker.add((Object)blueprint, this.getIndustryWeight(blueprint));
        }
        for (int i = 0; i < blueprintCount; ++i) {
            String industryId = (String)picker.pickAndRemove();
            if (industryId == null) continue;
            ourCargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData("industry_bp", industryId), 1.0f);
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)("Added Threat industrial blueprint: " + industryId));
        }
    }

    private float getIndustryWeight(String industryId) {
        return 1.0f;
    }

    private int getThreatBlueprintCount() {
        int baseCount = 1;
        if (this.market.getSize() >= 6) {
            baseCount = 2;
        }
        return Math.min(2, baseCount);
    }

    public boolean isIllegalOnSubmarket(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL;
    }

    public boolean isIllegalOnSubmarket(String commodityId, SubmarketPlugin.TransferAction action) {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL;
    }

    public boolean isIllegalOnSubmarket(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL;
    }

    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return "\u6b64\u5e02\u573a\u4e0d\u63a5\u53d7\u4efb\u4f55\u51fa\u552e\u3002";
        }
        return null;
    }

    public String getIllegalTransferText(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return "\u6b64\u5e02\u573a\u4e0d\u63a5\u53d7\u4efb\u4f55\u51fa\u552e\u3002";
        }
        return null;
    }

    public Highlights getIllegalTransferTextHighlights(CargoStackAPI stack, SubmarketPlugin.TransferAction action) {
        return null;
    }

    public Highlights getIllegalTransferTextHighlights(FleetMemberAPI member, SubmarketPlugin.TransferAction action) {
        return null;
    }

    public void reportPlayerMarketTransaction(PlayerMarketTransaction transaction) {
        super.reportPlayerMarketTransaction(transaction);
        for (CargoStackAPI stack : transaction.getBought().getStacksCopy()) {
            SpecialItemData data;
            if (!stack.isSpecialStack() || (data = stack.getSpecialDataIfSpecial()) == null) continue;
            if (NANITE_RECONSTRUCTOR_FORGE.equals(data.getId())) {
                Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().set("$threat_forge_owned", (Object)true);
                Global.getLogger(((Object)((Object)this)).getClass()).info((Object)"Player bought nanite_reconstructor_forge - marked as owned");
                continue;
            }
            if (!NANO_LIFE_SUPPORT_ENHANCER.equals(data.getId())) continue;
            Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().set("$threat_enhancer_owned", (Object)true);
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)"Player bought nano_life_support_enhancer - marked as owned");
        }
    }

    protected Object writeReplace() {
        if (this.okToUpdateShipsAndWeapons()) {
            this.pruneWeapons(0.0f);
            this.getCargo().getMothballedShips().clear();
        }
        return this;
    }

    public String getName() {
        return "\u9690\u79d8\u9ed1\u5e02";
    }

    public float getTariff() {
        return 1.0f;
    }

    public String getTooltipAppendix(CoreUIAPI ui) {
        if (this.isEnabled(ui)) {
            return "\u9a87\u5165\u5a01\u80c1\u7684\u6570\u636e\u5e93\uff0c\u83b7\u53d6\u5efa\u7b51\u84dd\u56fe\u548c\u7279\u6b8a\u5236\u54c1\u3002\u4ea4\u6613\u9700\u7f34\u7eb3100%\u7684\u9ad8\u989d\u5173\u7a0e\u3002";
        }
        return null;
    }

    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        String appendix = this.getTooltipAppendix(ui);
        if (appendix == null) {
            return null;
        }
        Highlights h = new Highlights();
        h.setText(new String[]{appendix});
        h.setColors(new Color[]{Misc.getPositiveHighlightColor()});
        return h;
    }

    public boolean isEnabled(CoreUIAPI ui) {
        return ui.getTradeMode() != CampaignUIAPI.CoreUITradeMode.SNEAK;
    }

    public boolean isBlackMarket() {
        return false;
    }

    public boolean isMilitaryMarket() {
        return false;
    }

    public SubmarketPlugin.PlayerEconomyImpactMode getPlayerEconomyImpactMode() {
        return SubmarketPlugin.PlayerEconomyImpactMode.PLAYER_SELL_ONLY;
    }

    public String getMarketInventoryDescription() {
        ArrayList<String> availableBlueprints = new ArrayList<String>();
        for (String string : THREAT_INDUSTRIES) {
            if (Global.getSector().getPlayerFaction().knowsIndustry(string)) continue;
            availableBlueprints.add(string);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\u53ef\u7528\u7684\u5a01\u80c1\u5de5\u4e1a\u84dd\u56fe:\n");
        for (String industryId : THREAT_INDUSTRIES) {
            if (Global.getSector().getPlayerFaction().knowsIndustry(industryId)) continue;
            String industryName = this.getIndustryDisplayName(industryId);
            sb.append("  \u2713 ").append(industryName).append("\n");
        }
        sb.append("\n\u7279\u6b8a\u5236\u54c1\uff08\u6bcf\u6b21\u8bbf\u95ee\u6709\u6982\u7387\u5237\u65b0\uff0c\u4f46\u53ea\u51fa\u73b0\u4e00\u6b21\uff09:\n");
        boolean bl = Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().getBoolean("$threat_forge_owned");
        boolean playerHasEnhancer = Global.getSector().getPlayerFaction().getMemoryWithoutUpdate().getBoolean("$threat_enhancer_owned");
        boolean forgeEverAdded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$threat_forge_ever_added");
        boolean enhancerEverAdded = Global.getSector().getMemoryWithoutUpdate().getBoolean("$threat_enhancer_ever_added");
        if (bl) {
            sb.append("  1. \u7eb3\u7c73\u91cd\u6784\u953b\u7089 (\u5df2\u83b7\u53d6)\n");
        } else if (forgeEverAdded) {
            sb.append("  1. \u7eb3\u7c73\u91cd\u6784\u953b\u7089 (\u5df2\u51fa\u73b0\uff0c\u7b49\u5f85\u8d2d\u4e70)\n");
        } else {
            sb.append("  1. \u7eb3\u7c73\u91cd\u6784\u953b\u7089 (\u7eb3\u7c73\u788e\u7247\u4ea7\u91cf\u63d0\u5347) [\u672a\u5237\u65b0]\n");
        }
        if (playerHasEnhancer) {
            sb.append("  2. \u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668 (\u5df2\u83b7\u53d6)\n");
        } else if (enhancerEverAdded) {
            sb.append("  2. \u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668 (\u989d\u5916-50%\u5371\u9669\u5ea6\uff0c+25%\u6d41\u901a\u6027) [\u5df2\u51fa\u73b0\uff0c\u7b49\u5f85\u8d2d\u4e70]\n");
        } else {
            sb.append("  2. \u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668 (\u989d\u5916-50%\u5371\u9669\u5ea6\uff0c+25%\u6d41\u901a\u6027) [\u672a\u5237\u65b0]\n");
        }
        CargoAPI cargo = this.getCargo();
        int fabricatorCount = 0;
        int processorCount = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            SpecialItemData data;
            if (!stack.isSpecialStack() || (data = stack.getSpecialDataIfSpecial()) == null) continue;
            if (FRAGMENT_FABRICATOR.equals(data.getId())) {
                fabricatorCount = (int)((float)fabricatorCount + stack.getSize());
                continue;
            }
            if (!THREAT_PROCESSING_UNIT.equals(data.getId())) continue;
            processorCount = (int)((float)processorCount + stack.getSize());
        }
        sb.append("\n\u53ef\u5806\u53e0\u7279\u6b8a\u7269\u54c1\uff08\u6bcf\u6b21\u5237\u65b0\u91cd\u7f6e\u6570\u91cf\uff09:\n");
        sb.append("  \u788e\u7247\u5236\u9020\u5668: ").append(fabricatorCount).append(" \u4e2a\n");
        sb.append("  \u5a01\u80c1\u5904\u7406\u5355\u5143: ").append(processorCount).append(" \u4e2a\n");
        if (availableBlueprints.isEmpty()) {
            sb.append("\n\u5df2\u638c\u63e1\u6240\u6709\u5a01\u80c1\u5de5\u4e1a\u84dd\u56fe");
        }
        return sb.toString();
    }

    private String getIndustryDisplayName(String industryId) {
        switch (industryId) {
            case "ThreatControlHQ": {
                return "\u5a01\u80c1\u5236\u9020\u9996\u8111";
            }
            case "NanoReconstructionArray": {
                return "\u7eb3\u7c73\u91cd\u6784\u9635\u5217";
            }
            case "wxkjz_c01": {
                return "\u8981\u585e\u5355\u5143 - \u5a01\u80c1";
            }
            case "NanoLifeSupportFacility": {
                return "\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd";
            }
        }
        return industryId;
    }

    public String getMarketSpecialItemsDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u7279\u6b8a\u5236\u54c1\u5e93\u5b58\uff08\u4e24\u4e2a\u56fa\u5b9a\u69fd\u4f4d\uff09:\n");
        CargoAPI cargo = this.getCargo();
        ArrayList<CargoStackAPI> specialStacks = new ArrayList<CargoStackAPI>();
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (!stack.isSpecialStack()) continue;
            specialStacks.add(stack);
        }
        if (specialStacks.isEmpty()) {
            sb.append("  \u65e0\u53ef\u7528\u7279\u6b8a\u5236\u54c1\n");
        } else {
            for (CargoStackAPI stack : specialStacks) {
                String itemId;
                SpecialItemData data = stack.getSpecialDataIfSpecial();
                if (data == null || !NANITE_RECONSTRUCTOR_FORGE.equals(itemId = data.getId()) && !NANO_LIFE_SUPPORT_ENHANCER.equals(itemId)) continue;
                String itemName = this.getSpecialItemDisplayName(itemId);
                int quantity = (int)stack.getSize();
                sb.append("  \u2713 ").append(itemName).append(" \u00d7").append(quantity).append("\uff08\u53ea\u6b64\u4e00\u6b21\uff09\n");
            }
        }
        return sb.toString();
    }

    private String getSpecialItemDisplayName(String itemId) {
        switch (itemId) {
            case "nanite_reconstructor_forge": {
                return "\u7eb3\u7c73\u91cd\u6784\u953b\u7089";
            }
            case "nano_life_support_enhancer": {
                return "\u7eb3\u7c73\u7ef4\u751f\u8bbe\u65bd\u589e\u5f3a\u5668";
            }
        }
        return itemId;
    }
}

