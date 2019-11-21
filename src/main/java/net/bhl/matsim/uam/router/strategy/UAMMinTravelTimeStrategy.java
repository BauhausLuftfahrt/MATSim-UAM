package net.bhl.matsim.uam.router.strategy;

import net.bhl.matsim.uam.data.UAMAccessOptions;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.facilities.Facility;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This strategy is used to assign to the passenger a UAMRoute based on the
 * minimum total travel time of the route.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class UAMMinTravelTimeStrategy implements UAMStrategy {
	private UAMStrategyUtils strategyUtils;

	public UAMMinTravelTimeStrategy(UAMStrategyUtils strategyUtils) {
		this.strategyUtils = strategyUtils;
	}

	@Override
	public UAMStrategyType getUAMStrategyType() {
		return UAMStrategyType.MINTRAVELTIME;
	}

	@Override
	public UAMRoute getRoute(Person person, Facility fromFacility, Facility toFacility, double departureTime) {
		UAMStation bestStationOrigin = null, bestStationDestination = null;
		Collection<UAMStation> stationsOrigin = strategyUtils.getPossibleStations(fromFacility);
		Collection<UAMStation> stationsDestination = strategyUtils.getPossibleStations(toFacility);
		Map<Id<UAMStation>, UAMAccessOptions> accessRoutesData = new HashMap<>();

		accessRoutesData = strategyUtils.getAccessOptions(true, stationsOrigin, fromFacility, departureTime);
		// UAM flight time + access and egress travel time
		double minTotalTime = Double.POSITIVE_INFINITY;
		Set<String> modes = strategyUtils.getModes();
		String bestModeEgress = TransportMode.walk;
		for (UAMStation stationOrigin : stationsOrigin) {
			for (UAMStation stationDestination : stationsDestination) {
				if (stationOrigin == stationDestination)
					continue;
				// fly time between stations
				double flyTime = strategyUtils.getFlightTime(stationOrigin, stationDestination);
				// updates departureTime
				double currentDepartureTime = departureTime
						+ accessRoutesData.get(stationOrigin.getId()).getFastestAccessTime() + flyTime;
				for (String mode : modes) {
					// Calculates the time travel for the egress routes
					double egressTravelTime = strategyUtils.estimateAccessLeg(false, toFacility, currentDepartureTime,
							stationDestination, mode).travelTime;
					// Calculates the minimum total time
					double totalTime = accessRoutesData.get(stationOrigin.getId()).getFastestAccessTime() + flyTime
							+ egressTravelTime;
					if (totalTime < minTotalTime) {
						bestStationOrigin = stationOrigin;
						bestStationDestination = stationDestination;
						minTotalTime = totalTime;
						bestModeEgress = mode;
					}
				}
			}
		}

		return new UAMRoute(accessRoutesData.get(bestStationOrigin.getId()).getFastestTimeMode(), bestStationOrigin,
				bestStationDestination, bestModeEgress);
	}
}
