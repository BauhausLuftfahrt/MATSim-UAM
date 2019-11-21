package net.bhl.matsim.uam.passenger;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

import java.util.Set;

/**
 * This class defines the pick up activity for the passenger and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMPassengerPickupActivity implements DynActivity, PassengerPickupActivity {

	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<? extends PassengerRequest> requests;
	private final double pickupDuration;

	private String activityType;
	private double maximumRequestT0 = 0;

	private double endTime = 0.0;
	private int passengersAboard = 0;

	public UAMPassengerPickupActivity(PassengerEngine passengerEngine, DynAgent driver,
									  DvrpVehicle vehicle, StayTask pickupTask, Set<? extends PassengerRequest> requests,
									  double pickupDuration, String activityType) {
		if (requests.size() > vehicle.getCapacity()) {
			// Number of requests exceeds number of seats
			throw new IllegalStateException();
		}

		this.activityType = activityType;

		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.pickupDuration = pickupDuration;
		this.requests = requests;

		endTime = pickupTask.getBeginTime();
		passengersAboard = 0;

		double now = pickupTask.getBeginTime();

		for (PassengerRequest request : requests) {
			if (passengerEngine.pickUpPassenger(this, driver, request, pickupTask.getBeginTime())) {
				passengersAboard++;
			}

			if (request.getEarliestStartTime() > maximumRequestT0) {
				maximumRequestT0 = request.getEarliestStartTime();
			}
		}

		if (passengersAboard == requests.size()) {
			endTime = now + pickupDuration;
		} else {
			setEndTimeIfWaitingForPassengers(now);
		}
	}

	private void setEndTimeIfWaitingForPassengers(double now) {
		endTime = Math.max(now, maximumRequestT0) + pickupDuration;

		if (endTime == now) {
			endTime += 1;
		}
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public void doSimStep(double now) {
		if (passengersAboard < requests.size()) {
			setEndTimeIfWaitingForPassengers(now);
		}
	}

	private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
		for (PassengerRequest request : requests) {
			if (passenger.getId().equals(request.getPassengerId()))
				return request;
		}

		return null;
	}

	@Override
	public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
		PassengerRequest request = getRequestForPassenger(passenger);

		if (request == null) {
			throw new IllegalArgumentException("I am waiting for different passengers!");
		}

		if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
			passengersAboard++;
		} else {
			throw new IllegalStateException("The passenger is not on the link or not available for departure!");
		}

		if (passengersAboard == requests.size()) {
			endTime = now + pickupDuration;
		}
	}
}
