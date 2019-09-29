package net.bhl.matsim.uam.analysis.transit.listeners;

import net.bhl.matsim.uam.analysis.transit.TransitTripItem;
import org.matsim.api.core.v01.Coord;

public class TransitTripListenerItem extends TransitTripItem {
	public String mode = "unknown";
	public Coord intermediateOrigin;
	public double legDepartureTime;
	public boolean waitingForFirstTransitEvent = true;
}
