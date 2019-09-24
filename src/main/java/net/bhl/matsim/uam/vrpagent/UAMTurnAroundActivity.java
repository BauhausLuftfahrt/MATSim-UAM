package net.bhl.matsim.uam.vrpagent;

import org.matsim.contrib.dynagent.AbstractDynActivity;

import net.bhl.matsim.uam.schedule.UAMTurnAroundTask;

/**
 * An implementation of AbstractDynActivity for UAM DynAgents for the
 * UAMTurnAroundTask.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMTurnAroundActivity extends AbstractDynActivity {

	final private UAMTurnAroundTask turnAroundTask;
	private double now;

	public UAMTurnAroundActivity(UAMTurnAroundTask turnAroundTask) {
		super(turnAroundTask.getName());
		this.turnAroundTask = turnAroundTask;
		this.now = turnAroundTask.getBeginTime();
	}

	@Override
	public void doSimStep(double now) {
		this.now = now;
	}

	@Override
	public double getEndTime() {
		return turnAroundTask.getEndTime();
	}

}
