package net.bhl.matsim.uam.modechoice.tracking;

import ch.ethz.matsim.baseline_scenario.traffic.BaselineLinkSpeedCalculator;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bhl.matsim.uam.modechoice.CustomCarDisutility;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class FreeflowTravelTimeValidator implements LinkEnterEventHandler, LinkLeaveEventHandler,
		VehicleLeavesTrafficEventHandler, VehicleEntersTrafficEventHandler {
	final private static Logger logger = Logger.getLogger(FreeflowTravelTimeValidator.class);

	final private Map<Id<Vehicle>, LinkEnterEvent> enterEvents = new HashMap<>();
	final private Network network;
	final private TravelDisutility travelDisutility;

	@Inject
	public FreeflowTravelTimeValidator(Network network, BaselineLinkSpeedCalculator linkSpeedCalculator) {
		this.network = network;
		this.travelDisutility = new CustomCarDisutility.Factory(linkSpeedCalculator)
				.createTravelDisutility(new FreeSpeedTravelTime());
	}

	@Override
	public void handleEvent(LinkLeaveEvent leaveEvent) {
		LinkEnterEvent enterEvent = enterEvents.remove(leaveEvent.getVehicleId());

		if (enterEvent != null && enterEvent.getLinkId().equals(leaveEvent.getLinkId())) {
			Link link = network.getLinks().get(enterEvent.getLinkId());

			double observedLinkTravelTime = leaveEvent.getTime() - enterEvent.getTime();
			double expectedLinkTravelTime = travelDisutility.getLinkTravelDisutility(link, enterEvent.getTime(), null,
					null);

			if (Math.abs(observedLinkTravelTime - expectedLinkTravelTime) > 1e-3) {
				logger.warn(
						String.format("Expected travel time %f on link %s at %s, but got %f", expectedLinkTravelTime,
								link.getId().toString(), Time.writeTime(enterEvent.getTime()), observedLinkTravelTime));
			}
		}
	}

	@Override
	public void handleEvent(LinkEnterEvent enterEvent) {
		enterEvents.put(enterEvent.getVehicleId(), enterEvent);
	}

	@Override
	public void reset(int iteration) {
		enterEvents.clear();
	}

	@Override
	public void handleEvent(VehicleEntersTrafficEvent event) {
		enterEvents.remove(event.getVehicleId());
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		enterEvents.remove(event.getVehicleId());
	}
}
