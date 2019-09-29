package net.bhl.matsim.uam.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.vrpagent.VrpActivity;
import org.matsim.contrib.dynagent.DynAgent;

/**
 * This class defines the drop off activity for the passenger and its properties.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class UAMPassengerDropoffActivity extends VrpActivity {
    private final PassengerEngine passengerEngine;
    private final DynAgent driver;
    private final Set<? extends PassengerRequest> requests;
    
    private double endTime = 0.0;
    
    public UAMPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, Vehicle vehicle, StayTask dropoffTask,
                                      Set<? extends PassengerRequest> requests, double dropoffDuration, String activityType)
    {
        super(activityType, dropoffTask);

        this.passengerEngine = passengerEngine;
        this.driver = driver;
        this.requests = requests;
        
        if (requests.size() > vehicle.getCapacity()) {
        	// Number of requests exceeds number of seats
        	throw new IllegalStateException();
        }
        
        endTime = dropoffTask.getBeginTime() + dropoffDuration;
    }


    @Override
    public void finalizeAction(double now)
    {
        for (PassengerRequest request : requests) {
        	passengerEngine.dropOffPassenger(driver, request, now);
        }
    }
    
    @Override
	public double getEndTime() {
		return endTime;
	}
}
