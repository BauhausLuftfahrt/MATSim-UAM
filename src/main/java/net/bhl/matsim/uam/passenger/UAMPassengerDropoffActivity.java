package net.bhl.matsim.uam.passenger;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;

import java.util.Set;

/**
 * This class defines the drop off activity for the passenger and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMPassengerDropoffActivity implements DynActivity {
	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<? extends PassengerRequest> requests;
	private final String activityType;

	private double endTime;

	public UAMPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, StayTask dropoffTask,
									   Set<? extends PassengerRequest> requests, double dropoffDuration, String activityType) {
		this.activityType = activityType;
		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.requests = requests;

		if (requests.size() > vehicle.getCapacity()) {
			// Number of requests exceeds number of seats
			throw new IllegalStateException();
		}

		endTime = dropoffTask.getBeginTime() + dropoffDuration;
	}

	@Override
	public void finalizeAction(double now) {
		for (PassengerRequest request : requests) {
			passengerEngine.dropOffPassengers(driver, request.getId(), now);
		}
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

	@Override
	public void doSimStep(double now) {
	}
}
