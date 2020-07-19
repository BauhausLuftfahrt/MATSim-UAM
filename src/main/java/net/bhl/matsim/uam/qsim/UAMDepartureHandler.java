package net.bhl.matsim.uam.qsim;

import com.google.inject.Inject;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
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
	@DvrpMode(UAMModes.UAM_MODE)
	private PassengerEngine passengerEngine;

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		// we request uam when the agent starts its access leg to the nearest station
		if (agent instanceof PlanAgent) {
			if (agent.getMode().startsWith("access_uam")) {
				Plan plan = ((PlanAgent) agent).getCurrentPlan();
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				final Leg accessLeg = (Leg) plan.getPlanElements().get(planElementsIndex);
				final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex + 2);
				Activity uav_interaction1 = (Activity) plan.getPlanElements().get(planElementsIndex + 1);
				passengerEngine.prebookTrip(now, (MobsimPassengerAgent) agent, leg.getRoute().getStartLinkId(),
						leg.getRoute().getEndLinkId(), now + uav_interaction1.getMaximumDuration()
								+ (accessLeg.getTravelTime() <= 0 ? 1 : accessLeg.getTravelTime()));
				// uav_interaction1.getMaximumDuration()
			} else if (agent.getMode().equals("transit_walk") || agent.getMode().equals("access_walk")) {
				Plan plan = ((PlanAgent) agent).getCurrentPlan();
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				if (isuamtrip(plan, planElementsIndex)) {
					if (!bookedTrips.contains(agent.getId())) {
						double travelTime = getTravelTime(plan, planElementsIndex);
						final Leg uamLeg = getUamLeg(plan, planElementsIndex);
						Activity uav_interaction1 = (Activity) plan.getPlanElements().get(planElementsIndex + 1);
						passengerEngine.prebookTrip(now, (MobsimPassengerAgent) agent,
								uamLeg.getRoute().getStartLinkId(), uamLeg.getRoute().getEndLinkId(),
								now + uav_interaction1.getMaximumDuration() + (travelTime <= 0 ? 1 : travelTime));
						bookedTrips.add(agent.getId());
					}
				}

			} else if (agent.getMode().equals("uam"))
				bookedTrips.remove(agent.getId());
		}

		return false;
	}

	private Leg getUamLeg(Plan plan, Integer planElementsIndex) {

		while (true) {
			PlanElement pe = plan.getPlanElements().get(planElementsIndex);

			if (pe instanceof Leg) {
				if (((Leg) pe).getMode().equals("uam"))
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
				if (((Leg) pe).getMode().equals("uam"))
					found = true;
				else
					travelTime += ((Leg) pe).getTravelTime();
			}
			index++;

		}

		return travelTime;
	}

	private boolean isuamtrip(Plan plan, Integer planElementsIndex) {

		int index = planElementsIndex + 1;
		while (true) {
			PlanElement pe = plan.getPlanElements().get(index);
			if (pe instanceof Activity) {
				if (((Activity) pe).getType().equals("uam_interaction"))
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
