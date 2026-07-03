/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.EveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashMap;
import java.util.List;

public class TacticalRelay
extends BaseHullMod {
    private static final String RANGE_BOOST_ID = "tactical_relay_range";
    private static final String EW_BOOST_ID = "tactical_relay_ew";
    private static final String PLUGIN_KEY = "tactical_relay_plugin";
    private static final Object STATUS_KEY = new Object();

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        if (engine.getCustomData().get(PLUGIN_KEY) == null) {
            engine.getCustomData().put(PLUGIN_KEY, true);
            final String hullmodId = id;
            engine.addPlugin((EveryFrameCombatPlugin)new BaseEveryFrameCombatPlugin(){

                public void advance(float amount, List events) {
                    int owner;
                    CombatEngineAPI engine = Global.getCombatEngine();
                    if (engine == null) {
                        return;
                    }
                    HashMap<Integer, Integer> countByOwner = new HashMap<Integer, Integer>();
                    for (ShipAPI s : engine.getShips()) {
                        if (!s.isAlive() || !s.getVariant().hasHullMod(hullmodId)) continue;
                        owner = s.getOwner();
                        countByOwner.put(owner, countByOwner.getOrDefault(owner, 0) + 1);
                    }
                    for (ShipAPI s : engine.getShips()) {
                        owner = s.getOwner();
                        int count = countByOwner.getOrDefault(owner, 0);
                        s.getMutableStats().getBallisticWeaponRangeBonus().unmodify(TacticalRelay.RANGE_BOOST_ID);
                        s.getMutableStats().getEnergyWeaponRangeBonus().unmodify(TacticalRelay.RANGE_BOOST_ID);
                        s.getMutableStats().getDynamic().getMod("electronic_warfare_flat").unmodify(TacticalRelay.EW_BOOST_ID);
                        if (count > 0) {
                            float rangePercent = 30.0f * (float)count;
                            s.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(TacticalRelay.RANGE_BOOST_ID, rangePercent);
                            s.getMutableStats().getEnergyWeaponRangeBonus().modifyPercent(TacticalRelay.RANGE_BOOST_ID, rangePercent);
                        }
                        if (!s.isAlive() || !s.getVariant().hasHullMod(hullmodId)) continue;
                        s.getMutableStats().getDynamic().getMod("electronic_warfare_flat").modifyFlat(TacticalRelay.EW_BOOST_ID, 20.0f);
                    }
                    ShipAPI playerShip = engine.getPlayerShip();
                    if (playerShip != null && playerShip.isAlive()) {
                        Object statusText;
                        int playerOwner = playerShip.getOwner();
                        int count = countByOwner.getOrDefault(playerOwner, 0);
                        float rangePercent = 30.0f * (float)count;
                        boolean hasEW = playerShip.getVariant().hasHullMod(hullmodId);
                        boolean isBad = false;
                        if (count > 0) {
                            statusText = "\u5c04\u7a0b +" + (int)rangePercent + "%";
                            if (hasEW) {
                                statusText = (String)statusText + ", \u7535\u5b50\u6218 +20";
                            }
                        } else {
                            statusText = "\u65e0\u52a0\u6210";
                            isBad = true;
                        }
                        String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");
                        engine.maintainStatusForPlayerShip(STATUS_KEY, icon, "\u4e3b\u5bb0\u6838\u5fc3", (String)statusText, isBad);
                    }
                }
            });
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "30%";
        }
        if (index == 1) {
            return "20";
        }
        return null;
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return null;
    }
}

