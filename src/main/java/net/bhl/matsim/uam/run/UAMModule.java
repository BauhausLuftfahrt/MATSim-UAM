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
import net.bhl.matsim.uam.data.UAMFleetData;
import net.bhl.matsim.uam.data.UAMLoader;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.UAMDemand;
import net.bhl.matsim.uam.listeners.ParallelLeastCostPathCalculatorShutdownListener;
import net.bhl.matsim.uam.listeners.UAMListener;
import net.bhl.matsim.uam.listeners.UAMShutdownListener;
import net.bhl.matsim.uam.qsim.UAMQSimPlugin;
import net.bhl.matsim.uam.router.UAMMainModeIdentifier;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.router.UAMRoutingModuleProvider;
import net.bhl.matsim.uam.scoring.UAMScoringFunctionFactory;
import net.bhl.matsim.uam.transit.simulation.UAMTransitPlugin;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.dynagent.run.DynActivityEnginePlugin;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.PopulationPlugin;
import org.matsim.core.mobsim.qsim.TeleportationPlugin;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import org.matsim.core.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A MATSim Abstract Module for the classes used by the UAM simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMModule extends AbstractModule {

	private UAMManager uamManager;
	private Scenario scenario;
	private Network networkUAM;
	private Network networkCar;

	public UAMModule(UAMManager uamManager, Scenario scenario, Network networkUAM, Network networkCar) {
		this.uamManager = uamManager;
		this.scenario = scenario;
		this.networkUAM = networkUAM;
		this.networkCar = networkCar;
	}

	@Override
	public void install() {

		// bining our own scoring function factory
		bind(ScoringFunctionFactory.class).to(UAMScoringFunctionFactory.class).asEagerSingleton();

		// binding UAMManager
		bind(UAMManager.class).toInstance(uamManager);
		// adding UAMDemand as singleton
		bind(UAMDemand.class).asEagerSingleton();

		addControlerListenerBinding().to(UAMLoader.class);

		// controler listeners need to be binded
		addControlerListenerBinding().toInstance(uamManager);
		addControlerListenerBinding().to(UAMListener.class);
		addControlerListenerBinding().to(UAMShutdownListener.class);
		addControlerListenerBinding().to(WaitingStationData.class);

		addControlerListenerBinding()
				.to(Key.get(ParallelLeastCostPathCalculatorShutdownListener.class, Names.named("uam")));

		bind(WaitingStationData.class).asEagerSingleton();
		// bindng of event handlers
		addEventHandlerBinding().to(UAMDemand.class);
		// addEventHandlerBinding().to(UAMEventHandler.class);

		// we need to bind our router for the uam trips
		addRoutingModuleBinding(UAMModes.UAM_MODE)
				.toProvider(UAMRoutingModuleProvider.class);

		// we still need to provide a way to identify our trips
		// as being uam trips.
		// This is for instance used at re-routing.
		bind(MainModeIdentifier.class).toInstance(new UAMMainModeIdentifier(new MainModeIdentifierImpl()));

		// here we provide vehicles and network to be used for uam trips
		bind(VehicleType.class).annotatedWith(Names.named("uam")).toInstance(VehicleUtils.getDefaultVehicleType());

		bind(TravelTime.class).annotatedWith(Names.named("uam"))
				.to(Key.get(TravelTime.class, Names.named(DvrpTravelTimeModule.DVRP_ESTIMATED)));

		bind(Network.class).annotatedWith(Names.named(DvrpModule.DVRP_ROUTING)).toInstance(this.networkUAM);
		bind(Network.class).annotatedWith(Names.named("car")).toInstance(this.networkCar);

		bind(Network.class).annotatedWith(Names.named("uam"))
				.to(Key.get(Network.class, Names.named(DvrpModule.DVRP_ROUTING)));

		// addControlerListenerBinding()
		// .to(Key.get(ParallelLeastCostPathCalculatorShutdownListener.class,
		// Names.named("uam")));
	}

	@Provides
	@Singleton
	public UAMFleetData provideData() {
		UAMFleetData data = new UAMFleetData();

		for (Vehicle veh : this.uamManager.getVehicles().values())
			data.addVehicle(veh);
		return data;
	}

	@Provides
	@Singleton
	@Named("uam")
	private ParallelLeastCostPathCalculator provideParallelLeastCostPathCalculator(UAMConfigGroup uamConfig,
																				   @Named("uam") Network network, @Named("uam") TravelTime travelTime) {
		// TODO: make this parameterized
		int paralelRouters = uamConfig.getParallelRouters();
		if (1 == paralelRouters) {
			return new SerialLeastCostPathCalculator(new DijkstraFactory().createPathCalculator(network,
					new OnlyTimeDependentTravelDisutility(travelTime), travelTime));
		} else {
			// TODO CHECK if this is really UAM only network
			return DefaultParallelLeastCostPathCalculator.create(paralelRouters, new DijkstraFactory(), network,
					new OnlyTimeDependentTravelDisutility(travelTime), travelTime);
		}
	}

	@Provides
	@Singleton
	@Named("uam")
	private ParallelLeastCostPathCalculatorShutdownListener provideParallelLeastCostPathCalculatorShutdownListener(
			@Named("uam") ParallelLeastCostPathCalculator calculator) {
		return new ParallelLeastCostPathCalculatorShutdownListener(calculator);
	}

	@Provides
	@Singleton
	public Collection<AbstractQSimPlugin> provideQSimPlugins(Config config) {
		final Collection<AbstractQSimPlugin> plugins = new ArrayList<>();

		plugins.add(new MessageQueuePlugin(config));
		plugins.add(new DynActivityEnginePlugin(config));
		plugins.add(new QNetsimEnginePlugin(config));

		if (config.network().isTimeVariantNetwork()) {
			plugins.add(new NetworkChangeEventsPlugin(config));
		}

		// TODO include config switch
		// plugins.add(new BaselineTransitPlugin(config));

		if (scenario.getConfig().transit().isUseTransit()) {
			plugins.add(new UAMTransitPlugin(config));
		}

		plugins.add(new TeleportationPlugin(config));
		plugins.add(new PopulationPlugin(config));
		plugins.add(new UAMQSimPlugin(config));

		return plugins;
	}

}
