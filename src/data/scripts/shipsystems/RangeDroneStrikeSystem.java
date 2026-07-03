/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.ShipSystemAPI
 *  com.fs.starfarer.api.combat.ShipSystemAPI$SystemState
 *  com.fs.starfarer.api.combat.ShipwideAIFlags$AIFlags
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.impl.combat.BaseShipSystemScript
 *  com.fs.starfarer.api.impl.combat.DroneStrikeStatsAIInfoProvider
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$State
 *  com.fs.starfarer.api.plugins.ShipSystemStatsScript$StatusData
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.impl.combat.DroneStrikeStatsAIInfoProvider;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.Misc;
import data.scripts.shipsystems.RangeAlliedDroneStrikeHelper;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public class RangeDroneStrikeSystem
extends BaseShipSystemScript
implements DroneStrikeStatsAIInfoProvider {
    protected WeaponAPI fakeWeapon;
    protected boolean fired = false;

    protected String getMissileWeaponId() {
        return "terminator_missile";
    }

    protected float getEffectRadius() {
        return 1000.0f;
    }

    protected int getMaxPerShip() {
        return -1;
    }

    protected int getMaxTotal() {
        return 30;
    }

    protected boolean requireTargetInRange() {
        return true;
    }

    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (ship == null) {
            return;
        }
        if (this.fakeWeapon == null) {
            this.fakeWeapon = Global.getCombatEngine().createFakeWeapon(ship, this.getMissileWeaponId());
        }
        if (effectLevel > 0.0f && !this.fired) {
            this.fired = true;
            Vector2f center = this.getEffectCenter(ship);
            RangeAlliedDroneStrikeHelper.convertAlliedDronesInRange((CombatEntityAPI)ship, center, this.getEffectRadius(), this.getMissileWeaponId(), this.getMaxPerShip(), this.getMaxTotal());
            Global.getSoundPlayer().playSound("system_emp_emitter_activate", 1.0f, 1.0f, ship.getLocation(), ship.getVelocity());
        }
        if (state == ShipSystemStatsScript.State.IDLE) {
            this.fired = false;
        }
    }

    protected Vector2f getEffectCenter(ShipAPI ship) {
        ShipAPI targetShip;
        boolean player;
        CombatEngineAPI engine = Global.getCombatEngine();
        boolean bl = player = ship == engine.getPlayerShip();
        if (player) {
            float dist;
            Vector2f mouseTarget = ship.getMouseTarget();
            if (mouseTarget != null && (dist = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)mouseTarget)) <= this.getEffectRadius() + ship.getCollisionRadius()) {
                return mouseTarget;
            }
            return ship.getLocation();
        }
        Object aiTarget = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
        if (aiTarget instanceof ShipAPI && (targetShip = (ShipAPI)aiTarget).isAlive()) {
            return targetShip.getLocation();
        }
        ShipAPI shipTarget = ship.getShipTarget();
        if (shipTarget != null && shipTarget.isAlive()) {
            return shipTarget.getLocation();
        }
        return ship.getLocation();
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        if (index == 0) {
            return new ShipSystemStatsScript.StatusData("\u8303\u56f4\u65e0\u4eba\u673a\u6253\u51fb\u5df2\u5c31\u7eea", false);
        }
        return null;
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target;
        if (system.isOutOfAmmo()) {
            return null;
        }
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return null;
        }
        int dronesInRange = this.countAlliedDronesInRange(ship, this.getEffectCenter(ship), this.getEffectRadius());
        if (dronesInRange == 0) {
            return "\u8303\u56f4\u5185\u6ca1\u6709\u53cb\u519b\u65e0\u4eba\u673a";
        }
        if (this.requireTargetInRange() && (target = this.findTargetInRange(ship)) == null) {
            return "\u6ca1\u6709\u6709\u6548\u76ee\u6807";
        }
        return "\u5c31\u7eea (" + dronesInRange + " \u67b6\u65e0\u4eba\u673a)";
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) {
            return true;
        }
        return this.countAlliedDronesInRange(ship, this.getEffectCenter(ship), this.getEffectRadius()) > 0;
    }

    protected int countAlliedDronesInRange(ShipAPI ship, Vector2f center, float radius) {
        int count = 0;
        CombatEngineAPI engine = Global.getCombatEngine();
        int owner = ship.getOwner();
        for (ShipAPI other : engine.getShips()) {
            float dist;
            if (other.getOwner() != owner || other.isFighter() || other.isHulk() || !((dist = Misc.getDistance((Vector2f)other.getLocation(), (Vector2f)center)) <= radius + other.getCollisionRadius())) continue;
            count += RangeAlliedDroneStrikeHelper.getAllDronesFromShip(other).size();
        }
        return count;
    }

    protected ShipAPI findTargetInRange(ShipAPI ship) {
        Vector2f center = this.getEffectCenter(ship);
        float searchRange = this.getEffectRadius() + 500.0f;
        return Misc.findClosestShipEnemyOf((ShipAPI)ship, (Vector2f)center, (ShipAPI.HullSize)ShipAPI.HullSize.FIGHTER, (float)searchRange, (boolean)true);
    }

    public float getMaxRange(ShipAPI shipAPI) {
        return 0.0f;
    }

    public boolean dronesUsefulAsPD() {
        return false;
    }

    public boolean droneStrikeUsefulVsFighters() {
        return false;
    }

    public List<ShipAPI> getDrones(ShipAPI shipAPI) {
        return List.of();
    }

    public int getMaxDrones() {
        return this.getMaxTotal();
    }

    public void setForceNextTarget(ShipAPI shipAPI) {
    }

    public float getMissileSpeed() {
        if (this.fakeWeapon == null) {
            ShipAPI dummy = Global.getCombatEngine().getPlayerShip();
            if (dummy == null) {
                return 500.0f;
            }
            this.fakeWeapon = Global.getCombatEngine().createFakeWeapon(dummy, this.getMissileWeaponId());
        }
        return this.fakeWeapon.getProjectileSpeed();
    }
}

