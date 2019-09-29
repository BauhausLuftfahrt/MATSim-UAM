package net.bhl.matsim.uam.modechoice.estimation.car;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.facilities.ActivityFacilities;

import java.util.List;

public class CustomCarPredictor {
	final private TripRouter router;
	final private TravelDisutility travelDisutility;
	final private Network network;

	public CustomCarPredictor(TripRouter router, ActivityFacilities facilities, TravelDisutility travelDisutility,
							  Network network) {
		this.router = router;
		this.travelDisutility = travelDisutility;
		this.network = network;
	}

	public CustomCarPrediction predict(ModeChoiceTrip trip) {
		Trip tripInformation = trip.getTripInformation();

		LinkWrapperFacility originFacility =
				new LinkWrapperFacility(this.network.getLinks().get(tripInformation.getOriginActivity().getLinkId()));
		LinkWrapperFacility destinationFacility =
				new LinkWrapperFacility(this.network.getLinks().get(tripInformation.getDestinationActivity().getLinkId()));

		List<? extends PlanElement> result = router.calcRoute("car", originFacility, destinationFacility,
				tripInformation.getOriginActivity().getEndTime(), trip.getPerson());

		NetworkRoute route = (NetworkRoute) ((Leg) result.get(0)).getRoute();

		// We use getTravelCost here, because we adjust the given travel times from
		// TravelTime via CustomCarDisutility. The provided costs are then the adjusted
		// travel times.
		double travelTime = route.getTravelCost();

		// Also, while the NetworkRoutingModule does not take the arrival link into
		// account, it is simulated in the Netsim. Hence, to arrive at the correct
		// estimate, we need to add the travel time for the arrival link.
		//double arrivalAtLinkTime = trip.getTripInformation().getOriginActivity().getEndTime() + travelTime;
		//Link arrivalLink = network.getLinks().get(route.getEndLinkId());

		//travelTime += travelDisutility.getLinkTravelDisutility(arrivalLink, arrivalAtLinkTime, trip.getPerson(), null);

		return new CustomCarPrediction(route.getDistance(), travelTime, route);
	}
}
