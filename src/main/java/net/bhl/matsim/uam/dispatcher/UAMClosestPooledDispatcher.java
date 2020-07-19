package net.bhl.matsim.uam.dispatcher;

import com.google.inject.Inject;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.schedule.*;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import java.util.*;

/**
 * UAM Dispatcher that allows pooled ride between passengers.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class UAMClosestPooledDispatcher implements Dispatcher {
	final Set<UAMVehicle> enRouteToPickupVehicles = new HashSet<>();
	@Inject
	final private UAMSingleRideAppender appender;
	final private Queue<UAMVehicle> availableVehicles = new LinkedList<>();
	final private Queue<UAMRequest> pendingRequests = new LinkedList<>();
	final private QuadTree<UAMVehicle> availableVehiclesTree;
	boolean reoptimize = true;
	private Map<UAMVehicle, Coord> locationVehicles = new HashMap<>();

	@Inject
	public UAMClosestPooledDispatcher(UAMSingleRideAppender appender, UAMManager uamManager, Network network, Fleet data) {
		this.appender = appender;
		this.appender.setStations(uamManager.getStations());

		double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minX, minY, maxX, maxY
		availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);

		for (DvrpVehicle veh : data.getVehicles().values()) {
			this.availableVehicles.add((UAMVehicle) veh);

			Id<UAMStation> stationId = ((UAMVehicle) veh).getInitialStationId();
			UAMStation uamStation = uamManager.getStations().getUAMStations().get(stationId);
			Link linkStation = uamStation.getLocationLink();
			Coord coord = linkStation.getCoord();

			this.availableVehiclesTree.put(coord.getX(), coord.getY(), (UAMVehicle) veh);
			locationVehicles.put((UAMVehicle) veh, coord);
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
		if (task instanceof UAMStayTask) {
			availableVehicles.add(vehicle);
			Coord coord = ((UAMStayTask) task).getLink().getCoord();
			this.availableVehiclesTree.put(coord.getX(), coord.getY(), vehicle);
			this.locationVehicles.put(vehicle, coord);
			reoptimize = true;
		} else if (task instanceof UAMPickupTask)
			this.enRouteToPickupVehicles.remove(vehicle);
	}

	private void reoptimize(double now) {
		// TODO: have pending requests per station
		while (availableVehicles.size() > 0 && pendingRequests.size() > 0) {
			UAMRequest request = pendingRequests.poll();

			if (!findEligableEnRouteVehicle(request)) {
				UAMVehicle vehicle = this.availableVehiclesTree.getClosest(request.getFromLink().getCoord().getX(),
						request.getFromLink().getCoord().getY());
				Coord coord = this.locationVehicles.get(vehicle);
				this.availableVehiclesTree.remove(coord.getX(), coord.getY(), vehicle);
				this.availableVehicles.remove(vehicle);

				appender.schedule(request, vehicle, now);
				if (vehicle.getCapacity() > 1)
					this.enRouteToPickupVehicles.add(vehicle);
			}
		}

		if (availableVehicles.size() == 0) {
			while (pendingRequests.size() > 0) {
				UAMRequest request = pendingRequests.peek();
				if (!findEligableEnRouteVehicle(request))
					break;
				else
					pendingRequests.remove();
			}
		}
	}

	/**
	 * @param request UAM Request
	 * @return True if origins and destinations of requests are the same and vehicle
	 * capacity constraint is met, otherwise false.
	 */
	private boolean findEligableEnRouteVehicle(UAMRequest request) {

		for (UAMVehicle vehicle : enRouteToPickupVehicles) {

			Schedule schedule = vehicle.getSchedule();
			if (schedule.getCurrentTask() instanceof UAMFlyTask) {
				int index = schedule.getTasks().indexOf(schedule.getCurrentTask());

				if (schedule.getTasks().get(index + 1) instanceof UAMPickupTask) {
					UAMPickupTask pickupTask = (UAMPickupTask) schedule.getTasks().get(index + 1);
					UAMRequest oldReq = (UAMRequest) pickupTask.getRequests().toArray()[0];
					if (oldReq.getToLink() == request.getToLink() && oldReq.getFromLink() == request.getFromLink()) {
						request.setDistance(oldReq.getDistance());
						pickupTask.getRequests().add(request);
						UAMDropoffTask dropOff = (UAMDropoffTask) schedule.getTasks().get(index + 3);
						dropOff.getRequests().add(request);

						if (vehicle.getCapacity() == dropOff.getRequests().size())
							this.enRouteToPickupVehicles.remove(vehicle);

						return true;
					}
				} else {
					Logger log = Logger.getLogger(UAMClosestPooledDispatcher.class);
					log.warn("Task following a UAMFlyTask is unexpectedly not a UAMPickupTask for vehicle: "
							+ vehicle.getId());
				}
			}
		}

		return false;
	}

}
