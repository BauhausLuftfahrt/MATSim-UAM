package net.bhl.matsim.uam.analysis.traffic.run;

import net.bhl.matsim.uam.analysis.traffic.CSVLinkStatsWriter;
import net.bhl.matsim.uam.analysis.traffic.LinkStatsItem;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

import java.io.IOException;
import java.util.*;

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
	static String[] analyzedModes = {TransportMode.car};

	static public void main(String[] args) throws IOException {
		// PROVIDE: NETWORK EVENTS OUTFILE-NAME
		extract(args[0], args[1], args[2]);
		System.out.println("done.");
	}

	static public void extract(String network, String events, String outfile) throws IOException {
		Network netw = NetworkUtils.createNetwork();
		new MatsimNetworkReader(netw).readFile(network);

		TravelTimeCalculatorConfigGroup tconfig = new TravelTimeCalculatorConfigGroup();

        Set<String> modes = new HashSet<>(Arrays.asList(analyzedModes));
		tconfig.setAnalyzedModes(modes);
		tconfig.setFilterModes(filterModes);

		tconfig.setCalculateLinkToLinkTravelTimes(calculateLinkToLinkTravelTimes);
		tconfig.setCalculateLinkTravelTimes(calculateLinkTravelTimes);

		tconfig.setMaxTime(maxTime);
		tconfig.setTraveltimeBinSize(timeBinSize);

		TravelTimeCalculator.Builder builder = new TravelTimeCalculator.Builder(netw);
		builder.configure(tconfig);
		TravelTimeCalculator ttc = builder.build();

		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(ttc);
		EventsReaderXMLv1 eventsReader = new EventsReaderXMLv1(manager);
		eventsReader.readFile(events);

		Collection<LinkStatsItem> linkStats = new HashSet<>();
		TravelTime tts = ttc.getLinkTravelTimes();
		for (Link link : netw.getLinks().values()) {
			Map<Integer, Double> timeDependantSpeeds = new HashMap<>();
			for (int time = timeBinSize / 2; time < maxTime; time += timeBinSize) {
				timeDependantSpeeds.put(time, link.getLength() / tts.getLinkTravelTime(link, time, null, null));
			}

			linkStats.add(new LinkStatsItem(link.getId(), link.getLength(), link.getFreespeed(), timeDependantSpeeds));
		}

		new CSVLinkStatsWriter(linkStats).write(outfile);
	}
}
