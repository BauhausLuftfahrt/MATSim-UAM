package net.bhl.matsim.uam.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.bhl.matsim.uam.schedule.UAMStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

/**
 * Class that loads the UAM vehicle data.
 * Initially all vehicles are in the Stay mode at their initial stations.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Singleton
public class UAMLoader implements BeforeMobsimListener {

	@Inject
	private UAMFleetData data;

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		for (DvrpVehicle vehicle : data.getVehicles().values()) {
			Schedule schedule = vehicle.getSchedule();

			// reset schedule
			while(schedule.getTaskCount() > 0) {
				schedule.removeLastTask();
			}

			// add idle/stay task
			schedule.addTask(new UAMStayTask(vehicle.getServiceBeginTime(),
					Double.POSITIVE_INFINITY, vehicle.getStartLink()));
		}

	}

}
