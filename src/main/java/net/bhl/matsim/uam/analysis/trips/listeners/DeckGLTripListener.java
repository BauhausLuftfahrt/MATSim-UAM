package net.bhl.matsim.uam.analysis.trips.listeners;

import net.bhl.matsim.uam.analysis.trips.DeckGLTripItem;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A listener that retrieves information from trip events and stores in
 * <{@link DeckGLTripItem} format.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class DeckGLTripListener implements LinkEnterEventHandler, LinkLeaveEventHandler {
	final private Map<Id<Vehicle>, List<DeckGLTripItem>> deckGLTrips;
	final private Network network;
	final private long minTime;
	final private long maxTime;

	public DeckGLTripListener(Network network) {
		this(network, 0, Long.MAX_VALUE);
	}

	public DeckGLTripListener(Network network, long minTime, long maxTime) {
		this.deckGLTrips = new HashMap<Id<Vehicle>, List<DeckGLTripItem>>();
		this.network = network;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}

	public Map<Id<Vehicle>, List<DeckGLTripItem>> getDeckGLTripItems() {
		return deckGLTrips;
	}

	@Override
	public void reset(int iteration) {
		deckGLTrips.clear();
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		long time = (long) event.getTime();

		if (minTime <= time && time <= maxTime) {
			List<DeckGLTripItem> trips = deckGLTrips.get(event.getVehicleId());

			if (trips == null) {
				trips = new LinkedList<DeckGLTripItem>();
				deckGLTrips.put(event.getVehicleId(), trips);
			}

			Coord location = network.getLinks().get(event.getLinkId()).getFromNode().getCoord();
			trips.add(new DeckGLTripItem(location, time, minTime));
		}
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		long time = (long) event.getTime();

		if (minTime <= time && time <= maxTime) {
			List<DeckGLTripItem> trips = deckGLTrips.get(event.getVehicleId());

			if (trips == null) {
				trips = new LinkedList<DeckGLTripItem>();
				deckGLTrips.put(event.getVehicleId(), trips);
			}

			Coord location = network.getLinks().get(event.getLinkId()).getToNode().getCoord();
			trips.add(new DeckGLTripItem(location, time, minTime));
		}
	}
}
