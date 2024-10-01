package net.bhl.matsim.uam.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.DefaultStayTask;

import net.bhl.matsim.uam.passenger.UAMRequest;

/**
 * This task represents the local pick up of passenger at stations, it doesn't
 * include the flying period.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMPickupTask extends DefaultStayTask {
	private final Set<PassengerRequest> requests = new HashSet<>();
	private final double boardingTime;

	public UAMPickupTask(double beginTime, double endTime, Link link, double boardingTime) {
		super(UAMTaskType.PICKUP, beginTime, endTime, link);
		this.boardingTime = boardingTime;
	}

	public UAMPickupTask(double beginTime, double endTime, Link link, double boardingTime,
						 Collection<UAMRequest> requests) {
		this(beginTime, endTime, link, boardingTime);

		this.requests.addAll(requests);
		for (UAMRequest request : requests)
			request.setPickupTask(this);
	}

	public Set<PassengerRequest> getRequests() {
		return this.requests;
	}

	@Deprecated
	public void addRequest(UAMRequest request) {
		requests.add(request);
		request.setPickupTask(this);

	}

	public double getBoardingTime() {
		return boardingTime;
	}
}
