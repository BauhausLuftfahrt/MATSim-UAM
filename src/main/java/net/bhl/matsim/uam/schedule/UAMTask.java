package net.bhl.matsim.uam.schedule;

import java.util.Collection;

import org.matsim.contrib.dvrp.schedule.Task;

import net.bhl.matsim.uam.passenger.UAMRequest;

public interface UAMTask extends Task {
	static enum UAMTaskType {
		PICKUP, DROPOFF, FLY, STAY, PARK, TURNAROUND 
	}
	
	UAMTaskType getUAMTaskType();
	Collection<UAMRequest> getRequests();
	void addRequest(UAMRequest request);

}
