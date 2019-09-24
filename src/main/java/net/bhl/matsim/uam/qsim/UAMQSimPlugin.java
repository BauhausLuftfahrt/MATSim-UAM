package net.bhl.matsim.uam.qsim;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;

import com.google.inject.Module;

import net.bhl.matsim.uam.dispatcher.UAMDispatcherListener;
import net.bhl.matsim.uam.schedule.UAMOptimizer;

/**
 * This class provides Qsim resources to the UAMModule.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMQSimPlugin extends AbstractQSimPlugin {

	public UAMQSimPlugin(Config config) {
		super(config);
	}

	public Collection<? extends Module> modules() {
		return Collections.singleton(new UAMQsimModule());
	}

	public Collection<Class<? extends MobsimEngine>> engines() {
		return Collections.singleton(PassengerEngine.class);
	}

	public Collection<Class<? extends MobsimListener>> listeners() {
		return Arrays.asList(UAMOptimizer.class, UAMDispatcherListener.class);
	}

	public Collection<Class<? extends AgentSource>> agentSources() {
		return Collections.singleton(VrpAgentSource.class);
	}

	public Collection<Class<? extends DepartureHandler>> departureHandlers() {
		return Arrays.asList(PassengerEngine.class, UAMDepartureHandler.class);
	}

}
