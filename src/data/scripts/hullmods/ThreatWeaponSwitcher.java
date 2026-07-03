/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.combat.BaseHullMod
 *  com.fs.starfarer.api.combat.MutableShipStatsAPI
 *  com.fs.starfarer.api.combat.ShipAPI
 *  com.fs.starfarer.api.combat.ShipAPI$HullSize
 *  com.fs.starfarer.api.combat.ShipHullSpecAPI
 *  com.fs.starfarer.api.combat.ShipVariantAPI
 *  com.fs.starfarer.api.ui.Alignment
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 */
package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ThreatWeaponSwitcher
extends BaseHullMod {
    private static final Map<String, String> SYSTEM_TO_HULL = new HashMap<String, String>();
    private static final Set<String> ALL_HULL_IDS;
    private static final List<String> LEFT_WEAPONS;
    private static final List<String> RIGHT_WEAPONS;
    private static final String LEFT_SLOT = "WS0005";
    private static final String RIGHT_SLOT = "WS0006";
    private static final Map<String, String> HULL_TO_MARKER;
    private final Color HL = Global.getSettings().getColor("hColor");

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        ShipVariantAPI variant = stats.getVariant();
        if (variant == null) {
            return;
        }
        this.handleHullSwitch(variant);
        this.handleWeaponSwitch(variant, LEFT_SLOT, LEFT_WEAPONS);
        this.handleWeaponSwitch(variant, RIGHT_SLOT, RIGHT_WEAPONS);
    }

    private void handleHullSwitch(ShipVariantAPI variant) {
        for (String marker : HULL_TO_MARKER.values()) {
            if (!variant.hasHullMod(marker)) continue;
            return;
        }
        String currentSystemId = variant.getHullSpec().getShipSystemId();
        if (currentSystemId == null) {
            return;
        }
        String targetHullId = SYSTEM_TO_HULL.get(currentSystemId);
        if (targetHullId == null) {
            return;
        }
        ShipHullSpecAPI targetSpec = Global.getSettings().getHullSpec(targetHullId);
        if (targetSpec == null) {
            return;
        }
        variant.setHullSpecAPI(targetSpec);
        String markerMod = HULL_TO_MARKER.get(targetHullId);
        if (markerMod != null) {
            variant.addMod(markerMod);
        }
    }

    private void handleWeaponSwitch(ShipVariantAPI variant, String slotId, List<String> weaponList) {
        boolean hasMarker = false;
        for (String weaponId : weaponList) {
            if (!variant.hasHullMod(weaponId)) continue;
            hasMarker = true;
            break;
        }
        if (hasMarker) {
            return;
        }
        String currentWeapon = variant.getWeaponId(slotId);
        if (currentWeapon == null) {
            return;
        }
        int index = weaponList.indexOf(currentWeapon);
        String nextWeapon = index >= 0 ? weaponList.get((index + 1) % weaponList.size()) : weaponList.get(0);
        variant.addMod(nextWeapon);
        variant.clearSlot(slotId);
        variant.addWeapon(slotId, nextWeapon);
        variant.autoGenerateWeaponGroups();
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {
            return "\u6839\u636e\u7cfb\u7edf\u81ea\u52a8\u5207\u6362\u8239\u4f53\uff1aThreatshell \u2192 wxxzj\uff0cPhasedDriftStats \u2192 wxxzj_xw";
        }
        if (index == 1) {
            return "\u5de6\u6b66\u5668\u5faa\u73af\uff1awxldp \u2192 wxbq \u2192 wxjg";
        }
        if (index == 2) {
            return "\u53f3\u6b66\u5668\u5faa\u73af\uff1awxldp1 \u2192 wxbq1 \u2192 wxjg1";
        }
        if (index == 3) {
            return "\u5728\u88c5\u914d\u754c\u9762\u79fb\u9664\u6807\u8bb0\u63d2\u4ef6\u53ef\u91cd\u65b0\u89e6\u53d1\u5207\u6362\u3002";
        }
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        tooltip.addSectionHeading("\u5207\u6362\u673a\u5236", Alignment.MID, 15.0f);
        tooltip.addPara("\u2022 \u82e5\u672a\u5b89\u88c5\u4efb\u4f55\u6784\u578b\u6807\u8bb0\u63d2\u4ef6\uff0c\u5219\u6839\u636e\u5f53\u524d\u7cfb\u7edfID\u81ea\u52a8\u66f4\u6362\u8239\u4f53\uff08\u6218\u672f\u7cfb\u7edf\u968f\u4e4b\u6539\u53d8\uff09\uff0c\u5e76\u6dfb\u52a0\u5bf9\u5e94\u6807\u8bb0\u63d2\u4ef6\u3002\n\u2022 \u82e5\u5de6\u53f3\u69fd\u6ca1\u6709\u5bf9\u5e94\u7684\u6b66\u5668\u63d2\u4ef6\uff0c\u5219\u5c06\u6b66\u5668\u5faa\u73af\u5207\u6362\u5230\u4e0b\u4e00\u4e2a\uff0c\u5e76\u6dfb\u52a0\u8be5\u6b66\u5668ID\u4f5c\u4e3a\u6807\u8bb0\u3002\n\u2022 \u79fb\u9664\u6807\u8bb0\u63d2\u4ef6\u540e\u91cd\u65b0\u90e8\u7f72\u5373\u53ef\u518d\u6b21\u5207\u6362\u3002", 10.0f, this.HL, new String[0]);
    }

    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    static {
        SYSTEM_TO_HULL.put("Threatshell", "wxxzj_xw");
        SYSTEM_TO_HULL.put("PhasedDriftStats", "wxxzj");
        ALL_HULL_IDS = new HashSet<String>(SYSTEM_TO_HULL.values());
        LEFT_WEAPONS = Arrays.asList("wxldp", "wxbq", "wxjg");
        RIGHT_WEAPONS = Arrays.asList("wxldp1", "wxbq1", "wxjg1");
        HULL_TO_MARKER = new HashMap<String, String>();
        HULL_TO_MARKER.put("wxxzj_xw", "threat_hull_phase");
        HULL_TO_MARKER.put("wxxzj", "threat_hull_shell");
    }
}

