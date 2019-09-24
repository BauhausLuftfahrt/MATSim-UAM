package net.bhl.matsim.uam.router.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.Facility;

import net.bhl.matsim.uam.data.UAMRoute;
import net.bhl.matsim.uam.events.UAMUtilitiesAccessEgress;
import net.bhl.matsim.uam.events.UAMUtilitiesData;
import net.bhl.matsim.uam.events.UAMUtilitiesTrip;
import net.bhl.matsim.uam.infrastructure.UAMStation;;

/**
 * This strategy is used to assign to the passenger the UAMRoute based on the
 * maximum total utility of the route.
 * 
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
 */
public class UAMMaxUtilityStrategy implements UAMStrategy{
	private UAMStrategyUtils strategyUtils;

	public UAMMaxUtilityStrategy(UAMStrategyUtils strategyUtils) {
		this.strategyUtils = strategyUtils;
	}

	@Override
	public UAMStrategyType getUAMStrategyType() {
		return UAMStrategyType.MAXUTILITY;
	}

	@Override
	public UAMRoute getRoute(Person person, Facility<?> fromFacility, Facility<?> toFacility, double departureTime) {
		Network network = strategyUtils.getNetwork();
		Set<String> modes = new HashSet<>();
		modes = strategyUtils.getModes(); 
		Collection<UAMStation> stationsOrigin = strategyUtils.getPossibleStations(fromFacility);
		Collection<UAMStation> stationsDestination = strategyUtils.getPossibleStations(toFacility);

		Map<Id<UAMStation>, Double> accessUtilityMap = new HashMap<>();
		Map<Id<UAMStation>, String> accessModeMap = new HashMap<>();
		for (UAMStation stationOrigin : stationsOrigin) {
			double bestUtility = Double.NEGATIVE_INFINITY;
			String bestMode = TransportMode.walk;

			for (String mode : modes) { //this can be turned into a method in the strategyUtils
				double utility = strategyUtils.estimateUtilityWrapper(person, true, fromFacility, departureTime, stationOrigin, mode);
				if (utility > bestUtility) {
					bestUtility = utility;
					bestMode = mode;
				}

				if (strategyUtils.getParameters().storeUAMUtilities != 0) {
					UAMUtilitiesData.accessEgressOptions.add(new UAMUtilitiesAccessEgress(person.getId(),
							stationOrigin.getId(), network.getLinks().get(fromFacility.getLinkId()).getId(), true, utility, mode, departureTime));
				}
			}
			accessUtilityMap.put(stationOrigin.getId(), bestUtility);
			accessModeMap.put(stationOrigin.getId(), bestMode);
		}		
		
		Map<UAMStationPairs, Double> stationPairsUtility = new HashMap<>();
		for (UAMStation stationOrigin : stationsOrigin) {
			for (UAMStation stationDestination : stationsDestination) {	
				if (stationOrigin == stationDestination)
					continue;			
				UAMStationPairs stationPair = new UAMStationPairs(stationOrigin, stationDestination);
				// access
				double accessUtility = accessUtilityMap.get(stationOrigin.getId());
				// uam wait
				// NO WAITING TIME SINCE WAITING TIME DATA HAS NEVER BEEN ESTAVLISHED WITHOUT
				// MODE CHOICE
				double uamWaitTime = stationOrigin.getDefaultWaitTime();  //switching to input file parameters parameters.defaultWaitTime_sec
				double uamWaitUtility = (uamWaitTime * strategyUtils.getParameters().betaWaitUAM_min / 60.0);
				stationPair.uamWaitUtility = uamWaitUtility;
				// uam flight
				double uamFlightUtility = strategyUtils.getStationConnectionutilities().getUtility(stationOrigin.getId(),
						stationDestination.getId());
				stationPair.uamFlightUtility = uamFlightUtility;
				// uam flight income
				double income;
				if (person.getAttributes().getAttribute("income") != null) {
					income = (double) person.getAttributes().getAttribute("income");
				}
				else {
					income = strategyUtils.getParameters().averageIncome;
				}				
				double distance = CoordUtils.calcEuclideanDistance(stationDestination.getLocationLink().getCoord(),
						stationOrigin.getLocationLink().getCoord());
				double uamIncomeUtility = strategyUtils.getParameters().betaCost(distance * 1e-3, income) * strategyUtils.getParameters().distanceCostUAM_km
						* distance * 1e-3;
				stationPair.uamIncomeUtility = uamIncomeUtility;								
				//fly time between stations
				double flyTime = strategyUtils.getFlyTime(stationOrigin, stationDestination);				
				double utilityBeforeEgress = accessUtility + uamWaitUtility + uamFlightUtility + uamIncomeUtility;
				double timeToAccess = strategyUtils.estimateTime(true, fromFacility, departureTime, stationOrigin, accessModeMap.get(stationOrigin.getId()));
				//time of egress
				double currentTimeBeforeEgress = timeToAccess + flyTime;
				stationPair.currentTimeBeforeEgress = currentTimeBeforeEgress;							
				stationPairsUtility.put(stationPair, utilityBeforeEgress);
			}
		}

		double maxUtility = Double.NEGATIVE_INFINITY;
		UAMStation bestStationOrigin = null, bestStationDestination = null;
		String bestModeEgress = TransportMode.walk;
		double timeOfEgress = departureTime;
		for (UAMStationPairs uamStationPair : stationPairsUtility.keySet()) {
			// data setup (optional)
			UAMUtilitiesTrip utilities = new UAMUtilitiesTrip();
			utilities.person = person.getId();
			utilities.originStation = uamStationPair.stationOrigin.getId();
			utilities.destinationStation = uamStationPair.stationDestination.getId();
			utilities.accessMode = accessModeMap.get(uamStationPair.stationOrigin.getId());
			utilities.originLink = network.getLinks().get(fromFacility.getLinkId()).getId();
			utilities.destinationLink = network.getLinks().get(toFacility.getLinkId()).getId();
			utilities.accessUtility = accessUtilityMap.get(uamStationPair.stationOrigin.getId());
			utilities.uamWaitUtility = uamStationPair.uamWaitUtility;
			utilities.uamFlightUtility = uamStationPair.uamFlightUtility;
			utilities.uamIncomeUtility = uamStationPair.uamIncomeUtility;
			// egress
			double tripUtilityBeforeEgress = stationPairsUtility.get(uamStationPair);		
			double currentDepartureTime = departureTime + uamStationPair.currentTimeBeforeEgress;
			for (String mode : modes) {
				double tripUtility = tripUtilityBeforeEgress;
				double egressUtility = strategyUtils.estimateUtilityWrapper(person, false, toFacility, currentDepartureTime, uamStationPair.stationDestination, mode);					
				utilities.egressUtility = egressUtility;
				tripUtility += egressUtility;
				utilities.totalUtility = tripUtility;
				// save data
				if (strategyUtils.getParameters().storeUAMUtilities != 0) {
					UAMUtilitiesData.tripOptions.add(utilities);
					UAMUtilitiesData.accessEgressOptions.add(new UAMUtilitiesAccessEgress(person.getId(),
							uamStationPair.stationDestination.getId(), network.getLinks().get(fromFacility.getLinkId()).getId(), false, egressUtility, mode, currentDepartureTime));
				}
				if (tripUtility > maxUtility) {
					bestStationOrigin = uamStationPair.stationOrigin;
					bestStationDestination = uamStationPair.stationDestination;
					maxUtility = tripUtility;
					bestModeEgress = mode;
					timeOfEgress = currentDepartureTime;
				}
			}			
		}		
		double accessDistance = strategyUtils.estimateDistance(true, fromFacility, departureTime, bestStationOrigin, accessModeMap.get(bestStationOrigin.getId()));
		double egressDistance = strategyUtils.estimateDistance(false, toFacility, timeOfEgress, bestStationDestination, bestModeEgress);
		//if the access/egress distance is less than walkDistance, then walk will be the uam access and egress mode
		String bestModeAccess = strategyUtils.checkStationAccessDistance(true, accessModeMap.get(bestStationOrigin.getId()), bestStationOrigin, null,
				accessDistance, fromFacility, toFacility, departureTime, null, false);
		bestModeEgress = strategyUtils.checkStationAccessDistance(false, bestModeEgress, bestStationDestination, bestStationOrigin,
				egressDistance, toFacility, fromFacility, departureTime, bestModeAccess, false);

		return new UAMRoute(bestModeAccess, bestStationOrigin, bestStationDestination, bestModeEgress);
	}
		
	class UAMStationPairs {
		UAMStation stationOrigin;
		UAMStation stationDestination;
		double uamWaitUtility;
		double uamFlightUtility;
		double uamIncomeUtility;
		double currentTimeBeforeEgress;
		
		public UAMStationPairs(UAMStation stationOrigin, UAMStation stationDestination) {
			this.stationOrigin = stationOrigin;
			this.stationDestination = stationDestination;
		}
	}

}
