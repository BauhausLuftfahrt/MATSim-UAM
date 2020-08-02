package net.bhl.matsim.uam.scenario;

/**
 * This script creates UAM-including MATSim network and corresponding
 * uam-vehicles file, which are prerequisites for running a UAM-enabled MATSim
 * simulation with a UAM flight being beeline connections between all stations.
 *
 * @author RRothfeld (Raoul Rothfeld)
 */
public class RunCreateUAMBeelineScenario {
	public static void main(String[] args) {
		System.out.println("ARGS: config.xml* uam-stations.csv* uam-link-freespeed* uam-link-capacity* uam-vehicles.csv");
		System.out.println("(* required)");

		// ARGS
		int j = 0;
		String configInput = args[j++];
		String stationInput = args[j++];
		double uamMaxLinkSpeed = Double.parseDouble(args[j++]);
		double uamLinkCapacity = Double.parseDouble(args[j++]);
		String vehicleInput = null;

		if (args.length > j)
			vehicleInput = args[j];

		// Run
		RunCreateUAMRoutedScenario.convert(configInput, stationInput, uamMaxLinkSpeed, uamLinkCapacity, vehicleInput);
	}
}