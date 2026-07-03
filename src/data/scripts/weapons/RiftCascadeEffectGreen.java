/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BeamAPI
 *  com.fs.starfarer.api.combat.BeamEffectPlugin
 *  com.fs.starfarer.api.combat.BoundsAPI
 *  com.fs.starfarer.api.combat.BoundsAPI$SegmentAPI
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.EmpArcEntityAPI
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.WeaponAPI$WeaponType
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.BoundsAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class RiftCascadeEffectGreen
implements BeamEffectPlugin {
    public static Color STANDARD_RIFT_COLOR = new Color(150, 200, 100, 255);
    public static Color EXPLOSION_UNDERCOLOR = new Color(50, 80, 20, 100);
    public static Color NEGATIVE_SOURCE_COLOR = new Color(180, 255, 150, 25);
    public static String RIFTCASCADE_MINELAYER = "minelayer4";
    public static int MAX_RIFTS = 3;
    public static float UNUSED_RANGE_PER_SPAWN = 200.0f;
    public static float SPAWN_SPACING = 300.0f;
    public static float SPAWN_INTERVAL = 0.3f;
    protected Vector2f arcFrom = null;
    protected Vector2f prevMineLoc = null;
    protected boolean doneSpawningMines = false;
    protected float spawned = 0.0f;
    protected int numToSpawn = 0;
    protected float untilNextSpawn = 0.0f;
    protected float spawnDir = 0.0f;
    protected IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        this.tracker.advance(amount);
        if (this.tracker.intervalElapsed()) {
            this.spawnNegativeParticles(engine, beam);
        }
        if (beam.getBrightness() < 1.0f) {
            return;
        }
        if (this.doneSpawningMines) {
            return;
        }
        if (this.numToSpawn <= 0 && beam.getDamageTarget() != null) {
            float range = beam.getWeapon().getRange();
            float length = beam.getLengthPrevFrame();
            this.numToSpawn = (int)((range - length) / UNUSED_RANGE_PER_SPAWN) + 1;
            if (this.numToSpawn > MAX_RIFTS) {
                this.numToSpawn = MAX_RIFTS;
            }
            this.untilNextSpawn = 0.0f;
        }
        this.untilNextSpawn -= amount;
        if (this.untilNextSpawn > 0.0f) {
            return;
        }
        float perSpawn = SPAWN_SPACING;
        ShipAPI ship = beam.getSource();
        boolean spawnedMine = false;
        if (beam.getLength() > beam.getWeapon().getRange() - 10.0f) {
            float angle = Misc.getAngleInDegrees((Vector2f)beam.getFrom(), (Vector2f)beam.getRayEndPrevFrame());
            Vector2f loc = Misc.getUnitVectorAtDegreeAngle((float)angle);
            loc.scale(beam.getLength());
            Vector2f.add((Vector2f)loc, (Vector2f)beam.getFrom(), (Vector2f)loc);
            if (Math.random() < (double)0.1f) {
                this.spawnMine(ship, loc);
                spawnedMine = true;
            }
        } else if (beam.getDamageTarget() != null) {
            Vector2f arcTo = this.getNextArcLoc(engine, beam, perSpawn);
            float thickness = beam.getWidth();
            float dist = Misc.getDistance((Vector2f)this.arcFrom, (Vector2f)arcTo);
            if (dist < SPAWN_SPACING * 2.0f) {
                EmpArcEntityAPI arc = engine.spawnEmpArcVisual(this.arcFrom, null, arcTo, null, thickness, beam.getFringeColor(), Color.white);
                arc.setCoreWidthOverride(Math.max(20.0f, thickness * 0.67f));
            }
            if (Math.random() < (double)0.1f) {
                this.spawnMine(ship, arcTo);
                spawnedMine = true;
                this.arcFrom = arcTo;
            }
        }
        this.untilNextSpawn = SPAWN_INTERVAL;
        if (spawnedMine) {
            this.spawned += 1.0f;
            if (this.spawned >= (float)this.numToSpawn) {
                this.doneSpawningMines = true;
            }
        }
    }

    protected void spawnNegativeParticles(CombatEngineAPI engine, BeamAPI beam) {
        float length = beam.getLengthPrevFrame();
        if (length <= 10.0f) {
            return;
        }
        ShipAPI ship = beam.getSource();
        Color color = NEGATIVE_SOURCE_COLOR;
        float sizeMult = 0.67f;
        for (int i = 0; i < 3; ++i) {
            float rampUp = 0.25f + 0.25f * (float)Math.random();
            float dur = 1.0f + 1.0f * (float)Math.random();
            float size = 200.0f + 50.0f * (float)Math.random();
            Vector2f loc = Misc.getPointAtRadius((Vector2f)beam.getWeapon().getLocation(), (float)((size *= sizeMult) * 0.33f));
            engine.addNegativeParticle(loc, ship.getVelocity(), size, rampUp / dur, dur, color);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected Vector2f getNextArcLoc(CombatEngineAPI engine, BeamAPI beam, float perSpawn) {
        boolean hitShield;
        ShipAPI ship;
        CombatEntityAPI target = beam.getDamageTarget();
        float radiusOverride = -1.0f;
        if (target instanceof ShipAPI && (ship = (ShipAPI)target).getParentStation() != null && ship.getStationSlot() != null) {
            radiusOverride = Misc.getDistance((Vector2f)beam.getRayEndPrevFrame(), (Vector2f)ship.getParentStation().getLocation());
            target = ship.getParentStation();
        }
        if (this.arcFrom == null) {
            this.arcFrom = new Vector2f((ReadableVector2f)beam.getRayEndPrevFrame());
            float beamAngle = Misc.getAngleInDegrees((Vector2f)beam.getFrom(), (Vector2f)beam.getRayEndPrevFrame());
            float beamSourceToTarget = Misc.getAngleInDegrees((Vector2f)beam.getFrom(), (Vector2f)target.getLocation());
            this.spawnDir = Misc.getClosestTurnDirection((float)beamAngle, (float)beamSourceToTarget);
            if (this.spawnDir == 0.0f) {
                this.spawnDir = 1.0f;
            }
            if (this.prevMineLoc == null) return this.arcFrom;
            float dist = Misc.getDistance((Vector2f)this.arcFrom, (Vector2f)this.prevMineLoc);
            if (!(dist < perSpawn)) return this.arcFrom;
            perSpawn -= dist;
        }
        Vector2f targetLoc = target.getLocation();
        float targetRadius = target.getCollisionRadius();
        if (radiusOverride >= 0.0f) {
            targetRadius = radiusOverride;
        }
        boolean bl = hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getRayEndPrevFrame());
        if (hitShield) {
            perSpawn *= 0.67f;
        }
        float prevAngle = Misc.getAngleInDegrees((Vector2f)targetLoc, (Vector2f)this.arcFrom);
        float anglePerSegment = 360.0f * perSpawn / (6.28f * targetRadius);
        if (anglePerSegment > 90.0f) {
            anglePerSegment = 90.0f;
        }
        float angle = prevAngle + anglePerSegment * this.spawnDir;
        Vector2f arcTo = Misc.getUnitVectorAtDegreeAngle((float)angle);
        arcTo.scale(targetRadius);
        Vector2f.add((Vector2f)targetLoc, (Vector2f)arcTo, (Vector2f)arcTo);
        float actualRadius = Global.getSettings().getTargetingRadius(arcTo, target, hitShield);
        if (radiusOverride >= 0.0f) {
            actualRadius = radiusOverride;
        }
        actualRadius = !hitShield ? (actualRadius += 30.0f + 50.0f * (float)Math.random()) : (actualRadius += 30.0f + 50.0f * (float)Math.random());
        arcTo = Misc.getUnitVectorAtDegreeAngle((float)angle);
        arcTo.scale(actualRadius);
        Vector2f.add((Vector2f)targetLoc, (Vector2f)arcTo, (Vector2f)arcTo);
        if (!(target instanceof ShipAPI)) return arcTo;
        ShipAPI ship2 = (ShipAPI)target;
        if (hitShield) return arcTo;
        BoundsAPI bounds = ship2.getExactBounds();
        if (bounds == null) return arcTo;
        Vector2f best = null;
        float bestDist = Float.MAX_VALUE;
        for (BoundsAPI.SegmentAPI segment : bounds.getSegments()) {
            float test = Misc.getDistance((Vector2f)segment.getP1(), (Vector2f)arcTo);
            if (!(test < bestDist)) continue;
            bestDist = test;
            best = segment.getP1();
        }
        if (best == null) return arcTo;
        float explosionRadius = 150.0f;
        float sizeMult = this.getSizeMult();
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)Misc.getAngleInDegrees(best, (Vector2f)arcTo));
        dir.scale((explosionRadius *= sizeMult) * 0.9f);
        Vector2f.add((Vector2f)best, (Vector2f)dir, (Vector2f)dir);
        return dir;
    }

    protected float getSizeMult() {
        float sizeMult = 1.0f - this.spawned / (float)Math.max(1, this.numToSpawn - 1);
        sizeMult = 0.75f + (1.0f - sizeMult) * 0.5f;
        return sizeMult;
    }

    protected void spawnMine(ShipAPI source, Vector2f mineLoc) {
        CombatEngineAPI engine = Global.getCombatEngine();
        MissileAPI mine = (MissileAPI)engine.spawnProjectile(source, null, RIFTCASCADE_MINELAYER, mineLoc, (float)Math.random() * 360.0f, null);
        float sizeMult = this.getSizeMult();
        mine.setCustomData("rift_size_mult", (Object)Float.valueOf(sizeMult));
        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(source, WeaponAPI.WeaponType.ENERGY, false, mine.getDamage());
        }
        mine.getDamage().getModifier().modifyMult("mine_sizeMult", sizeMult);
        mine.getVelocity().scale(0.0f);
        mine.fadeOutThenIn(0.05f);
        mine.setFlightTime(mine.getMaxFlightTime());
        mine.addDamagedAlready((CombatEntityAPI)source);
        mine.setNoMineFFConcerns(true);
        this.prevMineLoc = mineLoc;
    }
}

