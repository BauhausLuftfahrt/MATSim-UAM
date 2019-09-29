package net.bhl.matsim.uam.scenario.network;

// Adjusted from RunCreateNetworkSHP.java by matsim-code-examples

import com.vividsolutions.jts.geom.Coordinate;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.*;

public class RunNetworkToSHP {

	public static void main(String[] args) throws Exception {

		System.out.println("ARGS: input.xml EPSG:00000 allowed-modes*");
		System.out.println("(* optional)");

		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(args[0]);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();

		if (args.length == 3) {
			TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
			Set<String> modes = new HashSet<>();
			modes.addAll(new ArrayList<String>(Arrays.asList(args[2].split(","))));
			Network newNetwork = NetworkUtils.createNetwork();
			filter.filter(newNetwork, modes);
			network = newNetwork;
		}

		System.out.println(args[1]);
		CoordinateReferenceSystem crs = null;
		try {
			crs = MGC.getCRS(args[1]); // EPSG:code
		} catch (IllegalArgumentException e) {
			System.err.println("Old geotools version is not compatible with Java 9");
			e.printStackTrace();
			System.exit(1);
		}


		Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
		PolylineFeatureFactory linkFactory = new PolylineFeatureFactory.Builder().
				setCrs(crs).
				setName("link").
				addAttribute("ID", String.class).
				addAttribute("fromID", String.class).
				addAttribute("toID", String.class).
				addAttribute("length", Double.class).
				addAttribute("type", String.class).
				addAttribute("capacity", Double.class).
				addAttribute("freespeed", Double.class).
				addAttribute("modes", String.class).
				create();

		for (Link link : network.getLinks().values()) {
			Coordinate fromNodeCoordinate = new Coordinate(link.getFromNode().getCoord().getX(), link.getFromNode().getCoord().getY());
			Coordinate toNodeCoordinate = new Coordinate(link.getToNode().getCoord().getX(), link.getToNode().getCoord().getY());
			Coordinate linkCoordinate = new Coordinate(link.getCoord().getX(), link.getCoord().getY());
			SimpleFeature ft = linkFactory.createPolyline(new Coordinate[]{fromNodeCoordinate, linkCoordinate, toNodeCoordinate},
					new Object[]{link.getId().toString(),
							link.getFromNode().getId().toString(),
							link.getToNode().getId().toString(),
							link.getLength(),
							NetworkUtils.getType(link),
							link.getCapacity(),
							link.getFreespeed(),
							link.getAllowedModes()},
					null);
			features.add(ft);
		}
		ShapeFileWriter.writeGeometries(features, args[0] + "_links.shp");

		features = new ArrayList<SimpleFeature>();
		PointFeatureFactory nodeFactory = new PointFeatureFactory.Builder().
				setCrs(crs).
				setName("nodes").
				addAttribute("ID", String.class).
				create();

		for (Node node : network.getNodes().values()) {
			SimpleFeature ft = nodeFactory.createPoint(node.getCoord(),
					new Object[]{node.getId().toString()},
					null);
			features.add(ft);
		}
		ShapeFileWriter.writeGeometries(features, args[0] + "_nodes.shp");
		System.out.println("done.");
	}
}