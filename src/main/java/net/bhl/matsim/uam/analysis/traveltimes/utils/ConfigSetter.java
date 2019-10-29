package net.bhl.matsim.uam.analysis.traveltimes.utils;

import ch.sbb.matsim.routing.pt.raptor.RaptorStaticConfig;
import ch.sbb.matsim.routing.pt.raptor.RaptorUtils;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

public class ConfigSetter {
    public static Config createCarConfig(String networkInput, String networkEventsChangeFile) {
        Config config = ConfigUtils.createConfig(new UAMConfigGroup(), new DvrpConfigGroup());
        config.network().setInputFile(networkInput);

        config.network().setTimeVariantNetwork(true);
        config.network().setChangeEventsInputFile(networkEventsChangeFile);

        return config;
    }

    public static Config createPTConfig(String networkInput, String transitScheduleInput, String transitVehiclesInput) {
        Config config = ConfigUtils.createConfig(new UAMConfigGroup(), new DvrpConfigGroup());
        config.network().setInputFile(networkInput);

        config.transit().setTransitScheduleFile(transitScheduleInput);
        config.transit().setVehiclesFile(transitVehiclesInput);

        config.transitRouter().setSearchRadius(2500);
        config.planCalcScore().setPerforming_utils_hr(0);
        config.planCalcScore().setMarginalUtlOfWaitingPt_utils_hr(-2.3);
        config.planCalcScore().setUtilityOfLineSwitch(-0.17);
        config.transitRouter().setExtensionRadius(500);

        PlanCalcScoreConfigGroup.ModeParams accessWalk = new PlanCalcScoreConfigGroup.ModeParams(
                TransportMode.access_walk);
        accessWalk.setMarginalUtilityOfTraveling(-4.0);
        config.planCalcScore().addModeParams(accessWalk);
        PlanCalcScoreConfigGroup.ModeParams egressWalk = new PlanCalcScoreConfigGroup.ModeParams(
                TransportMode.egress_walk);
        egressWalk.setMarginalUtilityOfTraveling(-4.0);
        config.planCalcScore().addModeParams(egressWalk);

        config.planCalcScore().getOrCreateModeParams(TransportMode.pt).setMarginalUtilityOfTraveling(-1.32);
        config.planCalcScore().getOrCreateModeParams(TransportMode.walk).setMarginalUtilityOfTraveling(-6.46);
        config.transit().setUseTransit(true);

        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.walk).setTeleportedModeSpeed(1.2);
        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.walk).setBeelineDistanceFactor(1.3);
        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.bike).setTeleportedModeSpeed(3.1);
        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.bike).setBeelineDistanceFactor(1.4);

        return config;
    }

    public static Config createUAMConfig(String networkInput, String networkEventsChangeFile, String transitScheduleInput,
                                       String transitVehiclesInput, double searchRadius, String accessModes) {
        Config config = createPTConfig(networkInput, transitScheduleInput, transitVehiclesInput);

        config.network().setTimeVariantNetwork(true);
        config.network().setChangeEventsInputFile(networkEventsChangeFile);

        ((UAMConfigGroup) config.getModules().get(UAMModes.UAM_MODE)).setAvailableAccessModes(accessModes);
        ((UAMConfigGroup) config.getModules().get(UAMModes.UAM_MODE)).setSearchRadius("" + searchRadius);
        return config;
    }

    public static RaptorStaticConfig createRaptorConfig(Config config) {
        RaptorStaticConfig raptorStaticConfig = RaptorUtils.createStaticConfig(config);
        raptorStaticConfig.setBeelineWalkSpeed(0.9230769);
        raptorStaticConfig.setMinimalTransferTime(0);
        raptorStaticConfig.setBeelineWalkConnectionDistance(250);
        raptorStaticConfig.setMarginalUtilityOfTravelTimeAccessWalk_utl_s(-0.0017944);
        raptorStaticConfig.setMarginalUtilityOfTravelTimeEgressWalk_utl_s(-0.0017944);
        raptorStaticConfig.setMarginalUtilityOfTravelTimeWalk_utl_s(-0.0017944);
        return raptorStaticConfig;
    }
}
