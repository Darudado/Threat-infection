/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.util.IntervalUtil
 *  org.lazywizard.lazylib.MathUtils
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class SpeedNerfAura
extends BaseHullMod {
    private static final float AURA_RADIUS = 2500.0f;
    private static final float SPEED_MULT = 0.75f;
    private static final float CHECK_INTERVAL = 0.2f;
    private final IntervalUtil interval = new IntervalUtil(0.2f, 0.2f);
    private final Map<ShipAPI, Set<ShipAPI>> affectedEnemiesMap = new HashMap<ShipAPI, Set<ShipAPI>>();

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || !ship.isAlive() || Global.getCombatEngine() == null) {
            return;
        }
        this.interval.advance(amount);
        if (!this.interval.intervalElapsed()) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f shipLoc = ship.getLocation();
        Set currentlyAffected = this.affectedEnemiesMap.computeIfAbsent(ship, k -> new HashSet());
        HashSet<ShipAPI> inRangeNow = new HashSet<ShipAPI>();
        for (ShipAPI target : engine.getShips()) {
            float distance;
            if (target.getOwner() == ship.getOwner() || !target.isAlive() || target.isFighter() || !((distance = MathUtils.getDistance((Vector2f)shipLoc, (Vector2f)target.getLocation())) <= 2500.0f)) continue;
            inRangeNow.add(target);
            if (currentlyAffected.contains(target)) continue;
            this.applySpeedDebuff(ship, target);
            currentlyAffected.add(target);
        }
        Iterator iterator = currentlyAffected.iterator();
        while (iterator.hasNext()) {
            ShipAPI enemy = (ShipAPI)iterator.next();
            if (enemy.isAlive() && inRangeNow.contains(enemy)) continue;
            this.removeSpeedDebuff(ship, enemy);
            iterator.remove();
        }
        if (!ship.isAlive()) {
            this.affectedEnemiesMap.remove(ship);
        }
    }

    private void applySpeedDebuff(ShipAPI source, ShipAPI target) {
        String id = "speed_nerf_aura_" + source.getId();
        target.getMutableStats().getMaxSpeed().modifyMult(id, 0.75f);
    }

    private void removeSpeedDebuff(ShipAPI source, ShipAPI target) {
        String id = "speed_nerf_aura_" + source.getId();
        target.getMutableStats().getMaxSpeed().unmodify(id);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "25%";
        }
        if (index == 1) {
            return String.valueOf(2500);
        }
        return null;
    }
}

