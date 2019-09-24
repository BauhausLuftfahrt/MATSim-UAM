package net.bhl.matsim.uam.modechoice.estimation.uam;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.config.TransitConfigGroup;

import ch.ethz.matsim.baseline_scenario.transit.routing.EnrichedTransitRoute;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMRoutes;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.UAMUtilitiesAccessEgress;
import net.bhl.matsim.uam.events.UAMUtilitiesData;
import net.bhl.matsim.uam.events.UAMUtilitiesTrip;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionFinder;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionInformation;

/**
 * This class defines the predictor for UAM trips.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CustomUAMPredictor {
	public static final String UAM_INTERACTION = "uam_interaction";
	public static final String TELEPORTATION_LEG_MODE = "uam";

	private static final Logger log = Logger.getLogger(CustomUAMPredictor.class);

	private final UAMManager manager;
	private final Scenario scenario;
	private final WaitingStationData waitingData;
	private final UAMConfigGroup uamConfig;
	private final Set<String> availableModes;
	private final Network carNetwork;
	private final TripRouter tripRouter;
	private LeastCostPathCalculator plcpccar;
	private final CustomModeChoiceParameters parameters;
	private UAMStationConnectionGraph stationConnectionutilities;
	private SubscriptionFinder subscriptions;

	public CustomUAMPredictor(UAMManager manager, Scenario scenario, WaitingStationData waitingData,
			UAMConfigGroup uamConfig, TransitConfigGroup transitConfigGroup, Set<String> availableModes,
			Network carNetwork, LeastCostPathCalculator plcpccar, TripRouter transitRouter,
			CustomModeChoiceParameters parameters, UAMStationConnectionGraph stationConnectionutilities,
			SubscriptionFinder subscriptions) {
		this.manager = manager;
		this.scenario = scenario;
		this.waitingData = waitingData;
		this.uamConfig = uamConfig;
		this.availableModes = availableModes;
		this.carNetwork = carNetwork;
		this.plcpccar = plcpccar;
		this.tripRouter = transitRouter;
		this.parameters = parameters;
		this.stationConnectionutilities = stationConnectionutilities;
		this.subscriptions = subscriptions;
	}

	/**
	 * @param trip Mode choice trip
	 * @return A UAMPrediction of a trip containing its utility and travel time.
	 */
	public CustomUAMPrediction predictTrip(ModeChoiceTrip trip) {
		Trip tripInformation = trip.getTripInformation();

		Link originLink = this.carNetwork.getLinks().get(tripInformation.getOriginActivity().getLinkId());
		Link destinationLink = this.carNetwork.getLinks().get(tripInformation.getDestinationActivity().getLinkId());

		UAMTripData UAMTrip = calcBestRoute(trip.getPerson(), originLink, destinationLink,
				tripInformation.getOriginActivity().getEndTime());

		return new CustomUAMPrediction(UAMTrip.maxUtility, UAMTrip.minTravelTime);
	}

	/**
	 * @param person        Person to perform the trip
	 * @param fromLink      Origin link
	 * @param toLink        Destination link
	 * @param departureTime Trip departure time
	 * @return A UAMTripData containing travel time and utility for the given route.
	 */
	private UAMTripData calcBestRoute(Person person, Link fromLink, Link toLink, double departureTime) {

		Set<String> modes = new HashSet<>();
		modes.addAll(availableModes);
//		if (person.getAttributes().getAttribute("carAvailability").equals("never")) {
//
//			modes.remove("car");
//		}

		Coord originCoord = fromLink.getCoord();
		Coord destinationCoord = toLink.getCoord();

		Collection<UAMStation> stationsOrigin = manager.getStations().spatialStations.getDisk(originCoord.getX(),
				originCoord.getY(), uamConfig.getSearchRadius());
		Collection<UAMStation> stationsDestination = manager.getStations().spatialStations
				.getDisk(destinationCoord.getX(), destinationCoord.getY(), uamConfig.getSearchRadius());

		Map<Id<UAMStation>, Double> accessUtilityMap = new HashMap<>();
		Map<Id<UAMStation>, String> accessModeMap = new HashMap<>();
		for (UAMStation stationOrigin : stationsOrigin) {
			double bestUtility = Double.NEGATIVE_INFINITY;
			String bestMode = TransportMode.walk;

			for (String mode : modes) {
				double utility = estimateUtilityWrapper(person, true, fromLink, departureTime, stationOrigin, mode);
				if (utility > bestUtility) {
					bestUtility = utility;
					bestMode = mode;
				}

				if (parameters.storeUAMUtilities != 0) {
					UAMUtilitiesData.accessEgressOptions.add(new UAMUtilitiesAccessEgress(person.getId(),
							stationOrigin.getId(), fromLink.getId(), true, utility, mode, departureTime));
				}
			}

			accessUtilityMap.put(stationOrigin.getId(), bestUtility);
			accessModeMap.put(stationOrigin.getId(), bestMode);
		}

		Map<Id<UAMStation>, Double> egressUtilityMap = new HashMap<>();
		Map<Id<UAMStation>, String> egressModeMap = new HashMap<>();
		for (UAMStation stationDestination : stationsDestination) {
			double bestUtility = Double.NEGATIVE_INFINITY;
			String bestMode = TransportMode.walk;

			for (String mode : modes) {
				double utility = estimateUtilityWrapper(person, false, toLink, departureTime, stationDestination, mode);
				if (utility > bestUtility) {
					bestUtility = utility;
					bestMode = mode;
				}

				if (parameters.storeUAMUtilities != 0) {
					UAMUtilitiesData.accessEgressOptions.add(new UAMUtilitiesAccessEgress(person.getId(),
							stationDestination.getId(), fromLink.getId(), false, utility, mode, departureTime));
				}
			}

			egressUtilityMap.put(stationDestination.getId(), bestUtility);
			egressModeMap.put(stationDestination.getId(), bestMode);
		}

		double maxUtility = Double.NEGATIVE_INFINITY;
		UAMStation bestStationOrigin = null, bestStationDestination = null;

		for (UAMStation stationOrigin : stationsOrigin) {
			for (UAMStation stationDestination : stationsDestination) {
				if (stationOrigin == stationDestination)
					continue;

				// data setup (optional)
				UAMUtilitiesTrip utilities = new UAMUtilitiesTrip();
				utilities.person = person.getId();
				utilities.originStation = stationOrigin.getId();
				utilities.destinationStation = stationDestination.getId();
				utilities.accessMode = accessModeMap.get(stationOrigin.getId());
				utilities.egressMode = egressModeMap.get(stationDestination.getId());
				utilities.originLink = fromLink.getId();
				utilities.destinationLink = toLink.getId();
				utilities.time = departureTime;

				// access
				double accessUtility = accessUtilityMap.get(stationOrigin.getId());
				utilities.accessUtility = accessUtility;
				double tripUtility = accessUtility;

				// uam wait
				double uamWaitTime;
				try {
					int index = (int) Math.floor(departureTime / 1800.0);
					uamWaitTime = this.waitingData.getWaitingData().get(stationOrigin.getId()).getWaitingTimes()[index];
				} catch (IndexOutOfBoundsException e) {
					log.info("UAM waiting time requestes for non-existent time slot.");
					uamWaitTime = 600;
				}

				double uamWaitUtility = (uamWaitTime * this.parameters.betaWaitUAM_min / 60.0);
				utilities.uamWaitUtility = uamWaitUtility;
				tripUtility += uamWaitUtility;

				// uam flight
				double uamFlightUtility = this.stationConnectionutilities.getUtility(stationOrigin.getId(),
						stationDestination.getId());
				utilities.uamFlightUtility = uamFlightUtility;
				tripUtility += uamFlightUtility;

				// uam flight income
				double income;
				if (person.getAttributes().getAttribute("income") != null)
					income = (double) person.getAttributes().getAttribute("income");
				else
					income = parameters.averageIncome;

				double distance = CoordUtils.calcEuclideanDistance(stationDestination.getLocationLink().getCoord(),
						stationOrigin.getLocationLink().getCoord());

				double uamIncomeUtility = parameters.betaCost(distance * 1e-3, income) * parameters.distanceCostUAM_km
						* distance * 1e-3;
				utilities.uamIncomeUtility = uamIncomeUtility;
				tripUtility += uamIncomeUtility;

				// egress
				double egressUtility = egressUtilityMap.get(stationDestination.getId());
				utilities.egressUtility = egressUtility;
				tripUtility += egressUtility;
				utilities.totalUtility = tripUtility;

				// save data
				if (parameters.storeUAMUtilities != 0)
					UAMUtilitiesData.tripOptions.add(utilities);

				if (tripUtility > maxUtility) {
					bestStationOrigin = stationOrigin;
					bestStationDestination = stationDestination;
					maxUtility = tripUtility;
				}
			}
		}

		try {
			UAMRoutes.getInstance().add(person.getId(), departureTime,
					new UAMRoute(accessModeMap.get(bestStationOrigin.getId()), bestStationOrigin,
							bestStationDestination, egressModeMap.get(bestStationDestination.getId())));
		} catch (NullPointerException e) {
			// do not store best UAM route if any part of the UAM trip cannot be estimated
			// (e.g. no destination station)
			log.warn("Requested UAM prediction for person " + person.getId() + " did not provide all UAM trip parts.");
			// return Double.NEGATIVE_INFINITY;
			return new UAMTripData(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}

		double minTravelTime = calcTraveltime(fromLink, toLink, departureTime,
				accessModeMap.get(bestStationOrigin.getId()), bestStationOrigin,
				egressModeMap.get(bestStationDestination.getId()), bestStationDestination);

		return new UAMTripData(maxUtility, minTravelTime);
	}

	/**
	 * @param person  Person to perform the trip
	 * @param access  True if it is an access leg, false if it is an egress leg
	 * @param link    Origin link
	 * @param time    Trip departure time
	 * @param station UAM Station, leg destination if access, leg origin if egress
	 * @param mode    Mode used
	 * @return The utility of an access or egress leg to a UAM Station
	 */
	private double estimateUtilityWrapper(Person person, boolean access, Link link, double time, UAMStation station,
			String mode) {
		Coord destinationCoord;
		Link from, to;
		if (access) {
			from = link;
			to = station.getLocationLink();
			destinationCoord = station.getLocationLink().getCoord();
		} else {
			from = station.getLocationLink();
			to = link;
			destinationCoord = link.getCoord();
		}

		switch (mode) {
		case TransportMode.car:
			List<? extends PlanElement> result = tripRouter.calcRoute(TransportMode.car, new LinkWrapperFacility(from),
					new LinkWrapperFacility(to), time, person);

			NetworkRoute route = (NetworkRoute) ((Leg) result.get(0)).getRoute();

			return estimateUtility(mode, route.getTravelTime(), route.getDistance(), person,
					CoordUtils.calcEuclideanDistance(link.getCoord(), station.getLocationLink().getCoord()),
					destinationCoord);
		case TransportMode.taxi:
			// TODO just copy of car, rework into actual taxi
			List<? extends PlanElement> resultTaxi = tripRouter.calcRoute(TransportMode.car,
					new LinkWrapperFacility(from), new LinkWrapperFacility(to), time, person);

			NetworkRoute routeTaxi = (NetworkRoute) ((Leg) resultTaxi.get(0)).getRoute();
			return estimateUtility(mode, routeTaxi.getTravelTime(), routeTaxi.getDistance(), person,
					CoordUtils.calcEuclideanDistance(link.getCoord(), station.getLocationLink().getCoord()),
					destinationCoord);
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = tripRouter
						.calcRoute(TransportMode.pt, new LinkWrapperFacility(from), new LinkWrapperFacility(to), time,
								person)
						.stream().filter(Leg.class::isInstance).map(Leg.class::cast).collect(Collectors.toList());

				if (legs.size() > 1)
					return estiamteUtility(legs, person,
							CoordUtils.calcEuclideanDistance(link.getCoord(), station.getLocationLink().getCoord()));
				return Double.NEGATIVE_INFINITY;
			} else {
				double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
						.getTeleportedModeSpeeds().get(mode);
				double distanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
						.get("planscalcroute")).getBeelineDistanceFactors().get(mode);
				double distance = CoordUtils.calcEuclideanDistance(link.getCoord(),
						station.getLocationLink().getCoord());
				double travelDistance = distance * distanceFactor;
				double travelTime = travelDistance / speed;
				return estimateUtility(mode, travelTime, travelDistance, person,
						CoordUtils.calcEuclideanDistance(link.getCoord(), station.getLocationLink().getCoord()),
						destinationCoord);
			}

		default:
			double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
					.getTeleportedModeSpeeds().get(mode);
			double distanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules()
					.get("planscalcroute")).getBeelineDistanceFactors().get(mode);
			double egressDist = CoordUtils.calcEuclideanDistance(link.getCoord(), station.getLocationLink().getCoord());

			double travelDistance = egressDist * distanceFactor;
			double travelTime = travelDistance / speed;
			return estimateUtility(mode, travelTime, travelDistance, person, egressDist * 1e-03, destinationCoord);
		}
	}

	private double estiamteUtility(List<Leg> legs, Person person, double crowflyDistance_km) {
		SubscriptionInformation subscriptions = this.subscriptions.getSubscriptions(person);
		double distance = 0.0;
		double utility = 0.0;
		int transfers = -1;
		for (Leg leg : legs) {
			if (leg.getMode().equals(TransportMode.transit_walk) || leg.getMode().equals(TransportMode.access_walk)
					|| leg.getMode().equals(TransportMode.egress_walk))
				utility += leg.getTravelTime() * parameters.betaAccessEgressTimePublicTransport_min / 60.0;
			else {
				transfers++;
				utility += ((EnrichedTransitRoute) leg.getRoute()).getTravelTime()
						* parameters.betaInVehicleTimePublicTransport_min / 60.0;
				utility += ((EnrichedTransitRoute) leg.getRoute()).getWaitingTime() / 60.0
						+ parameters.betaTransferTimePublicTransport_min;
				distance += leg.getRoute().getDistance();
			}

		}
		double cost = parameters.costPublicTransport(subscriptions, distance * 1e-03);
		double income = 0.0;

		int shortDistance = crowflyDistance_km < 2.0 ? 1 : 0;
		int longDistance = crowflyDistance_km > 6.0 ? 1 : 0;

		int residence = 0;
		if (person.getAttributes().getAttribute("residence") != null) {
			residence = (int) person.getAttributes().getAttribute("residence");
		}

		if (person.getAttributes().getAttribute("income") != null)
			income = (double) person.getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;
		double betaCost = parameters.betaCost(distance * 1e-03, income);
		utility += parameters.alphaPublicTransport;
		utility += parameters.betaNumberOfTransfersPublicTransport * transfers;
		utility += parameters.betaShortPublicTransport * shortDistance;
		utility += parameters.betaLongPublicTransport * longDistance;
		utility += parameters.betaResidenceRuralPublicTransport * (residence == 3 ? 1 : 0);
		utility += betaCost * cost;
		utility += parameters.betaUAMTransfer;

		return utility;
	}

	/**
	 * @param mode              Mode used in the trip
	 * @param traveltime        Trip travel time
	 * @param distance          Trip distance
	 * @param person            Person to perform the trip
	 * @param croflyDistance_km Trip crowfly distance in km
	 * @param destinationCoord  Coordinates of the destination
	 * @return The utility of a trip
	 */
	private double estimateUtility(String mode, double traveltime, double distance, Person person,
			double croflyDistance_km, Coord destinationCoord) {
		if (mode.equals(TransportMode.walk)) {
			return parameters.alphaWalk + parameters.betaTravelTimeWalk_min * traveltime / 60.0;
		} else if (mode.equals(TransportMode.car)) {
			double income = 0.0;
			double distance_km = distance * 1e-3;
			if (person.getAttributes().getAttribute("income") != null)
				income = (double) person.getAttributes().getAttribute("income");
			else
				income = parameters.averageIncome;

			int distance_short = croflyDistance_km > 2.0 ? 0 : 1;

			int residence = 0;
			if (person.getAttributes().getAttribute("residence") != null) {
				residence = (int) person.getAttributes().getAttribute("residence");
			}

			int sex = 0;
			if (person.getAttributes().getAttribute("sex") != null) {
				sex = person.getAttributes().getAttribute("sex").equals("m") ? 1 : 0;
			}

			double betaCost = parameters.betaCost(distance_km, income);
			double utility = 0.0;
			utility += parameters.betaShortCar * distance_short;
			utility += parameters.betaResidenceRuralCar * (residence == 3 ? 1 : 0);

			utility += parameters.alphaCar
					+ (parameters.betaTravelTimeCar_min + parameters.betaTravelTImeCarMale * sex)
							* (traveltime / 60.0 + parameters.parkingSearchTimeCar_min)
					+ betaCost * parameters.distanceCostCar_km * distance_km + parameters.betaUAMTransfer;
//			utility += parameters.betaParking * (outsideParis ? 1 : 0);

			return utility;
		} else if (mode.equals(TransportMode.taxi)) {
			double income = 0.0;
			double distance_km = distance * 1e-3;
			if (person.getAttributes().getAttribute("income") != null)
				income = (double) person.getAttributes().getAttribute("income");
			else
				income = parameters.averageIncome;
			double betaCost = parameters.betaCost(distance_km, income);
			double cost = parameters.distanceCostTaxi_km * distance_km + parameters.timeCostTaxi_min * traveltime
					+ parameters.initialCostTaxi;

			return parameters.alphaTaxi + parameters.betaTravelTimeTaxi_min * (traveltime / 60.0)
					+ parameters.betaWaitingTimeTaxi_min * parameters.waitingTimeTaxi_min + betaCost * cost
					+ parameters.betaUAMTransfer;
		} else if (mode.equals(TransportMode.bike)) {
			double age;
			if (person.getAttributes().getAttribute("age") != null)
				age = (double) person.getAttributes().getAttribute("age");
			else
				age = parameters.averageAge;
			return parameters.alphaBike + parameters.betaTravelTimeBike_min * traveltime / 60.0
					+ parameters.betaAgeBike_yr * (age > 18 ? (age - 18) : 0) + parameters.betaUAMTransfer;
		} else if (mode.equals(TransportMode.pt)) {
			// UTILITY HERE FOR TELEPORTED PT
			double utility = 0.0;
			double income = 0.0;
			distance = distance * 1e-3;
			utility += parameters.betaInVehicleTimePublicTransport_min * traveltime / 60.0;
			utility += parameters.alphaPublicTransport;
			utility += parameters.betaUAMTransfer;
			if (person.getAttributes().getAttribute("income") != null)
				income = (double) person.getAttributes().getAttribute("income");
			else
				income = parameters.averageIncome;
			double cost = parameters.costPublicTransport(subscriptions.getSubscriptions(person), distance);
			double betaCost = parameters.betaCost(distance, income);
			utility += betaCost * cost;
			return utility;
		}

		log.warn("Could not estimate utility for mode: " + mode + " for person: " + person.getId());
		return Double.NEGATIVE_INFINITY;
	}

	/**
	 * @param fromLink               Origin link
	 * @param toLink                 Destination link
	 * @param departureTime          Trip departure time
	 * @param accessMode             Mode used for access leg
	 * @param bestOriginStation      Selected origin UAM Station
	 * @param egressMode             Mode used for egress leg
	 * @param bestDestinationStation Selected destination UAM Station
	 * @return Travel time of a UAM trip including access and egress travel times.
	 */
	private double calcTraveltime(Link fromLink, Link toLink, double departureTime, String accessMode,
			UAMStation bestOriginStation, String egressMode, UAMStation bestDestinationStation) {
		double minTravelTime = Double.POSITIVE_INFINITY;
		// Access travel Time
		Link accessStationLink = bestOriginStation.getLocationLink();
		Link originLink = fromLink;
		switch (accessMode) {
		case TransportMode.car:
			if (carNetwork.getLinks().get(originLink.getId()) != null)
				originLink = carNetwork.getLinks().get(originLink.getId());
			else
				originLink = NetworkUtils.getNearestLinkExactly(carNetwork, originLink.getCoord());

			if (carNetwork.getLinks().get(accessStationLink.getId()) != null)
				accessStationLink = carNetwork.getLinks().get(accessStationLink.getId());
			else
				accessStationLink = NetworkUtils.getNearestLinkExactly(carNetwork, accessStationLink.getCoord());
			Path path = plcpccar.calcLeastCostPath(originLink.getFromNode(), accessStationLink.getToNode(),
					departureTime, null, null);

			minTravelTime = path.travelTime;
			break;
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = tripRouter
						.calcRoute(TransportMode.pt, new LinkWrapperFacility(originLink),
								new LinkWrapperFacility(accessStationLink), departureTime, null)
						.stream().filter(Leg.class::isInstance).map(Leg.class::cast).collect(Collectors.toList());
				double timeByPt = 0.0;
				for (Leg leg : legs) {
					timeByPt += leg.getTravelTime();
				}
				minTravelTime = timeByPt;
			} else {
				minTravelTime = estimateBeeLineTravelTime(accessMode, new LinkWrapperFacility(originLink),
						bestOriginStation);
			}
			break;
		default:
			minTravelTime = estimateBeeLineTravelTime(accessMode, new LinkWrapperFacility(originLink),
					bestOriginStation);
			break;
		}
		// UAM flight time
		minTravelTime += this.stationConnectionutilities.getTravelTime(bestOriginStation.getId(),
				bestDestinationStation.getId());
		// Egress TravelTime
		Link egressStationLink = bestDestinationStation.getLocationLink();
		Link destinationLink = toLink;
		switch (egressMode) {
		case TransportMode.car:
			if (carNetwork.getLinks().get(egressStationLink.getId()) != null)
				egressStationLink = carNetwork.getLinks().get(egressStationLink.getId());
			else
				egressStationLink = NetworkUtils.getNearestLinkExactly(carNetwork, egressStationLink.getCoord());

			if (carNetwork.getLinks().get(destinationLink.getId()) != null)
				destinationLink = carNetwork.getLinks().get(destinationLink.getId());
			else
				destinationLink = NetworkUtils.getNearestLinkExactly(carNetwork, destinationLink.getCoord());
			Path path = plcpccar.calcLeastCostPath(egressStationLink.getFromNode(), destinationLink.getToNode(),
					departureTime + minTravelTime, null, null); // departureTime is updated

			minTravelTime += path.travelTime;
			break;
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = tripRouter
						.calcRoute(TransportMode.pt, new LinkWrapperFacility(egressStationLink),
								new LinkWrapperFacility(destinationLink), departureTime, null)
						.stream().filter(Leg.class::isInstance).map(Leg.class::cast).collect(Collectors.toList());
				double timeByPt = 0.0;
				for (Leg leg : legs) {
					timeByPt += leg.getTravelTime();
				}
				minTravelTime += timeByPt;
			} else {
				minTravelTime += estimateBeeLineTravelTime(egressMode, new LinkWrapperFacility(egressStationLink),
						bestDestinationStation);
			}
			break;
		default:
			minTravelTime += estimateBeeLineTravelTime(egressMode, new LinkWrapperFacility(egressStationLink),
					bestDestinationStation);
			break;
		}

		return minTravelTime;
	}

	double estimateBeeLineTravelTime(String mode, Facility<?> facility, UAMStation station) {
		double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
				.getTeleportedModeSpeeds().get(mode);
		double travelDistance = estimateBeeLineDistance(mode, facility, station);
		double travelTime = travelDistance / speed;
		return travelTime;
	}

	double estimateBeeLineDistance(String mode, Facility<?> facility, UAMStation station) {
		double distanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
				.getBeelineDistanceFactors().get(mode);
		double distance = CoordUtils.calcEuclideanDistance(
				scenario.getNetwork().getLinks().get(facility.getLinkId()).getCoord(),
				station.getLocationLink().getCoord());
		double travelDistance = distance * distanceFactor;
		return travelDistance;
	}

	private class UAMTripData {
		public double maxUtility;
		public double minTravelTime;

		public UAMTripData(double maxUtility, double minTravelTime) {
			this.maxUtility = maxUtility;
			this.minTravelTime = minTravelTime;
		}

	}
}
