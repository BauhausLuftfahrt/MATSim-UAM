package net.bhl.matsim.uam.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.schedule.UAMSingleRideAppender;
import net.bhl.matsim.uam.schedule.UAMTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import java.util.LinkedList;
import java.util.Queue;

/**
 * UAM Dispatcher that selects the first vehicle in the queue.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
@Singleton
public class UAMRandomDispatcher implements UAMDispatcher {

	@Inject
	final private UAMSingleRideAppender appender;

	final private Queue<UAMVehicle> availableVehicles = new LinkedList<>();
	final private Queue<UAMRequest> pendingRequests = new LinkedList<>();
	private boolean reoptimize = false;

	@Inject
	public UAMRandomDispatcher(UAMSingleRideAppender appender, UAMManager uamManager) {
		this.appender = appender;
		this.appender.setStations(uamManager.getStations());

		for (DvrpVehicle veh : uamManager.getVehicles().values()) {
			this.availableVehicles.add((UAMVehicle) veh);
		}
	}

	@Override
	public void onNextTimeStep(double now) {
		appender.update();
		if (reoptimize)
			reoptimize(now);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bhl.matsim.uam.dispatcher.Dispatcher#onRequestSubmitted(net.bhl.
	 * matsim.uam.passanger.UAMRequest)
	 */
	@Override
	public void onRequestSubmitted(UAMRequest request) {
		pendingRequests.add(request);
		reoptimize = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.bhl.matsim.uam.dispatcher.Dispatcher#onNextTaskStarted(net.bhl.matsim
	 * .uam.infrastructure.UAMVehicle)
	 */
	@Override
	public void onNextTaskStarted(UAMVehicle vehicle) {
		UAMTask task = (UAMTask) vehicle.getSchedule().getCurrentTask();
		if (task.getUAMTaskType() == UAMTask.UAMTaskType.STAY) {
			availableVehicles.add(vehicle);
		}
	}

	/**
	 * @param now current time
	 *            <p>
	 *            Method that dispatches a first vehicle in the Queue - no
	 *            optimization.
	 */
	private void reoptimize(double now) {

		while (availableVehicles.size() > 0 && pendingRequests.size() > 0) {
			UAMVehicle vehicle = availableVehicles.poll();
			UAMRequest request = pendingRequests.poll();
			appender.schedule(request, vehicle, now);
		}

		reoptimize = false;
	}

}
