package net.bhl.matsim.uam.router.strategy;

import net.bhl.matsim.uam.data.UAMRoute;

import java.util.concurrent.ExecutionException;

import org.matsim.api.core.v01.population.Person;
import org.matsim.facilities.Facility;

/**
 * Defines classes responsible to generate a UAMRoute based on a specific
 * criteria (strategy).
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public interface UAMStrategy {
	UAMStrategyType getUAMStrategyType();

	/**
	 * @return The UAMRoute for the passenger based on the selected UAMStrategy
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime);

	enum UAMStrategyType {
		MAXUTILITY, MAXACCESSUTILITY, MINTRAVELTIME, MINACCESSTRAVELTIME, MINDISTANCE, MINACCESSDISTANCE, PREDEFINED
	}

}
