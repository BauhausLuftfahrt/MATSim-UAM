package net.bhl.matsim.uam.qsim;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;

import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.LinkSpeedCalculator;

/**
 * A MATSim Abstract Module for classes used by UAM simulation regarding link
 * speeds in the simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMSpeedModule extends AbstractQSimModule {

	@Override
	public void configureQSim() {
		bind(LinkSpeedCalculator.class).to(UAMLinkSpeedCalculator.class);
	}

	@Provides
	@Singleton
	public UAMLinkSpeedCalculator provideUAMLinkSpeedCalculator(UAMXMLReader uamReader) {
		return new UAMLinkSpeedCalculator(uamReader.getMapVehicleVerticalSpeeds(), uamReader.getMapVehicleHorizontalSpeeds());
	}
}
