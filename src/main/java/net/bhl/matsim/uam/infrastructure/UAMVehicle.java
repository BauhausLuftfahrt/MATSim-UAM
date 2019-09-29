package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;

/**
 * This class defines the VTOL vehicle and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMVehicle extends VehicleImpl {

	// VTOL vehicle-specific properties
	private final double width; // [m]
	private final double length; // [m]
	private final double range; // [m]
	private final Id<UAMStation> initialStationId;
	private final UAMVehicleType vehicleType;

	public UAMVehicle(Id<Vehicle> id, Id<UAMStation> stationId, Link startLink, double capacity, double t0, double t1,
					  double width, double length, double range, UAMVehicleType vehicleType) {

		super(id, startLink, capacity, t0, t1);
		this.initialStationId = stationId;
		this.width = width;
		this.length = length;
		this.range = range;

		this.vehicleType = vehicleType;
	}

	public UAMVehicle(Id<Vehicle> id, Id<UAMStation> stationId, Link startLink, double capacity, double t0, double t1,
					  UAMVehicleType vehicleType) {

		super(id, startLink, capacity, t0, t1);
		this.initialStationId = stationId;
		this.width = 0;
		this.length = 0;
		this.range = 0;

		this.vehicleType = vehicleType;

	}

	public Id<UAMStation> getInitialStationId() {
		return initialStationId;
	}

	public double getWidth() {
		return width;
	}

	public double getLength() {
		return length;
	}

	public double getRange() {
		return range;
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
