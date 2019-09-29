package net.bhl.matsim.uam.analysis.transit.readers;

import ch.ethz.matsim.baseline_scenario.transit.events.PublicTransitEvent;
import ch.ethz.matsim.baseline_scenario.transit.events.PublicTransitEventMapper;
import net.bhl.matsim.uam.analysis.transit.TransitTripItem;
import net.bhl.matsim.uam.analysis.transit.listeners.TransitTripListener;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;

import java.util.Collection;

public class EventsTransitTripReader {
	final private TransitTripListener tripListener;

	public EventsTransitTripReader(TransitTripListener tripListener) {
		this.tripListener = tripListener;
	}

	public Collection<TransitTripItem> readTrips(String eventsPath) {
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(tripListener);

		EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
		reader.addCustomEventMapper(PublicTransitEvent.TYPE, new PublicTransitEventMapper());
		reader.readFile(eventsPath);

		return tripListener.getTransitTripItems();
	}
}
