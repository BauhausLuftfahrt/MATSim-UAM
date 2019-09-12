package net.bhl.matsim.uam.schedule;

import java.util.Collection;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

import net.bhl.matsim.uam.passenger.UAMRequest;
/** 
 *    During this task the UAM Vehicle is idle.
 */
public class UAMStayTask extends StayTaskImpl implements UAMTask{
    public UAMStayTask(double beginTime, double endTime, Link link, String name)
    {
        super(beginTime, endTime, link, name);
    }

    public UAMStayTask(double beginTime, double endTime, Link link)
    {
        this(beginTime, endTime, link, "AVStay");
    }

    @Override
    protected String commonToString()
    {
        return "[" + getUAMTaskType().toString() + "]" + super.commonToString();
    }

    @Override
    public UAMTaskType getUAMTaskType() {
        return UAMTaskType.STAY;
    }

	@Override
	public Collection<UAMRequest> getRequests() {
		return null;
	}

	@Override
	public void addRequest(UAMRequest request) {
		
	}
}
