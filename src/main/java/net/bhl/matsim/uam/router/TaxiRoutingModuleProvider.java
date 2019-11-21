package net.bhl.matsim.uam.router;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import java.util.Map;

/**
 * This class provides the routing module for Taxi mode.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class TaxiRoutingModuleProvider implements Provider<RoutingModule> {

	@Inject
	@Named("car")
	Network networkCar;
	@Inject
	private Scenario scenario;
	@Inject
	private LeastCostPathCalculatorFactory lcpcf;
	@Inject
	private Map<String, TravelTime> travelTimes;
	@Inject
	private Map<String, TravelDisutilityFactory> travelDisutilityFactories;

	@Override
	public RoutingModule get() {
		TravelTime travelTime = travelTimes.get(TransportMode.car);

		TravelDisutility travelDisutility = travelDisutilityFactories.get(TransportMode.car)
				.createTravelDisutility(travelTime);

		LeastCostPathCalculator pathCalculator = lcpcf.createPathCalculator(networkCar, travelDisutility, travelTime);

		return new TaxiRoutingModule(scenario, pathCalculator, networkCar);

		//return new UAMIntermodalRoutingModule(this.scenario, this.uamManager.getStations(), modes, this.plcpc,
		//		pathCalculator, networkCar, transitRouter, transitWalkRouter, uamConfig, transitConfig, transitRouting,
		//		parameters, waitingData);
	}
}