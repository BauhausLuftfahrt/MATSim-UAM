package net.bhl.matsim.uam.modechoice.estimation.other;

import ch.ethz.matsim.mode_choice.estimation.DefaultTripCandidate;
import ch.ethz.matsim.mode_choice.estimation.ModalTripEstimator;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;

import java.util.List;

/**
 * This class defines the estimator for car passenger trips.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomCarPassengerEstimator implements ModalTripEstimator {
	@Override
	public TripCandidate estimateTrip(ModeChoiceTrip trip, List<TripCandidate> preceedingTrips) {
		return new DefaultTripCandidate(0.0, "car_passenger");
	}
}
