package net.bhl.matsim.uam.qsim;

import net.bhl.matsim.uam.router.UAMFlightSegments;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

import java.util.Map;

/**
 * This class defines the LinkSpeedCalculator for UAM simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMLinkSpeedCalculator implements LinkSpeedCalculator {

	final private Map<String, Double> mapVehicleVerticalSpeeds;
	final private Map<String, Double> mapVehicleHorizontalSpeeds;

	final private LinkSpeedCalculator delegate;

	public UAMLinkSpeedCalculator(Map<String, Double> mapVehicleVerticalSpeeds,
								  Map<String, Double> mapVehicleHorizontalSpeeds, LinkSpeedCalculator delegate) {
		// TODO: Use mapping of vehicle types instead of vehicles themselves.
		this.mapVehicleVerticalSpeeds = mapVehicleVerticalSpeeds;
		this.mapVehicleHorizontalSpeeds = mapVehicleHorizontalSpeeds;

		this.delegate = delegate;
	}

	@Override
	public double getMaximumVelocity(QVehicle vehicle, Link link, double time) {
		try {
			String flightSegment = (String) link.getAttributes().getAttribute(UAMFlightSegments.ATTRIBUTE);

			if (flightSegment.equals(UAMFlightSegments.HORIZONTAL))
				return Math.min(link.getFreespeed(), this.mapVehicleHorizontalSpeeds.get(vehicle.getId().toString()));

			if (flightSegment.equals(UAMFlightSegments.VERTICAL))
				return Math.min(link.getFreespeed(), this.mapVehicleVerticalSpeeds.get(vehicle.getId().toString()));
		} catch (NullPointerException e) {
			// Non-flight link
		}

		return delegate.getMaximumVelocity(vehicle, link, time);
	}
}
