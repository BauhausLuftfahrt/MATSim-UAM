package net.bhl.matsim.uam.data;

/**
 * Class that store waiting times of a UAM station.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class WaitingData {
	private final double[] cumWaitingTimes;
	private final int[] count;

	private final double simulationEndTime;
	private final double waitingTimeBinSize;
	private final double defaultWaitTime;

	public WaitingData(double simulationEndTime, double waitingTimeBinSize, double defaultWaitTime) {
		this.simulationEndTime = simulationEndTime;
		this.waitingTimeBinSize = waitingTimeBinSize;
		this.defaultWaitTime = defaultWaitTime;

		int bins = (int) Math.ceil(simulationEndTime / waitingTimeBinSize);
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
			return cumWaitingTimes[i] / count[i];
		else
			return defaultWaitTime;
	}

	private int getIndex(double time) {
		return (int) Math.floor(time / waitingTimeBinSize);
	}

	public String toCsv() {
		StringBuilder sb = new StringBuilder("bin,count,waitingTimeSum,waitingTimeAvg\n");
		for (int i = 0; i < cumWaitingTimes.length; i++)
			sb.append(i).append(",").append(count[i]).append(",").append(cumWaitingTimes[i]).append(",")
					.append(count[i] == 0 ? defaultWaitTime : cumWaitingTimes[i] / count[i]).append("\n");
		return sb.toString();
	}
}
