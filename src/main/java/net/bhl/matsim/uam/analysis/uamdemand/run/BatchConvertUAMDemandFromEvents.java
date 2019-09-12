package net.bhl.matsim.uam.analysis.uamdemand.run;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

public class BatchConvertUAMDemandFromEvents {
	// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
	private static String eventfile = "output_events.xml";
	private static String networkfile = "output_network.xml";
	private static String vehiclefile = "output_uam_vehicles.xml";

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
			if (!f.getName().contains(vehiclefile) || alreadyExistingFileNames.contains(f.getAbsolutePath() + "_demand.csv"))
				continue;
			
			System.err.println("Working on: " + f.getAbsolutePath());
			String network = f.getAbsolutePath().replace(vehiclefile, networkfile);
			String events = f.getAbsolutePath().replace(vehiclefile, eventfile);
			ConvertUAMDemandFromEvents.extract(network, events, f.getAbsolutePath(), f.getAbsolutePath() + "_demand.csv");
		}
		
		System.err.println("done.");
	}
}
