package net.bhl.matsim.uam.modechoice.utils;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.TripStructureUtils.Trip;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;

/**
 * This class provides methods regarding vehicle location in a trip.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class VehicleLocationUtils {
	final private static Id<Link> outsideLinkId = Id.createLinkId("outside");
	
	static public Id<Link> getOriginLinkId(Trip trip) {
		if (trip.getOriginActivity().getType().equals("outside")) {
			return outsideLinkId;
		} else {
			return trip.getOriginActivity().getLinkId();
		}
	}

	static public Id<Link> getDestinationLinkId(Trip trip) {
		if (trip.getDestinationActivity().getType().equals("outside")) {
			return outsideLinkId;
		} else {
			return trip.getDestinationActivity().getLinkId();
		}
	}

	static public Id<Link> getHomeLinkId(List<ModeChoiceTrip> trips) {
		Id<Link> homeLinkId = outsideLinkId;

		for (ModeChoiceTrip trip : trips) {
			Trip tripInformation = trip.getTripInformation();

			if (tripInformation.getOriginActivity().getType().contains("home")) {
				homeLinkId = tripInformation.getOriginActivity().getLinkId();
				break;
			}

			if (tripInformation.getDestinationActivity().getType().contains("home")) {
				homeLinkId = tripInformation.getDestinationActivity().getLinkId();
				break;
			}
		}

		return homeLinkId;
	}
}
