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
 *  com.fs.starfarer.api.loading.FighterWingSpecAPI
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
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ThreatDronePodOnExplosionEffect
implements ProximityExplosionEffect {
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        int i;
        ArrayList<ShipAPI> fighters = new ArrayList<ShipAPI>();
        String variantId = "wxlks_wing";
        FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(variantId);
        ShipAPI[] ships = new ShipAPI[spec.getNumFighters()];
        CombatFleetManagerAPI fleetManager = Global.getCombatEngine().getFleetManager(explosion.getSource().getOriginalOwner());
        boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
        fleetManager.setSuppressDeploymentMessages(true);
        ShipAPI leader = fleetManager.spawnShipOrWing(variantId, explosion.getLocation(), 0.0f, 0.0f);
        fighters.add(leader);
        for (int i2 = 0; i2 < ships.length; ++i2) {
            ships[i2] = (ShipAPI)leader.getWing().getWingMembers().get(i2);
            ships[i2].getLocation().set((ReadableVector2f)leader.getLocation());
            fighters.add(ships[i2]);
        }
        fleetManager.setSuppressDeploymentMessages(wasSuppressed);
        for (final ShipAPI fighter : fighters) {
            Global.getCombatEngine().addPlugin((EveryFrameCombatPlugin)new BaseEveryFrameCombatPlugin(){
                float timer = 0.0f;
                final float max = 30.0f + (float)Math.random();
                final float resistanceTime = 1.5f;

                public void advance(float amount, List<InputEventAPI> events) {
                    if (this.timer <= 1.5f) {
                        fighter.getMutableStats().getHullDamageTakenMult().modifyMult("sotf_dronestrike", this.timer * 0.33333334f + 0.5f);
                        fighter.getMutableStats().getArmorDamageTakenMult().modifyMult("sotf_dronestrike", this.timer * 0.33333334f + 0.5f);
                        fighter.getMutableStats().getShieldDamageTakenMult().modifyMult("sotf_dronestrike", this.timer * 0.33333334f + 0.5f);
                    } else {
                        fighter.getMutableStats().getHullDamageTakenMult().unmodify("sotf_dronestrike");
                        fighter.getMutableStats().getArmorDamageTakenMult().unmodify("sotf_dronestrike");
                        fighter.getMutableStats().getShieldDamageTakenMult().unmodify("sotf_dronestrike");
                    }
                    if (!Global.getCombatEngine().isPaused()) {
                        this.timer += amount * Global.getCombatEngine().getTimeMult().getModifiedValue();
                        if (this.timer >= this.max) {
                            Global.getCombatEngine().applyDamage((CombatEntityAPI)fighter, fighter.getLocation(), 1000.0f, DamageType.HIGH_EXPLOSIVE, 0.0f, true, false, (Object)fighter);
                            Global.getCombatEngine().removePlugin((EveryFrameCombatPlugin)this);
                        }
                    }
                }
            });
        }
        for (i = 0; i < 5; ++i) {
            Vector2f flareLoc = Misc.getPointWithinRadius((Vector2f)explosion.getLocation(), (float)50.0f);
            Global.getCombatEngine().spawnProjectile(explosion.getSource(), (WeaponAPI)null, "flarelauncher2", flareLoc, Misc.getAngleInDegrees((Vector2f)explosion.getLocation(), (Vector2f)flareLoc), (Vector2f)null);
        }
        for (i = 0; i < 18; ++i) {
            float dur = 2.0f + (float)Math.random();
            Vector2f loc = new Vector2f((ReadableVector2f)explosion.getLocation());
            loc = Misc.getPointWithinRadius((Vector2f)loc, (float)100.0f);
            float s = 275.0f * (0.25f + (float)Math.random() * 0.25f);
            Global.getCombatEngine().addNebulaParticle(loc, explosion.getVelocity(), s, 1.5f, 0.1f, 0.0f, dur, new Color(35, 35, 35));
        }
    }
}

