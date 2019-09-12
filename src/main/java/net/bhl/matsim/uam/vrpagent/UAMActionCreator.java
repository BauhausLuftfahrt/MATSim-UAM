package net.bhl.matsim.uam.vrpagent;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

import com.google.inject.Inject;

import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.passenger.UAMPassengerDropoffActivity;
import net.bhl.matsim.uam.passenger.UAMPassengerPickupActivity;
import net.bhl.matsim.uam.schedule.UAMDropoffTask;
import net.bhl.matsim.uam.schedule.UAMPickupTask;
import net.bhl.matsim.uam.schedule.UAMStayTask;
import net.bhl.matsim.uam.schedule.UAMTask;
import net.bhl.matsim.uam.schedule.UAMTurnAroundTask;
/**
 * 
 * Class that is responsible for creating DynAction activities
 * depending on the task of the vehicle.
 * 
 * @author balacm
 *
 */
public class UAMActionCreator implements VrpAgentLogic.DynActionCreator {
	public static final String PICKUP_ACTIVITY_TYPE = "UAMPickup";
	public static final String DROPOFF_ACTIVITY_TYPE = "UAMDropoff";
	public static final String STAY_ACTIVITY_TYPE = "UAMStay";
	public static final String TURNAROUND_ACTIVITY_TYPE = "UAMTurnAround";

	@Inject
	private PassengerEngine passengerEngine;

	@Inject
	private VrpLegs.LegCreator legCreator;

	@Override
	public DynAction createAction(DynAgent dynAgent, Vehicle vehicle, double now) {
		Task task = vehicle.getSchedule().getCurrentTask();
		if (task instanceof UAMTask) {
			switch (((UAMTask) task).getUAMTaskType()) {
			case PICKUP:
				UAMPickupTask mpt = (UAMPickupTask) task;
				return new UAMPassengerPickupActivity(passengerEngine, dynAgent, vehicle, mpt, mpt.getRequests(),
						mpt.getBoardingTime(), PICKUP_ACTIVITY_TYPE);
			case DROPOFF:
				UAMDropoffTask mdt = (UAMDropoffTask) task;
				return new UAMPassengerDropoffActivity(passengerEngine, dynAgent, vehicle, mdt, mdt.getRequests(),
						mdt.getDeboardingTime(), DROPOFF_ACTIVITY_TYPE);
			case FLY:
				return legCreator.createLeg(vehicle);
			case STAY:
				return new UAMStayActivity((UAMStayTask) task);
			case TURNAROUND:
				return new UAMTurnAroundActivity((UAMTurnAroundTask) task);
			default:
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
