/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.FactionAPI
 *  com.fs.starfarer.api.campaign.RepLevel
 *  com.fs.starfarer.api.campaign.SectorAPI
 *  com.fs.starfarer.api.campaign.SectorGeneratorPlugin
 */
package data.world;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import data.CustomSystemGenerator;

public class threat_NormalGenerate
implements SectorGeneratorPlugin {
    public void generate(SectorAPI sector) {
        CustomSystemGenerator systemGenerator = new CustomSystemGenerator();
        systemGenerator.generate(sector);
        this.adjustRelations(sector);
    }

    protected void adjustRelations(SectorAPI sector) {
        String[] hostileFactions;
        FactionAPI threatFaction = sector.getFaction("threat_qr");
        if (threatFaction == null) {
            return;
        }
        for (String factionId : hostileFactions = new String[]{"tutorial", "remnant", "pirates", "neutral", "independent", "hegemony", "tritachyon", "luddic_church", "luddic_path", "persean", "sindrian_diktat"}) {
            FactionAPI other = sector.getFaction(factionId);
            if (other == null) continue;
            threatFaction.setRelationship(factionId, RepLevel.HOSTILE);
        }
        System.out.println("threat_qr \u521d\u59cb\u5173\u7cfb\u5df2\u8bbe\u7f6e\uff1a\u4e0e\u4e3b\u8981\u6d3e\u7cfb\u654c\u5bf9");
    }
}

