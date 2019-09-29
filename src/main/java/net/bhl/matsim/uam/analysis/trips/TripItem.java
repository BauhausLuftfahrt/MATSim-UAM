package net.bhl.matsim.uam.analysis.trips;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

/**
 * This class stores information about a trip.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class TripItem {
	public Id<Person> personId;
	public int personTripId;
	public Coord origin;
	public Coord destination;
	public double startTime;
	public double travelTime;
	public double networkDistance;
	public String mode;
	public String followingPurpose;
	public String preceedingPurpose;
	public boolean returning;
	public double crowflyDistance;

	public TripItem(Id<Person> personId, int personTripId, Coord origin, Coord destination, double startTime,
					double travelTime, double networkDistance, String mode, String preceedingPurpose, String followingPurpose,
					boolean returning, double crowflyDistance) {
		this.personId = personId;
		this.personTripId = personTripId;
		this.origin = origin;
		this.destination = destination;
		this.startTime = startTime;
		this.travelTime = travelTime;
		this.networkDistance = networkDistance;
		this.mode = mode;
		this.followingPurpose = followingPurpose;
		this.preceedingPurpose = preceedingPurpose;
		this.returning = returning;
		this.crowflyDistance = crowflyDistance;
	}
}
