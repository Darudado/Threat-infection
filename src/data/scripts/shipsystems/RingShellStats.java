/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineLayers
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.graphics.SpriteAPI
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 *  org.magiclib.util.MagicRender
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class RingShellStats
extends BaseShipSystemScript {
    public static final float MAX_TIME_MULT = 10.0f;
    public static final float MIN_TIME_MULT = 0.1f;
    public static final float DAM_MULT = 0.75f;
    public static final Color JITTER_COLOR = new Color(90, 0, 90, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(90, 0, 90, 155);
    private float rgbTimer = 0.0f;
    private float ghostTimer = 0.0f;
    private Vector2f lastGhostPosition = null;
    private float lastGhostFacing = 0.0f;

    private static double reduceSinAngle(double radians) {
        if (Math.abs(radians %= Math.PI * 2) > Math.PI) {
            radians -= Math.PI * 2;
        }
        if (Math.abs(radians) > 1.5707963267948966) {
            radians = Math.PI - radians;
        }
        return radians;
    }

    public static double sin(double radians) {
        return Math.abs(radians = RingShellStats.reduceSinAngle(radians)) <= 0.7853981633974483 ? Math.sin(radians) : Math.cos(1.5707963267948966 - radians);
    }

    public static double cos(double radians) {
        return RingShellStats.sin(radians + 1.5707963267948966);
    }

    private Color getDarkPurpleColor(float time, float effectLevel) {
        float baseR = 0.4f;
        float baseG = 0.0f;
        float baseB = 0.6f;
        float r = baseR + (float)(Math.sin(time * 0.5f + 0.0f) * (double)0.1f);
        float g = baseG + (float)(Math.sin(time * 0.5f + 2.094f) * (double)0.05f);
        float b = baseB + (float)(Math.sin(time * 0.5f + 4.189f) * (double)0.15f);
        r = Math.max(0.2f, Math.min(0.6f, r));
        g = Math.max(0.0f, Math.min(0.2f, g));
        b = Math.max(0.4f, Math.min(0.8f, b));
        float brightness = 0.7f + 0.3f * effectLevel;
        return new Color(Math.min(1.0f, r * brightness), Math.min(1.0f, g * brightness), Math.min(1.0f, b * brightness), 0.8f * effectLevel);
    }

    private Color getDarkPurpleGlowColor(float time, float effectLevel) {
        float pulse = (float)((Math.sin(time * 0.8f) + 1.0) * 0.5);
        if (pulse < 0.33f) {
            return new Color(80, 0, 120, (int)(180.0f * effectLevel));
        }
        if (pulse < 0.66f) {
            return new Color(100, 0, 140, (int)(200.0f * effectLevel));
        }
        return new Color(120, 0, 100, (int)(190.0f * effectLevel));
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = (String)id + "_" + ship.getId();
            float amount = Global.getCombatEngine().getElapsedInLastFrame();
            this.rgbTimer += amount;
            this.ghostTimer += amount;
            float jitterLevel = effectLevel;
            float jitterRangeBonus = 0.0f;
            float maxRangeBonus = 10.0f;
            if (state == ShipSystemStatsScript.State.IN) {
                jitterLevel = effectLevel / (1.0f / ship.getSystem().getChargeUpDur());
                if (jitterLevel > 1.0f) {
                    jitterLevel = 1.0f;
                }
                jitterRangeBonus = jitterLevel * maxRangeBonus;
            } else if (state == ShipSystemStatsScript.State.ACTIVE) {
                jitterLevel = 1.0f;
                jitterRangeBonus = maxRangeBonus;
            } else if (state == ShipSystemStatsScript.State.OUT) {
                jitterRangeBonus = effectLevel * maxRangeBonus;
            }
            jitterLevel = (float)Math.sqrt(jitterLevel);
            effectLevel *= effectLevel;
            Color purpleJitterColor = this.getDarkPurpleColor(this.rgbTimer, effectLevel);
            Color purpleJitterUnderColor = this.getDarkPurpleGlowColor(this.rgbTimer, effectLevel);
            ship.setJitter((Object)this, purpleJitterColor, jitterLevel, 5, 0.0f, 0.0f + jitterRangeBonus);
            ship.setJitterUnder((Object)this, purpleJitterUnderColor, jitterLevel, 30, 0.0f, 10.0f + jitterRangeBonus);
            stats.getHullDamageTakenMult().modifyMult((String)id, 0.75f);
            stats.getArmorDamageTakenMult().modifyMult((String)id, 0.75f);
            stats.getEmpDamageTakenMult().modifyMult((String)id, 0.75f);
            float shipTimeMult = 1.0f + 5.0f * effectLevel;
            stats.getTimeMult().modifyMult((String)id, shipTimeMult);
            if (player) {
                Global.getCombatEngine().getTimeMult().modifyMult((String)id, 3.0f / shipTimeMult);
            } else {
                Global.getCombatEngine().getTimeMult().unmodify((String)id);
            }
            ship.getEngineController().fadeToOtherColor((Object)this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevel, 0.5f);
            ship.getEngineController().extendFlame((Object)this, -0.25f, -0.25f, -0.25f);
            if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE || state == ShipSystemStatsScript.State.OUT) {
                for (int i = 0; i < 3; ++i) {
                    float sizeMultiplier = 1.05f + (float)i * 0.03f;
                    float alpha = 8.0E-4f - (float)i * 2.0E-4f;
                    Color edgeColor = this.getDarkPurpleColor(this.rgbTimer + (float)i * 0.03f, alpha);
                    MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()), (Vector2f)new Vector2f(ship.getLocation().getX(), ship.getLocation().getY()), (Vector2f)new Vector2f(0.0f, 0.0f), (Vector2f)new Vector2f(ship.getSpriteAPI().getWidth() * sizeMultiplier, ship.getSpriteAPI().getHeight() * sizeMultiplier), (Vector2f)new Vector2f(0.0f, 0.0f), (float)(ship.getFacing() - 90.0f), (float)0.0f, (Color)edgeColor, (boolean)true, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.05f, (float)0.1f, (float)0.05f, (float)0.05f, (float)0.05f, (CombatEngineLayers)CombatEngineLayers.ABOVE_SHIPS_LAYER);
                }
            }
            if (this.ghostTimer >= 2.0f) {
                this.ghostTimer = 0.0f;
                this.lastGhostPosition = new Vector2f((ReadableVector2f)ship.getLocation());
                this.lastGhostFacing = ship.getFacing();
                SpriteAPI sprite = ship.getSpriteAPI();
                float offsetX = sprite.getWidth() / 2.0f - sprite.getCenterX();
                float offsetY = sprite.getHeight() / 2.0f - sprite.getCenterY();
                float trueOffsetX = (float)RingShellStats.cos(Math.toRadians(this.lastGhostFacing - 90.0f)) * offsetX - (float)RingShellStats.sin(Math.toRadians(this.lastGhostFacing - 90.0f)) * offsetY;
                float trueOffsetY = (float)RingShellStats.sin(Math.toRadians(this.lastGhostFacing - 90.0f)) * offsetX + (float)RingShellStats.cos(Math.toRadians(this.lastGhostFacing - 90.0f)) * offsetY;
                Color ghostColor = this.getDarkPurpleColor(this.rgbTimer, 0.8f);
                MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()), (Vector2f)new Vector2f(this.lastGhostPosition.x + trueOffsetX, this.lastGhostPosition.y + trueOffsetY), (Vector2f)new Vector2f(0.0f, 0.0f), (Vector2f)new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()), (Vector2f)new Vector2f(0.0f, 0.0f), (float)(this.lastGhostFacing - 90.0f), (float)0.0f, (Color)ghostColor, (boolean)true, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f, (float)5.0f, (float)0.5f, (float)0.5f, (float)0.5f, (CombatEngineLayers)CombatEngineLayers.BELOW_SHIPS_LAYER);
                Color ghostEdgeColor = this.getDarkPurpleGlowColor(this.rgbTimer, 0.6f);
                MagicRender.battlespace((SpriteAPI)Global.getSettings().getSprite(ship.getHullSpec().getSpriteName()), (Vector2f)new Vector2f(this.lastGhostPosition.x + trueOffsetX, this.lastGhostPosition.y + trueOffsetY), (Vector2f)new Vector2f(0.0f, 0.0f), (Vector2f)new Vector2f(ship.getSpriteAPI().getWidth() * 1.05f, ship.getSpriteAPI().getHeight() * 1.05f), (Vector2f)new Vector2f(0.0f, 0.0f), (float)(this.lastGhostFacing - 90.0f), (float)0.0f, (Color)ghostEdgeColor, (boolean)true, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f, (float)14.0f, (float)0.5f, (float)0.5f, (float)0.5f, (CombatEngineLayers)CombatEngineLayers.BELOW_SHIPS_LAYER);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = null;
        boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI)stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = (String)id + "_" + ship.getId();
            Global.getCombatEngine().getTimeMult().unmodify((String)id);
            stats.getTimeMult().unmodify((String)id);
            stats.getHullDamageTakenMult().unmodify((String)id);
            stats.getArmorDamageTakenMult().unmodify((String)id);
            stats.getEmpDamageTakenMult().unmodify((String)id);
            this.rgbTimer = 0.0f;
            this.ghostTimer = 0.0f;
            this.lastGhostPosition = null;
        }
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        float shipTimeMult = 1.0f + 5.0f * effectLevel;
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("\u65f6\u95f4\u6d41\u901f\u6539\u53d8 (" + String.format("%.1f", Float.valueOf(shipTimeMult)) + "x)", false);
        }
        if (index == 1) {
            return new ShipSystemStatsScript.StatusData("\u5171\u9e23\u5df2\u6fc0\u6d3b", false);
        }
        if (index == 2) {
            return new ShipSystemStatsScript.StatusData("\u51cf\u4f2425%", false);
        }
        return null;
    }
}

