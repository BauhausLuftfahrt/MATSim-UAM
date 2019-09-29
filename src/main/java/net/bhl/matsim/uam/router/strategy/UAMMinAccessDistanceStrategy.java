package net.bhl.matsim.uam.router.strategy;

import net.bhl.matsim.uam.data.UAMAccessRouteData;
import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.facilities.Facility;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This strategy is used to assign to the passenger the UAMRoute based on the minimum travel distance of access to UAM Station and egress from UAM Station.
 * 
 * @author Aitan Militao
 */
public class UAMMinAccessDistanceStrategy implements UAMStrategy{
	private UAMStrategyUtils strategyUtils;
	public UAMMinAccessDistanceStrategy(UAMStrategyUtils strategyUtils) {
		this.strategyUtils = strategyUtils;
	}

	@Override
	public UAMStrategyType getUAMStrategyType() {
		return UAMStrategyType.MINACCESSDISTANCE;
	}

	@Override
	public UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime) {
		UAMStation bestStationOrigin = null, bestStationDestination = null;
		Collection<UAMStation> stationsOrigin = strategyUtils.getPossibleStations(fromFacility);
		Collection<UAMStation> stationsDestination = strategyUtils.getPossibleStations(toFacility);
		Map<Id<UAMStation>, UAMAccessRouteData> accessRoutesData = strategyUtils.getAccessRouteData(true,
				stationsOrigin, fromFacility, departureTime);
		//access trips
		double minAccessDistance = Double.POSITIVE_INFINITY;
		for (UAMStation stationOrigin : stationsOrigin) {
			if (accessRoutesData.get(stationOrigin.getId()).getDistance() < minAccessDistance) {
				bestStationOrigin = stationOrigin;
				minAccessDistance = accessRoutesData.get(stationOrigin.getId()).getDistance();
			}
		}
		//egress trips
		String bestModeEgress = TransportMode.walk;
		Set<String> modes = strategyUtils.getModes();
		double minEgressDistance = Double.POSITIVE_INFINITY;
		for (UAMStation stationDestination : stationsDestination) {		
			if (bestStationOrigin == stationDestination)
				continue;
			//fly time between stations
			double flyTime = strategyUtils.getFlyTime(bestStationOrigin, stationDestination);
			//updates departureTime 
			double currentDepartureTime = departureTime + accessRoutesData.get(bestStationOrigin.getId()).getAccessTravelTime()+flyTime;
			for (String mode : modes) {
				//Calculates the distance for the egress routes using updated departureTime
				double egressDistance = strategyUtils.estimateDistance(false, toFacility, currentDepartureTime, stationDestination, mode);
				if (egressDistance < minEgressDistance) {
					bestStationDestination = stationDestination;
					minEgressDistance = egressDistance;
					bestModeEgress = mode;
				}
			}
		}				

		return new UAMRoute(accessRoutesData.get(bestStationOrigin.getId()).getAccessModeDistance(), bestStationOrigin,
				bestStationDestination, bestModeEgress);
	}
		
}
