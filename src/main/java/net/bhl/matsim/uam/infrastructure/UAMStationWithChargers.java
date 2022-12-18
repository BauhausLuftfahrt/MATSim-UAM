package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/**
 * This class defines the UAM Station with chargers. *
 * 
 */
public class UAMStationWithChargers implements UAMStation {

	// UAM infrastructure-specific properties
	private final Link locationLink;
	private final Id<UAMStation> id;
	private final String name;
	// parameters added
	private final double preFlightTime;
	private final double postFlightTime;
	private final double defaultWaitTime;
	private final int numberOfChargers;
	private final double chargingSpeed;

	public UAMStationWithChargers(double preFlightTime, double postFlightTime,
							double defaultWaitTime, Link locationLink, Id<UAMStation> id,
							int numberOfChargers, double chargingSpeed) {
		this(preFlightTime, postFlightTime, defaultWaitTime, locationLink, id,
				id.toString(), numberOfChargers, chargingSpeed);
	}

	public UAMStationWithChargers(double preFlightTime, double postFlightTime,
							double defaultWaitTime, Link locationLink, Id<UAMStation> id, String name,
							int numberOfChargers, double chargingSpeed) {
		this.locationLink = locationLink;
		this.id = id;
		this.name = name;
		this.preFlightTime = preFlightTime;
		this.postFlightTime = postFlightTime;
		this.defaultWaitTime = defaultWaitTime;
		this.numberOfChargers = numberOfChargers;
		this.chargingSpeed = chargingSpeed;
	}

	@Override
	public String getName() {
		return name;
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

	public int getNumberOfChargers() {
		return numberOfChargers;
	}

	public double getChargingSpeed() {
		return chargingSpeed;
	}
}
