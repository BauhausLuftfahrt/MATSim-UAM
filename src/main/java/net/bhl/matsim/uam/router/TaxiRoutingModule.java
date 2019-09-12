package net.bhl.matsim.uam.router;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.CompositeStageActivityTypes;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.facilities.Facility;

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
	public List<? extends PlanElement> calcRoute(Facility<?> fromFacility, Facility<?> toFacility, double departureTime,
			Person person) {
		PopulationFactory populationFactory = scenario.getPopulation().getFactory();
		RouteFactories routeFactory = ((PopulationFactory) populationFactory).getRouteFactories();
		final List<PlanElement> trip = new ArrayList<PlanElement>();

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
		final CompositeStageActivityTypes stageTypes = new CompositeStageActivityTypes();

		return stageTypes;
	}

}
