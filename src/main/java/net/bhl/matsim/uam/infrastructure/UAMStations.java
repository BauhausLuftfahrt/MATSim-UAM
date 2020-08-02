package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import java.util.Collection;
import java.util.Map;

/**
 * Class to map stations and its locations in the network.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMStations {
	private Map<Id<UAMStation>, UAMStation> stations;
	private QuadTree<UAMStation> spatialStations;

	public UAMStations(Map<Id<UAMStation>, UAMStation> stations, Network network) {

		this.stations = stations;
		double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minx, miny, maxx, maxy

		spatialStations = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);
		for (UAMStation station : stations.values()) {
			double x = station.getLocationLink().getCoord().getX();
			double y = station.getLocationLink().getCoord().getY();
			spatialStations.put(x, y, station);
		}
	}

	public Map<Id<UAMStation>, UAMStation> getUAMStations() {
		return stations;
	}

	public UAMStation getNearestUAMStation(Link link) {
		return spatialStations.getClosest(link.getCoord().getX(), link.getCoord().getY());
	}

	public Collection<UAMStation> getUAMStationsInRadius(Coord coord, double radius) {
		return spatialStations.getDisk(coord.getX(), coord.getY(), radius);
	}
}
