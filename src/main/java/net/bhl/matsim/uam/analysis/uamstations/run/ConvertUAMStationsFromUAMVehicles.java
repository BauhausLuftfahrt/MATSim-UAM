package net.bhl.matsim.uam.analysis.uamstations.run;

import net.bhl.matsim.uam.analysis.uamstations.CSVUAMStationWriter;
import net.bhl.matsim.uam.analysis.uamstations.UAMStationItem;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * This script generates a Generates a csv file containing information about UAM
 * Stations. Necessary inputs are in the following order: -Network file; -UAM
 * Vehicles file -output file;
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

public class ConvertUAMStationsFromUAMVehicles {
	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK UAMVEHICLES OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}

	static public void extract(String network, String uamVehicles, String outfile) throws IOException {
		Network netw = NetworkUtils.createNetwork();
		new MatsimNetworkReader(netw).readFile(network);

		UAMXMLReader uamReader = new UAMXMLReader(netw);
		uamReader.readFile(uamVehicles);
		UAMStations uamStations = new UAMStations(uamReader.getStations(), netw);

		Collection<UAMStationItem> uamData = new HashSet<>();
		for (UAMStation station : uamStations.getUAMStations().values()) {
			uamData.add(new UAMStationItem(station.getName(), station.getId(),
					(int) station.getPreFlightTime(), (int) station.getPostFlightTime(),
					(int) station.getDefaultWaitTime(), station.getLocationLink().getId().toString()));
		}

		new CSVUAMStationWriter(uamData).write(outfile);
	}
}