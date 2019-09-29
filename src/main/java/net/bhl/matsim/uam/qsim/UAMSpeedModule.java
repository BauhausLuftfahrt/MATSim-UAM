package net.bhl.matsim.uam.qsim;

import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.qnetsimengine.ConfigurableQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.linkspeedcalculator.DefaultLinkSpeedCalculator;

import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * A MATSim Abstract Module for classes used by UAM simulation regarding link
 * speeds in the simulation.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMSpeedModule extends AbstractModule {

	final private Map<String, Double> mapVehicleVerticalSpeeds;
	final private Map<String, Double> mapVehicleHorizontalSpeeds;

	public UAMSpeedModule(Map<String, Double> mapVehicleVerticalSpeeds,
			Map<String, Double> mapVehicleHorizontalSpeeds) {
		this.mapVehicleVerticalSpeeds = mapVehicleVerticalSpeeds;
		this.mapVehicleHorizontalSpeeds = mapVehicleHorizontalSpeeds;
	}

	@Override
	public void install() {

	}

	@Provides
	@Singleton
	public QNetworkFactory provideQNetworkFactory(EventsManager events, Scenario scenario,
			UAMLinkSpeedCalculator linkSpeedCalculator) {
		ConfigurableQNetworkFactory networkFactory = new ConfigurableQNetworkFactory(events, scenario);
		networkFactory.setLinkSpeedCalculator(linkSpeedCalculator);
		return networkFactory;
	}

	@Provides
	@Singleton
	public UAMLinkSpeedCalculator provideUAMLinkSpeedCalculator() {
		DefaultLinkSpeedCalculator delegate = new DefaultLinkSpeedCalculator();
		return new UAMLinkSpeedCalculator(mapVehicleVerticalSpeeds, mapVehicleHorizontalSpeeds, delegate);
	}
}
