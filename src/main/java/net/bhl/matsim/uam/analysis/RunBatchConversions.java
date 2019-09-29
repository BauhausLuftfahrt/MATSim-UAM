package net.bhl.matsim.uam.analysis;

import net.bhl.matsim.uam.analysis.traffic.run.BatchConvertLinkStatsFromEvents;
import net.bhl.matsim.uam.analysis.transit.run.BatchConvertTransitTripsFromEvents;
import net.bhl.matsim.uam.analysis.trips.run.BatchConvertTripsFromEvents;
import net.bhl.matsim.uam.analysis.trips.run.BatchConvertTripsFromPopulation;
import net.bhl.matsim.uam.analysis.uamdemand.run.BatchConvertUAMDemandFromEvents;
import net.bhl.matsim.uam.analysis.uamstations.run.BatchConvertUAMStationsFromUAMVehicles;

import java.io.IOException;

public class RunBatchConversions {

	public static void main(String[] args) throws IOException {
		// PROVIDE PARENT FOLDER OF OUTPUT FOLDERS
		BatchConvertUAMStationsFromUAMVehicles.main(args);
		BatchConvertUAMDemandFromEvents.main(args);
		BatchConvertTripsFromEvents.main(args);
		BatchConvertTransitTripsFromEvents.main(args);
		BatchConvertTripsFromPopulation.main(args);
		BatchConvertLinkStatsFromEvents.main(args);
	}

}
