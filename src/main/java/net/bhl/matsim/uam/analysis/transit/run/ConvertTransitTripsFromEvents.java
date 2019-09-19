package net.bhl.matsim.uam.analysis.transit.run;

import java.io.IOException;
import java.util.Collection;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.pt.PtConstants;

import net.bhl.matsim.uam.analysis.transit.CSVTransitTripWriter;
import net.bhl.matsim.uam.analysis.transit.TransitTripItem;
import net.bhl.matsim.uam.analysis.transit.listeners.TransitTripListener;
import net.bhl.matsim.uam.analysis.transit.readers.EventsTransitTripReader;

/**
* This script generates a csv file containing information of all public transport
* trips performed from an events output file.
* Necessary inputs are in the following order:
* -Network file;
* -Events file;
* -output file;
* 
* @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
*/

public class ConvertTransitTripsFromEvents {
	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK EVENTS OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}
	
	static public void extract(String networkfile, String events, String outfile) throws IOException {
		// PROVIDE: NETWORK EVENTS OUTFILE-NAME
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkfile);

		StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE);

		TransitTripListener tripListener = new TransitTripListener(stageActivityTypes, network);
		Collection<TransitTripItem> trips = new EventsTransitTripReader(tripListener).readTrips(events);

		new CSVTransitTripWriter(trips).write(outfile);
	}
}
