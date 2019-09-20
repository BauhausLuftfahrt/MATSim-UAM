package net.bhl.matsim.uam.modechoice.estimation.other;

import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.core.population.PersonUtils;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import ch.ethz.matsim.mode_choice.prediction.TeleportationPrediction;
import ch.ethz.matsim.mode_choice.prediction.TeleportationPredictor;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;

/**
 * This class defines the estimator for Bike trips.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CustomBikeEstimator implements ModalTripEstimator {
	final private TeleportationPredictor predictor;
	final private CustomModeChoiceParameters parameters;
	private static final Logger log = Logger.getLogger(CustomBikeEstimator.class);
	private static int counterWarning = 0;
	private boolean isMinTravelTime;

	public CustomBikeEstimator(CustomModeChoiceParameters parameters, TeleportationPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	public CustomBikeEstimator(CustomModeChoiceParameters parameters, TeleportationPredictor predictor,
			boolean isMinTravelTime) {
		this(parameters, predictor);
		this.isMinTravelTime = isMinTravelTime;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		TeleportationPrediction prediction = predictor.predict(trip);

		// In case of standard simulation, returns travel time instead of utility.
		if (isMinTravelTime) {
			return new TripCandidateWithPrediction(prediction.travelTime, "bike", prediction);
		}

		double travelTime_min = prediction.travelTime / 60.0;

		double age = parameters.averageAge;
		try {
			age = PersonUtils.getAge(trip.getPerson());
		} catch (NullPointerException e) {
			// use default age
			if (CustomBikeEstimator.counterWarning <= 10) {
				log.warn("Age is missing for Person: " + trip.getPerson().getId() + ". Using default average age of "
						+ age + ".");
				if (CustomBikeEstimator.counterWarning == 10)
					log.warn("Additional warnings of this type will not be reported!");
				CustomBikeEstimator.counterWarning++;

			}
		}

		double utility = 0.0;
		utility += parameters.alphaBike;
		utility += parameters.betaTravelTimeBike_min * travelTime_min;
		utility += parameters.betaAgeBike_yr * Math.max(0.0, age - 18.0);

		return new TripCandidateWithPrediction(utility, "bike", prediction);
	}
}
