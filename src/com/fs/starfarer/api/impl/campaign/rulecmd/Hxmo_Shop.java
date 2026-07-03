/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.CargoAPI$CargoItemType
 *  com.fs.starfarer.api.campaign.CargoPickerListener
 *  com.fs.starfarer.api.campaign.CargoStackAPI
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.fleet.FleetMemberType
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.impl.campaign.rulecmd.FireAll
 *  com.fs.starfarer.api.loading.WeaponSpecAPI
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Misc$Token
 */
package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoPickerListener;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hxmo_Shop
extends BaseCommandPlugin {
    private static final Map<String, Integer> SHOP_ITEMS = new HashMap<String, Integer>();
    private static final Map<String, Integer> SHOP_MAX_QUANTITIES;
    private static final Map<String, Integer> CORE_VALUE;
    private static final Map<String, Integer> SHIP_PRICES;
    private static final String SHIP_BOUGHT_GLOBAL_PREFIX = "$hxmo_ship_bought_";
    private static final String ITEM_BOUGHT_GLOBAL_PREFIX = "$hxmo_item_bought_";
    private static final String POINTS_KEY = "$hxmo_shop_points";

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String action;
        switch (action = params.get(0).getString(memoryMap)) {
            case "exchange": {
                this.openCoreExchange(dialog, memoryMap);
                break;
            }
            case "open": {
                this.openShop(dialog, memoryMap);
                break;
            }
            case "ship": {
                String shipId;
                String string = shipId = params.size() > 1 ? params.get(1).getString(memoryMap) : null;
                if (shipId == null) break;
                this.buyShip(dialog, memoryMap, shipId);
            }
        }
        return true;
    }

    private void openCoreExchange(final InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
        CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
        CargoAPI exchangeCargo = Global.getFactory().createCargo(false);
        for (String coreId : CORE_VALUE.keySet()) {
            float qty = playerCargo.getCommodityQuantity(coreId);
            if (!(qty > 0.0f)) continue;
            exchangeCargo.addCommodity(coreId, qty);
        }
        if (exchangeCargo.isEmpty()) {
            dialog.getTextPanel().addPara("\u4f60\u6ca1\u6709\u4efb\u4f55AI\u6838\u5fc3\u53ef\u4ee5\u5151\u6362\u3002");
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        dialog.showCargoPickerDialog("\u9009\u62e9\u8981\u5151\u6362\u7684AI\u6838\u5fc3", "\u786e\u8ba4", "\u53d6\u6d88", true, 300.0f, exchangeCargo, new CargoPickerListener(){

            public void pickedCargo(CargoAPI cargo) {
                int totalPoints = 0;
                CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    String id = stack.getCommodityId();
                    int count = (int)stack.getSize();
                    int value = CORE_VALUE.getOrDefault(id, 0);
                    totalPoints += value * count;
                    playerCargo.removeCommodity(id, (float)count);
                }
                if (totalPoints > 0) {
                    MemoryAPI globalMem = Global.getSector().getMemoryWithoutUpdate();
                    int current = globalMem.getInt(Hxmo_Shop.POINTS_KEY);
                    globalMem.set(Hxmo_Shop.POINTS_KEY, (Object)(current + totalPoints));
                    dialog.getTextPanel().addPara("\u5151\u6362\u6210\u529f\uff01\u83b7\u5f97 " + totalPoints + " \u70b9\u6570\u3002");
                }
                FireAll.fire(null, (InteractionDialogAPI)dialog, (Map)memoryMap, (String)"hxmo_xiaohui_menu");
            }

            public void cancelledCargoSelection() {
                FireAll.fire(null, (InteractionDialogAPI)dialog, (Map)memoryMap, (String)"hxmo_xiaohui_menu");
            }

            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                float pad = 3.0f;
                float opad = 10.0f;
                panel.setParaFontOrbitron();
                panel.addPara("AI\u6838\u5fc3\u5151\u6362", Misc.getHighlightColor(), 1.0f);
                panel.setParaFontDefault();
                panel.addPara("\u9009\u62e9\u6838\u5fc3\uff0c\u5c06\u83b7\u5f97\u5546\u5e97\u70b9\u6570\u3002", opad);
                int total = 0;
                for (CargoStackAPI stack : combined.getStacksCopy()) {
                    int val = CORE_VALUE.getOrDefault(stack.getCommodityId(), 0) * (int)stack.getSize();
                    total += val;
                    panel.addPara(stack.getDisplayName() + " x" + (int)stack.getSize() + " \u2192 " + val + " \u70b9", pad);
                }
                panel.addPara("\u603b\u8ba1\u53ef\u83b7\u5f97: " + total + " \u70b9", Misc.getPositiveHighlightColor(), opad);
            }
        });
    }

    private void openShop(final InteractionDialogAPI dialog, final Map<String, MemoryAPI> memoryMap) {
        CargoAPI shopCargo = Global.getFactory().createCargo(false);
        MemoryAPI globalMem = Global.getSector().getMemoryWithoutUpdate();
        int validItems = 0;
        for (Map.Entry<String, Integer> entry : SHOP_ITEMS.entrySet()) {
            String boughtKey;
            int bought;
            String id = entry.getKey();
            int maxQuantity = SHOP_MAX_QUANTITIES.getOrDefault(id, 1);
            int remaining = maxQuantity - (bought = globalMem.getInt(boughtKey = ITEM_BOUGHT_GLOBAL_PREFIX + id));
            if (remaining <= 0) continue;
            boolean added = false;
            if (id.endsWith("_core") || id.startsWith("cl_") || id.startsWith("hl_") || id.startsWith("wllg_") || id.startsWith("xiaohui_")) {
                if (Global.getSettings().getCommoditySpec(id) != null) {
                    shopCargo.addCommodity(id, (float)remaining);
                    added = true;
                }
            } else if (Global.getSettings().getSpecialItemSpec(id) != null) {
                shopCargo.addItems(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData(id, null), (float)remaining);
                added = true;
            } else {
                WeaponSpecAPI weapon = Global.getSettings().getWeaponSpec(id);
                if (weapon != null) {
                    shopCargo.addWeapons(id, remaining);
                    added = true;
                }
            }
            if (!added) continue;
            ++validItems;
        }
        if (validItems == 0) {
            dialog.getTextPanel().addPara("\u5546\u5e97\u6682\u65f6\u6ca1\u6709\u53ef\u51fa\u552e\u7684\u5546\u54c1\u3002", Misc.getNegativeHighlightColor());
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        int points = this.getPoints();
        dialog.showCargoPickerDialog("\u5c0f\u7070\u7684\u5546\u5e97 (\u4f59\u989d: " + points + " \u70b9)", "\u8d2d\u4e70", "\u53d6\u6d88", false, 300.0f, shopCargo, new CargoPickerListener(){

            public void pickedCargo(CargoAPI cargo) {
                int totalCost = 0;
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    String id = Hxmo_Shop.this.getStackId(stack);
                    totalCost += SHOP_ITEMS.getOrDefault(id, 9999) * (int)stack.getSize();
                }
                int currentPoints = Hxmo_Shop.this.getPoints();
                if (totalCost > currentPoints) {
                    dialog.getTextPanel().addPara("\u70b9\u6570\u4e0d\u8db3\uff01\u9700\u8981 " + totalCost + " \u70b9\uff0c\u4f60\u53ea\u6709 " + currentPoints + " \u70b9\u3002", Misc.getNegativeHighlightColor());
                    FireAll.fire(null, (InteractionDialogAPI)dialog, (Map)memoryMap, (String)"hxmo_xiaohui_menu");
                    return;
                }
                MemoryAPI globalMem = Global.getSector().getMemoryWithoutUpdate();
                globalMem.set(Hxmo_Shop.POINTS_KEY, (Object)(currentPoints - totalCost));
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    String id = Hxmo_Shop.this.getStackId(stack);
                    if (!SHOP_MAX_QUANTITIES.containsKey(id)) continue;
                    String boughtKey = Hxmo_Shop.ITEM_BOUGHT_GLOBAL_PREFIX + id;
                    int bought = globalMem.getInt(boughtKey);
                    globalMem.set(boughtKey, (Object)(bought + (int)stack.getSize()));
                }
                CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.addFromStack(stack);
                }
                dialog.getTextPanel().addPara("\u8d2d\u4e70\u6210\u529f\uff01\u5269\u4f59\u70b9\u6570: " + (currentPoints - totalCost));
                FireAll.fire(null, (InteractionDialogAPI)dialog, (Map)memoryMap, (String)"hxmo_xiaohui_menu");
            }

            public void cancelledCargoSelection() {
                FireAll.fire(null, (InteractionDialogAPI)dialog, (Map)memoryMap, (String)"hxmo_xiaohui_menu");
            }

            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {
                panel.setParaFontOrbitron();
                panel.addPara("\u5c0f\u7070\u7684\u7269\u8d44\u5546\u5e97", Misc.getHighlightColor(), 1.0f);
                panel.setParaFontDefault();
                int cost = 0;
                for (CargoStackAPI stack : combined.getStacksCopy()) {
                    String id = Hxmo_Shop.this.getStackId(stack);
                    int price = SHOP_ITEMS.getOrDefault(id, 9999);
                    cost += price * (int)stack.getSize();
                    panel.addPara(stack.getDisplayName() + " \u2192 " + price + " \u70b9", 3.0f);
                }
                int points = Hxmo_Shop.this.getPoints();
                Color hl = cost > points ? Misc.getNegativeHighlightColor() : Misc.getPositiveHighlightColor();
                panel.addPara("\u603b\u4ef7: " + cost + " \u70b9\uff0c\u4f60\u7684\u4f59\u989d: " + points + " \u70b9", hl, 10.0f);
            }
        });
    }

    private void buyShip(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, String shipId) {
        MemoryAPI globalMem = Global.getSector().getMemoryWithoutUpdate();
        Integer price = SHIP_PRICES.get(shipId);
        if (price == null) {
            dialog.getTextPanel().addPara("\u672a\u77e5\u8230\u8239\u3002");
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        String boughtKey = SHIP_BOUGHT_GLOBAL_PREFIX + shipId;
        if (globalMem.getBoolean(boughtKey)) {
            dialog.getTextPanel().addPara("\u8fd9\u8258\u8230\u8239\u4f60\u5df2\u7ecf\u8d2d\u4e70\u8fc7\u4e86\u3002");
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        int points = this.getPoints();
        if (points < price) {
            dialog.getTextPanel().addPara("\u70b9\u6570\u4e0d\u8db3\uff01\u9700\u8981 " + price + " \u70b9\uff0c\u4f60\u53ea\u6709 " + points + " \u70b9\u3002");
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        if (Global.getSettings().getVariant(shipId) == null) {
            dialog.getTextPanel().addPara("\u8230\u8239\u6570\u636e\u9519\u8bef\uff0c\u65e0\u6cd5\u8d2d\u4e70\u3002");
            FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
            return;
        }
        globalMem.set(POINTS_KEY, (Object)(points - price));
        globalMem.set(boughtKey, (Object)true);
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, Global.getSettings().getVariant(shipId));
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
        dialog.getTextPanel().addPara("\u8d2d\u4e70\u6210\u529f\uff01" + ship.getVariant().getFullDesignationWithHullNameForShip() + " \u5df2\u52a0\u5165\u4f60\u7684\u8230\u961f\u3002\u5269\u4f59\u70b9\u6570: " + (points - price));
        FireAll.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"hxmo_xiaohui_menu");
    }

    private int getPoints() {
        return Global.getSector().getMemoryWithoutUpdate().getInt(POINTS_KEY);
    }

    private String getStackId(CargoStackAPI stack) {
        if (stack.isSpecialStack()) {
            return stack.getSpecialDataIfSpecial().getId();
        }
        if (stack.isWeaponStack()) {
            return stack.getWeaponSpecIfWeapon().getWeaponId();
        }
        if (stack.isCommodityStack()) {
            return stack.getCommodityId();
        }
        return null;
    }

    static {
        SHOP_ITEMS.put("pristine_nanoforge", 200);
        SHOP_ITEMS.put("cryoarithmetic_engine", 200);
        SHOP_ITEMS.put("drone_replicator", 150);
        SHOP_ITEMS.put("dealmaker_holosuite", 150);
        SHOP_ITEMS.put("synchrotron", 150);
        SHOP_ITEMS.put("orbital_fusion_lamp", 150);
        SHOP_ITEMS.put("coronal_portal", 150);
        SHOP_ITEMS.put("mantle_bore", 150);
        SHOP_ITEMS.put("catalytic_core", 150);
        SHOP_ITEMS.put("soil_nanites", 150);
        SHOP_ITEMS.put("biofactory_embryo", 150);
        SHOP_ITEMS.put("fullerene_spool", 150);
        SHOP_ITEMS.put("plasma_dynamo", 150);
        SHOP_ITEMS.put("nanite_reconstructor_forge", 150);
        SHOP_ITEMS.put("nano_life_support_enhancer", 150);
        SHOP_ITEMS.put("hl_core", 50);
        SHOP_ITEMS.put("cl_core", 30);
        SHOP_ITEMS.put("fragment_fabricator", 50);
        SHOP_ITEMS.put("threat_processing_unit", 50);
        SHOP_ITEMS.put("threat_package", 200);
        SHOP_ITEMS.put("threat_weapons_package", 150);
        SHOP_MAX_QUANTITIES = new HashMap<String, Integer>();
        SHOP_MAX_QUANTITIES.put("cl_core", 5);
        SHOP_MAX_QUANTITIES.put("hl_core", 5);
        SHOP_MAX_QUANTITIES.put("wllg_core", 5);
        SHOP_MAX_QUANTITIES.put("xiaohui_core", 5);
        SHOP_MAX_QUANTITIES.put("fragment_fabricator", 5);
        SHOP_MAX_QUANTITIES.put("threat_processing_unit", 5);
        SHOP_MAX_QUANTITIES.put("pristine_nanoforge", 1);
        SHOP_MAX_QUANTITIES.put("cryoarithmetic_engine", 1);
        SHOP_MAX_QUANTITIES.put("drone_replicator", 1);
        SHOP_MAX_QUANTITIES.put("dealmaker_holosuite", 1);
        SHOP_MAX_QUANTITIES.put("synchrotron", 1);
        SHOP_MAX_QUANTITIES.put("orbital_fusion_lamp", 1);
        SHOP_MAX_QUANTITIES.put("coronal_portal", 1);
        SHOP_MAX_QUANTITIES.put("mantle_bore", 1);
        SHOP_MAX_QUANTITIES.put("catalytic_core", 1);
        SHOP_MAX_QUANTITIES.put("soil_nanites", 1);
        SHOP_MAX_QUANTITIES.put("biofactory_embryo", 1);
        SHOP_MAX_QUANTITIES.put("fullerene_spool", 1);
        SHOP_MAX_QUANTITIES.put("plasma_dynamo", 1);
        SHOP_MAX_QUANTITIES.put("nanite_reconstructor_forge", 1);
        SHOP_MAX_QUANTITIES.put("nano_life_support_enhancer", 1);
        SHOP_MAX_QUANTITIES.put("threat_package", 1);
        SHOP_MAX_QUANTITIES.put("threat_weapons_package", 1);
        CORE_VALUE = new HashMap<String, Integer>();
        CORE_VALUE.put("gamma_core", 10);
        CORE_VALUE.put("beta_core", 20);
        CORE_VALUE.put("alpha_core", 30);
        CORE_VALUE.put("xiaohui_core", 50);
        CORE_VALUE.put("wllg_core", 50);
        CORE_VALUE.put("hl_core", 30);
        CORE_VALUE.put("cl_core", 20);
        SHIP_PRICES = new HashMap<String, Integer>();
        SHIP_PRICES.put("wxxzj_xw", 300);
        SHIP_PRICES.put("wxggwb", 300);
        SHIP_PRICES.put("wxmk", 250);
    }
}

