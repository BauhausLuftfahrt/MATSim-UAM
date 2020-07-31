package net.bhl.matsim.uam.qsim;

import com.google.inject.Inject;
import net.bhl.matsim.uam.events.UAMPrebookVehicle;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * This class defines the departure handler for UAM simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMDepartureHandler implements DepartureHandler {

	Set<Id<Person>> bookedTrips = new HashSet<>();
	@Inject
	@DvrpMode(UAMModes.uam)
	private PassengerEngine passengerEngine;

	@Inject
	private QSim qsim;

	private Set<String> modesRequiringManualUAMPrebooking;

	public void initiateUAMDepartureHandler() {
		if (modesRequiringManualUAMPrebooking == null) {
			qsim.getEventsManager().addHandler(new UAMPrebookVehicle(this));

			modesRequiringManualUAMPrebooking = new HashSet<>();
			String mainMode = this.qsim.getScenario().getConfig().getModules().get("qsim").getParams().get("mainMode");
			if (mainMode.contains(UAMModes.access + TransportMode.car))
				modesRequiringManualUAMPrebooking.add(UAMModes.access + TransportMode.car);
			if (mainMode.contains(UAMModes.egress + TransportMode.car))
				modesRequiringManualUAMPrebooking.add(UAMModes.egress + TransportMode.car);
		}
		// else: already initiated
	}

	public void manualUAMPrebooking(String mode, double now, Id<Person> id, Id<Link> linkId) {
		if (modesRequiringManualUAMPrebooking.contains(mode))
			handleDeparture(now, qsim.getAgents().get(id), linkId);
	}

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		// Must be initiated before first use (cannot be done in constructor as this itself is passes as a variable)
		initiateUAMDepartureHandler();
		// TODO is there a way to initiate the UAMDepartureHandler at the beginning of an iteration?

		if (agent instanceof PlanAgent) {
			if (agent.getMode().startsWith(UAMModes.access)) {
				Plan plan = ((PlanAgent) agent).getCurrentPlan();
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				final Leg accessLeg = (Leg) plan.getPlanElements().get(planElementsIndex);
				final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex + 2);
				Activity uam_interaction = (Activity) plan.getPlanElements().get(planElementsIndex + 1);
				passengerEngine.prebookTrip(now, (MobsimPassengerAgent) agent, leg.getRoute().getStartLinkId(),
						leg.getRoute().getEndLinkId(), now + uam_interaction.getMaximumDuration()
								+ (accessLeg.getTravelTime() <= 0 ? 1 : accessLeg.getTravelTime()));

			} else if (agent.getMode().equals(TransportMode.transit_walk)
					|| agent.getMode().equals(TransportMode.access_walk)) {
				Plan plan = ((PlanAgent) agent).getCurrentPlan();
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				if (isUamTrip(plan, planElementsIndex)) {
					if (!bookedTrips.contains(agent.getId())) {
						double travelTime = getTravelTime(plan, planElementsIndex);
						final Leg uamLeg = getUamLeg(plan, planElementsIndex);
						Activity uam_interaction = (Activity) plan.getPlanElements().get(planElementsIndex + 1);
						passengerEngine.prebookTrip(now, (MobsimPassengerAgent) agent,
								uamLeg.getRoute().getStartLinkId(), uamLeg.getRoute().getEndLinkId(),
								now + uam_interaction.getMaximumDuration() + (travelTime <= 0 ? 1 : travelTime));
						bookedTrips.add(agent.getId());
					}
				}

			} else if (agent.getMode().equals(UAMModes.uam))
				bookedTrips.remove(agent.getId());
		}

		return false;
	}

	private Leg getUamLeg(Plan plan, Integer planElementsIndex) {
		while (true) {
			PlanElement pe = plan.getPlanElements().get(planElementsIndex);
			if (pe instanceof Leg) {
				if (((Leg) pe).getMode().equals(UAMModes.uam))
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
				if (((Leg) pe).getMode().equals(UAMModes.uam))
					found = true;
				else
					travelTime += ((Leg) pe).getTravelTime();
			}
			index++;
		}
		return travelTime;
	}

	private boolean isUamTrip(Plan plan, Integer planElementsIndex) {
		int index = planElementsIndex + 1;
		while (true) {
			PlanElement pe = plan.getPlanElements().get(index);
			if (pe instanceof Activity) {
				if (((Activity) pe).getType().equals(UAMModes.interaction))
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
