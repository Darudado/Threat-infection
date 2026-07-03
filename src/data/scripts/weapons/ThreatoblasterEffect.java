/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.ArmorGridAPI
 *  com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEngineLayers
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.OnFireEffectPlugin
 *  com.fs.starfarer.api.combat.OnHitEffectPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ViewportAPI
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
 *  com.fs.starfarer.api.graphics.SpriteAPI
 *  com.fs.starfarer.api.util.FaderUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ThreatoblasterEffect
extends BaseCombatLayeredRenderingPlugin
implements OnFireEffectPlugin,
OnHitEffectPlugin {
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected DamagingProjectileAPI proj;
    protected Vector2f projVel;
    protected Vector2f projLoc;
    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
    protected boolean resetTrailSpeed = false;

    public ThreatoblasterEffect() {
    }

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        ShipAPI source;
        Color color = projectile.getProjectileSpec().getFringeColor();
        color = Misc.setAlpha((Color)color, (int)100);
        Vector2f vel = new Vector2f();
        if (target instanceof ShipAPI) {
            vel.set((ReadableVector2f)target.getVelocity());
        }
        float sizeMult = Misc.getHitGlowSize((float)100.0f, (float)projectile.getDamage().getBaseDamage(), (ApplyDamageResultAPI)damageResult) / 100.0f;
        for (int i = 0; i < 7; ++i) {
            float size = 40.0f * (0.75f + (float)Math.random() * 0.5f);
            float dur = 1.0f;
            float rampUp = 0.0f;
            Color c = Misc.scaleAlpha((Color)color, (float)projectile.getBrightness());
            engine.addNebulaParticle(point, vel, size, 5.0f + 3.0f * sizeMult, rampUp, 0.0f, dur, c, true);
        }
        if (!shieldHit && target instanceof ShipAPI && (source = projectile.getSource()) != null && source.isAlive() && source.getOwner() == projectile.getOwner()) {
            float maxHp = source.getMaxHitpoints();
            float newHp = Math.min(maxHp, source.getHitpoints() + maxHp * 0.05f);
            source.setHitpoints(newHp);
            ArmorGridAPI armorGrid = source.getArmorGrid();
            if (armorGrid != null) {
                int width = armorGrid.getGrid().length;
                int height = armorGrid.getGrid()[0].length;
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        float current = armorGrid.getArmorValue(x, y);
                        float max = armorGrid.getMaxArmorInCell();
                        float newArmor = Math.min(max, current + max * 0.05f);
                        armorGrid.setArmorValue(x, y, newArmor);
                    }
                }
            }
        }
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ThreatoblasterEffect trail = new ThreatoblasterEffect(projectile);
        CombatEntityAPI e = engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)trail);
        e.getLocation().set((ReadableVector2f)projectile.getLocation());
    }

    public ThreatoblasterEffect(DamagingProjectileAPI proj) {
        this.proj = proj;
        this.projVel = new Vector2f((ReadableVector2f)proj.getVelocity());
        this.projLoc = new Vector2f((ReadableVector2f)proj.getLocation());
        int num = 30;
        for (int i = 0; i < num; ++i) {
            this.particles.add(new ParticleData(proj));
        }
        float index = 0.0f;
        for (ParticleData p : this.particles) {
            p.offset = Misc.getPointWithinRadius((Vector2f)p.offset, (float)20.0f);
            index += 1.0f;
        }
    }

    public float getRenderRadius() {
        return 700.0f;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return this.layers;
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }

    public void advance(float amount) {
        if (!Global.getCombatEngine().isPaused()) {
            this.entity.getLocation().set((ReadableVector2f)this.proj.getLocation());
            float max = 0.0f;
            for (ParticleData p : this.particles) {
                p.advance(amount);
                max = Math.max(max, p.offset.lengthSquared());
            }
            if (this.proj.getElapsed() < 0.1f) {
                this.projVel.set((ReadableVector2f)this.proj.getVelocity());
                this.projLoc.set((ReadableVector2f)this.proj.getLocation());
            } else {
                Vector2f var10000 = this.projLoc;
                var10000.x += this.projVel.x * amount;
                var10000 = this.projLoc;
                var10000.y += this.projVel.y * amount;
                if (this.proj.didDamage()) {
                    if (!this.resetTrailSpeed) {
                        for (ParticleData p : this.particles) {
                            Vector2f.add((Vector2f)p.vel, (Vector2f)this.projVel, (Vector2f)p.vel);
                        }
                        this.projVel.scale(0.0f);
                        this.resetTrailSpeed = true;
                    }
                    for (ParticleData p : this.particles) {
                        float dist = p.offset.length();
                        p.vel.scale(Math.min(1.0f, dist / 100.0f));
                    }
                }
            }
        }
    }

    public boolean isExpired() {
        return this.proj.isExpired() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.proj);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = this.projLoc.x;
        float y = this.projLoc.y;
        Color color = this.proj.getProjectileSpec().getFringeColor();
        color = Misc.setAlpha((Color)color, (int)30);
        float b = this.proj.getBrightness();
        b *= viewport.getAlphaMult();
        for (ParticleData p : this.particles) {
            float size = 25.0f;
            Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
            float alphaMult = 1.0f;
            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size *= p.scale, size);
            p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
            p.sprite.setColor(color);
            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }

    public static class ParticleData {
        public SpriteAPI sprite;
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1.0f;
        public DamagingProjectileAPI proj;
        public float scaleIncreaseRate = 1.0f;
        public float turnDir = 1.0f;
        public float angle = 1.0f;
        public float maxDur;
        public Vector2f origVel;
        public FaderUtil fader;
        public Vector2f dirVelChange;

        public ParticleData(DamagingProjectileAPI proj) {
            this.proj = proj;
            this.sprite = Global.getSettings().getSprite("misc", "nebula_particles");
            float i = Misc.random.nextInt(4);
            float j = Misc.random.nextInt(4);
            this.sprite.setTexWidth(0.25f);
            this.sprite.setTexHeight(0.25f);
            this.sprite.setTexX(i * 0.25f);
            this.sprite.setTexY(j * 0.25f);
            this.sprite.setAdditiveBlend();
            this.angle = (float)Math.random() * 360.0f;
            this.maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
            this.scaleIncreaseRate = 2.5f / this.maxDur;
            this.scale = 1.0f;
            this.turnDir = Math.signum((float)Math.random() - 0.5f) * 30.0f * (float)Math.random();
            float driftDir = proj.getFacing() + 180.0f + ((float)Math.random() * 30.0f - 15.0f);
            this.vel = Misc.getUnitVectorAtDegreeAngle((float)driftDir);
            this.vel.scale(80.0f / this.maxDur * (0.0f + (float)Math.random() * 3.0f));
            this.origVel = new Vector2f((ReadableVector2f)this.vel);
            this.dirVelChange = Misc.getUnitVectorAtDegreeAngle((float)(proj.getFacing() + 180.0f));
            this.fader = new FaderUtil(0.0f, 0.25f, 0.05f);
            this.fader.fadeIn();
        }

        public void advance(float amount) {
            float speed;
            this.scale += this.scaleIncreaseRate * amount;
            Vector2f var10000 = this.offset;
            var10000.x += this.vel.x * amount;
            var10000 = this.offset;
            var10000.y += this.vel.y * amount;
            if (!this.proj.didDamage() && (speed = this.vel.length()) > 0.0f) {
                float speedIncrease = this.proj.getMoveSpeed() / this.maxDur * 0.5f;
                Vector2f dir = new Vector2f((ReadableVector2f)this.dirVelChange);
                dir.scale(speedIncrease * amount);
                Vector2f.add((Vector2f)this.vel, (Vector2f)dir, (Vector2f)this.vel);
            }
            this.angle += this.turnDir * amount;
            this.fader.advance(amount);
        }
    }
}

