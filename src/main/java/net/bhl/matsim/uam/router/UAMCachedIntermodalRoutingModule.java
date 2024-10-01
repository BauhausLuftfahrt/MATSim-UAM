package net.bhl.matsim.uam.router;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.DefaultRoutingRequest;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.RoutingRequest;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.config.TransitConfigGroup;
import org.matsim.pt.router.TransitRouter;
import org.matsim.utils.objectattributes.attributable.AttributesImpl;

import com.google.common.base.Verify;

import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMFlightLeg;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.data.UAMRoutes;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.router.strategy.UAMStrategyRouter;
import net.bhl.matsim.uam.run.UAMConstants;

/**
 * This class provides the route legs for a trip using UAM from pre-calculated UAM legs.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMCachedIntermodalRoutingModule implements RoutingModule {

	private static final Logger log = LogManager.getLogger(UAMCachedIntermodalRoutingModule.class);
	private final int counterLimit = 10;
	private final Scenario scenario;
	private int counterWarningWaitingTimeSlot = 0;
	private int counterWarningWaitingTimeNull = 0;
	private int counterWarningConvertedToWalk = 0;
	private LeastCostPathCalculator plcpccar;
	private Network carNetwork;
	private RoutingModule transitRouterDelegate;
	private WaitingStationData waitingData;
	private UAMStationConnectionGraph stationConnections;
	private UAMStrategyRouter strategyRouter;

	// constructor used in case of simulating PT
	public UAMCachedIntermodalRoutingModule(Scenario scenario, UAMStations landingStations,
											LeastCostPathCalculator uamPathCalculator, LeastCostPathCalculator plcpccar,
											Network carNetwork, TransitRouter transitRouter, UAMConfigGroup uamConfig,
											TransitConfigGroup transitConfigGroup,
											RoutingModule transitRouterDelegate,
											WaitingStationData waitingData,
											UAMStationConnectionGraph stationConnections) {
		this(scenario, landingStations, uamPathCalculator, plcpccar, carNetwork, uamConfig, transitConfigGroup,
				waitingData, stationConnections);
		this.transitRouterDelegate = transitRouterDelegate;
		this.strategyRouter = new UAMStrategyRouter(transitRouter, scenario, uamConfig, plcpccar,
				landingStations, carNetwork, stationConnections);

	}

	public UAMCachedIntermodalRoutingModule(Scenario scenario, UAMStations landingStations,
											LeastCostPathCalculator uamPathCalculator, LeastCostPathCalculator plcpccar, Network carNetwork,
											UAMConfigGroup uamConfig, TransitConfigGroup transitConfigGroup,
											WaitingStationData waitingData, UAMStationConnectionGraph stationConnections) {
		this.scenario = scenario;
		this.plcpccar = plcpccar;
		this.carNetwork = carNetwork;
		this.waitingData = waitingData;
		this.stationConnections = stationConnections;

		this.strategyRouter = new UAMStrategyRouter(scenario, uamConfig, plcpccar, landingStations,
				carNetwork, stationConnections);
	}


	private Leg createTeleportationLeg(RouteFactories routeFactory, PopulationFactory populationFactory,
									   Link originLink, Link destinationLink, String mode, String actualMode) {

		double speed = ((RoutingConfigGroup) scenario.getConfig().getModules().get(RoutingConfigGroup.GROUP_NAME))
				.getTeleportedModeSpeeds().get(mode);
		Route route = routeFactory.createRoute(Route.class, originLink.getId(), destinationLink.getId());
		double dist = CoordUtils.calcEuclideanDistance(originLink.getCoord(), destinationLink.getCoord());
		route.setDistance(dist);
		route.setTravelTime(dist / speed + 1.0);

		final Leg teleportationleg = populationFactory.createLeg(actualMode);
		teleportationleg.setTravelTime(dist / speed + 1.0);

		teleportationleg.setRoute(route);
		return teleportationleg;
	}

	private Leg createUAMLeg(Link originLink, Link destinationLink, UAMFlightLeg flightLeg,
							 RouteFactories routeFactory, PopulationFactory populationFactory) {
		return createNetworkLeg(UAMConstants.uam, originLink, destinationLink, flightLeg.links, routeFactory,
				populationFactory, flightLeg.travelTime, flightLeg.distance);
	}

	private Leg createCarLeg(String mode, Link originLink, Link destinationLink, double departureTime, Person person,
							 RouteFactories routeFactory, PopulationFactory populationFactory) {
		Verify.verifyNotNull(originLink);
		Verify.verifyNotNull(destinationLink);
		Path path = plcpccar.calcLeastCostPath(originLink.getToNode(), destinationLink.getFromNode(), departureTime,
				person, null);

		double distance = 0;
		for (Link link : path.links)
			distance += link.getLength();

		return createNetworkLeg(mode, originLink, destinationLink, path.links, routeFactory,
				populationFactory, path.travelTime, distance);
	}

	private Leg createNetworkLeg(String mode, Link originLink, Link destinationLink, List<Link> links,
								 RouteFactories routeFactory, PopulationFactory populationFactory, double travelTime,
								 double distance) {
		NetworkRoute route = routeFactory.createRoute(NetworkRoute.class, originLink.getId(), destinationLink.getId());
		route.setLinkIds(originLink.getId(), NetworkUtils.getLinkIds(links), destinationLink.getId());
		route.setTravelTime(travelTime);
		route.setDistance(distance);

		Leg leg = populationFactory.createLeg(mode);
		leg.setTravelTime(travelTime);
		leg.setRoute(route);
		return leg;
	}

	@Override
	public List<? extends PlanElement> calcRoute(RoutingRequest request) {

		Person person = request.getPerson();
		Facility fromFacility = request.getFromFacility();
		Facility toFacility = request.getToFacility();
		double departureTime = request.getDepartureTime();


		Network network = scenario.getNetwork();

		PopulationFactory populationFactory = scenario.getPopulation().getFactory();
		RouteFactories routeFactory = populationFactory.getRouteFactories();
		final List<PlanElement> trip = new ArrayList<>();

		Optional<UAMRoute> optionalUamRoute = Optional.ofNullable(UAMRoutes.getInstance().get(person.getId(), departureTime));
		
		if (optionalUamRoute.isEmpty())
			optionalUamRoute = strategyRouter.estimateUAMRoute(person, fromFacility, toFacility, departureTime);

		if (optionalUamRoute.isEmpty()) {
//			if (counterWarningConvertedToWalk < counterLimit)
//				log.warn("No UAM prediction for person: " + person.getId() + " at time: " + departureTime
//						+ " could be calculated. Trip has been converted to walk.");
//
//			if (counterWarningConvertedToWalk == counterLimit - 1)
//				log.warn("No more UAM prediction warnings will be reported.");

			counterWarningConvertedToWalk++;
			Leg l = createTeleportationLeg(routeFactory, populationFactory,
					network.getLinks().get(fromFacility.getLinkId()),
					network.getLinks().get(toFacility.getLinkId()), TransportMode.walk, TransportMode.walk);
			trip.add(l);
			return trip;
		}
		
		UAMRoute uamRoute = optionalUamRoute.get();

		/* access trip */
		double currentTime = departureTime;

		switch (uamRoute.accessMode) {
			case TransportMode.car:
				Link accessOriginLink = carNetwork.getLinks().get(fromFacility.getLinkId());
				if (accessOriginLink == null)
					accessOriginLink = NetworkUtils.getNearestLinkExactly(carNetwork, fromFacility.getCoord());
				Link accessDestinationLink = carNetwork.getLinks()
						.get(uamRoute.bestOriginStation.getLocationLink().getId());
				Leg carLeg = createCarLeg(UAMConstants.access + TransportMode.car, accessOriginLink,
						accessDestinationLink, departureTime, person, routeFactory, populationFactory);
				currentTime += carLeg.getTravelTime().seconds();
				trip.add(carLeg);
				break;
			case TransportMode.pt:
				// checks if pt is being simulated before adding the trip
				if (scenario.getConfig().transit().isUseTransit()) {

					RoutingRequest routingRequest = DefaultRoutingRequest.of(
					fromFacility,
					new LinkWrapperFacility(uamRoute.bestOriginStation.getLocationLink()),
					departureTime,
					person,
					new AttributesImpl());

					@SuppressWarnings("unchecked")				
					List<PlanElement> legs = (List<PlanElement>) this.transitRouterDelegate.calcRoute(routingRequest);
					for (PlanElement leg : legs) {
						if (leg instanceof Leg)
							currentTime += ((Leg) leg).getTravelTime().seconds();
					}
					trip.addAll(legs);
				} else {
					Leg uavPtAccessLeg = createTeleportationLeg(routeFactory, populationFactory,
							network.getLinks().get(fromFacility.getLinkId()), uamRoute.bestOriginStation.getLocationLink(),
							uamRoute.accessMode, uamRoute.accessMode);
					currentTime += uavPtAccessLeg.getTravelTime().seconds();
					trip.add(uavPtAccessLeg);
				}
				break;
			default:
				Leg uavAccessLeg = createTeleportationLeg(routeFactory, populationFactory,
						network.getLinks().get(fromFacility.getLinkId()), uamRoute.bestOriginStation.getLocationLink(),
						uamRoute.accessMode, UAMConstants.access + uamRoute.accessMode);
				currentTime += uavAccessLeg.getTravelTime().seconds();
				uavAccessLeg.setTravelTime(0.0);
				uavAccessLeg.getRoute().setTravelTime(0.0);
				trip.add(uavAccessLeg);
		}

		/* origin station */
		Activity uav_interaction1 = populationFactory.createActivityFromLinkId(UAMConstants.interaction,
				uamRoute.bestOriginStation.getLocationLink().getId());
		uav_interaction1.setMaximumDuration(uamRoute.bestOriginStation.getPreFlightTime()); // Changes the value for the
		// duration of UAM
		// interaction after
		// arriving at the station
		// from the access leg

		trip.add(uav_interaction1);
		currentTime += uamRoute.bestOriginStation.getPreFlightTime(); // Still have to figure out why this doesn't
		// affect events

		try {
			// TODO: Rework this section and make it more efficient.
			double waitTime = this.waitingData.getWaitingData().get(uamRoute.bestOriginStation.getId())
					.getWaitingTime(departureTime);
			currentTime += waitTime;
		} catch (IndexOutOfBoundsException e) {
//			if (counterWarningWaitingTimeSlot < counterLimit)
//				log.warn(UAMConstants.uam.toUpperCase() + " waiting time requests for non-existent time slot. Using default value of "
//						+ uamRoute.bestOriginStation.getDefaultWaitTime() + " for station "
//						+ uamRoute.bestOriginStation.getId().toString());
//
//			if (counterWarningWaitingTimeSlot == counterLimit - 1)
//				log.warn("No more waiting time warnings will be reported.");

			counterWarningWaitingTimeSlot++;
			currentTime += uamRoute.bestOriginStation.getDefaultWaitTime(); // Added wait time of Origin Station
		} catch (NullPointerException e) {
//			if (counterWarningWaitingTimeNull < counterLimit)
//				log.warn(UAMConstants.uam.toUpperCase() + " waiting time not available. Using default value of "
//						+ uamRoute.bestOriginStation.getDefaultWaitTime() + " for station "
//						+ uamRoute.bestOriginStation.getId().toString());
//
//			if (counterWarningWaitingTimeNull == counterLimit - 1)
//				log.warn("No more waiting time warnings will be reported.");
//
//			counterWarningWaitingTimeNull++;
			currentTime += uamRoute.bestOriginStation.getDefaultWaitTime(); // Added wait time of Origin Station
		}

		/* uam leg */
		// Retrieve pre-calculated UAM leg
		UAMFlightLeg cachedLeg = this.stationConnections.getFlightLeg(uamRoute.bestOriginStation.getId(),
				uamRoute.bestDestinationStation.getId());
		trip.add(createUAMLeg(uamRoute.bestOriginStation.getLocationLink(),
				uamRoute.bestDestinationStation.getLocationLink(), cachedLeg, routeFactory, populationFactory));
		currentTime += cachedLeg.travelTime;

		/* destination station */ // Add here passenger activities that only the passenger performs at destination
		// station
		Activity uav_interaction2 = populationFactory.createActivityFromLinkId(UAMConstants.interaction,
				uamRoute.bestDestinationStation.getLocationLink().getId());
		uav_interaction2.setMaximumDuration(uamRoute.bestDestinationStation.getPostFlightTime()); // Changes the value
		// for the duration
		// of UAM
		// interaction after
		// arriving at the
		// station from the
		// Flying leg

		trip.add(uav_interaction2);
		currentTime += uamRoute.bestDestinationStation.getPostFlightTime(); // Still have to figure out why this doesn't
		// affect events

		/* egress leg */
		switch (uamRoute.egressMode) {
			case TransportMode.car:
				Link egressDestinationLink = carNetwork.getLinks().get(toFacility.getLinkId());
				if (egressDestinationLink == null)
					egressDestinationLink = NetworkUtils.getNearestLinkExactly(carNetwork, toFacility.getCoord());

				Link egressOriginLink = carNetwork.getLinks()
						.get(uamRoute.bestDestinationStation.getLocationLink().getId());

				Leg carLeg = createCarLeg(UAMConstants.egress + TransportMode.car, egressOriginLink,
						egressDestinationLink, currentTime, person, routeFactory, populationFactory);
				trip.add(carLeg);
				break;
			case TransportMode.pt:
				// checks if pt is being simulated before adding the trip
				if (scenario.getConfig().transit().isUseTransit()) {
					RoutingRequest routingRequest = DefaultRoutingRequest.of(
						new LinkWrapperFacility(uamRoute.bestDestinationStation.getLocationLink()),
					toFacility,
					currentTime,
					person,
					new AttributesImpl());

					@SuppressWarnings("unchecked")
					List<PlanElement> legs = (List<PlanElement>) this.transitRouterDelegate.calcRoute(routingRequest);
					trip.addAll(legs);
				} else {
					Leg uavPtEgressLeg = createTeleportationLeg(routeFactory, populationFactory,
							uamRoute.bestDestinationStation.getLocationLink(),
							network.getLinks().get(toFacility.getLinkId()), uamRoute.egressMode, uamRoute.egressMode);
					trip.add(uavPtEgressLeg);
				}
				break;
			default:
				Leg uavEgressLeg = createTeleportationLeg(routeFactory, populationFactory,
						uamRoute.bestDestinationStation.getLocationLink(), network.getLinks().get(toFacility.getLinkId()),
						uamRoute.egressMode, UAMConstants.egress + uamRoute.egressMode);
				trip.add(uavEgressLeg);
		}

		return trip;

	}
}