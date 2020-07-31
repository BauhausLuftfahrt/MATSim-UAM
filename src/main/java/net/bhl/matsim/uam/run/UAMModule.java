package net.bhl.matsim.uam.run;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.SerialLeastCostPathCalculator;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.UAMDemand;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import net.bhl.matsim.uam.listeners.ParallelLeastCostPathCalculatorShutdownListener;
import net.bhl.matsim.uam.listeners.UAMListener;
import net.bhl.matsim.uam.listeners.UAMShutdownListener;
import net.bhl.matsim.uam.qsim.UAMQsimModule;
import net.bhl.matsim.uam.router.UAMMainModeIdentifier;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.router.UAMRoutingModuleProvider;
import net.bhl.matsim.uam.scoring.UAMScoringFunctionFactory;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.DefaultPassengerRequestValidator;
import org.matsim.contrib.dvrp.passenger.PassengerRequestValidator;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSourceQSimModule;
import org.matsim.contrib.dynagent.run.DynActivityEngineModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

/**
 * A MATSim Abstract Module for the classes used by the UAM simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMModule extends AbstractModule {

	private UAMManager uamManager;
	private Network networkUAM;
	private Network networkCar;
	private UAMXMLReader uamReader;

	public UAMModule(UAMManager uamManager, Network networkUAM, Network networkCar, UAMXMLReader uamReader) {
		this.uamManager = uamManager;
		this.networkUAM = networkUAM;
		this.networkCar = networkCar;
		this.uamReader = uamReader;
	}

	@Override
	public void install() {
		bind(DvrpModes.key(PassengerRequestValidator.class, UAMModes.uam))
				.toInstance(new DefaultPassengerRequestValidator());
		installQSimModule(new UAMQsimModule(uamReader, uamManager));
		installQSimModule(new DynActivityEngineModule());

		// bining our own scoring function factory
		bind(ScoringFunctionFactory.class).to(UAMScoringFunctionFactory.class).asEagerSingleton();

		// binding UAMManager
		bind(UAMManager.class).toInstance(uamManager);
		// adding UAMDemand as singleton
		bind(UAMDemand.class).asEagerSingleton();

		// controler listeners need to be binded
		addControlerListenerBinding().toInstance(uamManager);
		addControlerListenerBinding().to(UAMListener.class);
		addControlerListenerBinding().to(UAMShutdownListener.class);
		addControlerListenerBinding().to(WaitingStationData.class);

		addControlerListenerBinding()
				.to(Key.get(ParallelLeastCostPathCalculatorShutdownListener.class, Names.named(UAMModes.uam)));

		bind(WaitingStationData.class).asEagerSingleton();
		// bindng of event handlers
		addEventHandlerBinding().to(UAMDemand.class);

		// we need to bind our router for the uam trips
		addRoutingModuleBinding(UAMModes.uam).toProvider(UAMRoutingModuleProvider.class);

		// we still need to provide a way to identify our trips
		// as being uam trips.
		// This is for instance used at re-routing.
		bind(MainModeIdentifier.class).toInstance(new UAMMainModeIdentifier(new MainModeIdentifierImpl()));

		// here we provide vehicles and network to be used for uam trips
		bind(VehicleType.class).annotatedWith(Names.named(UAMModes.uam)).toInstance(VehicleUtils.getDefaultVehicleType());

		bind(VehicleType.class).annotatedWith(Names.named(VrpAgentSourceQSimModule.DVRP_VEHICLE_TYPE))
				.toInstance(VehicleUtils.getDefaultVehicleType());

		bind(TravelTime.class).annotatedWith(Names.named(UAMModes.uam))
				.to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

		bind(Network.class).annotatedWith(Names.named(DvrpRoutingNetworkProvider.DVRP_ROUTING))
				.toInstance(this.networkUAM);
		bind(Network.class).annotatedWith(Names.named("car")).toInstance(this.networkCar);

		bind(Network.class).annotatedWith(Names.named(UAMModes.uam))
				.to(Key.get(Network.class, Names.named(DvrpRoutingNetworkProvider.DVRP_ROUTING)));
	}

	@Provides
	@Singleton
	@Named(UAMModes.uam)
	private ParallelLeastCostPathCalculator provideParallelLeastCostPathCalculator(UAMConfigGroup uamConfig,
																				   @Named(UAMModes.uam) TravelTime travelTime) {
		int parallelRouters = uamConfig.getParallelRouters();
		if (1 == parallelRouters) {
			return new SerialLeastCostPathCalculator(new DijkstraFactory().createPathCalculator(networkUAM,
					new OnlyTimeDependentTravelDisutility(travelTime), travelTime));
		} else {
			return DefaultParallelLeastCostPathCalculator.create(parallelRouters, new DijkstraFactory(), networkUAM,
					new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
		}
	}

	@Provides
	@Singleton
	@Named(UAMModes.uam)
	private ParallelLeastCostPathCalculatorShutdownListener provideParallelLeastCostPathCalculatorShutdownListener(
			@Named(UAMModes.uam) ParallelLeastCostPathCalculator calculator) {
		return new ParallelLeastCostPathCalculatorShutdownListener(calculator);
	}

	@Provides
	@Singleton
	public UAMStationConnectionGraph provideUAMStationConnectionGraph(UAMManager uamManager, @Named(UAMModes.uam) ParallelLeastCostPathCalculator plcpc) {
		return new UAMStationConnectionGraph(uamManager, plcpc);
	}


}
