package net.bhl.matsim.uam.analysis.trips.readers;

import java.util.Collection;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import net.bhl.matsim.uam.analysis.transit.TransitTripItem;
import net.bhl.matsim.uam.analysis.trips.TripItem;
import net.bhl.matsim.uam.analysis.trips.listeners.TripListener;

/**
 * This class is used to retrieve the a collection of {@link TripItem} by using
 * a simulation events file as input
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class EventsTripReader {
	final private TripListener tripListener;

	public EventsTripReader(TripListener tripListener) {
		this.tripListener = tripListener;
	}

	/**
	 * @param eventsPath the events file path
	 * @return A collection of {@link TripItem}.
	 */
	public Collection<TripItem> readTrips(String eventsPath) {
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(tripListener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);
		return tripListener.getTripItems();
	}
}
