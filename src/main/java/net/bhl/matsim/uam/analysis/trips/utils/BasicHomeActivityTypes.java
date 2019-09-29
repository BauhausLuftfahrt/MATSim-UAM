package net.bhl.matsim.uam.analysis.trips.utils;

/**
 * An implementation of {@link HomeActivityTypes} for basic home activities.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class BasicHomeActivityTypes implements HomeActivityTypes {
	@Override
	public boolean isHomeActivity(String activityType) {
		return activityType.contains("home");
	}
}
