/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.util.IntervalUtil
 *  org.lazywizard.lazylib.MathUtils
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class ExcelsiorPhaseStat_Independent
extends BaseShipSystemScript {
    private static final float MAX_TIME_MULT_MAX = 4.0f;
    private static final Color SPARK_COLOR = new Color(255, 0, 200);
    private static final Color GLOW_COLOR = new Color(200, 0, 255, 75);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 100);
    private static final Vector2f ZERO = new Vector2f(0.0f, 0.0f);
    private final Object STATUSKEY1 = new Object();
    private final Object STATUSKEY2 = new Object();
    private final Object STATUSKEY4 = new Object();
    private final Object STATUSKEY5 = new Object();
    private final IntervalUtil syncInterval = new IntervalUtil(0.4f, 0.8f);
    private final IntervalUtil afterimageInterval = new IntervalUtil(0.033f, 0.033f);
    private float killTimer = 0.0f;

    private static Color fadeColor(Color color, float factor) {
        if (factor < 0.0f) {
            factor = 0.0f;
        }
        if (factor > 1.0f) {
            factor = 1.0f;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(factor * 255.0f));
    }

    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1.0f + 3.0f * stats.getDynamic().getValue("phase_time_mult");
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ShipAPI ship = (ShipAPI)stats.getEntity();
        boolean player = ship == engine.getPlayerShip();
        String actualId = id + "_" + ship.getId();
        if (player) {
            this.maintainStatus(ship, effectLevel);
        }
        if (engine.isPaused()) {
            return;
        }
        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            this.unapply(stats, actualId);
            return;
        }
        ship.setPhased(true);
        ship.setExtraAlphaMult(1.0f - 0.75f * effectLevel);
        ship.setApplyExtraAlphaToEngines(true);
        float fluxLevel = ship.getFluxTracker().getCurrFlux() / stats.getFluxCapacity().getBaseValue();
        float shipTimeMult = Math.min(Math.max(1.0f + (ExcelsiorPhaseStat_Independent.getMaxTimeMult(stats) - 1.0f) * effectLevel * Math.max(0.0f, fluxLevel), 1.0f), ExcelsiorPhaseStat_Independent.getMaxTimeMult(stats));
        stats.getTimeMult().modifyMult(actualId, shipTimeMult);
        if (player) {
            engine.getTimeMult().modifyMult(actualId, 1.0f / shipTimeMult);
        } else {
            engine.getTimeMult().unmodify(actualId);
        }
        this.syncInterval.advance(engine.getElapsedInLastFrame());
        if (this.syncInterval.intervalElapsed()) {
            ship.syncWithArmorGridState();
            ship.syncWeaponDecalsWithArmorDamage();
        }
        this.killTimer -= engine.getElapsedInLastFrame() * stats.getTimeMult().getModifiedValue();
        if (ship.getFluxTracker().getCurrFlux() < 100.0f && this.killTimer <= 0.0f && state == ShipSystemStatsScript.State.ACTIVE) {
            ship.getFluxTracker().setCurrFlux(100.0f);
            this.killTimer = 0.5f;
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
        }
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().unmodify(actualId);
            stats.getMaxTurnRate().unmodify(actualId);
        } else {
            float level = (1.0f + fluxLevel * 0.5f) * effectLevel;
            float speedPercentMod = stats.getDynamic().getMod("phase_cloak_speed").computeEffective(0.0f);
            stats.getMaxSpeed().modifyFlat(actualId, 50.0f * level);
            stats.getMaxSpeed().modifyPercent(actualId, speedPercentMod * effectLevel);
            stats.getMaxTurnRate().modifyPercent(actualId, 50.0f * level);
            stats.getAcceleration().modifyFlat(actualId, 200.0f * level);
            stats.getDeceleration().modifyFlat(actualId, 200.0f * level);
            stats.getTurnAcceleration().modifyPercent(actualId, 75.0f * level);
            if (player) {
                engine.maintainStatusForPlayerShip(this.STATUSKEY4, "graphics/icons/hullsys/phase_cloak.png", "\u76f8\u4f4d\u9a71\u52a8", "+" + (int)(50.0f * level) + " \u822a\u901f", false);
                engine.maintainStatusForPlayerShip(this.STATUSKEY5, "graphics/icons/hullsys/phase_cloak.png", "\u76f8\u4f4d\u9a71\u52a8", "+" + (int)(125.0f * level) + "% \u673a\u52a8\u6027", false);
            }
            if (ship.getFluxTracker().getCurrFlux() <= 50.0f) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
            this.afterimageInterval.advance(engine.getElapsedInLastFrame() * shipTimeMult);
            if (this.afterimageInterval.intervalElapsed()) {
                float randRange = 15.0f * level;
                float randAngle = MathUtils.getRandomNumberInRange((float)0.0f, (float)360.0f);
                float randRadiusFrac = (float)(Math.random() + Math.random());
                randRadiusFrac = randRadiusFrac > 1.0f ? 2.0f - randRadiusFrac : randRadiusFrac;
                Vector2f randLoc = MathUtils.getPointOnCircumference((Vector2f)ZERO, (float)(randRange * randRadiusFrac), (float)randAngle);
                ship.addAfterimage(GLOW_COLOR, randLoc.x, randLoc.y, 0.0f, 0.0f, randRange, 0.0f, 0.0f, 0.1f, true, false, false);
                Color afterimageColor = ExcelsiorPhaseStat_Independent.fadeColor(SPARK_COLOR, fluxLevel);
                ship.addAfterimage(SHADOW_COLOR, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.033f, 0.0f, 0.033f, false, false, false);
                ship.addAfterimage(afterimageColor, 0.0f, 0.0f, -ship.getVelocity().x, -ship.getVelocity().y, 0.0f, 0.0f, 0.0f, 0.1f * level, true, false, false);
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }
        ShipAPI ship = (ShipAPI)stats.getEntity();
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);
        ship.setPhased(false);
        ship.setExtraAlphaMult(1.0f);
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return ship.getFluxTracker().getCurrFlux() > 125.0f || ship.getPhaseCloak().isActive();
    }

    private void maintainStatus(ShipAPI playerShip, float effectLevel) {
        ShipSystemAPI cloak = playerShip.getPhaseCloak();
        if (cloak == null) {
            cloak = playerShip.getSystem();
        }
        if (cloak == null) {
            return;
        }
        if (effectLevel > 0.0f) {
            Global.getCombatEngine().maintainStatusForPlayerShip(this.STATUSKEY2, cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "\u65f6\u95f4\u6d41\u901f\u6539\u53d8", false);
        }
    }
}

