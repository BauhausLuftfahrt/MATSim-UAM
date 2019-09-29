package net.bhl.matsim.uam.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.Map;

/**
 * An event for the case of no UAM Stations available for landing.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class NoUAMLandingSpaceEvent extends Event {

	public static final String EVENT_TYPE = "nouamstationcapacity";
	private Link destinationLink;
	private Id<Person> personId;

	public NoUAMLandingSpaceEvent(double time, Link destinationLink, Id<Person> personId) {
		super(time);
		this.destinationLink = destinationLink;
		this.personId = personId;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> atts = super.getAttributes();
		atts.put("personid", this.personId.toString());
		atts.put("destinationlink", this.destinationLink.getId().toString());
		return atts;
	}

	@Override
	public String toString() {
		Map<String, String> attr = this.getAttributes();
		StringBuilder eventXML = new StringBuilder("\t<event ");
		for (Map.Entry<String, String> entry : attr.entrySet()) {
			eventXML.append(entry.getKey());
			eventXML.append("=\"");
			eventXML.append(entry.getValue());
			eventXML.append("\" ");
		}
		eventXML.append(" />");
		return eventXML.toString();
	}

}
