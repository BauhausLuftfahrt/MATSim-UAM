package net.bhl.matsim.uam.passenger;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

import java.util.List;
import java.util.Set;

/**
 * This class defines the pick up activity for the passenger and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */

public class UAMSinglePassengerPerRequestPickupActivity implements PassengerPickupActivity {

	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<PassengerRequest> requests;
	private final double pickupDuration;
	private final String activityType;

	private double maximumRequestT0 = 0;

	private double endTime;
	private int passengersAboard;

	public UAMSinglePassengerPerRequestPickupActivity(PassengerEngine passengerEngine, DynAgent driver,
									  DvrpVehicle vehicle, StayTask pickupTask, Set<PassengerRequest> requests,
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
		int passengerCount = 0;
		for (PassengerRequest request : requests) {
			passengerCount += request.getPassengerCount();
			if (passengerEngine.tryPickUpPassengers(this, driver, request.getId(), pickupTask.getBeginTime())) {
				passengersAboard += request.getPassengerCount();
			}

			if (request.getEarliestStartTime() > maximumRequestT0) {
				maximumRequestT0 = request.getEarliestStartTime();
			}
		}

		if (passengersAboard == passengerCount) {
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
			if (request.getPassengerIds().contains( passenger.getId()))
				return request;
		}

		return null;
	}


	@Override
	public void notifyPassengersAreReadyForDeparture(List<MobsimPassengerAgent> passengers, double now) {
		for (MobsimPassengerAgent mpa : passengers) {

			PassengerRequest pr = getRequestForPassenger(mpa);
			if (pr == null)
				throw new IllegalArgumentException("I am waiting for a different passenger!");
			if (passengerEngine.tryPickUpPassengers(this, driver, pr.getId(), now)) {
				passengersAboard++;
			} else {
				throw new IllegalStateException("The passenger is not on the link or not available for departure!");
			}
			
		}
		
		if (passengersAboard == requests.size()) {
			endTime = now + pickupDuration;
		}
	}

}
