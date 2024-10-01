package net.bhl.matsim.uam.schedule;

import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.vrpagent.UAMActionCreator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.HasPersonId;
import org.matsim.api.core.v01.population.Person;

import java.util.Map;

/**
 * Event that is generated when the drop off occurs, in order to know which
 * person was dropped off.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMTransitEvent extends Event implements HasPersonId {
	final private UAMRequest request;

	public UAMTransitEvent(UAMRequest request, double time) {
		super(time);
		this.request = request;
	}

	public UAMRequest getRequest() {
		return request;
	}

	@Override
	public String getEventType() {
		return UAMActionCreator.TRANSIT_ACTIVITY_TYPE;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put("person", request.getPassengerIds().toString());
		return attr;
	}

	@Override
	public Id<Person> getPersonId() {
		//TODO: currently only returning one person, should be corrected int eh future milos '24'
		return request.getPassengerIds().get(0);
	}
}
