package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.qsim.UAMDepartureHandler;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;

public class UAMPrebookVehicle implements PersonDepartureEventHandler {
	private Scenario scenario;
	private UAMDepartureHandler departureHandler;

	public UAMPrebookVehicle(Scenario scenario, UAMDepartureHandler departureHandler) {
		this.scenario = scenario;
		this.departureHandler = departureHandler;
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		// Required only for network modes (i.e. access_uam_car) since the default VehicluarDepartureHandler handles
		// such departures, resulting in UAMDepartureHandler not being able to register such bookings
		departureHandler.manualUAMPrebooking(event.getLegMode(), event.getTime(), event.getPersonId(),
				event.getLinkId());
	}
}
