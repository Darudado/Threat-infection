/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.EveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI$SystemState
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.combat.WeaponAPI$WeaponType
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider
 *  com.fs.starfarer.api.input.InputEventAPI
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.MineStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class ThreatMineStrikeStats
extends BaseShipSystemScript
implements MineStrikeStatsAIInfoProvider {
    protected static float MINE_RANGE = 1250.0f;
    public static final float MIN_SPAWN_DIST = 75.0f;
    public static final float MIN_SPAWN_DIST_FRIGATE = 110.0f;
    public static final float LIVE_TIME = 5.0f;
    public static final Color JITTER_COLOR = new Color(255, 155, 255, 75);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 155, 255, 155);

    public static float getRange(ShipAPI ship) {
        return ship == null ? MINE_RANGE : ship.getMutableStats().getSystemRangeBonus().computeEffective(MINE_RANGE);
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            float jitterLevel = effectLevel;
            if (state == ShipSystemStatsScript.State.OUT) {
                jitterLevel = effectLevel * effectLevel;
            }
            float maxRangeBonus = 25.0f;
            float jitterRangeBonus = jitterLevel * maxRangeBonus;
            ShipSystemStatsScript.State var10000 = ShipSystemStatsScript.State.OUT;
            ship.setJitterUnder((Object)this, JITTER_UNDER_COLOR, jitterLevel, 11, 0.0f, 3.0f + jitterRangeBonus);
            ship.setJitter((Object)this, JITTER_COLOR, jitterLevel, 4, 0.0f, 0.0f + jitterRangeBonus);
            if (state != ShipSystemStatsScript.State.IN) {
                if (effectLevel >= 1.0f) {
                    Vector2f target = ship.getMouseTarget();
                    if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS)) {
                        target = (Vector2f)ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
                    }
                    if (target != null) {
                        float max;
                        float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)target);
                        if (dist > (max = this.getMaxRange(ship) + ship.getCollisionRadius())) {
                            float dir = Misc.getAngleInDegrees((Vector2f)ship.getLocation(), (Vector2f)target);
                            target = Misc.getUnitVectorAtDegreeAngle((float)dir);
                            target.scale(max);
                            Vector2f.add((Vector2f)target, (Vector2f)ship.getLocation(), (Vector2f)target);
                        }
                        if ((target = this.findClearLocation(ship, target)) != null) {
                            this.spawnMine(ship, target);
                        }
                    }
                } else {
                    var10000 = ShipSystemStatsScript.State.OUT;
                }
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    public void spawnMine(ShipAPI source, Vector2f mineLoc) {
        float start;
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f currLoc = Misc.getPointAtRadius((Vector2f)mineLoc, (float)(30.0f + (float)Math.random() * 30.0f));
        for (float angle = start = (float)Math.random() * 360.0f; angle < start + 390.0f; angle += 30.0f) {
            if (angle != start) {
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle((float)angle);
                loc.scale(50.0f + (float)Math.random() * 30.0f);
                currLoc = Vector2f.add((Vector2f)mineLoc, (Vector2f)loc, (Vector2f)new Vector2f());
            }
            for (MissileAPI other : Global.getCombatEngine().getMissiles()) {
                float dist;
                if (!other.isMine() || !((dist = Misc.getDistance((Vector2f)currLoc, (Vector2f)other.getLocation())) < other.getCollisionRadius() + 40.0f)) continue;
                currLoc = null;
                break;
            }
            if (currLoc != null) break;
        }
        if (currLoc == null) {
            currLoc = Misc.getPointAtRadius((Vector2f)mineLoc, (float)(30.0f + (float)Math.random() * 30.0f));
        }
        MissileAPI mine = (MissileAPI)engine.spawnProjectile(source, (WeaponAPI)null, "minelayer3", currLoc, (float)Math.random() * 360.0f, (Vector2f)null);
        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
        }
        float fadeInTime = 0.5f;
        mine.getVelocity().scale(0.0f);
        mine.fadeOutThenIn(fadeInTime);
        Global.getCombatEngine().addPlugin(this.createMissileJitterPlugin(mine, fadeInTime));
        float liveTime = 5.0f;
        mine.setFlightTime(mine.getMaxFlightTime() - liveTime);
        Global.getSoundPlayer().playSound("mine_teleport", 1.0f, 1.0f, mine.getLocation(), mine.getVelocity());
    }

    protected EveryFrameCombatPlugin createMissileJitterPlugin(final MissileAPI mine, final float fadeInTime) {
        return new BaseEveryFrameCombatPlugin(){
            float elapsed = 0.0f;

            public void advance(float amount, List<InputEventAPI> events) {
                if (!Global.getCombatEngine().isPaused()) {
                    this.elapsed += amount;
                    float jitterLevel = mine.getCurrentBaseAlpha();
                    jitterLevel = jitterLevel < 0.5f ? (jitterLevel *= 2.0f) : (1.0f - jitterLevel) * 2.0f;
                    float jitterRange = 1.0f - mine.getCurrentBaseAlpha();
                    float maxRangeBonus = 50.0f;
                    float jitterRangeBonus = jitterRange * maxRangeBonus;
                    Color c = JITTER_UNDER_COLOR;
                    c = Misc.setAlpha((Color)c, (int)70);
                    mine.setJitter((Object)this, c, jitterLevel, 15, jitterRangeBonus * 0.0f, jitterRangeBonus);
                    if (jitterLevel >= 1.0f || this.elapsed > fadeInTime) {
                        Global.getCombatEngine().removePlugin((EveryFrameCombatPlugin)this);
                    }
                }
            }
        };
    }

    protected float getMaxRange(ShipAPI ship) {
        return this.getMineRange(ship);
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return null;
        }
        Vector2f target = ship.getMouseTarget();
        if (target != null) {
            float max;
            float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)target);
            return dist > (max = this.getMaxRange(ship) + ship.getCollisionRadius()) ? "\u8d85\u51fa\u8303\u56f4" : "\u5c31\u7eea";
        }
        return null;
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return ship.getMouseTarget() != null;
    }

    private Vector2f findClearLocation(ShipAPI ship, Vector2f dest) {
        if (this.isLocationClear(dest)) {
            return dest;
        }
        float incr = 50.0f;
        WeightedRandomPicker tested = new WeightedRandomPicker();
        for (float distIndex = 1.0f; distIndex <= 32.0f; distIndex *= 2.0f) {
            float start;
            for (float angle = start = (float)Math.random() * 360.0f; angle < start + 360.0f; angle += 60.0f) {
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle((float)angle);
                loc.scale(incr * distIndex);
                Vector2f.add((Vector2f)dest, (Vector2f)loc, (Vector2f)loc);
                tested.add((Object)loc);
                if (!this.isLocationClear(loc)) continue;
                return loc;
            }
        }
        if (tested.isEmpty()) {
            return dest;
        }
        return (Vector2f)tested.pick();
    }

    private boolean isLocationClear(Vector2f loc) {
        for (ShipAPI other : Global.getCombatEngine().getShips()) {
            if (other.isShuttlePod() || other.isFighter()) continue;
            Vector2f otherLoc = other.getShieldCenterEvenIfNoShield();
            float otherR = other.getShieldRadiusEvenIfNoShield();
            if (other.isPiece()) {
                otherLoc = other.getLocation();
                otherR = other.getCollisionRadius();
            }
            float dist = Misc.getDistance((Vector2f)loc, (Vector2f)otherLoc);
            float checkDist = 75.0f;
            if (other.isFrigate()) {
                checkDist = 110.0f;
            }
            if (!(dist < otherR + checkDist)) continue;
            return false;
        }
        for (com.fs.starfarer.api.combat.CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
            float dist = Misc.getDistance((Vector2f)loc, (Vector2f)other.getLocation());
            if (!(dist < other.getCollisionRadius() + 75.0f)) continue;
            return false;
        }
        return true;
    }

    public float getFuseTime() {
        return 3.0f;
    }

    public float getMineRange(ShipAPI ship) {
        return ThreatMineStrikeStats.getRange(ship);
    }
}

