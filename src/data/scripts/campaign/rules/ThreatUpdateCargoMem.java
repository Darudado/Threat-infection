/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.InteractionDialogAPI
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
 *  com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
 *  com.fs.starfarer.api.util.Misc$Token
 */
package data.scripts.campaign.rules;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;

public class ThreatUpdateCargoMem
extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        String cmd = "";
        if (!params.isEmpty()) {
            cmd = params.get(0).getString(memoryMap);
        }
        switch (cmd) {
            case "core_exchange": {
                ThreatUpdateCargoMem.updateAll("credits", "alpha_core", "beta_core", "gamma_core", "fragment_fabricator", "threat_processing_unit");
                return true;
            }
            case "threat_items": {
                ThreatUpdateCargoMem.updateAll("fragment_fabricator", "threat_processing_unit");
                return true;
            }
            case "": {
                ThreatUpdateCargoMem.updateAll("credits");
                return true;
            }
        }
        return false;
    }

    public static void updateAll(String ... ids) {
        for (String id : ids) {
            AddRemoveCommodity.updatePlayerMemoryQuantity((String)id);
        }
    }
}

