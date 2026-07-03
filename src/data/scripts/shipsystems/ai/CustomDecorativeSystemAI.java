/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipSystemAIScript
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipwideAIFlags
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.CustomDecorativeSystemScript;
import org.lwjgl.util.vector.Vector2f;

public class CustomDecorativeSystemAI
implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private CustomDecorativeSystemScript script;
    private IntervalUtil tracker = new IntervalUtil(0.5f, 1.0f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        this.script = (CustomDecorativeSystemScript)system.getScript();
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        this.tracker.advance(amount);
        if (this.system.isActive()) {
            this.flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_VENT);
            return;
        }
        if (!this.tracker.intervalElapsed()) {
            return;
        }
        if (this.system.getCooldownRemaining() > 0.0f) {
            return;
        }
        if (this.system.isOutOfAmmo()) {
            return;
        }
        if (this.ship.getFluxTracker().isOverloadedOrVenting()) {
            return;
        }
        if (target == null || !target.isAlive() || target.isHulk()) {
            return;
        }
        float range = Misc.getDistance((Vector2f)this.ship.getLocation(), (Vector2f)target.getLocation());
        if (range > this.script.targetWeapons.get(0).getRange()) {
            return;
        }
        Vector2f to = Misc.getUnitVectorAtDegreeAngle((float)this.ship.getFacing());
        to.scale(this.script.targetWeapons.get(0).getRange());
        Vector2f.add((Vector2f)this.ship.getLocation(), (Vector2f)to, (Vector2f)to);
        float ffDanger = Global.getSettings().getFriendlyFireDanger(this.ship, null, this.ship.getLocation(), to, Float.MAX_VALUE, 3.0f, this.script.targetWeapons.get(0).getRange());
        if (ffDanger > 0.5f) {
            return;
        }
        if (this.ship.getFluxLevel() > 0.8f) {
            return;
        }
        this.ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
    }
}

