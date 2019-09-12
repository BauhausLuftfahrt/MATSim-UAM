package net.bhl.matsim.uam.router.strategy;

import org.matsim.api.core.v01.population.Person;
import org.matsim.facilities.Facility;

import net.bhl.matsim.uam.data.UAMRoute;

/**
* @author Aitan Militao
*  */
public interface UAMStrategy {
	public static enum UAMStrategyType {
		MAXUTILITY, MAXACCESSUTILITY, MINTRAVELTIME, MINACCESSTRAVELTIME, MINDISTANCE, MINACCESSDISTANCE, PREDEFINED;
	}

	UAMStrategyType getUAMStrategyType();

	/**
	 * @return The UAMRoute for the passenger based on the selected UAMStrategy
	 */
	UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime);
		
}
