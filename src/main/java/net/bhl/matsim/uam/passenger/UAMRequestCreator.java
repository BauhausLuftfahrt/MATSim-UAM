package net.bhl.matsim.uam.passenger;

import com.google.inject.Inject;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.dispatcher.UAMDispatcher;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

import java.util.List;

/**
 * This class creates a UAM request.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMRequestCreator implements PassengerRequestCreator {

    @Inject
    List<UAMDispatcher> dispatchers;

    @Inject
    private UAMStationConnectionGraph stationConnectionutilities;

    @Inject
    private UAMManager uamManager;

    @Override
    public PassengerRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger, Link fromLink, Link toLink,
                                          double departureTime, double submissionTime) {

        // We currently have only one dispatcher, this might change in the future
        double distance = stationConnectionutilities.getFlightLeg(
                uamManager.getStations().getNearestUAMStation(fromLink).getId(),
                uamManager.getStations().getNearestUAMStation(toLink).getId()).distance;
        return new UAMRequest(id, passenger, fromLink, toLink, departureTime, submissionTime, dispatchers.get(0), distance);
    }

}
