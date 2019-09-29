package net.bhl.matsim.uam.analysis.trips.listeners;

import net.bhl.matsim.uam.analysis.trips.TripItem;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;

import java.util.LinkedList;
import java.util.List;

/**
 * This class stores information about a trip performed.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class TripListenerItem extends TripItem {
	public List<PlanElement> elements = new LinkedList<>();
	public List<Id<Link>> route = new LinkedList<>();

	public TripListenerItem(Id<Person> personId, int personTripId, Coord origin, double startTime,
							String startPurpose) {
		super(personId, personTripId, origin, null, startTime, Double.NaN, Double.NaN, "unknown", startPurpose,
				"unknown", false, Double.NaN);
	}
}
