package net.bhl.matsim.uam.analysis.uamstations.run;

import java.io.IOException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.pt.PtConstants;

import net.bhl.matsim.uam.analysis.uamdemand.CSVUAMDemandWriter;
import net.bhl.matsim.uam.analysis.uamdemand.UAMDemandItem;
import net.bhl.matsim.uam.analysis.uamdemand.listeners.UAMListener;
import net.bhl.matsim.uam.analysis.uamdemand.readers.EventsUAMReader;
import net.bhl.matsim.uam.analysis.uamstations.CSVUAMStationWriter;
import net.bhl.matsim.uam.analysis.uamstations.UAMStationItem;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import net.bhl.matsim.uam.router.UAMIntermodalRoutingModule;
import net.bhl.matsim.uam.router.UAMMainModeIdentifier;

/**
 * This script generates a Generates a csv file containing information about UAM
 * Stations. Necessary inputs are in the following order: -Network file; -UAM
 * Vehicles file -output file;
 *
 * @author Aitanm (Aitan Militão), RRothfeld (Raoul Rothfeld)
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
			uamData.add(new UAMStationItem(station.getName(), station.getId(), station.getLandingCapacity(),
					(int) station.getPreFlightTime(), (int) station.getPostFlightTime(),
					(int) station.getDefaultWaitTime(), station.getLocationLink().getId().toString()));
		}

		new CSVUAMStationWriter(uamData).write(outfile);
	}
}