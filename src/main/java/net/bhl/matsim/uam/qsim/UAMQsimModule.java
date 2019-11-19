package net.bhl.matsim.uam.qsim;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.data.UAMFleetData;
import net.bhl.matsim.uam.data.UAMLoader;
import net.bhl.matsim.uam.dispatcher.Dispatcher;
import net.bhl.matsim.uam.dispatcher.UAMDispatcherListener;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.dispatcher.UAMPooledDispatcher;
import net.bhl.matsim.uam.passenger.UAMRequestCreator;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.schedule.UAMOptimizer;
import net.bhl.matsim.uam.schedule.UAMSingleRideAppender;
import net.bhl.matsim.uam.vrpagent.UAMActionCreator;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerEngineQSimModule;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic.DynActionCreator;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSourceQSimModule;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.run.DynActivityEngineModule;
import org.matsim.core.mobsim.qsim.PopulationModule;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.TeleportationModule;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.vehicles.VehicleType;

import java.util.ArrayList;
import java.util.List;

/**
 * A MATSim Abstract Module for classes used by Qsim for UAM simulation.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMQsimModule extends AbstractDvrpModeQSimModule {
	public final static String COMPONENT_NAME = "UAMExtension";
	// private final QSim qsim;
	// private final UAMConfigGroup uamConfigGroup;

	public UAMQsimModule() {
		super(UAMModes.UAM_MODE);
	}

	@Override
	protected void configureQSim() {
		//install(new VrpAgentSourceQSimModule(getMode()));
		install(new PassengerEngineQSimModule(getMode()));
		
		bindModal(PassengerRequestCreator.class).to(UAMRequestCreator.class);
		//bind(UAMRequestCreator.class);
		bindModal(DynActionCreator.class).to(UAMActionCreator.class);
		//bind(UAMActionCreator.class);
		bindModal(VrpOptimizer.class).to(UAMOptimizer.class);
//		bindModal(UAMLoader.class).to(UAMLoader.class);
		
		bind(UAMOptimizer.class);
		bind(UAMDispatcherListener.class);
//		bind(UAMLoader.class);
	
		bindModal(UAMDispatcherListener.class).to(UAMDispatcherListener.class);
		bindModal(Fleet.class).to(UAMFleetData.class);
		
//		bindModal(UAMSingleRideAppender.class).to(UAMSingleRideAppender.class);
		bind(UAMSingleRideAppender.class);
//		bindModal(UAMDepartureHandler.class).to(UAMDepartureHandler.class);
		bind(UAMDepartureHandler.class);
		
		bindModal(DepartureHandler.class).to(UAMDepartureHandler.class);
		addModalQSimComponentBinding().to(UAMDispatcherListener.class);
		addModalQSimComponentBinding().to(UAMOptimizer.class);
		addModalQSimComponentBinding().to(VrpAgentSource.class);
		addModalQSimComponentBinding().to(UAMDepartureHandler.class);
		
	}
	
	public static void configureComponents(QSimComponentsConfig components) {
		DynActivityEngineModule.configureComponents(components); //THIS HAS TO BE USED WHEN RUNNNIN IN EQASIM
		
		// components.addNamedComponent(COMPONENT_NAME);
		// components.addNamedComponent(AVModule.AV_MODE);
//		components.addNamedComponent(TeleportationModule.COMPONENT_NAME);
//		components.addNamedComponent(DynActivityEngineModule.COMPONENT_NAME);
		components.addComponent(DvrpModes.mode(UAMModes.UAM_MODE));
	}
	
	

	/*
	 * @Provides
	 * 
	 * @Singleton public PassengerEngine providePassengerEngine(EventsManager
	 * events, UAMRequestCreator requestCreator, UAMOptimizer
	 * optimizer, @Named("uam") Network network) { return new PassengerEngine("uam",
	 * events, requestCreator, optimizer, network); }
	 */

	@Provides
	@Singleton
	VrpLegFactory provideLegCreator(@DvrpMode(UAMModes.UAM_MODE) VrpOptimizer optimizer, QSim qSim) {
		return new VrpLegFactory() {
			@Override
			public VrpLeg create(DvrpVehicle vehicle) {
				return VrpLegFactory.createWithOnlineTracker(TransportMode.car, vehicle, (OnlineTrackerListener) optimizer, qSim.getSimTimer());
			}
		};
	}

	@Provides
	@Singleton
	public VrpAgentSource provideAgentSource(@DvrpMode(UAMModes.UAM_MODE) DynActionCreator actionCreator,@DvrpMode(UAMModes.UAM_MODE) Fleet data, @DvrpMode(UAMModes.UAM_MODE) VrpOptimizer optimizer,
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
	
	
	  @Provides	  
	  @Singleton public UAMFleetData provideData(UAMManager uamManager) {
	  UAMFleetData data = new UAMFleetData();
	  
	  for (DvrpVehicle veh : uamManager.getVehicles().values())
	  data.addVehicle(veh); return data; }
	 


}
