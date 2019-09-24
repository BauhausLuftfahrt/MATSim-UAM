package net.bhl.matsim.uam.scenario;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
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
import com.google.common.collect.Iterables;
import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;

/**
 * This script creates UAM-including MATSim network and corresponding
 * uam-vehicles file, which are prerequisites for running a UAM-enabled MATSim
 * simulation.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class RunCreateUAMScenario {

	static boolean use_z_values = false;

	static String name_uam_vehicles = "uam_vh_";
	static String name_uam_nodes = "uam_st_";
	static String name_uam_waypoints = "uam_wp_";
	static String name_uam_ground_link = "uam_gl-";
	static String name_uam_station_link = "uam_sl-";
	public static String name_uam_vertical_link = "uam_vl-";
	public static String name_uam_horizontal_link = "uam_hl-";
	static String name_uam_station_ground_access = "_ga";
	static String name_uam_station_flight_access = "_fa";
	static String name_uam_station_flight_level = "_fl";

	static String name_uam_stations_xml = "stations";

	static String name_uam_dtd = "src/main/resources/dtd/uam.dtd";

	static String mode_uam = "uam";
	static String mode_car = "car";

	static double min_link_length = 1;
	static double max_horizontal_vtol_distance = 10;
	static double permlanes = 1;
	static double detour_factor = 1.0; // default: 1.0, i.e. no detour from link distance

	static double default_link_capacity = 999999.9;
	static double default_link_freespeed = 999999.9;

	static double NO_LENGTH = -1;

	public static void main(String[] args) throws Exception {

		System.out.println(
				"ARGS: base-folder* base-network.xml* uam-stations.csv* detour-factor vehicles.csv flight-nodes.csv flight-links.csv");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String folder = args[j++];
		String networkInput = folder + "\\" + args[j++];
		String stationInput = folder + "\\" + args[j++];
		String nodesInput = null;
		String linksInput = null;
		String vehicleInput = null;

		boolean withDetour = args.length >= 4;
		boolean withVehicles = args.length >= 5;
		boolean withNetwork = args.length >= 6;

		if (withDetour)
			detour_factor = Double.parseDouble(args[j++]);

		if (withVehicles)
			vehicleInput = folder + "\\" + args[j++];

		if (withNetwork) {
			nodesInput = folder + "\\" + args[j++];
			linksInput = folder + "\\" + args[j++];
		}

		// READ NETWORK
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(networkInput);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();

		// READ CSV INPUT
		List<String[]> stations = CSVReaders.readCSV(stationInput);
		List<String[]> nodes = null;
		List<String[]> links = null;
		List<String[]> vehicles = null;

		if (withNetwork) {
			nodes = CSVReaders.readCSV(nodesInput);
			links = CSVReaders.readCSV(linksInput);
		}

		if (withVehicles)
			vehicles = CSVReaders.readCSV(vehicleInput);

		// GENERATE FLIGHT LINKS AND NODES FOR DIRECT OR USE INPUT FLIGHT NETWORK
		if (withNetwork) {
			// INDIRECT FLIGHT
			// create flight nodes
			for (String[] line : Iterables.skip(nodes, 1)) { // skip CSV header
				Id<Node> id = Id.createNodeId(name_uam_waypoints + encode(line[0]) + name_uam_station_flight_level);
				addNode(network, id, Double.parseDouble(line[1]), Double.parseDouble(line[2]),
						Double.parseDouble(line[3]));
			}

			// create node-connecting links
			for (String[] line : Iterables.skip(links, 1)) { // skip CSV header
				Id<Node> from = Id.createNodeId(name_uam_waypoints + encode(line[0]) + name_uam_station_flight_level);
				Id<Node> to = Id.createNodeId(name_uam_waypoints + encode(line[1]) + name_uam_station_flight_level);

				Double capacity = Double.parseDouble(line[2]);
				Double freespeed = Double.parseDouble(line[3]);

				Set<String> modesUam = new HashSet<String>();
				modesUam.add(mode_uam);
				addLink(network, from, to, modesUam, capacity, freespeed);
				addLink(network, to, from, modesUam, capacity, freespeed);
			}

		} else {
			// DIRECT FLIGHT BETWEEN ALL STATIONS
			// create flight level accesses
			List<Id<Node>> flightAccesses = new ArrayList<Id<Node>>();
			for (String[] line : Iterables.skip(stations, 1)) { // skip CSV header
				Id<Node> node_id = Id.createNodeId(name_uam_nodes + line[0] + name_uam_station_flight_level);
				double node_x = Double.parseDouble(line[3]);
				double node_y = Double.parseDouble(line[4]);
				double node_z = Double.parseDouble(line[6]);

				addNode(network, node_id, node_x, node_y, node_z);
				flightAccesses.add(node_id);
			}

			// connect flight accesses
			for (int i = 0; i < flightAccesses.size(); i++) {
				for (int k = i + 1; k < flightAccesses.size(); k++) {

					Id<Node> to = flightAccesses.get(i);
					Id<Node> from = flightAccesses.get(k);

					Set<String> mode = new HashSet<String>();
					mode.add(mode_uam);

					addLink(network, to, from, mode);
					addLink(network, from, to, mode);
				}
			}
		}

		// PROVIDE MODE-SPECIFIC NETWORKS
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Network networkUAM = NetworkUtils.createNetwork();
		Set<String> modesUam = new HashSet<String>();
		modesUam.add(mode_uam);
		filter.filter(networkUAM, modesUam);

		// clean up UAM network
		new NetworkCleaner().run(networkUAM);

		filter = new TransportModeNetworkFilter(network);
		Set<String> modesCar = new HashSet<String>();
		modesCar.add(mode_car); // only car
		Network networkCar = NetworkUtils.createNetwork();
		filter.filter(networkCar, modesCar);

		// temp. storage of all UAM stations
		Set<Id<Node>> stationIDs = new HashSet<>();

		for (String[] line : Iterables.skip(stations, 1)) { // skip CSV header
			int i = 0;
			String station_id = line[i++];
			i++; // skip station_name
			i++; // skip station_landing_cap
			double station_x = Double.parseDouble(line[i++]);
			double station_y = Double.parseDouble(line[i++]);
			double station_z = Double.parseDouble(line[i++]);
			double vtol_z = Double.parseDouble(line[i++]);
			double road_access_capacity = Double.parseDouble(line[i++]);
			double road_access_freespeed = Double.parseDouble(line[i++]);
			double station_capacity = Double.parseDouble(line[i++]);
			double station_freespeed = Double.parseDouble(line[i++]);
			double flight_access_capacity = Double.parseDouble(line[i++]);
			double flight_access_freespeed = Double.parseDouble(line[i++]);

			// create ground and flight access nodes and station link
			Id<Node> node_ga_id = Id.createNodeId(name_uam_nodes + station_id + name_uam_station_ground_access);
			Id<Node> node_fa_id = Id.createNodeId(name_uam_nodes + station_id + name_uam_station_flight_access);
			stationIDs.add(node_fa_id);

			// find nearest flight node
			Coord station = createCoord(station_x, station_y, station_z);
			Node uamNode = NetworkUtils.getNearestNode(networkUAM, station);

			// if no UAM flight node is nearby, create own flight level node and connect to
			// nearest UAM node via
			// horizontal flight (rather than vertical)
			if (CoordUtils.calcEuclideanDistance(station, uamNode.getCoord()) >= max_horizontal_vtol_distance) {
				Id<Node> node_fl_id = Id.createNodeId(name_uam_nodes + station_id + name_uam_station_flight_level);
				addNode(network, node_fl_id, station_x, station_y, vtol_z);

				addLink(network, node_fl_id, uamNode.getId(), modesUam);
				addLink(network, uamNode.getId(), node_fl_id, modesUam);

				uamNode = network.getNodes().get(node_fl_id);
			}

			// find nearest ground node
			Node roadNode = NetworkUtils.getNearestNode(networkCar, station);

			// add station nodes to network (after access node search, since search for
			// closest node would return station nodes)
			addNode(network, node_ga_id, station_x, station_y, station_z);
			addNode(network, node_fa_id, station_x, station_y, station_z);

			// station links
			Set<String> modes = new HashSet<String>();
			modes.add(mode_uam);
			modes.add(mode_car);
			addLink(network, node_ga_id, node_fa_id, modes, station_capacity, station_freespeed);
			addLink(network, node_fa_id, node_ga_id, modes, station_capacity, station_freespeed);

			// flight access links
			addLink(network, node_fa_id, uamNode.getId(), modesUam, flight_access_capacity, flight_access_freespeed,
					vtol_z);
			addLink(network, uamNode.getId(), node_fa_id, modesUam, flight_access_capacity, flight_access_freespeed,
					vtol_z);

			// ground access links
			addLink(network, node_ga_id, roadNode.getId(), modesCar, road_access_capacity, road_access_freespeed);
			addLink(network, roadNode.getId(), node_ga_id, modesCar, road_access_capacity, road_access_freespeed);
		}

		// WRITE STATION DISTANCE CSV
		calculateStationDistances(network, stationIDs,
				networkInput.substring(0, networkInput.lastIndexOf("\\")) + "\\"
						+ networkInput.substring(networkInput.lastIndexOf("\\"), networkInput.lastIndexOf(".xml"))
						+ "_uam_distances.csv");

		// WRITE UAM NETWORK
		NetworkWriter netwriter = new NetworkWriter(network);
		netwriter.write(networkInput.substring(0, networkInput.lastIndexOf("\\")) + "\\"
				+ networkInput.substring(networkInput.lastIndexOf("\\"), networkInput.lastIndexOf(".xml"))
				+ "_uam.xml.gz");

		// ADD UAM VEHICLES
		UAMVehiclesXmlWriter vehwriter = new UAMVehiclesXmlWriter();
		vehwriter.write(networkInput.substring(0, networkInput.lastIndexOf("\\")) + "\\"
				+ networkInput.substring(networkInput.lastIndexOf("\\"), networkInput.lastIndexOf(".xml"))
				+ "_uam_vehicles.xml.gz", stations, vehicles);

		System.out.println("done.");
	}

	private static class UAMVehiclesXmlWriter extends MatsimXmlWriter {

		public void write(String file, List<String[]> stations, List<String[]> vehicles) {
			openFile(file);
			writeXmlHead();
			writeDoctype(mode_uam, name_uam_dtd);
			writeStartTag(mode_uam, Collections.<Tuple<String, String>>emptyList());

			writeStartTag("stations", Collections.<Tuple<String, String>>emptyList());
			writeStations(stations);
			writeEndTag("stations");

			writeStartTag("vehicleTypes", Collections.<Tuple<String, String>>emptyList());
			writeVehicleTypes(vehicles);
			writeEndTag("vehicleTypes");

			writeStartTag("vehicles", Collections.<Tuple<String, String>>emptyList());
			writeVehicles(stations, vehicles);
			writeEndTag("vehicles");

			writeEndTag(mode_uam);
			close();
		}

		private void writeStations(List<String[]> stations) {
			for (String[] station : Iterables.skip(stations, 1)) {
				List<Tuple<String, String>> atts = new ArrayList<>();

				int i = 0;
				String station_id = station[i++];
				String station_name = station[i++];
				String station_landing_cap = station[i++];
				i = 13;
				String station_preflighttime = station[i++];
				String station_postflighttime = station[i++];
				String station_defaultwaittime = station[i++];

				atts.add(new Tuple<String, String>("id", station_id));
				atts.add(new Tuple<String, String>("name", station_name));
				atts.add(new Tuple<String, String>("landingcap", station_landing_cap));
				atts.add(new Tuple<String, String>("preflighttime", station_preflighttime));
				atts.add(new Tuple<String, String>("postflighttime", station_postflighttime));
				atts.add(new Tuple<String, String>("defaultwaittime", station_defaultwaittime));
				atts.add(new Tuple<String, String>("link",
						name_uam_station_link + name_uam_nodes + station_id + name_uam_station_ground_access + "-"
								+ name_uam_nodes + station_id + name_uam_station_flight_access));

				writeStartTag("station", atts, true);
			}
		}

		private void writeVehicleTypes(List<String[]> vehicles) {
			for (String[] vehicle : Iterables.skip(vehicles, 1)) {

				int i = 0;
				String type = vehicle[i++];
				i = 4;
				String capacity = vehicle[i++];
				String cruisespeed = vehicle[i++];
				String verticalspeed = vehicle[i++];
				String boardingtime = vehicle[i++];
				String deboardingtime = vehicle[i++];
				String turnaroundtime = vehicle[i++];

				List<Tuple<String, String>> atts = new ArrayList<>();
				atts.add(new Tuple<String, String>("id", type));
				atts.add(new Tuple<String, String>("capacity", capacity));
				atts.add(new Tuple<String, String>("cruisespeed", cruisespeed));
				atts.add(new Tuple<String, String>("verticalspeed", verticalspeed));
				atts.add(new Tuple<String, String>("boardingtime", boardingtime));
				atts.add(new Tuple<String, String>("deboardingtime", deboardingtime));
				atts.add(new Tuple<String, String>("turnaroundtime", turnaroundtime));
				writeStartTag("vehicleType", atts, true);
			}
		}

		private void writeVehicles(List<String[]> stations, List<String[]> vehicles) {
			for (String[] vehicle : Iterables.skip(vehicles, 1)) {

				int i = 0;
				String type = vehicle[i++];
				Double vehiclesperstation = Double.parseDouble(vehicle[i++]);
				String starttime = vehicle[i++];
				String endtime = vehicle[i++];

				for (String[] station : Iterables.skip(stations, 1)) {
					for (int j = 0; j < vehiclesperstation; j++) {
						List<Tuple<String, String>> atts = new ArrayList<>();
						atts.add(new Tuple<String, String>("id", name_uam_vehicles + station[0] + "-" + j));
						atts.add(new Tuple<String, String>("type", type));
						atts.add(new Tuple<String, String>("initialstation", station[0]));
						atts.add(new Tuple<String, String>("starttime", starttime));
						atts.add(new Tuple<String, String>("endtime", endtime));
						writeStartTag("vehicle", atts, true);
					}
				}
			}
		}

	}

	private static void calculateStationDistances(Network network, Set<Id<Node>> stations, String filename)
			throws IOException, InterruptedException, ExecutionException {
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Set<String> modes = new HashSet<>();
		modes.add("uam");
		Network networkUAM = NetworkUtils.createNetwork();
		filter.filter(networkUAM, modes);

		int routers = Runtime.getRuntime().availableProcessors();
		TravelTime travelTime = new FreeSpeedTravelTime();
		DefaultParallelLeastCostPathCalculator router = DefaultParallelLeastCostPathCalculator.create(routers,
				new DijkstraFactory(), networkUAM, new OnlyTimeDependentTravelDisutility(travelTime), travelTime);

		FileWriter csvWriter = new FileWriter(filename);
		csvWriter.append("fromStation,fromNode,toStation,toNode,cruisedistance_m,vtoldistance_m\n");

		for (Id<Node> uamStationOrigin : stations) {
			for (Id<Node> uamStationDestination : stations) {
				if (uamStationDestination.equals(uamStationOrigin))
					continue;

				// calculate distance
				Future<Path> path = router.calcLeastCostPath(networkUAM.getNodes().get(uamStationOrigin),
						networkUAM.getNodes().get(uamStationDestination), 0.0, null, null);

				double cruisedistance = 0;
				double vtoldistance = 0;
				for (Link link : path.get().links) {
					if (link.getId().toString().contains(name_uam_vertical_link))
						vtoldistance += link.getLength();

					else
						cruisedistance += link.getLength();
				}

				// write out to file
				String fromStation = uamStationOrigin.toString().substring(name_uam_nodes.length(),
						uamStationOrigin.toString().length() - name_uam_station_flight_access.length());
				String toStation = uamStationDestination.toString().substring(name_uam_nodes.length(),
						uamStationDestination.toString().length() - name_uam_station_flight_access.length());
				csvWriter.append(fromStation + "," + uamStationOrigin + "," + toStation + "," + uamStationDestination
						+ "," + cruisedistance + "," + vtoldistance + "\n");
			}
		}

		router.close();

		csvWriter.flush();
		csvWriter.close();
	}

	private static void addNode(Network network, Id<Node> station_id, double station_x, double station_y,
			double station_z) {
		NetworkUtils.createAndAddNode(network, station_id, createCoord(station_x, station_y, station_z));
	}

	private static void addLink(Network network, Id<Node> from, Id<Node> to, Set<String> modes) throws Exception {
		addLink(network, from, to, modes, default_link_capacity, default_link_freespeed);
	}

	private static void addLink(Network network, Id<Node> from, Id<Node> to, Set<String> modes, double capacity,
			double freespeed) throws Exception {
		addLink(network, from, to, modes, capacity, freespeed, NO_LENGTH);
	}

	private static void addLink(Network network, Id<Node> from, Id<Node> to, Set<String> modes, double capacity,
			double freespeed, double length) throws Exception {
		Map<Id<Node>, ? extends Node> allNodes = network.getNodes();
		Node fromNode = allNodes.get(from);
		Node toNode = allNodes.get(to);

		Id<Link> id = null;
		String f = from.toString();
		String t = to.toString();

		if ((f.endsWith(name_uam_station_ground_access) && t.endsWith(name_uam_station_flight_access))
				|| (f.endsWith(name_uam_station_flight_access) && t.endsWith(name_uam_station_ground_access))) {
			// station link
			id = Id.createLinkId(name_uam_station_link + from + "-" + to);
		} else if ((f.endsWith(name_uam_station_flight_level) && t.endsWith(name_uam_station_flight_level))
				|| (f.endsWith(name_uam_station_flight_level) && t.endsWith(name_uam_station_flight_level))) {
			// flight level link (horizontal)
			id = Id.createLinkId(name_uam_horizontal_link + from + "-" + to);
		} else if ((f.endsWith(name_uam_station_flight_access) && !t.endsWith(name_uam_station_flight_access))
				|| (!f.endsWith(name_uam_station_flight_access) && t.endsWith(name_uam_station_flight_access))) {
			// flight access link (vertical)
			id = Id.createLinkId(name_uam_vertical_link + from + "-" + to);
		} else if ((f.endsWith(name_uam_station_ground_access) && !t.endsWith(name_uam_station_ground_access))
				|| (!f.endsWith(name_uam_station_ground_access) && t.endsWith(name_uam_station_ground_access))) {
			// ground access link
			id = Id.createLinkId(name_uam_ground_link + from + "-" + to);
		} else
			throw new Exception("Unknown UAM link type.");

		if (length == NO_LENGTH)
			length = Math.max(CoordUtils.calcEuclideanDistance(fromNode.getCoord(), toNode.getCoord()) * detour_factor,
					min_link_length);

		Link link = network.getFactory().createLink(id, fromNode, toNode);
		link.setLength(length);
		link.setFreespeed(freespeed);
		link.setCapacity(capacity);
		link.setNumberOfLanes(permlanes);
		link.setAllowedModes(modes);

		try {
			network.addLink(link);
		} catch (Exception e) {
			// Avoid error on duplicate link entries (simply skip)
		}

	}

	private static Coord createCoord(double x, double y, double z) {
		if (use_z_values)
			return new Coord(x, y, z);
		else
			return new Coord(x, y);
	}

	private static String encode(String s) {
		return s.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
	}
}