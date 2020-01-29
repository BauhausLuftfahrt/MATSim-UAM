package net.bhl.matsim.uam.analysis.traveltimes.utils;

import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;


public class TripItem {
  public Coord origin;
  public Coord destination;
  public double departureTime;
  public double travelTime;
  public String description;

	public double distance;
	public double accessTime;
	public double flightTime;
	public double egressTime;
	public String accessMode;
	public String egressMode;
	public String originStation;
	public String destinationStation;
}
