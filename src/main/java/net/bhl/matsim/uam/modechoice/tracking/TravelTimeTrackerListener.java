package net.bhl.matsim.uam.modechoice.tracking;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class provides output for the {@link TrackingModeChoiceModel} usage.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
@Singleton
public class TravelTimeTrackerListener
		implements IterationEndsListener, PersonDepartureEventHandler, PersonArrivalEventHandler {
	final private TravelTimeTracker tracker;
	final private OutputDirectoryHierarchy directoryHierarchy;

	@Inject
	public TravelTimeTrackerListener(TravelTimeTracker tracker, OutputDirectoryHierarchy directoryHierarchy) {
		this.tracker = tracker;
		this.directoryHierarchy = directoryHierarchy;
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		try {
			tracker.write(new File(
					directoryHierarchy.getIterationFilename(event.getIteration(), "tracked_travel_times.csv")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		tracker.resetTravelTimes();
	}

	final private Map<Id<Person>, Double> departureTimes = new HashMap<>();

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if (event.getLegMode().equals("car")) {
			departureTimes.put(event.getPersonId(), event.getTime());
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		if (event.getLegMode().equals("car")) {
			Double departureTime = departureTimes.remove(event.getPersonId());

			if (departureTime != null) {
				tracker.addObservation(event.getPersonId(), event.getTime() - departureTime);
			}
		}
	}
}
