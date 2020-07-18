package net.bhl.matsim.uam.schedule;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class adds tasks for each vehicle schedule based on the requests
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMSingleRideAppender {
	@Inject
	@Named("uam")
	private ParallelLeastCostPathCalculator router;

	@Inject
	@Named("uam")
	private TravelTime travelTime;

	private List<AppendTask> tasks = new LinkedList<>();
	private UAMStations stations;

	/**
	 * @param request UAM request
	 * @param vehicle UAM Vehicle
	 * @param now     simulation step now
	 *                <p>
	 *                This method generates the paths for pickup and drop-off and
	 *                create a new AppendTask containing this information. The new
	 *                AppendTask is added to the AppendTask list.
	 */
	public void schedule(UAMRequest request, UAMVehicle vehicle, double now) {
		Schedule schedule = vehicle.getSchedule();
		UAMStayTask stayTask = (UAMStayTask) Schedules.getLastTask(schedule); // selects the last task in the schedule
		Future<Path> pickup = router.calcLeastCostPath(stayTask.getLink().getToNode(), // pickup path is from downstream
				// node of stayTask Link to
				// upstream node from request
				// origin Link.
				request.getFromLink().getFromNode(), now, null, null);
		Future<Path> dropoff = router.calcLeastCostPath(request.getFromLink().getToNode(), // dropoff path is from
				// downstream node of
				// request origin link to
				// upstream node from
				// request destination Link.
				request.getToLink().getFromNode(), now, null, null);

		tasks.add(new AppendTask(request, vehicle, now, pickup, dropoff)); // adds the task to the tasks list
	}

	// There is a difference between an AppendTask and a Task that implements the
	// Task interface.

	// Uses the AppendTasks from the list containing the paths to generate the Tasks
	// (Tasks that implements the Task interface) and add them in order to the
	// vehicle schedule
	public void schedule(AppendTask task, Path plainPickupPath, Path plainDropoffPath) {
		UAMRequest request = task.request;
		UAMVehicle vehicle = task.vehicle;
		double now = task.time;

		Schedule schedule = vehicle.getSchedule();

		UAMStayTask stayTask = (UAMStayTask) Schedules.getLastTask(schedule); // selects the last task in the schedule
		// and create a stayTask with it
		double startTime = 0.0;
		double scheduleEndTime = schedule.getEndTime();

		if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
			startTime = now;
		} else {
			startTime = stayTask.getBeginTime();
		}
		UAMStation stationDestination = stations.getNearestUAMStation(request.getToLink());
		VrpPathWithTravelData pickupPath = VrpPaths.createPath(stayTask.getLink(), request.getFromLink(), startTime,
				plainPickupPath, travelTime);
		VrpPathWithTravelData dropoffPath = VrpPaths.createPath(request.getFromLink(), request.getToLink(),
				pickupPath.getArrivalTime() + vehicle.getBoardingTime(), plainDropoffPath, travelTime); // departure
		// time =
		// arrival time
		// + boarding
		// time

		UAMFlyTask pickupDriveTask = new UAMFlyTask(pickupPath); // Vehicle flies to pick up the passenger
		double pickUpTaskStartTime = startTime;
		//For the case when the Aircraft is already at the station there will be no pickUpDriveTask
		if (!stayTask.getLink().getId().equals(request.getFromLink().getId())) {
			pickUpTaskStartTime = pickupPath.getArrivalTime();
		}

		UAMPickupTask pickupTask = new UAMPickupTask(pickUpTaskStartTime, // Vehicle picks up the passenger at
				// the station
				pickUpTaskStartTime + vehicle.getBoardingTime(), // end time = arrival time + boarding time
				request.getFromLink(), vehicle.getBoardingTime(), Arrays.asList(request));

		UAMFlyTask dropoffDriveTask = new UAMFlyTask(dropoffPath, Arrays.asList(request)); // Vehicle flies to drop off
		// the passenger
		UAMDropoffTask dropoffTask = new UAMDropoffTask(dropoffPath.getArrivalTime(), // Vehicle drops off the passenger
				// at the station
				dropoffPath.getArrivalTime() + vehicle.getDeboardingTime(), // Dropoff task lasts according to the
				// Deboarding time selected.
				request.getToLink(), vehicle.getDeboardingTime(), Arrays.asList(request));

		UAMTurnAroundTask turnAroundTask = new UAMTurnAroundTask(
				dropoffPath.getArrivalTime() + vehicle.getDeboardingTime(), // Vehicle has a TurnAround time after
				// DropOff task
				dropoffPath.getArrivalTime() + vehicle.getDeboardingTime() + vehicle.getTurnAroundTime(),
				request.getToLink(), Arrays.asList(request));

		if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
			stayTask.setEndTime(startTime);
		} else {
			schedule.removeLastTask();
		}

		if (!stayTask.getLink().getId().equals(request.getFromLink().getId())) {
			schedule.addTask(pickupDriveTask);
		}
		schedule.addTask(pickupTask);
		schedule.addTask(dropoffDriveTask);
		schedule.addTask(dropoffTask);
		schedule.addTask(turnAroundTask);

		double distance = 0.0;
		for (int i = 0; i < dropoffPath.getLinkCount(); i++) {
			distance += dropoffPath.getLink(i).getLength();
		}

		request.setDistance(distance);

		if (turnAroundTask.getEndTime() < scheduleEndTime) {
			schedule.addTask(new UAMStayTask(turnAroundTask.getEndTime(), scheduleEndTime, turnAroundTask.getLink()));
			/*
			 * If TurnAround task ends before the scheduleEndTime, a new StayTask is created
			 * and added, beginning at the end of TurnAround Task and ending at
			 * scheduleEndTime. Why(What is the case that this would happen?)??
			 */
		}
	}

	public void update() {
		// TODO: This can be made more efficient if one knows which ones have
		// just been added and which ones are still
		// to be processed. Depends mainly on if "update" is called before new
		// tasks are submitted or after ...

		try {
			for (AppendTask task : tasks) {
				schedule(task, task.pickup.get(), task.dropoff.get());
			}
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		tasks.clear();

		/*
		 * Iterator<AppendTask> iterator = tasks.iterator();
		 *
		 * while (iterator.hasNext()) { AppendTask task = iterator.r();
		 *
		 * if (task.pickup.isDone() && task.dropoff.isDone()) { schedule(task);
		 * iterator.remove(); } }
		 */
	}

	public void setStations(UAMStations stations) {
		this.stations = stations;
	}

	private class AppendTask {
		final public UAMRequest request;
		final public UAMVehicle vehicle;

		final public Future<Path> pickup;
		final public Future<Path> dropoff;

		final public double time;

		public AppendTask(UAMRequest request, UAMVehicle vehicle, double time, Future<Path> pickup,
						  Future<Path> dropoff) {
			this.request = request;
			this.vehicle = vehicle;
			this.pickup = pickup;
			this.dropoff = dropoff;
			this.time = time;
		}
	}
}
