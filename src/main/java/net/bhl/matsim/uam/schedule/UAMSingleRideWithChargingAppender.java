package net.bhl.matsim.uam.schedule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.DefaultDriveTask;
import org.matsim.contrib.dvrp.schedule.DefaultStayTask;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.run.UAMConstants;

/**
 * This class adds tasks for each vehicle schedule based on the requests
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMSingleRideWithChargingAppender {
	@Inject
	@Named(UAMConstants.uam)
	private LeastCostPathCalculator uamPathCalculator;

	@Inject
	@Named(UAMConstants.uam)
	private TravelTime travelTime;

	@Inject
	private UAMStations uamStations;

	private List<AppendTask> tasks = new LinkedList<>();

	/**
	 * @param request UAM request
	 * @param vehicle UAM Vehicle
	 * @param now     simulation step now
	 *                <p>
	 *                This method generates the paths for pickup and drop-off and
	 *                create a new AppendTask containing this information. The new
	 *                AppendTask is added to the AppendTask list.
	 */
	public void schedule(UAMRequest request, UAMVehicle vehicle, double now, Id<UAMStation> chargingStationId) {
		tasks.add(new AppendTask(request, vehicle, now, chargingStationId)); // adds the task to the tasks list
	}

	// There is a difference between an AppendTask and a Task that implements the
	// Task interface.

	// Uses the AppendTasks from the list containing the paths to generate the Tasks
	// (Tasks that implements the Task interface) and add them in order to the
	// vehicle schedule
	public void schedule(AppendTask task) throws ExecutionException, InterruptedException {
		UAMRequest request = task.request;
		UAMVehicle vehicle = task.vehicle;
		double now = task.time;
		Schedule schedule = vehicle.getSchedule();

		StayTask stayTask = (StayTask) Schedules.getLastTask(schedule);
		boolean requiresPickupFlight = !stayTask.getLink().getId().equals(request.getFromLink().getId());

		double startTime = stayTask.getStatus() == Task.TaskStatus.STARTED ? now : stayTask.getBeginTime();
		double scheduleEndTime = schedule.getEndTime();

		Path pickupPath = uamPathCalculator.calcLeastCostPath(stayTask.getLink().getToNode(),
				request.getFromLink().getFromNode(), startTime, null, null);

		VrpPathWithTravelData pickupPathWithTravelData = VrpPaths.createPath(stayTask.getLink(), request.getFromLink(),
				startTime, pickupPath, travelTime);
		DriveTask pickupFlyTask = new DefaultDriveTask(UAMTaskType.FLY, pickupPathWithTravelData);

		double pickUpTaskStartTime = request.getEarliestStartTime();
		// For the case when the Aircraft is not already at the correct station:
		if (requiresPickupFlight)
			pickUpTaskStartTime = Math.max(pickUpTaskStartTime, pickupPathWithTravelData.getArrivalTime());

		double flyTaskStartTime = pickUpTaskStartTime + vehicle.getBoardingTime();
		UAMPickupTask pickupTask = new UAMPickupTask(pickUpTaskStartTime, flyTaskStartTime, request.getFromLink(),
				vehicle.getBoardingTime(), Collections.singletonList(request));

		Path dropoffPath = uamPathCalculator.calcLeastCostPath(request.getFromLink().getToNode(),
				request.getToLink().getFromNode(), flyTaskStartTime, null, null);

		VrpPathWithTravelData dropoffPathWithTravelData = VrpPaths.createPath(request.getFromLink(),
				request.getToLink(), flyTaskStartTime, dropoffPath, travelTime);
		DriveTask dropoffFlyTask = new DefaultDriveTask(UAMTaskType.FLY, dropoffPathWithTravelData);

		double dropOffStartTime = flyTaskStartTime + dropoffPathWithTravelData.getTravelTime();
		double tatStartTime = dropOffStartTime + vehicle.getDeboardingTime();
		UAMDropoffTask dropoffTask = new UAMDropoffTask(dropOffStartTime, tatStartTime, request.getToLink(),
				vehicle.getDeboardingTime(), Collections.singletonList(request));

		double tatEndTime = tatStartTime + vehicle.getTurnAroundTime();
		StayTask turnAroundTask = new DefaultStayTask(UAMTaskType.TURNAROUND, tatStartTime, tatEndTime, request.getToLink());

		double stayEndTime = pickUpTaskStartTime;
		if (requiresPickupFlight)
			stayEndTime = startTime;
		stayTask.setEndTime(stayEndTime);

		if (requiresPickupFlight) {
			schedule.addTask(pickupFlyTask);

			StayTask uamStayTask = new DefaultStayTask(UAMTaskType.STAY, pickupFlyTask.getEndTime(), pickUpTaskStartTime,
					pickupFlyTask.getPath().getToLink());
			schedule.addTask(uamStayTask);
		}

		schedule.addTask(pickupTask);
		schedule.addTask(dropoffFlyTask);
		schedule.addTask(dropoffTask);
		schedule.addTask(turnAroundTask);
		Link chargingLink = this.uamStations.getUAMStations().get(task.chargingStationId).getLocationLink();

		// check if we need to fly to the charging station or are we already there
		if (dropoffTask.getLink().getId().toString().equals(chargingLink.getId().toString())) {
			// we are already there
			UAMChargingTask chargingTask = new UAMChargingTask(UAMTaskType.CHARGING, turnAroundTask.getEndTime(),
					turnAroundTask.getEndTime() + 60.0, turnAroundTask.getLink(),
					this.uamStations.getNearestUAMStation(turnAroundTask.getLink()).getId());
			schedule.addTask(chargingTask);
		}
		else {
			// we are not there and we need to fly
			
			Path chargingPath = uamPathCalculator.calcLeastCostPath(dropoffTask.getLink().getToNode(),
					chargingLink.getFromNode(), turnAroundTask.getEndTime(), null, null);

			VrpPathWithTravelData chargingPathWithTravelData = VrpPaths.createPath(dropoffTask.getLink(), chargingLink,
					turnAroundTask.getEndTime(), chargingPath, travelTime);
			DriveTask chargingFlyTask = new DefaultDriveTask(UAMTaskType.FLY, chargingPathWithTravelData);
			schedule.addTask(chargingFlyTask);
			
			UAMChargingTask chargingTask = new UAMChargingTask(UAMTaskType.CHARGING, chargingFlyTask.getEndTime(),
					chargingFlyTask.getEndTime() + 60.0, chargingLink,
					this.uamStations.getNearestUAMStation(chargingLink).getId());
			schedule.addTask(chargingTask);
			
		}
		schedule.addTask(new DefaultStayTask(UAMTaskType.STAY, turnAroundTask.getEndTime() + 60, scheduleEndTime,
				turnAroundTask.getLink()));
	}

	public void update() {
		// TODO: This can be made more efficient if one knows which ones have
		// just been added and which ones are still
		// to be processed. Depends mainly on if "update" is called before new
		// tasks are submitted or after ...
		try {
			for (AppendTask task : tasks)
				schedule(task);
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		tasks.clear();
	}

	private class AppendTask {
		final public UAMRequest request;
		final public UAMVehicle vehicle;
		final public Id<UAMStation> chargingStationId;

		final public double time;

		public AppendTask(UAMRequest request, UAMVehicle vehicle, double time, Id<UAMStation> chargingStationId) {
			this.request = request;
			this.vehicle = vehicle;
			this.time = time;
			this.chargingStationId = chargingStationId;
		}
	}
}
