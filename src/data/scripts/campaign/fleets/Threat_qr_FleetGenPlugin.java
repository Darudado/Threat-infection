/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.AICoreOfficerPlugin
 *  com.fs.starfarer.api.campaign.CampaignFleetAPI
 *  com.fs.starfarer.api.characters.PersonAPI
 *  com.fs.starfarer.api.fleet.FleetMemberAPI
 *  com.fs.starfarer.api.impl.campaign.fleets.BaseGenerateFleetOfficersPlugin
 *  com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
 *  com.fs.starfarer.api.impl.campaign.fleets.GenerateFleetOfficersPlugin$GenerateFleetOfficersPickData
 *  com.fs.starfarer.api.impl.campaign.ids.Ranks
 *  com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 */
package data.scripts.campaign.fleets;

import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.BaseGenerateFleetOfficersPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.GenerateFleetOfficersPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Threat_qr_FleetGenPlugin
extends BaseGenerateFleetOfficersPlugin {
    public int getHandlingPriority(Object params) {
        if (!(params instanceof GenerateFleetOfficersPlugin.GenerateFleetOfficersPickData)) {
            return -1;
        }
        GenerateFleetOfficersPlugin.GenerateFleetOfficersPickData data = (GenerateFleetOfficersPlugin.GenerateFleetOfficersPickData)params;
        if (data.params != null && !data.params.withOfficers) {
            return -1;
        }
        if (data.params.aiCores != null) {
            return 200;
        }
        if (data.fleet == null || !data.fleet.getFaction().getId().equals("threat_qr")) {
            return -1;
        }
        return 200;
    }

    public void addCommanderAndOfficers(CampaignFleetAPI fleet, FleetParamsV3 params, Random random) {
        List<FleetMemberAPI> members;
        if (random == null) {
            random = Misc.random;
        }
        if ((members = fleet.getFleetData().getMembersListCopy()).isEmpty()) {
            return;
        }
        HashMap<String, AICoreOfficerPlugin> plugins = new HashMap<String, AICoreOfficerPlugin>();
        plugins.put("cl_core", Misc.getAICoreOfficerPlugin((String)"cl_core"));
        plugins.put("hl_core", Misc.getAICoreOfficerPlugin((String)"hl_core"));
        float fleetFP = 0.0f;
        FleetMemberAPI flagShip = null;
        int flagFP = -1;
        for (FleetMemberAPI member : members) {
            fleetFP += (float)member.getFleetPointCost();
            if (member.isFighterWing() || member.getFleetPointCost() <= flagFP) continue;
            flagFP = member.getFleetPointCost();
            flagShip = member;
        }
        if (flagFP <= 0) {
            return;
        }
        int numCommanderSkills = 0;
        if (fleetFP > 75.0f) {
            ++numCommanderSkills;
        }
        if (fleetFP > 125.0f) {
            ++numCommanderSkills;
        }
        if (fleetFP > 175.0f) {
            ++numCommanderSkills;
        }
        if (params != null && params.noCommanderSkills != null && params.noCommanderSkills.booleanValue()) {
            numCommanderSkills = 0;
        }
        for (FleetMemberAPI member : members) {
            AICoreOfficerPlugin plugin;
            String chosenCore;
            if (member.isFighterWing()) continue;
            boolean isFlagship = member == flagShip;
            boolean isCivilian = member.isCivilian();
            WeightedRandomPicker picker = new WeightedRandomPicker(random);
            if (isFlagship) {
                picker.add((Object)"hl_core", 1.0f);
            } else {
                float fpRatio = (float)member.getFleetPointCost() / (float)flagFP;
                picker.add((Object)"cl_core", fpRatio * 1.5f);
                picker.add((Object)"hl_core", fpRatio * 1.0f);
                if (isCivilian) {
                    picker.add((Object)"cl_core", fpRatio * 0.5f);
                    picker.add((Object)"hl_core", fpRatio * 0.1f);
                }
            }
            if ((chosenCore = (String)picker.pick()) == null || (plugin = (AICoreOfficerPlugin)plugins.get(chosenCore)) == null) continue;
            PersonAPI person = plugin.createPerson(chosenCore, fleet.getFaction().getId(), random);
            person.setName(fleet.getFaction().createRandomPerson().getName());
            member.setCaptain(person);
            if (!isCivilian || member.getVariant() == null) continue;
            member.getVariant().addTag("no_ai_core_drop");
        }
        if (flagShip != null && flagShip.getCaptain() != null) {
            PersonAPI commander = flagShip.getCaptain();
            commander.setRankId(Ranks.SPACE_COMMANDER);
            commander.setPostId(Ranks.POST_FLEET_COMMANDER);
            fleet.setCommander(commander);
            fleet.getFleetData().setFlagship(flagShip);
            commander.getStats().setSkipRefresh(true);
            commander.getStats().setSkillLevel("crew_training", 1.0f);
            commander.getStats().setSkillLevel("officer_training", 1.0f);
            commander.getStats().setSkillLevel("support_doctrine", 1.0f);
            commander.getStats().setSkillLevel("electronic_warfare", 1.0f);
            commander.getStats().setSkillLevel("flux_regulation", 1.0f);
            commander.getStats().setSkillLevel("coordinated_maneuvers", 1.0f);
            RemnantOfficerGeneratorPlugin.addCommanderSkills((PersonAPI)commander, (CampaignFleetAPI)fleet, (FleetParamsV3)params, (int)numCommanderSkills, (Random)random);
            commander.getStats().setSkipRefresh(false);
        }
    }
}

