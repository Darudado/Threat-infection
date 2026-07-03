/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.combat.WeaponAPI$WeaponType
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class threatMinefield
extends BaseHullMod {
    public static String MINEFIELD_DATA_KEY = "minefield_data_key";

    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);
        CombatEngineAPI engine = Global.getCombatEngine();
        MinefieldData data = (MinefieldData)engine.getCustomData().get(MINEFIELD_DATA_KEY);
        if (data == null) {
            data = new MinefieldData();
            data.source = ship;
            engine.getCustomData().put(MINEFIELD_DATA_KEY, data);
        }
        if (data.source == ship && ship.isAlive()) {
            for (IncomingMine incomingMine : new ArrayList<IncomingMine>(data.incoming)) {
                incomingMine.delay -= amount;
                if (!(incomingMine.delay <= 0.0f)) continue;
                this.spawnMine(ship, incomingMine.mineLoc, incomingMine.target);
                data.incoming.remove(incomingMine);
            }
            data.tracker.advance(amount);
            if (data.tracker.intervalElapsed()) {
                WeightedRandomPicker picker = new WeightedRandomPicker();
                for (ShipAPI enemy : engine.getShips()) {
                    float minOk;
                    Vector2f mineLoc;
                    if (enemy == ship || enemy.isHulk() || enemy.getOwner() == ship.getOwner() || enemy.isFighter() || enemy.isDrone() || enemy.isStation() || enemy.isStationModule() || enemy.getTravelDrive() != null && enemy.getTravelDrive().isActive() || (float)Math.random() > 0.25f || !this.isAreaClear(mineLoc = Misc.getPointAtRadius((Vector2f)enemy.getLocation(), (float)(enemy.getCollisionRadius() + 400.0f + 200.0f * (float)Math.random())), minOk = 400.0f + enemy.getCollisionRadius())) continue;
                    IncomingMine inc = new IncomingMine();
                    inc.delay = (float)Math.random() * 1.5f;
                    inc.target = enemy;
                    inc.mineLoc = mineLoc;
                    picker.add((Object)inc);
                }
                int n = Math.max(1, Math.min(new Random().nextInt(6) + 0, picker.getItems().size()));
                for (int i = 0; i < n && !picker.isEmpty(); ++i) {
                    IncomingMine inc = (IncomingMine)picker.pickAndRemove();
                    data.incoming.add(inc);
                }
            }
        }
    }

    public void spawnMine(ShipAPI source, Vector2f mineLoc, ShipAPI target) {
        float mineDir = Misc.getAngleInDegrees((Vector2f)mineLoc, (Vector2f)target.getLocation());
        CombatEngineAPI engine = Global.getCombatEngine();
        Vector2f currLoc = Misc.getPointAtRadius((Vector2f)mineLoc, (float)(50.0f + (float)Math.random() * 50.0f));
        MissileAPI mine = (MissileAPI)engine.spawnProjectile(source, (WeaponAPI)null, this.getWeaponId(), currLoc, mineDir, (Vector2f)null);
        if (source != null) {
            Global.getCombatEngine().applyDamageModifiersToSpawnedProjectileWithNullWeapon(source, WeaponAPI.WeaponType.MISSILE, false, mine.getDamage());
        }
        mine.setFlightTime((float)Math.random());
        mine.fadeOutThenIn(1.0f);
        Global.getSoundPlayer().playSound("mine_spawn", 1.0f, 1.0f, mine.getLocation(), mine.getVelocity());
    }

    protected String getWeaponId() {
        return "minelayer4";
    }

    public boolean isAreaClear(Vector2f loc, float range) {
        float dist;
        CombatEngineAPI engine = Global.getCombatEngine();
        for (ShipAPI other : engine.getShips()) {
            if (other.isFighter() || other.isDrone() || !((dist = Misc.getDistance((Vector2f)loc, (Vector2f)other.getLocation())) < range)) continue;
            return false;
        }
        for (com.fs.starfarer.api.combat.CombatEntityAPI other : Global.getCombatEngine().getAsteroids()) {
            dist = Misc.getDistance((Vector2f)loc, (Vector2f)other.getLocation());
            if (!(dist < other.getCollisionRadius() + 100.0f)) continue;
            return false;
        }
        return true;
    }

    public static class MinefieldData {
        ShipAPI source;
        IntervalUtil tracker = new IntervalUtil(0.5f, 1.5f);
        List<IncomingMine> incoming = new ArrayList<IncomingMine>();
    }

    public static class IncomingMine {
        Vector2f mineLoc;
        float delay;
        ShipAPI target;
    }
}

