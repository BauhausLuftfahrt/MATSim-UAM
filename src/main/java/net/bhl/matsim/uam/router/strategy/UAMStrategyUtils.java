package net.bhl.matsim.uam.router.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.router.TransitRouter;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMAccessRouteData;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.router.UAMIntermodalRoutingModule;

/**
 * This class provides the methods used for different UAMStrategies
 * 
 * @author Aitan Militao
 */
public class UAMStrategyUtils {
	private final Scenario scenario;
	private UAMStations landingStations;
	private UAMConfigGroup uamConfig;
	private UAMStationConnectionGraph stationConnectionutilities;
	private Network carNetwork;
	private TransitRouter transitRouter;
	private ParallelLeastCostPathCalculator plcpc;
	private LeastCostPathCalculator plcpccar;
	private CustomModeChoiceParameters parameters;
	private static final Logger log = Logger.getLogger(UAMIntermodalRoutingModule.class);

	public UAMStrategyUtils(UAMStations landingStations, UAMConfigGroup uamConfig, Scenario scenario,
			UAMStationConnectionGraph stationConnectionutilities, Network carNetwork, TransitRouter transitRouter,
			ParallelLeastCostPathCalculator plcpc, LeastCostPathCalculator plcpccar,
			CustomModeChoiceParameters parameters) {
		this.landingStations = landingStations;
		this.uamConfig = uamConfig;
		this.scenario = scenario;
		this.stationConnectionutilities = stationConnectionutilities;
		this.carNetwork = carNetwork;
		this.transitRouter = transitRouter;
		this.plcpc = plcpc;
		this.plcpccar = plcpccar;
		this.parameters = parameters;
	}

	/**
	 * @return the list of possible UAMStations accessible from/to a specific
	 *         location (facility) based in the searchRadius parameter defined in
	 *         the config file.
	 */
	Collection<UAMStation> getPossibleStations(Facility<?> facility) {
		Coord facilityCoord = facility.getCoord();
		Collection<UAMStation> possibleStations = landingStations.spatialStations.getDisk(facilityCoord.getX(),
				facilityCoord.getY(), uamConfig.getSearchRadius());
		return possibleStations;
	}

	/**
	 * @param access   true if access leg, false for egress legs.
	 * @param stations list of possible stations to be checked
	 * @param facility the origin of the trip (if access leg); the trip final
	 *                 destination (if egress leg).
	 * @return a map with station Id's as keys and UAMAcceRouteData as values.
	 */
	Map<Id<UAMStation>, UAMAccessRouteData> getAccessRouteData(boolean access, Collection<UAMStation> stations,
			Facility<?> facility, double departureTime) {
		Set<String> modes = new HashSet<>();
		modes.addAll(uamConfig.getAvailableAccessModes());
		Map<Id<UAMStation>, UAMAccessRouteData> accessRouteData = new HashMap<>();
		String bestModeDistance = TransportMode.walk;
		String bestModeTime = TransportMode.walk;
		for (UAMStation station : stations) {
			double minDistance = Double.POSITIVE_INFINITY;
			double minAccessTime = Double.POSITIVE_INFINITY;
			for (String mode : modes) {
				double travelTime = estimateTime(access, facility, departureTime, station, mode);
				double distance = estimateDistance(access, facility, departureTime, station, mode);

				if (distance < minDistance) {
					minDistance = distance;
					bestModeDistance = mode;
				}
				if (travelTime < minAccessTime) {
					minAccessTime = travelTime;
					bestModeTime = mode;
				}
			}
			UAMAccessRouteData accessRoute = new UAMAccessRouteData(minDistance, minAccessTime, bestModeDistance,
					bestModeTime, station);
			accessRouteData.put(station.getId(), accessRoute);
		}
		return accessRouteData;
	}

	/**
	 * Compares access/egress distance to the minimum walking distance set in the
	 * config file. For distance based strategy it also selects the access/egress
	 * modes based on minimum travel time.
	 * 
	 * @return updated mode for access/egress routes
	 */
	String checkStationAccessDistance(boolean access, String currentMode, UAMStation station, UAMStation originStation,
			double distance, Facility<?> facility, Facility<?> originFacility, double departureTime, String accessMode,
			boolean distanceStrategy) {
		Set<String> modes = new HashSet<>();
		modes.addAll(uamConfig.getAvailableAccessModes());
		String bestMode = currentMode;
		double currentDepartureTime = departureTime;
		if (!access && distanceStrategy) {
			double flyTime = this.stationConnectionutilities.getTravelTime(originStation.getId(), station.getId());
			currentDepartureTime += estimateTime(true, originFacility, departureTime, originStation, accessMode)
					+ flyTime;
		}
		if (distance <= uamConfig.getWalkDistance()) {
			return TransportMode.walk;
		} else if (distanceStrategy) {
			double minAccessTime = Double.POSITIVE_INFINITY;
			for (String mode : modes) {
				double travelTime = estimateTime(access, facility, currentDepartureTime, station, mode);
				if (travelTime < minAccessTime) {
					bestMode = mode;
					minAccessTime = travelTime;
				}
			}
		}
		return bestMode;
	}

	String checkStationAccessMinDistance(double distance, String currentMode) {
		if (distance <= uamConfig.getWalkDistance()) {
			return TransportMode.walk;
		} else {
			return currentMode;
		}
	}

	double estimateDistance(boolean access, Facility<?> facility, double time, UAMStation station, String mode) {
		Network network = scenario.getNetwork();
		Link from, to;
		if (access) {
			from = network.getLinks().get(facility.getLinkId());
			to = station.getLocationLink();
		} else {
			from = station.getLocationLink();
			to = network.getLinks().get(facility.getLinkId());
		}

		switch (mode) {
		case TransportMode.taxi:
		case TransportMode.car:
			if (carNetwork.getLinks().get(from.getId()) != null)
				from = carNetwork.getLinks().get(from.getId());
			else
				from = NetworkUtils.getNearestLinkExactly(carNetwork, from.getCoord());

			if (carNetwork.getLinks().get(to.getId()) != null)
				to = carNetwork.getLinks().get(to.getId());
			else
				to = NetworkUtils.getNearestLinkExactly(carNetwork, to.getCoord());

			Path path = plcpccar.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, null, null);
			double distanceByCar = 0.0;
			for (Link l : path.links) {
				distanceByCar += l.getLength();
			}
			return distanceByCar;
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to),
						time, null);
				double distanceByPt = 0.0;
				for (Leg leg : legs) {
					distanceByPt += leg.getRoute().getDistance();
				}
				return distanceByPt;
			} else {
				return estimateBeeLineDistance(mode, facility, station);
			}
		default:

			return estimateBeeLineDistance(mode, facility, station);
		}
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

	double estimateTime(boolean access, Facility<?> facility, double time, UAMStation station, String mode) {
		Network network = scenario.getNetwork();
		Link from, to;
		if (access) {
			from = network.getLinks().get(facility.getLinkId());
			to = station.getLocationLink();
		} else {
			from = station.getLocationLink();
			to = network.getLinks().get(facility.getLinkId());
		}

		switch (mode) {
		case TransportMode.taxi:
		case TransportMode.car:
			if (carNetwork.getLinks().get(from.getId()) != null)
				from = carNetwork.getLinks().get(from.getId());
			else
				from = NetworkUtils.getNearestLinkExactly(carNetwork, from.getCoord());

			if (carNetwork.getLinks().get(to.getId()) != null)
				to = carNetwork.getLinks().get(to.getId());
			else
				to = NetworkUtils.getNearestLinkExactly(carNetwork, to.getCoord());
			Path path = plcpccar.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, null, null);

			return path.travelTime;
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to),
						time, null);
				double timeByPt = 0.0;
				for (Leg leg : legs) {
					timeByPt += leg.getTravelTime();
				}
				return timeByPt;
			} else {
				return estimateBeeLineTravelTime(mode, facility, station);
			}

		default:
			return estimateBeeLineTravelTime(mode, facility, station);
		}
	}

	double estimateBeeLineTravelTime(String mode, Facility<?> facility, UAMStation station) {
		double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
				.getTeleportedModeSpeeds().get(mode);
		double travelDistance = estimateBeeLineDistance(mode, facility, station);
		double travelTime = travelDistance / speed;
		return travelTime;
	}

	double estimateUtilityWrapper(Person person, boolean access, Facility<?> facility, double time, UAMStation station,
			String mode) {
		Network network = scenario.getNetwork();
		Link from, to;
		if (access) {
			from = network.getLinks().get(facility.getLinkId());
			to = station.getLocationLink();
		} else {
			from = station.getLocationLink();
			to = network.getLinks().get(facility.getLinkId());
		}

		switch (mode) {
		case TransportMode.taxi:
		case TransportMode.car:
			if (carNetwork.getLinks().get(from.getId()) != null)
				from = carNetwork.getLinks().get(from.getId());
			else
				from = NetworkUtils.getNearestLinkExactly(carNetwork, from.getCoord());

			if (carNetwork.getLinks().get(to.getId()) != null)
				to = carNetwork.getLinks().get(to.getId());
			else
				to = NetworkUtils.getNearestLinkExactly(carNetwork, to.getCoord());

			Path path = plcpccar.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, person, null);
			double distance = 0.0;
			for (Link l : path.links) {
				distance += l.getLength();
			}
			return estimateUtility(mode, path.travelTime, distance, person);
		case TransportMode.pt:
			if (uamConfig.getPtSimulation()) {
				List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to),
						time, person);
				if (legs.size() > 1)
					return estiamteUtility(legs, person);
				return Double.NEGATIVE_INFINITY;
			} else {
				double travelDistance = estimateBeeLineDistance(mode, facility, station);
				double travelTime = estimateBeeLineTravelTime(mode, facility, station);
				;
				return estimateUtility(mode, travelTime, travelDistance, person);
			}

		default:
			double travelDistance = estimateBeeLineDistance(mode, facility, station);
			double travelTime = estimateBeeLineTravelTime(mode, facility, station);
			;
			return estimateUtility(mode, travelTime, travelDistance, person);
		}
	}

	private double estimateUtility(String mode, double traveltime, double distance, Person person) {
		if (mode.equals(TransportMode.walk)) {
			return parameters.alphaWalk + parameters.betaTravelTimeWalk_min * traveltime / 60.0;
		} else if (mode.equals(TransportMode.car)) {
			double income = 0.0;
			double distance_km = distance * 1e-3;
			if (person.getAttributes().getAttribute("income") != null)
				income = (double) person.getAttributes().getAttribute("income");
			else
				income = parameters.averageIncome;
			double betaCost = parameters.betaCost(distance_km, income);

			return parameters.alphaCar + parameters.betaTravelTimeCar_min * (traveltime / 60.0)
					+ parameters.betaWaitingTimeTaxi_min * parameters.waitingTimeTaxi_min // SHARED CAR
					+ betaCost * parameters.distanceCostUAMCar_km * distance_km + parameters.betaUAMTransfer;
		} else if (mode.equals(TransportMode.taxi)) {
			double income;
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
			// Not sure yet what would be the best method to estimate utility when not
			// simulating pt, currently the first one is being used
			if (!uamConfig.getPtSimulation()) {
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
				double cost = parameters.costPublicTransport(null, distance);
				double betaCost = parameters.betaCost(distance, income);
				utility += betaCost * cost;
				return utility;
			} else {
				double utility = 0.0;
				utility += 1 + parameters.alphaWalk + parameters.betaTravelTimeWalk_min * traveltime / 60.0;
				return utility;
			}
		}

		log.warn("Could not estimate utility for mode: " + mode + " for person: " + person.getId());
		return Double.NEGATIVE_INFINITY;
	}

	private double estiamteUtility(List<Leg> legs, Person person) {
		double distance = 0.0;
		double utility = 0.0;
		int transfers = -1;
		for (Leg leg : legs) {
			if (leg.getMode().equals(TransportMode.transit_walk) || leg.getMode().equals(TransportMode.access_walk)
					|| leg.getMode().equals(TransportMode.egress_walk))
				utility += leg.getTravelTime() * parameters.betaAccessEgressTimePublicTransport_min / 60.0;
			else {
				transfers++;
				utility += leg.getRoute().getTravelTime() * parameters.betaInVehicleTimePublicTransport_min / 60.0;
				distance += leg.getRoute().getDistance();
			}
		}

		utility += transfers * parameters.betaNumberOfTransfersPublicTransport;
		distance = distance * 1e-3;

		double cost = parameters.costPublicTransport(null, distance);
		double income = 0.0;

		if (person.getAttributes().getAttribute("income") != null)
			income = (double) person.getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;
		double betaCost = parameters.betaCost(distance, income);
		utility += betaCost * cost;
		utility += parameters.alphaPublicTransport;
		utility += parameters.betaUAMTransfer;

		return utility;
	}

	Future<Path> getFlyDistance(Node fromNode, Node toNode) {
		return plcpc.calcLeastCostPath(fromNode, toNode, 0.0, null, null);
	}

	double getFlyTime(UAMStation stationOrigin, UAMStation stationDestination) {
		return this.stationConnectionutilities.getTravelTime(stationOrigin.getId(), stationDestination.getId());
	}

	Network getNetwork() {
		return scenario.getNetwork();
	}

	Set<String> getModes() {
		return uamConfig.getAvailableAccessModes();
	}

	public CustomModeChoiceParameters getParameters() {
		return parameters;
	}

	public UAMStationConnectionGraph getStationConnectionutilities() {
		return stationConnectionutilities;
	}

	public List<Double> getTotalTravelTime(Facility<?> fromFacility, Facility<?> toFacility, Double departureTime,
			UAMRoute uamRoute) {
		List<Double> travelTimes = new ArrayList<Double>(4);
		Double accessTravelTime = estimateTime(true, fromFacility, departureTime, uamRoute.bestOriginStation,
				uamRoute.accessMode);
		Double flyTime = getFlyTime(uamRoute.bestOriginStation, uamRoute.bestDestinationStation);
		departureTime += accessTravelTime + flyTime;
		Double egressTravelTime = estimateTime(false, toFacility, departureTime, uamRoute.bestDestinationStation,
				uamRoute.egressMode);
		travelTimes.add(accessTravelTime + flyTime + egressTravelTime);
		travelTimes.add(accessTravelTime);
		travelTimes.add(flyTime);
		travelTimes.add(egressTravelTime);

		return travelTimes;
	}

}
