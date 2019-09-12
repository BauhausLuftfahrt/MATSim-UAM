package net.bhl.matsim.uam.analysis.trips.utils;

public class BasicHomeActivityTypes implements HomeActivityTypes {
	@Override
	public boolean isHomeActivity(String activityType) {
		return activityType.contains("home");
	}
}
