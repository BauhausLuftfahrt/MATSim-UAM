package net.bhl.matsim.uam.analysis.uamroutes.run;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelDisutilityUtils;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This script generates a CSV file containing the distance, travel time and
 * utility between UAM stations. Necessary inputs are in the following order:
 * -Network file; -UAMVehicles file; -output file;
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class RunCalculateUAMRoutes {

	static private Map<Id<UAMStation>, UAMStation> stations;
	static private UAMStationConnectionGraph uamSCG;
	static final private String delimiter = ",";

	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK UAMVEHICLES OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}

	static public void extract(String networkString, String uamVehicles, String outfile) throws IOException {
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkString);
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkString);

		UAMXMLReader uamReader = new UAMXMLReader(network);
		uamReader.readFile(uamVehicles);

		stations = uamReader.getStations();
		uamSCG = calculateRoutes(network, uamReader);
		write(outfile);
	}

	static public UAMStationConnectionGraph calculateRoutes(Network network, UAMXMLReader uamReader) {
		TravelTime tt = new FreeSpeedTravelTime();
		TravelDisutility td = TravelDisutilityUtils
				.createFreespeedTravelTimeAndDisutility(ConfigUtils.createConfig().planCalcScore());

		UAMManager uamManager = new UAMManager(network);
		uamManager.setStations(new UAMStations(uamReader.getStations(), network));
		uamManager.setVehicles(uamReader.getVehicles());

		return new UAMStationConnectionGraph(uamManager, null, DefaultParallelLeastCostPathCalculator
				.create(Runtime.getRuntime().availableProcessors(), new DijkstraFactory(), network, td, tt));
	}

	private static void write(String outputPath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
		writer.write(formatHeader() + "\n");

		for (UAMStation stationFrom : stations.values()) {
			for (UAMStation stationTo : stations.values()) {
				if (stationFrom.equals(stationTo))
					continue;

				writer.write(String.join(delimiter,
						new String[] { String.valueOf(stationFrom.getId()), String.valueOf(stationTo.getId()),
								String.valueOf(uamSCG.getTravelTime(stationFrom.getId(), stationTo.getId())),
								String.valueOf(uamSCG.getDistance(stationFrom.getId(), stationTo.getId())),
								uamSCG.getUtility(stationFrom.getId(), stationTo.getId()) + "\n" }));
			}
		}

		writer.flush();
		writer.close();
	}

	private static String formatHeader() {
		return String.join(delimiter,
				new String[] { "station_from", "station_to", "travel_time_s", "travel_distance_m", "travel_utility" });
	}
}
