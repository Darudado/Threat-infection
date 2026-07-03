/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatFleetManagerAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipCommand
 *  com.fs.starfarer.api.combat.ShipSystemAIScript
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipwideAIFlags
 *  com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript
 *  com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
 *  com.fs.starfarer.api.util.IntervalUtil
 *  org.lwjgl.util.vector.Vector2f
 */
package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.impl.combat.threat.ConstructionSwarmSystemScript;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class ConstructionSwarmSystemAI
implements ShipSystemAIScript {
    public static float REQUIRED_DP_FOR_NORMAL_USE = 35.0f;
    protected ShipAPI ship;
    protected CombatEngineAPI engine;
    protected ShipwideAIFlags flags;
    protected ShipSystemAPI system;
    protected IntervalUtil tracker = new IntervalUtil(0.5f, 1.0f);
    protected float keepUsingFor = 0.0f;
    protected float timeSpentAtHighFragmentLevel = 0.0f;

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        this.tracker.advance(amount);
        this.keepUsingFor -= amount;
        if (this.tracker.intervalElapsed()) {
            int max;
            if (this.system.getCooldownRemaining() > 0.0f) {
                return;
            }
            if (this.system.isOutOfAmmo()) {
                return;
            }
            if (this.system.isActive()) {
                return;
            }
            ConstructionSwarmSystemScript script = (ConstructionSwarmSystemScript)this.system.getScript();
            if (!script.isUsable(this.system, this.ship)) {
                return;
            }
            CombatFleetManagerAPI manager = Global.getCombatEngine().getFleetManager(this.ship.getOriginalOwner());
            int dpLeft = 0;
            if (manager != null) {
                dpLeft = manager.getMaxStrength() - manager.getCurrStrength();
            }
            float cr = this.ship.getCurrentCR();
            float softFlux = this.ship.getFluxLevel();
            float hardFlux = this.ship.getHardFluxLevel();
            if (cr < 0.2f + hardFlux * 0.2f) {
                return;
            }
            RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)this.ship);
            if (swarm == null) {
                return;
            }
            int active = swarm.getNumActiveMembers();
            this.timeSpentAtHighFragmentLevel = (float)active >= (float)(max = swarm.getNumMembersToMaintain()) * 0.9f ? (this.timeSpentAtHighFragmentLevel += this.tracker.getIntervalDuration()) : 0.0f;
            if ((active >= max || this.timeSpentAtHighFragmentLevel >= 10.0f) && (float)dpLeft >= REQUIRED_DP_FOR_NORMAL_USE) {
                this.keepUsingFor = 3.0f + (float)Math.random() * 2.0f;
            }
            if (this.keepUsingFor <= 0.0f && (hardFlux > 0.5f || softFlux > 0.9f)) {
                this.keepUsingFor = 0.5f;
            }
            if (this.keepUsingFor > 0.0f) {
                this.ship.giveCommand(ShipCommand.USE_SYSTEM, null, 0);
            }
        }
    }
}

