package net.bhl.matsim.uam.modechoice.constraints;

import ch.ethz.matsim.mode_choice.constraints.AbstractTripConstraint;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraint;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;

import java.util.Collection;
import java.util.List;

/**
 * This class is a hybrid constraint including the
 * {@link AdvancedVehicleTripConstraint} and {@link ShortDistanceConstraint}.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomHybridConstraint extends AbstractTripConstraint {
	final private AdvancedVehicleTripConstraint vehicleConstraint;
	final private TripConstraint shortDistanceConstraint;

	public CustomHybridConstraint(AdvancedVehicleTripConstraint vehicleConstraint,
								  TripConstraint shortDistanceConstraint) {
		this.vehicleConstraint = vehicleConstraint;
		this.shortDistanceConstraint = shortDistanceConstraint;
	}

	@Override
	public boolean validateBeforeEstimation(ModeChoiceTrip trip, String mode, List<String> previousModes) {
		if (!vehicleConstraint.validateBeforeEstimation(trip, mode, previousModes)) {
			return false;
		}

		return vehicleConstraint.getModeWasEnforced()
				|| shortDistanceConstraint.validateBeforeEstimation(trip, mode, previousModes);
	}

	public static class Factory implements TripConstraintFactory {
		final private AdvancedVehicleTripConstraint.Factory vehicleConstraintFactory;
		final private TripConstraintFactory shortDistanceConstraintFactory;

		public Factory(AdvancedVehicleTripConstraint.Factory vehicleConstraintFactory,
					   TripConstraintFactory shortDistanceConstraintFactory) {
			this.vehicleConstraintFactory = vehicleConstraintFactory;
			this.shortDistanceConstraintFactory = shortDistanceConstraintFactory;
		}

		@Override
		public TripConstraint createConstraint(List<ModeChoiceTrip> trips, Collection<String> availableModes) {
			return new CustomHybridConstraint(vehicleConstraintFactory.createConstraint(trips, availableModes),
					shortDistanceConstraintFactory.createConstraint(trips, availableModes));
		}
	}
}
