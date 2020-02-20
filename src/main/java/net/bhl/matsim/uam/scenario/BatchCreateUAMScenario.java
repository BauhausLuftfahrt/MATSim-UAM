package net.bhl.matsim.uam.scenario;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

/**
 * This script takes a specific folder path and runs
 * {@link RunCreateUAMScenario} for all station input files within
 * the provided base folder.
 *
 * @author RRothfeld (Raoul Rothfeld)
 */
public class BatchCreateUAMScenario {
	// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
	private static String stationsString = "stations";
	private static String vehiclesString = "vehicles";
	private static String networkString = "network";
	private static String nodesEndString = "_nodes.csv";
	private static String linksEndString = "_links.csv";

	public static void main(final String[] args) throws IOException {
		System.out.println("ARGS: base-folder");
		File folder = Paths.get(args[0]).toFile();

		String[] extNetwork = {"gz", "xml"};
		Collection<File> potentialNetworkFiles = FileUtils.listFiles(folder, extNetwork, false);
		Collection<String> networkFiles = new HashSet<>();
		for (File f : potentialNetworkFiles) {
			if (f.getName().contains(networkString))
				networkFiles.add(f.getAbsolutePath());
		}

		String[] extStations = {"csv"};
		Collection<File> potentialStationsFiles = FileUtils.listFiles(folder, extStations, false);
		Collection<String> stationFiles = new HashSet<>();
		Collection<String> vehiclesFiles = new HashSet<>();
		for (File f : potentialStationsFiles) {
			if (f.getName().contains(stationsString) && !f.getName().contains(nodesEndString) && !f.getName().contains(linksEndString))
				stationFiles.add(f.getAbsolutePath());
			else if (f.getName().contains(vehiclesString))
				vehiclesFiles.add(f.getAbsolutePath());
		}

		int total = networkFiles.size() * stationFiles.size() * vehiclesFiles.size();
		System.out.println("INFO: " + networkFiles.size() + " network files, " +
				stationFiles.size() + " stations files, " +
				"and " + vehiclesFiles.size() + " vehicles files have been found. " +
				total + " combinations.");

		int i = 0;
		for (String n : networkFiles) {
			for (String s : stationFiles) {
				String nodesFile = s.replace(".csv", nodesEndString);
				String linksFile = s.replace(".csv", linksEndString);
				boolean routed = new File(nodesFile).exists() && new File(linksFile).exists();

				for (String v : vehiclesFiles) {
					System.out.println("=========================================================");
					System.out.println("Working on combination " + ++i + "/" + total + ": " + n + ", " + s + ", " + v);

					if (routed)
						RunCreateUAMScenario.convert(n, s, v, nodesFile, linksFile);
					else
						RunCreateUAMScenario.convert(n, s, v);
				}
			}
		}

		System.err.println("All done.");
	}
}
