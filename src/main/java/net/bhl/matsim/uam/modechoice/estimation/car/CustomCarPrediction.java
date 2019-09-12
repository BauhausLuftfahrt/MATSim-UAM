package net.bhl.matsim.uam.modechoice.estimation.car;

import org.matsim.core.population.routes.NetworkRoute;

public class CustomCarPrediction {
	final public double distance;
	final public double travelTime;
	final public NetworkRoute route;

	public CustomCarPrediction(double distance, double travelTime, NetworkRoute route) {
		this.distance = distance;
		this.travelTime = travelTime;
		this.route = route;
	}
}
