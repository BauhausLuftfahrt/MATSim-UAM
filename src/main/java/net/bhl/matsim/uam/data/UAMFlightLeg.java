package net.bhl.matsim.uam.data;

import org.matsim.api.core.v01.network.Link;

import java.util.List;

public class UAMFlightLeg {
	public final double travelTime;
	public final double distance;
	public final List<Link> links;

	public UAMFlightLeg(double travelTime, double distance, List<Link> links) {
		this.travelTime = travelTime;
		this.distance = distance;
		this.links = links;
	}
}