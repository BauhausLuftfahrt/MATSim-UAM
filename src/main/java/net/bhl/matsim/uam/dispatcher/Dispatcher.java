package net.bhl.matsim.uam.dispatcher;

import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;

public interface Dispatcher {

	void onRequestSubmitted(UAMRequest request);

	void onNextTaskStarted(UAMVehicle vehicle);

	void onNextTimeStep(double simulationTime);

}