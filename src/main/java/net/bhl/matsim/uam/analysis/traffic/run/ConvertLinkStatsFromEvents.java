package net.bhl.matsim.uam.analysis.traffic.run;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

import net.bhl.matsim.uam.analysis.traffic.CSVLinkStatsWriter;
import net.bhl.matsim.uam.analysis.traffic.LinkStatsItem;

/**
 * This script generates a csv file containing the average speed per link per
 * hour of the input network from an output simulation events file. trips
 * performed from an events output file. Necessary inputs are in the following
 * order: -Network file; -Events file; -output file;
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class ConvertLinkStatsFromEvents {

	static int timeBinSize = 3600;
	static int maxTime = 30 * 3600;
	static boolean calculateLinkTravelTimes = true;
	static boolean calculateLinkToLinkTravelTimes = false;
	static boolean filterModes = true;
	static String analyzedMode = "car";

	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK EVENTS OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}

	static public void extract(String network, String events, String outfile) throws IOException {
		Network netw = NetworkUtils.createNetwork();
		new MatsimNetworkReader(netw).readFile(network);

		TravelTimeCalculatorConfigGroup tconfig = new TravelTimeCalculatorConfigGroup();
		tconfig.setAnalyzedModes(analyzedMode); // TODO does nothing?
		tconfig.setCalculateLinkToLinkTravelTimes(calculateLinkToLinkTravelTimes);
		tconfig.setCalculateLinkTravelTimes(calculateLinkTravelTimes);
		tconfig.setFilterModes(filterModes); // TODO does nothing?
		tconfig.setMaxTime(maxTime);
		tconfig.setTraveltimeBinSize(timeBinSize);

		TravelTimeCalculator ttc = TravelTimeCalculator.create(netw, tconfig);
		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(ttc);
		EventsReaderXMLv1 eventsReader = new EventsReaderXMLv1(manager);
		eventsReader.readFile(events);

		Collection<LinkStatsItem> linkStats = new HashSet<>();
		for (Link link : netw.getLinks().values()) {
			Map<Integer, Double> timeDependantSpeeds = new HashMap<>();
			for (int time = 0 + timeBinSize / 2; time < maxTime; time += timeBinSize) {
				timeDependantSpeeds.put(time, link.getLength() / ttc.getLinkTravelTime(link, time));
			}

			linkStats.add(new LinkStatsItem(link.getId(), link.getLength(), link.getFreespeed(), timeDependantSpeeds));
		}

		new CSVLinkStatsWriter(linkStats).write(outfile);
	}
}
