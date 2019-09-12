package net.bhl.matsim.uam.router;

import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.pt.config.TransitConfigGroup;
import org.matsim.pt.router.TransitRouter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.baseline_scenario.transit.routing.BaselineTransitRoutingModule;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionFinder;

public class UAMRoutingModuleProvider implements Provider<RoutingModule> {

	@Inject
	private Scenario scenario;
	@Inject
	private UAMManager uamManager;
	@Inject
	@Named("uam")
	private ParallelLeastCostPathCalculator plcpc;

	@Inject
	@Named("car")
	Network networkCar;
	@Inject
	private LeastCostPathCalculatorFactory lcpcf;
	@Inject
	private Map<String, TravelTime> travelTimes;
	@Inject
	private Map<String, TravelDisutilityFactory> travelDisutilityFactories;

	@Inject
	private UAMConfigGroup uamConfig;
	@Inject
	private TransitConfigGroup transitConfig;

	@Inject(optional = true)
	private TransitRouter transitRouter;
	@Inject(optional = true)
	BaselineTransitRoutingModule transitRouting;

	@Inject
	CustomModeChoiceParameters parameters;

	@Inject
	private WaitingStationData waitingData;

	@Inject
	private UAMStationConnectionGraph stationConnectionutilities;

	@Inject
	private SubscriptionFinder subscriptionFinder;

	@Override
	public RoutingModule get() {
		Set<String> modes = uamConfig.getAvailableAccessModes();
		TravelTime travelTime = travelTimes.get(TransportMode.car);

		TravelDisutility travelDisutility = travelDisutilityFactories.get(TransportMode.car)
				.createTravelDisutility(travelTime);

		LeastCostPathCalculator pathCalculator = lcpcf.createPathCalculator(networkCar, travelDisutility, travelTime);

		// returns the OptimizedUAMIntermodalRoutingModule containing or not transit routing modules
		if (scenario.getConfig().transit().isUseTransit()) {
			return new UAMIntermodalRoutingModule(scenario, uamManager.getStations(), modes, plcpc, pathCalculator,
					networkCar, transitRouter, uamConfig, transitConfig, transitRouting, parameters, waitingData,
					stationConnectionutilities, subscriptionFinder);
		} else {
			return new UAMIntermodalRoutingModule(scenario, uamManager.getStations(), modes, plcpc, pathCalculator,
					networkCar, uamConfig, transitConfig, parameters, waitingData, stationConnectionutilities,
					subscriptionFinder);
		}
	}
}