package net.bhl.matsim.uam.modechoice.constraints;

import java.util.Collection;
import java.util.List;

import ch.ethz.matsim.mode_choice.constraints.AbstractTripConstraint;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraint;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;

/**
 * This class ensures that a trip has car passenger as mode.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class CarPassangerTripConstraint extends AbstractTripConstraint {
	@Override
	public boolean validateBeforeEstimation(ModeChoiceTrip trip, String mode, List<String> previousModes) {
		if (trip.getInitialMode().equals("car_passenger")) {
			return mode.equals("car_passenger");
		} else if (mode.equals("car_passenger")) {
			return trip.getInitialMode().equals("car_passenger");
		}

		return true;
	}

	static public class Factory implements TripConstraintFactory {
		@Override
		public TripConstraint createConstraint(List<ModeChoiceTrip> trips, Collection<String> availableModes) {
			return new CarPassangerTripConstraint();
		}
	}
}
