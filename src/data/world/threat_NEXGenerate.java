/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.campaign.SectorAPI
 *  exerelin.campaign.SectorManager
 */
package data.world;

import com.fs.starfarer.api.campaign.SectorAPI;
import data.world.threat_NormalGenerate;
import exerelin.campaign.SectorManager;

public class threat_NEXGenerate
extends threat_NormalGenerate {
    @Override
    public void generate(SectorAPI sector) {
        if (SectorManager.getManager().isCorvusMode()) {
            super.generate(sector);
        }
    }
}

