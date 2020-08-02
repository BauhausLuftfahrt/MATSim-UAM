package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.run.UAMConstants;

/**
 * Class that store waiting times of a UAM station.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class WaitingData {
	double[] cumWaitingTimes;
	int[] count;
	int bins;

	private double simulationEndTime;
	private double defaultWaitTime;

	public WaitingData(double simulationEndTime, double defaultWaitTime) {
		this.simulationEndTime = simulationEndTime;
		this.defaultWaitTime = defaultWaitTime;

		this.bins = (int) Math.ceil(simulationEndTime / UAMConstants.waitingTimeBinSize);
		this.cumWaitingTimes = new double[bins];
		this.count = new int[bins];
	}

	public void addWaitingTime(double waitingTime, double arrivalTime) {
		if (arrivalTime < simulationEndTime) {
			cumWaitingTimes[getIndex(arrivalTime)] += waitingTime;
			count[getIndex(arrivalTime)]++;
		}
	}

	public double getWaitingTime(double departureTime) {
		int i = getIndex(departureTime);
		if (count[i] != 0)
			return this.cumWaitingTimes[i] / this.count[i];
		else
			return defaultWaitTime;
	}

	private int getIndex(double time) {
		return (int) Math.floor(time / UAMConstants.waitingTimeBinSize);
	}
}
