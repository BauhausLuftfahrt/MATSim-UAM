package net.bhl.matsim.uam.analysis.uamdemand.listeners;

public class ChargingItem {
	public String vehicleId;
	public double startingTime;
	public double endTime;
	public double startingCharge;
	public String stationId;

	public ChargingItem(String vehicleId, double startingTime, double endTime, double startingCharge, String stationId) {
		this.vehicleId = vehicleId;
		this.startingTime = startingTime;
		this.startingCharge = startingCharge;
		this.endTime = endTime;
		this.stationId = stationId;

	}
}
