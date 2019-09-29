package net.bhl.matsim.uam.modechoice.tracking;

import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceModel;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceResult;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.car.CustomCarPrediction;

import java.util.List;
import java.util.Random;

public class TrackingModeChoiceModel implements ModeChoiceModel {
	final private ModeChoiceModel delegate;
	final private TravelTimeTracker tracker;

	public TrackingModeChoiceModel(ModeChoiceModel delegate, TravelTimeTracker tracker) {
		this.delegate = delegate;
		this.tracker = tracker;
	}

	@Override
	public ModeChoiceResult chooseModes(List<ModeChoiceTrip> trips, Random random) throws NoFeasibleChoiceException {
		ModeChoiceResult result = delegate.chooseModes(trips, random);

		for (TripCandidate trip : result.getTripCandidates()) {
			if (trip.getMode().equals("car")) {
				CustomCarPrediction prediction = (CustomCarPrediction) ((TripCandidateWithPrediction) trip)
						.getPrediction();

				double tripTravelTime = prediction.travelTime;
				int numberOfLinks = prediction.route.getLinkIds().size();

				tracker.addPrediction(trips.get(0).getPerson().getId(),
						new TrackedPrediction(tripTravelTime, numberOfLinks));
			}
		}

		return result;
	}
}
