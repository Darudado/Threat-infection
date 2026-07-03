/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.EveryFrameScript
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CampaignFleetAPI
 *  com.fs.starfarer.api.campaign.FactionAPI
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.RepLevel
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.characters.CharacterCreationData
 *  com.fs.starfarer.api.characters.MutableCharacterStatsAPI
 *  com.fs.starfarer.api.combat.ShipVariantAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.impl.campaign.DModManager
 *  com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
 *  com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType
 *  com.fs.starfarer.api.loading.HullModSpecAPI
 *  com.fs.starfarer.api.loading.VariantSource
 *  com.fs.starfarer.api.plugins.LevelupPlugin
 *  com.fs.starfarer.api.util.Misc
 *  exerelin.campaign.PlayerFactionStore
 *  exerelin.campaign.customstart.CustomStart
 *  exerelin.utilities.NexUtils
 */
package data.scripts.campaign.customstart;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.CharacterCreationData;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.NGCAddStartingShipsByFleetType;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.LevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.customstart.HXMO_Upgrade_EFS;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import exerelin.utilities.NexUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WxbsStart
extends CustomStart {
    protected List<String> ships = new ArrayList<String>(Arrays.asList("crig_Standard", "HXMO555"));
    private static final List<String> SHIPS_WITH_DMODS = Arrays.asList("HXMO");
    private static final String HXMO_UPGRADE_HASSHIP = "$HXMO_upgrade_hasship";
    private static final String HXMO_UPGRADE_LEVEL = "$HXMO_upgrade_level";
    private static final String HXMO_UPGRADE_NEEDUPDATE = "$HXMO_upgrade_needupdate";

    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PlayerFactionStore.setPlayerFactionIdNGC((String)"independent");
        CharacterCreationData data = (CharacterCreationData)memoryMap.get("local").get("$characterData");
        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds((InteractionDialogAPI)dialog, (CharacterCreationData)data, (String)null, this.ships);
        data.getPerson().getStats().setSkillLevel("automated_ships", 1.0f);
        MutableCharacterStatsAPI playerStats = data.getPerson().getStats();
        LevelupPlugin plugin = Global.getSettings().getLevelupPlugin();
        long xpNeeded = plugin.getXPForLevel(Math.min(plugin.getMaxLevel(), playerStats.getLevel() + 1)) - playerStats.getXP();
        playerStats.addXP(xpNeeded);
        data.addScript(() -> {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            Random random = new Random(NexUtils.getStartingSeed());
            String hxmoId = null;
            block12: for (FactionAPI faction : Global.getSector().getAllFactions()) {
                if (faction.isNeutralFaction() || faction.isPlayerFaction()) continue;
                switch (faction.getId()) {
                    case "pirates": {
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), RepLevel.VENGEFUL);
                        continue block12;
                    }
                    case "independent": {
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), 0.0f);
                        continue block12;
                    }
                    case "tritachyon": {
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -1.0f);
                        continue block12;
                    }
                    case "hegemony": {
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -1.0f);
                        continue block12;
                    }
                }
                float currLevel = Global.getSector().getPlayerFaction().getRelationship(faction.getId());
                if (!(currLevel > -0.5f)) continue;
                Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -0.5f);
            }
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                ShipVariantAPI v = member.getVariant().clone();
                v.setSource(VariantSource.REFIT);
                v.setHullVariantId(Misc.genUID());
                member.setVariant(v, false, false);
                String hullId = member.getHullSpec().getHullId();
                if (SHIPS_WITH_DMODS.contains(hullId)) {
                    hxmoId = member.getId();
                    for (int i = 0; i < 10; ++i) {
                        DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)10, (Random)random);
                        if (member.getVariant().hasHullMod("degraded_drive_field")) {
                            member.getVariant().removePermaMod("degraded_drive_field");
                        }
                        if (member.getVariant().hasHullMod("increased_maintenance")) {
                            member.getVariant().removePermaMod("increased_maintenance");
                        }
                        if (member.getVariant().hasHullMod("erratic_injector")) {
                            member.getVariant().removePermaMod("erratic_injector");
                        }
                        if (member.getVariant().hasHullMod("degraded_life_support")) {
                            member.getVariant().removePermaMod("degraded_life_support");
                        }
                        if (member.getVariant().hasHullMod("faulty_auto")) {
                            member.getVariant().removePermaMod("faulty_auto");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_automation")) {
                            member.getVariant().removePermaMod("vayra_damaged_automation");
                        }
                        if (member.getVariant().hasHullMod("vayra_damaged_everything")) {
                            member.getVariant().removePermaMod("vayra_damaged_everything");
                        }
                        if (!member.getVariant().hasHullMod("vayra_damaged_lifesupport")) continue;
                        member.getVariant().removePermaMod("vayra_damaged_lifesupport");
                    }
                    System.out.println("Applied D-Mods to ship: " + hullId);
                    continue;
                }
                System.out.println("Skipped D-Mods for ship: " + hullId);
            }
            if (hxmoId != null) {
                Global.getSector().addScript((EveryFrameScript)new HXMO_Upgrade_EFS(hxmoId));
                Global.getSector().getMemoryWithoutUpdate().set(HXMO_UPGRADE_HASSHIP, (Object)true);
                Global.getSector().getMemoryWithoutUpdate().set(HXMO_UPGRADE_LEVEL, (Object)0);
                Global.getSector().getMemoryWithoutUpdate().set(HXMO_UPGRADE_NEEDUPDATE, (Object)false);
                System.out.println("HXMO\u5347\u7ea7\u7cfb\u7edf\u5df2\u521d\u59cb\u5316\uff0c\u8230\u8239ID: " + hxmoId);
            } else {
                System.out.println("\u8b66\u544a\uff1a\u672a\u627e\u5230HXMO\u8230\u8239\uff0c\u5347\u7ea7\u7cfb\u7edf\u672a\u521d\u59cb\u5316");
            }
            Global.getSector().addScript(new EveryFrameScript(){
                private boolean done = false;

                public boolean isDone() {
                    return this.done;
                }

                public boolean runWhilePaused() {
                    return true;
                }

                public void advance(float amount) {
                    CampaignFleetAPI fleet;
                    if (!this.done && (fleet = Global.getSector().getPlayerFleet()) != null && !fleet.getFleetData().getMembersListCopy().isEmpty()) {
                        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                            String hullId = member.getHullSpec().getHullId();
                            if (!SHIPS_WITH_DMODS.contains(hullId)) continue;
                            this.done = true;
                            int numDMods = 0;
                            for (String modId : member.getVariant().getHullMods()) {
                                HullModSpecAPI modSpec = Global.getSettings().getHullModSpec(modId);
                                if (modSpec == null || !modSpec.hasTag("dmod")) continue;
                                ++numDMods;
                            }
                            Global.getSector().getMemoryWithoutUpdate().set("$swpRestoreTarget", (Object)member.getId());
                            Global.getSector().getMemoryWithoutUpdate().set("$swpRestoreSeed", (Object)Misc.genRandomSeed());
                            Global.getSector().getMemoryWithoutUpdate().set("$swpStartingDMods", (Object)numDMods);
                            System.out.println("Recorded D-Mod count for " + hullId + ": " + numDMods);
                            break;
                        }
                    }
                }
            });
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        });
        FireBest.fire((String)null, (InteractionDialogAPI)dialog, memoryMap, (String)"ExerelinNGCStep4");
    }
}

