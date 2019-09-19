package net.bhl.matsim.uam.analysis.traffic;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

/**
* This class stores link information in a specific time.
* 
* @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
*/
public class LinkStatsItem {
	public Id<Link> linkId;
	public double distance;
	public double freeSpeed;
	public Map<Integer, Double> timeDependantSpeed;

	public LinkStatsItem(Id<Link> linkId, double distance, double freeSpeed, Map<Integer, Double> timeDependantSpeed) {
		this.linkId = linkId;
		this.distance = distance;
		this.freeSpeed = freeSpeed;
		this.timeDependantSpeed = timeDependantSpeed;
	}
}
