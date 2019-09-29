package net.bhl.matsim.uam.analysis.traveltimes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkChangeEventsWriter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelDisutilityUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.utils.misc.Time;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;

/**
 * This script generates csv file containing estimated travel times by CAR for
 * trips. The trips file must contain departure time and origin and destination
 * coordinates for the trips. Necessary inputs are in the following order:
 * -Network file; -Events file; -Trips file; -output file; -output
 * networkEventsChangeFile;
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class RunCalculateCarTravelTimes {
	private static final Logger log = Logger.getLogger(RunCalculateCarTravelTimes.class);

	public static void main(String[] args) throws Exception {
		System.out.println(
				"ARGS: base-network.xml* eventsFile.xml* tripsCoordinateFile.csv* outputfile-name.csv* outputfile-networkEventsChangeFile.xml.gz*");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String networkInput = args[j++];
		String eventsFileInput = args[j++]; // ADD EVENTS INPUT
		String tripsInput = args[j++];
		String outputPath = args[j++];
		String networkEventsChangeFile = args[j++];

		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkInput);

		// Generate networkChangeEvents file for the Time-Dependent Network
		Network networkForReader = NetworkUtils.createNetwork();
		new MatsimNetworkReader(networkForReader).readFile(networkInput);
		TravelTimeCalculator tcc = readEventsIntoTravelTimeCalculator(networkForReader, eventsFileInput,
				config.travelTimeCalculator());
		double timeStep = 15 * 60;
		double minFreeSpeed = 3;
		config.qsim().setEndTime(30 * 60 * 60);
		List<NetworkChangeEvent> networkChangeEvents = createNetworkChangeEvents(networkForReader, tcc,
				config.qsim().getEndTime(), timeStep, minFreeSpeed);
		new NetworkChangeEventsWriter().write(networkEventsChangeFile, networkChangeEvents);

		config.network().setTimeVariantNetwork(true);
		config.network().setChangeEventsInputFile(networkEventsChangeFile);

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

		DefaultParallelLeastCostPathCalculator pathCalculator = DefaultParallelLeastCostPathCalculator.create(
				Runtime.getRuntime().availableProcessors(), new DijkstraFactory(), networkCar, travelDisutility,
				travelTime);

		// READ TRIPS FILE AND CALCULATES TRAVEL TIMES
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tripsInput)));
		String line = null;
		List<String> header = null;
		List<TripItem> trips = new ArrayList<>();

		while ((line = reader.readLine()) != null) {
			List<String> row = Arrays.asList(line.split(","));

			if (header == null) {
				header = row;
			} else {
				double originX = Double.parseDouble(row.get(header.indexOf("origin_x")));
				double originY = Double.parseDouble(row.get(header.indexOf("origin_y")));
				double destX = Double.parseDouble(row.get(header.indexOf("destination_x")));
				double destY = Double.parseDouble(row.get(header.indexOf("destination_y")));
				double departureTime = Time.parseTime(row.get(header.indexOf("trip_time")));

				Coord originCood = new Coord(originX, originY);
				Coord destinationCoord = new Coord(destX, destY);

				TripItem trip = new TripItem();
				trip.origin = originCood;
				trip.destination = destinationCoord;
				trip.departureTime = departureTime;

				trips.add(trip);
			}
		}
		reader.close();

		// Calculate travel times
		log.info("Calculating travel times...");
		List<TripItem> failedTrips = new ArrayList<>();
		int counter = 1;
		for (TripItem trip : trips) {
			if (trips.size() < 100 || counter % (trips.size() / 100) == 0)
				log.info("Calculation completion: " + counter + "/" + trips.size() + " ("
						+ String.format("%.0f", (double) counter / trips.size() * 100) + "%).");
			try {
				Link from = NetworkUtils.getNearestLink(network, trip.origin);
				Link to = NetworkUtils.getNearestLink(network, trip.destination);
				trip.travelTime = estimateTravelTime(from, to, trip.departureTime, networkCar, pathCalculator);
			} catch (NullPointerException e) {
				log.warn("No travel time estimation could be made for trip #" + counter + " from " + trip.origin
						+ " to " + trip.destination + " at departure time " + trip.departureTime + "!");
				failedTrips.add(trip);
			}

			counter++;
		}

		log.info("" + failedTrips.size() + " trips without travel times have been removed.");
		trips.removeAll(failedTrips);
		pathCalculator.close();

		// Writes output file
		log.info("Writing travelTimes file...");
		write(outputPath, trips);
		log.info("...done.");
	}

	private static double estimateTravelTime(Link from, Link to, double departureTime, Network carNetwork,
			DefaultParallelLeastCostPathCalculator pathCalculator) throws InterruptedException, ExecutionException {
		if (carNetwork.getLinks().get(from.getId()) != null)
			from = carNetwork.getLinks().get(from.getId());
		else
			from = NetworkUtils.getNearestLinkExactly(carNetwork, from.getCoord());

		if (carNetwork.getLinks().get(to.getId()) != null)
			to = carNetwork.getLinks().get(to.getId());
		else
			to = NetworkUtils.getNearestLinkExactly(carNetwork, to.getCoord());
		Future<Path> path = pathCalculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), departureTime, null,
				null);
		/*
		 * for (Link link : path.get().links) { log.warn("Link ID: " + link.getId() +
		 * "  Capacity: "+ link.getCapacity() + " FlowCapacityPerSec: " +
		 * link.getFlowCapacityPerSec() +"   FreeSpeed: "+link.getFreespeed()); }
		 */

		return path.get().travelTime;
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

	public static class TripItem {
		public Coord origin;
		public Coord destination;
		public double departureTime;
		public double travelTime;

	}

	public static TravelTimeCalculator readEventsIntoTravelTimeCalculator(Network network, String eventsFile,
			TravelTimeCalculatorConfigGroup group) {
		EventsManager manager = EventsUtils.createEventsManager();
		TravelTimeCalculator tcc = TravelTimeCalculator.create(network, group);
		manager.addHandler(tcc);
		new MatsimEventsReader(manager).readFile(eventsFile);
		return tcc;
	}

	public static List<NetworkChangeEvent> createNetworkChangeEvents(Network network, TravelTimeCalculator tcc,
			Double endTime, Double timeStep, Double MinFreeSpeed) {
		List<NetworkChangeEvent> networkChangeEvents = new ArrayList<>();
		for (Link l : network.getLinks().values()) {

//			if (l.getId().toString().startsWith("pt")) continue;

			double length = l.getLength();
			double previousTravelTime = l.getLength() / l.getFreespeed();

			for (double time = 0; time < endTime; time = time + timeStep) {

				double newTravelTime = tcc.getLinkTravelTimes().getLinkTravelTime(l, time, null, null);

				if (newTravelTime != previousTravelTime) {
					// log.warn("Linkd ID: "+ l.getId()+" previousTravelTime: "+previousTravelTime+"
					// NewTravelTime: "+ newTravelTime);
					NetworkChangeEvent nce = new NetworkChangeEvent(time);
					nce.addLink(l);
					double newFreespeed = length / newTravelTime;
					if (newFreespeed < MinFreeSpeed)
						newFreespeed = MinFreeSpeed;
					NetworkChangeEvent.ChangeValue freespeedChange = new NetworkChangeEvent.ChangeValue(
							ChangeType.ABSOLUTE_IN_SI_UNITS, newFreespeed);
					nce.setFreespeedChange(freespeedChange);

					networkChangeEvents.add(nce);
					previousTravelTime = newTravelTime;
				}
			}
		}
		return networkChangeEvents;
	}
}
