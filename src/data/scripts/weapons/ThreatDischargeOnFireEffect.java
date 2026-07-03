/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.CollisionClass
 *  com.fs.starfarer.api.combat.CombatEngineAPI
 *  com.fs.starfarer.api.combat.CombatEntityAPI
 *  com.fs.starfarer.api.combat.DamageType
 *  com.fs.starfarer.api.combat.DamagingProjectileAPI
 *  com.fs.starfarer.api.combat.EmpArcEntityAPI
 *  com.fs.starfarer.api.combat.EmpArcEntityAPI$EmpArcParams
 *  com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
 *  com.fs.starfarer.api.combat.MissileAPI
 *  com.fs.starfarer.api.combat.OnFireEffectPlugin
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.WeaponAPI
 *  com.fs.starfarer.api.combat.WeaponAPI$AIHints
 *  com.fs.starfarer.api.impl.combat.threat.AttackSwarmPhaseModeScript
 *  com.fs.starfarer.api.impl.combat.threat.FragmentWeapon
 *  com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect
 *  com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect$SwarmMember
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 *  org.lwjgl.util.vector.Vector2f
 */
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.threat.AttackSwarmPhaseModeScript;
import com.fs.starfarer.api.impl.combat.threat.FragmentWeapon;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.Iterator;
import org.lwjgl.util.vector.Vector2f;

public class ThreatDischargeOnFireEffect
implements OnFireEffectPlugin,
EveryFrameWeaponEffectPlugin,
FragmentWeapon {
    public static String SWARM_TAG_PHASE_MODE = "swarm_tag_phase_mode";
    public static Color EMP_FRINGE_COLOR_BRIGHT = new Color(213, 255, 237, 255);
    public static Color EMP_FRINGE_COLOR = new Color(130, 155, 145, 255);
    public static Color PHASE_FRINGE_COLOR = new Color(120, 110, 185, 255);
    public static Color PHASE_CORE_COLOR = new Color(255, 255, 255, 127);
    public static float EXTRA_ARC = 360.0f;
    public static int FRAGMENTS_TO_FIRE = 10;
    public static String[] HIT_TEXTS = new String[]{"\u6211\u4e0d\u4f1a\u544a\u8bc9\u4f60\u4eec\u4efb\u4f55\u4e8b", "\u554a\u554a\u554a\u554a\u554a\u554a\u554a\u554a\u554a", "\u54e6\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41\u9f41", "19!19!19~~~~~~~"};
    public static Color TEXT_COLOR = new Color(255, 255, 200, 255);
    public static float TEXT_CHANCE = 0.5f;
    public static float TEXT_SIZE = 25.0f;

    public static boolean isSwarmPhaseMode(ShipAPI ship) {
        RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)ship);
        return swarm != null && swarm.getParams().tags.contains(SWARM_TAG_PHASE_MODE);
    }

    public static void setSwarmPhaseMode(ShipAPI ship) {
        new AttackSwarmPhaseModeScript(ship);
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (ship != null) {
            RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)ship);
            int active = swarm == null ? 0 : swarm.getNumActiveMembers();
            int required = this.getNumFragmentsToFire();
            boolean disable = active < required;
            weapon.setForceDisabled(disable);
        }
    }

    public int getNumFragmentsToFire() {
        return FRAGMENTS_TO_FIRE;
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float emp = projectile.getEmpAmount();
        float dam = projectile.getDamageAmount();
        RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor((CombatEntityAPI)projectile.getSource());
        if (swarm != null && swarm.getAttachedTo() != null) {
            RoilingSwarmEffect.SwarmMember pick;
            CombatEntityAPI target = this.findTarget(projectile, weapon, engine);
            Vector2f noTargetDest = null;
            if (target == null) {
                noTargetDest = this.pickNoTargetDest(projectile, weapon, engine);
            }
            Vector2f towards = noTargetDest;
            if (target != null) {
                towards = target.getLocation();
            }
            if ((pick = ThreatDischargeOnFireEffect.pickFragmentTowardsPointWithinRange(swarm, towards, 150.0f)) != null) {
                pick.setRecentlyPicked(1.0f);
                float thickness = 30.0f;
                Color color = EMP_FRINGE_COLOR;
                Color coreColor = Color.white;
                boolean phaseMode = ThreatDischargeOnFireEffect.isSwarmPhaseMode(projectile.getSource());
                if (phaseMode) {
                    color = PHASE_FRINGE_COLOR;
                    if (target instanceof ShipAPI && ((ShipAPI)target).isPhased()) {
                        coreColor = PHASE_CORE_COLOR;
                    }
                }
                float coreWidthMult = 0.75f;
                EmpArcEntityAPI.EmpArcParams params = new EmpArcEntityAPI.EmpArcParams();
                params.segmentLengthMult = 8.0f;
                params.zigZagReductionFactor = 0.5f;
                params.flickerRateMult = 1.0f;
                params.fadeOutDist = 1000.0f;
                params.minFadeOutMult = 1.0f;
                params.glowSizeMult = 0.5f;
                params.glowAlphaMult = 0.75f;
                pick.flash();
                pick.flash.forceIn();
                pick.flash.setDurationOut(0.25f);
                Vector2f hitLocation = null;
                if (target != null) {
                    float targetRadius = target.getCollisionRadius();
                    hitLocation = Misc.getPointWithinRadius((Vector2f)target.getLocation(), (float)(targetRadius * 0.8f));
                }
                if (target != null && (float)Math.random() < TEXT_CHANCE && hitLocation != null) {
                    String hitText = HIT_TEXTS[(int)(Math.random() * (double)HIT_TEXTS.length)];
                    engine.addFloatingText(hitLocation, hitText, TEXT_SIZE, TEXT_COLOR, target, 0.5f, 0.8f);
                }
                if (target != null) {
                    EmpArcEntityAPI arc = engine.spawnEmpArc(projectile.getSource(), pick.loc, (CombatEntityAPI)weapon.getShip(), target, DamageType.ENERGY, dam, emp, 100000.0f, "voltaic_discharge_emp_impact", thickness, color, coreColor, params);
                    arc.setCoreWidthOverride(thickness * coreWidthMult);
                    arc.setSingleFlickerMode();
                    arc.setUpdateFromOffsetEveryFrame(true);
                    arc.setRenderGlowAtStart(false);
                    arc.setFadedOutAtStart(true);
                } else {
                    params.flickerRateMult = 1.0f;
                    EmpArcEntityAPI arc = engine.spawnEmpArcVisual(pick.loc, (CombatEntityAPI)weapon.getShip(), noTargetDest, (CombatEntityAPI)weapon.getShip(), thickness, color, coreColor, params);
                    arc.setCoreWidthOverride(thickness * coreWidthMult);
                    arc.setSingleFlickerMode();
                    arc.setUpdateFromOffsetEveryFrame(true);
                    arc.setRenderGlowAtStart(false);
                    arc.setFadedOutAtStart(true);
                }
            }
        }
    }

    public Vector2f pickNoTargetDest(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float spread = 50.0f;
        float range = Math.min(weapon.getRange() - spread, 150.0f);
        Vector2f from = projectile.getLocation();
        Vector2f dir = Misc.getUnitVectorAtDegreeAngle((float)(weapon.getCurrAngle() + (EXTRA_ARC / 2.0f - EXTRA_ARC * (float)Math.random())));
        dir.scale(range);
        Vector2f.add((Vector2f)from, (Vector2f)dir, (Vector2f)dir);
        dir = Misc.getPointWithinRadius((Vector2f)dir, (float)spread);
        return dir;
    }

    public CombatEntityAPI findTarget(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float range = weapon.getRange() + 50.0f;
        Vector2f from = projectile.getLocation();
        Iterator iter = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(from, range * 2.0f, range * 2.0f);
        int owner = weapon.getShip().getOwner();
        CombatEntityAPI best = null;
        float minScore = Float.MAX_VALUE;
        ShipAPI ship = weapon.getShip();
        boolean ignoreFlares = ship != null && ship.getMutableStats().getDynamic().getValue("pd_ignores_flares", 0.0f) >= 1.0f;
        ignoreFlares |= weapon.hasAIHint(WeaponAPI.AIHints.IGNORES_FLARES);
        boolean phaseMode = ThreatDischargeOnFireEffect.isSwarmPhaseMode(ship);
        while (iter.hasNext()) {
            MissileAPI missile;
            CombatEntityAPI other;
            Object o = iter.next();
            if (!(o instanceof MissileAPI) && !(o instanceof ShipAPI) || (other = (CombatEntityAPI)o).getOwner() == owner) continue;
            boolean phaseHit = false;
            if (other instanceof ShipAPI) {
                ShipAPI otherShip = (ShipAPI)other;
                if (otherShip.isHulk()) continue;
                if (otherShip.isPhased()) {
                    if (!phaseMode) continue;
                    phaseHit = true;
                }
                if (!otherShip.isTargetable()) continue;
            }
            if (!phaseHit && other.getCollisionClass() == CollisionClass.NONE || ignoreFlares && other instanceof MissileAPI && (missile = (MissileAPI)other).isFlare()) continue;
            float radius = Misc.getTargetingRadius((Vector2f)from, (CombatEntityAPI)other, (boolean)false);
            float dist = Misc.getDistance((Vector2f)from, (Vector2f)other.getLocation()) - radius;
            if (dist > range || !Misc.isInArc((float)weapon.getCurrAngle(), (float)EXTRA_ARC, (Vector2f)from, (Vector2f)other.getLocation()) || !(dist < minScore)) continue;
            minScore = dist;
            best = other;
        }
        return best;
    }

    public static RoilingSwarmEffect.SwarmMember pickFragmentTowardsPointWithinRange(RoilingSwarmEffect swarm, Vector2f towards, float maxRange) {
        WeightedRandomPicker picker = swarm.getPicker(true, true, towards);
        while (!picker.isEmpty()) {
            RoilingSwarmEffect.SwarmMember p = (RoilingSwarmEffect.SwarmMember)picker.pickAndRemove();
            float dist = Misc.getDistance((Vector2f)p.loc, (Vector2f)swarm.getAttachedTo().getLocation());
            if (dist > maxRange) continue;
            return p;
        }
        return null;
    }
}

