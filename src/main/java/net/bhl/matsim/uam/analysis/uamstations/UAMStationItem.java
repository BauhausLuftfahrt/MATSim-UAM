package net.bhl.matsim.uam.analysis.uamstations;

import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;

/**
 * This class stores information about a UAM Station.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class UAMStationItem {
	public String name;
	public Id<UAMStation> id;
	public int preflighttime;
	public int postflighttime;
	public int defaultwaittime;
	public String link;

	public UAMStationItem(String name, Id<UAMStation> id, int preflighttime, int postflighttime,
						  int defaultwaittime, String link) {
		this.name = name;
		this.id = id;
		this.preflighttime = preflighttime;
		this.postflighttime = postflighttime;
		this.defaultwaittime = defaultwaittime;
		this.link = link;
	}
}
