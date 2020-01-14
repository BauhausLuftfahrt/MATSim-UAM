package net.bhl.matsim.uam.analysis.traveltimes.utils;

public class ThreadCounter {
	private int processes;

	public synchronized void register() {
		processes++;
	}

	public synchronized void deregister() {
		processes--;
	}

	public synchronized int getProcesses() {
		return processes;
	}
}
