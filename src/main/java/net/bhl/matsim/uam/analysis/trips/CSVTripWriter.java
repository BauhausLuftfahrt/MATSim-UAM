package net.bhl.matsim.uam.analysis.trips;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

/**
* This class writes a CSV file containing information about each trip.
* 
* @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
*/
public class CSVTripWriter {
	final private Collection<TripItem> trips;
	final private String delimiter;

	public CSVTripWriter(Collection<TripItem> trips) {
		this(trips, ",");
	}

	public CSVTripWriter(Collection<TripItem> trips, String delimiter) {
		this.trips = trips;
		this.delimiter = delimiter;
	}

	public void write(String outputPath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		writer.write(formatHeader() + "\n");
		writer.flush();

		for (TripItem trip : trips) {
			writer.write(formatTrip(trip) + "\n");
			writer.flush();
		}

		writer.flush();
		writer.close();
	}

	private String normalizeActivityType(String activityType) {
		return activityType.replaceAll("_[0-9]+$", "");
	}

	private String formatHeader() {
		return String.join(delimiter,
				new String[] { "person_id", "person_trip_id", "origin_x", "origin_y", "destination_x", "destination_y",
						"start_time", "travel_time", "network_distance", "mode", "preceedingPurpose",
						"followingPurpose", "returning", "crowfly_distance" });
	}

	private String formatTrip(TripItem trip) {
		return String.join(delimiter, new String[] { trip.personId.toString(), String.valueOf(trip.personTripId),
				String.valueOf(trip.origin.getX()), String.valueOf(trip.origin.getY()),
				String.valueOf(trip.destination.getX()), String.valueOf(trip.destination.getY()),
				String.valueOf(trip.startTime), String.valueOf(trip.travelTime), String.valueOf(trip.networkDistance),
				String.valueOf(trip.mode), normalizeActivityType(String.valueOf(trip.preceedingPurpose)),
				normalizeActivityType(String.valueOf(trip.followingPurpose)), String.valueOf(trip.returning),
				String.valueOf(trip.crowflyDistance) });
	}
}
