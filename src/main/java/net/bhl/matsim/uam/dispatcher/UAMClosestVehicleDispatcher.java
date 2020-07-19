package net.bhl.matsim.uam.dispatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.schedule.UAMSingleRideAppender;
import net.bhl.matsim.uam.schedule.UAMStayTask;
import net.bhl.matsim.uam.schedule.UAMTask;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * UAM Dispatcher that selects the closest available vehicle to a request.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
@Singleton
public class UAMClosestVehicleDispatcher implements MobsimBeforeSimStepListener {

	@Inject
	final private UAMSingleRideAppender appender;

	final private List<UAMVehicle> availableVehicles = new LinkedList<>();
	final private QuadTree<UAMVehicle> availableVehiclesTree;
	final private Queue<UAMRequest> pendingRequests = new LinkedList<>();
	private boolean reoptimize = false;

	@Inject
	public UAMClosestVehicleDispatcher(UAMSingleRideAppender appender, UAMManager uamManager, Network network) {
		this.appender = appender;
		this.appender.setStations(uamManager.getStations());

		double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
		// minX, minY, maxX, maxY
		availableVehiclesTree = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);

		for (DvrpVehicle veh : uamManager.getVehicles().values()) {
			this.availableVehicles.add((UAMVehicle) veh);

			Id<UAMStation> stationId = ((UAMVehicle) veh).getInitialStationId();
			UAMStation uamStation = uamManager.getStations().getUAMStations().get(stationId);
			Link linkStation = uamStation.getLocationLink();
			Coord coord = linkStation.getCoord();

			this.availableVehiclesTree.put(coord.getX(), coord.getY(), (UAMVehicle) veh);
		}
	}

	@Override
	public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
		appender.update();
		if (reoptimize)
			reoptimize(e.getSimulationTime());
	}

	public void onRequestSubmitted(UAMRequest request) {
		pendingRequests.add(request);
		reoptimize = true;
	}

	public void onNextTaskStarted(UAMVehicle vehicle) {
		UAMTask task = (UAMTask) vehicle.getSchedule().getCurrentTask();
		if (task.getUAMTaskType() == UAMTask.UAMTaskType.STAY) {
			availableVehicles.add(vehicle);
			Coord coord = ((UAMStayTask) task).getLink().getCoord();
			this.availableVehiclesTree.put(coord.getX(), coord.getY(), vehicle);
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
			UAMRequest request = pendingRequests.poll();
			UAMVehicle vehicle = this.availableVehiclesTree.getClosest(request.getFromLink().getCoord().getX(),
					request.getFromLink().getCoord().getY());

			this.availableVehicles.remove(vehicle);
			appender.schedule(request, vehicle, now);
		}

		reoptimize = false;
	}

}
