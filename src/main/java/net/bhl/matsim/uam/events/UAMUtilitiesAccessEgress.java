package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

/**
 * A class that stores information about an access or egress leg to a UAM
 * station.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class UAMUtilitiesAccessEgress {
	private Id<Person> person;
	private Id<UAMStation> station;
	private Id<Link> link;
	private boolean access;
	private double utility;
	private String mode;
	private double time;

	public UAMUtilitiesAccessEgress(Id<Person> person, Id<UAMStation> station, Id<Link> link, boolean access,
									double utility, String mode, double time) {
		this.person = person;
		this.station = station;
		this.link = link;
		this.access = access;
		this.utility = utility;
		this.mode = mode;
		this.time = time;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof UAMUtilitiesAccessEgress)) {
			return false;
		}
		UAMUtilitiesAccessEgress other = (UAMUtilitiesAccessEgress) o;
		return this.toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	public String getHeader() {
		return "person,station,link,access,utility,mode,time";
	}

	public String toString() {
		String access = this.access ? "access" : "egress";
		return "" + person + "," + station + "," + link + "," + access + "," + utility + "," + mode + "," + time;
	}
}