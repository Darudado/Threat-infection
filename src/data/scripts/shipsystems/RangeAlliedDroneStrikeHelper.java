/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.CollisionClass
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.EmpArcEntityAPI
 *  com.fs.starfarer.api.combat.FighterLaunchBayAPI
 *  com.fs.starfarer.api.combat.GuidedMissileAI
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.util.Misc
 *  org.lwjgl.util.vector.ReadableVector2f
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.CombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.util.vector.ReadableVector2f;
import org.lwjgl.util.vector.Vector2f;

public class RangeAlliedDroneStrikeHelper {
    /*
     * WARNING - void declaration
     */
    public static int convertAlliedDronesInRange(CombatEntityAPI source, Vector2f center, float radius, String missileWeaponId, int maxPerShip, int maxTotal) {
        int var11_15 = 0;
        CombatEngineAPI engine = Global.getCombatEngine();
        int owner = source.getOwner();
        ArrayList<ShipAPI> alliedShips = new ArrayList<ShipAPI>();
        for (ShipAPI ship : engine.getShips()) {
            float f;
            if (ship.getOwner() != owner || ship.isFighter() || ship.isHulk() || !((f = Misc.getDistance((Vector2f)ship.getLocation(), (Vector2f)center)) <= radius + ship.getCollisionRadius())) continue;
            alliedShips.add(ship);
        }
        HashMap<ShipAPI, WeaponAPI> fakeWeapons = new HashMap<ShipAPI, WeaponAPI>();
        for (ShipAPI shipAPI : alliedShips) {
            WeaponAPI weaponAPI = engine.createFakeWeapon(shipAPI, missileWeaponId);
            fakeWeapons.put(shipAPI, weaponAPI);
        }
        ArrayList<DroneContext> dronesToConvert = new ArrayList<DroneContext>();
        block2: for (ShipAPI shipAPI : alliedShips) {
            List<ShipAPI> drones = RangeAlliedDroneStrikeHelper.getAllDronesFromShip(shipAPI);
            int countFromThisShip = 0;
            for (ShipAPI drone : drones) {
                if (maxTotal > 0 && dronesToConvert.size() >= maxTotal) break block2;
                if (drone.isHulk() || drone.getHitpoints() <= 0.0f) continue;
                dronesToConvert.add(new DroneContext(shipAPI, drone, (WeaponAPI)fakeWeapons.get(shipAPI)));
                if (maxPerShip <= 0 || ++countFromThisShip < maxPerShip) continue;
                continue block2;
            }
        }
        boolean bl = false;
        for (DroneContext ctx : dronesToConvert) {
            boolean success = RangeAlliedDroneStrikeHelper.convertSingleDrone(ctx.mothership, ctx.drone, ctx.fakeWeapon, source);
            if (!success) continue;
            ++var11_15;
        }
        return (int)var11_15;
    }

    public static List<ShipAPI> getAllDronesFromShip(ShipAPI ship) {
        ArrayList<ShipAPI> result = new ArrayList<ShipAPI>();
        for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
            if (bay.getWing() == null) continue;
            for (ShipAPI drone : bay.getWing().getWingMembers()) {
                if (drone.isHulk() || !(drone.getHitpoints() > 0.0f)) continue;
                result.add(drone);
            }
        }
        return result;
    }

    private static boolean convertSingleDrone(ShipAPI mothership, ShipAPI drone, WeaponAPI fakeWeapon, CombatEntityAPI source) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (drone.getWing() == null) {
            return false;
        }
        MissileAPI missile = (MissileAPI)engine.spawnProjectile(mothership, fakeWeapon, fakeWeapon.getId(), new Vector2f((ReadableVector2f)drone.getLocation()), drone.getFacing(), new Vector2f((ReadableVector2f)drone.getVelocity()));
        missile.setEmpResistance(10000);
        missile.setOwner(mothership.getOwner());
        float baseRange = fakeWeapon.getRange();
        float rangeBonus = mothership.getMutableStats().getSystemRangeBonus().computeEffective(baseRange);
        missile.setMaxRange(rangeBonus);
        missile.setMaxFlightTime(missile.getMaxFlightTime() * rangeBonus / baseRange);
        ShipAPI target = Misc.findClosestShipEnemyOf((ShipAPI)mothership, (Vector2f)drone.getLocation(), (ShipAPI.HullSize)ShipAPI.HullSize.FIGHTER, (float)rangeBonus, (boolean)true);
        if (target != null && missile.getAI() instanceof GuidedMissileAI) {
            ((GuidedMissileAI)missile.getAI()).setTarget((CombatEntityAPI)target);
        }
        drone.getWing().removeMember(drone);
        drone.setWing(null);
        drone.setExplosionScale(0.67f);
        drone.setExplosionVelocityOverride(new Vector2f());
        drone.setExplosionFlashColorOverride(new Color(255, 100, 50, 255));
        engine.addLayeredRenderingPlugin((CombatLayeredRenderingPlugin)new DroneMissileBindingScript(drone, missile));
        float thickness = 26.0f;
        float coreMult = 0.67f;
        EmpArcEntityAPI arc = engine.spawnEmpArcVisual(source.getLocation(), source, missile.getLocation(), (CombatEntityAPI)missile, thickness, new Color(255, 100, 100, 255), Color.white, null);
        arc.setCoreWidthOverride(thickness * coreMult);
        arc.setSingleFlickerMode();
        return true;
    }

    private static class DroneContext {
        ShipAPI mothership;
        ShipAPI drone;
        WeaponAPI fakeWeapon;

        DroneContext(ShipAPI m, ShipAPI d, WeaponAPI w) {
            this.mothership = m;
            this.drone = d;
            this.fakeWeapon = w;
        }
    }

    public static class DroneMissileBindingScript
    extends BaseCombatLayeredRenderingPlugin {
        protected ShipAPI drone;
        protected MissileAPI missile;
        protected boolean done = false;

        public DroneMissileBindingScript(ShipAPI drone, MissileAPI missile) {
            this.drone = drone;
            this.missile = missile;
            missile.setNoFlameoutOnFizzling(true);
        }

        public void advance(float amount) {
            boolean missileExpired;
            super.advance(amount);
            if (this.done) {
                return;
            }
            CombatEngineAPI engine = Global.getCombatEngine();
            this.missile.setEccmChanceOverride(1.0f);
            this.missile.setOwner(this.drone.getOriginalOwner());
            this.drone.getLocation().set((ReadableVector2f)this.missile.getLocation());
            this.drone.getVelocity().set((ReadableVector2f)this.missile.getVelocity());
            this.drone.setCollisionClass(CollisionClass.FIGHTER);
            this.drone.setFacing(this.missile.getFacing());
            this.drone.getEngineController().fadeToOtherColor((Object)this, new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), 1.0f, 1.0f);
            float dist = Misc.getDistance((Vector2f)this.missile.getLocation(), (Vector2f)this.missile.getStart());
            float jitterFraction = dist / this.missile.getMaxRange();
            jitterFraction = Math.max(jitterFraction, this.missile.getFlightTime() / this.missile.getMaxFlightTime());
            float jitterMax = 1.0f + 10.0f * jitterFraction;
            this.drone.setJitter((Object)this, new Color(255, 100, 50, (int)(25.0f + 50.0f * jitterFraction)), 1.0f, 10, 1.0f, jitterMax);
            boolean droneDestroyed = this.drone.isHulk() || this.drone.getHitpoints() <= 0.0f;
            boolean bl = missileExpired = this.missile.isFizzling() || this.missile.getHitpoints() <= 0.0f && !this.missile.didDamage();
            if (this.missile.didDamage() || missileExpired || droneDestroyed) {
                this.drone.getVelocity().set(0.0f, 0.0f);
                this.missile.getVelocity().set(0.0f, 0.0f);
                if (!droneDestroyed) {
                    Vector2f damagePoint = Misc.getPointWithinRadius((Vector2f)this.drone.getLocation(), (float)20.0f);
                    engine.applyDamage((CombatEntityAPI)this.drone, damagePoint, 1000000.0f, DamageType.ENERGY, 0.0f, true, false, (Object)this.drone, false);
                }
                this.missile.interruptContrail();
                engine.removeEntity((CombatEntityAPI)this.drone);
                engine.removeEntity((CombatEntityAPI)this.missile);
                this.missile.explode();
                this.done = true;
            }
        }

        public boolean isExpired() {
            return this.done;
        }
    }
}

