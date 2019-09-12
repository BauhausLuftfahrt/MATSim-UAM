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
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.router.TransitRouter;

import ch.sbb.matsim.routing.pt.raptor.DefaultRaptorIntermodalAccessEgress;
import ch.sbb.matsim.routing.pt.raptor.DefaultRaptorParametersForPerson;
import ch.sbb.matsim.routing.pt.raptor.LeastCostRaptorRouteSelector;
import ch.sbb.matsim.routing.pt.raptor.RaptorStaticConfig;
import ch.sbb.matsim.routing.pt.raptor.RaptorUtils;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptor;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorData;

/**
 * This script generates csv file containing estimated travel times by Pt for
 * trips. The trips file must contain departure time and origin and destination
 * coordinates for the trips. Necessary inputs are in the following order:
 * -Network file; -Transit Schedule file; -Transit Vehicles file; -Trips file;
 * -output file;
 *
 * @author Aitan Militao
 */

public class RunCalculatePTTravelTimes {
	private static final Logger log = Logger.getLogger(RunCalculatePTTravelTimes.class);

	public static void main(String[] args) throws Exception {
		System.out.println(
				"ARGS: base-network.xml* transitScheduleFile.xml* transitVehiclesFile.xml* tripsCoordinateFile.csv* outputfile-name*");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String networkInput = args[j++];
		String transitScheduleInput = args[j++];
		String transitVehiclesInput = args[j++];
		String tripsInput = args[j++];
		String outputPath = args[j++];

		// READ NETWORK
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkInput);

		// READ TRANSIT SCHEDULE
		config.transit().setTransitScheduleFile(transitScheduleInput);

		// READ TRANSIT VEHICLES FILE
		config.transit().setVehiclesFile(transitVehiclesInput);

		// CONFIGURING PARAMETERS
		config.transitRouter().setSearchRadius(2500);
		config.planCalcScore().setPerforming_utils_hr(0);
		config.planCalcScore().setMarginalUtlOfWaitingPt_utils_hr(-2.3);
		config.planCalcScore().setUtilityOfLineSwitch(-0.17);
		config.transitRouter().setExtensionRadius(500);
		config.plansCalcRoute().getModeRoutingParams().get("walk").setTeleportedModeSpeed(1.2);

		PlanCalcScoreConfigGroup.ModeParams accessWalk = new PlanCalcScoreConfigGroup.ModeParams("access_walk");
		accessWalk.setMarginalUtilityOfTraveling(-4.0);
		config.planCalcScore().addModeParams(accessWalk);
		PlanCalcScoreConfigGroup.ModeParams egressWalk = new PlanCalcScoreConfigGroup.ModeParams("egress_walk");
		egressWalk.setMarginalUtilityOfTraveling(-4.0);
		config.planCalcScore().addModeParams(egressWalk);

		config.planCalcScore().getOrCreateModeParams("pt").setMarginalUtilityOfTraveling(-1.32);
		config.planCalcScore().getOrCreateModeParams("walk").setMarginalUtilityOfTraveling(-6.46);
		config.transit().setUseTransit(true);

		Scenario scenario = ScenarioUtils.createScenario(config);
		ScenarioUtils.loadScenario(scenario);
		Network network = scenario.getNetwork();

		// Old Router
		/*
		 * TransitRouterConfig transitConfig= new TransitRouterConfig(
		 * config.planCalcScore(), config.plansCalcRoute(), config.transitRouter(),
		 * config.vspExperimental());
		 * 
		 * TransitRouterImpl TransitRouterImpl = new TransitRouterImpl(transitConfig,
		 * scenario.getTransitSchedule());
		 */

		RaptorStaticConfig raptorStaticConfig = RaptorUtils.createStaticConfig(config);
		raptorStaticConfig.setBeelineWalkSpeed(0.9230769);
		raptorStaticConfig.setMinimalTransferTime(0);
		raptorStaticConfig.setBeelineWalkConnectionDistance(250);
		raptorStaticConfig.setMarginalUtilityOfTravelTimeAccessWalk_utl_s(-0.0017944);
		raptorStaticConfig.setMarginalUtilityOfTravelTimeEgressWalk_utl_s(-0.0017944);
		raptorStaticConfig.setMarginalUtilityOfTravelTimeWalk_utl_s(-0.0017944);

		SwissRailRaptorData data = SwissRailRaptorData.create(scenario.getTransitSchedule(), raptorStaticConfig,
				network);
		DefaultRaptorParametersForPerson parametersForPerson = new DefaultRaptorParametersForPerson(config);

		SwissRailRaptor router = new SwissRailRaptor(data, parametersForPerson, new LeastCostRaptorRouteSelector(),
				new DefaultRaptorIntermodalAccessEgress());

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
				log.info("Calculation completion: " + counter + "/" + trips.size() +
						" (" + String.format("%.0f", (double) counter / trips.size() * 100)  + "%).");
			try {
				Link from = NetworkUtils.getNearestLink(network, trip.origin);
				Link to = NetworkUtils.getNearestLink(network, trip.destination);
				trip.travelTime = estimateTravelTime(from, to, trip.departureTime, router);
			} catch (NullPointerException e) {
				log.warn("No travel time estimation could be made for trip #" + counter + " from " + trip.origin +
						" to " + trip.destination + " at departure time " + trip.departureTime + "!");
				failedTrips.add(trip);
			}

			counter++;
		}

		log.info("" + failedTrips.size() + " trips without travel times have been removed.");
		trips.removeAll(failedTrips);

		// Writes output file
		log.info("Writing travelTimes file...");
		write(outputPath, trips);
		log.info("...done.");

	}

	private static double estimateTravelTime(Link from, Link to, double departureTime, TransitRouter router) {

		List<Leg> legs = router.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to), departureTime,
				null);
		double timeByPt = 0.0;
		for (Leg leg : legs) {
			timeByPt += leg.getTravelTime();
		}

		return timeByPt;
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

}
