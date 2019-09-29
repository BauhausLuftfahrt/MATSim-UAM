package net.bhl.matsim.uam.analysis.transit.listeners;

import org.matsim.api.core.v01.Coord;

import net.bhl.matsim.uam.analysis.transit.TransitTripItem;

public class TransitTripListenerItem extends TransitTripItem {
	public String mode = "unknown";
	public Coord intermediateOrigin;
	public double legDepartureTime;
	public boolean waitingForFirstTransitEvent = true;
}
