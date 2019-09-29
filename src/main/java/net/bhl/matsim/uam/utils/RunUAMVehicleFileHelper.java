package net.bhl.matsim.uam.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * This script generates a text file containing UAM Station and UAM Vehicles
 * information.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class RunUAMVehicleFileHelper {

	public static void main(final String[] args) throws IOException {
		File fout = new File("C:\\Users\\Raoul\\Downloads\\5000.txt");
		int vehiclesPerStation = 5000;
		String[] stations = {"11", "12", "13", "14", "15", "16", "21", "22", "23"};

		FileOutputStream fos = new FileOutputStream(fout);
		OutputStreamWriter osw = new OutputStreamWriter(fos);

		for (String station : stations) {
			for (int i = 0; i < vehiclesPerStation; i++) {
				osw.write("		<vehicle id=\"uam_vh_" + station + "-" + i
						+ "\" capacity=\"1\" starttime=\"00:00:00\" endtime=\"30:00:00\" cruisespeed=\"33.3333\" verticalspeed=\"10\" initialstation=\""
						+ station + "\" />\n");
			}
		}

		osw.close();

		System.out.println("done.");
	}

}
