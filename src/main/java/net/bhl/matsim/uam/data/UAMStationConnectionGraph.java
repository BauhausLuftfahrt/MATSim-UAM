package net.bhl.matsim.uam.data;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.router.UAMFlightSegments;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UAMStationConnectionGraph {

    private Map<Id<UAMStation>, Map<Id<UAMStation>, UAMFlightLeg>> legs;

    final private static Logger log = Logger.getLogger(UAMStationConnectionGraph.class);

    public UAMStationConnectionGraph(UAMManager uamManager, CustomModeChoiceParameters parameters,
                                     @Named("uam") ParallelLeastCostPathCalculator plcpc) {
        log.info("Calculating travel times and distances between all UAM stations.");

        legs = new HashMap<>();

        //TODO for now it assumes there only being one singular UAM vehicle type, enhancing this would be part of future work
        double horizontalSpeed = uamManager.getVehicles().entrySet().iterator().next().getValue().getCruiseSpeed();
        double verticalSpeed = uamManager.getVehicles().entrySet().iterator().next().getValue().getVerticalSpeed();

        for (UAMStation uamStationOrigin : uamManager.getStations().stations.values()) {
            for (UAMStation uamStationDestination : uamManager.getStations().stations.values()) {
                if (uamStationOrigin == uamStationDestination)
                    continue;

                // TODO when reworking the router, rework this as well!
                Future<Path> path = plcpc.calcLeastCostPath(uamStationOrigin.getLocationLink().getFromNode(),
                        uamStationDestination.getLocationLink().getToNode(), 0.0, null, null);

                double distance = 0;
                double travelTime = 0;
                double utility = 0;
                List<Link> links = null;

                try {
                    links = path.get().links;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                for (Link link : links) {
                    distance += link.getLength();

                    String flightSegment = (String) link.getAttributes().getAttribute(UAMFlightSegments.ATTRIBUTE);

                    if (flightSegment.equals(UAMFlightSegments.HORIZONTAL))
                        travelTime += link.getLength() / horizontalSpeed;

                    if (flightSegment.equals(UAMFlightSegments.VERTICAL))
                        travelTime += link.getLength() / verticalSpeed;
                }

                if (parameters != null)
                    utility = estimateUtilityUAM(travelTime, distance, parameters, uamStationOrigin, uamStationDestination);

                if (legs.containsKey(uamStationOrigin.getId())) {
                    this.legs.get(uamStationOrigin.getId()).put(uamStationDestination.getId(),
                            new UAMFlightLeg(travelTime, distance, utility, links));
                } else {
                    Map<Id<UAMStation>, UAMFlightLeg> newEntry = new HashMap<>();
                    newEntry.put(uamStationDestination.getId(), new UAMFlightLeg(travelTime, distance, utility, links));
                    this.legs.put(uamStationOrigin.getId(), newEntry);
                }
            }
        }
    }

    public UAMFlightLeg getFlightLeg(Id<UAMStation> originStation, Id<UAMStation> destinationStation) {
        return this.legs.get(originStation).get(destinationStation);
    }

    // TODO remove all utility from this class
    private double estimateUtilityUAM(double travelTime, double distance, CustomModeChoiceParameters parameters,
                                      UAMStation stationOrigin, UAMStation stationDestination) {

        double utility = 0.0;

        utility += parameters.alphaUAM;
        utility += parameters.betaTravelTimeUAM_min * travelTime / 60.0;
        utility += parameters.betaWaitUAM_min *
                (stationOrigin.getPreFlightTime() + stationDestination.getPostFlightTime()) / 60.0;
        // utility += parameters.betaCost(distance * 1e-3, parameters.averageIncome) *
        // parameters.distanceCostUAM_km * distance * 1e-3;

        return utility;
    }
}
