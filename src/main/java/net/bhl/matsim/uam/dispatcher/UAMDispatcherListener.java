package net.bhl.matsim.uam.dispatcher;

import com.google.inject.Inject;

import net.bhl.matsim.uam.router.UAMModes;

import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.List;

/**
 * A listener for the UAMDispatchers to prepare for next simulation step.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMDispatcherListener implements MobsimBeforeSimStepListener {

	@Inject
	List<Dispatcher> dispatchers;

	@Override
	public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {

		for (Dispatcher d : dispatchers) {
			d.onNextTimeStep(e.getSimulationTime());
		}

	}

}
