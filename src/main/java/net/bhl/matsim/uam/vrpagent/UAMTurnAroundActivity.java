package net.bhl.matsim.uam.vrpagent;

import net.bhl.matsim.uam.schedule.UAMTask;
import net.bhl.matsim.uam.schedule.UAMTask.UAMTaskType;
import net.bhl.matsim.uam.schedule.UAMTurnAroundTask;
import org.matsim.contrib.dynagent.DynActivity;

/**
 * An implementation of AbstractDynActivity for UAM DynAgents for the
 * UAMTurnAroundTask.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMTurnAroundActivity implements DynActivity {

	final private UAMTurnAroundTask turnAroundTask;
	private double now;
	private final String activityType;

	public UAMTurnAroundActivity(UAMTurnAroundTask turnAroundTask) {
		activityType = "UAMTurnAround";
		this.turnAroundTask = turnAroundTask;
		this.now = turnAroundTask.getBeginTime();
	}

	private static String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doSimStep(double now) {
		this.now = now;
	}

	@Override
	public double getEndTime() {
		return turnAroundTask.getEndTime();
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

}
