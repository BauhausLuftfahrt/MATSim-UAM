package net.bhl.matsim.uam.modechoice.estimation.uam;

import java.util.List;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;

/**
 * This class defines the estimator for UAM trips.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CustomUAMEstimator implements ModalTripEstimator {
	final private CustomUAMPredictor predictor;
	final private CustomModeChoiceParameters parameters;
	private boolean isMinTravelTime;

	public CustomUAMEstimator(CustomModeChoiceParameters parameters, CustomUAMPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	public CustomUAMEstimator(CustomModeChoiceParameters parameters, CustomUAMPredictor predictor,
			boolean isMinTravelTime) {
		this(parameters, predictor);
		this.isMinTravelTime = isMinTravelTime;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {

		CustomUAMPrediction prediction = predictor.predictTrip(trip);

		// In case of standard simulation, returns travel time instead of utility.
		if (isMinTravelTime) {
			return new TripCandidateWithPrediction(prediction.getTravelTime(), "uam", prediction);
		}

		return new TripCandidateWithPrediction(prediction.getUtility(), "uam", prediction);

	}

}
