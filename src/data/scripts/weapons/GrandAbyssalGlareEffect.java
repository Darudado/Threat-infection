/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BeamAPI
 *  com.fs.starfarer.api.combat.BeamEffectPlugin
 *  com.fs.starfarer.api.combat.CollisionClass
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.EmpArcEntityAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.graphics.SpriteAPI
 *  com.fs.starfarer.api.impl.combat.dweller.DarkenedGazeSystemScript
 *  com.fs.starfarer.api.impl.combat.dweller.RiftLightningEffect
 *  com.fs.starfarer.api.loading.DamagingExplosionSpec
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lazywizard.lazylib.MathUtils
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 *  org.magiclib.util.MagicRender
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.dweller.DarkenedGazeSystemScript;
import com.fs.starfarer.api.impl.combat.dweller.RiftLightningEffect;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class GrandAbyssalGlareEffect
implements BeamEffectPlugin {
    public static float RIFT_DAMAGE = 200.0f;
    public static float DAMAGE_MULT_NORMAL_WEAPON = 0.5f;
    private static final float EMP_EXPLOSION_DAMAGE = 400.0f;
    private static final float EMP_EXPLOSION_RADIUS = 1500.0f;
    private static final float ARC_DAMAGE_MULT = 0.25f;
    private static final float ARC_EMP_MULT = 0.5f;
    private static final float BRANCH_ARC_CHANCE = 0.6f;
    protected IntervalUtil fireInterval = new IntervalUtil(0.15f, 0.25f);
    protected IntervalUtil shockwaveInterval = new IntervalUtil(2.0f, 2.0f);
    protected boolean hadDamageTargetPrev = false;
    protected boolean lengthChangedPrev = false;
    protected float sinceRiftSpawn = 0.0f;
    protected Vector2f prevTo = null;
    protected Vector2f prevFrom = null;
    private final List<NukeLayerData> nukeLayers = new ArrayList<NukeLayerData>();

    public GrandAbyssalGlareEffect() {
        this.fireInterval.randomize();
        this.shockwaveInterval.randomize();
        this.initNukeLayers();
    }

    private void initNukeLayers() {
        NukeLayerData flash = new NukeLayerData();
        flash.spritePath = "graphics/fx/qx_explosion_2a.png";
        flash.initialSize = new Vector2f(2500.0f, 2500.0f);
        flash.growth = new Vector2f(-300.0f, -300.0f);
        flash.color = new Color(255, 220, 150, 200);
        flash.fadeIn = 0.05f;
        flash.duration = 1.0f;
        flash.fadeOut = 1.5f;
        this.nukeLayers.add(flash);
        NukeLayerData glow = new NukeLayerData();
        glow.spritePath = "graphics/fx/qx_glow_3.png";
        glow.initialSize = new Vector2f(2000.0f, 2000.0f);
        glow.growth = new Vector2f(800.0f, 800.0f);
        glow.color = new Color(200, 180, 255, 180);
        glow.fadeIn = 0.1f;
        glow.duration = 2.0f;
        glow.fadeOut = 2.5f;
        this.nukeLayers.add(glow);
        NukeLayerData shockwave = new NukeLayerData();
        shockwave.spritePath = "graphics/fx/qx_shockwave_2.png";
        shockwave.initialSize = new Vector2f(600.0f, 600.0f);
        shockwave.growth = new Vector2f(1600.0f, 1600.0f);
        shockwave.color = new Color(180, 140, 255, 150);
        shockwave.fadeIn = 0.1f;
        shockwave.duration = 2.2f;
        shockwave.fadeOut = 2.0f;
        this.nukeLayers.add(shockwave);
        NukeLayerData smoke = new NukeLayerData();
        smoke.spritePath = "graphics/fx/qx_aftermath_1.png";
        smoke.initialSize = new Vector2f(1500.0f, 1500.0f);
        smoke.growth = new Vector2f(600.0f, 600.0f);
        smoke.color = new Color(100, 100, 120, 150);
        smoke.fadeIn = 1.0f;
        smoke.duration = 3.0f;
        smoke.fadeOut = 2.0f;
        this.nukeLayers.add(smoke);
        NukeLayerData particles = new NukeLayerData();
        particles.spritePath = "graphics/fx/qx_particle_1.png";
        particles.initialSize = new Vector2f(300.0f, 300.0f);
        particles.growth = new Vector2f(700.0f, 700.0f);
        particles.color = new Color(255, 200, 150, 200);
        particles.fadeIn = 0.5f;
        particles.duration = 2.5f;
        particles.fadeOut = 2.5f;
        this.nukeLayers.add(particles);
    }

    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        boolean forceRiftSpawn;
        float mult;
        boolean primary;
        if (beam.getSource() == null || beam.getWeapon() == null) {
            return;
        }
        boolean normalWeaponMode = !beam.getSource().hasTag(DarkenedGazeSystemScript.DARKENED_GAZE_SYSTEM_TAG);
        boolean bl = primary = beam.getWeapon().getCustom() == DarkenedGazeSystemScript.DARKENED_GAZE_PRIMARY_WEAPON_TAG;
        if (normalWeaponMode) {
            primary = true;
        }
        this.sinceRiftSpawn += amount;
        float maxRange = beam.getWeapon().getRange();
        Vector2f from = beam.getFrom();
        Vector2f to = beam.getRayEndPrevFrame();
        Vector2f to2 = beam.getTo();
        float dist = Misc.getDistance((Vector2f)from, (Vector2f)to);
        float dist2 = Misc.getDistance((Vector2f)from, (Vector2f)to2);
        if (dist2 < dist) {
            to = to2;
            dist = dist2;
        }
        if ((mult = Global.getCombatEngine().getTimeMult().getModifiedValue()) < 0.1f) {
            mult = 0.1f;
        }
        float lengthChangeThreshold = 180.0f / mult * amount + Math.max(1.0f, beam.getSource().getMutableStats().getTimeMult().getModifiedValue()) * 10.0f;
        boolean hasDamageTarget = beam.getDamageTarget() != null;
        boolean lengthChanged = this.prevTo == null || Math.abs(Misc.getDistance((Vector2f)this.prevFrom, (Vector2f)this.prevTo) - Misc.getDistance((Vector2f)from, (Vector2f)to)) > lengthChangeThreshold;
        boolean bl2 = forceRiftSpawn = hasDamageTarget && !this.hadDamageTargetPrev || !lengthChanged && this.lengthChangedPrev;
        if (!primary) {
            forceRiftSpawn = false;
        }
        this.lengthChangedPrev = lengthChanged;
        this.hadDamageTargetPrev = hasDamageTarget;
        this.prevFrom = new Vector2f((ReadableVector2f)from);
        this.prevTo = new Vector2f((ReadableVector2f)to);
        this.fireInterval.advance(amount);
        this.shockwaveInterval.advance(amount);
        if (this.shockwaveInterval.intervalElapsed() && beam.getBrightness() > 0.5f) {
            this.spawnShockwave(engine, to, beam.getBrightness(), normalWeaponMode);
        }
        if (this.fireInterval.intervalElapsed() || forceRiftSpawn) {
            boolean shouldSpawnExplosion;
            if (beam.getDamageTarget() == null && dist < maxRange * 0.9f) {
                return;
            }
            if (beam.getBrightness() < 1.0f) {
                return;
            }
            Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
            float maxTimeWithoutExplosion = normalWeaponMode ? 0.4f : 0.8f;
            boolean bl3 = shouldSpawnExplosion = Math.random() > (double)0.6f || forceRiftSpawn || primary && this.sinceRiftSpawn > maxTimeWithoutExplosion;
            if (shouldSpawnExplosion) {
                float damageMult = normalWeaponMode ? DAMAGE_MULT_NORMAL_WEAPON : 1.0f;
                ShipAPI source = beam.getSource();
                engine.spawnDamagingExplosion(this.createNukeExplosionSpec(damageMult), source, to);
                this.spawnMultiLayerNukeVisual(engine, to, normalWeaponMode);
                this.spawnEmpExplosion(engine, to, source, damageMult);
                this.sinceRiftSpawn = 0.0f;
            }
            if (dist > 100.0f && (Math.random() > (double)0.3f || normalWeaponMode && shouldSpawnExplosion)) {
                this.spawnGrandLightningArcs(engine, from, to, beam, beam.getSource());
            }
            this.spawnBeamTrailParticles(engine, from, to, color, beam.getBrightness());
        }
        this.playLoopingSounds(beam, from, to, normalWeaponMode, primary);
    }

    private void spawnEmpExplosion(CombatEngineAPI engine, Vector2f location, ShipAPI source, float damageMult) {
        DamagingExplosionSpec spec = new DamagingExplosionSpec(0.5f, 1500.0f, 900.00006f, 400.0f * damageMult, 400.0f * damageMult * 0.5f, CollisionClass.PROJECTILE_FF, CollisionClass.PROJECTILE_FIGHTER, 3.0f, 2.0f, 0.5f, 0, new Color(100, 180, 255, 100), new Color(50, 120, 255, 50));
        spec.setDamageType(DamageType.ENERGY);
        spec.setUseDetailedExplosion(false);
        spec.setSoundSetId("abyssal_glare_explosion");
        spec.setSoundVolume(damageMult * 0.8f);
        engine.spawnDamagingExplosion(spec, source, location);
    }

    private void spawnShockwave(CombatEngineAPI engine, Vector2f location, float brightness, boolean normalMode) {
        float intensity = normalMode ? 0.8f : 1.2f;
        float baseAngle = MathUtils.getRandomNumberInRange((float)0.0f, (float)360.0f);
        try {
            Vector2f size1 = new Vector2f(1000.0f, 1000.0f);
            size1.scale(intensity);
            Vector2f growth1 = new Vector2f(-150.0f, -150.0f);
            Color color1 = new Color(180, 220, 255, (int)(220.0f * brightness * intensity));
            MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite("graphics/fx/qx_glow_1.png"), (Vector2f)location, (Vector2f)new Vector2f(), (Vector2f)size1, (Vector2f)growth1, (float)baseAngle, (float)0.0f, (Color)color1, (boolean)true, (float)0.0f, (float)1.0f, (float)1.5f);
        }
        catch (Exception size1) {
            // empty catch block
        }
        try {
            Vector2f size2 = new Vector2f(800.0f, 800.0f);
            size2.scale(intensity);
            Vector2f growth2 = new Vector2f(2200.0f, 2200.0f);
            Color color2 = new Color(80, 180, 255, (int)(180.0f * brightness * intensity));
            MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite("graphics/fx/qx_shockwave_2.png"), (Vector2f)location, (Vector2f)new Vector2f(), (Vector2f)size2, (Vector2f)growth2, (float)baseAngle, (float)0.0f, (Color)color2, (boolean)true, (float)0.0f, (float)4.0f, (float)2.0f);
        }
        catch (Exception size2) {
            // empty catch block
        }
        try {
            Vector2f size3 = new Vector2f(500.0f, 500.0f);
            size3.scale(intensity);
            Vector2f growth3 = new Vector2f(1500.0f, 1500.0f);
            Color color3 = new Color(120, 200, 255, (int)(150.0f * brightness * intensity));
            MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite("graphics/fx/qx_glow_3.png"), (Vector2f)location, (Vector2f)new Vector2f(), (Vector2f)size3, (Vector2f)growth3, (float)(baseAngle + 45.0f), (float)0.0f, (Color)color3, (boolean)true, (float)0.5f, (float)3.5f, (float)2.0f);
        }
        catch (Exception size3) {
            // empty catch block
        }
        try {
            Vector2f size4 = new Vector2f(350.0f, 350.0f);
            size4.scale(intensity);
            Vector2f growth4 = new Vector2f(1200.0f, 1200.0f);
            Color color4 = new Color(160, 210, 255, (int)(200.0f * brightness * intensity));
            MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite("graphics/fx/qx_particle_1.png"), (Vector2f)location, (Vector2f)new Vector2f(), (Vector2f)size4, (Vector2f)growth4, (float)(baseAngle + 90.0f), (float)0.0f, (Color)color4, (boolean)true, (float)0.8f, (float)3.0f, (float)2.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        engine.spawnExplosion(location, new Vector2f(), new Color(100, 180, 255, 120), 1800.0f * intensity, 6.0f);
    }

    private void spawnMultiLayerNukeVisual(CombatEngineAPI engine, Vector2f location, boolean normalMode) {
        float intensity = normalMode ? 0.6f : 1.0f;
        float randomAngle = MathUtils.getRandomNumberInRange((float)0.0f, (float)360.0f);
        for (NukeLayerData layer : this.nukeLayers) {
            Vector2f size = new Vector2f((ReadableVector2f)layer.initialSize);
            size.scale(intensity);
            Vector2f growth = new Vector2f((ReadableVector2f)layer.growth);
            growth.scale(intensity);
            Color color = new Color((int)((float)layer.color.getRed() * intensity), (int)((float)layer.color.getGreen() * intensity), (int)((float)layer.color.getBlue() * intensity), (int)((float)layer.color.getAlpha() * intensity));
            try {
                MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite(layer.spritePath), (Vector2f)location, (Vector2f)layer.velocity, (Vector2f)size, (Vector2f)growth, (float)randomAngle, (float)0.0f, (Color)color, (boolean)true, (float)layer.fadeIn, (float)layer.duration, (float)layer.fadeOut);
            }
            catch (Exception exception) {}
        }
    }

    private void spawnGrandLightningArcs(CombatEngineAPI engine, Vector2f from, Vector2f to, BeamAPI beam, ShipAPI source) {
        float beamFlux;
        float arcEmp;
        CombatEntityAPI target = beam.getDamageTarget();
        if (target == null) {
            return;
        }
        float beamDamage = beam.getDamage().getDamage();
        float arcDamage = beamDamage * 0.25f;
        EmpArcEntityAPI arc = engine.spawnEmpArcPierceShields(source, from, (CombatEntityAPI)source, target, DamageType.ENERGY, arcDamage, arcEmp = (beamFlux = beam.getDamage().getFluxComponent()) * 0.5f, 100000.0f, "abyssal_glare_lightning", beam.getWidth() + 20.0f, new Color(100, 180, 255, 255), new Color(50, 80, 200, 200));
        if (arc != null) {
            arc.setCoreWidthOverride(50.0f);
            arc.setRenderGlowAtStart(false);
            arc.setFadedOutAtStart(true);
            arc.setSingleFlickerMode(true);
        }
        if (Math.random() < (double)0.6f) {
            Vector2f mid = Vector2f.add((Vector2f)from, (Vector2f)to, (Vector2f)new Vector2f());
            mid.scale(0.5f);
            for (int i = 0; i < 2; ++i) {
                Vector2f branchEnd = Misc.getPointAtRadius((Vector2f)mid, (float)(80.0f + (float)Math.random() * 120.0f));
                EmpArcEntityAPI branchArc = engine.spawnEmpArcVisual(mid, (CombatEntityAPI)source, branchEnd, null, 25.0f, new Color(130, 180, 255, 200), new Color(60, 100, 180, 150));
                if (branchArc == null) continue;
                branchArc.setCoreWidthOverride(25.0f);
                branchArc.setSingleFlickerMode(true);
            }
        }
    }

    private void spawnBeamTrailParticles(CombatEngineAPI engine, Vector2f from, Vector2f to, Color color, float brightness) {
        if (brightness < 0.5f) {
            return;
        }
        Vector2f dir = Vector2f.sub((Vector2f)to, (Vector2f)from, (Vector2f)new Vector2f());
        float length = dir.length();
        if (length < 10.0f) {
            return;
        }
        dir.normalise();
        Vector2f perp = new Vector2f(-dir.y, dir.x);
        int particleCount = Math.max(10, (int)(length / 15.0f) + 1);
        for (int i = 0; i < particleCount; ++i) {
            float t = (float)i / (float)(particleCount - 1);
            Vector2f basePos = Vector2f.add((Vector2f)from, (Vector2f)((Vector2f)new Vector2f((ReadableVector2f)dir).scale(t * length)), (Vector2f)new Vector2f());
            float offsetMagnitude = (float)Math.random() * 15.0f * brightness;
            Vector2f offset = (Vector2f)new Vector2f((ReadableVector2f)perp).scale((float)Math.random() * offsetMagnitude);
            Vector2f finalPos = Vector2f.add((Vector2f)basePos, (Vector2f)offset, (Vector2f)new Vector2f());
            float size = 6.0f + (float)Math.random() * 8.0f;
            float life = 0.8f + (float)Math.random() * 0.5f;
            engine.addSmoothParticle(finalPos, new Vector2f(), size, brightness * 0.8f, life, new Color(100, 180, 255, 200));
        }
    }

    private void playLoopingSounds(BeamAPI beam, Vector2f from, Vector2f to, boolean normalMode, boolean primary) {
        Vector2f pt = Vector2f.add((Vector2f)from, (Vector2f)to, (Vector2f)new Vector2f());
        pt.scale(0.5f);
        if (normalMode) {
            Global.getSoundPlayer().playLoop("abyssal_glare_loop", (Object)beam.getSource(), 1.0f, beam.getBrightness(), pt, beam.getSource().getVelocity());
        } else if (primary) {
            Global.getSoundPlayer().playLoop("darkened_gaze_loop", (Object)beam.getSource(), 1.2f, beam.getBrightness(), pt, beam.getSource().getVelocity());
        }
    }

    public DamagingExplosionSpec createNukeExplosionSpec(float damageMult) {
        float damage = RIFT_DAMAGE * damageMult;
        DamagingExplosionSpec spec = new DamagingExplosionSpec(0.5f, 120.0f, 80.0f, damage, damage / 2.0f, CollisionClass.PROJECTILE_FF, CollisionClass.PROJECTILE_FIGHTER, 5.0f, 5.0f, 0.8f, 0, new Color(255, 255, 255, 0), new Color(255, 180, 100, 0));
        spec.setDamageType(DamageType.HIGH_EXPLOSIVE);
        spec.setUseDetailedExplosion(false);
        spec.setRadius(250.0f);
        spec.setSoundSetId("abyssal_glare_explosion");
        spec.setSoundVolume(damageMult * 1.2f);
        return spec;
    }

    private static class NukeLayerData {
        String spritePath;
        Vector2f velocity = new Vector2f(0.0f, 0.0f);
        Vector2f initialSize = new Vector2f(100.0f, 100.0f);
        Vector2f growth = new Vector2f(10.0f, 10.0f);
        Color color = new Color(255, 255, 255, 255);
        float fadeIn = 0.1f;
        float duration = 1.0f;
        float fadeOut = 1.0f;

        private NukeLayerData() {
        }
    }
}

