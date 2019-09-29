package net.bhl.matsim.uam.modechoice.estimation.motorcycle;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;

import java.util.List;


public class CustomMCEstimator implements ModalTripEstimator {
	final private CustomMCPredictor predictor;
	final private CustomModeChoiceParameters parameters;

	public CustomMCEstimator(CustomModeChoiceParameters parameters, CustomMCPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		CustomMCPrediction prediction = predictor.predict(trip);
		double travelTime_min = prediction.travelTime / 60.0;
		double distance_km = prediction.distance * 1e-3;

		double cost = parameters.distanceCostCar_km * distance_km;
		double income = 0.0;

		if (trip.getPerson().getAttributes().getAttribute("income") != null)
			income = (double) trip.getPerson().getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;
		double betaCost = parameters.betaCost(distance_km,
				income);

		double utility = 0.0;
		utility += parameters.alphaMC;

		utility += parameters.betaTravelTimeMC_min * travelTime_min;
		utility += parameters.betaTravelTimeWalk_min * parameters.accessEgressWalkTimeMC_min;

		utility += betaCost * cost;

		return new TripCandidateWithPrediction(utility, "mc", prediction);
	}
}
