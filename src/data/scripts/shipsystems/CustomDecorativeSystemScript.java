/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShieldAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class CustomDecorativeSystemScript
extends BaseShipSystemScript {
    public static final float BACK_OFF_RANGE_OFFSET = 300.0f;
    public static final float SHIELD_UNFOLD_MULT = 20.0f;
    private static final Color JITTER_COLOR = new Color(80, 160, 255, 100);
    private static final Color JITTER_UNDER_COLOR = new Color(100, 180, 255, 150);
    private static final Color ENGINE_FLAME_COLOR = new Color(70, 150, 255, 200);
    public List<WeaponAPI> targetWeapons = null;

    protected void findWeapons(ShipAPI ship) {
        if (this.targetWeapons == null) {
            this.targetWeapons = new ArrayList<WeaponAPI>();
            for (WeaponAPI w : ship.getAllWeapons()) {
                if (!w.getSlot().isDecorative()) continue;
                this.targetWeapons.add(w);
            }
        }
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = null;
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ship = (ShipAPI)stats.getEntity();
        this.findWeapons(ship);
        ShieldAPI shield = ship.getShield();
        String uniqueId = id + "_" + ship.getId();
        float jitterLevel = effectLevel;
        float jitterRangeBonus = 0.0f;
        float maxRangeBonus = 8.0f;
        if (state == ShipSystemStatsScript.State.IN) {
            float chargeUpTime = ship.getSystem().getChargeUpDur();
            if (chargeUpTime > 0.0f && (jitterLevel = effectLevel / (1.0f / chargeUpTime)) > 1.0f) {
                jitterLevel = 1.0f;
            }
            jitterRangeBonus = jitterLevel * maxRangeBonus;
        } else if (state == ShipSystemStatsScript.State.ACTIVE) {
            jitterLevel = 1.0f;
            jitterRangeBonus = maxRangeBonus;
        } else if (state == ShipSystemStatsScript.State.OUT) {
            jitterRangeBonus = effectLevel * maxRangeBonus;
        }
        ship.setJitter((Object)this, JITTER_COLOR, jitterLevel, 3, 0.0f, 0.0f + jitterRangeBonus);
        ship.setJitterUnder((Object)this, JITTER_UNDER_COLOR, jitterLevel, 20, 0.0f, 7.0f + jitterRangeBonus);
        ship.getEngineController().fadeToOtherColor((Object)this, ENGINE_FLAME_COLOR, null, effectLevel, 0.5f);
        ship.getEngineController().extendFlame((Object)this, -0.2f, -0.2f, -0.2f);
        if (shield != null) {
            if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
                if (!ship.getFluxTracker().isOverloadedOrVenting()) {
                    shield.toggleOn();
                }
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                stats.getShieldUnfoldRateMult().modifyMult(uniqueId, 20.0f);
            } else if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IDLE) {
                stats.getShieldUnfoldRateMult().unmodifyMult(uniqueId);
            }
        }
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            stats.getShieldDamageTakenMult().modifyMult(uniqueId, 0.5f);
            stats.getBeamWeaponFluxCostMult().modifyMult(uniqueId, 0.75f);
        } else if (state == ShipSystemStatsScript.State.OUT || state == ShipSystemStatsScript.State.IDLE) {
            stats.getShieldDamageTakenMult().unmodifyMult(uniqueId);
            stats.getBeamWeaponFluxCostMult().unmodifyMult(uniqueId);
        }
        if (state == ShipSystemStatsScript.State.ACTIVE) {
            for (WeaponAPI w : this.targetWeapons) {
                w.setForceFireOneFrame(true);
            }
        }
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE) {
            if (!this.targetWeapons.isEmpty()) {
                float range = this.targetWeapons.get(0).getRange();
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE, effectLevel, (Object)Float.valueOf(range - 300.0f));
            }
        } else if (state == ShipSystemStatsScript.State.IDLE) {
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE, 0.0f, (Object)Float.valueOf(-1.0f));
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship != null) {
            String uniqueId = id + "_" + ship.getId();
            stats.getShieldUnfoldRateMult().unmodifyMult(uniqueId);
            stats.getShieldDamageTakenMult().unmodifyMult(uniqueId);
            stats.getBeamWeaponFluxCostMult().unmodifyMult(uniqueId);
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.BACK_OFF_MIN_RANGE, 0.0f, (Object)Float.valueOf(-1.0f));
        }
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("\u70bc\u72f1\u5c04\u7ebf\uff1a\u5df2\u542f\u7528", false);
        }
        return null;
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        return null;
    }
}

