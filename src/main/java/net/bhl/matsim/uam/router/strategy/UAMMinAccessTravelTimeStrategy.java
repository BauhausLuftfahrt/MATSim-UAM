package net.bhl.matsim.uam.router.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.facilities.Facility;

import net.bhl.matsim.uam.data.UAMAccessRouteData;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.infrastructure.UAMStation;

/**
 * This strategy is used to assign to the passenger the UAMRoute based on the
 * minimum travel time to access to UAM Station and egress travel time from UAM
 * Station.
 * 
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
 */
public class UAMMinAccessTravelTimeStrategy implements UAMStrategy {
	private UAMStrategyUtils strategyUtils;

	public UAMMinAccessTravelTimeStrategy(UAMStrategyUtils strategyUtils) {
		this.strategyUtils = strategyUtils;
	}

	@Override
	public UAMStrategyType getUAMStrategyType() {
		return UAMStrategyType.MINACCESSTRAVELTIME;
	}

	@Override
	public UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime) {
		UAMStation bestStationOrigin = null, bestStationDestination = null;
		Collection<UAMStation> stationsOrigin = strategyUtils.getPossibleStations(fromFacility);
		Collection<UAMStation> stationsDestination = strategyUtils.getPossibleStations(toFacility);
		Map<Id<UAMStation>, UAMAccessRouteData> accessRoutesData = new HashMap<>();
		accessRoutesData = strategyUtils.getAccessRouteData(true, stationsOrigin, fromFacility, departureTime);
		double minAccessTime = Double.POSITIVE_INFINITY;

		for (UAMStation stationOrigin : stationsOrigin) {
			if (accessRoutesData.get(stationOrigin.getId()).getAccessTravelTime() < minAccessTime) {
				bestStationOrigin = stationOrigin;
				minAccessTime = accessRoutesData.get(stationOrigin.getId()).getAccessTravelTime();
			}
		}
		// egress trips
		double timeOfEgress = departureTime;
		Set<String> modes = new HashSet<>();
		modes = strategyUtils.getModes();
		double minEgressTime = Double.POSITIVE_INFINITY;
		String bestModeEgress = TransportMode.walk;
		for (UAMStation stationDestination : stationsDestination) {
			if (bestStationOrigin == stationDestination)
				continue;
			// fly time between stations
			double flyTime = strategyUtils.getFlyTime(bestStationOrigin, stationDestination);
			// updates departureTime
			double currentDepartureTime = departureTime
					+ accessRoutesData.get(bestStationOrigin.getId()).getAccessTravelTime() + flyTime;
			for (String mode : modes) {
				double egressTravelTime = strategyUtils.estimateTime(false, toFacility, currentDepartureTime,
						stationDestination, mode);
				if (egressTravelTime < minEgressTime) {
					bestStationDestination = stationDestination;
					minEgressTime = egressTravelTime;
					bestModeEgress = mode;
					timeOfEgress = currentDepartureTime;
				}
			}
		}

		double egressDistance = strategyUtils.estimateDistance(false, toFacility, timeOfEgress, bestStationDestination,
				bestModeEgress);
		String bestModeAccess = strategyUtils.checkStationAccessDistance(true,
				accessRoutesData.get(bestStationOrigin.getId()).getAccessModeTime(), bestStationOrigin, null,
				accessRoutesData.get(bestStationOrigin.getId()).getDistance(), fromFacility, toFacility, departureTime,
				null, false);
		bestModeEgress = strategyUtils.checkStationAccessDistance(false, bestModeEgress, bestStationDestination,
				bestStationOrigin, egressDistance, toFacility, fromFacility, departureTime, bestModeAccess, false);

		return new UAMRoute(bestModeAccess, bestStationOrigin, bestStationDestination, bestModeEgress);
	}

}
