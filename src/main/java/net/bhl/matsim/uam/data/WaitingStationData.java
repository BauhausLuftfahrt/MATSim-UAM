package net.bhl.matsim.uam.data;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

import com.google.inject.Inject;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.WaitingData;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;

public class WaitingStationData implements BeforeMobsimListener{
	
	@Inject
	private UAMManager uamManager;	
	
	Map<Id<UAMStation>, WaitingData> waitingData = new HashMap<>();
	
	
	public Map<Id<UAMStation>, WaitingData> getWaitingData() {
		return waitingData;
	}
	
	public WaitingData getWaitingDataForStation(Id<UAMStation> stationId) {
		return this.waitingData.get(stationId);
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		waitingData.clear();
		for (UAMStation station : uamManager.getStations().stations.values()) {
			
			waitingData.put(station.getId(), new WaitingData(station.getDefaultWaitTime()));
		}
		
	}
	

}
