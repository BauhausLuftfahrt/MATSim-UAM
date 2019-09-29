package net.bhl.matsim.uam.analysis.traffic;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class CSVLinkStatsWriter {
	final private Collection<LinkStatsItem> links;
	final private String delimiter;
	private SortedSet<Integer> timeHeaders;

	public CSVLinkStatsWriter(Collection<LinkStatsItem> links) {
		this(links, ",");
	}

	public CSVLinkStatsWriter(Collection<LinkStatsItem> links, String delimiter) {
		this.links = links;
		this.delimiter = delimiter;
	}

	public void write(String outputPath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
		writer.write(formatHeader() + "\n");
		for (LinkStatsItem link : links) {
			writer.write(formatLinkStatsItem(link) + "\n");
		}
		writer.close();
	}

	private String formatHeader() {
		List<String> header = new ArrayList<>();
		header.add("link_id");
		header.add("length_m");
		header.add("freespeed_ms");

		timeHeaders = new TreeSet<Integer>();
		for (LinkStatsItem link : links) {
			for (Integer i : link.timeDependantSpeed.keySet()) {
				timeHeaders.add(i);
			}
		}

		for (Integer timeHead : timeHeaders) {
			header.add("avgspeed_ms_at_H" + (timeHead / 3600) + "M" + ((timeHead % 3600) / 60));
		}

		return String.join(delimiter, header);
	}

	private String formatLinkStatsItem(LinkStatsItem link) {
		List<String> row = new ArrayList<>();
		row.add(link.linkId.toString());
		row.add(String.valueOf(link.distance));
		row.add(String.valueOf(link.freeSpeed));

		for (Integer timeHead : timeHeaders) {
			if (link.timeDependantSpeed.containsKey(timeHead))
				row.add(String.valueOf(link.timeDependantSpeed.get(timeHead)));
		}

		return String.join(delimiter, row);
	}
}
