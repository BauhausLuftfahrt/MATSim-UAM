package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;

/**
 * This class defines the VTOL vehicle type and its properties.
 * 
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class UAMVehicleType {
	private final Id<UAMVehicleType> id;
	private final int capacity;
	private final double cruiseSpeed; // [m/s]
	private final double verticalSpeed; // [m/s]
	private final double boardingTime; // [s]
	private final double deboardingTime; // [s]
	private final double turnAroundTime; // [s]

	public UAMVehicleType(Id<UAMVehicleType> id, int capacity, double cruiseSpeed, double verticalSpeed,
			double boardingTime, double deboardingTime, double turnAroundTime) {
		this.id = id;
		this.capacity = capacity;
		this.cruiseSpeed = cruiseSpeed;
		this.verticalSpeed = verticalSpeed;
		this.boardingTime = boardingTime;
		this.deboardingTime = deboardingTime;
		this.turnAroundTime = turnAroundTime;
	}

	public int getCapacity() { // used only in the reader, given that the UAMVehicle superclass already has it
		return this.capacity;
	}

	public double getCruiseSpeed() {
		return this.cruiseSpeed;
	}

	public double getVerticalSpeed() {
		return this.verticalSpeed;
	}

	public double getboardingTime() {
		return this.boardingTime;
	}

	public double getDeboardingTime() {
		return this.deboardingTime;
	}

	public double getTurnAroundTime() {
		return this.turnAroundTime;
	}
}
