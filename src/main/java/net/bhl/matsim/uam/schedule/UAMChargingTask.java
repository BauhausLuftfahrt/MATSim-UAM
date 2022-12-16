package net.bhl.matsim.uam.schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTask;

public class UAMChargingTask extends StayTask{

	public UAMChargingTask(TaskType taskType, double beginTime, double endTime, Link link) {
		super(taskType, beginTime, endTime, link);
		
	}

}
