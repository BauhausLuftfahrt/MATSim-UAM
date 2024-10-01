package net.bhl.matsim.uam.schedule;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.DefaultStayTask;

import net.bhl.matsim.uam.infrastructure.UAMStation;

public class UAMChargingTask extends DefaultStayTask{

	private Id<UAMStation> uamStationId;
	public UAMChargingTask(TaskType taskType, double beginTime, double endTime, Link link, Id<UAMStation> uamStationId) {
		super(taskType, beginTime, endTime, link);
		this.uamStationId = uamStationId;
		
	}
	public Id<UAMStation> getUamStationId() {
		return uamStationId;
	}

}
