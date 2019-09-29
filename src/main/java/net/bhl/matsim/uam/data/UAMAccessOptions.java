package net.bhl.matsim.uam.data;

import net.bhl.matsim.uam.infrastructure.UAMStation;

/**
 * This class is used to store data from access/egress legs to/from UAMStations.
 * 
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class UAMAccessOptions {
	private double accessDistance;  
	private double accessTravelTime;
	private String accessBestModeDistance;
	private String accessBestModeTime;
	private UAMStation station;

	public UAMAccessOptions(double accessDistance, double accessTravelTime, String accessModeDistance,
							String accessModeTime, UAMStation station) {
		this.accessDistance = accessDistance;
		this.accessTravelTime = accessTravelTime;
		this.accessBestModeDistance = accessModeDistance;
		this.accessBestModeTime = accessModeTime;
		this.station = station;
	}

	public double getShortestAccessDistance() {
		return accessDistance;
	}

	public double getFastestAccessTime() {
		return accessTravelTime;
	}

	public String getShortestDistanceMode() {
		return accessBestModeDistance;
	}

	public String getFastestTimeMode() {
		return accessBestModeTime;
	}

	public UAMStation getStation() {
		return station;
	}
	
	
}
