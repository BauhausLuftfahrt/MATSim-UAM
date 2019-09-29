package net.bhl.matsim.uam.modechoice.utils;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.TripStructureUtils;

/**
 * This class filter the input plans based on the number of long trips that a
 * person has.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class LongPlanFilter {
	final private Logger logger = Logger.getLogger(LongPlanFilter.class);
	final private long maximumNumberOfTrips;
	final private StageActivityTypes stageActivityTypes;

	public LongPlanFilter(long maximumNumberOfTrips, StageActivityTypes stageActivityTypes) {
		this.maximumNumberOfTrips = maximumNumberOfTrips;
		this.stageActivityTypes = stageActivityTypes;
	}

	public void run(Population population) {
		Iterator<? extends Person> iterator = population.getPersons().values().iterator();

		logger.info(String.format("Removing persons with long plans (> %d trips) ...", maximumNumberOfTrips));
		long numberOfRemovedPersons = 0;
		long numberOfPersons = population.getPersons().size();

		while (iterator.hasNext()) {
			Person person = iterator.next();

			int numberOfRelevantTrips = 0;

			for (TripStructureUtils.Trip trip : TripStructureUtils.getTrips(person.getSelectedPlan(),
					stageActivityTypes)) {
				if (!trip.getOriginActivity().getType().equals("outside")
						&& !trip.getDestinationActivity().getType().equals("outside")) {
					numberOfRelevantTrips++;
				}
			}

			if (numberOfRelevantTrips > maximumNumberOfTrips) {
				iterator.remove();
				population.getPersonAttributes().removeAllAttributes(person.getId().toString());
				numberOfRemovedPersons++;
			}
		}

		logger.info(String.format("Removed %d/%d persons with long trips (%.2f%%)", numberOfRemovedPersons,
				numberOfPersons, 100.0 * numberOfRemovedPersons / numberOfPersons));
	}
}