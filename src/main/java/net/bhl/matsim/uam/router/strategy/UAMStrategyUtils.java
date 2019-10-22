package net.bhl.matsim.uam.router.strategy;

import ch.ethz.matsim.av.plcpc.DefaultParallelLeastCostPathCalculator;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptor;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMAccessLeg;
import net.bhl.matsim.uam.data.UAMAccessOptions;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
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

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class provides the methods used for different UAMStrategies.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class UAMStrategyUtils {
	private static final Logger log = Logger.getLogger(UAMStrategyUtils.class);
	private final Scenario scenario;
	private UAMStations landingStations;
	private UAMConfigGroup uamConfig;
	private UAMStationConnectionGraph stationConnections;
	private Network carNetwork;
	private TransitRouter transitRouter;
	private ParallelLeastCostPathCalculator plcpc;
	private LeastCostPathCalculator plcpccar;
	private ParallelLeastCostPathCalculator plcpccar2;
	private CustomModeChoiceParameters parameters;
	private boolean parallel = false;

	public UAMStrategyUtils(UAMStations landingStations, UAMConfigGroup uamConfig, Scenario scenario,
							UAMStationConnectionGraph stationConnections, Network carNetwork, TransitRouter transitRouter,
							ParallelLeastCostPathCalculator plcpc, LeastCostPathCalculator plcpccar,
							CustomModeChoiceParameters parameters) {
		this.landingStations = landingStations;
		this.uamConfig = uamConfig;
		this.scenario = scenario;
		this.stationConnections = stationConnections;
		this.carNetwork = carNetwork;
		this.transitRouter = transitRouter;
		this.plcpc = plcpc;
		this.plcpccar = plcpccar;
		this.parameters = parameters;
	}

	public UAMStrategyUtils(UAMStations landingStations, UAMConfigGroup uamConfig, Scenario scenario,
			UAMStationConnectionGraph stationConnections, Network carNetwork, TransitRouter transitRouter,
			ParallelLeastCostPathCalculator plcpc, ParallelLeastCostPathCalculator plcpccar,
			CustomModeChoiceParameters parameters) {
		this.landingStations = landingStations;
		this.uamConfig = uamConfig;
		this.scenario = scenario;
		this.stationConnections = stationConnections;
		this.carNetwork = carNetwork;
		this.transitRouter = transitRouter;
		this.plcpc = plcpc;
		this.plcpccar2 = plcpccar;
		this.parameters = parameters;
		this.parallel = true;
	}

	/**
	 * @return the list of possible UAMStations accessible from/to a specific
	 * location (facility) based in the searchRadius parameter defined in
	 * the config file.
	 */
	Collection<UAMStation> getPossibleStations(Facility<?> facility) {
		return landingStations.spatialStations.getDisk(facility.getCoord().getX(), facility.getCoord().getY(),
				uamConfig.getSearchRadius());
	}

	/**
	 * @param access   true if access leg, false for egress legs.
	 * @param stations list of possible stations to be checked
	 * @param facility the origin of the trip (if access leg); the trip final
	 *                 destination (if egress leg).
	 * @return a map with station Id's as keys and UAMAccessRouteData as values.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	Map<Id<UAMStation>, UAMAccessOptions> getAccessOptions(boolean access, Collection<UAMStation> stations,
														   Facility<?> facility, double departureTime) throws InterruptedException, ExecutionException {
		Set<String> modes = new HashSet<>(uamConfig.getAvailableAccessModes());
		Map<Id<UAMStation>, UAMAccessOptions> accessRouteData = new HashMap<>();
		String bestModeDistance = TransportMode.walk;
		String bestModeTime = TransportMode.walk;

		for (UAMStation station : stations) {
			double minDistance = Double.POSITIVE_INFINITY;
			double minAccessTime = Double.POSITIVE_INFINITY;

			for (String mode : modes) {
				UAMAccessLeg uamAccessLeg = estimateAccessLeg(access, facility, departureTime, station, mode);
				double distance = uamAccessLeg.distance;
				double travelTime = uamAccessLeg.travelTime;

				boolean walkingFilter = distance > uamConfig.getWalkDistance() || mode.equals(TransportMode.walk);
				if (distance < minDistance && walkingFilter) {
					minDistance = distance;
					bestModeDistance = mode;
				}
				if (travelTime < minAccessTime && walkingFilter) {
					minAccessTime = travelTime;
					bestModeTime = mode;
				}
			}

			accessRouteData.put(station.getId(), new UAMAccessOptions(minDistance, minAccessTime,
					bestModeDistance, bestModeTime, station));
		}
		return accessRouteData;
	}

	UAMAccessLeg estimateAccessLeg(boolean access, Facility<?> facility, double time, UAMStation station, String mode) throws InterruptedException, ExecutionException {
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

				if (parallel) {
					Future<Path> path = plcpccar2.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, null, null);
					
					double distanceByCar = 0.0;
					for (Link l : path.get().links) {
						distanceByCar += l.getLength();
					}
					return new UAMAccessLeg(path.get().travelTime, distanceByCar, path.get().links);
				} else {
					Path path = plcpccar.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, null, null);
				
					double distanceByCar = 0.0;
					for (Link l : path.links) {
						distanceByCar += l.getLength();
					}
					return new UAMAccessLeg(path.travelTime, distanceByCar, path.links);
				}
				
				
		
			case TransportMode.pt:
				if (uamConfig.getPtSimulation()) {
					List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to),
							time, null);
					double distanceByPt = 0.0;
					double timeByPt = 0.0;
					for (Leg leg : legs) {
						timeByPt += leg.getTravelTime();
						distanceByPt += leg.getRoute().getDistance();
					}
					return new UAMAccessLeg(timeByPt, distanceByPt, null);
				}
			default:
				return getBeelineAccessLeg(facility, station, mode);
		}
	}

	private UAMAccessLeg getBeelineAccessLeg(Facility<?> facility, UAMStation station, String mode) {
		double distanceFactor = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
				.getBeelineDistanceFactors().get(mode);
		double distance = CoordUtils.calcEuclideanDistance(
				scenario.getNetwork().getLinks().get(facility.getLinkId()).getCoord(),
				station.getLocationLink().getCoord());
		double speed = ((PlansCalcRouteConfigGroup) scenario.getConfig().getModules().get("planscalcroute"))
				.getTeleportedModeSpeeds().get(mode);

		return new UAMAccessLeg(distance * distanceFactor / speed,
				distance * distanceFactor, null);
	}

	double estimateUtilityWrapper(Person person, boolean access, Facility<?> facility, double time, UAMStation station,
								  String mode) throws InterruptedException, ExecutionException {
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

				if (parallel) {
					Future<Path> path = plcpccar2.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, person, null);
					
					
					double distance = 0.0;
					for (Link l : path.get().links) {
						distance += l.getLength();
					}
					return estimateUtility(mode, path.get().travelTime, distance, person);
				} else {
					Path path = plcpccar.calcLeastCostPath(from.getFromNode(), to.getToNode(), time, person, null);
					
					
					double distance = 0.0;
					for (Link l : path.links) {
						distance += l.getLength();
					}
					return estimateUtility(mode, path.travelTime, distance, person);
				}

			case TransportMode.pt:
				if (uamConfig.getPtSimulation()) {
					List<Leg> legs = transitRouter.calcRoute(new LinkWrapperFacility(from), new LinkWrapperFacility(to),
							time, person);
					if (legs.size() > 1)
						return estimateUtility(legs, person);
					return Double.NEGATIVE_INFINITY;
				}
			default:
				UAMAccessLeg beelineAccessLeg = getBeelineAccessLeg(facility, station, mode);
				return estimateUtility(mode, beelineAccessLeg.travelTime, beelineAccessLeg.distance, person);
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

	private double estimateUtility(List<Leg> legs, Person person) {
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

	Network getNetwork() {
		return scenario.getNetwork();
	}

	Set<String> getModes() {
		return uamConfig.getAvailableAccessModes();
	}

	public CustomModeChoiceParameters getParameters() {
		return parameters;
	}

	UAMStationConnectionGraph getStationConnections() {
		return stationConnections;
	}

	public double getAccessTime(Facility<?> fromFacility, Double time, UAMStation departureStation, String mode) throws InterruptedException, ExecutionException {
		return estimateAccessLeg(true, fromFacility, time, departureStation, mode).travelTime;
	}

	public double getFlightTime(UAMStation originStation, UAMStation destinationStation) {
		return stationConnections.getFlightLeg(originStation.getId(), destinationStation.getId()).travelTime;
	}

	public double getFlightDistance(UAMStation originStation, UAMStation destinationStation) {
		return stationConnections.getFlightLeg(originStation.getId(), destinationStation.getId()).distance;
	}

	public double getEgressTime(Facility<?> toFacility, Double time, UAMStation destinationStation, String mode) throws InterruptedException, ExecutionException {
		return estimateAccessLeg(false, toFacility, time, destinationStation, mode).travelTime;
	}
}
