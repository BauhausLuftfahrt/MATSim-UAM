package net.bhl.matsim.uam.qsim;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.vehicles.VehicleType;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.bhl.matsim.uam.data.UAMFleetData;
import net.bhl.matsim.uam.dispatcher.Dispatcher;
import net.bhl.matsim.uam.dispatcher.UAMDispatcherListener;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.dispatcher.UAMPooledDispatcher;
import net.bhl.matsim.uam.passenger.UAMRequestCreator;
import net.bhl.matsim.uam.schedule.UAMSingleRideAppender;
import net.bhl.matsim.uam.schedule.UAMOptimizer;
import net.bhl.matsim.uam.vrpagent.UAMActionCreator;

/**
 * A MATSim Abstract Module for classes used by Qsim for UAM simulation.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMQsimModule extends AbstractModule {

	// private final QSim qsim;
	// private final UAMConfigGroup uamConfigGroup;

	@Override
	protected void configure() {
		bind(UAMRequestCreator.class);
		bind(UAMOptimizer.class);
		bind(UAMActionCreator.class);
		bind(UAMSingleRideAppender.class);
		bind(UAMDepartureHandler.class);
		bind(UAMDispatcherListener.class);

	}

	@Provides
	@Singleton
	public PassengerEngine providePassengerEngine(EventsManager events, UAMRequestCreator requestCreator,
			UAMOptimizer optimizer, @Named("uam") Network network) {
		return new PassengerEngine("uam", events, requestCreator, optimizer, network);
	}

	@Provides
	@Singleton
	VrpLegs.LegCreator provideLegCreator(UAMOptimizer avOptimizer, QSim qSim) {
		return VrpLegs.createLegWithOnlineTrackerCreator(avOptimizer, qSim.getSimTimer());
	}

	@Provides
	@Singleton
	public VrpAgentSource provideAgentSource(UAMActionCreator actionCreator, UAMFleetData data, UAMOptimizer optimizer,
			@Named("uam") VehicleType vehicleType, QSim qSim) {
		return new VrpAgentSource(actionCreator, data, optimizer, qSim, vehicleType);
	}

	@Provides
	@Singleton
	List<Dispatcher> provideDispatchers(UAMSingleRideAppender appender, UAMManager uamManager,
			@Named("uam") Network network) {

		Dispatcher dispatcher = new UAMPooledDispatcher(appender, uamManager, network);

		List<Dispatcher> dispatchers = new ArrayList<>();
		dispatchers.add(dispatcher);

		return dispatchers;

	}

}
