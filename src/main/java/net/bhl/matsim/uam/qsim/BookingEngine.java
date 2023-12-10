package net.bhl.matsim.uam.qsim;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.dvrp.passenger.PassengerEngineWithPrebooking;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.pt.TransitDriverAgentImpl;

import net.bhl.matsim.uam.run.UAMConstants;

public class BookingEngine implements MobsimEngine, PersonDepartureEventHandler, MobsimBeforeSimStepListener {
	private Set<Id<Person>> bookedTrips = new HashSet<>();

	private final List<MobsimAgent> bookingAgents = new LinkedList<>();
	private final List<MobsimAgent> processBookingAgents = new LinkedList<>();
	private InternalInterface internalInterface;
	private Set<String> uamModes = new HashSet<>();
	private PassengerEngineWithPrebooking passengerEngine;
	private EventsManager eventsManager;
	private Network network;

	public BookingEngine(Scenario scenario, PassengerEngineWithPrebooking passengerEngine, EventsManager eventsManager,
			Network network) {
		this.passengerEngine = passengerEngine;
		this.eventsManager = eventsManager;
		this.network = network;
	}

	@Override
	public void doSimStep(double time) {
		for (MobsimAgent ma : this.processBookingAgents) {
			manualUAMPrebooking(time, ma);
		}

	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		MobsimAgent agent = internalInterface.getMobsim().getAgents().get(event.getPersonId());
		if (agent instanceof TransitDriverAgentImpl || agent instanceof DynAgent)
			return;
		if (uamModes.contains(event.getLegMode())) {
			bookingAgents.add(agent);
		}

		else {
			Plan plan = ((PlanAgent) agent).getCurrentPlan();

			final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
			if (isUamTrip(plan, planElementsIndex)) {
				if (!bookedTrips.contains(agent.getId())) {
					bookingAgents.add(agent);
				}
			}
		}

		if (event.getLegMode().equals("uam"))
			this.bookedTrips.remove(agent.getId());

	}

	public void manualUAMPrebooking(double now, MobsimAgent agent) {

		if (!bookedTrips.contains(agent.getId())) {
			Plan plan = ((PlanAgent) agent).getCurrentPlan();
			int planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);

			double travelTime = getTravelTime(plan, planElementsIndex);
			final Leg uamLeg = getUamLeg(plan, planElementsIndex);

			this.bookedTrips.add(plan.getPerson().getId());

			performPrebooking(uamLeg, agent, now + (travelTime <= 0 ? 1 : travelTime), now);
		}

	}

	private void performPrebooking(Leg uamLeg, MobsimAgent agent, double departureTime, double submissionTime) {
		Route uamRoute = uamLeg.getRoute();

		Link startLink = network.getLinks().get(uamRoute.getStartLinkId());
		Link endLink = network.getLinks().get(uamRoute.getEndLinkId());

		UAMTripInfo tripInfo = new UAMTripInfo(startLink, endLink, uamRoute, departureTime);
		passengerEngine.bookTrip((MobsimPassengerAgent) agent, tripInfo);
	}

	@Override
	public void onPrepareSim() {
		uamModes = new HashSet<>();

		uamModes.add(UAMConstants.access + TransportMode.car);
		uamModes.add(UAMConstants.access + TransportMode.walk);
		eventsManager.addHandler(this);
	}

	@Override
	public void afterSim() {
		eventsManager.removeHandler(this);

	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface;

	}

	@Override
	public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {

		processBookingAgents.clear();

		processBookingAgents.addAll(bookingAgents);

		bookingAgents.clear();
	}

	private Leg getUamLeg(Plan plan, Integer planElementsIndex) {
		while (true) {
			PlanElement pe = plan.getPlanElements().get(planElementsIndex);
			if (pe instanceof Leg) {
				if (((Leg) pe).getMode().equals(UAMConstants.uam))
					return (Leg) pe;
			}
			planElementsIndex++;
		}
	}

	private double getTravelTime(Plan plan, Integer planElementsIndex) {
		boolean found = false;
		int index = planElementsIndex;
		double travelTime = 0.0;
		while (!found) {
			PlanElement pe = plan.getPlanElements().get(index);
			if (pe instanceof Leg) {
				if (((Leg) pe).getMode().equals(UAMConstants.uam))
					found = true;
				else
					// It seems the leg itself does no longer store travel times, but only its route
					// travelTime += ((Leg) pe).getTravelTime().seconds();
					travelTime += ((Leg) pe).getRoute().getTravelTime().seconds();
			}
			else {
				travelTime += ((Activity)pe).getMaximumDuration().seconds();
			}
			index++;
		}
		return travelTime;
	}

	private boolean isUamTrip(Plan plan, Integer planElementsIndex) {
		int index = planElementsIndex;
		while (true) {
			PlanElement pe = plan.getPlanElements().get(index);
			if (pe instanceof Activity) {
				if (((Activity) pe).getType().equals(UAMConstants.interaction))
					return true;
				else if (((Activity) pe).getType().equals("pt interaction")) {
					index++;

					continue;
				} else
					return false;
			}
			index++;
		}

	}

}
