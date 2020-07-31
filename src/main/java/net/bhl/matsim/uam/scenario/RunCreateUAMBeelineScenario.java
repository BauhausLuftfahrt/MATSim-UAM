package net.bhl.matsim.uam.scenario;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import com.google.common.collect.Iterables;
import net.bhl.matsim.uam.router.UAMFlightSegments;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.costcalculators.OnlyTimeDependentTravelDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This script creates UAM-including MATSim network and corresponding
 * uam-vehicles file, which are prerequisites for running a UAM-enabled MATSim
 * simulation with a UAM flight being beeline connections between all stations.
 *
 * @author RRothfeld (Raoul Rothfeld)
 */
public class RunCreateUAMBeelineScenario {
	public static void main(String[] args) {
		System.out.println("ARGS: config.xml* uam-stations.csv* uam-link-freespeed* uam-link-capacity* uam-vehicles.csv");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String configInput = args[j++];
		String stationInput = args[j++];
		double uamMaxLinkSpeed = Double.parseDouble(args[j++]);
		double uamLinkCapacity = Double.parseDouble(args[j++]);
		String vehicleInput = null;

		if (args.length > j)
			vehicleInput = args[j];

		// Run
		RunCreateUAMRoutedScenario.convert(configInput, stationInput, uamMaxLinkSpeed, uamLinkCapacity, vehicleInput);
	}
}