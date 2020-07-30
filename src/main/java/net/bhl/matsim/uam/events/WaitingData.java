package net.bhl.matsim.uam.events;

/**
 * Class that store waiting times of a UAM station.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class WaitingData {

	// 30min bins
	double[] cumWaitingTimes = new double[60];

	int[] count = new int[60];

	private double defaultWaitTime;

	public WaitingData(double defaultWaitTime) {
		this.defaultWaitTime = defaultWaitTime;
	}

	public void addWaitingTime(double waitingTime, double arrivalTime) {
		// TODO avoid literal (30 = 30:00:00 simulation end)
		if (arrivalTime < 30 * 3600.0) {
			int index = (int) Math.floor(arrivalTime / 1800.0);

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
