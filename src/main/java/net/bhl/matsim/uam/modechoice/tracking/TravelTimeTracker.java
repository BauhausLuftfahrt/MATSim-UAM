package net.bhl.matsim.uam.modechoice.tracking;

import com.google.inject.Singleton;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.io.*;
import java.util.*;

@Singleton
public class TravelTimeTracker {
	final private Map<Id<Person>, List<TrackedPrediction>> predictions = new HashMap<>();
	final private Map<Id<Person>, List<Double>> observations = new HashMap<>();

	public void resetTravelTimes() {
		predictions.clear();
		observations.clear();
	}

	public void addPrediction(Id<Person> personId, TrackedPrediction prediction) {
		synchronized (predictions) {
			if (!predictions.containsKey(personId)) {
				predictions.put(personId, new LinkedList<>());
			}

			predictions.get(personId).add(prediction);
		}
	}

	public void addObservation(Id<Person> personId, double travelTime) {
		synchronized (observations) {
			if (!observations.containsKey(personId)) {
				observations.put(personId, new LinkedList<>());
			}

			observations.get(personId).add(travelTime);
		}
	}

	public void write(File outputFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));

		writer.write(String.join(";", new String[]{ //
				"person_id", //
				"car_trip_id", //
				"predicted_trip_travel_time", //
				"predicted_number_of_links", //
				"observation" //
		}) + "\n");

		for (Id<Person> personId : predictions.keySet()) {
			List<TrackedPrediction> predictionList = predictions.get(personId);
			List<Double> observationList = observations.get(personId);

			if (observationList == null) {
				observationList = Collections.emptyList();
			}

			for (int i = 0; i < predictionList.size(); i++) {
				TrackedPrediction prediction = predictionList.get(i);
				double observation = i < observationList.size() ? observationList.get(i) : Double.NaN;

				writer.write(String.join(";", new String[]{ //
						personId.toString(), //
						String.valueOf(i), //
						String.valueOf(prediction.tripTravelTime), //
						String.valueOf(prediction.numberOfLinks), //
						String.valueOf(observation) //
				}) + "\n");
			}
		}

		writer.close();
	}
}
