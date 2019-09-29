package net.bhl.matsim.uam.qsim;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.NoUAMLandingSpaceEvent;
import net.bhl.matsim.uam.events.NoUAMVehicleEvent;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.utils.geometry.CoordUtils;

/**
 * This class defines the departure handler for UAM simulation. This is an
 * obsolete version, not being used.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMDepartureHandlerOld implements DepartureHandler {

	private final UAMManager uamManager;
	private Scenario scenario;
	private EventsManager eventsManager;

	public UAMDepartureHandlerOld(UAMManager uamManager, Scenario scenario, EventsManager eventsManager) {
		this.uamManager = uamManager;
		this.scenario = scenario;
		this.eventsManager = eventsManager;
	}

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {

		Network network = scenario.getNetwork();
		Link link = network.getLinks().get(linkId);
		Coord coord = link.getCoord();

		Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
		final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
		final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex);
		// if the current trip is a uam trip, adapt the travel time, destination/origin
		String mode = agent.getMode();
		switch (mode) {
			case "access_uam":
				UAMVehicle vehicle = this.uamManager.getClosestAvailableVehicle(coord);
				Coord destC = network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord();

				UAMStation destStation = this.uamManager.getClosestStationWithLandingSpace(destC);

				if (vehicle == null || destStation == null) {

					if (vehicle == null)
						this.eventsManager.processEvent(new NoUAMVehicleEvent(now, link, plan.getPerson().getId()));
					else {
						Link dLink = network.getLinks().get(leg.getRoute().getEndLinkId());
						this.eventsManager.processEvent(new NoUAMLandingSpaceEvent(now, dLink, plan.getPerson().getId()));
					}

					plan.getPlanElements().remove(planElementsIndex + 1);
					plan.getPlanElements().remove(planElementsIndex + 1);
					Coord destinationCoord = network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord();
					double distance = CoordUtils.calcEuclideanDistance(coord, destinationCoord);
					double walkSpeed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
							.getTeleportedModeSpeeds().get("walk");
					double beelineDistanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
							.get("planscalcroute")).getBeelineDistanceFactors().get("walk");
					double travelTime = distance * beelineDistanceFactor / walkSpeed;

					leg.setTravelTime(travelTime);

					break;
				}
				UAMStation ls = this.uamManager.getLSWHereVehicleIs(vehicle);

				if (destStation.equals(ls)) {
					// the agent wants to take off and land at the same station
					// just make the agent walk
					plan.getPlanElements().remove(planElementsIndex + 1);
					plan.getPlanElements().remove(planElementsIndex + 1);
					Coord destinationCoord = network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord();
					double distance = CoordUtils.calcEuclideanDistance(coord, destinationCoord);
					double walkSpeed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
							.getTeleportedModeSpeeds().get("walk");
					double beelineDistanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
							.get("planscalcroute")).getBeelineDistanceFactors().get("walk");
					double travelTime = distance * beelineDistanceFactor / walkSpeed;

					leg.setTravelTime(travelTime);

					break;

				}
				this.uamManager.reserveLandingSpot(plan.getPerson().getId(), destStation);
				this.uamManager.reserveVehicle(agent.getId(), vehicle);
				leg.getRoute().setEndLinkId(ls.getLocationLink().getId());

				double distance = CoordUtils.calcEuclideanDistance(coord, ls.getLocationLink().getCoord());
				double walkSpeed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
						.getTeleportedModeSpeeds().get("walk");
				double beelineDistanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
						.get("planscalcroute")).getBeelineDistanceFactors().get("walk");
				double travelTime = distance * beelineDistanceFactor / walkSpeed != 0.0
						? distance * beelineDistanceFactor / walkSpeed
						: 1.0;

				leg.setTravelTime(travelTime);

				break;
			case "uam":

				// Coord destCoord =
				// network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord();
				UAMStation destLS = this.uamManager.getReservedStation(plan.getPerson().getId());
				if (destLS == null) {

					throw new RuntimeException("The system is an incosistent state! \n "
							+ "Trying to park a vehicle but there is not reserved ladning space!");

				}
				leg.getRoute().setEndLinkId(destLS.getLocationLink().getId());

				// Retrieve UAM Vehicle
				UAMVehicle vehicle2 = uamManager.getReservedVehicle(agent.getId());

				// Retrieve UAM route and distance
				// UAMStation origin = uamManager.getLSWHereVehicleIs(vehicle2);
				// UAMRoute route = uamManager.getRoutes().getRoute(origin , destLS);

				double distance2 = CoordUtils.calcEuclideanDistance(coord, destLS.getLocationLink().getCoord());

				// Time calculation
				double flightTime = distance2 / vehicle2.getCruiseSpeed();

				double vtolTime = 500.0 / vehicle2.getVerticalSpeed();
				// double vtolTime = route.getHeight() / vehicle2.getVerticalSpeed();
				double totalFlightTime = vtolTime + flightTime + vtolTime; // Take-off + Flight + Landing

				// Set distance and time for current leg
				leg.getRoute().setDistance(distance2);
				leg.setTravelTime(totalFlightTime);

				break;
			case "egress_uam":
				Link destLink = network.getLinks().get(leg.getRoute().getEndLinkId());
				leg.getRoute().setStartLinkId(linkId);

				double distanceEgress = CoordUtils.calcEuclideanDistance(coord, destLink.getCoord());
				double walkSpeedEgress = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
						.get("planscalcroute")).getTeleportedModeSpeeds().get("walk");
				double beelineDistanceFactorEgress = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
						.get("planscalcroute")).getBeelineDistanceFactors().get("walk");
				double travelTimeEgress = distanceEgress * beelineDistanceFactorEgress / walkSpeedEgress != 0.0
						? distanceEgress * beelineDistanceFactorEgress / walkSpeedEgress
						: 1.0;
				leg.setTravelTime(travelTimeEgress);

				break;
			default:
				break;
		}

		// we did not handle the departure, we just addapt the plan, we will let the
		// teleportationEngine to handle the departure
		return false;
	}

}
