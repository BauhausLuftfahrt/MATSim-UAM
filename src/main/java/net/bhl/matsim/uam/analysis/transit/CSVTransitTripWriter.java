package net.bhl.matsim.uam.analysis.transit;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

public class CSVTransitTripWriter {
	final private Collection<TransitTripItem> trips;
	final private String delimiter;

	public CSVTransitTripWriter(Collection<TransitTripItem> trips) {
		this(trips, ",");
	}

	public CSVTransitTripWriter(Collection<TransitTripItem> trips, String delimiter) {
		this.trips = trips;
		this.delimiter = delimiter;
	}

	public void write(String outputPath) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));

		writer.write(formatHeader() + "\n");
		writer.flush();

		for (TransitTripItem trip : trips) {
			writer.write(formatTrip(trip) + "\n");
			writer.flush();
		}

		writer.flush();
		writer.close();
	}

	private String formatHeader() {
		return String.join(delimiter,
				new String[] { "person_id", "person_trip_id", "origin_x", "origin_y", "destination_x", "destination_y",
						"start_time", "in_vehicle_time", "waiting_time", "transfer_time", "in_vehicle_distance",
						"in_vehicle_crowfly_distance", "transfer_distance", "transfer_crowfly_distance",
						"number_of_transfers", "crowfly_distance", "first_waiting_time", "routing" });
	}

	private String formatTrip(TransitTripItem trip) {
		return String.join(delimiter, new String[] { trip.personId.toString(), String.valueOf(trip.personTripId),
				String.valueOf(trip.origin.getX()), String.valueOf(trip.origin.getY()),
				String.valueOf(trip.destination.getX()), String.valueOf(trip.destination.getY()),
				String.valueOf(trip.startTime), String.valueOf(trip.inVehicleTime), String.valueOf(trip.waitingTime),
				String.valueOf(trip.transferTime), String.valueOf(trip.inVehicleDistance),
				String.valueOf(trip.inVehicleCrowflyDistance), String.valueOf(trip.transferDistance),
				String.valueOf(trip.transferCrowflyDistance), String.valueOf(trip.numberOfTransfers),
				String.valueOf(trip.crowflyDistance), String.valueOf(trip.firstWaitingTime), String.valueOf(trip.routing) });
	}
}
