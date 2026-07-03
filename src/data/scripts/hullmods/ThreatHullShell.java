/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.ui.Alignment
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.Misc
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class ThreatHullShell
extends BaseHullMod {
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyPercent(id, 20.0f);
        stats.getHullBonus().modifyPercent(id, 15.0f);
        stats.getMaxSpeed().modifyMult(id, 0.9f);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "\u88c5\u7532 +20%";
        }
        if (index == 1) {
            return "\u7ed3\u6784 +15%";
        }
        if (index == 2) {
            return "\u6700\u9ad8\u822a\u901f -10%";
        }
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSectionHeading("\u8230\u4f53\u5207\u6362\u4fe1\u606f", Alignment.MID, 15.0f);
        Color yellow = Misc.getHighlightColor();
        tooltip.addPara("\u66f4\u6539\u4e86\u9884\u8a00\u5355\u4f4d\u7684\u9632\u5fa1\u7cfb\u7edf\u4e0e\u6218\u672f\u7cfb\u7edf\uff1a", yellow, 10.0f);
        tooltip.addPara("\u76ee\u524d\u8230\u4f53\uff1a\u62a4\u76fe\uff0c\u4e0b\u4e00\u8230\u4f53\uff1a\u76f8\u4f4d", yellow, 5.0f);
        tooltip.addPara("\u6218\u672f\u7cfb\u7edf\uff1a\u5a01\u80c1\u4e4b\u58f3", yellow, 5.0f);
        tooltip.addPara("\u9632\u5fa1\u7cfb\u7edf\uff1a\u62a4\u76fe", yellow, 5.0f);
        Color green = Misc.getPositiveHighlightColor();
        tooltip.addPara("\u589e\u76ca\uff1a", green, 10.0f);
        tooltip.setBulletedListMode(" \u2022 ");
        tooltip.addPara("\u88c5\u7532 +20%%", green, 3.0f);
        tooltip.addPara("\u7ed3\u6784 +15%%", green, 3.0f);
        tooltip.addPara("\u6700\u9ad8\u822a\u901f -10%%", green, 3.0f);
        tooltip.setBulletedListMode(null);
        Color red = Misc.getNegativeHighlightColor();
        tooltip.addPara("\u6ce8\u610f\uff1a\u5207\u6362\u4e0d\u540c\u8230\u4f53\u65f6\uff0c\u53ef\u80fd\u4f1a\u56e0\u4e3a\u9632\u5fa1\u7cfb\u7edf\u7684\u7f18\u6545\uff0c\u5bfc\u81f4\u8230\u8239\u90e8\u5206\u63d2\u4ef6\u51b2\u7a81\u800c\u6d88\u5931\uff0c\u56e0\u6b64\u5efa\u8bae\u5148\u79fb\u9664\u6602\u8d35\u63d2\u4ef6\uff08\u9700\u8981\u6d88\u8017\u7269\u54c1\u7684\uff09\u518d\u66f4\u6539\u8230\u4f53\u3002", red, 10.0f);
    }
}

