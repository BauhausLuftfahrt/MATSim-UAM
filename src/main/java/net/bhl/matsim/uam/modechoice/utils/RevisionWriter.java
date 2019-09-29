package net.bhl.matsim.uam.modechoice.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

import java.io.*;

/**
 * This class searches for the current git revision being used and writes it down.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Singleton
public class RevisionWriter implements StartupListener {
	final private static Logger logger = Logger.getLogger(RevisionWriter.class);
	final private OutputDirectoryHierarchy outputDirectoryHierarchy;

	@Inject
	public RevisionWriter(OutputDirectoryHierarchy outputDirectoryHierarchy) {
		this.outputDirectoryHierarchy = outputDirectoryHierarchy;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		try {
			File revsionFile = new File(RevisionWriter.class.getResource("git").getPath());

			if (revsionFile.exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(revsionFile)));
				String revisionLine = reader.readLine().trim();
				reader.close();

				logger.info("Found revision: " + revisionLine);

				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputDirectoryHierarchy.getOutputFilename("revision"))));
				writer.write(revisionLine);
				writer.close();
			} else {
				logger.warn("No revision file found!");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
