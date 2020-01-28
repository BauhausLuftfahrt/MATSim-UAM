package net.bhl.matsim.uam.analysis.traveltimes;

import ch.sbb.matsim.routing.pt.raptor.*;
import net.bhl.matsim.uam.analysis.traveltimes.utils.ConfigSetter;
import net.bhl.matsim.uam.analysis.traveltimes.utils.ThreadCounter;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItem;
import net.bhl.matsim.uam.analysis.traveltimes.utils.TripItemReader;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.router.TransitRouter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This script generates csv file containing estimated travel times by Pt for
 * trips. The trips file must contain departure time and origin and destination
 * coordinates for the trips. Necessary inputs are in the following order:
 * -Network file; -Transit Schedule file; -Transit Vehicles file; -Trips file;
 * -output file;
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class RunCalculatePTTravelTimes {
    private static final int processes = Runtime.getRuntime().availableProcessors();
    private static final Logger log = Logger.getLogger(RunCalculatePTTravelTimes.class);
    private static ArrayBlockingQueue<TransitRouter> ptRouters = new ArrayBlockingQueue<>(processes);

    public static void main(String[] args) throws Exception {
        System.out.println(
                "ARGS: base-network.xml* transitScheduleFile.xml* transitVehiclesFile.xml* tripsCoordinateFile.csv* outputfile-name*");
        System.out.println("(* required)");

        // ARGS
        int j = 0;
        String networkInput = args[j++];
        String transitScheduleInput = args[j++];
        String tripsInput = args[j++];
        String outputPath = args[j++];

        // READ NETWORK
        Config config = ConfigSetter.createPTConfig(networkInput, transitScheduleInput);
        Scenario scenario = ScenarioUtils.createScenario(config);
        ScenarioUtils.loadScenario(scenario);
        Network network = scenario.getNetwork();

        RaptorStaticConfig raptorStaticConfig = ConfigSetter.createRaptorConfig(config);
        SwissRailRaptorData data = SwissRailRaptorData.create(scenario.getTransitSchedule(), raptorStaticConfig,
                network);

        //Provide routers
        for (int i = 0; i < processes; i++) {
            ptRouters.add(new SwissRailRaptor(data, new DefaultRaptorParametersForPerson(config),
                    new LeastCostRaptorRouteSelector(), new DefaultRaptorIntermodalAccessEgress()));
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

            es.execute(new PTTravelTimeCalculator(threadCounter, network, config, trip, network, scenario));
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
                    new String[]{String.valueOf(trip.origin.getX()), String.valueOf(trip.origin.getY()),
                            String.valueOf(trip.destination.getX()), String.valueOf(trip.destination.getY()),
                            String.valueOf(trip.departureTime), String.valueOf(trip.travelTime), String.valueOf(trip.travelTime)})
                    + "\n");
        }

        writer.flush();
        writer.close();
    }

    private static String formatHeader() {
        return String.join(",", new String[]{"origin_x", "origin_y", "destination_x", "destination_y",
                "departure_time", "travel_time", "distance"});
    }

    static class PTTravelTimeCalculator implements Runnable {

        private TripItem trip;
        private Network network;
        private Config config;
        private ThreadCounter threadCounter;
        private Network networkCar;
        private Scenario scenario;
        private TransitRouter transitRouter;

        PTTravelTimeCalculator(ThreadCounter threadCounter, Network network, Config config, TripItem trip,
                               Network networkCar, Scenario scenario) {
            this.trip = trip;
            this.network = network;
            this.config = config;
            this.threadCounter = threadCounter;
            this.networkCar = networkCar;
            this.scenario = scenario;
        }

        @Override
        public void run() {
            threadCounter.register();

            try {
                transitRouter = ptRouters.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Link from = NetworkUtils.getNearestLink(network, trip.origin);
            Link to = NetworkUtils.getNearestLink(network, trip.destination);

            try {
            	 List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to), trip.departureTime,
                         null);
            	 double time = 0;
            	 double distanceByPt = 0.0;
                 for (Leg leg : legs) {
                	 time += leg.getTravelTime();
                	 distanceByPt += leg.getRoute().getDistance();
                 }
         		 trip.travelTime = time;
         		 trip.distance = distanceByPt;
            } catch (NullPointerException e) {
                // Do nothing; failed trip will show as null in results.
            }

            try {
                ptRouters.put(transitRouter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            threadCounter.deregister();
        }
    }

    private static double estimateTravelTime(Link from, Link to, double departureTime, TransitRouter router) {
        List<Leg> legs = router.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to), departureTime,
                null);
        double time = 0;
        for (Leg leg : legs)
			time += leg.getTravelTime();
        return time;
    }


}
