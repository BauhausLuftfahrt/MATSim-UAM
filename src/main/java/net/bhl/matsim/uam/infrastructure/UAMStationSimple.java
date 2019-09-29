package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/**
 * This class defines the UAM Station and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMStationSimple implements UAMStation {

	// UAM infrastructure-specific properties
	private final int landingCapacity;
	private final int parkingCapacity;
	private final Link locationLink;
	private final Id<UAMStation> id;
	private final String name;
	// parameters added
	private final double preFlightTime;
	private final double postFlightTime;
	private final double defaultWaitTime;

	public UAMStationSimple(int landingCapacity, int parkingCapacity, double preFlightTime, double postFlightTime,
							double defaultWaitTime, Link locationLink, Id<UAMStation> id) {
		this(landingCapacity, parkingCapacity, preFlightTime, postFlightTime, defaultWaitTime, locationLink, id,
				id.toString());
	}

	public UAMStationSimple(int landingCapacity, int parkingCapacity, double preFlightTime, double postFlightTime,
							double defaultWaitTime, Link locationLink, Id<UAMStation> id, String name) {
		this.landingCapacity = landingCapacity;
		this.parkingCapacity = parkingCapacity;
		this.locationLink = locationLink;
		this.id = id;
		this.name = name;
		this.preFlightTime = preFlightTime;
		this.postFlightTime = postFlightTime;
		this.defaultWaitTime = defaultWaitTime;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getLandingCapacity() {
		return landingCapacity;
	}

	@Override
	public int getParkingCapacity() {
		return parkingCapacity;
	}

	@Override
	public Link getLocationLink() {
		return locationLink;
	}

	@Override
	public Id<UAMStation> getId() {
		return id;
	}

	@Override
	public double getPreFlightTime() {
		return preFlightTime;
	}

	@Override
	public double getPostFlightTime() {
		return postFlightTime;
	}

	@Override
	public double getDefaultWaitTime() {
		return defaultWaitTime;
	}

}
