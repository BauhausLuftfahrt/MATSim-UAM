package net.bhl.matsim.uam.analysis.trips.readers;

import net.bhl.matsim.uam.analysis.trips.TripItem;
import net.bhl.matsim.uam.analysis.trips.listeners.TripListener;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import java.util.Collection;

public class EventsTripReader {
	final private TripListener tripListener;

	public EventsTripReader(TripListener tripListener) {
		this.tripListener = tripListener;
	}

	public Collection<TripItem> readTrips(String eventsPath) {
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(tripListener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);
		return tripListener.getTripItems();
	}
}
