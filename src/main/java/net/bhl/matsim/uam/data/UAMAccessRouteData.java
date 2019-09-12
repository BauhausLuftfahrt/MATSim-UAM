package net.bhl.matsim.uam.data;

import net.bhl.matsim.uam.infrastructure.UAMStation;

/**
 * This class is used to store data from access/egress legs to/from UAMStations
 * 
 * @author Aitan Militao
 */
public class UAMAccessRouteData {
	private double accessDistance;  
	private double accessTravelTime;
	private String accessBestModeDistance;
	private String accessBestModeTime;
	private UAMStation station;

	public UAMAccessRouteData(double accessDistance, double accessTravelTime, String accessModeDistance, String accessModeTime, UAMStation station) {
		this.accessDistance = accessDistance;
		this.accessTravelTime = accessTravelTime;
		this.accessBestModeDistance = accessModeDistance;
		this.accessBestModeTime = accessModeTime;
		
		this.station = station;
	}

	public double getDistance() {
		return accessDistance;
	}
	
	public void setDistance(double accessDistance) {
		this.accessDistance = accessDistance;
	}

	public double getAccessTravelTime() {
		return accessTravelTime;
	}

	public String getAccessModeDistance() {
		return accessBestModeDistance;
	}
		
	public void setAccessBestModeDistance(String accessBestModeDistance) {
		this.accessBestModeDistance = accessBestModeDistance;
	}

	public String getAccessModeTime() {
		return accessBestModeTime;
	}

	public UAMStation getStation() {
		return station;
	}
	
	
}
