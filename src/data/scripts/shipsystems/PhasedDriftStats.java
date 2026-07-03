/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;

public class PhasedDriftStats
extends BaseShipSystemScript {
    private static final float TURN_ACC_BUFF_PERCENT = 1000.0f;
    private static final float TURN_RATE_BUFF_PERCENT = 500.0f;
    private static final float ACCEL_BUFF_PERCENT = 500.0f;
    private static final float DECCEL_BUFF_PERCENT = 300.0f;
    private static final float SPEED_BUFF_PERCENT = 200.0f;
    private static final float TIME_BUFF_PERCENT = 1000.0f;
    private static final float DAMAGE_REDUCTION = 0.7f;
    private String displayText = "\u76f8\u4f4d\u6f02\u79fb";

    public void setDisplayText(String text) {
        this.displayText = text;
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship == null) {
            return;
        }
        float effect = this.smooth(effectLevel);
        switch (state) {
            case IN: 
            case ACTIVE: {
                ship.setPhased(true);
                ship.setExtraAlphaMult(1.0f - 0.8f * effect);
                ship.setApplyExtraAlphaToEngines(true);
                break;
            }
            case OUT: {
                ship.setPhased(effectLevel > 0.5f);
                ship.setExtraAlphaMult(0.2f + 0.8f * (1.0f - effectLevel));
                ship.setApplyExtraAlphaToEngines(true);
                break;
            }
            default: {
                ship.setPhased(false);
                ship.setExtraAlphaMult(1.0f);
            }
        }
        stats.getTurnAcceleration().modifyPercent(id, 1000.0f * effect);
        stats.getMaxTurnRate().modifyPercent(id, 500.0f * effect);
        stats.getMaxSpeed().modifyPercent(id, 200.0f * effect);
        stats.getAcceleration().modifyPercent(id, 500.0f * effect);
        stats.getDeceleration().modifyPercent(id, 300.0f * effect);
        stats.getTimeMult().modifyPercent(id, 1000.0f * effect);
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            float damageMult = 1.0f - 0.7f * effect;
            stats.getHullDamageTakenMult().modifyMult(id, damageMult);
            stats.getArmorDamageTakenMult().modifyMult(id, damageMult);
            stats.getShieldDamageTakenMult().modifyMult(id, damageMult);
        }
        this.applyVisualEffects(ship, effect, state);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship != null) {
            stats.getTurnAcceleration().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            stats.getMaxSpeed().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getTimeMult().unmodify(id);
            stats.getHullDamageTakenMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getShieldDamageTakenMult().unmodify(id);
            ship.setPhased(false);
            ship.setExtraAlphaMult(1.0f);
            ship.getEngineController().fadeToOtherColor((Object)this, Color.white, new Color(0, 0, 0, 0), 0.0f, 0.0f);
        }
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            float timeMult = 1.0f + 10.0f * effectLevel;
            return new ShipSystemStatsScript.StatusData(this.displayText + " \u65f6\u6d41 x" + String.format("%.1f", Float.valueOf(timeMult)), false);
        }
        if (index == 1 && (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE)) {
            int dmgRed = (int)(0.7f * effectLevel * 100.0f);
            return new ShipSystemStatsScript.StatusData("\u51cf\u4f24 " + dmgRed + "%", false);
        }
        return null;
    }

    private float smooth(float x) {
        if (x <= 0.0f) {
            return 0.0f;
        }
        if (x >= 1.0f) {
            return 1.0f;
        }
        return x * x * (3.0f - 2.0f * x);
    }

    private void applyVisualEffects(ShipAPI ship, float effect, ShipSystemStatsScript.State state) {
        float jitterLevel = state == ShipSystemStatsScript.State.OUT ? 1.0f - effect : effect;
        jitterLevel = Math.min(1.0f, jitterLevel * 1.5f);
        Color jitterColor = new Color(200, 160, 60, 100);
        ship.setJitter((Object)this, jitterColor, jitterLevel, 3, ship.getCollisionRadius() * 0.1f);
        ship.setJitterUnder((Object)this, jitterColor, jitterLevel * 0.7f, 5, ship.getCollisionRadius() * 0.15f);
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            ship.getEngineController().fadeToOtherColor((Object)this, new Color(255, 200, 50, 200), new Color(100, 90, 70, 150), effect, 0.5f);
            ship.getEngineController().extendFlame((Object)this, 2.0f * effect, 0.8f * effect, 0.5f * effect);
        } else if (state == ShipSystemStatsScript.State.OUT) {
            ship.getEngineController().fadeToOtherColor((Object)this, new Color(255, 200, 50, 200), new Color(100, 90, 70, 150), 1.0f - effect, 0.5f);
            ship.getEngineController().extendFlame((Object)this, 2.0f * (1.0f - effect), 0.8f * (1.0f - effect), 0.5f * (1.0f - effect));
        }
    }
}

