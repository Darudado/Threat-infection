/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fs.starfarer.api.EveryFrameScript
 *  com.fs.starfarer.api.Global
 *  com.fs.starfarer.api.campaign.BattleAPI
 *  com.fs.starfarer.api.campaign.CampaignEventListener$FleetDespawnReason
 *  com.fs.starfarer.api.campaign.CampaignFleetAPI
 *  com.fs.starfarer.api.campaign.FactionAPI$ShipPickMode
 *  com.fs.starfarer.api.campaign.SectorEntityToken
 *  com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
 *  com.fs.starfarer.api.campaign.econ.Industry
 *  com.fs.starfarer.api.campaign.econ.Industry$IndustryTooltipMode
 *  com.fs.starfarer.api.campaign.econ.MarketAPI
 *  com.fs.starfarer.api.campaign.listeners.FleetEventListener
 *  com.fs.starfarer.api.campaign.rules.MemoryAPI
 *  com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
 *  com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase
 *  com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase$PatrolFleetData
 *  com.fs.starfarer.api.impl.campaign.fleets.FleetFactory$PatrolType
 *  com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
 *  com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
 *  com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4
 *  com.fs.starfarer.api.impl.campaign.fleets.RouteManager
 *  com.fs.starfarer.api.impl.campaign.fleets.RouteManager$OptionalFleetData
 *  com.fs.starfarer.api.impl.campaign.fleets.RouteManager$RouteData
 *  com.fs.starfarer.api.impl.campaign.fleets.RouteManager$RouteFleetSpawner
 *  com.fs.starfarer.api.impl.campaign.fleets.RouteManager$RouteSegment
 *  com.fs.starfarer.api.impl.campaign.ids.Ranks
 *  com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD$RaidDangerLevel
 *  com.fs.starfarer.api.ui.TooltipMakerAPI
 *  com.fs.starfarer.api.util.IntervalUtil
 *  com.fs.starfarer.api.util.Misc
 *  com.fs.starfarer.api.util.Pair
 *  com.fs.starfarer.api.util.WeightedRandomPicker
 *  org.lwjgl.util.vector.Vector2f
 */
package data.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.awt.Color;
import java.util.Random;
import org.lwjgl.util.vector.Vector2f;

public class ThreatControlHQ
extends BaseIndustry
implements RouteManager.RouteFleetSpawner,
FleetEventListener {
    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f, Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
    protected float returningPatrolValue = 0.0f;
    private static final String NANITES = "nanites";
    private static final int NANITES_DEMAND_MOD = 2;
    public static float FLEET_SIZE_BONUS = 0.5f;
    private static final String FLEET_SIZE_MOD_ID_PREFIX = "threat_hq_fleet_size";

    public boolean isHidden() {
        return false;
    }

    public boolean isFunctional() {
        if (!super.isFunctional()) {
            return false;
        }
        Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
        return nanitesDeficit == null || (Integer)nanitesDeficit.two <= 0;
    }

    public void apply() {
        Pair nanitesDeficit;
        super.apply(true);
        int size = this.market.getSize();
        this.demand("supplies", size - 1);
        this.demand("fuel", size - 1);
        this.demand("ships", size - 1);
        this.demand("hand_weapons", size);
        this.demand(NANITES, size + 2);
        this.modifyStabilityWithBaseMod();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$patrol", (String)this.getModId(), (boolean)true, (float)-1.0f);
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$military", (String)this.getModId(), (boolean)true, (float)-1.0f);
        float mult = this.getDeficitMult(new String[]{"supplies", "fuel", "ships", "hand_weapons", NANITES});
        Object extra = "";
        if (mult != 1.0f) {
            String com = (String)this.getMaxDeficit((String[])new String[]{"supplies", "fuel", "ships", "hand_weapons", NANITES}).one;
            extra = " (" + ThreatControlHQ.getDeficitText((String)com).toLowerCase() + ")";
        }
        String fleetSizeModId = "threat_hq_fleet_size_" + this.getModId();
        float actualBonus = FLEET_SIZE_BONUS * mult;
        this.market.getStats().getDynamic().getMod("combat_fleet_size_mult").modifyFlat(fleetSizeModId, actualBonus, this.getNameForModifier() + (String)extra);
        if (Global.getSettings().isDevMode()) {
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("ThreatControlHQ: Applied fleet size bonus for market %s. ModId: %s, Base Bonus: %.1f%%, Actual Bonus: %.1f%%, Multiplier: %.2f", this.market.getName(), fleetSizeModId, Float.valueOf(FLEET_SIZE_BONUS * 100.0f), Float.valueOf(actualBonus * 100.0f), Float.valueOf(1.0f + actualBonus)));
            float currentValue = this.market.getStats().getDynamic().getMod("combat_fleet_size_mult").computeEffective(0.0f);
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("Current combat_fleet_size_mult for market %s: %.2f", this.market.getName(), Float.valueOf(currentValue)));
        }
        if ((nanitesDeficit = this.getMaxDeficit(new String[]{NANITES})) != null && (Integer)nanitesDeficit.two > 0 && !this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        }
        if (!this.isFunctional()) {
            this.supply.clear();
            this.unapply();
        }
    }

    public void unapply() {
        super.unapply();
        MemoryAPI memory = this.market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$patrol", (String)this.getModId(), (boolean)false, (float)-1.0f);
        Misc.setFlagWithReason((MemoryAPI)memory, (String)"$military", (String)this.getModId(), (boolean)false, (float)-1.0f);
        this.unmodifyStabilityWithBaseMod();
        String fleetSizeModId = "threat_hq_fleet_size_" + this.getModId();
        this.market.getStats().getDynamic().getMod("combat_fleet_size_mult").unmodifyFlat(fleetSizeModId);
        if (Global.getSettings().isDevMode()) {
            Global.getLogger(((Object)((Object)this)).getClass()).info((Object)String.format("ThreatControlHQ: Removed fleet size bonus for market %s. ModId: %s", this.market.getName(), fleetSizeModId));
        }
    }

    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional();
    }

    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        if (mode != Industry.IndustryTooltipMode.NORMAL || this.isFunctional()) {
            this.addStabilityPostDemandSection(tooltip, hasDemand, mode);
            this.addFleetSizeImpactSection(tooltip);
        }
    }

    protected void addFleetSizeImpactSection(TooltipMakerAPI tooltip) {
        String[] commodities = new String[]{"supplies", "fuel", "ships", "hand_weapons", NANITES};
        float mult = this.getDeficitMult(commodities);
        float actualBonus = FLEET_SIZE_BONUS * mult;
        Object extra = "";
        if (mult != 1.0f) {
            String com = (String)this.getMaxDeficit((String[])commodities).one;
            extra = " (" + ThreatControlHQ.getDeficitText((String)com).toLowerCase() + ")";
        }
        Color h = Misc.getHighlightColor();
        if (mult != 1.0f) {
            h = Misc.getNegativeHighlightColor();
        }
        tooltip.addPara("\u8230\u961f\u89c4\u6a21: +%s", 3.0f, h, new String[]{(int)(actualBonus * 100.0f) + "%" + (String)extra});
    }

    protected int getBaseStabilityMod() {
        return 2;
    }

    public String getNameForModifier() {
        return this.getSpec().getName().contains("HQ") ? this.getSpec().getName() : Misc.ucFirst((String)this.getSpec().getName());
    }

    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return this.getMaxDeficit(new String[]{"supplies", "fuel", "ships", "hand_weapons", NANITES});
    }

    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    protected void buildingFinished() {
        super.buildingFinished();
        this.tracker.forceIntervalElapsed();
    }

    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        this.tracker.forceIntervalElapsed();
    }

    public void advance(float amount) {
        super.advance(amount);
        if (!Global.getSector().getEconomy().isSimMode() && this.isFunctional()) {
            Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
            if (nanitesDeficit != null && (Integer)nanitesDeficit.two > 0) {
                return;
            }
            float days = Global.getSector().getClock().convertToDays(amount);
            float spawnRate = 1.0f;
            float rateMult = this.market.getStats().getDynamic().getStat("combat_fleet_spawn_rate_mult").getModifiedValue();
            spawnRate *= rateMult;
            float extraTime = 0.0f;
            if (this.returningPatrolValue > 0.0f) {
                float interval = this.tracker.getIntervalDuration();
                extraTime = interval * days;
                this.returningPatrolValue -= days;
                if (this.returningPatrolValue < 0.0f) {
                    this.returningPatrolValue = 0.0f;
                }
            }
            this.tracker.advance(days * spawnRate + extraTime);
            if (this.tracker.intervalElapsed()) {
                String sid = this.getRouteSourceId();
                int light = this.getCount(FleetFactory.PatrolType.FAST);
                int medium = this.getCount(FleetFactory.PatrolType.COMBAT);
                int heavy = this.getCount(FleetFactory.PatrolType.HEAVY);
                int maxLight = 2;
                int maxMedium = 2;
                int maxHeavy = 2;
                WeightedRandomPicker picker = new WeightedRandomPicker();
                picker.add((Object)FleetFactory.PatrolType.HEAVY, (float)(maxHeavy - heavy));
                picker.add((Object)FleetFactory.PatrolType.COMBAT, (float)(maxMedium - medium));
                picker.add((Object)FleetFactory.PatrolType.FAST, (float)(maxLight - light));
                if (picker.isEmpty()) {
                    return;
                }
                FleetFactory.PatrolType type = (FleetFactory.PatrolType)picker.pick();
                MilitaryBase.PatrolFleetData custom = new MilitaryBase.PatrolFleetData(type);
                RouteManager.OptionalFleetData extra = new RouteManager.OptionalFleetData(this.market);
                extra.fleetType = type.getFleetType();
                RouteManager.RouteData route = RouteManager.getInstance().addRoute(sid, this.market, Long.valueOf(Misc.genRandomSeed()), extra, (RouteManager.RouteFleetSpawner)this, (Object)custom);
                extra.strength = Float.valueOf(MilitaryBase.getPatrolCombatFP((FleetFactory.PatrolType)type, (Random)route.getRandom()));
                extra.strength = Float.valueOf(Misc.getAdjustedStrength((float)extra.strength.floatValue(), (MarketAPI)this.market));
                float patrolDays = 35.0f + (float)Math.random() * 10.0f;
                route.addSegment(new RouteManager.RouteSegment(patrolDays, this.market.getPrimaryEntity()));
            }
        }
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) {
    }

    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    public int getCount(FleetFactory.PatrolType ... types) {
        int count = 0;
        block0: for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(this.getRouteSourceId())) {
            if (!(data.getCustom() instanceof MilitaryBase.PatrolFleetData)) continue;
            MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)data.getCustom();
            FleetFactory.PatrolType[] var9 = types;
            int var8 = types.length;
            for (int var7 = 0; var7 < var8; ++var7) {
                FleetFactory.PatrolType type = var9[var7];
                if (type != custom.type) continue;
                ++count;
                continue block0;
            }
        }
        return count;
    }

    public int getMaxPatrols(FleetFactory.PatrolType type) {
        if (type == FleetFactory.PatrolType.FAST) {
            return (int)this.market.getStats().getDynamic().getMod("patrol_num_light_mod").computeEffective(0.0f);
        }
        if (type == FleetFactory.PatrolType.COMBAT) {
            return (int)this.market.getStats().getDynamic().getMod("patrol_num_medium_mod").computeEffective(0.0f);
        }
        return type == FleetFactory.PatrolType.HEAVY ? (int)this.market.getStats().getDynamic().getMod("patrol_num_heavy_mod").computeEffective(0.0f) : 0;
    }

    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData route) {
        return false;
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        RouteManager.RouteData route;
        if (this.isFunctional() && reason == CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION && (route = RouteManager.getInstance().getRoute(this.getRouteSourceId(), fleet)).getCustom() instanceof MilitaryBase.PatrolFleetData) {
            MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)route.getCustom();
            if (custom.spawnFP > 0) {
                float fraction = fleet.getFleetPoints() / custom.spawnFP;
                this.returningPatrolValue += fraction;
            }
        }
    }

    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {
        MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData)route.getCustom();
        FleetFactory.PatrolType type = custom.type;
        Random random = route.getRandom();
        float combat = 0.0f;
        float tanker = 0.0f;
        float freighter = 0.0f;
        String fleetType = type.getFleetType();
        switch (type) {
            case FAST: {
                combat = (float)Math.round(3.0f + random.nextFloat() * 2.0f) * 5.0f;
                break;
            }
            case COMBAT: {
                combat = (float)Math.round(6.0f + random.nextFloat() * 3.0f) * 5.0f;
                tanker = (float)Math.round(random.nextFloat()) * 5.0f;
                break;
            }
            case HEAVY: {
                combat = (float)Math.round(10.0f + random.nextFloat() * 5.0f) * 5.0f;
                tanker = (float)Math.round(random.nextFloat()) * 10.0f;
                freighter = (float)Math.round(random.nextFloat()) * 10.0f;
            }
        }
        String factionId = "threat_qr";
        String fleetFactionId = this.market.getFactionId();
        boolean isPlayerMarket = this.market.getFaction().isPlayerFaction();
        FleetParamsV3 params = new FleetParamsV3(this.market, (Vector2f)null, factionId, route.getQualityOverride(), fleetType, combat, freighter, tanker, 0.0f, 0.0f, 0.0f, 0.0f);
        params.timestamp = route.getTimestamp();
        params.random = random;
        params.modeOverride = Misc.getShipPickMode((MarketAPI)this.market);
        params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet((FleetParamsV3)params);
        if (fleet != null && !fleet.isEmpty()) {
            if (isPlayerMarket) {
                fleet.setFaction("player", true);
                fleet.getMemoryWithoutUpdate().set("$player_threat_fleet", (Object)true);
            } else {
                fleet.setFaction(fleetFactionId, true);
            }
            fleet.setNoFactionInName(false);
            fleet.addEventListener((FleetEventListener)this);
            fleet.getMemoryWithoutUpdate().set("$isPatrol", (Object)true);
            fleet.getMemoryWithoutUpdate().set("$cfai_ignoreOtherFleets", (Object)true, 0.3f);
            if (type == FleetFactory.PatrolType.FAST || type == FleetFactory.PatrolType.COMBAT) {
                fleet.getMemoryWithoutUpdate().set("$isCustomsInspector", (Object)true);
            }
            String postId = Ranks.POST_PATROL_COMMANDER;
            String rankId = Ranks.SPACE_COMMANDER;
            fleet.getCommander().setPostId(postId);
            fleet.getCommander().setRankId(rankId);
            String fleetName = this.getFleetNameByType(type);
            if (fleetName != null) {
                if (isPlayerMarket) {
                    fleet.setName("\u590d\u5236" + fleetName);
                } else {
                    fleet.setName(fleetName);
                }
            }
            this.market.getContainingLocation().addEntity((SectorEntityToken)fleet);
            fleet.setFacing((float)Math.random() * 360.0f);
            fleet.setLocation(this.market.getPrimaryEntity().getLocation().x, this.market.getPrimaryEntity().getLocation().y);
            fleet.addScript((EveryFrameScript)new PatrolAssignmentAIV4(fleet, route));
            if (custom.spawnFP <= 0) {
                custom.spawnFP = fleet.getFleetPoints();
            }
            return fleet;
        }
        return null;
    }

    private String getFleetNameByType(FleetFactory.PatrolType type) {
        switch (type) {
            case FAST: {
                return "\u5a01\u80c1\u76d1\u89c6\u96c6\u7fa4";
            }
            case COMBAT: {
                return "\u5a01\u80c1\u8ffd\u730e\u96c6\u7fa4";
            }
            case HEAVY: {
                return "\u5a01\u80c1\u96c6\u56e2\u519b";
            }
        }
        return null;
    }

    public String getRouteSourceId() {
        return this.getMarket().getId() + "_threat_hq";
    }

    public boolean isAvailableToBuild() {
        if (!Global.getSector().getPlayerFaction().knowsIndustry(this.getId())) {
            return false;
        }
        boolean hasMilitaryBase = this.market.hasIndustry("militarybase") || this.market.hasIndustry("patrolhq") || this.market.hasIndustry("highcommand");
        return this.market.getFaction().isPlayerFaction() && hasMilitaryBase;
    }

    public String getUnavailableReason() {
        boolean hasMilitaryBase;
        if (!Global.getSector().getPlayerFaction().knowsIndustry(this.getId())) {
            return "\u9700\u8981\u5a01\u80c1\u63a7\u5236\u603b\u90e8\u84dd\u56fe";
        }
        if (!this.market.getFaction().isPlayerFaction()) {
            return "\u53ea\u80fd\u5efa\u9020\u5728\u73a9\u5bb6\u63a7\u5236\u7684\u5e02\u573a";
        }
        boolean bl = hasMilitaryBase = this.market.hasIndustry("militarybase") || this.market.hasIndustry("patrolhq") || this.market.hasIndustry("highcommand");
        if (!hasMilitaryBase) {
            return "\u9700\u8981\u5148\u5efa\u9020\u519b\u4e8b\u57fa\u5730\u3001\u5de1\u903b\u603b\u90e8\u6216\u9ad8\u7ea7\u6307\u6325\u4e2d\u5fc3";
        }
        Pair nanitesDeficit = this.getMaxDeficit(new String[]{NANITES});
        if (nanitesDeficit != null && (Integer)nanitesDeficit.two > 0) {
            return "\u7eb3\u7c73\u788e\u7247\u4f9b\u5e94\u4e0d\u8db3\uff08\u77ed\u7f3a" + String.valueOf(nanitesDeficit.two) + "\u5355\u4f4d\uff09";
        }
        return super.getUnavailableReason();
    }

    public boolean showWhenUnavailable() {
        return Global.getSector().getPlayerFaction().knowsIndustry(this.getId());
    }

    public boolean canImprove() {
        return false;
    }

    public MarketCMD.RaidDangerLevel adjustCommodityDangerLevel(String commodityId, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }

    public MarketCMD.RaidDangerLevel adjustItemDangerLevel(String itemId, String data, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }

    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        super.addPostDescriptionSection(tooltip, mode);
        if (mode == Industry.IndustryTooltipMode.NORMAL && this.isFunctional()) {
            float opad = 10.0f;
            tooltip.addPara("\u8be5\u5efa\u7b51\u4e3a\u661f\u7403\u63d0\u4f9b50%%\u8230\u961f\u89c4\u6a21\u589e\u76ca\uff0c\u5e76\u751f\u6210\u5a01\u80c1\u6d3e\u7cfb\u7684\u5de1\u903b\u8230\u961f\u3002", opad, Misc.getHighlightColor(), new String[]{"50%"});
            int nanitesDemand = this.market.getSize() + 2;
            Color nanitesColor = Misc.getHighlightColor();
            if (this.getMaxDeficit(new String[]{NANITES}) != null && (Integer)this.getMaxDeficit((String[])new String[]{NANITES}).two > 0) {
                nanitesColor = Misc.getNegativeHighlightColor();
            }
            if (this.market.getFaction().isPlayerFaction()) {
                tooltip.addPara("\u7531\u4e8e\u8be5\u661f\u7403\u5c5e\u4e8e\u73a9\u5bb6\uff0c\u751f\u6210\u7684\u5a01\u80c1\u8230\u961f\u5c06\u81ea\u52a8\u52a0\u5165\u73a9\u5bb6\u9635\u8425\u3002", opad);
            }
        }
    }

    public boolean hasEffects() {
        return true;
    }

    public boolean isBeneficial() {
        return true;
    }
}

