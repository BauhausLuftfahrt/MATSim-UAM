package net.bhl.matsim.uam.analysis.uamdemand.listeners;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import net.bhl.matsim.uam.analysis.uamdemand.UAMDemandItem;

public class UAMListenerItem extends UAMDemandItem{
		public UAMListenerItem(Id<Person> personId, Coord origin, double startTime, String accessMode) {
		super(personId, origin, null, null, null, null, null, startTime, Double.NaN, Double.NaN,  Double.NaN,  Double.NaN,  Double.NaN,
				"unknown", accessMode, "unknown", false);
	}
}
