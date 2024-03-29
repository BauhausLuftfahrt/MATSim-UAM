package net.bhl.matsim.uam.passenger;

import org.matsim.contrib.dynagent.DynActivity;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.schedule.UAMChargingTask;

public class UAMChargingActivity implements DynActivity {

	public static final String ACTIVITY_TYPE = "Charging";
	private UAMChargingTask chargingTask;
	private UAMVehicle uamVehicle;
	private UAMManager uamManager;

	private double endTime = END_ACTIVITY_LATER;

	public UAMChargingActivity(UAMChargingTask chargingTask, UAMVehicle uamVehicle, UAMManager uamManager) {
		this.chargingTask = chargingTask;
		this.uamVehicle = uamVehicle;
		this.uamManager = uamManager;
	}

	@Override
	public String getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public void doSimStep(double now) {
		// check if not charging then increase end time
		// if ()
		// check if charged then adjust end time
		if (this.uamManager.getChargeVehicle().get(uamVehicle.getId()) < uamVehicle.getVehicleType().getMaximumCharge()) {
			if (this.endTime == now)
				this.endTime++;
		}
		else {
			this.endTime = now;
			//this means we can unplug the vehicle and move to the StayTask
			//make a charger available for the next vehicle
		}
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

}
