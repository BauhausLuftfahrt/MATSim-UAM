package net.bhl.matsim.uam.modechoice.estimation.motorcycle;

import org.matsim.core.population.routes.NetworkRoute;

/**
 * This class stores information about a motorcycle trip prediction.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CustomMCPrediction {
	final public double distance;
	final public double travelTime;
	final public NetworkRoute route;

	public CustomMCPrediction(double distance, double travelTime, NetworkRoute route) {
		this.distance = distance;
		this.travelTime = travelTime;
		this.route = route;
	}
}
