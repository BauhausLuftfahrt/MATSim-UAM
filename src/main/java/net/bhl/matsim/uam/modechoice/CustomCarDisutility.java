package net.bhl.matsim.uam.modechoice;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleUtils;

public class CustomCarDisutility implements TravelDisutility {
	final private static QVehicle qVehicle = new QVehicle(
			new VehicleImpl(Id.createVehicleId("calculation"), VehicleUtils.getDefaultVehicleType()));

	final private LinkSpeedCalculator speedCalculator;
	final private TravelTime travelTimeCalculator;

	public CustomCarDisutility(LinkSpeedCalculator speedCalculator, TravelTime travelTimeCalculator) {
		this.speedCalculator = speedCalculator;
		this.travelTimeCalculator = travelTimeCalculator;
	}

	@Override
	public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
		double estimatedTravelTime = travelTimeCalculator.getLinkTravelTime(link, time, person, vehicle);

		double adjustedFreeflowTravelTime = link.getLength() / speedCalculator.getMaximumVelocity(qVehicle, link, time);
		adjustedFreeflowTravelTime = Math.floor(adjustedFreeflowTravelTime) + 1.0;

		if (estimatedTravelTime < adjustedFreeflowTravelTime) {
			return adjustedFreeflowTravelTime;
		} else {
			return estimatedTravelTime;
		}
	}

	@Override
	public double getLinkMinimumTravelDisutility(Link link) {
		return link.getLength() / link.getFreespeed();
	}

	static public class Factory implements TravelDisutilityFactory {
		final private LinkSpeedCalculator speedCalculator;

		public Factory(LinkSpeedCalculator speedCalculator) {
			this.speedCalculator = speedCalculator;
		}

		@Override
		public TravelDisutility createTravelDisutility(TravelTime travelTimeCalculator) {
			return new CustomCarDisutility(speedCalculator, travelTimeCalculator);
		}
	}
}
