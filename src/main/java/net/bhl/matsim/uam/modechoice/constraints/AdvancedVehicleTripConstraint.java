package net.bhl.matsim.uam.modechoice.constraints;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;
import net.bhl.matsim.uam.modechoice.utils.VehicleLocationUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.Collection;
import java.util.List;

/**
 * This class ensures that the vehicle needs to be returned home if it was taken
 * in the first place. However, it must be noted that if the person start a tour
 * with a certain vehicle it needs to bring it back at the end of the tour. This
 * means that if the person has two consecutive tours h-w-h it will not be
 * allowed to take car to work go back home by pt go with pt again to work and
 * take a car back home. The agent will be forced to being the car back home in
 * the first tour. This was done in order to have easier implementation. If this
 * has an effect on the results is not clear.
 *
 * @author balacmi (Milos Balac), sebhoerl (Sebastian Hörl)
 */
public class AdvancedVehicleTripConstraint extends VehicleTripConstraint {
	private boolean modeWasEnforced;

	public AdvancedVehicleTripConstraint(List<ModeChoiceTrip> trips, Collection<String> constrainedModes,
										 Id<Link> homeLinkId) {
		super(trips, constrainedModes, homeLinkId);
	}

	@Override
	public boolean validateBeforeEstimation(ModeChoiceTrip trip, String mode, List<String> previousModes) {
		modeWasEnforced = false;

		if (super.validateBeforeEstimation(trip, mode, previousModes)) {
			for (String constrainedMode : constrainedModes) {
				Id<Link> currentVehicleLinkId = getCurrentVehicleLinkId(constrainedMode, previousModes);
				Id<Link> currentDepartureLinkId = VehicleLocationUtils.getOriginLinkId(trip.getTripInformation());
				boolean isVehiclePresent = currentDepartureLinkId.equals(currentVehicleLinkId);

				if (isVehiclePresent && !currentDepartureLinkId.equals(homeLinkId)
						&& !willReturnBeforeHome(currentDepartureLinkId, trip)) {
					// We enforce the constrained mode, because otherwise the vehicle cannot return
					// home
					modeWasEnforced = true;
					return mode.equals(constrainedMode);
				}
			}

			return true;
		}

		return false;
	}

	public boolean getModeWasEnforced() {
		return modeWasEnforced;
	}

	/**
	 * @param currentDepartureLinkId Current departure link
	 * @param trip                   Trip
	 * @return True if the agent will return to this location before going home,
	 * otherwise false.
	 */
	private boolean willReturnBeforeHome(Id<Link> currentDepartureLinkId, ModeChoiceTrip trip) {
		int indexOfTrip = this.trips.indexOf(trip);

		for (ModeChoiceTrip futureTrip : trips.subList(indexOfTrip + 1, trips.size())) {
			Id<Link> futureLinkId = VehicleLocationUtils.getOriginLinkId(futureTrip.getTripInformation());

			if (futureLinkId.equals(currentDepartureLinkId)) {
				return true;
			}

			if (futureLinkId.equals(homeLinkId)) {
				return false;
			}
		}

		return false;
	}

	public static class Factory implements TripConstraintFactory {
		final private Collection<String> constrainedModes;

		public Factory(Collection<String> constrainedModes) {
			this.constrainedModes = constrainedModes;
		}

		@Override
		public AdvancedVehicleTripConstraint createConstraint(List<ModeChoiceTrip> trips,
															  Collection<String> availableModes) {
			Id<Link> homeLinkId = VehicleLocationUtils.getHomeLinkId(trips);
			return new AdvancedVehicleTripConstraint(trips, constrainedModes, homeLinkId);
		}
	}
}
