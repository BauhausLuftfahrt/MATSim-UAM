package net.bhl.matsim.uam.schedule;

import net.bhl.matsim.uam.passenger.UAMRequest;
import org.matsim.contrib.dvrp.schedule.Task;

import java.util.Collection;

public interface UAMTask extends Task {
	UAMTaskType getUAMTaskType();

	Collection<UAMRequest> getRequests();

	void addRequest(UAMRequest request);

	enum UAMTaskType {
		PICKUP, DROPOFF, FLY, STAY, PARK, TURNAROUND
	}

}
