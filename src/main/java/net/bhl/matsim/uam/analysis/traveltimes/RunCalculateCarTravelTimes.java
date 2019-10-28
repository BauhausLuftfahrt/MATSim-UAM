package net.bhl.matsim.uam.analysis.traveltimes;

import net.bhl.matsim.uam.analysis.traveltimes.utils.ConfigSetter;
import net.bhl.matsim.uam.analysis.traveltimes.utils.ThreadCounter;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItem;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItemReader;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.AStarLandmarksFactory;
import org.matsim.core.router.util.*;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This script generates csv file containing estimated travel times by CAR for
 * trips. The trips file must contain departure time and origin and destination
 * coordinates for the trips. Necessary inputs are in the following order:
 * -Network file; -networkEventsChangeFile file; -Trips file; -output file;
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class RunCalculateCarTravelTimes {
	private static final int processes = Runtime.getRuntime().availableProcessors();
	private static final Logger log = Logger.getLogger(RunCalculateCarTravelTimes.class);
	private static ArrayBlockingQueue<LeastCostPathCalculator> carRouters = new ArrayBlockingQueue<>(processes);

	public static void main(String[] args) throws Exception {
		System.out.println("ARGS: base-network.xml* networkEventsChangeFile.xml* tripsCoordinateFile.csv* outputfile-name.csv*");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String networkInput = args[j++];
		String networkEventsChangeFile = args[j++];
		String tripsInput = args[j++];
		String outputPath = args[j];

		Config config = ConfigSetter.createCarConfig(networkInput, networkEventsChangeFile);
		Scenario scenario = ScenarioUtils.createScenario(config);
		ScenarioUtils.loadScenario(scenario);
		Network network = scenario.getNetwork();

		// CREATE CAR NETWORK
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Network networkCar = NetworkUtils.createNetwork();
		Set<String> modesCar = new HashSet<>();
		modesCar.add("car");
		filter.filter(networkCar, modesCar);

		// LEAST COST PATH CALCULATOR
		TravelTimeCalculator tcc2 = TravelTimeCalculator.create(network, config.travelTimeCalculator());
		TravelTime travelTime = tcc2.getLinkTravelTimes();
		TravelDisutility travelDisutility = TravelDisutilityUtils
				.createFreespeedTravelTimeAndDisutility(config.planCalcScore());

		com.google.inject.Injector injector = Injector.createInjector(config, new AbstractModule() {
			@Override
			public void install() {
				bind(LeastCostPathCalculatorFactory.class).to(AStarLandmarksFactory.class);
			}
		});
		LeastCostPathCalculatorFactory pathCalculatorFactory = injector
				.getInstance(LeastCostPathCalculatorFactory.class); // AStarLandmarksFactory

		//Provide routers
		for (int i = 0; i < processes; i++) {
			carRouters.add(pathCalculatorFactory.createPathCalculator(networkCar, travelDisutility, travelTime));
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

			es.execute(new CarTravelTimeCalculator(threadCounter, networkCar, trip));
			counter++;
		}
		es.shutdown();
		// Make sure that the file is not written before all threads are finished
		while (!es.isTerminated())
			Thread.sleep(200);

		// Writes output file
		log.info("Writing travelTimes file...");
		write(outputPath, trips);
		log.info("...done.");
	}

	public static void write(String outputPath, List<TripItem> trips) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		writer.write(formatHeader() + "\n");
		for (TripItem trip : trips) {
			writer.write(String.join(",",
					new String[] { String.valueOf(trip.origin.getX()), String.valueOf(trip.origin.getY()),
							String.valueOf(trip.destination.getX()), String.valueOf(trip.destination.getY()),
							String.valueOf(trip.departureTime), String.valueOf(trip.travelTime) })
					+ "\n");
		}

		writer.flush();
		writer.close();
	}

	private static String formatHeader() {
		return String.join(",", new String[] { "origin_x", "origin_y", "destination_x", "destination_y",
				"departure_time", "travel_time" });
	}

	static class CarTravelTimeCalculator implements Runnable {

		private TripItem trip;
		private ThreadCounter threadCounter;
		private Network networkCar;
		private LeastCostPathCalculator plcpccar;

		CarTravelTimeCalculator(ThreadCounter threadCounter, Network network, TripItem trip) {
			this.threadCounter = threadCounter;
			this.networkCar = network;
			this.trip = trip;
		}

		@Override
		public void run() {
			threadCounter.register();

			try {
				plcpccar = carRouters.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Link from = NetworkUtils.getNearestLink(networkCar, trip.origin);
			Link to = NetworkUtils.getNearestLink(networkCar, trip.destination);

			try {
				trip.travelTime = estimateTravelTime(from, to, trip.departureTime, networkCar, plcpccar);
			} catch (NullPointerException e) {
				log.warn("No travel time estimation could be made for trip from " + trip.origin
						+ " to " + trip.destination + " at departure time " + trip.departureTime + "!");
			}

			try {
				carRouters.put(plcpccar);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			threadCounter.deregister();
		}
	}

	private static double estimateTravelTime(Link from, Link to, double departureTime, Network carNetwork,
											 LeastCostPathCalculator pathCalculator) {
		if (carNetwork.getLinks().get(from.getId()) != null)
			from = carNetwork.getLinks().get(from.getId());
		else
			from = NetworkUtils.getNearestLinkExactly(carNetwork, from.getCoord());

		if (carNetwork.getLinks().get(to.getId()) != null)
			to = carNetwork.getLinks().get(to.getId());
		else
			to = NetworkUtils.getNearestLinkExactly(carNetwork, to.getCoord());
		Path path = pathCalculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), departureTime, null,
				null);

		double time = 0;
		for (Link link : path.links)
			time += link.getLength() / link.getFreespeed(departureTime);
		return time;
	}

}
