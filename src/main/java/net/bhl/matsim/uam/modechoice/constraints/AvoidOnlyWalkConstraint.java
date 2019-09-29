package net.bhl.matsim.uam.modechoice.constraints;

import java.util.Collection;
import java.util.List;

import ch.ethz.matsim.mode_choice.constraints.AbstractTripConstraint;
import ch.ethz.matsim.mode_choice.estimation.TripCandidateWithPrediction;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraint;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;
import ch.ethz.matsim.mode_choice.framework.trip_based.estimation.TripCandidate;
import net.bhl.matsim.uam.modechoice.estimation.pt.CustomPublicTransportPrediction;

public class AvoidOnlyWalkConstraint extends AbstractTripConstraint {
	@Override
	public boolean validateAfterEstimation(ModeChoiceTrip trip, TripCandidate candidate,
			List<TripCandidate> previousCandidates) {
		if (candidate.getMode().equals("pt")) {
			CustomPublicTransportPrediction prediction = (CustomPublicTransportPrediction) ((TripCandidateWithPrediction) candidate)
					.getPrediction();

			return !prediction.isOnlyWalk;
		}

		return true;
	}

	static public class Factory implements TripConstraintFactory {
		@Override
		public TripConstraint createConstraint(List<ModeChoiceTrip> trips, Collection<String> availableModes) {
			return new AvoidOnlyWalkConstraint();
		}
	}
}
