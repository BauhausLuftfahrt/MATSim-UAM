package net.bhl.matsim.uam.modechoice.estimation.pt;

import org.matsim.api.core.v01.population.Leg;

import java.util.List;

/**
 * This class stores information about a Public transport trip prediction.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomPublicTransportPrediction {
	final public int numberOfTransfers;
	final public boolean isOnlyWalk;
	final public double transferTime;
	final public double accessEgressTime;
	final public double inVehicleTime;
	final public double inVehicleDistance;
	final public double crowflyDistance;
	final public List<Leg> legs;

	public CustomPublicTransportPrediction(double inVehicleTime, double inVehicleDistance, double accessEgressTime,
										   double transferTime, int numberOfTransfers, boolean isOnlyWalk, double crowflyDistance, List<Leg> legs) {
		this.inVehicleTime = inVehicleTime;
		this.inVehicleDistance = inVehicleDistance;
		this.accessEgressTime = accessEgressTime;
		this.transferTime = transferTime;
		this.numberOfTransfers = numberOfTransfers;
		this.isOnlyWalk = isOnlyWalk;
		this.crowflyDistance = crowflyDistance;
		this.legs = legs;
	}
}
