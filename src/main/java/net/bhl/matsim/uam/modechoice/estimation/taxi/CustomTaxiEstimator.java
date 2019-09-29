package net.bhl.matsim.uam.modechoice.estimation.taxi;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.modechoice.estimation.car.CustomCarPrediction;
import net.bhl.matsim.uam.modechoice.estimation.car.CustomCarPredictor;

import java.util.List;

/**
 * This class defines the estimator for Taxi mode.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomTaxiEstimator implements ModalTripEstimator {
	final private CustomCarPredictor predictor;
	final private CustomModeChoiceParameters parameters;

	public CustomTaxiEstimator(CustomModeChoiceParameters parameters, CustomCarPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		CustomCarPrediction prediction = predictor.predict(trip);
		double travelTime_min = prediction.travelTime / 60.0;
		double distance_km = prediction.distance * 1e-3;

		double cost = parameters.distanceCostTaxi_km * distance_km + parameters.timeCostTaxi_min * travelTime_min
				+ parameters.initialCostTaxi;
		double income = 0.0;

		if (trip.getPerson().getAttributes().getAttribute("income") != null)
			income = (double) trip.getPerson().getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;
		double betaCost = parameters.betaCost(distance_km, income);
		double utility = 0.0;
		utility += parameters.alphaTaxi;

		utility += parameters.betaTravelTimeTaxi_min * travelTime_min;
		utility += parameters.betaWaitingTimeTaxi_min * parameters.waitingTimeTaxi_min;

		utility += betaCost * cost;

		return new TripCandidateWithPrediction(utility, "taxi", prediction);
	}
}
