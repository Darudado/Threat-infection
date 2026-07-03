/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipSystemAIScript
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipwideAIFlags
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ReleaseDeviceStatsAI
implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private final IntervalUtil timer = new IntervalUtil(0.5f, 1.0f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (this.system.isCoolingDown() || this.system.isActive() || this.system.isOutOfAmmo()) {
            return;
        }
        this.timer.advance(amount);
        if (!this.timer.intervalElapsed()) {
            return;
        }
        if (!this.hasDeployedFighters()) {
            return;
        }
        ShipAPI bestTarget = this.findNearestEnemy();
        if (bestTarget != null) {
            float distanceToEnemy = Misc.getDistance((Vector2f)this.ship.getLocation(), (Vector2f)bestTarget.getLocation());
            if (distanceToEnemy < 2000.0f) {
                return;
            }
            Vector2f targetLocation = new Vector2f((ReadableVector2f)bestTarget.getLocation());
            this.ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS, 0.5f, (Object)targetLocation);
            this.ship.useSystem();
        }
    }

    private boolean hasDeployedFighters() {
        int deployedCount = 0;
        for (ShipAPI fighter : this.engine.getShips()) {
            if (!this.isDeployedFighter(fighter) || ++deployedCount < 1) continue;
            return true;
        }
        return false;
    }

    private boolean isDeployedFighter(ShipAPI fighter) {
        float deployThreshold;
        if (!fighter.isFighter() || fighter.getWing() == null || fighter.getWing().getSourceShip() != this.ship || !fighter.isAlive() || fighter.isHulk()) {
            return false;
        }
        float distanceToCarrier = Misc.getDistance((Vector2f)fighter.getLocation(), (Vector2f)this.ship.getLocation());
        return distanceToCarrier > (deployThreshold = this.ship.getCollisionRadius() + 250.0f);
    }

    private ShipAPI findNearestEnemy() {
        ShipAPI nearest = null;
        float minDist = Float.MAX_VALUE;
        float maxRange = 3000.0f;
        for (ShipAPI enemy : this.engine.getShips()) {
            float dist;
            if (enemy.getOwner() == this.ship.getOwner() || !enemy.isAlive() || enemy.isHulk() || enemy.isPhased() || !((dist = Misc.getDistance((Vector2f)this.ship.getLocation(), (Vector2f)enemy.getLocation())) < maxRange) || !(dist < minDist)) continue;
            minDist = dist;
            nearest = enemy;
        }
        return nearest;
    }
}

