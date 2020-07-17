package net.bhl.matsim.uam.infrastructure;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/**
 * An interface to define UAM Stations methods.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public interface UAMStation {

	Id<UAMStation> getId();

	/**
	 * This method is used to retrieve the time needed by the passenger to get from
	 * vertiport entry to his/her aircraft seat in seconds.
	 *
	 * @return the time needed by the passenger before entering the aircraft
	 */
	double getPreFlightTime();

	/**
	 * This method is used to retrieve the Time needed by the passenger after
	 * leaving the aircraft to leave the vertiport in seconds.
	 *
	 * @return the time needed by the passenger after the flight to start the next
	 * leg
	 */
	double getPostFlightTime();

	/**
	 * This method is used to retrieve the default waiting time
	 *
	 * @return the default waiting time
	 */
	double getDefaultWaitTime();

	/**
	 * TODO
	 */
	Link getLocationLink();

	String getName();

}
