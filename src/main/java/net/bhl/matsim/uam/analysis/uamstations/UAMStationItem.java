package net.bhl.matsim.uam.analysis.uamstations;

import org.matsim.api.core.v01.Id;

import net.bhl.matsim.uam.infrastructure.UAMStation;

public class UAMStationItem {
	public String name;
	public Id<UAMStation> id;
	public int landingcap;
	public int preflighttime;
	public int postflighttime;
	public int defaultwaittime;
	public String link;

	public UAMStationItem(String name, Id<UAMStation> id, int landingcap, int preflighttime, int postflighttime,
			int defaultwaittime, String link) {
		this.name = name;
		this.id = id;
		this.landingcap = landingcap;
		this.preflighttime = preflighttime;
		this.postflighttime = postflighttime;
		this.defaultwaittime = defaultwaittime;
		this.link = link;
	}
}
