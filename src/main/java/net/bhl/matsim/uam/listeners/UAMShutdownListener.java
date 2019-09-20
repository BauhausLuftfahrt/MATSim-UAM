package net.bhl.matsim.uam.listeners;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.utils.io.IOUtils;

import com.google.inject.Inject;

/**
 * This listener copies the input UAM Vehicle file into the output folder after
 * the simulation ends
 *
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
 */

public class UAMShutdownListener implements ShutdownListener {
	@Inject
	private OutputDirectoryHierarchy controlerIO;
	private static final Logger log = Logger.getLogger(UAMListener.class);

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		writeUAMVehiclesFile(event);
	}

	private void writeUAMVehiclesFile(ShutdownEvent event) {
		String configPath = event.getServices().getConfig().getContext().getPath();
		int index = configPath.lastIndexOf('/');
		configPath = configPath.substring(0, index + 1);

		String uamFileName = event.getServices().getConfig().getModules().get("uam").getParams().get("inputUAMFile");

		InputStream fromStream = IOUtils.getInputStream(configPath + uamFileName);
		OutputStream toStream = IOUtils.getOutputStream(controlerIO.getOutputFilename("output_uam_vehicles.xml.gz"));

		try {
			try {
				IOUtils.copyStream(fromStream, toStream);
			} catch (IOException ee) {
				log.warn("writing output UAM Vehicles did not work; probably parameters were such that no events were "
						+ "generated in the final iteration");
			}

			fromStream.close();
			toStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
