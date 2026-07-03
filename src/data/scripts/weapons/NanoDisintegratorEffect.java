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
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.OnHitEffectPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ViewportAPI
 *  com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
 *  com.fs.starfarer.api.graphics.SpriteAPI
 *  com.fs.starfarer.api.util.FaderUtil
 *  com.fs.starfarer.api.util.IntervalUtil
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
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class NanoDisintegratorEffect
extends BaseCombatLayeredRenderingPlugin
implements OnHitEffectPlugin {
    public static int NUM_TICKS = 6;
    public static float TOTAL_DAMAGE = 200.0f;
    private static final float ARC_CHANCE = 0.1f;
    private static final Color NANO_PARTICLE_COLOR = new Color(0, 255, 200, 35);
    private static final Color ARC_CORE_COLOR = new Color(0, 255, 200, 255);
    private static final Color ARC_FRINGE_COLOR = new Color(200, 255, 230, 255);
    protected List<ParticleData> particles = new ArrayList<ParticleData>();
    protected DamagingProjectileAPI proj;
    protected ShipAPI target;
    protected Vector2f offset;
    protected int ticks = 0;
    protected IntervalUtil interval;
    protected FaderUtil fader = new FaderUtil(1.0f, 0.5f, 0.5f);
    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.BELOW_INDICATORS_LAYER);

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && !projectile.isFading() && target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI)target;
            Vector2f offset = Vector2f.sub((Vector2f)point, (Vector2f)ship.getLocation(), (Vector2f)new Vector2f());
            offset = Misc.rotateAroundOrigin((Vector2f)offset, (float)(-ship.getFacing()));
            NanoDisintegratorEffect effect = new NanoDisintegratorEffect(projectile, ship, offset);
            CombatEntityAPI e = engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)effect);
            e.getLocation().set((ReadableVector2f)projectile.getLocation());
        }
    }

    public NanoDisintegratorEffect() {
    }

    public NanoDisintegratorEffect(DamagingProjectileAPI proj, ShipAPI target, Vector2f offset) {
        this.proj = proj;
        this.target = target;
        this.offset = offset;
        this.interval = new IntervalUtil(0.8f, 1.0f);
        this.interval.forceIntervalElapsed();
    }

    protected float getTotalDamage() {
        return TOTAL_DAMAGE;
    }

    protected int getNumTicks() {
        return NUM_TICKS;
    }

    protected boolean canDamageHull() {
        return false;
    }

    protected String getSoundLoopId() {
        return "disintegrator_loop";
    }

    protected int getNumParticlesPerTick() {
        return 3;
    }

    public float getRenderRadius() {
        return 500.0f;
    }

    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return this.layers;
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
        float volume = 1.0f;
        if (this.ticks >= this.getNumTicks() || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.target)) {
            this.fader.fadeOut();
            this.fader.advance(amount);
            volume = this.fader.getBrightness();
        }
        Global.getSoundPlayer().playLoop(this.getSoundLoopId(), (Object)this.target, 1.0f, volume, loc, this.target.getVelocity());
        this.interval.advance(amount);
        if (this.interval.intervalElapsed() && this.ticks < this.getNumTicks()) {
            this.dealDamage();
            ++this.ticks;
        }
    }

    public boolean isExpired() {
        return this.particles.isEmpty() && (this.ticks >= this.getNumTicks() || !this.target.isAlive() || !Global.getCombatEngine().isEntityInPlay((CombatEntityAPI)this.target));
    }

    protected void dealDamage() {
        float showHullDamage;
        CombatEngineAPI engine = Global.getCombatEngine();
        int num = this.getNumParticlesPerTick();
        for (int i = 0; i < num; ++i) {
            this.addParticle();
        }
        Vector2f point = new Vector2f((ReadableVector2f)this.entity.getLocation());
        ArmorGridAPI grid = this.target.getArmorGrid();
        int[] cell = grid.getCellAtLocation(point);
        if (cell == null) {
            return;
        }
        int gridWidth = grid.getGrid().length;
        int gridHeight = grid.getGrid()[0].length;
        float damageTypeMult = NanoDisintegratorEffect.getDamageTypeMult(this.proj.getSource(), this.target);
        float damagePerTick = this.getTotalDamage() / (float)this.getNumTicks();
        float armorDamageDealt = 0.0f;
        float hullDamage = 0.0f;
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                float armorInCell;
                if (!(i != 2 && i != -2 || j != 2 && j != -2)) continue;
                int cx = cell[0] + i;
                int cy = cell[1] + j;
                if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;
                float damMult = i == 0 && j == 0 ? 0.06666667f : (Math.abs(i) <= 1 && Math.abs(j) <= 1 ? 0.06666667f : 0.033333335f);
                float damage = damagePerTick * damMult * damageTypeMult;
                if (damage > (armorInCell = grid.getArmorValue(cx, cy)) && this.canDamageHull()) {
                    hullDamage += damage - armorInCell;
                }
                if (!((damage = Math.min(damage, armorInCell)) > 0.0f)) continue;
                grid.setArmorValue(cx, cy, Math.max(0.0f, armorInCell - damage));
                armorDamageDealt += damage;
            }
        }
        if (armorDamageDealt > 0.0f) {
            if (Misc.shouldShowDamageFloaty((ShipAPI)this.proj.getSource(), (ShipAPI)this.target)) {
                engine.addFloatingDamageText(point, armorDamageDealt, 0.0f, Misc.FLOATY_ARMOR_DAMAGE_COLOR, (CombatEntityAPI)this.target, (CombatEntityAPI)this.proj.getSource());
            }
            this.target.syncWithArmorGridState();
        }
        if (hullDamage > 1.0f && (showHullDamage = Math.min(hullDamage, this.target.getHitpoints())) >= 0.0f) {
            this.target.setHitpoints(this.target.getHitpoints() - hullDamage);
            if (this.target.getHitpoints() <= 0.0f && !this.target.isHulk()) {
                this.target.setSpawnDebris(false);
                engine.applyDamage((CombatEntityAPI)this.target, point, 100.0f, DamageType.ENERGY, 0.0f, true, false, (Object)this.proj.getSource(), false);
            }
            if (Misc.shouldShowDamageFloaty((ShipAPI)this.proj.getSource(), (ShipAPI)this.target)) {
                Vector2f p2 = new Vector2f((ReadableVector2f)point);
                p2.y += 20.0f;
                engine.addFloatingDamageText(p2, hullDamage, 0.0f, Misc.FLOATY_HULL_DAMAGE_COLOR, (CombatEntityAPI)this.target, (CombatEntityAPI)this.proj.getSource());
            }
        }
        if (Math.random() < (double)0.1f) {
            engine.spawnEmpArcPierceShields(this.proj.getSource(), point, (CombatEntityAPI)this.target, (CombatEntityAPI)this.target, DamageType.ENERGY, damagePerTick, 500.0f, 500.0f, null, 20.0f, ARC_CORE_COLOR, ARC_FRINGE_COLOR);
        }
        this.damageDealt(point, hullDamage, armorDamageDealt);
    }

    protected void damageDealt(Vector2f loc, float hullDamage, float armorDamage) {
    }

    protected void addParticle() {
        ParticleData p = new ParticleData(30.0f, 3.0f + (float)Math.random() * 2.0f, 2.0f);
        p.color = NANO_PARTICLE_COLOR;
        this.particles.add(p);
        p.offset = Misc.getPointWithinRadius((Vector2f)p.offset, (float)20.0f);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        float x = this.entity.getLocation().x;
        float y = this.entity.getLocation().y;
        float alphaMult = viewport.getAlphaMult();
        for (ParticleData p : this.particles) {
            float size = p.baseSize * p.scale;
            Vector2f loc = new Vector2f(x + p.offset.x, y + p.offset.y);
            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size, size);
            p.sprite.setAlphaMult(alphaMult * p.fader.getBrightness());
            p.sprite.setColor(p.color);
            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }

    public static float getDamageTypeMult(ShipAPI source, ShipAPI target) {
        if (source == null || target == null) {
            return 1.0f;
        }
        float mult = target.getMutableStats().getArmorDamageTakenMult().getModifiedValue();
        switch (target.getHullSize()) {
            case FIGHTER: {
                mult *= source.getMutableStats().getDamageToFighters().getModifiedValue();
                break;
            }
            case FRIGATE: {
                mult *= source.getMutableStats().getDamageToFrigates().getModifiedValue();
                break;
            }
            case DESTROYER: {
                mult *= source.getMutableStats().getDamageToDestroyers().getModifiedValue();
                break;
            }
            case CRUISER: {
                mult *= source.getMutableStats().getDamageToCruisers().getModifiedValue();
                break;
            }
            case CAPITAL_SHIP: {
                mult *= source.getMutableStats().getDamageToCapital().getModifiedValue();
            }
        }
        return mult;
    }

    public static class ParticleData {
        public SpriteAPI sprite = Global.getSettings().getSprite("misc", "nebula_particles");
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1.0f;
        public float scaleIncreaseRate = 1.0f;
        public float turnDir = 1.0f;
        public float angle = 1.0f;
        public float maxDur;
        public FaderUtil fader;
        public float elapsed = 0.0f;
        public float baseSize;
        public Color color = new Color(0, 255, 200, 35);

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
            this.turnDir = Math.signum((float)Math.random() - 0.5f) * 20.0f * (float)Math.random();
            float driftDir = (float)Math.random() * 360.0f;
            this.vel = Misc.getUnitVectorAtDegreeAngle((float)driftDir);
            this.vel.scale(0.25f * baseSize / maxDur * (1.0f + (float)Math.random() * 1.0f));
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

