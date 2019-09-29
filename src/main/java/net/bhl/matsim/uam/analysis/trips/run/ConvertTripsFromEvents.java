package net.bhl.matsim.uam.analysis.trips.run;

import net.bhl.matsim.uam.analysis.trips.CSVTripWriter;
import net.bhl.matsim.uam.analysis.trips.TripItem;
import net.bhl.matsim.uam.analysis.trips.listeners.TripListener;
import net.bhl.matsim.uam.analysis.trips.readers.EventsTripReader;
import net.bhl.matsim.uam.analysis.trips.utils.BasicHomeActivityTypes;
import net.bhl.matsim.uam.analysis.trips.utils.HomeActivityTypes;
import net.bhl.matsim.uam.router.UAMMainModeIdentifier;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.pt.PtConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * This script creates a trips file by reading through and gathering trip
 * information from an existing events file. Necessary inputs are in the
 * following order: -Network file; -Events file; -output file;
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class ConvertTripsFromEvents {
	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK EVENTS OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}

	static public void extract(String network, String events, String outfile) throws IOException {
		Network netw = NetworkUtils.createNetwork();
		new MatsimNetworkReader(netw).readFile(network);

		// Add UAM stage activity types
		StageActivityTypes stageActivityTypes = new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE,
				UAMModes.UAM_INTERACTION);

		HomeActivityTypes homeActivityTypes = new BasicHomeActivityTypes();
		MainModeIdentifier mainModeIdentifier = new UAMMainModeIdentifier(new MainModeIdentifierImpl());
		Collection<String> networkRouteModes = Arrays.asList("car", "uam", "access_uam_car", "egress_uam_car");

		TripListener tripListener = new TripListener(netw, stageActivityTypes, homeActivityTypes, mainModeIdentifier,
				networkRouteModes);
		Collection<TripItem> trips = new EventsTripReader(tripListener).readTrips(events);

		new CSVTripWriter(trips).write(outfile);
	}
}
