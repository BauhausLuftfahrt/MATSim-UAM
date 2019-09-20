package net.bhl.matsim.uam.analysis.trips.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

/**
 * This script takes a specific folder path and runs
 * {@link ConvertTripsFromEvents} for all MATSim output folders within the
 * provided base folder.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class BatchConvertTripsFromEvents {
	// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
	private static String eventfile = "output_events.xml";
	private static String networkfile = "output_network.xml";

	public static void main(final String[] args) throws IOException {
		File folder = Paths.get(args[0]).toFile();

		String[] ext = { "gz", "xml" };
		Collection<File> potentialFiles = FileUtils.listFiles(folder, ext, true);

		String[] ecl = { "csv" };
		Collection<File> alreadyExistingFiles = FileUtils.listFiles(folder, ecl, true);
		Collection<String> alreadyExistingFileNames = new HashSet<String>();
		for (File f : alreadyExistingFiles) {
			alreadyExistingFileNames.add(f.getAbsolutePath());
		}

		for (File f : potentialFiles) {
			if (!f.getName().contains(eventfile) || alreadyExistingFileNames.contains(f.getAbsolutePath() + ".csv"))
				continue;

			System.err.println("Working on: " + f.getAbsolutePath());
			String network = f.getAbsolutePath().replace(eventfile, networkfile);
			ConvertTripsFromEvents.extract(network, f.getAbsolutePath(), f.getAbsolutePath() + ".csv");
		}

		System.err.println("done.");
	}
}
