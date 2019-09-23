package net.bhl.matsim.uam.router.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.facilities.Facility;

import net.bhl.matsim.uam.data.UAMAccessRouteData;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.infrastructure.UAMStation;

/**
 * This strategy is used to assign to the passenger a UAMRoute based on the
 * minimum travel distance of the route.
 * 
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
 */
public class UAMMinDistanceStrategy implements UAMStrategy {
	private UAMStrategyUtils strategyUtils;

	public UAMMinDistanceStrategy(UAMStrategyUtils strategyUtils) {
		this.strategyUtils = strategyUtils;
	}

	@Override
	public UAMStrategyType getUAMStrategyType() {
		return UAMStrategyType.MINDISTANCE;
	}

	@Override
	public UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime) {
		UAMStation bestStationOrigin = null, bestStationDestination = null;
		Collection<UAMStation> stationsOrigin = strategyUtils.getPossibleStations(fromFacility);
		Collection<UAMStation> stationsDestination = strategyUtils.getPossibleStations(toFacility);
		Map<Id<UAMStation>, UAMAccessRouteData> accessRoutesData = new HashMap<>();
		accessRoutesData = strategyUtils.getAccessRouteData(true, stationsOrigin, fromFacility, departureTime);
		String bestModeEgress = TransportMode.walk;

		double timeOfEgress = departureTime;
		Set<String> modes = new HashSet<>();
		modes = strategyUtils.getModes();
		// UAM Flight Distance + access and egress distance
		double minTotalDistance = Double.POSITIVE_INFINITY;
		for (UAMStation stationOrigin : stationsOrigin) {
			for (UAMStation stationDestination : stationsDestination) {
				if (stationOrigin == stationDestination)
					continue;
				// collects the distance between stations and stores it
				Future<Path> uavPath = strategyUtils.getFlyDistance(stationOrigin.getLocationLink().getFromNode(),
						stationDestination.getLocationLink().getToNode());
				double flyDistance = 0;
				try {
					for (Link link : uavPath.get().links) {
						flyDistance += link.getLength();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				// fly time between stations
				double flyTime = strategyUtils.getFlyTime(stationOrigin, stationDestination);
				double accessTime = strategyUtils.estimateTime(true, fromFacility, departureTime, stationOrigin,
						accessRoutesData.get(stationOrigin.getId()).getAccessModeDistance());
				// updates departureTime
				double currentDepartureTime = departureTime + accessTime + flyTime;
				// Calculates the shortest path
				for (String mode : modes) {
					// Calculates the distance for the egress routes using updated departureTime
					double egressDistance = strategyUtils.estimateDistance(false, toFacility, currentDepartureTime,
							stationDestination, mode);
					double totalDistance = accessRoutesData.get(stationOrigin.getId()).getDistance() + flyDistance
							+ egressDistance;
					if (totalDistance < minTotalDistance) {
						minTotalDistance = totalDistance;
						bestStationOrigin = stationOrigin;
						bestStationDestination = stationDestination;
						bestModeEgress = mode;
						timeOfEgress = currentDepartureTime;
					}
				}
			}
		}
		double egressDistance = strategyUtils.estimateDistance(false, toFacility, timeOfEgress, bestStationDestination,
				bestModeEgress);
		// if the access/egress distance is less than walkDistance, then walk will be
		// the uam access and egress mode, otherwise time is used to select the mode
		// access/egress
		String bestModeAccess = strategyUtils.checkStationAccessDistance(true,
				accessRoutesData.get(bestStationOrigin.getId()).getAccessModeDistance(), bestStationOrigin, null,
				accessRoutesData.get(bestStationOrigin.getId()).getDistance(), fromFacility, toFacility, departureTime,
				null, true);
		bestModeEgress = strategyUtils.checkStationAccessDistance(false, bestModeEgress, bestStationDestination,
				bestStationOrigin, egressDistance, toFacility, fromFacility, departureTime, bestModeAccess, true);

		return new UAMRoute(bestModeAccess, bestStationOrigin, bestStationDestination, bestModeEgress);
	}

}
