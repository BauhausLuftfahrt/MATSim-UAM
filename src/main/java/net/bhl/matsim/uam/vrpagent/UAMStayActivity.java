package net.bhl.matsim.uam.vrpagent;

import org.matsim.contrib.dynagent.AbstractDynActivity;

import net.bhl.matsim.uam.schedule.UAMStayTask;

/**
 * An implementation of AbstractDynActivity for UAM DynAgents for the
 * UAMStayTask.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMStayActivity extends AbstractDynActivity {
	final private UAMStayTask stayTask;
	private double now;

	public UAMStayActivity(UAMStayTask stayTask) {
		super(stayTask.getName());
		this.stayTask = stayTask;
		this.now = stayTask.getBeginTime();
	}

	@Override
	public void doSimStep(double now) {
		this.now = now;
	}

	@Override
	public double getEndTime() {
		if (Double.isInfinite(stayTask.getEndTime())) {
			return now + 1;
		} else {
			return stayTask.getEndTime();
		}
	}

}
