package net.bhl.matsim.uam.schedule;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.bhl.matsim.uam.data.UAMLoader;
import net.bhl.matsim.uam.dispatcher.Dispatcher;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;

import java.util.List;

/**
 * An optimizer for UAM vehicles schedule.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Singleton
public class UAMOptimizer implements VrpOptimizer,OnlineTrackerListener, MobsimBeforeSimStepListener {
	private double now;
	private static final Logger log = Logger.getLogger(UAMOptimizer.class);
	private Dispatcher dispatcher;
	@Inject
	private EventsManager eventsManager;

	@Override
	public void requestSubmitted(Request request) {
		UAMRequest uamRequest = (UAMRequest) request;
		dispatcher = uamRequest.getDispatcher();

		synchronized (dispatcher) {
			dispatcher.onRequestSubmitted(uamRequest);
		}
	}

	@Override
	public void nextTask(DvrpVehicle vehicle) {
	
		Schedule schedule = vehicle.getSchedule();
		// this happens at the start of the simulation since
		// the schedule has not started yet
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
			log.warn("Inside OPtimizer IF");
			schedule.nextTask();
			return;
		}
		// STARTED
		log.warn("Vehicle: " + vehicle.getId());
		log.warn("Optimzer Running NEXT TASK METHOD");
		log.warn("Schedule status: "+ String.valueOf(schedule.getStatus()));
		log.warn("Current task: "+ String.valueOf(schedule.getCurrentTask()));
		log.warn("Current task Id: "+ String.valueOf(schedule.getCurrentTask().getTaskIdx()));
		log.warn("Tasks size: "+ String.valueOf(schedule.getTasks().size()));
		
		for (Task task : schedule.getTasks()) {
			log.warn("task: " + String.valueOf(task));
		}
		
		// get the current task and make it end now
		Task currentTask = schedule.getCurrentTask();
		currentTask.setEndTime(now);

		List<? extends Task> tasks = schedule.getTasks();
		int index = currentTask.getTaskIdx() + 1;
		UAMTask nextTask = null;

		if (index < tasks.size()) {
			nextTask = (UAMTask) tasks.get(index);
		} else {
			throw new IllegalStateException("A UAM schedule should never end!");
		}

		double startTime = now;

		UAMTask indexTask;
		// we have to adapt the times of the rest of the tasks
		// in order to take into account any delays vehicle has experienced so far
		while (index < tasks.size()) {
			indexTask = (UAMTask) tasks.get(index);

			if (indexTask.getUAMTaskType() == UAMTask.UAMTaskType.STAY) {
				if (indexTask.getEndTime() < startTime)
					indexTask.setEndTime(startTime);
			} else {
				indexTask.setEndTime(indexTask.getEndTime() - indexTask.getBeginTime() + startTime);
			}

			indexTask.setBeginTime(startTime);
			startTime = indexTask.getEndTime();
			index++;
		}

		ensureNonFinishingSchedule(schedule);
		schedule.nextTask();
		ensureNonFinishingSchedule(schedule);

		// UAMDispatcher dispatcher = ((UAMVehicle) vehicle).getDispatcher();
		log.warn("Next task:" + String.valueOf(nextTask));
		if (nextTask != null) {
			synchronized (dispatcher) {
				dispatcher.onNextTaskStarted((UAMVehicle) vehicle);
			}
		}

		if (nextTask != null && nextTask instanceof UAMDropoffTask) {
			// throws a transit event in order to let us know that the drop off
			// has been performed
			// this is used later in the analysis to know which person was
			// dropped off
			processTransitEvent((UAMDropoffTask) nextTask);
		}
	}

	private void ensureNonFinishingSchedule(Schedule schedule) {
		UAMTask lastTask = (UAMTask) Schedules.getLastTask(schedule);

		if (lastTask.getUAMTaskType() != UAMTask.UAMTaskType.STAY) {
			throw new IllegalStateException("A UAM schedule should always end with a STAY task");
		}

		if (!Double.isInfinite(lastTask.getEndTime())) {
			throw new IllegalStateException("A UAM schedule should always end at time Infinity");
		}
	}

	@Override
	public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
		now = e.getSimulationTime();
	}

	private void processTransitEvent(UAMDropoffTask task) {
		for (UAMRequest request : task.getRequests()) {
			eventsManager.processEvent(new UAMTransitEvent(request, now));
		}
	}

	public void vehicleEnteredNextLink(DvrpVehicle vehicle, Link link) {
	}
}
