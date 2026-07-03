/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  com.fs.starfarer.api.impl.campaign.rulecmd.newgame.Nex_NGCFinalize
 *  com.fs.starfarer.api.loading.VariantSource
 *  com.fs.starfarer.api.plugins.LevelupPlugin
 *  com.fs.starfarer.api.util.Misc
 *  exerelin.campaign.PlayerFactionStore
 *  exerelin.campaign.customstart.CustomStart
 *  exerelin.utilities.NexUtils
 */
package data.scripts.campaign.customstart;

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
import com.fs.starfarer.api.impl.campaign.rulecmd.newgame.Nex_NGCFinalize;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.plugins.LevelupPlugin;
import com.fs.starfarer.api.util.Misc;
import exerelin.campaign.PlayerFactionStore;
import exerelin.campaign.customstart.CustomStart;
import exerelin.utilities.NexUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FabricatorStart
extends CustomStart {
    protected List<String> ships = new ArrayList<String>(Arrays.asList("venture_Exploration", "wxbzz", "crig_Standard"));

    public void execute(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        PlayerFactionStore.setPlayerFactionIdNGC((String)"independent");
        CharacterCreationData data = (CharacterCreationData)memoryMap.get("local").get("$characterData");
        NGCAddStartingShipsByFleetType.generateFleetFromVariantIds((InteractionDialogAPI)dialog, (CharacterCreationData)data, null, this.ships);
        data.getPerson().getStats().setSkillLevel("automated_ships", 1.0f);
        MutableCharacterStatsAPI playerStats = data.getPerson().getStats();
        LevelupPlugin plugin = Global.getSettings().getLevelupPlugin();
        long xpNeeded = plugin.getXPForLevel(Math.min(plugin.getMaxLevel(), playerStats.getLevel() + 1)) - playerStats.getXP();
        playerStats.addXP(xpNeeded);
        data.addScript(() -> {
            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            Random random = new Random(NexUtils.getStartingSeed());
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
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -0.5f);
                        continue block12;
                    }
                    case "hegemony": {
                        Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -0.5f);
                        continue block12;
                    }
                }
                float currLevel = Global.getSector().getPlayerFaction().getRelationship(faction.getId());
                if (!(currLevel > -0.35f)) continue;
                Global.getSector().getPlayerFaction().setRelationship(faction.getId(), -0.35f);
            }
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                ShipVariantAPI v = member.getVariant().clone();
                v.setSource(VariantSource.REFIT);
                v.setHullVariantId(Misc.genUID());
                member.setVariant(v, false, false);
                DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)3, (Random)random);
                for (int i = 0; i < 5; ++i) {
                    if (member.getVariant().hasHullMod("degraded_engines")) {
                        member.getVariant().removePermaMod("degraded_engines");
                        DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)1, (Random)random);
                    }
                    if (member.getVariant().hasHullMod("increased_maintenance")) {
                        member.getVariant().removePermaMod("increased_maintenance");
                        DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)1, (Random)random);
                    }
                    if (member.getVariant().hasHullMod("erratic_injector")) {
                        member.getVariant().removePermaMod("erratic_injector");
                        DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)1, (Random)random);
                    }
                    if (member.getVariant().hasHullMod("degraded_life_support")) {
                        member.getVariant().removePermaMod("degraded_life_support");
                        DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)1, (Random)random);
                    }
                    if (!member.getVariant().hasHullMod("faulty_auto")) continue;
                    member.getVariant().removePermaMod("faulty_auto");
                    DModManager.addDMods((FleetMemberAPI)member, (boolean)true, (int)1, (Random)random);
                }
            }
            fleet.getFleetData().setSyncNeeded();
            fleet.getFleetData().syncIfNeeded();
        });
        Nex_NGCFinalize.addStartingDModScript((MemoryAPI)memoryMap.get("local"));
        FireBest.fire(null, (InteractionDialogAPI)dialog, memoryMap, (String)"ExerelinNGCStep4");
    }
}

