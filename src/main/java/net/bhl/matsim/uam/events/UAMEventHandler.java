package net.bhl.matsim.uam.events;

import com.google.inject.Inject;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

/**
 * Class that handles arrival and departure events.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMEventHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler {

	@Inject
	private UAMManager manager;
	@Inject
	private Scenario scenario;

	@Inject
	public UAMEventHandler(UAMManager manager, Scenario scenario) {
		this.manager = manager;
		this.scenario = scenario;
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		Network network = scenario.getNetwork();
		// if the agent just arrived with uam, place the vehicle on the
		// nearest station oto the arrival link, this should be the link where the
		// station is located if there are multiple stations at the same link this will cause problems
		// balac feb '18
		if (event.getLegMode().equals("uam")) {
			Link link = network.getLinks().get(event.getLinkId());
			UAMVehicle vehicle = this.manager.getReservedVehicle(event.getPersonId());
			UAMStation station = this.manager.getStations().getNearestUAMStation(link);
			this.manager.addVehicle(vehicle, station);
		}

	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		Network network = scenario.getNetwork();
		// if the agent is departing with the uam, we remove the vehicle from the
		// station, if there are multiple stations at the same link this will cause problems balac feb '18
		if (event.getLegMode().equals("uam")) {
			Link link = network.getLinks().get(event.getLinkId());
			UAMVehicle vehicle = this.manager.getReservedVehicle(event.getPersonId());
			UAMStation station = this.manager.getStations().getNearestUAMStation(link);
			this.manager.removeVehicle(vehicle, station);
		}

	}

}
