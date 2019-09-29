package net.bhl.matsim.uam.router.strategy;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMRoutes;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.router.UAMIntermodalRoutingModule;

/**
 * This class uses the strategy selected in the config file to generate the
 * routes for agents using UAM.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class UAMStrategyRouter {
    private final Scenario scenario;
    private UAMConfigGroup uamConfig;
    private UAMStations landingStations;
    private ParallelLeastCostPathCalculator plcpc;
    private LeastCostPathCalculator plcpccar;
    private Network carNetwork;
    private TransitRouter transitRouter;
    private UAMStationConnectionGraph stationConnectionutilities;
    private CustomModeChoiceParameters parameters;
    private static final Logger log = Logger.getLogger(UAMIntermodalRoutingModule.class);
    private UAMStrategy strategy;

    public UAMStrategyRouter(
            Scenario scenario, UAMConfigGroup uamConfig, CustomModeChoiceParameters parameters,
            ParallelLeastCostPathCalculator plcpc, LeastCostPathCalculator plcpccar, UAMStations landingStations, Network carNetwork,
            UAMStationConnectionGraph stationConnectionutilities) {
        this.scenario = scenario;
        this.uamConfig = uamConfig;
        this.parameters = parameters;
        this.plcpc = plcpc;
        this.plcpccar = plcpccar;
        this.landingStations = landingStations;
        this.carNetwork = carNetwork;
        this.stationConnectionutilities = stationConnectionutilities;
    }

    public UAMStrategyRouter(
            TransitRouter transitRouter,
            Scenario scenario, UAMConfigGroup uamConfig, CustomModeChoiceParameters parameters,
            ParallelLeastCostPathCalculator plcpc, LeastCostPathCalculator plcpccar, UAMStations landingStations, Network carNetwork,
            UAMStationConnectionGraph stationConnectionutilities) {
        this(scenario, uamConfig, parameters, plcpc, plcpccar, landingStations, carNetwork, stationConnectionutilities);
        this.transitRouter = transitRouter;
    }

    public boolean estimateUAMRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime) {
        try {
            if (strategy == null)
                this.setStrategy();
            UAMRoutes.getInstance().add(person.getId(), departureTime,
                    strategy.getRoute(person, fromFacility, toFacility, departureTime));
            return true;
        } catch (NullPointerException e) {
            // do not store best UAM route if any part of the UAM trip cannot be estimated
            // (e.g. no destination station)
            return false;
        }
    }

    /**
     * This method instantiate the strategy according to the parameter set in the Config file. Any new strategy class
	 * created has to be added here.
     *
     * @throws Exception
     */
    private void setStrategy() {
        UAMStrategyUtils strategyUtils = new UAMStrategyUtils(this.landingStations, this.uamConfig, this.scenario,
				this.stationConnectionutilities, this.carNetwork, this.transitRouter, this.plcpc, this.plcpccar,
				this.parameters);
        log.info("Setting UAM routing strategy to " + uamConfig.getUAMRoutingStrategy());
        switch (uamConfig.getUAMRoutingStrategy()) {
            case MAXUTILITY:
                this.strategy = new UAMMaxUtilityStrategy(strategyUtils, parameters);
                return;
            case MAXACCESSUTILITY:
                this.strategy = new UAMMaxAccessUtilityStrategy(strategyUtils);
                return;
            case MINTRAVELTIME:
                this.strategy = new UAMMinTravelTimeStrategy(strategyUtils);
                return;
            case MINACCESSTRAVELTIME:
                this.strategy = new UAMMinAccessTravelTimeStrategy(strategyUtils);
                return;
            case MINDISTANCE:
                this.strategy = new UAMMinDistanceStrategy(strategyUtils);
                return;
            case MINACCESSDISTANCE:
                this.strategy = new UAMMinAccessDistanceStrategy(strategyUtils);
                return;
            case PREDEFINED:
                this.strategy = new UAMPredefinedStrategy(strategyUtils);
		}
    }
}
