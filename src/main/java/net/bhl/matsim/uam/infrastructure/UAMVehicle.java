package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleImpl;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;

/**
 * This class defines the VTOL vehicle and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMVehicle extends DvrpVehicleImpl {

	// VTOL vehicle-specific properties
	private final Id<UAMStation> initialStationId;
	private final UAMVehicleType vehicleType;

	public UAMVehicle(ImmutableDvrpVehicleSpecification specification, Link startLink, Id<UAMStation> stationId,
					  UAMVehicleType vehicleType) {
		super(specification, startLink);
		this.initialStationId = stationId;
		this.vehicleType = vehicleType;
	}

	public Id<UAMStation> getInitialStationId() {
		return initialStationId;
	}

	/**
	 * This method is used to retrieve the vehicle cruise speed during level flight.
	 *
	 * @return vehicle cruise speed in meter/second
	 */
	public double getCruiseSpeed() {
		return vehicleType.getCruiseSpeed();
	}

	/**
	 * This method is used to retrieve the vehicle vertical speed during take-off
	 * and landing.
	 *
	 * @return vehicle vertical speed in meter/second
	 */
	public double getVerticalSpeed() {
		return vehicleType.getVerticalSpeed();
	}

	/**
	 * This method is used to retrieve the Time for the passenger to board in the
	 * aircraft in seconds. Used in the <code>UAMPickUpTask</code>.
	 *
	 * @return the passenger boarding time in the aircraft
	 */
	public double getBoardingTime() {
		return vehicleType.getboardingTime();
	}

	/**
	 * This method is used to retrieve the Time for the passenger to board off from
	 * the aircraft in seconds. Used in the <code>UAMDropOffTask</code>.
	 *
	 * @return the passenger deboarding time in from the aircraft
	 */
	public double getDeboardingTime() {
		return vehicleType.getDeboardingTime();
	}

	/**
	 * This method is used to retrieve the Time needed for the UAM Vehicle to
	 * perform the <code>TurnAroundTask</code> (unavailable for other trips after
	 * landing)
	 *
	 * @return the UAM Vehicle turn around time (time unavailable for other trips
	 * after landing)
	 */
	public double getTurnAroundTime() {
		return vehicleType.getTurnAroundTime();
	}

}
