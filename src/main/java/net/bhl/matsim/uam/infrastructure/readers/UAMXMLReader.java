package net.bhl.matsim.uam.infrastructure.readers;

import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStationSimple;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.infrastructure.UAMVehicleType;
import org.jfree.util.Log;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.Time;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * This class reads reads the UAM xml file containing the parameters for UAM
 * simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMXMLReader extends MatsimXmlParser {

	public final Network network;
	private Map<Id<UAMStation>, UAMStation> stations = new HashMap<Id<UAMStation>, UAMStation>();
	private Map<Id<Vehicle>, UAMVehicle> vehicles = new HashMap<Id<Vehicle>, UAMVehicle>(); // added
	private Map<Id<UAMVehicleType>, UAMVehicleType> vehicleTypes = new HashMap<Id<UAMVehicleType>, UAMVehicleType>(); // added
	private Map<String, Double> mapVehicleHorizontalSpeeds = new HashMap<>();
	private Map<String, Double> mapVehicleVerticalSpeeds = new HashMap<>();

	public UAMXMLReader(Network uamNetwork) {
		this.network = uamNetwork;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (name.equals("station")) {
			// Get station ID
			Id<UAMStation> id = Id.create(atts.getValue("id"), UAMStation.class);

			String stationName = atts.getValue("name");
			if (stationName == null)
				stationName = id.toString();

			// Get landing station capacity for simultaneous VTOL UNUSED
			int landingCapacity = Integer.parseInt(atts.getValue("landingcap"));

			// Get parking station capacity for simultaneous VTOL vehicle parking UNUSED
			int parkingCapacity = 0;
			// Integer.parseInt(atts.getValue("parkingcap"));

			// Get station attributes: preFlighTime, postFlightTime, and defaultWaitTime
			double preFlightTime = Double.parseDouble(atts.getValue("preflighttime"));
			double postFlightTime = Double.parseDouble(atts.getValue("postflighttime"));
			double defaultWaitTime = Double.parseDouble(atts.getValue("defaultwaittime"));

			// Get station access/egress link or coordinates, to find nearest link
			String linkName = atts.getValue("link");
			Link link;
			if (linkName != null) {
				// A network link, to which the landing station is connected has been provided
				List<Link> links = NetworkUtils.getLinks(network, linkName);
				link = links.get(0);
			} else {
				// No network link has been provided, automatically assign the nearest link
				Double x = Double.parseDouble(atts.getValue("x"));
				Double y = Double.parseDouble(atts.getValue("y"));
				link = NetworkUtils.getNearestLink(network, new Coord(x, y));
			}

			// Create UAm station and register it on station map
			UAMStation ls = new UAMStationSimple(landingCapacity, parkingCapacity, preFlightTime, postFlightTime,
					defaultWaitTime, link, id, stationName);
			stations.put(id, ls);

		} else if (name.equals("vehicleType")) {
			Id<UAMVehicleType> id = Id.create(atts.getValue("id"), UAMVehicleType.class);
			int capacity = Integer.parseInt(atts.getValue("capacity"));
			double horizontalSpeed = Double.parseDouble(atts.getValue("cruisespeed"));
			double verticalSpeed = Double.parseDouble(atts.getValue("verticalspeed"));
			// Get vehicle type attributes: boardingTime, deboardingTime and turnAroundTime
			double boardingTime = Double.parseDouble(atts.getValue("boardingtime"));
			double deboardingTime = Double.parseDouble(atts.getValue("deboardingtime"));
			double turnAroundTime = Double.parseDouble(atts.getValue("turnaroundtime"));
			UAMVehicleType vehicleType = new UAMVehicleType(id, capacity, horizontalSpeed, verticalSpeed, boardingTime,
					deboardingTime, turnAroundTime);
			vehicleTypes.put(id, vehicleType);
		} else if (name.equals("vehicle")) {
			Id<Vehicle> id = Id.create(atts.getValue("id"), Vehicle.class);
			Id<UAMVehicleType> vehicleTypeId = Id.create(atts.getValue("type"), UAMVehicleType.class);

			// gets starttime and endtime
			double starttime = Time.parseTime(atts.getValue("starttime"));
			double endtime = Time.parseTime(atts.getValue("endtime"));
			// double width = Double.parseDouble(atts.getValue("width"));
			// double length = Double.parseDouble(atts.getValue("length"));
			// int range = Integer.parseInt(atts.getValue("range"));

			// gets initial station
			Id<UAMStation> stationid = Id.create(atts.getValue("initialstation"), UAMStation.class);

			// liftofftime, width, length, range);
			double horizontalSpeed = vehicleTypes.get(vehicleTypeId).getCruiseSpeed();
			double verticalSpeed = vehicleTypes.get(vehicleTypeId).getVerticalSpeed();
			int capacity = vehicleTypes.get(vehicleTypeId).getCapacity();

			this.mapVehicleVerticalSpeeds.put(id.toString(), verticalSpeed);
			this.mapVehicleHorizontalSpeeds.put(id.toString(), horizontalSpeed);

			try {
				UAMVehicle vehicle = new UAMVehicle(id, stationid, this.stations.get(stationid).getLocationLink(), capacity,
						starttime, endtime, vehicleTypes.get(vehicleTypeId));
				vehicles.put(id, vehicle);
			} catch (NullPointerException e) {
				Log.warn("UAM vehicle " + id + " could not be added. Check correct initial station or vehicle type.");
			}
		} else {
			Log.warn("There was an error parsing the UAM xml-file. Non vehicle/station element found.");
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {

	}

	public Map<Id<UAMStation>, UAMStation> getStations() {
		return stations;
	}

	public Map<Id<Vehicle>, UAMVehicle> getVehicles() {
		return vehicles;
	}

	public Map<String, Double> getMapVehicleHorizontalSpeeds() {
		return this.mapVehicleHorizontalSpeeds;
	}

	public Map<String, Double> getMapVehicleVerticalSpeeds() {
		return this.mapVehicleVerticalSpeeds;
	}

}
