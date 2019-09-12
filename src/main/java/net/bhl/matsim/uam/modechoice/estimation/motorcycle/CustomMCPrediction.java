package net.bhl.matsim.uam.modechoice.estimation.motorcycle;

import org.matsim.core.population.routes.NetworkRoute;

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
