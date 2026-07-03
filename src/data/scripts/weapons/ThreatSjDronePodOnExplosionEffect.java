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

public class ThreatSjDronePodOnExplosionEffect
implements ProximityExplosionEffect {
    private static final String[] SHIP_VARIANTS = new String[]{"skirmish_unit_Type100", "overseer_unit_Type250", "assault_unit_Type200", "wxbb", "wxct", "wxmds", "wxby"};

    private String getRandomShipVariantSimple() {
        int index = (int)(Math.random() * (double)SHIP_VARIANTS.length);
        return SHIP_VARIANTS[index];
    }

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        int i;
        ArrayList<ShipAPI> spawnedShips = new ArrayList<ShipAPI>();
        CombatFleetManagerAPI fleetManager = Global.getCombatEngine().getFleetManager(explosion.getSource().getOriginalOwner());
        boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
        fleetManager.setSuppressDeploymentMessages(true);
        String selectedVariant = this.getRandomShipVariantSimple();
        ShipAPI spawnedShip = fleetManager.spawnShipOrWing(selectedVariant, explosion.getLocation(), explosion.getFacing());
        if (spawnedShip != null) {
            spawnedShips.add(spawnedShip);
            spawnedShip.setOwner(explosion.getSource().getOriginalOwner());
            spawnedShip.getVelocity().set((ReadableVector2f)explosion.getVelocity());
            final float existenceTime = this.getExistenceTimeForShip(spawnedShip);
            float invincibilityTime = this.getInvincibilityTimeForShip(spawnedShip);
            this.applyInvincibility(spawnedShip);
            for (final ShipAPI ship : spawnedShips) {
                Global.getCombatEngine().addPlugin((EveryFrameCombatPlugin)new BaseEveryFrameCombatPlugin(){
                    float timer = 0.0f;
                    final float invincibilityTime = ThreatSjDronePodOnExplosionEffect.this.getInvincibilityTimeForShip(ship);
                    final float max = existenceTime;
                    final float resistanceTime = 5.0f;

                    public void advance(float amount, List<InputEventAPI> events) {
                        if (!Global.getCombatEngine().isPaused()) {
                            this.timer += amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
                            if (this.timer <= this.invincibilityTime) {
                                ThreatSjDronePodOnExplosionEffect.this.maintainInvincibility(ship);
                                if (this.timer % 0.5f < 0.25f) {
                                    ship.setJitter((Object)ship, new Color(100, 255, 100, 255), 1.0f, 3, 0.0f, 10.0f);
                                    ship.setJitterUnder((Object)ship, new Color(100, 255, 100, 100), 1.0f, 5, 0.0f, 15.0f);
                                }
                            } else if (this.timer <= this.invincibilityTime + 5.0f) {
                                ThreatSjDronePodOnExplosionEffect.this.removeInvincibility(ship);
                                float transitionProgress = (this.timer - this.invincibilityTime) / 5.0f;
                                float resistance = 1.0f - transitionProgress * 0.7f;
                                ship.getMutableStats().getHullDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.getMutableStats().getArmorDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.getMutableStats().getShieldDamageTakenMult().modifyMult("sotf_shipspawn", resistance);
                                ship.setJitter((Object)ship, new Color(255, 255, 100, 200), 0.8f, 2, 0.0f, 5.0f);
                            } else {
                                ThreatSjDronePodOnExplosionEffect.this.removeInvincibility(ship);
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
            Global.getSoundPlayer().playSound("system_phase_skimmer", 1.0f, 0.8f + (float)Math.random() * 0.4f, explosion.getLocation(), explosion.getVelocity());
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
    }

    private float getExistenceTimeForShip(ShipAPI ship) {
        switch (ship.getHullSize()) {
            case FRIGATE: {
                return 45.0f + (float)Math.random() * 15.0f;
            }
            case DESTROYER: {
                return 35.0f + (float)Math.random() * 10.0f;
            }
            case CRUISER: {
                return 25.0f + (float)Math.random() * 10.0f;
            }
            case CAPITAL_SHIP: {
                return 20.0f + (float)Math.random() * 5.0f;
            }
        }
        return 40.0f;
    }

    private float getInvincibilityTimeForShip(ShipAPI ship) {
        switch (ship.getHullSize()) {
            case FRIGATE: {
                return 2.0f;
            }
            case DESTROYER: {
                return 2.5f;
            }
            case CRUISER: {
                return 3.0f;
            }
            case CAPITAL_SHIP: {
                return 4.0f;
            }
        }
        return 2.0f;
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

