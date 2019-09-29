package net.bhl.matsim.uam.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTaskImpl;

import net.bhl.matsim.uam.passenger.UAMRequest;

/**
 * During this task the UAM vehicle is flying.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 * 
 */
public class UAMFlyTask extends DriveTaskImpl implements UAMTask {
	private final Set<UAMRequest> requests = new HashSet<>();

	public UAMFlyTask(VrpPathWithTravelData path) {
		super(path);
	}

	public UAMFlyTask(VrpPathWithTravelData path, Collection<UAMRequest> requests) {
		this(path);
		this.requests.addAll(requests);
	}

	@Override
	public UAMTaskType getUAMTaskType() {
		return UAMTaskType.FLY;
	}

	@Override
	public Collection<UAMRequest> getRequests() {
		return this.requests;
	}

	@Override
	public void addRequest(UAMRequest request) {
		requests.add(request);
	}
}
