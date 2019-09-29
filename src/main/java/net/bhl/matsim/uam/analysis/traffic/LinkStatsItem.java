package net.bhl.matsim.uam.analysis.traffic;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.Map;

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
