package net.bhl.matsim.uam.analysis.traffic.run;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

/**
 * This script takes a specific folder path and runs
 * {@link ConvertLinkStatsFromEvents} for all all MATSim output folders within
 * the provided base folder.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class BatchConvertLinkStatsFromEvents {
	// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
	private static String eventfile = "output_events.xml";
	private static String networkfile = "output_network.xml";

	private static String fileending = ".linkstats.csv";

	public static void main(final String[] args) throws IOException {
		File folder = Paths.get(args[0]).toFile();

		String[] ext = {"gz", "xml"};
		Collection<File> potentialFiles = FileUtils.listFiles(folder, ext, true);

		String[] ecl = {"csv"};
		Collection<File> alreadyExistingFiles = FileUtils.listFiles(folder, ecl, true);
		Collection<String> alreadyExistingFileNames = new HashSet<String>();
		for (File f : alreadyExistingFiles) {
			alreadyExistingFileNames.add(f.getAbsolutePath());
		}

		for (File f : potentialFiles) {
			if (!f.getName().contains(eventfile) || alreadyExistingFileNames.contains(f.getAbsolutePath() + fileending))
				continue;

			System.err.println("Working on: " + f.getAbsolutePath());
			String network = f.getAbsolutePath().replace(eventfile, networkfile);
			ConvertLinkStatsFromEvents.extract(network, f.getAbsolutePath(), f.getAbsolutePath() + fileending);
		}

		System.err.println("done.");
	}
}
