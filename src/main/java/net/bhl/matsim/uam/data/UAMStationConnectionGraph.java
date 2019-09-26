package net.bhl.matsim.uam.data;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.scenario.RunCreateUAMScenario;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.Id;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UAMStationConnectionGraph {

    private Map<Id<UAMStation>, Map<Id<UAMStation>, Double>> utilities;
    private Map<Id<UAMStation>, Map<Id<UAMStation>, Double>> travelTimes;
    private Map<Id<UAMStation>, Map<Id<UAMStation>, Double>> distances;
    private Map<Id<UAMStation>, Map<Id<UAMStation>, List<Link>>> links;

    // TODO this class should store the distance each pair of stations for each vehicle, e.g.:
    //private Map<Map<Id<UAMStation>, Id<UAMStation>>, Map<Id<UAMVehicle>, Double>> travelTimes;

    final private static Logger log = Logger.getLogger(UAMStationConnectionGraph.class);

    public UAMStationConnectionGraph(UAMManager uamManager, CustomModeChoiceParameters parameters,
                                     @Named("uam") ParallelLeastCostPathCalculator plcpc) {
        log.info("Calculating travel times and distances between all UAM stations.");

        utilities = new HashMap<>();
        travelTimes = new HashMap<>();
        distances = new HashMap<>();
        links = new HashMap<>();

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

                    if (!link.getId().toString().startsWith(RunCreateUAMScenario.name_uam_horizontal_link))
                        travelTime += link.getLength() / verticalSpeed;
                    else
                        travelTime += link.getLength() / horizontalSpeed;
                }

                if (parameters != null)
                    utility = estimateUtilityUAM(travelTime, distance, parameters, uamStationOrigin, uamStationDestination);

                if (travelTimes.containsKey(uamStationOrigin.getId())) {
                    this.utilities.get(uamStationOrigin.getId()).put(uamStationDestination.getId(), utility);
                    this.travelTimes.get(uamStationOrigin.getId()).put(uamStationDestination.getId(), travelTime);
                    this.distances.get(uamStationOrigin.getId()).put(uamStationDestination.getId(), distance);
                    this.links.get(uamStationOrigin.getId()).put(uamStationDestination.getId(), links);
                } else {
                    Map<Id<UAMStation>, Double> newEntry = new HashMap<>();
                    newEntry.put(uamStationDestination.getId(), utility);
                    this.utilities.put(uamStationOrigin.getId(), newEntry);

                    Map<Id<UAMStation>, Double> newEntryTT = new HashMap<>();
                    newEntryTT.put(uamStationDestination.getId(), travelTime);
                    this.travelTimes.put(uamStationOrigin.getId(), newEntryTT);

                    Map<Id<UAMStation>, Double> newEntryD = new HashMap<>();
                    newEntryD.put(uamStationDestination.getId(), distance);
                    this.distances.put(uamStationOrigin.getId(), newEntryD);

                    Map<Id<UAMStation>, List<Link>> newEntryL = new HashMap<>();
                    newEntryL.put(uamStationDestination.getId(), links);
                    this.links.put(uamStationOrigin.getId(), newEntryL);
                }
            }
        }
    }

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

    public double getUtility(Id<UAMStation> originStation, Id<UAMStation> destinationStation) {
        return this.utilities.get(originStation).get(destinationStation);
    }

    public double getTravelTime(Id<UAMStation> originStation, Id<UAMStation> destinationStation) {
        return this.travelTimes.get(originStation).get(destinationStation);
    }

    public double getDistance(Id<UAMStation> originStation, Id<UAMStation> destinationStation) {
        return this.distances.get(originStation).get(destinationStation);
    }

}
