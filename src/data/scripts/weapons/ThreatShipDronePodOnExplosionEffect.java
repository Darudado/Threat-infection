/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatFleetManagerAPI
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.EveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.ProximityExplosionEffect
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.input.InputEventAPI
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ThreatShipDronePodOnExplosionEffect
implements ProximityExplosionEffect {
    private static final String SHIP_VARIANT_ID = "skirmish_unit_Type100";

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        int i;
        ArrayList<ShipAPI> spawnedShips = new ArrayList<ShipAPI>();
        CombatFleetManagerAPI fleetManager = Global.getCombatEngine().getFleetManager(explosion.getSource().getOriginalOwner());
        boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
        fleetManager.setSuppressDeploymentMessages(true);
        ShipAPI spawnedShip = fleetManager.spawnShipOrWing(SHIP_VARIANT_ID, explosion.getLocation(), explosion.getFacing());
        if (spawnedShip != null) {
            spawnedShips.add(spawnedShip);
            spawnedShip.setOwner(explosion.getSource().getOriginalOwner());
            spawnedShip.getVelocity().set((ReadableVector2f)explosion.getVelocity());
            this.applyInvincibility(spawnedShip);
            for (final ShipAPI ship : spawnedShips) {
                Global.getCombatEngine().addPlugin((EveryFrameCombatPlugin)new BaseEveryFrameCombatPlugin(){
                    float timer = 0.0f;
                    final float invincibilityTime = 3.0f;
                    final float max = 45.0f + (float)Math.random() * 15.0f;
                    final float resistanceTime = 5.0f;

                    public void advance(float amount, List<InputEventAPI> events) {
                        if (!Global.getCombatEngine().isPaused()) {
                            this.timer += amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
                            if (this.timer <= 3.0f) {
                                ThreatShipDronePodOnExplosionEffect.this.maintainInvincibility(ship);
                                if (this.timer % 0.5f < 0.25f) {
                                    ship.setJitter((Object)ship, new Color(100, 255, 100, 255), 1.0f, 3, 0.0f, 10.0f);
                                    ship.setJitterUnder((Object)ship, new Color(100, 255, 100, 100), 1.0f, 5, 0.0f, 15.0f);
                                }
                            } else if (this.timer <= 8.0f) {
                                ThreatShipDronePodOnExplosionEffect.this.removeInvincibility(ship);
                                float transitionProgress = (this.timer - 3.0f) / 5.0f;
                                float resistance = 1.0f - transitionProgress * 0.7f;
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.getMutableStats().getShieldDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.setJitter((Object)ship, new Color(255, 255, 100, 200), 0.8f, 2, 0.0f, 5.0f);
                            } else {
                                ThreatShipDronePodOnExplosionEffect.this.removeInvincibility(ship);
                                ship.getMutableStats().getHullDamageTakenMult().unmodify("sotf_shipspawn");
                                ship.getMutableStats().getArmorDamageTakenMult().unmodify("sotf_shipspawn");
                                ship.getMutableStats().getShieldDamageTakenMult().unmodify("sotf_shipspawn");
                            }
                            if (this.timer >= this.max) {
                                Global.getCombatEngine().applyDamage((CombatEntityAPI)ship, ship.getLocation(), ship.getMaxHitpoints() * 2.0f, DamageType.HIGH_EXPLOSIVE, 0.0f, true, false, (Object)ship);
                                Global.getCombatEngine().removePlugin((EveryFrameCombatPlugin)this);
                            }
                        }
                    }
                });
            }
        }
        fleetManager.setSuppressDeploymentMessages(wasSuppressed);
        for (i = 0; i < 8; ++i) {
            Vector2f flareLoc = Misc.getPointWithinRadius((Vector2f)explosion.getLocation(), (float)75.0f);
            Global.getCombatEngine().spawnProjectile(explosion.getSource(), (WeaponAPI)null, "flarelauncher2", flareLoc, Misc.getAngleInDegrees((Vector2f)explosion.getLocation(), (Vector2f)flareLoc), (Vector2f)null);
        }
        for (i = 0; i < 25; ++i) {
            float dur = 2.5f + (float)Math.random() * 1.5f;
            Vector2f loc = new Vector2f((ReadableVector2f)explosion.getLocation());
            loc = Misc.getPointWithinRadius((Vector2f)loc, (float)150.0f);
            float s = 400.0f * (0.3f + (float)Math.random() * 0.4f);
            Global.getCombatEngine().addNebulaParticle(loc, explosion.getVelocity(), s, 2.0f, 0.1f, 0.0f, dur, new Color(35, 35, 35));
        }
        Global.getSoundPlayer().playSound("system_phase_skimmer", 1.0f, 1.0f, explosion.getLocation(), explosion.getVelocity());
    }

    private void applyInvincibility(ShipAPI ship) {
        ship.getMutableStats().getHullDamageTakenMult().modifyMult("invincible", 0.0f);
        ship.getMutableStats().getArmorDamageTakenMult().modifyMult("invincible", 0.0f);
        ship.getMutableStats().getShieldDamageTakenMult().modifyMult("invincible", 0.0f);
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult("invincible", 0.0f);
        if (ship.getShield() != null) {
            ship.getShield().toggleOn();
            ship.getMutableStats().getShieldUpkeepMult().modifyMult("invincible", 0.0f);
        }
        ship.setJitter((Object)ship, new Color(100, 255, 100, 255), 1.0f, 4, 0.0f, 15.0f);
        ship.setJitterUnder((Object)ship, new Color(100, 255, 100, 100), 1.0f, 6, 0.0f, 20.0f);
    }

    private void maintainInvincibility(ShipAPI ship) {
        this.applyInvincibility(ship);
    }

    private void removeInvincibility(ShipAPI ship) {
        ship.getMutableStats().getHullDamageTakenMult().unmodify("invincible");
        ship.getMutableStats().getArmorDamageTakenMult().unmodify("invincible");
        ship.getMutableStats().getShieldDamageTakenMult().unmodify("invincible");
        ship.getMutableStats().getEmpDamageTakenMult().unmodify("invincible");
        ship.getMutableStats().getShieldUpkeepMult().unmodify("invincible");
    }
}

