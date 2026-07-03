/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.ViewportAPI
 *  com.fs.starfarer.api.input.InputEventAPI
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class BlueRiftTrailEffect
extends BaseEveryFrameCombatPlugin {
    private MissileAPI missile;
    private String soundId;
    private static final Color TRAIL_COLOR = new Color(70, 130, 240, 220);

    public BlueRiftTrailEffect(MissileAPI missile, String soundId) {
        this.missile = missile;
        this.soundId = soundId;
    }

    public void init(CombatEngineAPI engine) {
    }

    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }
        if (!this.isValid()) {
            return;
        }
        if (this.missile.getWeapon() != null) {
            Global.getSoundPlayer().playLoop(this.soundId, (Object)this.missile, 1.0f, 1.0f, this.missile.getLocation(), this.missile.getVelocity());
        }
        Vector2f loc = this.missile.getLocation();
        Vector2f vel = this.missile.getVelocity();
        float speedSq = vel.lengthSquared();
        if (speedSq < 200.0f) {
            return;
        }
        float speed = (float)Math.sqrt(speedSq);
        Vector2f backDir = new Vector2f((ReadableVector2f)vel);
        backDir.normalise();
        float offset = -15.0f * Math.min(1.5f, speed / 300.0f);
        backDir.scale(offset);
        int particlesPerFrame = 4;
        for (int i = 0; i < particlesPerFrame; ++i) {
            float randomOffsetX = (float)Math.random() * 6.0f - 3.0f;
            float randomOffsetY = (float)Math.random() * 6.0f - 3.0f;
            float size = 5.0f + (float)Math.random() * 8.0f;
            float alpha = 0.8f + (float)Math.random() * 0.2f;
            float life = 0.9f + (float)Math.random() * 0.4f;
            float brightnessFactor = 0.9f + 0.2f * (float)Math.random();
            int red = (int)((float)TRAIL_COLOR.getRed() * brightnessFactor);
            int green = (int)((float)TRAIL_COLOR.getGreen() * brightnessFactor);
            int blue = (int)((float)TRAIL_COLOR.getBlue() * brightnessFactor);
            int alphaInt = (int)(255.0f * alpha);
            red = Math.min(255, Math.max(0, red));
            green = Math.min(255, Math.max(0, green));
            blue = Math.min(255, Math.max(0, blue));
            alphaInt = Math.min(255, Math.max(0, alphaInt));
            Color particleColor = new Color(red, green, blue, alphaInt);
            Vector2f particlePos = new Vector2f(loc.x + backDir.x + randomOffsetX, loc.y + backDir.y + randomOffsetY);
            Vector2f particleVel = new Vector2f((ReadableVector2f)vel);
            particleVel.scale(0.3f);
            engine.addSmoothParticle(particlePos, particleVel, size, alpha, life, particleColor);
        }
    }

    public void render(CombatEngineAPI engine, ViewportAPI viewport) {
    }

    private boolean isValid() {
        CombatEngineAPI engine = Global.getCombatEngine();
        return this.missile != null && !this.missile.isExpired() && !this.missile.isFizzling() && !this.missile.isFading() && engine.isEntityInPlay((CombatEntityAPI)this.missile);
    }
}

