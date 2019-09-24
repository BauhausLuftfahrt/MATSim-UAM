package net.bhl.matsim.uam.analysis.uamdemand.readers;

import java.util.Collection;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import net.bhl.matsim.uam.analysis.uamdemand.listeners.UAMListener;
import net.bhl.matsim.uam.analysis.trips.TripItem;
import net.bhl.matsim.uam.analysis.uamdemand.UAMDemandItem;

/**
 * This class is used to retrieve the a collection of {@link UAMDemandItem} by
 * using a simulation events file as input
 * 
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
 */
public class EventsUAMReader {
	final private UAMListener uamListener;

	public EventsUAMReader(UAMListener uamListener) {
		this.uamListener = uamListener;
	}

	/**
	 * @param eventsPath the events file path
	 * @return A collection of {@link UAMDemandItem}.
	 */
	public Collection<UAMDemandItem> readUAMData(String eventsPath) {
		EventsManager eventsManager = EventsUtils.createEventsManager();
		eventsManager.addHandler(uamListener);
		new MatsimEventsReader(eventsManager).readFile(eventsPath);
		return uamListener.getUAMItems();
	}
}
