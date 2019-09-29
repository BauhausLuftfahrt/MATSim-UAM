package net.bhl.matsim.uam.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.Map;

/**
 * An event for the case of no UAM Vehicle available.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class NoUAMVehicleEvent extends Event {
	public static final String EVENT_TYPE = "nouamvehicle";

	private Link originLink;
	private Id<Person> personId;

	public NoUAMVehicleEvent(double time, Link originLink, Id<Person> personId) {
		super(time);
		this.originLink = originLink;
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
		atts.put("originlink", this.originLink.getId().toString());
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
