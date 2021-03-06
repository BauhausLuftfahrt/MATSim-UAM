package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.qsim.UAMDepartureHandler;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;

import com.google.inject.Inject;

public class UAMPrebookVehicle implements PersonDepartureEventHandler {
	@Inject
	private UAMDepartureHandler departureHandler;

	@Override
	public void reset(int iteration) {
		// After iteration "self-destruct" otherwise old event handlers will still exist in new iterations next to
		// that iterations, newly created event handler
		this.departureHandler = null;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		// Required only for network modes (i.e. access_uam_car) since the default VehicluarDepartureHandler handles
		// such departures, resulting in UAMDepartureHandler not being able to register such bookings
		if (departureHandler != null)
			departureHandler.manualUAMPrebooking(event.getLegMode(), event.getTime(), event.getPersonId(),
					event.getLinkId());
	}
}
