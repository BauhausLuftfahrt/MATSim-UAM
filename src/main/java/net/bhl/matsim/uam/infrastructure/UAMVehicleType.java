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
	private final double range; // [m]
	private final double cruiseSpeed; // [m/s]
	private final double verticalSpeed; // [m/s]
	private final double boardingTime; // [s]
	private final double deboardingTime; // [s]
	private final double turnAroundTime; // [s]
	private final double maximumCharge;
	private final double energyConsumptionVertical;
	private final double energyConsumptionHorizontal;

	

	public UAMVehicleType(Id<UAMVehicleType> id, int capacity, double range, double cruiseSpeed, double verticalSpeed,
						  double boardingTime, double deboardingTime, double turnAroundTime,
						  double energyConsumptionVertical, double energyConsumptionHorizontal, double maximumCharge) {
		this.id = id;
		this.capacity = capacity;
		this.range = range;
		this.cruiseSpeed = cruiseSpeed;
		this.verticalSpeed = verticalSpeed;
		this.boardingTime = boardingTime;
		this.deboardingTime = deboardingTime;
		this.turnAroundTime = turnAroundTime;
		this.energyConsumptionHorizontal = energyConsumptionHorizontal;
		this.energyConsumptionVertical = energyConsumptionVertical;
		this.maximumCharge = maximumCharge;
	}

	public int getCapacity() { // used only in the reader, given that the UAMVehicle superclass already has it
		return this.capacity;
	}

	public double getRange() { // used only in the reader, given that the UAMVehicle superclass already has it
		return this.range;
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

	public double getMaximumCharge() {
		return maximumCharge;
	}

	public double getEnergyConsumptionVertical() {
		return energyConsumptionVertical;
	}

	public double getEnergyConsumptionHorizontal() {
		return energyConsumptionHorizontal;
	}
}
