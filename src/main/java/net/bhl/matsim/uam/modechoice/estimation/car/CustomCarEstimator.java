package net.bhl.matsim.uam.modechoice.estimation.car;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import org.matsim.api.core.v01.population.Person;

import java.util.List;

/**
 * This class defines the estimator for car trips.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomCarEstimator implements ModalTripEstimator {
	final private CustomCarPredictor predictor;
	final private CustomModeChoiceParameters parameters;
	private boolean isMinTravelTime;

	public CustomCarEstimator(CustomModeChoiceParameters parameters, CustomCarPredictor predictor) {
		this.predictor = predictor;
		this.parameters = parameters;
	}

	public CustomCarEstimator(CustomModeChoiceParameters parameters, CustomCarPredictor predictor,
							  boolean isMinTravelTime) {
		this(parameters, predictor);
		this.isMinTravelTime = isMinTravelTime;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		CustomCarPrediction prediction = predictor.predict(trip);

		// In case of standard simulation, returns travel time instead of utility.
		if (isMinTravelTime) {
			return new TripCandidateWithPrediction(prediction.travelTime, "car", prediction);
		}

		Person person = trip.getPerson();
		double travelTime_min = prediction.travelTime / 60.0;
		double distance_km = prediction.distance * 1e-3;
		int distance_short = distance_km > 2.0 ? 0 : 1;
		double cost = parameters.distanceCostCar_km * distance_km;
		double income = 0.0;

		if (person.getAttributes().getAttribute("income") != null)
			income = (double) trip.getPerson().getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;

		int residence = (int) person.getAttributes().getAttribute("residence");

		double betaCost = parameters.betaCost(distance_km, income);
		double utility = 0.0;

		boolean innerParisDes = (boolean) (trip.getTripInformation().getDestinationActivity().getAttributes()
				.getAttribute("innerParis"));
		boolean innerParisOrigin = (boolean) (trip.getTripInformation().getOriginActivity().getAttributes()
				.getAttribute("innerParis"));
		boolean home = trip.getTripInformation().getDestinationActivity().getType().equals("home");

		utility += parameters.alphaCar;

		utility += (parameters.betaTravelTimeCar_min
				+ parameters.betaTravelTImeCarMale * (person.getAttributes().getAttribute("sex").equals("m") ? 1 : 0))
				* (travelTime_min + parameters.parkingSearchTimeCar_min);
		utility += parameters.betaTravelTimeWalk_min * parameters.accessEgressWalkTimeCar_min;
		utility += parameters.betaShortCar * distance_short;
		if (!innerParisDes && !innerParisOrigin) {
			utility += parameters.betaResidenceRuralCar * (residence == 3 ? 1 : 0);
		}
		if (!innerParisDes) {
			utility += parameters.betaParking;
		} else if (residence == 1 && home)
			utility += parameters.betaParking / 2.0;
		if (innerParisDes && !home)
			utility += parameters.betaParkingCity * (innerParisDes ? 1 : 0);
		utility += betaCost * cost;

		return new TripCandidateWithPrediction(utility, "car", prediction);
	}
}
