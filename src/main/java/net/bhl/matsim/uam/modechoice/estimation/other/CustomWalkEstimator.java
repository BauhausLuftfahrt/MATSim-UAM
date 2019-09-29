package net.bhl.matsim.uam.modechoice.estimation.other;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import ch.ethz.matsim.mode_choice.prediction.TeleportationPrediction;
import ch.ethz.matsim.mode_choice.prediction.TeleportationPredictor;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;

import java.util.List;

public class CustomWalkEstimator implements ModalTripEstimator {
	final private TeleportationPredictor predictor;
	final private CustomModeChoiceParameters parameters;
	private boolean isMinTravelTime;

	public CustomWalkEstimator(CustomModeChoiceParameters parameters, TeleportationPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	public CustomWalkEstimator(CustomModeChoiceParameters parameters,
							   TeleportationPredictor predictor, boolean isMinTravelTime) {
		this(parameters, predictor);
		this.isMinTravelTime = isMinTravelTime;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		TeleportationPrediction prediction = predictor.predict(trip);
		if (isMinTravelTime) {
			return new TripCandidateWithPrediction(prediction.travelTime, "walk", prediction);  //In case of standard simulation, returns travel time instead of utility.
		}

		double travelTime_min = prediction.travelTime / 60.0;

		double utility = parameters.alphaWalk;
		utility += parameters.betaTravelTimeWalk_min * travelTime_min;


		return new TripCandidateWithPrediction(utility, "walk", prediction);
	}
}
