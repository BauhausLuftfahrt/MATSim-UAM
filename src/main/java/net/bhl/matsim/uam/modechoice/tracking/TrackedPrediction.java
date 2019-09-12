package net.bhl.matsim.uam.modechoice.tracking;

public class TrackedPrediction {
	public final double tripTravelTime;
	public final int numberOfLinks;

	public TrackedPrediction(double tripTravelTime, int numberOfLinks) {
		this.tripTravelTime = tripTravelTime;
		this.numberOfLinks = numberOfLinks;
	}
}
