/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatFleetManagerAPI
 *  com.fs.starfarer.api.combat.DeployedFleetMemberAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript
 *  com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript$SwarmConstructableType
 *  com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript$SwarmConstructableVariant
 *  com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript$SwarmConstructionData
 *  com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 */
package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AutonomousFabricationSystemScript
extends ConstructionSwarmSystemScript {
    protected static boolean wxdfInited = false;
    private static final Map<Object, Queue<String>> recentBuilds = new HashMap<Object, Queue<String>>();
    private static final int RECENT_BUILD_HISTORY_SIZE = 3;

    public static void init() {
        if (!ConstructionSwarmSystemScript.inited) {
            ConstructionSwarmSystemScript.init();
        }
        if (!wxdfInited) {
            wxdfInited = true;
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxdf"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxby"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxzgd"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxjt"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxct"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxqy"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxmds"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxbb"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxml"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxhh"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxatls"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxey"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxfs"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxgs"));
            CONSTRUCTABLE.add(new ConstructionSwarmSystemScript.SwarmConstructableVariant(ConstructionSwarmSystemScript.SwarmConstructableType.COMBAT_UNIT, "wxxz"));
            AutonomousFabricationSystemScript.updateMinMaxValues();
        }
    }

    private static void updateMinMaxValues() {
        MIN_CR = 1.0f;
        MIN_DP = 100.0f;
        MIN_FRAGMENTS = 500;
        MAX_FRAGMENTS = 0;
        for (ConstructionSwarmSystemScript.SwarmConstructableVariant v : CONSTRUCTABLE) {
            MIN_CR = Math.min(v.cr, MIN_CR);
            MIN_DP = Math.min(v.dp, MIN_DP);
            MIN_FRAGMENTS = Math.min(v.fragments, MIN_FRAGMENTS);
            MAX_FRAGMENTS = Math.max(v.fragments, MAX_FRAGMENTS);
        }
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        AutonomousFabricationSystemScript.init();
        super.apply(stats, id, state, effectLevel);
    }

    public ConstructionSwarmSystemScript.SwarmConstructableVariant pickVariant(ShipAPI ship) {
        AutonomousFabricationSystemScript.init();
        ConstructionSwarmSystemScript.SwarmConstructableVariant pick = this.pickVariantWithDiversity(ship);
        if (pick != null && this.isOurCustomShip(pick.variantId)) {
            this.recordBuild(ship, pick.variantId);
        }
        return pick;
    }

    private ConstructionSwarmSystemScript.SwarmConstructableVariant pickVariantWithDiversity(ShipAPI ship) {
        float weight;
        CombatEngineAPI engine = Global.getCombatEngine();
        CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
        if (manager == null) {
            return null;
        }
        RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)ship);
        int fragments = swarm == null ? 0 : swarm.getNumActiveMembers();
        int dpLeft = manager.getMaxStrength() - manager.getCurrStrength();
        float cr = ship.getCurrentCR();
        Map<String, Integer> currentFleetComposition = this.getCurrentFleetComposition(manager);
        Queue<String> recentBuilds = this.getRecentBuilds(ship);
        WeightedRandomPicker picker = new WeightedRandomPicker();
        for (ConstructionSwarmSystemScript.SwarmConstructableVariant variant : CONSTRUCTABLE) {
            if (!(variant.dp <= (float)dpLeft) || !(variant.cr <= cr) || variant.fragments > fragments) continue;
            weight = this.calculateVariantWeight(variant, currentFleetComposition, recentBuilds);
            picker.add((Object)variant, weight);
        }
        if (picker.isEmpty()) {
            for (ConstructionSwarmSystemScript.SwarmConstructableVariant variant : CONSTRUCTABLE) {
                if (!(variant.dp <= (float)dpLeft) || !(variant.cr <= cr)) continue;
                weight = this.calculateVariantWeight(variant, currentFleetComposition, recentBuilds) * 0.5f;
                picker.add((Object)variant, weight);
            }
        }
        return picker.isEmpty() ? null : (ConstructionSwarmSystemScript.SwarmConstructableVariant)picker.pick();
    }

    private float calculateVariantWeight(ConstructionSwarmSystemScript.SwarmConstructableVariant variant, Map<String, Integer> currentFleetComposition, Queue<String> recentBuilds) {
        int countInFleet;
        float weight = 1.0f;
        switch (variant.size) {
            case FRIGATE: {
                weight = 2.0f;
                break;
            }
            case DESTROYER: {
                weight = 1.5f;
                break;
            }
            case CRUISER: {
                weight = 1.2f;
                break;
            }
            case CAPITAL_SHIP: {
                weight = 1.0f;
            }
        }
        if (this.isOurCustomShip(variant.variantId)) {
            weight *= 1.5f;
        }
        if ((countInFleet = currentFleetComposition.getOrDefault(variant.variantId, 0).intValue()) > 0) {
            weight /= 1.0f + (float)countInFleet * 0.5f;
        }
        if (recentBuilds.contains(variant.variantId)) {
            weight *= 0.3f;
        }
        int sameSizeCount = 0;
        for (String shipId : currentFleetComposition.keySet()) {
            ConstructionSwarmSystemScript.SwarmConstructableVariant other = this.getVariantById(shipId);
            if (other == null || other.size != variant.size) continue;
            sameSizeCount += currentFleetComposition.get(shipId).intValue();
        }
        if (sameSizeCount < 3) {
            weight *= 1.5f;
        }
        return weight;
    }

    private Map<String, Integer> getCurrentFleetComposition(CombatFleetManagerAPI manager) {
        HashMap<String, Integer> composition = new HashMap<String, Integer>();
        for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
            ShipAPI ship = dfm.getShip();
            if (ship == null || ship.isFighter()) continue;
            String variantId = ship.getVariant().getHullVariantId();
            composition.put(variantId, composition.getOrDefault(variantId, 0) + 1);
        }
        return composition;
    }

    private ConstructionSwarmSystemScript.SwarmConstructableVariant getVariantById(String variantId) {
        for (ConstructionSwarmSystemScript.SwarmConstructableVariant variant : CONSTRUCTABLE) {
            if (!variant.variantId.equals(variantId)) continue;
            return variant;
        }
        return null;
    }

    private void recordBuild(ShipAPI ship, String variantId) {
        Object key = ship.getFleetMember() != null ? ship.getFleetMember().getId() : ship;
        if (!recentBuilds.containsKey(key)) {
            recentBuilds.put(key, new LinkedList());
        }
        Queue<String> history = recentBuilds.get(key);
        history.add(variantId);
        while (history.size() > 3) {
            history.poll();
        }
    }

    private Queue<String> getRecentBuilds(ShipAPI ship) {
        Object key = ship.getFleetMember() != null ? ship.getFleetMember().getId() : ship;
        return recentBuilds.getOrDefault(key, new LinkedList());
    }

    private boolean isOurCustomShip(String variantId) {
        return variantId.equals("wxdf") || variantId.equals("wxby") || variantId.equals("wxzgd") || variantId.equals("wxjt") || variantId.equals("wxct") || variantId.equals("wxqy") || variantId.equals("wxmds") || variantId.equals("wxbb") || variantId.equals("wxml") || variantId.equals("wxhh") || variantId.equals("wxatls") || variantId.equals("wxey") || variantId.equals("wxfs") || variantId.equals("wxgs") || variantId.equals("wxxz");
    }

    protected void launchSwarm(ShipAPI ship) {
        super.launchSwarm(ship);
        try {
            CombatEngineAPI engine = Global.getCombatEngine();
            CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
            for (DeployedFleetMemberAPI dfm : manager.getDeployedCopyDFM()) {
                RoilingSwarmEffect swarm;
                ShipAPI deployedShip = dfm.getShip();
                if (deployedShip == null || !deployedShip.isFighter() || deployedShip.getWing() == null || deployedShip.getWing().getSourceShip() != ship || (swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)deployedShip)) == null || !(swarm.custom1 instanceof ConstructionSwarmSystemScript.SwarmConstructionData)) continue;
                ConstructionSwarmSystemScript.SwarmConstructionData data = (ConstructionSwarmSystemScript.SwarmConstructionData)swarm.custom1;
                if (!this.isOurCustomShip(data.variantId)) continue;
                swarm.custom2 = "CUSTOM_SHIP";
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static int getNumFabricatorsDeployed(CombatFleetManagerAPI manager) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.getNumFabricatorsDeployed((CombatFleetManagerAPI)manager);
    }

    public static int getNumOverseersDeployed(CombatFleetManagerAPI manager) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.getNumOverseersDeployed((CombatFleetManagerAPI)manager);
    }

    public static int getNumHivesDeployed(CombatFleetManagerAPI manager) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.getNumHivesDeployed((CombatFleetManagerAPI)manager);
    }

    public static float getCombatWeightDeployed(CombatFleetManagerAPI manager) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.getCombatWeightDeployed((CombatFleetManagerAPI)manager);
    }

    public static int getCombatDeployed(CombatFleetManagerAPI manager, ShipAPI.HullSize size) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.getCombatDeployed((CombatFleetManagerAPI)manager, (ShipAPI.HullSize)size);
    }

    public static boolean constructionSwarmWillBuild(ShipAPI ship, String tag, ShipAPI.HullSize size) {
        AutonomousFabricationSystemScript.init();
        return ConstructionSwarmSystemScript.constructionSwarmWillBuild((ShipAPI)ship, (String)tag, (ShipAPI.HullSize)size);
    }
}

