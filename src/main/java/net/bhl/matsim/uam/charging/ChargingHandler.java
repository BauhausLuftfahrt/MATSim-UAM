package net.bhl.matsim.uam.charging;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.events.MobsimAfterSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimAfterSimStepListener;

import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;

public class ChargingHandler implements MobsimAfterSimStepListener{

	
	private Map<Id<UAMStation>, Set<UAMVehicle>> chargingVehicles = new HashMap<>();
	private Map<Id<UAMStation>, Queue<UAMVehicle> > queueingVehicles = new HashMap<>();
	
	
		
	@Override
	public void notifyMobsimAfterSimStep(MobsimAfterSimStepEvent e) {
		
		//charge vehicle
		//if vehicle full remove from chargingVehicles and add a new one
	}

}
