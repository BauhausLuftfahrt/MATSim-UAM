package net.bhl.matsim.uam.modechoice.utils;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.algorithms.TripsToLegsAlgorithm;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.pt.PtConstants;

/**
 * This class simplifies the population plans by using
 * {@link TripsToLegsAlgorithm} class.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class RemoveRoutes {
	public void run(Population population) {
		StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);
		MainModeIdentifier mainModeIdentifier = new MainModeIdentifierImpl();
		TripsToLegsAlgorithm algorithm = new TripsToLegsAlgorithm(stageActivityTypes, mainModeIdentifier);

		for (Person person : population.getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				algorithm.run(plan);
			}
		}
	}
}
