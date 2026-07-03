/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
 *  com.fs.starfarer.api.campaign.JumpPointAPI
 *  com.fs.starfarer.api.campaign.LocationAPI
 *  com.fs.starfarer.api.campaign.PlanetAPI
 *  com.fs.starfarer.api.campaign.SectorAPI
 *  com.fs.starfarer.api.campaign.SectorEntityToken
 *  com.fs.starfarer.api.campaign.StarSystemAPI
 *  com.fs.starfarer.api.impl.campaign.procgen.StarAge
 *  com.fs.starfarer.api.util.Misc
 */
package data;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class CustomSystemGenerator {
    public void generate(SectorAPI sector) {
        if (sector.getStarSystem("NOVA_REGULA") != null) {
            Global.getLogger(CustomSystemGenerator.class).info((Object)"NOVA_REGULA \u5df2\u5b58\u5728\uff0c\u8df3\u8fc7\u751f\u6210");
            return;
        }
        StarSystemAPI system = sector.createStarSystem("NOVA_REGULA");
        system.getLocation().set(-60000.0f, -12000.0f);
        system.setBackgroundTextureFilename("graphics/backgrounds/background3.jpg");
        try {
            SectorEntityToken nebula = Misc.addNebulaFromPNG((String)"data/campaign/terrain/hybrasil_nebula.png", (float)0.0f, (float)0.0f, (LocationAPI)system, (String)"terrain", (String)"nebula", (int)4, (int)4, (StarAge)StarAge.OLD);
            nebula.setName("\u7eb3\u7c73\u661f\u4e91");
        }
        catch (Exception e) {
            Global.getLogger(CustomSystemGenerator.class).warn((Object)("NOVA_REGULA \u661f\u4e91\u6dfb\u52a0\u5931\u8d25: " + e.getMessage()));
        }
        PlanetAPI star = system.initStar("nova_regula_star", "star_blue_supergiant", 600.0f, 800.0f, 5.0f, 0.8f, 1.0f);
        system.setLightColor(new Color(140, 180, 255, 255));
        float innerBeltRadius = 1400.0f;
        float innerBeltWidth = 200.0f;
        system.addAsteroidBelt((SectorEntityToken)star, 60, innerBeltRadius, innerBeltWidth, innerBeltWidth, 100.0f);
        try {
            system.addRingBand((SectorEntityToken)star, "misc", "rings_dust0", innerBeltWidth, 3, new Color(150, 170, 255, 150), innerBeltWidth, innerBeltRadius - innerBeltWidth / 2.0f, 110.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)star, "misc", "rings_asteroids0", innerBeltWidth, 3, new Color(130, 150, 235, 130), innerBeltWidth, innerBeltRadius + innerBeltWidth / 2.0f, 100.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        PlanetAPI barrenPlanet = system.addPlanet("nova_regula_1", (SectorEntityToken)star, "CX-9811", "barren", 0.0f, 100.0f, 2800.0f, 85.0f);
        PlanetAPI gasGiant = system.addPlanet("nova_regula_2", (SectorEntityToken)star, "Ignis", "gas_giant", 90.0f, 200.0f, 4900.0f, 350.0f);
        CustomCampaignEntityAPI gasStation = system.addCustomEntity("nova_regula_gas_station", "\u8054\u90a6\u4e4b\u7ec8 \u6c14\u4f53\u91c7\u96c6\u7ad9", "station_side00", "threat");
        gasStation.setCircularOrbitPointingDown((SectorEntityToken)gasGiant, 60.0f, 600.0f, 40.0f);
        gasStation.setInteractionImage("illustrations", "orbital");
        system.addPlanet("nova_regula_2a", (SectorEntityToken)gasGiant, "Frost", "frozen", 0.0f, 50.0f, 700.0f, 42.0f);
        system.addAsteroidBelt((SectorEntityToken)gasGiant, 0, 700.0f, 698.0f, 702.0f, 800.0f, "asteroid_belt", "\u51b0\u971c\u5c0f\u884c\u661f\u5e26");
        PlanetAPI oceanPlanet = system.addPlanet("nova_regula_3", (SectorEntityToken)star, "\u77e5\u89c9\u4e4b\u6d77", "water", 180.0f, 150.0f, 3150.0f, 280.0f);
        PlanetAPI tundraPlanet = system.addPlanet("nova_regula_4", (SectorEntityToken)star, "\u5929\u6d25\u56db", "tundra", 270.0f, 120.0f, 4340.0f, 385.0f);
        PlanetAPI barrenBeforeToxic = system.addPlanet("nova_regula_before_toxic", (SectorEntityToken)star, "Arcanum", "barren", 300.0f, 90.0f, 4800.0f, 415.0f);
        PlanetAPI toxicPlanet = system.addPlanet("nova_regula_5", (SectorEntityToken)star, "Toxica", "toxic", 330.0f, 130.0f, 5600.0f, 490.0f);
        PlanetAPI bombardedAfterToxic = system.addPlanet("nova_regula_after_toxic", (SectorEntityToken)star, "\u7a7a\u5fc3\u7194\u7089", "barren-bombarded", 30.0f, 80.0f, 6400.0f, 530.0f);
        float outerBeltRadius = 8400.0f;
        float outerBeltWidth = 700.0f;
        system.addAsteroidBelt((SectorEntityToken)star, 150, outerBeltRadius, outerBeltWidth / 2.0f, outerBeltWidth / 2.0f, 265.0f);
        JumpPointAPI jumpPointOcean = Global.getFactory().createJumpPoint("nova_regula_jump_ocean", "\u5927\u4f24\u53e3");
        jumpPointOcean.setCircularOrbit((SectorEntityToken)oceanPlanet, 45.0f, 560.0f, 42.0f);
        jumpPointOcean.setStandardWormholeToHyperspaceVisual();
        system.addEntity((SectorEntityToken)jumpPointOcean);
        CustomCampaignEntityAPI gate = system.addCustomEntity("nova_regula_gate", "\u7ec8\u7aef\u51fa\u53e3", "inactive_gate", (String)null);
        gate.setCircularOrbit((SectorEntityToken)star, 0.0f, 9100.0f, 700.0f);
        CustomCampaignEntityAPI commRelay = system.addCustomEntity("nova_regula_comm_relay", "\u96f7\u53e4\u62c9\u901a\u8baf\u4e2d\u7ee7\u5668", "comm_relay", (String)null);
        commRelay.setCircularOrbit((SectorEntityToken)star, 120.0f, 7700.0f, 560.0f);
        CustomCampaignEntityAPI navBuoy = system.addCustomEntity("nova_regula_nav_buoy", "\u96f7\u53e4\u62c9\u5bfc\u822a\u6d6e\u6807", "nav_buoy", (String)null);
        navBuoy.setCircularOrbit((SectorEntityToken)star, 240.0f, 7700.0f, 560.0f);
        system.autogenerateHyperspaceJumpPoints(true, true, true);
        try {
            system.addRingBand((SectorEntityToken)gasGiant, "misc", "rings_dust0", 180.0f, 2, Color.white, 180.0f, 455.0f, 32.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)gasGiant, "misc", "rings_dust0", 180.0f, 3, Color.white, 180.0f, 560.0f, 35.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)star, "misc", "rings_dust0", outerBeltWidth / 2.0f, 3, new Color(200, 200, 255, 120), outerBeltWidth / 2.0f, outerBeltRadius - outerBeltWidth / 4.0f, 290.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)star, "misc", "rings_asteroids0", outerBeltWidth / 2.0f, 3, new Color(180, 180, 220, 100), outerBeltWidth / 2.0f, outerBeltRadius + outerBeltWidth / 4.0f, 280.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)tundraPlanet, "misc", "rings_ice0", 60.0f, 2, new Color(200, 220, 255, 100), 60.0f, 210.0f, 85.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)toxicPlanet, "misc", "rings_dust0", 70.0f, 3, new Color(150, 200, 150, 120), 70.0f, 280.0f, 105.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)barrenBeforeToxic, "misc", "rings_dust0", 50.0f, 2, new Color(180, 160, 140, 100), 50.0f, 180.0f, 90.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)bombardedAfterToxic, "misc", "rings_asteroids0", 60.0f, 3, new Color(140, 120, 100, 120), 60.0f, 200.0f, 95.0f, "ring", (String)null);
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            system.addRingBand((SectorEntityToken)star, "misc", "rings_dust0", 100.0f, 2, new Color(100, 150, 255, 100), 100.0f, 900.0f, 65.0f);
        }
        catch (Exception exception) {
            // empty catch block
        }
        Global.getLogger(CustomSystemGenerator.class).info((Object)"NOVA_REGULA \u5b9e\u4f53\u751f\u6210\u5b8c\u6210\uff0c\u7b49\u5f85\u5e02\u573a JSON \u6302\u8f7d\u3002");
    }
}

