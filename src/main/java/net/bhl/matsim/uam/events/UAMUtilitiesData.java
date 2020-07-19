package net.bhl.matsim.uam.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that stores utilities for trips and access legs.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class UAMUtilitiesData {
	public static Set<UAMUtilitiesAccessEgress> accessEgressOptions = new HashSet<UAMUtilitiesAccessEgress>();
	public static Set<UAMUtilitiesTrip> tripOptions = new HashSet<UAMUtilitiesTrip>();
}
