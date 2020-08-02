package net.bhl.matsim.uam.analysis.traveltimes;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.sbb.matsim.routing.pt.raptor.*;
import net.bhl.matsim.uam.analysis.traveltimes.utils.ThreadCounter;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItem;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItemReader;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import net.bhl.matsim.uam.run.UAMConstants;
import net.bhl.matsim.uam.router.strategy.*;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.*;
import org.matsim.core.router.util.*;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.facilities.Facility;
import org.matsim.pt.router.TransitRouter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This script generates csv file containing estimated travel times by UAM for
 * trips. The trips file must contain departure time and origin and destination
 * coordinates for the trips. Necessary inputs are in the following order:
 * -Network file; -UAM vehicles file -Transit Schedule file; -Transit Vehicles
 * file; -Trips file; -strategy name(minTraveltime, minDistance,
 * minAccessTravelTime, minAccessDistance) -output file; ;
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class RunCalculateUAMTravelTimes {
	private static final int processes = Runtime.getRuntime().availableProcessors();
	private static final Logger log = Logger.getLogger(RunCalculateUAMTravelTimes.class);
	private static ArrayBlockingQueue<LeastCostPathCalculator> carRouters = new ArrayBlockingQueue<>(processes);
	private static ArrayBlockingQueue<TransitRouter> ptRouters = new ArrayBlockingQueue<>(processes);
	private static ArrayBlockingQueue<DefaultParallelLeastCostPathCalculator> uamRouters = new ArrayBlockingQueue<>(processes);

	public static void main(String[] args) throws Exception {
		System.out.println("ARGS: config.xml* tripsCoordinateFile.csv* outputfile-name*");
		System.out.println("(* required)");

		log.warn("UAM process times are being ignored! All passenger processes are set to duration of 0.");

		// ARGS
		int j = 0;
		String configInput = args[j++];
		String tripsInput = args[j++];
		String outputPath = args[j];

		UAMConfigGroup uamConfigGroup = new UAMConfigGroup();
		Config config = ConfigUtils.loadConfig(configInput, uamConfigGroup, new DvrpConfigGroup());

		// Build scenario
		Scenario scenario = ScenarioUtils.createScenario(config);
		ScenarioUtils.loadScenario(scenario);
		Network network = scenario.getNetwork();

		// CREATE CAR/UAM NETWORK
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Set<String> modes = new HashSet<>();
		modes.add(UAMConstants.uam);
		Network networkUAM = NetworkUtils.createNetwork();
		filter.filter(networkUAM, modes);
		Network networkCar = NetworkUtils.createNetwork();
		Set<String> modesCar = new HashSet<>();
		modesCar.add(TransportMode.car);
		filter.filter(networkCar, modesCar);

		// SETUP UAM MANAGER AND STATIONCONENCTIONUTILITIES
		UAMXMLReader uamReader = new UAMXMLReader(networkUAM);
		uamReader.readFile(ConfigGroup.getInputFileURL(config.getContext(), uamConfigGroup.getUAM())
				.getPath().replace("%20", " "));
		UAMManager uamManager = new UAMManager(network);
		uamManager.setStations(new UAMStations(uamReader.getStations(), network));
		uamManager.setVehicles(uamReader.getVehicles());

		// data for parallel public transport router
		SwissRailRaptorData data = SwissRailRaptorData.create(scenario.getTransitSchedule(),
				RaptorUtils.createStaticConfig(config), network);

		// Generate data for other routers
		TravelTimeCalculator.Builder builder = new TravelTimeCalculator.Builder(network);
		builder.configure(config.travelTimeCalculator());
		TravelTimeCalculator ttc = builder.build();
		TravelTime travelTime = ttc.getLinkTravelTimes();
		TravelDisutility travelDisutility = TravelDisutilityUtils.createFreespeedTravelTimeAndDisutility(config.planCalcScore());

		com.google.inject.Injector injector = Injector.createInjector(config, new AbstractModule() {
			@Override
			public void install() {
				bind(LeastCostPathCalculatorFactory.class).to(AStarLandmarksFactory.class);
			}
		});
		LeastCostPathCalculatorFactory pathCalculatorFactory = injector
				.getInstance(LeastCostPathCalculatorFactory.class); // AStarLandmarksFactory

		// This router is used only to create the UAMStationConnectionGraph class
		DefaultParallelLeastCostPathCalculator pathCalculatorForStations = DefaultParallelLeastCostPathCalculator
				.create(processes, new DijkstraFactory(), networkUAM,
						travelDisutility, travelTime);
		UAMStationConnectionGraph stationConnectionutilities = new UAMStationConnectionGraph(uamManager,
				pathCalculatorForStations);
		pathCalculatorForStations.close();

		//Provide routers
		for (int i = 0; i < processes; i++) {
			carRouters.add(pathCalculatorFactory.createPathCalculator(networkCar, travelDisutility, travelTime));
			Map<String, RoutingModule> router = new HashMap<>();
			router.put(TransportMode.pt, new TeleportationRoutingModule(TransportMode.pt,
					scenario.getPopulation().getFactory(), 0, 1.5));
			ptRouters.add(new SwissRailRaptor(data, new DefaultRaptorParametersForPerson(config),
					new LeastCostRaptorRouteSelector(),
					new DefaultRaptorStopFinder(null, new DefaultRaptorIntermodalAccessEgress(), router)));
			uamRouters.add(DefaultParallelLeastCostPathCalculator.create(
					processes, new DijkstraFactory(), networkUAM, travelDisutility,
					travelTime));
		}

		// READ TRIPS INPUT
		List<TripItem> trips = TripItemReader.getTripItems(tripsInput);

		// Calculate travel times
		log.info("Calculating travel times...");
		int counter = 1;
		ThreadCounter threadCounter = new ThreadCounter();
		ExecutorService es = Executors.newFixedThreadPool(processes);
		for (TripItem trip : trips) {
			if (trips.size() < 100 || counter % (trips.size() / 100) == 0)
				log.info("Calculation completion: " + counter + "/" + trips.size() + " ("
						+ String.format("%.0f", (double) counter / trips.size() * 100) + "%).");

			while (threadCounter.getProcesses() >= processes - 1)
				Thread.sleep(200);

			es.execute(new UAMTravelTimeCalculator(threadCounter, network, config, trip, uamManager,
					networkCar, scenario, stationConnectionutilities));
			counter++;
		}
		es.shutdown();
		// Make sure that the file is not written before all threads are finished
		while (!es.isTerminated())
			Thread.sleep(200);

		// Writes output file
		log.info("Writing travel times file...");
		write(outputPath, trips);
		log.info("...done.");

	}

	public static void write(String outputPath, List<TripItem> trips) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		writer.write(formatHeader() + "\n");
		for (TripItem trip : trips) {
			writer.write(String.join(",",
					new String[]{String.valueOf(trip.origin.getX()), String.valueOf(trip.origin.getY()),
							String.valueOf(trip.destination.getX()), String.valueOf(trip.destination.getY()),
							String.valueOf(trip.departureTime), String.valueOf(trip.travelTime),
							String.valueOf(trip.accessTime), String.valueOf(trip.flightTime),
							String.valueOf(trip.egressTime), trip.accessMode, trip.egressMode,
							trip.originStation, trip.destinationStation})
					+ "\n");
		}

		writer.flush();
		writer.close();
	}

	private static String formatHeader() {
		return String.join(",",
				new String[]{"origin_x", "origin_y", "destination_x", "destination_y", "departure_time",
						"travel_time", "access_time", "flight_time", "egress_time", "access_mode",
						"egress_mode", "orig_station", "dest_station"});
	}

	static class UAMTravelTimeCalculator implements Runnable {

		private TripItem trip;
		private Network network;
		private Config config;
		private ThreadCounter threadCounter;
		private UAMManager uamManager;
		private Network networkCar;
		private Scenario scenario;
		private UAMStationConnectionGraph stationConnectionutilities;
		private ParallelLeastCostPathCalculator pathCalculator;
		private LeastCostPathCalculator plcpccar;
		private TransitRouter transitRouter;

		UAMTravelTimeCalculator(ThreadCounter threadCounter, Network network, Config config, TripItem trip,
								UAMManager uamManager, Network networkCar, Scenario scenario,
								UAMStationConnectionGraph stationConnectionutilities) {
			this.trip = trip;
			this.network = network;
			this.config = config;
			this.threadCounter = threadCounter;
			this.uamManager = uamManager;
			this.networkCar = networkCar;
			this.scenario = scenario;
			this.stationConnectionutilities = stationConnectionutilities;
		}

		@Override
		public void run() {
			threadCounter.register();

			try {
				pathCalculator = uamRouters.take();
				plcpccar = carRouters.take();
				transitRouter = ptRouters.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Link from = NetworkUtils.getNearestLink(network, trip.origin);
			Link to = NetworkUtils.getNearestLink(network, trip.destination);
			Facility fromFacility = new LinkWrapperFacility(from);
			Facility toFacility = new LinkWrapperFacility(to);

			UAMConfigGroup uamConfig = (UAMConfigGroup) config.getModules().get(UAMConstants.uam);
			UAMStrategyUtils strategyUtils = new UAMStrategyUtils(uamManager.getStations(), uamConfig,
					scenario, stationConnectionutilities, networkCar, transitRouter, pathCalculator, plcpccar);
			UAMStrategy strategy = null;
			switch (uamConfig.getUAMRoutingStrategy()) {
				case MINTRAVELTIME:
					strategy = new UAMMinTravelTimeStrategy(strategyUtils);
					break;
				case MINACCESSTRAVELTIME:
					strategy = new UAMMinAccessTravelTimeStrategy(strategyUtils);
					break;
				case MINDISTANCE:
					strategy = new UAMMinDistanceStrategy(strategyUtils);
					break;
				case MINACCESSDISTANCE:
					strategy = new UAMMinAccessDistanceStrategy(strategyUtils);
					break;
				default:
					log.warn("Strategy not available, please provide an available strategy.");
					System.exit(-1);
			}

			try {
				UAMRoute uamRoute = strategy.getRoute(null, fromFacility, toFacility, trip.departureTime);

				trip.accessMode = uamRoute.accessMode;
				trip.egressMode = uamRoute.egressMode;
				trip.originStation = uamRoute.bestOriginStation.getId().toString();
				trip.destinationStation = uamRoute.bestDestinationStation.getId().toString();

				trip.accessTime = strategyUtils.getAccessTime(fromFacility, trip.departureTime,
						uamRoute.bestOriginStation, uamRoute.accessMode);

				trip.flightTime = strategyUtils.getFlightTime(uamRoute.bestOriginStation, uamRoute.bestDestinationStation);

				trip.egressTime = strategyUtils.getEgressTime(toFacility, trip.departureTime,
						uamRoute.bestDestinationStation, uamRoute.egressMode);

				trip.travelTime = trip.accessTime + trip.flightTime + trip.egressTime;
			} catch (NullPointerException e) {
				//e.printStackTrace();
			}

			try {
				ptRouters.put(transitRouter);
				uamRouters.put((DefaultParallelLeastCostPathCalculator) pathCalculator);
				carRouters.put(plcpccar);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			threadCounter.deregister();
		}
	}

}
