package net.bhl.matsim.uam.modechoice.estimation.pt;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilities;

import ch.ethz.matsim.baseline_scenario.transit.routing.EnrichedTransitRoute;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;

public class CustomPublicTransportPredictor {
	final private TripRouter router;
	final private Network network;
	private Scenario scenario;
	
	public CustomPublicTransportPredictor(TripRouter router, ActivityFacilities facilities, Scenario scenario) {
		this.router = router;
		this.scenario = scenario;
		this.network = scenario.getNetwork();
	}

	public CustomPublicTransportPrediction predict(ModeChoiceTrip trip) {
		Trip tripInformation = trip.getTripInformation();		

		LinkWrapperFacility newOriginFacility = new LinkWrapperFacility(
				network.getLinks().get(tripInformation.getOriginActivity().getLinkId()));
		LinkWrapperFacility newDestinationFacility = new LinkWrapperFacility(
				network.getLinks().get(tripInformation.getDestinationActivity().getLinkId()));

		List<Leg> legs = router
				.calcRoute("pt", newOriginFacility, newDestinationFacility,
						tripInformation.getOriginActivity().getEndTime(), trip.getPerson())
				.stream().filter(Leg.class::isInstance).map(Leg.class::cast).collect(Collectors.toList());

		double crowflyDistance = CoordUtils.calcEuclideanDistance(tripInformation.getOriginActivity().getCoord(),
				tripInformation.getDestinationActivity().getCoord());

		boolean isOnlyWalk = true;
		int numberOfTransfers = -1;

		double transferTime = 0.0;

		double inVehicleTime = 0.0;
		double inVehicleDistance = 0.0;

		double accessEgressTime = 0.0;

		for (Leg leg : legs) {
			if (leg.getMode().equals("pt")) {
				if(scenario.getConfig().transit().isUseTransit()) {
					numberOfTransfers++;

					EnrichedTransitRoute route = (EnrichedTransitRoute) leg.getRoute();

					inVehicleTime += route.getInVehicleTime();
					inVehicleDistance += route.getDistance();

					if (isOnlyWalk) {
						// This is the first PT leg. Only score 60s of waiting time here!
						transferTime += Math.min(route.getWaitingTime(), 60.0);
					} else {
						transferTime += route.getWaitingTime();
					}

					isOnlyWalk = false;
				} else { // teleported pt
					double distanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute")).getBeelineDistanceFactors().get(TransportMode.pt);
					double distance = CoordUtils.calcEuclideanDistance((tripInformation.getOriginActivity().getCoord()), tripInformation.getDestinationActivity().getCoord());
					inVehicleDistance += distance * distanceFactor;
				
					double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute")).getTeleportedModeSpeeds().get(TransportMode.pt);
				
					inVehicleTime += inVehicleDistance / speed;	
					isOnlyWalk = false;
				}

			} else if (leg.getMode().contains("walk")) {
				accessEgressTime += leg.getTravelTime();
			} else {
				throw new IllegalStateException("Can only enrich pt and *_walk legs");
			}
		}

		numberOfTransfers = Math.max(0, numberOfTransfers);

		return new CustomPublicTransportPrediction(inVehicleTime, inVehicleDistance, accessEgressTime, transferTime,
				numberOfTransfers, isOnlyWalk, crowflyDistance, legs);
	}
}
