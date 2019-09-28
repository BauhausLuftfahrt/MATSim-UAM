package net.bhl.matsim.uam.qsim;

import java.util.Map;

import net.bhl.matsim.uam.infrastructure.UAMFlightSegments;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

public class UAMLinkSpeedCalculator implements LinkSpeedCalculator {

	final private Map<String, Double> mapVehicleVerticalSpeeds;
	final private Map<String, Double> mapVehicleHorizontalSpeeds;

	final private LinkSpeedCalculator delegate;
	final private double crossingPenalty;

	public UAMLinkSpeedCalculator(Map<String, Double> mapVehicleVerticalSpeeds,
			Map<String, Double> mapVehicleHorizontalSpeeds, LinkSpeedCalculator delegate, double crossingPenalty) {
		// TODO use mapping of vehicle types instead of vehicles themselves!
		this.mapVehicleVerticalSpeeds = mapVehicleVerticalSpeeds;
		this.mapVehicleHorizontalSpeeds = mapVehicleHorizontalSpeeds;

		this.delegate = delegate;
		this.crossingPenalty = crossingPenalty;
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

		boolean isMajor = true;

		for (Link other : link.getToNode().getInLinks().values()) {
			if (other.getCapacity() >= link.getCapacity())
				isMajor = false;
		}

		if (isMajor || link.getToNode().getInLinks().size() == 1) {
			return delegate.getMaximumVelocity(vehicle, link, time);
		} else {
			double travelTime = link.getLength() / delegate.getMaximumVelocity(vehicle, link, time);
			travelTime += crossingPenalty;
			return link.getLength() / travelTime;
		}
	}
}
