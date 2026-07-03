/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.EveryFrameScript
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CargoAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.combat.ShipHullSpecAPI
 *  com.fs.starfarer.api.combat.ShipVariantAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 */
package data.scripts.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.io.PrintStream;
import java.util.Random;

public class HXMO_Upgrade_EFS
implements EveryFrameScript {
    public static final String KEY_HASSHIP = "$HXMO_upgrade_hasship";
    public static final String KEY_UPGRADE = "$HXMO_upgrade_level";
    public static final String KEY_CANUPGRADE = "$HXMO_upgrade_canupgrade";
    public static final String KEY_NEEDUPDATE = "$HXMO_upgrade_needupdate";
    public static final String KEY_FIX = "$HXMO_upgrade_fix";
    public static final String KEY_CANFIX = "$HXMO_upgrade_canfix";
    public static final String KEY_WEAPON_UPGRADE_2 = "$HXMO_upgrade_weapon_level2";
    public static final String KEY_CAN_WEAPON_UPGRADE_2 = "$HXMO_upgrade_weapon_canupgrade2";
    public static final String KEY_CARRIER_UPGRADE = "$HXMO_upgrade_carrier_level";
    public static final String KEY_CAN_CARRIER_UPGRADE = "$HXMO_upgrade_carrier_canupgrade";
    public static final String KEY_CARRIER_UPGRADE_2 = "$HXMO_upgrade_carrier_level2";
    public static final String KEY_CAN_CARRIER_UPGRADE_2 = "$HXMO_upgrade_carrier_canupgrade2";
    public static final String KEY_ARMOR_UPGRADE = "$HXMO_upgrade_armor_level";
    public static final String KEY_CAN_ARMOR_UPGRADE = "$HXMO_upgrade_armor_canupgrade";
    public static final String MODULE_SLOT_LEFT = "WS0003";
    public static final String MODULE_SLOT_RIGHT = "WS0004";
    public static final String HULL_ID_LEFT_OLD = "ZSMO555";
    public static final String HULL_ID_LEFT_NEW = "ZSMO554";
    public static final String HULL_ID_LEFT_LEVEL2 = "ZSMO553";
    public static final String HULL_ID_RIGHT_OLD = "ZXMO555";
    public static final String HULL_ID_RIGHT_NEW = "ZXMO554";
    public static final String HULL_ID_RIGHT_LEVEL2 = "ZXMO553";
    public static final String MODULE_SLOT_CARRIER_LEFT = "WS0005";
    public static final String MODULE_SLOT_CARRIER_RIGHT = "WS0006";
    public static final String HULL_ID_CARRIER_LEFT_OLD = "SHMMO555";
    public static final String HULL_ID_CARRIER_LEFT_NEW = "SHMMO554";
    public static final String HULL_ID_CARRIER_LEFT_LEVEL2 = "SHMMO553";
    public static final String HULL_ID_CARRIER_RIGHT_OLD = "XHMMO555";
    public static final String HULL_ID_CARRIER_RIGHT_NEW = "XHMMO554";
    public static final String HULL_ID_CARRIER_RIGHT_LEVEL2 = "XHMMO553";
    public static final String MODULE_SLOT_ARMOR_LEFT = "WS0008";
    public static final String MODULE_SLOT_ARMOR_RIGHT = "WS0009";
    public static final String HULL_ID_ARMOR_LEFT_OLD = "ZZJMO555";
    public static final String HULL_ID_ARMOR_LEFT_NEW = "ZZJMO";
    public static final String HULL_ID_ARMOR_RIGHT_OLD = "YZJMO555";
    public static final String HULL_ID_ARMOR_RIGHT_NEW = "YZJMO";
    public static final float CREDITS_COST_UPGRADE = 500000.0f;
    public static final int STORY_POINTS_COST_UPGRADE = 2;
    public static final float CREDITS_COST_WEAPON_LEVEL2 = 600000.0f;
    public static final int STORY_POINTS_COST_WEAPON_LEVEL2 = 3;
    public static final float CREDITS_COST_CARRIER_UPGRADE = 400000.0f;
    public static final int STORY_POINTS_COST_CARRIER_UPGRADE = 3;
    public static final float CREDITS_COST_CARRIER_LEVEL2 = 500000.0f;
    public static final int STORY_POINTS_COST_CARRIER_LEVEL2 = 4;
    public static final float CREDITS_COST_ARMOR_UPGRADE = 300000.0f;
    public static final int STORY_POINTS_COST_ARMOR_UPGRADE = 1;
    public static final float CREDITS_COST_FIX = 200000.0f;
    protected String hxmoId;
    protected int weaponUpgradeLevel = 0;
    protected int carrierUpgradeLevel = 0;
    protected int armorUpgradeLevel = 0;
    protected long seed;
    protected boolean weaponUpgradeInProgress = false;
    protected boolean carrierUpgradeInProgress = false;
    protected boolean armorUpgradeInProgress = false;
    protected boolean fixInProgress = false;

    public HXMO_Upgrade_EFS(String id) {
        this.hxmoId = id;
        this.seed = (long)(Math.random() * 1000000.0 + 4455.0);
        this.init();
    }

    private boolean needUpdate() {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_NEEDUPDATE)) {
            System.out.println("HXMO: \u6ca1\u6709\u68c0\u6d4b\u5230\u66f4\u65b0\u6807\u8bb0");
            return false;
        }
        boolean needsUpdate = Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_NEEDUPDATE);
        System.out.println("HXMO: \u68c0\u67e5\u66f4\u65b0\u6807\u8bb0 - " + needsUpdate);
        return needsUpdate;
    }

    private boolean checkForFix() {
        if (this.fixInProgress) {
            System.out.println("HXMO: \u4fee\u590d\u6b63\u5728\u8fdb\u884c\u4e2d");
            return false;
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_FIX)) {
            System.out.println("HXMO: \u6ca1\u6709\u4fee\u590d\u6807\u8bb0");
            return false;
        }
        boolean shouldFix = Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_FIX);
        System.out.println("HXMO: \u68c0\u67e5\u4fee\u590d\u6807\u8bb0 - " + shouldFix);
        if (shouldFix) {
            this.fixInProgress = true;
            return true;
        }
        return false;
    }

    private boolean checkForWeaponUpgrade() {
        if (this.weaponUpgradeInProgress) {
            System.out.println("HXMO: \u706b\u529b\u5347\u7ea7\u6b63\u5728\u8fdb\u884c\u4e2d\uff0c\u8df3\u8fc7");
            return false;
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_UPGRADE)) {
            System.out.println("HXMO: \u6ca1\u6709\u706b\u529b\u5347\u7ea7\u76ee\u6807\u6807\u8bb0");
            return false;
        }
        int targetLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_UPGRADE);
        System.out.println("HXMO: \u706b\u529b\u5347\u7ea7\u68c0\u67e5 - \u5f53\u524d\u7b49\u7ea7=" + this.weaponUpgradeLevel + ", \u76ee\u6807\u7b49\u7ea7=" + targetLevel);
        if (targetLevel >= 0 && targetLevel <= 2) {
            if (this.weaponUpgradeLevel >= targetLevel) {
                System.out.println("HXMO: \u5f53\u524d\u7b49\u7ea7\u5df2\u6ee1\u8db3\u76ee\u6807\u7b49\u7ea7\uff0c\u65e0\u9700\u5347\u7ea7");
                return false;
            }
            if (targetLevel == 1) {
                if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CANUPGRADE)) {
                    System.out.println("HXMO: \u4e0d\u6ee1\u8db31\u7ea7\u5347\u7ea7\u6761\u4ef6");
                    return false;
                }
            } else if (targetLevel == 2 && !Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CAN_WEAPON_UPGRADE_2)) {
                System.out.println("HXMO: \u4e0d\u6ee1\u8db32\u7ea7\u5347\u7ea7\u6761\u4ef6");
                return false;
            }
            System.out.println("HXMO: \u6ee1\u8db3\u5347\u7ea7\u6761\u4ef6\uff0c\u5f00\u59cb\u706b\u529b\u5347\u7ea7\u5904\u7406");
            this.weaponUpgradeInProgress = true;
            return true;
        }
        System.out.println("HXMO: \u65e0\u6548\u7684\u76ee\u6807\u7b49\u7ea7 " + targetLevel);
        return false;
    }

    private boolean checkForCarrierUpgrade() {
        if (this.carrierUpgradeInProgress) {
            System.out.println("HXMO: \u822a\u6bcd\u5347\u7ea7\u6b63\u5728\u8fdb\u884c\u4e2d");
            return false;
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_CARRIER_UPGRADE)) {
            System.out.println("HXMO: \u6ca1\u6709\u822a\u6bcd\u5347\u7ea7\u76ee\u6807");
            return false;
        }
        int targetLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_CARRIER_UPGRADE);
        System.out.println("HXMO: \u822a\u6bcd\u5347\u7ea7\u68c0\u67e5 - \u5f53\u524d\u7b49\u7ea7=" + this.carrierUpgradeLevel + ", \u76ee\u6807\u7b49\u7ea7=" + targetLevel);
        if (targetLevel > this.carrierUpgradeLevel) {
            this.carrierUpgradeInProgress = true;
            System.out.println("HXMO: \u5f00\u59cb\u822a\u6bcd\u5347\u7ea7\u5904\u7406");
            return true;
        }
        System.out.println("HXMO: \u65e0\u9700\u822a\u6bcd\u5347\u7ea7");
        return false;
    }

    private boolean checkForArmorUpgrade() {
        if (this.armorUpgradeInProgress) {
            System.out.println("HXMO: \u88c5\u7532\u5347\u7ea7\u6b63\u5728\u8fdb\u884c\u4e2d");
            return false;
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_ARMOR_UPGRADE)) {
            System.out.println("HXMO: \u6ca1\u6709\u88c5\u7532\u5347\u7ea7\u76ee\u6807");
            return false;
        }
        int targetLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_ARMOR_UPGRADE);
        System.out.println("HXMO: \u88c5\u7532\u5347\u7ea7\u68c0\u67e5 - \u5f53\u524d\u7b49\u7ea7=" + this.armorUpgradeLevel + ", \u76ee\u6807\u7b49\u7ea7=" + targetLevel);
        if (targetLevel > this.armorUpgradeLevel) {
            this.armorUpgradeInProgress = true;
            System.out.println("HXMO: \u5f00\u59cb\u88c5\u7532\u5347\u7ea7\u5904\u7406");
            return true;
        }
        System.out.println("HXMO: \u65e0\u9700\u88c5\u7532\u5347\u7ea7");
        return false;
    }

    private void recordCanUpgradeOrNot() {
        FleetMemberAPI member = this.getHXMO();
        if (member == null) {
            System.out.println("HXMO: \u8230\u8239\u4e0d\u5b58\u5728\uff0c\u91cd\u7f6e\u6240\u6709\u5347\u7ea7\u6761\u4ef6\u4e3afalse");
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CANUPGRADE, (Object)false);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_WEAPON_UPGRADE_2, (Object)false);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_CARRIER_UPGRADE, (Object)false);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_CARRIER_UPGRADE_2, (Object)false);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_ARMOR_UPGRADE, (Object)false);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CANFIX, (Object)false);
        } else {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            float credits = cargo.getCredits().get();
            int storyPoints = Global.getSector().getPlayerStats().getStoryPoints();
            System.out.println("=== HXMO: \u5f00\u59cb\u68c0\u67e5\u5347\u7ea7\u6761\u4ef6 ===");
            System.out.println("HXMO: \u5f53\u524d\u8d44\u6e90 - \u661f\u5e01=" + credits + ", \u6545\u4e8b\u70b9=" + storyPoints);
            System.out.println("HXMO: \u5f53\u524d\u7b49\u7ea7 - \u706b\u529b=" + this.weaponUpgradeLevel + ", \u822a\u6bcd=" + this.carrierUpgradeLevel + ", \u88c5\u7532=" + this.armorUpgradeLevel);
            boolean canWeaponUpgrade1 = false;
            boolean canWeaponUpgrade2 = false;
            if (this.weaponUpgradeLevel == 0) {
                boolean hasCredits1 = credits >= 500000.0f;
                boolean hasStoryPoints1 = storyPoints >= 2;
                canWeaponUpgrade1 = hasCredits1 && hasStoryPoints1;
                System.out.println("HXMO: \u706b\u529b1\u7ea7\u5347\u7ea7\u68c0\u67e5");
                System.out.println("  \u9700\u8981: 500000.0\u661f\u5e01 + 2\u6545\u4e8b\u70b9");
                System.out.println("  \u5f53\u524d: " + credits + "\u661f\u5e01 + " + storyPoints + "\u6545\u4e8b\u70b9");
                System.out.println("  \u661f\u5e01\u8db3\u591f: " + hasCredits1);
                System.out.println("  \u6545\u4e8b\u70b9\u8db3\u591f: " + hasStoryPoints1);
                System.out.println("  \u53ef\u5347\u7ea7: " + canWeaponUpgrade1);
            } else if (this.weaponUpgradeLevel == 1) {
                boolean hasCredits2 = credits >= 600000.0f;
                boolean hasStoryPoints2 = storyPoints >= 3;
                canWeaponUpgrade2 = hasCredits2 && hasStoryPoints2;
                System.out.println("HXMO: \u706b\u529b2\u7ea7\u5347\u7ea7\u68c0\u67e5");
                System.out.println("  \u9700\u8981: 600000.0\u661f\u5e01 + 3\u6545\u4e8b\u70b9");
                System.out.println("  \u5f53\u524d: " + credits + "\u661f\u5e01 + " + storyPoints + "\u6545\u4e8b\u70b9");
                System.out.println("  \u661f\u5e01\u8db3\u591f: " + hasCredits2);
                System.out.println("  \u6545\u4e8b\u70b9\u8db3\u591f: " + hasStoryPoints2);
                System.out.println("  \u53ef\u5347\u7ea7: " + canWeaponUpgrade2);
            } else if (this.weaponUpgradeLevel == 2) {
                System.out.println("HXMO: \u706b\u529b\u5df2\u6ee1\u7ea7(\u7b49\u7ea72)\uff0c\u65e0\u9700\u5347\u7ea7");
            }
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CANUPGRADE, (Object)canWeaponUpgrade1);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_WEAPON_UPGRADE_2, (Object)canWeaponUpgrade2);
            System.out.println("HXMO: \u8bbe\u7f6e\u5168\u5c40\u53d8\u91cf - \u706b\u529b1\u7ea7\u6761\u4ef6=" + canWeaponUpgrade1 + ", \u706b\u529b2\u7ea7\u6761\u4ef6=" + canWeaponUpgrade2);
            boolean canCarrierUpgrade1 = false;
            boolean canCarrierUpgrade2 = false;
            if (this.carrierUpgradeLevel == 0) {
                boolean hasCreditsCarrier1 = credits >= 400000.0f;
                boolean hasStoryPointsCarrier1 = storyPoints >= 3;
                canCarrierUpgrade1 = hasCreditsCarrier1 && hasStoryPointsCarrier1;
            } else if (this.carrierUpgradeLevel == 1) {
                boolean hasCreditsCarrier2 = credits >= 500000.0f;
                boolean hasStoryPointsCarrier2 = storyPoints >= 4;
                canCarrierUpgrade2 = hasCreditsCarrier2 && hasStoryPointsCarrier2;
            }
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_CARRIER_UPGRADE, (Object)canCarrierUpgrade1);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_CARRIER_UPGRADE_2, (Object)canCarrierUpgrade2);
            boolean canArmorUpgrade = false;
            if (this.armorUpgradeLevel == 0) {
                boolean hasCreditsArmor = credits >= 300000.0f;
                boolean hasStoryPointsArmor = storyPoints >= 1;
                canArmorUpgrade = hasCreditsArmor && hasStoryPointsArmor;
            }
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CAN_ARMOR_UPGRADE, (Object)canArmorUpgrade);
            boolean hasDMods = member.getVariant().hasDMods();
            boolean hasCreditsFix = credits >= 200000.0f;
            boolean canFix = hasDMods && hasCreditsFix;
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CANFIX, (Object)canFix);
            System.out.println("=== HXMO: \u6700\u7ec8\u5347\u7ea7\u6761\u4ef6 ===");
            System.out.println("  \u706b\u529b1\u7ea7: " + canWeaponUpgrade1);
            System.out.println("  \u706b\u529b2\u7ea7: " + canWeaponUpgrade2);
            System.out.println("  \u822a\u6bcd1\u7ea7: " + canCarrierUpgrade1);
            System.out.println("  \u822a\u6bcd2\u7ea7: " + canCarrierUpgrade2);
            System.out.println("  \u88c5\u7532: " + canArmorUpgrade);
            System.out.println("  \u4fee\u590d: " + canFix);
            System.out.println("=== HXMO: \u5168\u5c40\u53d8\u91cf\u9a8c\u8bc1 ===");
            PrintStream var10000 = System.out;
            MemoryAPI var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_canupgrade: " + var10001.getBoolean(KEY_CANUPGRADE));
            var10000 = System.out;
            var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_weapon_canupgrade2: " + var10001.getBoolean(KEY_CAN_WEAPON_UPGRADE_2));
            var10000 = System.out;
            var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_carrier_canupgrade: " + var10001.getBoolean(KEY_CAN_CARRIER_UPGRADE));
            var10000 = System.out;
            var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_carrier_canupgrade2: " + var10001.getBoolean(KEY_CAN_CARRIER_UPGRADE_2));
            var10000 = System.out;
            var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_armor_canupgrade: " + var10001.getBoolean(KEY_CAN_ARMOR_UPGRADE));
            var10000 = System.out;
            var10001 = Global.getSector().getMemoryWithoutUpdate();
            var10000.println("  $HXMO_upgrade_canfix: " + var10001.getBoolean(KEY_CANFIX));
        }
    }

    private void completeUpdate() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)false);
        if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_FIX)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_FIX, (Object)false);
        }
        System.out.println("HXMO: \u66f4\u65b0\u6807\u8bb0\u5df2\u6e05\u9664");
    }

    private void executeFix() {
        System.out.println("HXMO: \u5f00\u59cb\u6267\u884c\u4fee\u590d...");
        FleetMemberAPI member = this.getHXMO();
        if (member == null) {
            System.out.println("HXMO: \u4fee\u590d\u5931\u8d25 - \u672a\u627e\u5230\u8230\u8239");
            this.fixInProgress = false;
        } else {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            if (!member.getVariant().hasDMods()) {
                System.out.println("HXMO: \u4fee\u590d\u5931\u8d25 - \u6ca1\u6709D\u63d2\u4ef6");
                this.fixInProgress = false;
            } else if (cargo.getCredits().get() < 200000.0f) {
                System.out.println("HXMO: \u4fee\u590d\u5931\u8d25 - \u661f\u5e01\u4e0d\u8db3");
                this.fixInProgress = false;
            } else {
                cargo.getCredits().subtract(200000.0f);
                System.out.println("HXMO\u4fee\u590d\uff1a\u6263\u9664200000.0\u661f\u5e01\uff0c\u5269\u4f59\uff1a" + cargo.getCredits().get());
                WeightedRandomPicker dmods = new WeightedRandomPicker(new Random(this.seed));
                for (String modId : member.getVariant().getPermaMods()) {
                    if (!Global.getSettings().getHullModSpec(modId).hasTag("dmod")) continue;
                    dmods.add((Object)modId);
                }
                if (!dmods.isEmpty()) {
                    String removed = (String)dmods.pick();
                    member.getVariant().removePermaMod(removed);
                    System.out.println("HXMO\u4fee\u590d\uff1a\u5df2\u79fb\u9664D-mod: " + removed);
                    member.setVariant(member.getVariant(), false, true);
                }
                this.fixInProgress = false;
                System.out.println("HXMO: \u4fee\u590d\u5b8c\u6210");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void executeWeaponUpgrade() {
        System.out.println("=== HXMO: \u5f00\u59cb\u6267\u884c\u706b\u529b\u5347\u7ea7 ===");
        FleetMemberAPI member = this.getHXMO();
        if (member == null) {
            System.out.println("HXMO: \u9519\u8bef - \u672a\u627e\u5230HXMO\u8230\u8239");
            this.weaponUpgradeInProgress = false;
        } else {
            ShipVariantAPI variant = member.getVariant();
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            int sp = Global.getSector().getPlayerStats().getStoryPoints();
            PrintStream var10000 = System.out;
            float var10001 = cargo.getCredits().get();
            var10000.println("HXMO: \u5347\u7ea7\u524d\u8d44\u6e90 - \u661f\u5e01=" + var10001 + ", \u6545\u4e8b\u70b9=" + sp);
            int targetLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_UPGRADE);
            float creditsCost = 0.0f;
            int storyPointsCost = 0;
            String leftHullId = "";
            String rightHullId = "";
            System.out.println("HXMO: \u76ee\u6807\u5347\u7ea7\u7b49\u7ea7 = " + targetLevel);
            if (targetLevel == 1) {
                creditsCost = 500000.0f;
                storyPointsCost = 2;
                leftHullId = HULL_ID_LEFT_NEW;
                rightHullId = HULL_ID_RIGHT_NEW;
                System.out.println("HXMO: \u6267\u884c1\u7ea7\u706b\u529b\u5347\u7ea7");
                System.out.println("  \u6240\u9700\u8d44\u6e90: " + creditsCost + "\u661f\u5e01 + " + storyPointsCost + "\u6545\u4e8b\u70b9");
                System.out.println("  \u5347\u7ea7\u8def\u5f84: ZSMO555/ZXMO555 -> ZSMO554/ZXMO554");
            } else {
                if (targetLevel != 2) {
                    System.out.println("HXMO: \u9519\u8bef - \u65e0\u6548\u7684\u76ee\u6807\u7b49\u7ea7 " + targetLevel);
                    this.weaponUpgradeInProgress = false;
                    return;
                }
                creditsCost = 600000.0f;
                storyPointsCost = 3;
                leftHullId = HULL_ID_LEFT_LEVEL2;
                rightHullId = HULL_ID_RIGHT_LEVEL2;
                System.out.println("HXMO: \u6267\u884c2\u7ea7\u706b\u529b\u5347\u7ea7");
                System.out.println("  \u6240\u9700\u8d44\u6e90: " + creditsCost + "\u661f\u5e01 + " + storyPointsCost + "\u6545\u4e8b\u70b9");
                System.out.println("  \u5347\u7ea7\u8def\u5f84: ZSMO554/ZXMO554 -> ZSMO553/ZXMO553");
            }
            System.out.println("HXMO: \u5f00\u59cb\u8d44\u6e90\u68c0\u67e5...");
            System.out.println("  \u6240\u9700\u661f\u5e01: " + creditsCost);
            var10000 = System.out;
            var10001 = cargo.getCredits().get();
            var10000.println("  \u5f53\u524d\u661f\u5e01: " + var10001);
            var10000 = System.out;
            boolean var31 = cargo.getCredits().get() >= creditsCost;
            var10000.println("  \u661f\u5e01\u8db3\u591f: " + var31);
            System.out.println("  \u6240\u9700\u6545\u4e8b\u70b9: " + storyPointsCost);
            System.out.println("  \u5f53\u524d\u6545\u4e8b\u70b9: " + sp);
            System.out.println("  \u6545\u4e8b\u70b9\u8db3\u591f: " + (sp >= storyPointsCost));
            if (cargo.getCredits().get() < creditsCost) {
                System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5931\u8d25\uff1a\u661f\u5e01\u4e0d\u8db3\uff01\u9700\u8981 " + creditsCost + "\uff0c\u53ea\u6709 " + cargo.getCredits().get());
                this.weaponUpgradeInProgress = false;
            } else if (sp < storyPointsCost) {
                System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5931\u8d25\uff1a\u6545\u4e8b\u70b9\u4e0d\u8db3\uff01\u9700\u8981 " + storyPointsCost + "\uff0c\u53ea\u6709 " + sp);
                this.weaponUpgradeInProgress = false;
            } else {
                System.out.println("HXMO: \u8d44\u6e90\u68c0\u67e5\u901a\u8fc7\uff0c\u5f00\u59cb\u6a21\u5757\u5347\u7ea7...");
                try {
                    ShipVariantAPI leftModuleVariant = variant.getModuleVariant(MODULE_SLOT_LEFT);
                    ShipVariantAPI rightModuleVariant = variant.getModuleVariant(MODULE_SLOT_RIGHT);
                    if (leftModuleVariant != null && rightModuleVariant != null) {
                        String currentLeftHull = leftModuleVariant.getHullSpec().getHullId();
                        String currentRightHull = rightModuleVariant.getHullSpec().getHullId();
                        System.out.println("HXMO: \u5f53\u524d\u6a21\u5757 - \u5de6:" + currentLeftHull + " \u53f3:" + currentRightHull);
                        System.out.println("HXMO: \u76ee\u6807\u6a21\u5757 - \u5de6:" + leftHullId + " \u53f3:" + rightHullId);
                        if (!currentLeftHull.equals(leftHullId) || !currentRightHull.equals(rightHullId)) {
                            ShipHullSpecAPI newLeftHullSpec = Global.getSettings().getHullSpec(leftHullId);
                            ShipHullSpecAPI newRightHullSpec = Global.getSettings().getHullSpec(rightHullId);
                            if (newLeftHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u5de6\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + leftHullId);
                            }
                            if (newRightHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u53f3\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + rightHullId);
                            }
                            ShipVariantAPI newLeftVariant = Global.getSettings().createEmptyVariant("hxmo_weapon_left_level" + targetLevel, newLeftHullSpec);
                            ShipVariantAPI newRightVariant = Global.getSettings().createEmptyVariant("hxmo_weapon_right_level" + targetLevel, newRightHullSpec);
                            variant.setModuleVariant(MODULE_SLOT_LEFT, newLeftVariant);
                            variant.setModuleVariant(MODULE_SLOT_RIGHT, newRightVariant);
                            cargo.getCredits().subtract(creditsCost);
                            Global.getSector().getPlayerStats().setStoryPoints(sp - storyPointsCost);
                            System.out.println("HXMO\u706b\u529b\u5347\u7ea7\uff1a\u6263\u9664" + creditsCost + "\u661f\u5e01\u548c" + storyPointsCost + "\u6545\u4e8b\u70b9");
                            var10000 = System.out;
                            float var32 = cargo.getCredits().get();
                            var10000.println("HXMO: \u5269\u4f59\u8d44\u6e90 - \u661f\u5e01:" + var32 + " \u6545\u4e8b\u70b9:" + Global.getSector().getPlayerStats().getStoryPoints());
                            this.weaponUpgradeLevel = targetLevel;
                            member.setVariant(variant, false, true);
                            System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5230\u7b49\u7ea7 " + targetLevel + " \u6210\u529f\uff01");
                            return;
                        }
                        System.out.println("HXMO\u706b\u529b\u6a21\u5757\u5df2\u7ecf\u662f\u6700\u65b0\u7248\u672c");
                        this.weaponUpgradeInProgress = false;
                        return;
                    }
                    System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5931\u8d25\uff1a\u65e0\u6cd5\u83b7\u53d6\u6a21\u5757\u53d8\u4f53");
                    this.weaponUpgradeInProgress = false;
                }
                catch (Exception e) {
                    System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5931\u8d25\uff1a" + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                finally {
                    this.weaponUpgradeInProgress = false;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void executeCarrierUpgrade() {
        System.out.println("HXMO: \u5f00\u59cb\u6267\u884c\u822a\u6bcd\u5347\u7ea7...");
        FleetMemberAPI member = this.getHXMO();
        if (member == null) {
            System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5931\u8d25\uff1a\u672a\u627e\u5230HXMO\u8230\u8239");
            this.carrierUpgradeInProgress = false;
        } else {
            ShipVariantAPI variant = member.getVariant();
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            int sp = Global.getSector().getPlayerStats().getStoryPoints();
            int targetLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_CARRIER_UPGRADE);
            float creditsCost = 0.0f;
            int storyPointsCost = 0;
            String leftHullId = "";
            String rightHullId = "";
            if (targetLevel == 1) {
                creditsCost = 400000.0f;
                storyPointsCost = 3;
                leftHullId = HULL_ID_CARRIER_LEFT_NEW;
                rightHullId = HULL_ID_CARRIER_RIGHT_NEW;
                System.out.println("HXMO: \u822a\u6bcd1\u7ea7\u5347\u7ea7 - SHMMO555/XHMMO555 -> SHMMO554/XHMMO554");
            } else if (targetLevel == 2) {
                creditsCost = 500000.0f;
                storyPointsCost = 4;
                leftHullId = HULL_ID_CARRIER_LEFT_LEVEL2;
                rightHullId = HULL_ID_CARRIER_RIGHT_LEVEL2;
                System.out.println("HXMO: \u822a\u6bcd2\u7ea7\u5347\u7ea7 - SHMMO554/XHMMO554 -> SHMMO553/XHMMO553");
            }
            if (cargo.getCredits().get() < creditsCost) {
                PrintStream var23 = System.out;
                float var24 = cargo.getCredits().get();
                var23.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5931\u8d25\uff1a\u661f\u5e01\u4e0d\u8db3 " + var24 + "/" + creditsCost);
                this.carrierUpgradeInProgress = false;
            } else if (sp < storyPointsCost) {
                System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5931\u8d25\uff1a\u6545\u4e8b\u70b9\u4e0d\u8db3 " + sp + "/" + storyPointsCost);
                this.carrierUpgradeInProgress = false;
            } else {
                System.out.println("\u5f00\u59cbHXMO\u822a\u6bcd\u5347\u7ea7\u5230\u7b49\u7ea7 " + targetLevel + "...");
                try {
                    ShipVariantAPI leftModuleVariant = variant.getModuleVariant(MODULE_SLOT_CARRIER_LEFT);
                    ShipVariantAPI rightModuleVariant = variant.getModuleVariant(MODULE_SLOT_CARRIER_RIGHT);
                    if (leftModuleVariant != null && rightModuleVariant != null) {
                        String currentLeftHull = leftModuleVariant.getHullSpec().getHullId();
                        String currentRightHull = rightModuleVariant.getHullSpec().getHullId();
                        System.out.println("HXMO: \u5f53\u524d\u822a\u6bcd\u6a21\u5757 - \u5de6:" + currentLeftHull + " \u53f3:" + currentRightHull);
                        System.out.println("HXMO: \u76ee\u6807\u822a\u6bcd\u6a21\u5757 - \u5de6:" + leftHullId + " \u53f3:" + rightHullId);
                        if (!currentLeftHull.equals(leftHullId) || !currentRightHull.equals(rightHullId)) {
                            ShipHullSpecAPI newLeftHullSpec = Global.getSettings().getHullSpec(leftHullId);
                            ShipHullSpecAPI newRightHullSpec = Global.getSettings().getHullSpec(rightHullId);
                            if (newLeftHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u5de6\u822a\u6bcd\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + leftHullId);
                            }
                            if (newRightHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u53f3\u822a\u6bcd\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + rightHullId);
                            }
                            ShipVariantAPI newLeftVariant = Global.getSettings().createEmptyVariant("hxmo_carrier_left_level" + targetLevel, newLeftHullSpec);
                            ShipVariantAPI newRightVariant = Global.getSettings().createEmptyVariant("hxmo_carrier_right_level" + targetLevel, newRightHullSpec);
                            variant.setModuleVariant(MODULE_SLOT_CARRIER_LEFT, newLeftVariant);
                            variant.setModuleVariant(MODULE_SLOT_CARRIER_RIGHT, newRightVariant);
                            cargo.getCredits().subtract(creditsCost);
                            Global.getSector().getPlayerStats().setStoryPoints(sp - storyPointsCost);
                            System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\uff1a\u6263\u9664" + creditsCost + "\u661f\u5e01\u548c" + storyPointsCost + "\u6545\u4e8b\u70b9");
                            PrintStream var10000 = System.out;
                            float var10001 = cargo.getCredits().get();
                            var10000.println("HXMO: \u5269\u4f59\u8d44\u6e90 - \u661f\u5e01:" + var10001 + " \u6545\u4e8b\u70b9:" + Global.getSector().getPlayerStats().getStoryPoints());
                            this.carrierUpgradeLevel = targetLevel;
                            member.setVariant(variant, false, true);
                            System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5230\u7b49\u7ea7 " + targetLevel + " \u6210\u529f\uff01");
                            return;
                        }
                        System.out.println("HXMO\u822a\u6bcd\u6a21\u5757\u5df2\u7ecf\u662f\u6700\u65b0\u7248\u672c");
                        this.carrierUpgradeInProgress = false;
                        return;
                    }
                    System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5931\u8d25\uff1a\u65e0\u6cd5\u83b7\u53d6\u6a21\u5757\u53d8\u4f53");
                    this.carrierUpgradeInProgress = false;
                }
                catch (Exception e) {
                    System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5931\u8d25\uff1a" + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                finally {
                    this.carrierUpgradeInProgress = false;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void executeArmorUpgrade() {
        System.out.println("HXMO: \u5f00\u59cb\u6267\u884c\u88c5\u7532\u5347\u7ea7...");
        FleetMemberAPI member = this.getHXMO();
        if (member == null) {
            System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u5931\u8d25\uff1a\u672a\u627e\u5230HXMO\u8230\u8239");
            this.armorUpgradeInProgress = false;
        } else {
            ShipVariantAPI variant = member.getVariant();
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            int sp = Global.getSector().getPlayerStats().getStoryPoints();
            float creditsCost = 300000.0f;
            int storyPointsCost = 1;
            String leftHullId = HULL_ID_ARMOR_LEFT_NEW;
            String rightHullId = HULL_ID_ARMOR_RIGHT_NEW;
            System.out.println("HXMO: \u88c5\u7532\u5347\u7ea7 - ZZJMO555/YZJMO555 -> ZZJMO/YZJMO");
            if (cargo.getCredits().get() < creditsCost) {
                PrintStream var22 = System.out;
                float var23 = cargo.getCredits().get();
                var22.println("HXMO\u88c5\u7532\u5347\u7ea7\u5931\u8d25\uff1a\u661f\u5e01\u4e0d\u8db3 " + var23 + "/" + creditsCost);
                this.armorUpgradeInProgress = false;
            } else if (sp < storyPointsCost) {
                System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u5931\u8d25\uff1a\u6545\u4e8b\u70b9\u4e0d\u8db3 " + sp + "/" + storyPointsCost);
                this.armorUpgradeInProgress = false;
            } else {
                System.out.println("\u5f00\u59cbHXMO\u88c5\u7532\u5347\u7ea7...");
                try {
                    ShipVariantAPI leftModuleVariant = variant.getModuleVariant(MODULE_SLOT_ARMOR_LEFT);
                    ShipVariantAPI rightModuleVariant = variant.getModuleVariant(MODULE_SLOT_ARMOR_RIGHT);
                    if (leftModuleVariant != null && rightModuleVariant != null) {
                        String currentLeftHull = leftModuleVariant.getHullSpec().getHullId();
                        String currentRightHull = rightModuleVariant.getHullSpec().getHullId();
                        System.out.println("HXMO: \u5f53\u524d\u88c5\u7532\u6a21\u5757 - \u5de6:" + currentLeftHull + " \u53f3:" + currentRightHull);
                        System.out.println("HXMO: \u76ee\u6807\u88c5\u7532\u6a21\u5757 - \u5de6:" + leftHullId + " \u53f3:" + rightHullId);
                        if (!currentLeftHull.equals(leftHullId) || !currentRightHull.equals(rightHullId)) {
                            ShipHullSpecAPI newLeftHullSpec = Global.getSettings().getHullSpec(leftHullId);
                            ShipHullSpecAPI newRightHullSpec = Global.getSettings().getHullSpec(rightHullId);
                            if (newLeftHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u5de6\u88c5\u7532\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + leftHullId);
                            }
                            if (newRightHullSpec == null) {
                                throw new RuntimeException("\u627e\u4e0d\u5230\u53f3\u88c5\u7532\u6a21\u5757\u8239\u4f53\u89c4\u683c\uff1a" + rightHullId);
                            }
                            ShipVariantAPI newLeftVariant = Global.getSettings().createEmptyVariant("hxmo_armor_left_upgraded", newLeftHullSpec);
                            ShipVariantAPI newRightVariant = Global.getSettings().createEmptyVariant("hxmo_armor_right_upgraded", newRightHullSpec);
                            variant.setModuleVariant(MODULE_SLOT_ARMOR_LEFT, newLeftVariant);
                            variant.setModuleVariant(MODULE_SLOT_ARMOR_RIGHT, newRightVariant);
                            cargo.getCredits().subtract(creditsCost);
                            Global.getSector().getPlayerStats().setStoryPoints(sp - storyPointsCost);
                            System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\uff1a\u6263\u9664" + creditsCost + "\u661f\u5e01\u548c" + storyPointsCost + "\u6545\u4e8b\u70b9");
                            PrintStream var10000 = System.out;
                            float var10001 = cargo.getCredits().get();
                            var10000.println("HXMO: \u5269\u4f59\u8d44\u6e90 - \u661f\u5e01:" + var10001 + " \u6545\u4e8b\u70b9:" + Global.getSector().getPlayerStats().getStoryPoints());
                            this.armorUpgradeLevel = 1;
                            member.setVariant(variant, false, true);
                            System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u6210\u529f\uff01");
                            return;
                        }
                        System.out.println("HXMO\u88c5\u7532\u6a21\u5757\u5df2\u7ecf\u662f\u6700\u65b0\u7248\u672c");
                        this.armorUpgradeInProgress = false;
                        return;
                    }
                    System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u5931\u8d25\uff1a\u65e0\u6cd5\u83b7\u53d6\u6a21\u5757\u53d8\u4f53");
                    this.armorUpgradeInProgress = false;
                }
                catch (Exception e) {
                    System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u5931\u8d25\uff1a" + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                finally {
                    this.armorUpgradeInProgress = false;
                }
            }
        }
    }

    private boolean hasHXMOInFleet() {
        boolean hasHXMO = this.getHXMO() != null;
        System.out.println("HXMO: \u68c0\u67e5\u8230\u8239\u5728\u8230\u961f\u4e2d - " + hasHXMO);
        return hasHXMO;
    }

    private FleetMemberAPI getHXMO() {
        try {
            if (Global.getSector() != null && Global.getSector().getPlayerFleet() != null) {
                for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                    if (!member.getId().equals(this.hxmoId) || !"HXMO".equals(member.getHullId())) continue;
                    PrintStream var10000 = System.out;
                    String var10001 = member.getId();
                    var10000.println("HXMO: \u627e\u5230\u8230\u8239 - ID:" + var10001 + ", \u8239\u4f53:" + member.getHullId());
                    return member;
                }
            }
            System.out.println("HXMO: \u672a\u627e\u5230\u8230\u8239 - \u76ee\u6807ID:" + this.hxmoId);
        }
        catch (Exception e) {
            System.out.println("\u83b7\u53d6HXMO\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return null;
    }

    private void resetUpgradeState() {
        System.out.println("HXMO: \u91cd\u7f6e\u5347\u7ea7\u72b6\u6001");
        this.weaponUpgradeInProgress = false;
        this.carrierUpgradeInProgress = false;
        this.armorUpgradeInProgress = false;
        this.fixInProgress = false;
    }

    private void init() {
        System.out.println("HXMO: \u521d\u59cb\u5316\u5347\u7ea7\u7cfb\u7edf...");
        boolean hasShip = this.hasHXMOInFleet();
        Global.getSector().getMemoryWithoutUpdate().set(KEY_HASSHIP, (Object)hasShip);
        System.out.println("HXMO: \u8bbe\u7f6e\u8230\u8239\u5b58\u5728\u72b6\u6001 - " + hasShip);
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_UPGRADE)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_UPGRADE, (Object)0);
            System.out.println("HXMO: \u521d\u59cb\u5316\u706b\u529b\u7b49\u7ea7\u4e3a0");
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_CARRIER_UPGRADE)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CARRIER_UPGRADE, (Object)0);
            System.out.println("HXMO: \u521d\u59cb\u5316\u822a\u6bcd\u7b49\u7ea7\u4e3a0");
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_ARMOR_UPGRADE)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_ARMOR_UPGRADE, (Object)0);
            System.out.println("HXMO: \u521d\u59cb\u5316\u88c5\u7532\u7b49\u7ea7\u4e3a0");
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_NEEDUPDATE)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)false);
            System.out.println("HXMO: \u521d\u59cb\u5316\u66f4\u65b0\u6807\u8bb0\u4e3afalse");
        }
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_FIX)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_FIX, (Object)false);
            System.out.println("HXMO: \u521d\u59cb\u5316\u4fee\u590d\u6807\u8bb0\u4e3afalse");
        }
        this.weaponUpgradeLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_UPGRADE);
        this.carrierUpgradeLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_CARRIER_UPGRADE);
        this.armorUpgradeLevel = Global.getSector().getMemoryWithoutUpdate().getInt(KEY_ARMOR_UPGRADE);
        System.out.println("HXMO\u5347\u7ea7\u7cfb\u7edf\u521d\u59cb\u5316\u5b8c\u6210\uff0c\u5f53\u524d\u7b49\u7ea7:");
        System.out.println("  \u706b\u529b: " + this.weaponUpgradeLevel);
        System.out.println("  \u822a\u6bcd: " + this.carrierUpgradeLevel);
        System.out.println("  \u88c5\u7532: " + this.armorUpgradeLevel);
    }

    public void advance(float amount) {
        boolean hasHXMO = this.hasHXMOInFleet();
        Global.getSector().getMemoryWithoutUpdate().set(KEY_HASSHIP, (Object)hasHXMO);
        if (hasHXMO) {
            System.out.println("HXMO: \u8230\u8239\u5728\u8230\u961f\u4e2d\uff0c\u68c0\u67e5\u5347\u7ea7\u6761\u4ef6...");
            this.recordCanUpgradeOrNot();
            if (this.needUpdate()) {
                System.out.println("HXMO: \u68c0\u6d4b\u5230\u9700\u8981\u66f4\u65b0\uff0c\u5f00\u59cb\u5904\u7406...");
                if (this.checkForFix()) {
                    this.executeFix();
                }
                if (this.checkForWeaponUpgrade()) {
                    this.executeWeaponUpgrade();
                }
                if (this.checkForCarrierUpgrade()) {
                    this.executeCarrierUpgrade();
                }
                if (this.checkForArmorUpgrade()) {
                    this.executeArmorUpgrade();
                }
                this.completeUpdate();
                System.out.println("HXMO: \u66f4\u65b0\u5904\u7406\u5b8c\u6210");
            } else {
                System.out.println("HXMO: \u65e0\u9700\u66f4\u65b0");
            }
        } else {
            System.out.println("HXMO: \u8230\u8239\u4e0d\u5728\u8230\u961f\u4e2d\uff0c\u91cd\u7f6e\u72b6\u6001");
            this.resetUpgradeState();
        }
    }

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return true;
    }

    public static int getHXMOUpgradeLevel() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_UPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getInt(KEY_UPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u83b7\u53d6HXMO\u5347\u7ea7\u7b49\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return 0;
    }

    public static int getHXMOCarrierUpgradeLevel() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CARRIER_UPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getInt(KEY_CARRIER_UPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u83b7\u53d6HXMO\u822a\u6bcd\u5347\u7ea7\u7b49\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return 0;
    }

    public static int getHXMOArmorUpgradeLevel() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_ARMOR_UPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getInt(KEY_ARMOR_UPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u83b7\u53d6HXMO\u88c5\u7532\u5347\u7ea7\u7b49\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return 0;
    }

    public static boolean hasHXMO() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_HASSHIP)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_HASSHIP);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5HXMO\u72b6\u6001\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canUpgrade() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CANUPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CANUPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u5347\u7ea7\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canWeaponUpgrade2() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CAN_WEAPON_UPGRADE_2)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CAN_WEAPON_UPGRADE_2);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u706b\u529b\u4e8c\u7ea7\u5347\u7ea7\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canCarrierUpgrade() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CAN_CARRIER_UPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CAN_CARRIER_UPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u822a\u6bcd\u5347\u7ea7\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canCarrierUpgrade2() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CAN_CARRIER_UPGRADE_2)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CAN_CARRIER_UPGRADE_2);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u822a\u6bcd\u4e8c\u7ea7\u5347\u7ea7\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canArmorUpgrade() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CAN_ARMOR_UPGRADE)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CAN_ARMOR_UPGRADE);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u88c5\u7532\u5347\u7ea7\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static boolean canFix() {
        try {
            if (Global.getSector().getMemoryWithoutUpdate().contains(KEY_CANFIX)) {
                return Global.getSector().getMemoryWithoutUpdate().getBoolean(KEY_CANFIX);
            }
        }
        catch (Exception e) {
            System.out.println("\u68c0\u67e5\u4fee\u590d\u6761\u4ef6\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
        return false;
    }

    public static void triggerUpgrade() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_UPGRADE, (Object)1);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u706b\u529b\u5347\u7ea7\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u5347\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }

    public static void triggerWeaponUpgrade2() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_UPGRADE, (Object)2);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u706b\u529b\u4e8c\u7ea7\u5347\u7ea7\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u706b\u529b\u4e8c\u7ea7\u5347\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }

    public static void triggerCarrierUpgrade() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CARRIER_UPGRADE, (Object)1);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u822a\u6bcd\u5347\u7ea7\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u822a\u6bcd\u5347\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }

    public static void triggerCarrierUpgrade2() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_CARRIER_UPGRADE, (Object)2);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u822a\u6bcd\u4e8c\u7ea7\u5347\u7ea7\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u822a\u6bcd\u4e8c\u7ea7\u5347\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }

    public static void triggerArmorUpgrade() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_ARMOR_UPGRADE, (Object)1);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u88c5\u7532\u5347\u7ea7\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u88c5\u7532\u5347\u7ea7\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }

    public static void triggerFix() {
        try {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_FIX, (Object)true);
            Global.getSector().getMemoryWithoutUpdate().set(KEY_NEEDUPDATE, (Object)true);
            System.out.println("HXMO\u4fee\u590d\u5df2\u89e6\u53d1");
        }
        catch (Exception e) {
            System.out.println("\u89e6\u53d1HXMO\u4fee\u590d\u65f6\u53d1\u751f\u5f02\u5e38\uff1a" + e.getMessage());
        }
    }
}

