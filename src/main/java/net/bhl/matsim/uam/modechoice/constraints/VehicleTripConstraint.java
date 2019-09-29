package net.bhl.matsim.uam.modechoice.constraints;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.mode_choice.constraints.AbstractTripConstraint;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraint;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;
import net.bhl.matsim.uam.modechoice.utils.VehicleLocationUtils;

public class VehicleTripConstraint extends AbstractTripConstraint {
	final protected Collection<String> constrainedModes;
	final protected List<ModeChoiceTrip> trips;
	final protected Id<Link> homeLinkId;

	public VehicleTripConstraint(List<ModeChoiceTrip> trips, Collection<String> constrainedModes, Id<Link> homeLinkId) {
		this.trips = trips;
		this.constrainedModes = constrainedModes;
		this.homeLinkId = homeLinkId;
	}

	protected Id<Link> getCurrentVehicleLinkId(String mode, List<String> previousModes) {
		int currentVehicleIndex = previousModes.lastIndexOf(mode);
		Id<Link> currentVehicleLinkId = homeLinkId;

		if (currentVehicleIndex > -1) {
			currentVehicleLinkId = VehicleLocationUtils
					.getDestinationLinkId(trips.get(currentVehicleIndex).getTripInformation());
		}

		return currentVehicleLinkId;
	}

	@Override
	public boolean validateBeforeEstimation(ModeChoiceTrip trip, String mode, List<String> previousModes) {
		if (constrainedModes.contains(mode)) {
			Id<Link> currentVehicleLinkId = getCurrentVehicleLinkId(mode, previousModes);
			Id<Link> currentDepartureLinkId = VehicleLocationUtils.getOriginLinkId(trip.getTripInformation());

			return currentDepartureLinkId.equals(currentVehicleLinkId);
		}

		return true;
	}

	public static class Factory implements TripConstraintFactory {
		final private Collection<String> constrainedModes;

		public Factory(Collection<String> constrainedModes) {
			this.constrainedModes = constrainedModes;
		}

		@Override
		public TripConstraint createConstraint(List<ModeChoiceTrip> trips, Collection<String> availableModes) {
			Id<Link> homeLinkId = VehicleLocationUtils.getHomeLinkId(trips);
			return new VehicleTripConstraint(trips, constrainedModes, homeLinkId);
		}
	}
}
