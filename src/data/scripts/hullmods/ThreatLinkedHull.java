/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipEngineControllerAPI
 *  com.fs.starfarer.api.combat.ShipEngineControllerAPI$ShipEngineAPI
 *  com.fs.starfarer.api.combat.ShipVariantAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.loading.HullModSpecAPI
 *  com.fs.starfarer.api.loading.VariantSource
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import java.util.ArrayList;
import java.util.List;

public class ThreatLinkedHull
extends BaseHullMod {
    private static void advanceChild(ShipAPI child, ShipAPI parent) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null && parent.isAlive()) {
            ShipEngineControllerAPI cec;
            if (ec.isAccelerating()) {
                child.giveCommand(ShipCommand.ACCELERATE, null, 0);
            }
            if (ec.isAcceleratingBackwards()) {
                child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            }
            if (ec.isDecelerating()) {
                child.giveCommand(ShipCommand.DECELERATE, null, 0);
            }
            if (ec.isStrafingLeft()) {
                child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            }
            if (ec.isStrafingRight()) {
                child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            }
            if (ec.isTurningLeft()) {
                child.giveCommand(ShipCommand.TURN_LEFT, null, 0);
            }
            if (ec.isTurningRight()) {
                child.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            }
            if ((cec = child.getEngineController()) != null && (ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
                child.getEngineController().forceFlameout(true);
            }
        }
        float objectiveAmount = Global.getCombatEngine().getElapsedInLastFrame();
        if (Global.getCombatEngine().isPaused()) {
            objectiveAmount = 0.0f;
        }
        objectiveAmount *= Global.getCombatEngine().getTimeMult().getModifiedValue();
        float currLevel = child.getMutableStats().getZeroFluxMinimumFluxLevel().getFlatMod();
        if (parent.getFluxLevel() > parent.getMutableStats().getZeroFluxMinimumFluxLevel().getModifiedValue() || parent.getMutableStats().getZeroFluxMinimumFluxLevel().getModifiedValue() == 0.0f && parent.getCurrFlux() > 0.0f) {
            if ((currLevel -= objectiveAmount * 2.0f) < -2.0f) {
                currLevel = -2.0f;
            }
        } else if ((currLevel += objectiveAmount * 2.0f) > 2.0f) {
            currLevel = 2.0f;
        }
        child.getMutableStats().getZeroFluxMinimumFluxLevel().modifyFlat("hxmo_linkedhull", currLevel);
    }

    private static void advanceParent(ShipAPI parent, List<ShipAPI> children) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            float enginePerformance = ThreatLinkedHull.calculateEnginePerformance(parent, children);
            parent.getMutableStats().getAcceleration().modifyMult("hxmo_linkedhull", enginePerformance);
            parent.getMutableStats().getDeceleration().modifyMult("hxmo_linkedhull", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("hxmo_linkedhull", enginePerformance);
        }
    }

    private static float calculateEnginePerformance(ShipAPI parent, List<ShipAPI> children) {
        float totalEngines = 0.0f;
        float activeEngines = 0.0f;
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            for (ShipEngineControllerAPI.ShipEngineAPI engine : ec.getShipEngines()) {
                totalEngines += 1.0f;
                if (!engine.isActive() || engine.isDisabled() || engine.isPermanentlyDisabled()) continue;
                activeEngines += engine.getContribution();
            }
        }
        for (ShipAPI child : children) {
            ShipEngineControllerAPI cec;
            if (child.getParentStation() != parent || !child.isAlive() || (cec = child.getEngineController()) == null) continue;
            for (ShipEngineControllerAPI.ShipEngineAPI engine : cec.getShipEngines()) {
                totalEngines += 1.0f;
                if (!engine.isActive() || engine.isDisabled() || engine.isPermanentlyDisabled()) continue;
                activeEngines += engine.getContribution();
            }
        }
        if (totalEngines > 0.0f) {
            float performance = activeEngines / totalEngines;
            return Math.max(0.5f, Math.min(1.5f, performance));
        }
        return 1.0f;
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        List children;
        ShipAPI parent = ship.getParentStation();
        if (parent != null) {
            ThreatLinkedHull.advanceChild(ship, parent);
        }
        if ((children = ship.getChildModulesCopy()) != null && !children.isEmpty()) {
            ThreatLinkedHull.advanceParent(ship, children);
        }
    }

    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        if (member == null) {
            return;
        }
        if (member.getFleetData() == null || member.getFleetData().getFleet() == null || !member.getFleetData().getFleet().isPlayerFleet()) {
            return;
        }
        ShipVariantAPI memberVariant = member.getVariant();
        if (memberVariant.isStockVariant()) {
            memberVariant = memberVariant.clone();
            memberVariant.setSource(VariantSource.REFIT);
            member.setVariant(memberVariant, false, false);
        }
        boolean changesMade = false;
        if (memberVariant.getStationModules() != null) {
            int index = 0;
            for (String slotId : memberVariant.getStationModules().keySet()) {
                ShipVariantAPI childVariant = memberVariant.getModuleVariant(slotId);
                if (childVariant == null) continue;
                boolean childChangesMade = false;
                for (String modId : memberVariant.getHullMods()) {
                    HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                    if (!modSpec.hasTag("dmod") || childVariant.hasHullMod(modId)) continue;
                    if (childVariant.isStockVariant()) {
                        childVariant = childVariant.clone();
                        childVariant.setSource(VariantSource.REFIT);
                        childVariant.setOriginalVariant(null);
                        childVariant.setHullVariantId(childVariant.getHullVariantId() + "_" + index);
                    }
                    childVariant.addPermaMod(modId);
                    changesMade = true;
                    childChangesMade = true;
                }
                ArrayList<String> childHullMods = new ArrayList<String>(childVariant.getHullMods());
                for (String modId : childHullMods) {
                    HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                    if (!modSpec.hasTag("dmod") || memberVariant.hasHullMod(modId)) continue;
                    if (childVariant.isStockVariant()) {
                        childVariant = childVariant.clone();
                        childVariant.setSource(VariantSource.REFIT);
                        childVariant.setOriginalVariant(null);
                        childVariant.setHullVariantId(childVariant.getHullVariantId() + "_" + index);
                    }
                    childVariant.removePermaMod(modId);
                    changesMade = true;
                    childChangesMade = true;
                }
                ++index;
                if (!childChangesMade) continue;
                memberVariant.setModuleVariant(slotId, childVariant);
            }
        }
        if (changesMade) {
            member.getFleetData().setSyncNeeded();
        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "\u5f15\u64ce\u540c\u6b65";
        }
        if (index == 1) {
            return "CR\u540c\u6b65";
        }
        if (index == 2) {
            return "D\u63d2\u540c\u6b65";
        }
        return null;
    }
}

