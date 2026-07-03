/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BeamAPI
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipSystemAIScript
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipwideAIFlags
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  org.lazywizard.lazylib.MathUtils
 *  org.lazywizard.lazylib.combat.AIUtils
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class PhasedDriftAI
implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private boolean runOnce = false;
    private boolean shieldRunOnce = false;
    private float checkAgain = 0.25f;
    private float delay = 0.0f;
    private float timer = 0.0f;
    private boolean shieldDownFlag = false;
    private static final float PROJECTILE_THREAT_RANGE = 1000.0f;
    private static final float BEAM_THREAT_RANGE = 2000.0f;
    private static final float ENEMY_DISTANCE_THRESHOLD = 2500.0f;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (this.engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (this.engine.isPaused() || this.ship.getShipAI() == null) {
            return;
        }
        if (!this.runOnce) {
            this.runOnce = true;
            this.delay = 1.0f;
        }
        this.timer += amount;
        if (this.shieldDownFlag) {
            if (this.ship.getShield() != null && this.ship.getShield().isOn()) {
                this.ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
            if (this.ship.getShield() != null && this.ship.getShield().isOff()) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            if (!this.shieldRunOnce && AIUtils.canUseSystemThisFrame((ShipAPI)this.ship) && this.ship.getShield() != null && this.ship.getShield().isOff()) {
                this.ship.useSystem();
                this.shieldRunOnce = true;
            }
            if (!this.system.canBeActivated() && this.shieldRunOnce) {
                this.shieldDownFlag = false;
                this.shieldRunOnce = false;
            }
            return;
        }
        if (this.timer > this.delay + this.checkAgain) {
            this.timer = 0.0f;
            this.checkAgain = 0.0f;
            if (!this.system.isActive()) {
                if (AIUtils.getNearbyEnemies((CombatEntityAPI)this.ship, (float)2500.0f).isEmpty() && this.system.getAmmo() > this.system.getMaxAmmo() - 1) {
                    if (AIUtils.canUseSystemThisFrame((ShipAPI)this.ship)) {
                        this.ship.useSystem();
                        this.checkAgain = 1.0f;
                        return;
                    }
                } else {
                    boolean beamSafe;
                    boolean projectileSafe = !this.isThreatenedByProjectile(this.ship, 1000.0f);
                    boolean bl = beamSafe = !this.isThreatenedByBeam(this.ship, 2000.0f);
                    if (AIUtils.canUseSystemThisFrame((ShipAPI)this.ship)) {
                        float hardFluxLevel = this.ship.getFluxTracker().getHardFlux() / this.ship.getFluxTracker().getMaxFlux();
                        float softFlux = this.ship.getFluxTracker().getCurrFlux() - this.ship.getFluxTracker().getHardFlux();
                        if (softFlux >= 3000.0f) {
                            this.ship.useSystem();
                            this.checkAgain = 1.0f;
                            return;
                        }
                        if (hardFluxLevel <= 0.8f && hardFluxLevel >= 0.2f && projectileSafe && beamSafe) {
                            this.shieldDownFlag = true;
                            this.checkAgain = 1.0f;
                            return;
                        }
                        if (hardFluxLevel > 0.8f) {
                            this.ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                            if (this.ship.getHitpoints() > this.ship.getMaxHitpoints() * 0.25f && beamSafe) {
                                this.shieldDownFlag = true;
                            }
                            this.checkAgain = 0.0f;
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isThreatenedByProjectile(ShipAPI ship, float range) {
        for (DamagingProjectileAPI projectile : this.engine.getProjectiles()) {
            WeaponAPI weapon;
            if (projectile.getOwner() == ship.getOwner() || MathUtils.getDistance((Vector2f)projectile.getLocation(), (Vector2f)ship.getLocation()) > range || (weapon = projectile.getWeapon()) == null) continue;
            if (weapon.getSpec().getAIHints().contains("MISSILE")) {
                return true;
            }
            if (!(MathUtils.getDistance((Vector2f)projectile.getLocation(), (Vector2f)ship.getLocation()) < 300.0f)) continue;
            return true;
        }
        return false;
    }

    private boolean isThreatenedByBeam(ShipAPI ship, float range) {
        for (BeamAPI beam : this.engine.getBeams()) {
            if (beam.getDamageTarget() == ship) {
                return true;
            }
            if (beam.getSource() == null || beam.getSource().getOwner() == ship.getOwner() || !MathUtils.isWithinRange((Vector2f)beam.getFrom(), (Vector2f)ship.getLocation(), (float)range)) continue;
            return true;
        }
        return false;
    }
}

