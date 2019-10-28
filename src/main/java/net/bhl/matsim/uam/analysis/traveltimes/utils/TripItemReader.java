package net.bhl.matsim.uam.analysis.traveltimes.utils;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.misc.Time;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripItemReader {
    public static List<TripItem> getTripItems(String tripsInput) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tripsInput)));
        String line;
        List<String> header = null;

        List<TripItem> trips = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            List<String> row = Arrays.asList(line.split(","));

            if (header == null) {
                header = row;
            } else {
                double originX = Double.parseDouble(row.get(header.indexOf("origin_x")));
                double originY = Double.parseDouble(row.get(header.indexOf("origin_y")));
                double destX = Double.parseDouble(row.get(header.indexOf("destination_x")));
                double destY = Double.parseDouble(row.get(header.indexOf("destination_y")));
                double departureTime = Time.parseTime(row.get(header.indexOf("trip_time")));

                Coord originCood = new Coord(originX, originY);
                Coord destinationCoord = new Coord(destX, destY);

                UAMTripItem trip = new UAMTripItem();
                trip.origin = originCood;
                trip.destination = destinationCoord;
                trip.departureTime = departureTime;

                trips.add(trip);
            }
        }
        reader.close();
        return trips;
    }
}
