/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI$SystemState
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ReleaseDeviceStats
extends BaseShipSystemScript {
    public static final Object KEY_JITTER = new Object();
    public static final Object KEY_TEMP_INVULNERABLE = new Object();
    public static final Color JITTER_COLOR = new Color(100, 165, 255, 155);
    public static final Color JITTER_UNDER_COLOR = new Color(100, 165, 255, 255);
    public static final Color TARGET_JITTER_COLOR = new Color(255, 200, 100, 200);
    private static final float MAX_RANGE = 3000.0f;
    private static final float MIN_DISTANCE_FROM_SHIP = 300.0f;
    private static final float TEMP_INVULNERABILITY_DURATION = 1.5f;
    private Map<String, Float> fighterInvulnerability = new HashMap<String, Float>();
    private Vector2f targetLocation = null;

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            if (effectLevel > 0.0f) {
                this.updateTargetLocation(ship);
            }
            if (this.targetLocation != null && effectLevel > 0.0f) {
                this.showTargetLocation(ship, this.targetLocation, effectLevel);
            }
            if (effectLevel > 0.0f) {
                float jitterLevel = effectLevel;
                boolean firstTime = false;
                String fightersKey = ship.getId() + "_recall_device_target";
                List<ShipAPI> fightersList = null;
                if (!Global.getCombatEngine().getCustomData().containsKey(fightersKey)) {
                    fightersList = ReleaseDeviceStats.getFighters(ship);
                    Global.getCombatEngine().getCustomData().put(fightersKey, fightersList);
                    firstTime = true;
                } else {
                    Object data = Global.getCombatEngine().getCustomData().get(fightersKey);
                    if (data instanceof List) {
                        fightersList = (List<ShipAPI>)data;
                    }
                }
                if (fightersList == null) {
                    fightersList = new ArrayList<ShipAPI>();
                }
                for (ShipAPI fighter : fightersList) {
                    if (fighter.isHulk()) continue;
                    float maxRangeBonus = fighter.getCollisionRadius() * 1.0f;
                    float jitterRangeBonus = 5.0f + jitterLevel * maxRangeBonus;
                    if (firstTime) {
                        Global.getSoundPlayer().playSound("system_phase_skimmer", 1.0f, 0.5f, fighter.getLocation(), fighter.getVelocity());
                    }
                    fighter.setJitter(KEY_JITTER, JITTER_COLOR, jitterLevel, 10, 0.0f, jitterRangeBonus);
                    fighter.setJitterUnder(KEY_JITTER, JITTER_UNDER_COLOR, jitterLevel, 15, 0.0f, jitterRangeBonus + 5.0f);
                    if (fighter.isAlive()) {
                        fighter.setPhased(true);
                    }
                    if (state == ShipSystemStatsScript.State.IN) {
                        float alpha = 1.0f - effectLevel * 0.5f;
                        fighter.setExtraAlphaMult(alpha);
                    }
                    if (effectLevel != 1.0f) continue;
                    if (this.targetLocation != null) {
                        Vector2f clearLocation = this.findClearLocationForFighter(fighter, this.targetLocation);
                        this.teleportFighterToLocation(fighter, clearLocation);
                        continue;
                    }
                    Vector2f defaultLocation = this.getDefaultTeleportLocation(ship, fighter);
                    this.teleportFighterToLocation(fighter, defaultLocation);
                }
            }
            this.updateFighterInvulnerability();
        }
    }

    private void updateFighterInvulnerability() {
        float currentTime = Global.getCombatEngine().getTotalElapsedTime(false);
        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Float> entry : this.fighterInvulnerability.entrySet()) {
            String fighterId = entry.getKey();
            float endTime = entry.getValue().floatValue();
            ShipAPI fighter = this.findFighterById(fighterId);
            if (fighter == null || !fighter.isAlive()) {
                toRemove.add(fighterId);
                continue;
            }
            if (currentTime >= endTime) {
                fighter.setPhased(false);
                fighter.setExtraAlphaMult(1.0f);
                fighter.setJitter(KEY_TEMP_INVULNERABLE, JITTER_COLOR, 0.0f, 0, 0.0f, 0.0f);
                toRemove.add(fighterId);
                continue;
            }
            float remaining = endTime - currentTime;
            float intensity = Math.min(1.0f, remaining / 1.5f);
            fighter.setJitter(KEY_TEMP_INVULNERABLE, JITTER_COLOR, intensity, 5, 0.0f, 10.0f);
            if (fighter.isPhased()) continue;
            fighter.setPhased(true);
        }
        for (String fighterId : toRemove) {
            this.fighterInvulnerability.remove(fighterId);
        }
    }

    private ShipAPI findFighterById(String fighterId) {
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.getId().equals(fighterId)) continue;
            return ship;
        }
        return null;
    }

    private void updateTargetLocation(ShipAPI ship) {
        this.targetLocation = ship.getMouseTarget();
        if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS)) {
            this.targetLocation = (Vector2f)ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS);
        }
        if (this.targetLocation != null) {
            float dir;
            float maxRange;
            float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)this.targetLocation);
            if (dist > (maxRange = this.getMaxRange(ship))) {
                dir = Misc.getAngleInDegrees((Vector2f)ship.getLocation(), (Vector2f)this.targetLocation);
                this.targetLocation = Misc.getUnitVectorAtDegreeAngle((float)dir);
                this.targetLocation.scale(maxRange);
                Vector2f.add((Vector2f)this.targetLocation, (Vector2f)ship.getLocation(), (Vector2f)this.targetLocation);
            }
            if (dist < 300.0f) {
                dir = Misc.getAngleInDegrees((Vector2f)ship.getLocation(), (Vector2f)this.targetLocation);
                this.targetLocation = Misc.getUnitVectorAtDegreeAngle((float)dir);
                this.targetLocation.scale(300.0f);
                Vector2f.add((Vector2f)this.targetLocation, (Vector2f)ship.getLocation(), (Vector2f)this.targetLocation);
            }
        }
    }

    private float getMaxRange(ShipAPI ship) {
        return 3000.0f;
    }

    private Vector2f findClearLocationForFighter(ShipAPI fighter, Vector2f target) {
        if (this.isLocationClear(fighter, target)) {
            return target;
        }
        float increment = fighter.getCollisionRadius() * 2.0f;
        WeightedRandomPicker tested = new WeightedRandomPicker();
        for (float distIndex = 1.0f; distIndex <= 8.0f; distIndex *= 2.0f) {
            float start;
            for (float angle = start = (float)Math.random() * 360.0f; angle < start + 360.0f; angle += 60.0f) {
                Vector2f loc = Misc.getUnitVectorAtDegreeAngle((float)angle);
                loc.scale(increment * distIndex);
                Vector2f.add((Vector2f)target, (Vector2f)loc, (Vector2f)loc);
                tested.add((Object)loc);
                if (!this.isLocationClear(fighter, loc)) continue;
                return loc;
            }
        }
        return target;
    }

    private boolean isLocationClear(ShipAPI fighter, Vector2f loc) {
        float dist;
        for (ShipAPI other : Global.getCombatEngine().getShips()) {
            float requiredDist;
            if (other == fighter || other.isShuttlePod() || !((dist = Misc.getDistance((Vector2f)loc, (Vector2f)other.getLocation())) < (requiredDist = fighter.getCollisionRadius() + other.getCollisionRadius() + 50.0f))) continue;
            return false;
        }
        for (com.fs.starfarer.api.combat.CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
            dist = Misc.getDistance((Vector2f)loc, (Vector2f)other.getLocation());
            if (!(dist < other.getCollisionRadius() + fighter.getCollisionRadius() + 25.0f)) continue;
            return false;
        }
        return true;
    }

    private void showTargetLocation(ShipAPI ship, Vector2f location, float effectLevel) {
        float radius = 50.0f + effectLevel * 100.0f;
        Global.getCombatEngine().addHitParticle(location, new Vector2f(), radius * 2.0f, 0.8f, 0.2f, TARGET_JITTER_COLOR);
        int particles = 8 + (int)(effectLevel * 8.0f);
        for (int i = 0; i < particles; ++i) {
            float angle = (float)i / (float)particles * 360.0f + effectLevel * 360.0f;
            float distance = radius * 0.8f;
            Vector2f offset = new Vector2f((float)Math.cos(Math.toRadians(angle)) * distance, (float)Math.sin(Math.toRadians(angle)) * distance);
            Vector2f particlePos = Vector2f.add((Vector2f)location, (Vector2f)offset, null);
            Global.getCombatEngine().addHitParticle(particlePos, new Vector2f(), 8.0f, 0.8f, 0.15f, TARGET_JITTER_COLOR);
        }
        float dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)location);
        if (dist > this.getMaxRange(ship)) {
            Global.getCombatEngine().addHitParticle(location, new Vector2f(), radius * 3.0f, 1.0f, 0.3f, Color.RED);
        }
    }

    private void teleportFighterToLocation(ShipAPI fighter, Vector2f location) {
        Global.getSoundPlayer().playSound("system_phase_skimmer", 1.0f, 0.8f, location, new Vector2f());
        this.addTeleportEffects(fighter.getLocation());
        fighter.getLocation().set((ReadableVector2f)location);
        this.addTeleportEffects(location);
        fighter.setExtraAlphaMult(1.0f);
        fighter.getVelocity().set((ReadableVector2f)new Vector2f());
        this.applyTemporaryInvulnerability(fighter);
    }

    private void applyTemporaryInvulnerability(ShipAPI fighter) {
        float invulnEndTime = Global.getCombatEngine().getTotalElapsedTime(false) + 1.5f;
        this.fighterInvulnerability.put(fighter.getId(), Float.valueOf(invulnEndTime));
        fighter.setPhased(true);
        fighter.setJitter(KEY_TEMP_INVULNERABLE, JITTER_COLOR, 1.0f, 5, 0.0f, 10.0f);
    }

    private void addTeleportEffects(Vector2f location) {
        Global.getCombatEngine().spawnExplosion(location, new Vector2f(), JITTER_COLOR, 80.0f, 0.4f);
        for (int i = 0; i < 15; ++i) {
            float angle = (float)(Math.random() * 360.0);
            float speed = 30.0f + (float)(Math.random() * 70.0);
            Vector2f velocity = new Vector2f((float)Math.cos(Math.toRadians(angle)) * speed, (float)Math.sin(Math.toRadians(angle)) * speed);
            Global.getCombatEngine().addSmokeParticle(location, velocity, 15.0f + (float)(Math.random() * 25.0), 0.8f, 1.2f, JITTER_COLOR);
        }
        try {
            Global.getCombatEngine().spawnEmpArc(null, location, null, null, DamageType.OTHER, 0.0f, 0.0f, 500.0f, "tachyon_lance_emp_impact", 15.0f, new Color(100, 165, 255, 255), new Color(255, 255, 255, 255));
        }
        catch (Exception e) {
            this.addAlternativeEmpEffect(location);
        }
    }

    private void addAlternativeEmpEffect(Vector2f location) {
        for (int i = 0; i < 8; ++i) {
            float angle = (float)(Math.random() * 360.0);
            float distance = 50.0f + (float)(Math.random() * 100.0);
            Vector2f endPoint = new Vector2f(location.x + (float)Math.cos(Math.toRadians(angle)) * distance, location.y + (float)Math.sin(Math.toRadians(angle)) * distance);
            for (int j = 0; j < 3; ++j) {
                float progress = (float)j / 3.0f;
                Vector2f particlePos = new Vector2f(location.x + (endPoint.x - location.x) * progress, location.y + (endPoint.y - location.y) * progress);
                Global.getCombatEngine().addHitParticle(particlePos, new Vector2f(), 5.0f + (float)(Math.random() * 5.0), 1.0f, 0.1f, new Color(100, 165, 255, 255));
            }
        }
    }

    private Vector2f getDefaultTeleportLocation(ShipAPI carrier, ShipAPI fighter) {
        Vector2f baseLocation = new Vector2f((ReadableVector2f)carrier.getLocation());
        float angle = (float)(Math.random() * 360.0);
        float distance = carrier.getCollisionRadius() + fighter.getCollisionRadius() + 100.0f;
        baseLocation.x += (float)Math.cos(Math.toRadians(angle)) * distance;
        baseLocation.y += (float)Math.sin(Math.toRadians(angle)) * distance;
        return baseLocation;
    }

    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        ArrayList<ShipAPI> result = new ArrayList<ShipAPI>();
        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter() || ship.getWing() == null || ship.getWing().getSourceShip() != carrier) continue;
            result.add(ship);
        }
        return result;
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            String fightersKey = ship.getId() + "_recall_device_target";
            Global.getCombatEngine().getCustomData().remove(fightersKey);
            this.targetLocation = null;
            List<ShipAPI> fighters = ReleaseDeviceStats.getFighters(ship);
            for (ShipAPI fighter : fighters) {
                if (!fighter.isAlive()) continue;
                fighter.setPhased(false);
                fighter.setExtraAlphaMult(1.0f);
                fighter.setJitter(KEY_TEMP_INVULNERABLE, JITTER_COLOR, 0.0f, 0, 0.0f, 0.0f);
            }
            this.fighterInvulnerability.clear();
        }
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("\u4f20\u9001\u8230\u8f7d\u673a\u81f3\u9f20\u6807\u4f4d\u7f6e", false);
        }
        return null;
    }

    public String getInfoText(ShipAPI ship, ShipSystemAPI system) {
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
            return dist > (max = this.getMaxRange(ship)) ? "\u8d85\u51fa\u8303\u56f4" : "\u5c31\u7eea";
        }
        return null;
    }

    public boolean isUsable(ShipAPI ship, ShipSystemAPI system) {
        return ship.getMouseTarget() != null;
    }
}

