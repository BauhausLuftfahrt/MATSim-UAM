package net.bhl.matsim.uam.modechoice.constraints;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;

import ch.ethz.matsim.mode_choice.constraints.AbstractTripConstraint;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;

/**
 * This class ensures that the trip as a distance higher than a pre-defined
 * short distance.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class ShortDistanceConstraint extends AbstractTripConstraint {
	final public static double DEFAULT_SHORT_DISTANCE = 0.0;
	final private double shortDistance;

	private ShortDistanceConstraint(double shortDistance) {
		this.shortDistance = shortDistance;
	}

	@Override
	public boolean validateBeforeEstimation(ModeChoiceTrip trip, String mode, List<String> previousModes) {
		if (mode.equals("car") || mode.equals("pt") || mode.equals("bike")) {
			Coord originCoord = trip.getTripInformation().getOriginActivity().getCoord();
			Coord destinationCoord = trip.getTripInformation().getDestinationActivity().getCoord();

			return CoordUtils.calcEuclideanDistance(originCoord, destinationCoord) > shortDistance;
		}

		return true;
	}

	static public class Factory implements TripConstraintFactory {
		final private double shortDistance;

		public Factory(double shortDistance) {
			this.shortDistance = shortDistance;
		}

		public ShortDistanceConstraint createConstraint(List<ModeChoiceTrip> trips, Collection<String> availableModes) {
			return new ShortDistanceConstraint(shortDistance);
		}
	}
}
