package net.bhl.matsim.uam.passenger;

import com.google.inject.Inject;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.dispatcher.Dispatcher;
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
	List<Dispatcher> dispatchers;

	@Override
	public PassengerRequest createRequest(Id<Request> id, MobsimPassengerAgent passenger, Link fromLink, Link toLink,
										  double departureTime, double submissionTime) {

		// We currently have only one dispatcher, this might change in the future
		// TODO ADD DISTANCE
		return new UAMRequest(id, passenger, fromLink, toLink, departureTime, submissionTime, dispatchers.get(0), 0);
	}

}
