package net.bhl.matsim.uam.analysis.transit.run;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

public class BatchConvertTransitTripsFromEvents {
	// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
	private static String eventfile = "output_events.xml";
	private static String networkfile = "output_network.xml";

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
			if (!f.getName().contains(eventfile) || alreadyExistingFileNames.contains(f.getAbsolutePath() + "_transit.csv"))
				continue;

			System.err.println("Working on: " + f.getAbsolutePath());
			String network = f.getAbsolutePath().replace(eventfile, networkfile);
			ConvertTransitTripsFromEvents.extract(network, f.getAbsolutePath(), f.getAbsolutePath() + "_transit.csv");
		}

		System.err.println("done.");
	}
}
