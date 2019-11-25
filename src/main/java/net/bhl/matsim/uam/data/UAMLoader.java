package net.bhl.matsim.uam.data;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.router.UAMCachedIntermodalRoutingModule;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.schedule.UAMStayTask;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.ScheduleImpl;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

/**
 * Class that loads the UAM vehicle data. Initially all vehicles are in the Stay
 * mode at their initial stations.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Singleton
public class UAMLoader implements BeforeMobsimListener {
	private static final Logger log = Logger.getLogger(UAMLoader.class);

//	@Inject
//	@DvrpMode(UAMModes.UAM_MODE)
//	private UAMFleetData data;
	@Inject
	UAMManager uamManager;

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		for (DvrpVehicle vehicle : uamManager.getVehicles().values()) {

			/*
			 * List<Integer> taskIdList = new ArrayList<>(); for (Task task :
			 * vehicle.getSchedule().getTasks()) { taskIdList.add(task.getTaskIdx()); }
			 * 
			 * for (Integer i : taskIdList) { log.warn("task id: " + i+ " task: "+
			 * String.valueOf(vehicle.getSchedule().getTasks().get(i))); //
			 * vehicle.getSchedule().removeTask( vehicle.getSchedule().getTasks().get(i)); }
			 * 
			 * 
			 * //reset vehicle -> temporary solution for the old method of reseting schedule
			 * 
			 * if (!(vehicle.getSchedule()==null)) { for (int i = 0; i <
			 * vehicle.getSchedule().getTasks().size(); i++) {
			 * vehicle.getSchedule().removeLastTask(); } }
			 * 
			 * // Schedule schedule = new
			 * ScheduleImpl(ImmutableDvrpVehicleSpecification.newBuilder().id(vehicle.getId(
			 * )).capacity(vehicle.getCapacity()). //
			 * startLinkId(vehicle.getStartLink().getId()).serviceBeginTime(vehicle.
			 * getServiceBeginTime()).serviceEndTime(vehicle.getServiceEndTime()).build());
			 * 
			 * log.warn("Vehicle schedule status: " +
			 * String.valueOf(vehicle.getSchedule().getStatus()));
			 */
			

//			Schedule schedule = vehicle.getSchedule();
//			schedule.addTask(
//					new UAMStayTask(vehicle.getServiceBeginTime(), Double.POSITIVE_INFINITY, vehicle.getStartLink())); // Usage
																														// of
																														// UAMStayTask
			
			//reset BY COMMENTING THE CODE BELLOW YOU GET A DIFFERENT ERROR WITH ARRAY BOUNDS - CHECK THIS
			
			
			log.warn("Task List size BEFORE: " + vehicle.getSchedule().getTasks().size());
			
	//		  for (Task task : vehicle.getSchedule().getTasks()) { 
//				  vehicle.getSchedule().removeTask(task);}
			
//			  if ((vehicle.getSchedule().getTasks().size()==0)) {
//			  log.warn("Adding stayTask..."); vehicle.getSchedule().addTask( new
//			  UAMStayTask(vehicle.getServiceBeginTime(), Double.POSITIVE_INFINITY,
	//		  vehicle.getStartLink())); }
//			Schedule schedule = vehicle.getSchedule();
//			while(schedule.getTaskCount() >= 0) {
//				schedule.removeLastTask();
//			}
			// add idle/stay task
//			schedule.addTask(new UAMStayTask(vehicle.getServiceBeginTime(),
//					Double.POSITIVE_INFINITY, vehicle.getStartLink()));
			  
			  log.warn("Task List size AFTER: " + vehicle.getSchedule().getTasks().size());
			 
			log.warn("Vehicle schedule status AFTER: " + String.valueOf(vehicle.getSchedule().getStatus()));
		}

	}

}
