package net.bhl.matsim.uam.events;

import com.google.inject.Inject;
import net.bhl.matsim.uam.run.UAMConstants;
import org.matsim.api.core.v01.Scenario;

/**
 * Class that store waiting times of a UAM station.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class WaitingData {
	double[] cumWaitingTimes = new double[60];
	int[] count = new int[60];

	private double defaultWaitTime;

	public WaitingData(double defaultWaitTime) {
		this.defaultWaitTime = defaultWaitTime;
	}

	public void addWaitingTime(double waitingTime, double arrivalTime) {
		//int simulationEnd = Integer.parseInt(scenario.getConfig().getModules().get("qsim").getParams().get("endTime").substring(0,2));
		if (arrivalTime < 30 * 3600.0) {
			int index = (int) Math.floor(arrivalTime / UAMConstants.waitingTimeBinSize);

			cumWaitingTimes[index] += waitingTime;
			count[index]++;
		}
	}

	public double[] getWaitingTimes() {
		double[] waitingTimes = new double[60];
		for (int i = 0; i < 60; i++) {
			if (count[i] != 0)
				waitingTimes[i] = this.cumWaitingTimes[i] / this.count[i];
			else
				waitingTimes[i] = defaultWaitTime;
		}
		return waitingTimes;
	}
}
