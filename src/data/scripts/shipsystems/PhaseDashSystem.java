/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipEngineControllerAPI$ShipEngineAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI$SystemState
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class PhaseDashSystem
extends BaseShipSystemScript {
    private static final Color WARP_CORE = new Color(180, 220, 255, 255);
    private static final Color WARP_STREAK = new Color(120, 180, 255, 220);
    private static final Color WARP_GLOW = new Color(80, 160, 255, 180);
    private static final Color WARP_HALO = new Color(200, 220, 255, 150);
    private static final Color WARP_SMOKE = new Color(100, 140, 200, 100);
    private static final Color WARP_DARK = new Color(40, 80, 160, 200);
    private static final float MAX_SPEED_BONUS = 120.0f;
    private static final float ACCEL_BONUS = 250.0f;
    private static final float TURN_BONUS = 120.0f;
    private static final float DAMAGE_REDUCTION = 0.6f;
    private static final float TIME_MULT = 2.0f;
    private boolean phaseStarted = false;
    private boolean phaseEnded = false;
    private float particleTimer = 0.0f;
    private List<ParticleEffect> activeEffects = new ArrayList<ParticleEffect>();

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship == null) {
            return;
        }
        CombatEngineAPI engine = Global.getCombatEngine();
        String systemId = id + "_" + ship.getId();
        this.updateParticles(engine);
        switch (state) {
            case IN: {
                this.applyPhaseEffects(stats, systemId, effectLevel, ship, true);
                ship.setPhased(true);
                ship.setExtraAlphaMult(1.0f - 0.85f * effectLevel);
                ship.setApplyExtraAlphaToEngines(true);
                if (this.phaseStarted) break;
                this.createWarpInEffect(ship, engine);
                this.phaseStarted = true;
                this.phaseEnded = false;
                break;
            }
            case ACTIVE: {
                this.applyPhaseEffects(stats, systemId, 1.0f, ship, true);
                ship.setPhased(true);
                ship.setExtraAlphaMult(0.15f);
                ship.setApplyExtraAlphaToEngines(true);
                this.createActiveWarpTrails(ship, engine);
                this.createWarpHaloEffect(ship, engine);
                break;
            }
            case OUT: {
                if (effectLevel > 0.6f) {
                    ship.setPhased(false);
                    float damageReduction = (1.0f - effectLevel) * 0.6f;
                    stats.getHullDamageTakenMult().modifyMult(systemId, 1.0f - damageReduction);
                    stats.getArmorDamageTakenMult().modifyMult(systemId, 1.0f - damageReduction);
                } else {
                    ship.setPhased(true);
                }
                this.applyPhaseEffects(stats, systemId, 1.0f - effectLevel, ship, false);
                ship.setExtraAlphaMult(0.15f + 0.85f * (1.0f - effectLevel));
                if (this.phaseEnded) break;
                this.createWarpOutEffect(ship, engine);
                this.phaseEnded = true;
                this.phaseStarted = false;
                break;
            }
            default: {
                this.removeEffects(stats, systemId);
                ship.setPhased(false);
                ship.setExtraAlphaMult(1.0f);
                this.activeEffects.clear();
            }
        }
        this.applyBaseVisualEffects(ship, effectLevel, state, engine);
    }

    private void updateParticles(CombatEngineAPI engine) {
        if (engine == null) {
            return;
        }
        this.particleTimer += engine.getElapsedInLastFrame();
        ArrayList<ParticleEffect> toRemove = new ArrayList<ParticleEffect>();
        for (ParticleEffect effect : this.activeEffects) {
            if (!effect.update(engine.getElapsedInLastFrame())) {
                toRemove.add(effect);
                continue;
            }
            engine.addHitParticle(effect.position, new Vector2f(0.0f, 0.0f), effect.getCurrentSize(), 1.0f, 0.2f, effect.getCurrentColor());
        }
        this.activeEffects.removeAll(toRemove);
    }

    private void createWarpInEffect(ShipAPI ship, CombatEngineAPI engine) {
        Vector2f pos;
        Vector2f offset;
        if (engine == null) {
            return;
        }
        float radius = ship.getCollisionRadius();
        Vector2f loc = ship.getLocation();
        float facing = ship.getFacing();
        Vector2f forward = Misc.getUnitVectorAtDegreeAngle((float)facing);
        Vector2f right = Misc.getUnitVectorAtDegreeAngle((float)(facing + 90.0f));
        int spiralParticles = 80;
        for (int i = 0; i < spiralParticles; ++i) {
            float angle = (float)((double)((float)i * (360.0f / (float)spiralParticles)) + Math.random() * 20.0);
            float distFactor = 1.8f + (float)Math.random() * 0.5f;
            offset = Misc.getUnitVectorAtDegreeAngle((float)angle);
            offset.scale(radius * distFactor);
            pos = Vector2f.add((Vector2f)loc, (Vector2f)offset, null);
            Vector2f tangent = new Vector2f(-offset.y, offset.x);
            tangent.scale(0.4f);
            Vector2f radial = new Vector2f((ReadableVector2f)offset);
            radial.scale(-1.5f);
            Vector2f vel = new Vector2f();
            Vector2f.add((Vector2f)vel, (Vector2f)tangent, (Vector2f)vel);
            Vector2f.add((Vector2f)vel, (Vector2f)radial, (Vector2f)vel);
            vel.scale(1.0f + (float)Math.random() * 0.5f);
            Color col = Math.random() > 0.5 ? WARP_STREAK : WARP_GLOW;
            this.activeEffects.add(new ParticleEffect(pos, vel, radius * (0.04f + (float)Math.random() * 0.08f), 1.2f + (float)Math.random() * 0.8f, col));
        }
        int streakCount = 30;
        for (int i = 0; i < streakCount; ++i) {
            float spread = (float)(Math.random() - 0.5) * 1.2f;
            offset = new Vector2f((ReadableVector2f)forward);
            offset.scale(radius * (1.2f + (float)Math.random() * 1.0f));
            Vector2f.add((Vector2f)offset, (Vector2f)new Vector2f(right.x * spread * radius * 0.6f, right.y * spread * radius * 0.6f), (Vector2f)offset);
            pos = Vector2f.add((Vector2f)loc, (Vector2f)offset, null);
            Vector2f vel = new Vector2f((ReadableVector2f)forward);
            vel.scale(-300.0f - (float)Math.random() * 200.0f);
            vel.x += (float)(Math.random() - 0.5) * 100.0f;
            vel.y += (float)(Math.random() - 0.5) * 100.0f;
            this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.06f, 0.8f + (float)Math.random() * 0.6f, WARP_CORE));
        }
        int ringLayers = 3;
        for (int layer = 0; layer < ringLayers; ++layer) {
            float ringRadius = radius * (1.2f + (float)layer * 0.3f);
            int ringParticles = 40 + layer * 10;
            float life = 1.0f + (float)layer * 0.3f;
            for (int j = 0; j < ringParticles; ++j) {
                float angle = (float)((double)((float)j * (360.0f / (float)ringParticles)) + Math.random() * 10.0);
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
                Vector2f pos2 = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * ringRadius, dir.y * ringRadius), null);
                Vector2f tangent = new Vector2f(-dir.y, dir.x);
                Vector2f radial = new Vector2f((ReadableVector2f)dir);
                Vector2f vel = new Vector2f();
                Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(tangent.x * 0.6f, tangent.y * 0.6f), (Vector2f)vel);
                Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(radial.x * -0.2f, radial.y * -0.2f), (Vector2f)vel);
                vel.scale(250.0f);
                Color col = layer % 2 == 0 ? WARP_HALO : WARP_GLOW;
                this.activeEffects.add(new ParticleEffect(pos2, vel, radius * 0.03f, life, col));
            }
        }
        for (int m = 0; m < 40; ++m) {
            float angle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
            float dist = radius * (2.0f + (float)Math.random() * 3.0f);
            Vector2f pos3 = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * dist, dir.y * dist), null);
            Vector2f vel = new Vector2f((ReadableVector2f)dir);
            vel.scale(-100.0f - (float)Math.random() * 150.0f);
            this.activeEffects.add(new ParticleEffect(pos3, vel, radius * 0.02f, 1.5f + (float)Math.random(), WARP_SMOKE));
        }
    }

    private void createActiveWarpTrails(ShipAPI ship, CombatEngineAPI engine) {
        if (engine == null || this.particleTimer < 0.08f) {
            return;
        }
        this.particleTimer = 0.0f;
        float radius = ship.getCollisionRadius();
        Vector2f loc = ship.getLocation();
        Vector2f velShip = ship.getVelocity();
        float facing = ship.getFacing();
        Vector2f forward = Misc.getUnitVectorAtDegreeAngle((float)facing);
        Vector2f right = Misc.getUnitVectorAtDegreeAngle((float)(facing + 90.0f));
        for (int i = 0; i < 5; ++i) {
            float sideOffset = (float)(Math.random() - 0.5) * radius * 0.5f;
            Vector2f startOffset = new Vector2f((ReadableVector2f)forward);
            startOffset.scale(-radius * (0.6f + (float)i * 0.2f));
            Vector2f.add((Vector2f)startOffset, (Vector2f)new Vector2f(right.x * sideOffset, right.y * sideOffset), (Vector2f)startOffset);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)startOffset, null);
            Vector2f vel = new Vector2f((ReadableVector2f)forward);
            vel.scale(-400.0f - (float)Math.random() * 300.0f);
            vel.x += (float)(Math.random() - 0.5) * 150.0f;
            vel.y += (float)(Math.random() - 0.5) * 150.0f;
            Vector2f.add((Vector2f)vel, (Vector2f)velShip, (Vector2f)vel);
            float size = radius * (0.05f + (float)Math.random() * 0.08f);
            float life = 0.7f + (float)Math.random() * 0.5f;
            Color col = Math.random() > (double)0.7f ? WARP_CORE : (Math.random() > 0.5 ? WARP_STREAK : WARP_GLOW);
            this.activeEffects.add(new ParticleEffect(pos, vel, size, life, col));
        }
        for (int side = -1; side <= 1; side += 2) {
            if (Math.random() > (double)0.6f) continue;
            for (int k = 0; k < 4; ++k) {
                float t = (float)k / 3.0f;
                Vector2f baseOffset = new Vector2f((ReadableVector2f)right);
                baseOffset.scale((float)side * radius * (0.5f + t * 0.3f));
                Vector2f forwardOffset = new Vector2f((ReadableVector2f)forward);
                forwardOffset.scale(radius * t * 0.6f);
                Vector2f.add((Vector2f)baseOffset, (Vector2f)forwardOffset, (Vector2f)baseOffset);
                Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)baseOffset, null);
                Vector2f vel = new Vector2f((ReadableVector2f)forward);
                vel.scale(200.0f + (float)Math.random() * 150.0f);
                Vector2f sideVel = new Vector2f((ReadableVector2f)right);
                sideVel.scale((float)side * 100.0f * t);
                Vector2f.add((Vector2f)vel, (Vector2f)sideVel, (Vector2f)vel);
                Vector2f.add((Vector2f)vel, (Vector2f)velShip, (Vector2f)vel);
                this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.03f, 0.6f, WARP_HALO));
            }
        }
        for (int m = 0; m < 3; ++m) {
            float angle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
            float dist = radius * (0.8f + (float)Math.random() * 1.2f);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * dist, dir.y * dist), null);
            Vector2f vel = new Vector2f((ReadableVector2f)dir);
            vel.scale(150.0f + (float)Math.random() * 200.0f);
            Vector2f.add((Vector2f)vel, (Vector2f)velShip, (Vector2f)vel);
            this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.02f, 0.8f, WARP_SMOKE));
        }
    }

    private void createWarpHaloEffect(ShipAPI ship, CombatEngineAPI engine) {
        if (engine == null) {
            return;
        }
        float radius = ship.getCollisionRadius();
        Vector2f loc = ship.getLocation();
        int haloCount = 12;
        for (int i = 0; i < haloCount; ++i) {
            if (Math.random() > (double)0.3f) continue;
            float baseAngle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)baseAngle);
            float dist = radius * (0.7f + (float)Math.random() * 0.8f);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * dist, dir.y * dist), null);
            Vector2f tangent = new Vector2f(-dir.y, dir.x);
            Vector2f radial = new Vector2f((ReadableVector2f)dir);
            Vector2f vel = new Vector2f();
            Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(tangent.x * 150.0f, tangent.y * 150.0f), (Vector2f)vel);
            Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(radial.x * 50.0f * (float)(Math.random() - 0.5), radial.y * 50.0f * (float)(Math.random() - 0.5)), (Vector2f)vel);
            Vector2f.add((Vector2f)vel, (Vector2f)ship.getVelocity(), (Vector2f)vel);
            float life = 0.8f + (float)Math.random() * 0.7f;
            this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.02f, life, WARP_HALO));
        }
        if (Math.random() > (double)0.8f) {
            float angle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
            float dist = radius * 1.2f;
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * dist, dir.y * dist), null);
            Vector2f vel = new Vector2f(-dir.y, dir.x);
            vel.scale(250.0f);
            Vector2f.add((Vector2f)vel, (Vector2f)ship.getVelocity(), (Vector2f)vel);
            for (int k = 0; k < 5; ++k) {
                Vector2f subPos = Vector2f.add((Vector2f)pos, (Vector2f)new Vector2f(vel.x * 0.05f * (float)k, vel.y * 0.05f * (float)k), null);
                engine.addSmoothParticle(subPos, new Vector2f(0.0f, 0.0f), radius * 0.03f, 1.0f, 0.3f, WARP_CORE);
            }
        }
    }

    private void createWarpOutEffect(ShipAPI ship, CombatEngineAPI engine) {
        if (engine == null) {
            return;
        }
        float radius = ship.getCollisionRadius();
        Vector2f loc = ship.getLocation();
        float facing = ship.getFacing();
        Vector2f forward = Misc.getUnitVectorAtDegreeAngle((float)facing);
        Vector2f right = Misc.getUnitVectorAtDegreeAngle((float)(facing + 90.0f));
        int streakCount = 40;
        for (int i = 0; i < streakCount; ++i) {
            float spread = (float)Math.random() * 0.8f - 0.4f;
            Vector2f offset = new Vector2f((ReadableVector2f)forward);
            offset.scale(radius * (1.0f + (float)Math.random() * 1.5f));
            Vector2f.add((Vector2f)offset, (Vector2f)new Vector2f(right.x * spread * radius * 0.5f, right.y * spread * radius * 0.5f), (Vector2f)offset);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)offset, null);
            Vector2f vel = new Vector2f((ReadableVector2f)forward);
            vel.scale(400.0f + (float)Math.random() * 300.0f);
            vel.x += (float)(Math.random() - 0.5) * 150.0f;
            vel.y += (float)(Math.random() - 0.5) * 150.0f;
            float size = radius * (0.1f + (float)Math.random() * 0.15f);
            Color col = Math.random() > (double)0.6f ? WARP_CORE : WARP_STREAK;
            this.activeEffects.add(new ParticleEffect(pos, vel, size, 1.2f + (float)Math.random() * 0.8f, col));
        }
        int ringLayers = 5;
        for (int layer = 0; layer < ringLayers; ++layer) {
            float baseRadius = radius * (0.8f + (float)layer * 0.4f);
            int particlesPerRing = 30 + layer * 10;
            float ringSpeed = 200.0f + (float)layer * 50.0f;
            float life = 1.0f + (float)layer * 0.3f;
            for (int j = 0; j < particlesPerRing; ++j) {
                float angle = (float)((double)((float)j * (360.0f / (float)particlesPerRing)) + Math.random() * 20.0);
                Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
                Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * baseRadius, dir.y * baseRadius), null);
                Vector2f tangent = new Vector2f(-dir.y, dir.x);
                Vector2f radial = new Vector2f((ReadableVector2f)dir);
                Vector2f vel = new Vector2f();
                Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(tangent.x * ringSpeed * 0.3f, tangent.y * ringSpeed * 0.3f), (Vector2f)vel);
                Vector2f.add((Vector2f)vel, (Vector2f)new Vector2f(radial.x * ringSpeed * 0.7f, radial.y * ringSpeed * 0.7f), (Vector2f)vel);
                vel.x += (float)(Math.random() - 0.5) * 50.0f;
                vel.y += (float)(Math.random() - 0.5) * 50.0f;
                Color ringColor = layer % 2 == 0 ? WARP_HALO : WARP_GLOW;
                this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.03f, life, ringColor));
            }
        }
        for (int k = 0; k < 15; ++k) {
            float angle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * radius * 0.3f * (float)Math.random(), dir.y * radius * 0.3f * (float)Math.random()), null);
            Vector2f vel = new Vector2f((ReadableVector2f)dir);
            vel.scale(200.0f + (float)Math.random() * 300.0f);
            float size = radius * (0.05f + (float)Math.random() * 0.1f);
            float life = 0.8f + (float)Math.random() * 0.7f;
            this.activeEffects.add(new ParticleEffect(pos, vel, size, life, WARP_CORE));
        }
        for (int m = 0; m < 30; ++m) {
            float angle = (float)(Math.random() * 360.0);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)angle);
            float dist = radius * (1.5f + (float)Math.random() * 3.0f);
            Vector2f pos = Vector2f.add((Vector2f)loc, (Vector2f)new Vector2f(dir.x * dist, dir.y * dist), null);
            Vector2f vel = new Vector2f((ReadableVector2f)dir);
            vel.scale(100.0f + (float)Math.random() * 200.0f);
            this.activeEffects.add(new ParticleEffect(pos, vel, radius * 0.02f, 1.5f + (float)Math.random(), WARP_SMOKE));
        }
    }

    private void applyPhaseEffects(MutableShipStatsAPI stats, String id, float effectLevel, ShipAPI ship, boolean isPhasing) {
        stats.getMaxSpeed().modifyFlat(id, 120.0f * effectLevel);
        stats.getAcceleration().modifyFlat(id, 250.0f * effectLevel);
        stats.getDeceleration().modifyFlat(id, 250.0f * effectLevel * 0.6f);
        stats.getMaxTurnRate().modifyFlat(id, 120.0f * effectLevel);
        stats.getTurnAcceleration().modifyFlat(id, 120.0f * effectLevel);
        float timeMult = 1.0f + 1.0f * effectLevel;
        stats.getTimeMult().modifyMult(id, timeMult);
        if (isPhasing) {
            float damageMult = 1.0f - 0.6f * effectLevel;
            stats.getHullDamageTakenMult().modifyMult(id, damageMult);
            stats.getArmorDamageTakenMult().modifyMult(id, damageMult);
            stats.getShieldDamageTakenMult().modifyMult(id, damageMult);
        }
        stats.getBallisticRoFMult().modifyMult(id, 0.4f + 0.6f * (1.0f - effectLevel));
        stats.getEnergyRoFMult().modifyMult(id, 0.4f + 0.6f * (1.0f - effectLevel));
        stats.getMissileRoFMult().modifyMult(id, 0.4f + 0.6f * (1.0f - effectLevel));
    }

    private void applyBaseVisualEffects(ShipAPI ship, float effectLevel, ShipSystemStatsScript.State state, CombatEngineAPI engine) {
        float jitterIntensity = effectLevel * 1.2f;
        if (state == ShipSystemStatsScript.State.OUT) {
            jitterIntensity = (1.0f - effectLevel) * 0.8f;
        }
        ship.setJitter((Object)this, WARP_CORE, jitterIntensity * 0.9f, 5, ship.getCollisionRadius() * 0.15f);
        ship.setJitterUnder((Object)this, WARP_STREAK, jitterIntensity * 0.7f, 7, ship.getCollisionRadius() * 0.2f);
        ship.getEngineController().fadeToOtherColor((Object)this, new Color(200, 220, 255, 200), WARP_SMOKE, effectLevel, 0.7f);
        ship.getEngineController().extendFlame((Object)this, 2.5f * effectLevel, 1.0f * effectLevel, 0.7f * effectLevel);
        if (engine != null && effectLevel > 0.5f && Math.random() > (double)0.7f) {
            for (ShipEngineControllerAPI.ShipEngineAPI engineSlot : ship.getEngineController().getShipEngines()) {
                if (!engineSlot.isActive()) continue;
                Vector2f engineLoc = engineSlot.getLocation();
                Vector2f engineDir = Misc.getUnitVectorAtDegreeAngle((float)engineSlot.getEngineSlot().getAngle());
                for (int i = 0; i < 2; ++i) {
                    Vector2f sparkVel = new Vector2f((ReadableVector2f)engineDir);
                    sparkVel.scale(-200.0f - (float)Math.random() * 100.0f);
                    sparkVel.x += (float)(Math.random() - 0.5) * 50.0f;
                    sparkVel.y += (float)(Math.random() - 0.5) * 50.0f;
                    engine.addHitParticle(engineLoc, sparkVel, ship.getCollisionRadius() * 0.02f, 1.0f, 0.2f, WARP_CORE);
                }
            }
        }
    }

    private void removeEffects(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getTimeMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getShieldDamageTakenMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship != null) {
            String systemId = id + "_" + ship.getId();
            this.removeEffects(stats, systemId);
            ship.setPhased(false);
            ship.setExtraAlphaMult(1.0f);
            ship.getEngineController().fadeToOtherColor((Object)this, Color.white, new Color(0, 0, 0, 0), 0.0f, 0.0f);
        }
        this.phaseStarted = false;
        this.phaseEnded = false;
        this.activeEffects.clear();
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return null;
        }
        return "\u9634\u5f71\u7a81\u88ad";
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return true;
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
                float timeMult = 1.0f + 1.0f * effectLevel;
                return new ShipSystemStatsScript.StatusData("\u65f6\u6d41\u00d7" + String.format("%.1f", Float.valueOf(timeMult)), false);
            }
            if (state == ShipSystemStatsScript.State.OUT) {
                return new ShipSystemStatsScript.StatusData("\u76f8\u4f4d\u9000\u51fa", false);
            }
        } else if (index == 1 && (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE)) {
            float damageReduction = 0.6f * effectLevel * 100.0f;
            return new ShipSystemStatsScript.StatusData("\u51cf\u4f24" + (int)damageReduction + "%", false);
        }
        return null;
    }

    private class ParticleEffect {
        Vector2f position;
        Vector2f velocity;
        float size;
        float lifetime;
        float maxLifetime;
        Color color;

        public ParticleEffect(Vector2f pos, Vector2f vel, float sz, float life, Color col) {
            this.position = pos;
            this.velocity = vel;
            this.size = sz;
            this.lifetime = life;
            this.maxLifetime = life;
            this.color = col;
        }

        public boolean update(float amount) {
            this.lifetime -= amount;
            if (this.lifetime <= 0.0f) {
                return false;
            }
            Vector2f.add((Vector2f)this.position, (Vector2f)new Vector2f(this.velocity.x * amount, this.velocity.y * amount), (Vector2f)this.position);
            this.velocity.scale(0.95f);
            return true;
        }

        public Color getCurrentColor() {
            float alpha = this.lifetime / this.maxLifetime * ((float)this.color.getAlpha() / 255.0f);
            return new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)(alpha * 255.0f));
        }

        public float getCurrentSize() {
            return this.size * (this.lifetime / this.maxLifetime);
        }
    }
}

