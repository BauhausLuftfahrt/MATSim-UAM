package net.bhl.matsim.uam.schedule;

import java.util.Collection;

import org.matsim.contrib.dvrp.schedule.Task;

import net.bhl.matsim.uam.passenger.UAMRequest;

/**
 * Defines the classes responsible for a given UAM vehicle task.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 * 
 */
public interface UAMTask extends Task {
	static enum UAMTaskType {
		PICKUP, DROPOFF, FLY, STAY, PARK, TURNAROUND
	}

	UAMTaskType getUAMTaskType();

	Collection<UAMRequest> getRequests();

	void addRequest(UAMRequest request);

}
