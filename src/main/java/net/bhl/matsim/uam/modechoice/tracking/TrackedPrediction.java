package net.bhl.matsim.uam.modechoice.tracking;

/**
 * This class stores information about a tracked prediction for the {@link TrackingModeChoiceModel}.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class TrackedPrediction {
	public final double tripTravelTime;
	public final int numberOfLinks;

	public TrackedPrediction(double tripTravelTime, int numberOfLinks) {
		this.tripTravelTime = tripTravelTime;
		this.numberOfLinks = numberOfLinks;
	}
}
