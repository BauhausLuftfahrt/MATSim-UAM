package net.bhl.matsim.uam.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import net.bhl.matsim.uam.infrastructure.UAMStation;

public class UAMUtilitiesTrip {
	public Id<Person> person;
	public Id<Link> originLink;
	public Id<UAMStation> originStation;
	public Id<UAMStation> destinationStation;
	public Id<Link> destinationLink;
	public String accessMode;
	public String egressMode;
	public double time;
	public double accessUtility;
	public double uamWaitUtility;
	public double uamFlightUtility;
	public double uamIncomeUtility;
	public double egressUtility;
	public double totalUtility;

	public String getHeader() {
		return "person,time,originLink,originStation,destinationStation,destinationLink,accessMode,egressMode,"
				+ "accessUtility,uamWaitUtility,uamFlightUtility,uamIncomeUtility,egressUtility,totalUtility";
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof UAMUtilitiesTrip)) {
			return false;
		}
		UAMUtilitiesTrip other = (UAMUtilitiesTrip) o;
		return this.toString().equals(other.toString());
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public String toString() {
		return "" + person + "," + time + "," + originLink + "," + originStation + "," + destinationStation + ","
				+ destinationLink + "," + accessMode + "," + egressMode + "," + accessUtility + "," + uamWaitUtility
				+ "," + uamFlightUtility + "," + uamIncomeUtility + "," + egressUtility + "," + totalUtility;
	}
}
