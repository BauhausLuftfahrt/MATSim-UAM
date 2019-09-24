package net.bhl.matsim.uam.modechoice.estimation.uam;

/**
 * This class stores information about a UAM trip prediction.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CustomUAMPrediction {

	private double utility;
	private double travelTime;

	public CustomUAMPrediction(double utility, double travelTime) {
		this.utility = utility;
		this.travelTime = travelTime;
	}

	public double getUtility() {
		return utility;
	}

	public double getTravelTime() {
		return travelTime;
	}

}
