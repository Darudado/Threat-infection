/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.CargoAPI$CargoItemType
 *  com.fs.starfarer.api.campaign.CargoStackAPI
 *  com.fs.starfarer.api.campaign.SpecialItemData
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.FighterLaunchBayAPI
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.ShipVariantAPI
 *  com.fs.starfarer.api.loading.FighterWingSpecAPI
 *  com.fs.starfarer.api.ui.Alignment
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class TheatUniversalDecksUpgrade2
extends BaseHullMod {
    private final Color HL = Global.getSettings().getColor("hColor");
    private final String ID = "universal_gantry";
    private final int EXTRA_DEPLOYMENTS = 1;
    private static final float SMOD_READINESS_REDUCTION = 0.2f;
    private static final float SMOD_MAINTENANCE_PENALTY = 50.0f;

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return String.valueOf(1);
        }
        return null;
    }

    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return Math.round(20.0f) + "%%";
        }
        if (index == 1) {
            return Math.round(50.0f) + "%%";
        }
        return null;
    }

    public boolean isSModEffectAPenalty() {
        return true;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSectionHeading("\u8be6\u7ec6\u4fe1\u606f", Alignment.MID, 15.0f);
        tooltip.addPara("\u5b89\u88c5\u9700\u8981\u6d88\u8017 1 \u4e2a \u5a01\u80c1\u5904\u7406\u5355\u5143 (threat_processing_unit)", 10.0f, this.HL, this.HL, new String[0]);
        if (ship != null && ship.getVariant() != null) {
            if (ship.getVariant().getFittedWings().isEmpty()) {
                tooltip.addPara("\u672a\u5b89\u88c5\u4efb\u4f55\u8230\u8f7d\u673a", 10.0f, this.HL, new String[0]);
            } else {
                List<String> allWings = this.getAllWings(ship.getVariant());
                if (!allWings.isEmpty()) {
                    tooltip.addPara("\u6240\u6709\u8230\u8f7d\u673a\u90fd\u5c06\u83b7\u5f97\u4ee5\u4e0b\u589e\u5f3a:", 10.0f, this.HL, new String[]{"\u6240\u6709\u8230\u8f7d\u673a"});
                    tooltip.setBulletedListMode("    - ");
                    tooltip.addPara("\u6bcf\u7ffc\u989d\u5916\u90e8\u7f72 1 \u67b6\u8230\u8f7d\u673a", 3.0f);
                    tooltip.addPara("\u90e8\u7f72\u5b8c\u6210\u540e\u5927\u5e45\u51cf\u5c11\u8865\u5145\u65f6\u95f4", 3.0f);
                    tooltip.addPara("\u63d0\u5347\u8865\u5145\u901f\u7387", 3.0f);
                    boolean sMod = this.isSMod(ship.getVariant().getStatsForOpCosts());
                    if (sMod) {
                        tooltip.addPara("S-Mod\u8d1f\u9762\u6548\u679c:", 10.0f, new Color(255, 100, 100, 255), new String[]{"\u8d1f\u9762\u6548\u679c"});
                        tooltip.addPara("\u6700\u5927\u6218\u5907\u503c\u964d\u4f4e " + Math.round(20.0f) + "%%", 3.0f, new Color(255, 150, 150, 255), new String[0]);
                        tooltip.addPara("\u7ef4\u62a4\u8d39\u589e\u52a0 " + Math.round(50.0f) + "%%", 3.0f, new Color(255, 150, 150, 255), new String[0]);
                    }
                    tooltip.addPara("\u9002\u7528\u7684\u8230\u8f7d\u673a:", 10.0f);
                    for (String w : allWings) {
                        tooltip.addPara(w, 3.0f);
                    }
                    tooltip.setBulletedListMode((String)null);
                }
            }
        }
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getOriginalOwner() != -1) {
            boolean allDeployed = true;
            boolean ranOnce = false;
            for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
                if (bay.getWing() == null) continue;
                ranOnce = true;
                FighterWingSpecAPI wingSpec = bay.getWing().getSpec();
                int deployed = bay.getWing().getWingMembers().size();
                int maxTotal = wingSpec.getNumFighters() + 1;
                int actualAdd = maxTotal - deployed;
                if (actualAdd > 0) {
                    bay.setExtraDeployments(actualAdd);
                    bay.setExtraDeploymentLimit(maxTotal);
                    bay.setExtraDuration(9999999.0f);
                    allDeployed = false;
                } else {
                    bay.setExtraDeployments(0);
                    bay.setExtraDeploymentLimit(0);
                    bay.setFastReplacements(0);
                }
                if (ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod("universal_gantry") != null || actualAdd == 0) continue;
                bay.setFastReplacements(actualAdd);
            }
            if (ship.getMutableStats().getFighterRefitTimeMult().getPercentStatMod("universal_gantry") == null && allDeployed && ranOnce) {
                ship.getMutableStats().getFighterRefitTimeMult().modifyPercent("universal_gantry", 1.0f);
            }
        }
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterRefitTimeMult().unmodify("universal_gantry");
        boolean sMod = this.isSMod(stats);
        if (sMod) {
            stats.getMaxCombatReadiness().modifyFlat(id, -0.2f, "Universal Decks S-Mod");
            stats.getSuppliesPerMonth().modifyPercent(id, 50.0f);
        }
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        Integer totalCrafts = 0;
        Integer totalExtraCrafts = 0;
        for (String w : ship.getVariant().getFittedWings()) {
            FighterWingSpecAPI wingSpec = Global.getSettings().getFighterWingSpec(w);
            totalCrafts = totalCrafts + wingSpec.getNumFighters();
            totalExtraCrafts = totalExtraCrafts + 1;
        }
        if (totalExtraCrafts > 0 && totalCrafts > 0) {
            float replacementRateMult = (float)(totalCrafts + totalExtraCrafts) / (float)totalCrafts.intValue();
            ship.getMutableStats().getDynamic().getMod("replacement_rate_decrease_mult").modifyMult(id, replacementRateMult);
        }
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship == null) {
            return false;
        }
        return ship.getMutableStats().getNumFighterBays().getModifiedValue() > 0.0f && !ship.getVariant().getHullMods().contains("diableavionics_universaldecksBI");
    }

    public String getUnapplicableReason(ShipAPI ship) {
        return ship.getVariant().getHullMods().contains("diableavionics_universaldecksBI") ? "\u4e0e\u5185\u7f6e\u7684\u8230\u8f7d\u673a\u5347\u7ea7\u51b2\u7a81" : "\u8230\u8239\u6ca1\u6709\u8230\u8f7d\u673a\u8231";
    }

    private List<String> getAllWings(ShipVariantAPI variant) {
        ArrayList<String> allWings = new ArrayList<String>();
        for (String w : variant.getFittedWings()) {
            FighterWingSpecAPI f = Global.getSettings().getFighterWingSpec(w);
            allWings.add(f.getWingName() + " " + f.getRoleDesc());
        }
        return allWings;
    }

    public CargoStackAPI getRequiredItem() {
        return Global.getSettings().createCargoStack(CargoAPI.CargoItemType.SPECIAL, (Object)new SpecialItemData("threat_processing_unit", null), (CargoAPI)null);
    }
}

