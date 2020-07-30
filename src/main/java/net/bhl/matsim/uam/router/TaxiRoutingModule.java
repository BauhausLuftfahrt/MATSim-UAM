package net.bhl.matsim.uam.router;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.CompositeStageActivityTypes;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.facilities.Facility;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the route legs for a trip using Taxi mode.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class TaxiRoutingModule implements RoutingModule {

	private LeastCostPathCalculator plcpccar;
	private Network carNetwork;
	private Scenario scenario;

	public TaxiRoutingModule(Scenario scenario2, LeastCostPathCalculator pathCalculator, Network networkCar) {

		this.plcpccar = pathCalculator;
		this.carNetwork = networkCar;
		this.scenario = scenario2;
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
												 Person person) {
		PopulationFactory populationFactory = scenario.getPopulation().getFactory();
		RouteFactories routeFactory = populationFactory.getRouteFactories();
		final List<PlanElement> trip = new ArrayList<>();

		Link originLink = this.carNetwork.getLinks().get(fromFacility.getLinkId());

		Link destinationLink = this.carNetwork.getLinks().get(toFacility.getLinkId());

		Path path = plcpccar.calcLeastCostPath(originLink.getToNode(), destinationLink.getFromNode(), departureTime,
				person, null);

		Route route = routeFactory.createRoute(Route.class, originLink.getId(), destinationLink.getId());

		route.setTravelTime(path.travelCost);
		double distance = 0.0;
		for (Link link : path.links) {
			distance += link.getLength();
		}
		route.setDistance(distance);
		final Leg teleportationleg = populationFactory.createLeg("taxi");
		teleportationleg.setTravelTime(path.travelCost);
		teleportationleg.setRoute(route);
		trip.add(teleportationleg);

		return trip;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {

		return new CompositeStageActivityTypes();
	}

}
