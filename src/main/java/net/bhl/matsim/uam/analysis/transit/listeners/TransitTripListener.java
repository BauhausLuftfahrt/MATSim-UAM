package net.bhl.matsim.uam.analysis.transit.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.GenericEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonStuckEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.api.experimental.events.handler.TeleportationArrivalEventHandler;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.matsim.baseline_scenario.transit.events.PublicTransitEvent;
import net.bhl.matsim.uam.analysis.transit.TransitTripItem;

/**
 * A listener that retrieves information from transit events.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class TransitTripListener
		implements ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler,
		PersonArrivalEventHandler, GenericEventHandler, TeleportationArrivalEventHandler, PersonStuckEventHandler {
	final private StageActivityTypes stageActivityTypes;
	final private Network network;

	final private List<TransitTripItem> trips = new LinkedList<>();
	final private Map<Id<Person>, Integer> tripIndex = new HashMap<>();
	final private Map<Id<Person>, TransitTripListenerItem> ongoing = new HashMap<>();

	public TransitTripListener(StageActivityTypes stageActivityTypes, Network network) {
		this.stageActivityTypes = stageActivityTypes;
		this.network = network;
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (!stageActivityTypes.isStageActivity(event.getActType())) {
			Integer tripId = tripIndex.get(event.getPersonId());

			if (tripId == null) {
				tripId = 0;
			} else {
				tripId += 1;
			}

			TransitTripListenerItem item = new TransitTripListenerItem();
			item.personId = event.getPersonId();
			item.personTripId = tripId;
			item.origin = network.getLinks().get(event.getLinkId()).getCoord();
			item.startTime = event.getTime();

			ongoing.put(event.getPersonId(), item);
			tripIndex.put(event.getPersonId(), tripId);
		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		TransitTripListenerItem item = ongoing.get(event.getPersonId());

		if (item != null) {
			item.mode = event.getLegMode();
			item.legDepartureTime = event.getTime();
			item.intermediateOrigin = network.getLinks().get(event.getLinkId()).getCoord();

			if (item.mode.equals("pt")) {
				item.numberOfTransfers += 1;
			}
		}
	}

	@Override
	public void handleEvent(TeleportationArrivalEvent event) {
		TransitTripListenerItem item = ongoing.get(event.getPersonId());

		if (item != null) {
			if (item.mode.equals("pt")) {
				item.inVehicleDistance += event.getDistance();
			} else if (item.mode.contains("walk") && !item.mode.equals("walk")) {
				item.transferDistance += event.getDistance();
			}
		}
	}

	private void handleEvent(PublicTransitEvent event) {
		TransitTripListenerItem item = ongoing.get(event.getPersonId());

		if (item != null) {
			double inVehicleTime = event.getVehicleDepartureTime() - item.legDepartureTime;
			double waitingTime = event.getTime() - item.legDepartureTime - inVehicleTime;

			item.inVehicleTime += inVehicleTime;
			item.waitingTime += waitingTime;
			item.routing += "Line: " + event.getTransitLineId().toString() + " Route: "
					+ event.getTransitRouteId().toString() + " Distance: " + event.getTravelDistance() + " -- ";

			if (item.waitingForFirstTransitEvent) {
				item.waitingForFirstTransitEvent = false;
				item.firstWaitingTime = waitingTime;
			}
		}
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {
		TransitTripListenerItem item = ongoing.get(event.getPersonId());
		Coord intermediateDestination = network.getLinks().get(event.getLinkId()).getCoord();

		if (item != null) {
			if (item.mode.equals("pt")) {
				item.inVehicleCrowflyDistance += CoordUtils.calcEuclideanDistance(item.intermediateOrigin,
						intermediateDestination);
			} else if (item.mode.contains("walk") && !item.mode.equals("walk")) {
				item.transferCrowflyDistance += CoordUtils.calcEuclideanDistance(item.intermediateOrigin,
						intermediateDestination);
				item.transferTime += event.getTime() - item.legDepartureTime;
			}
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (!stageActivityTypes.isStageActivity(event.getActType())) {
			TransitTripListenerItem item = ongoing.remove(event.getPersonId());

			if (item != null) {
				if (item.mode.equals("pt") || (item.mode.contains("walk") && !item.mode.equals("walk"))) {
					item.destination = network.getLinks().get(event.getLinkId()).getCoord();
					item.numberOfTransfers = Math.max(item.numberOfTransfers, 0);
					item.crowflyDistance = CoordUtils.calcEuclideanDistance(item.origin, item.destination);
					trips.add(item);
				}
			}
		}
	}

	@Override
	public void handleEvent(GenericEvent event) {
		if (event instanceof PublicTransitEvent) {
			handleEvent((PublicTransitEvent) event);
		}
	}

	public Collection<TransitTripItem> getTransitTripItems() {
		return trips;
	}

	@Override
	public void handleEvent(PersonStuckEvent event) {
		TransitTripListenerItem item = ongoing.remove(event.getPersonId());

		if (item != null) {
			item.routing += "STUCK AND ABORTED";
			item.destination = network.getLinks().get(event.getLinkId()).getCoord();
			item.numberOfTransfers = Math.max(item.numberOfTransfers, 0);
			item.crowflyDistance = CoordUtils.calcEuclideanDistance(item.origin, item.destination);
			trips.add(item);
		}
	}
}
