/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEngineLayers
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.DamageAPI
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.OnHitEffectPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ViewportAPI
 *  com.fs.starfarer.api.combat.listeners.AdvanceableListener
 *  com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
 *  com.fs.starfarer.api.combat.listeners.DamageTakenModifier
 *  com.fs.starfarer.api.graphics.SpriteAPI
 *  com.fs.starfarer.api.util.FaderUtil
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class ShieldPenetrationEffect
implements OnHitEffectPlugin {
    private static final float SHIELD_DEBUFF_DURATION = 2.0f;
    private static final float SHIELD_DAMAGE_MULT = 1.25f;
    private static final float FRACTURE_DURATION = 5.0f;
    private static final float FRACTURE_DPS_MULT = 0.1f;
    private static final float FRACTURE_INTERVAL = 1.0f;
    private static final Random rand = new Random();

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!(target instanceof ShipAPI)) {
            return;
        }
        ShipAPI ship = (ShipAPI)target;
        if (shieldHit) {
            float baseDamage = projectile.getDamageAmount();
            engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)new ShieldFractureEffect(ship, point, baseDamage, projectile.getSource()));
            this.applyShieldDebuff(ship, 2.0f);
            if (rand.nextFloat() < 0.5f) {
                float damage = projectile.getDamageAmount();
                float emp = projectile.getEmpAmount();
                DamageType type = projectile.getDamageType();
                engine.applyDamage((CombatEntityAPI)ship, point, damage, type, emp, true, false, (Object)projectile.getSource(), false);
            }
        }
    }

    private void applyShieldDebuff(ShipAPI ship, float duration) {
        ship.addListener((Object)new ShieldDebuffListener(ship, duration));
    }

    private static class ShieldFractureEffect
    extends BaseCombatLayeredRenderingPlugin {
        private final ShipAPI target;
        private final Vector2f offset;
        private final float damagePerTick;
        private final ShipAPI source;
        private float elapsed = 0.0f;
        private IntervalUtil interval = new IntervalUtil(1.0f, 1.0f);
        private FaderUtil fader = new FaderUtil(1.0f, 0.5f, 0.5f);
        private List<ParticleData> particles = new ArrayList<ParticleData>();

        public ShieldFractureEffect(ShipAPI target, Vector2f point, float baseDamage, ShipAPI source) {
            this.target = target;
            this.source = source;
            Vector2f temp = Vector2f.sub((Vector2f)point, (Vector2f)target.getLocation(), (Vector2f)new Vector2f());
            this.offset = Misc.rotateAroundOrigin((Vector2f)temp, (float)(-target.getFacing()));
            this.damagePerTick = baseDamage * 0.1f;
            this.interval.forceIntervalElapsed();
        }

        public void init(CombatEntityAPI entity) {
            super.init(entity);
        }

        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) {
                return;
            }
            Vector2f loc = new Vector2f((ReadableVector2f)this.offset);
            loc = Misc.rotateAroundOrigin((Vector2f)loc, (float)this.target.getFacing());
            Vector2f.add((Vector2f)this.target.getLocation(), (Vector2f)loc, (Vector2f)loc);
            this.entity.getLocation().set((ReadableVector2f)loc);
            ArrayList<ParticleData> remove = new ArrayList<ParticleData>();
            for (ParticleData p : this.particles) {
                p.advance(amount);
                if (!(p.elapsed >= p.maxDur)) continue;
                remove.add(p);
            }
            this.particles.removeAll(remove);
            this.elapsed += amount;
            if (this.elapsed >= 5.0f || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.target)) {
                this.fader.fadeOut();
                this.fader.advance(amount);
                if (this.fader.isFadedOut()) {
                    // empty if block
                }
                return;
            }
            this.interval.advance(amount);
            if (this.interval.intervalElapsed()) {
                this.dealDamage();
                for (int i = 0; i < 3; ++i) {
                    this.addParticle();
                }
            }
        }

        private void dealDamage() {
            CombatEngineAPI engine = Global.getCombatEngine();
            Vector2f point = new Vector2f((ReadableVector2f)this.entity.getLocation());
            engine.applyDamage((CombatEntityAPI)this.target, point, this.damagePerTick, DamageType.ENERGY, 0.0f, false, false, (Object)this.source, false);
            engine.addFloatingDamageText(point, this.damagePerTick, 0.0f, new Color(150, 200, 255), (CombatEntityAPI)this.target, (CombatEntityAPI)this.source);
        }

        private void addParticle() {
            ParticleData p = new ParticleData(20.0f, 0.8f + (float)Math.random() * 0.4f, 0.5f);
            p.offset = Misc.getPointWithinRadius((Vector2f)new Vector2f(), (float)15.0f);
            p.color = new Color(100, 180, 255, 80);
            this.particles.add(p);
        }

        public float getRenderRadius() {
            return 300.0f;
        }

        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER);
        }

        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            float x = this.entity.getLocation().x;
            float y = this.entity.getLocation().y;
            float alpha = viewport.getAlphaMult() * this.fader.getBrightness();
            for (ParticleData p : this.particles) {
                float size = p.baseSize * p.scale;
                Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
                p.sprite.setAngle(p.angle);
                p.sprite.setSize(size, size);
                p.sprite.setAlphaMult(alpha * p.fader.getBrightness());
                p.sprite.setColor(p.color);
                p.sprite.renderAtCenter(loc.x, loc.y);
            }
        }

        public boolean isExpired() {
            return this.elapsed >= 5.0f && this.particles.isEmpty() || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.target);
        }
    }

    private static class ShieldDebuffListener
    implements DamageTakenModifier,
    AdvanceableListener {
        private final ShipAPI ship;
        private float remainingTime;

        public ShieldDebuffListener(ShipAPI ship, float duration) {
            this.ship = ship;
            this.remainingTime = duration;
        }

        public void advance(float amount) {
            this.remainingTime -= amount;
            if (this.remainingTime <= 0.0f) {
                this.ship.removeListener((Object)this);
            }
        }

        public float modifyDamageTaken(CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (shieldHit && target == this.ship) {
                return 1.25f;
            }
            return 1.0f;
        }

        public String modifyDamageTaken(Object o, CombatEntityAPI combatEntityAPI, DamageAPI damageAPI, Vector2f vector2f, boolean b) {
            return "";
        }
    }

    private static class ParticleData {
        public SpriteAPI sprite = Global.getSettings().getSprite("misc", "nebula_particles");
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1.0f;
        public float scaleIncreaseRate;
        public float turnDir;
        public float angle;
        public float maxDur;
        public FaderUtil fader;
        public float elapsed = 0.0f;
        public float baseSize;
        public Color color = new Color(100, 150, 255, 35);

        public ParticleData(float baseSize, float maxDur, float endSizeMult) {
            float i = Misc.random.nextInt(4);
            float j = Misc.random.nextInt(4);
            this.sprite.setTexWidth(0.25f);
            this.sprite.setTexHeight(0.25f);
            this.sprite.setTexX(i * 0.25f);
            this.sprite.setTexY(j * 0.25f);
            this.sprite.setAdditiveBlend();
            this.angle = (float)Math.random() * 360.0f;
            this.maxDur = maxDur;
            this.scaleIncreaseRate = (endSizeMult - 1.0f) / maxDur;
            this.scale = 1.0f;
            this.baseSize = baseSize;
            this.turnDir = (float)Math.signum(Math.random() - 0.5) * 20.0f * (float)Math.random();
            float driftDir = (float)Math.random() * 360.0f;
            this.vel = Misc.getUnitVectorAtDegreeAngle((float)driftDir);
            this.vel.scale(0.25f * baseSize / maxDur * (1.0f + (float)Math.random()));
            this.fader = new FaderUtil(0.0f, 0.5f, 0.5f);
            this.fader.forceOut();
            this.fader.fadeIn();
        }

        public void advance(float amount) {
            this.scale += this.scaleIncreaseRate * amount;
            this.offset.x += this.vel.x * amount;
            this.offset.y += this.vel.y * amount;
            this.angle += this.turnDir * amount;
            this.elapsed += amount;
            if (this.maxDur - this.elapsed <= this.fader.getDurationOut() + 0.1f) {
                this.fader.fadeOut();
            }
            this.fader.advance(amount);
        }
    }
}

