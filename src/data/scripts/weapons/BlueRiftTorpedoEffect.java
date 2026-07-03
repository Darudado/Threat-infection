/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.EveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.OnFireEffectPlugin
 *  com.fs.starfarer.api.combat.OnHitEffectPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
 *  com.fs.starfarer.api.impl.combat.NegativeExplosionVisual$NEParams
 *  com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion
 *  com.fs.starfarer.api.loading.MissileSpecAPI
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import data.scripts.weapons.BlueRiftTrailEffect;
import java.awt.Color;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class BlueRiftTorpedoEffect
implements OnFireEffectPlugin,
OnHitEffectPlugin {
    public static final Color BLUE_RIFT_COLOR = new Color(70, 130, 240, 255);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI)target;
            float damageMult = 1.0f;
            switch (ship.getHullSize()) {
                case FRIGATE: {
                    damageMult = 1.1f;
                    break;
                }
                case DESTROYER: {
                    damageMult = 1.2f;
                    break;
                }
                case CRUISER: {
                    damageMult = 1.3f;
                    break;
                }
                case CAPITAL_SHIP: {
                    damageMult = 1.4f;
                    break;
                }
                default: {
                    damageMult = 1.0f;
                }
            }
            if (damageMult != 1.0f) {
                float originalDamage = damageResult.getDamageToHull();
                float newDamage = originalDamage * damageMult;
                damageResult.setDamageToHull(newDamage);
            }
        }
        Color color = BLUE_RIFT_COLOR;
        Object o = projectile.getWeapon().getSpec().getProjectileSpec();
        if (o instanceof MissileSpecAPI) {
            MissileSpecAPI spec = (MissileSpecAPI)o;
            color = spec.getExplosionColor();
        }
        NegativeExplosionVisual.NEParams p = RiftCascadeMineExplosion.createStandardRiftParams((Color)color, (float)40.0f);
        p.fadeOut = 2.0f;
        p.hitGlowSizeMult = 1.0f;
        RiftCascadeMineExplosion.spawnStandardRift((DamagingProjectileAPI)projectile, (NegativeExplosionVisual.NEParams)p);
        Vector2f vel = new Vector2f();
        if (target != null) {
            vel.set((ReadableVector2f)target.getVelocity());
        }
        Global.getSoundPlayer().playSound("rifttorpedo_explosion", 1.0f, 1.0f, point, vel);
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        BlueRiftTrailEffect trail = new BlueRiftTrailEffect((MissileAPI)projectile, "rifttorpedo_loop");
        ((MissileAPI)projectile).setEmpResistance(1000);
        ((MissileAPI)projectile).setEccmChanceOverride(1.0f);
        Global.getCombatEngine().addPlugin((EveryFrameCombatPlugin)trail);
    }
}

