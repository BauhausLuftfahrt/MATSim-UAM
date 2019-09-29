package net.bhl.matsim.uam.modechoice.estimation.pt;

import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionFinder;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionInformation;
import org.matsim.api.core.v01.population.Person;

import java.util.List;

public class CustomPublicTransportEstimator implements ModalTripEstimator {
	final private CustomPublicTransportPredictor predictor;
	final private CustomModeChoiceParameters parameters;
	final private SubscriptionFinder subscriptionFinder;
	private boolean isMinTravelTime;

	public CustomPublicTransportEstimator(CustomModeChoiceParameters parameters,
										  CustomPublicTransportPredictor predictor, SubscriptionFinder subscriptionFinder) {
		this.predictor = predictor;
		this.parameters = parameters;
		this.subscriptionFinder = subscriptionFinder;
	}

	public CustomPublicTransportEstimator(CustomModeChoiceParameters parameters,
										  CustomPublicTransportPredictor predictor, SubscriptionFinder subscriptionFinder, boolean isMinTravelTime) {
		this(parameters, predictor, subscriptionFinder);
		this.isMinTravelTime = isMinTravelTime;
	}

	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		CustomPublicTransportPrediction prediction = predictor.predict(trip);

		// In case of standard simulation, returns travel time instead of utility.
		if (isMinTravelTime) {
			double totalTravelTime = prediction.inVehicleTime + prediction.transferTime + prediction.accessEgressTime;
			return new TripCandidateWithPrediction(totalTravelTime, "pt", prediction);
		}

		Person person = trip.getPerson();
		SubscriptionInformation subscriptions = subscriptionFinder.getSubscriptions(trip.getPerson());

		double crowflyDistance_km = prediction.crowflyDistance * 1e-3;
		double inVehicleTime_min = prediction.inVehicleTime / 60.0;
		double transferTime_min = prediction.transferTime / 60.0;
		double accessEgressTime_min = prediction.accessEgressTime / 60.0;
		double inVehicleDistance_km = prediction.inVehicleDistance * 1e-3;

		double cost = parameters.costPublicTransport(subscriptions, crowflyDistance_km);
		double income = 0.0;

		int shortDistance = crowflyDistance_km < 2.0 ? 1 : 0;
		int longDistance = crowflyDistance_km > 6.0 ? 1 : 0;

		if (trip.getPerson().getAttributes().getAttribute("income") != null)
			income = (double) trip.getPerson().getAttributes().getAttribute("income");
		else
			income = parameters.averageIncome;

		int residence = 0;
		if (trip.getPerson().getAttributes().getAttribute("residence") != null)
			residence = (int) person.getAttributes().getAttribute("residence");

		double betaCost = parameters.betaCost(inVehicleDistance_km, income);
		double utility = 0.0;
		utility += parameters.alphaPublicTransport;
		utility += parameters.betaNumberOfTransfersPublicTransport * prediction.numberOfTransfers;
		utility += parameters.betaShortPublicTransport * shortDistance;
		utility += parameters.betaLongPublicTransport * longDistance;
		utility += parameters.betaInVehicleTimePublicTransport_min * inVehicleTime_min;
		utility += parameters.betaTransferTimePublicTransport_min * transferTime_min;
		utility += parameters.betaAccessEgressTimePublicTransport_min * accessEgressTime_min;
		utility += parameters.betaResidenceRuralPublicTransport * (residence == 3 ? 1 : 0);
		utility += betaCost * cost;

		return new TripCandidateWithPrediction(utility, "pt", prediction);
	}
}
