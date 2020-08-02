package net.bhl.matsim.uam.data;

import com.google.inject.Inject;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.WaitingData;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores waiting data of stations.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class WaitingStationData implements BeforeMobsimListener {

	Map<Id<UAMStation>, WaitingData> waitingData = new HashMap<>();
	@Inject
	private UAMManager uamManager;

	public Map<Id<UAMStation>, WaitingData> getWaitingData() {
		return waitingData;
	}

	public WaitingData getWaitingDataForStation(Id<UAMStation> stationId) {
		return this.waitingData.get(stationId);
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		waitingData.clear();
		for (UAMStation station : uamManager.getStations().getUAMStations().values())
			waitingData.put(station.getId(), new WaitingData(station.getDefaultWaitTime()));
	}
}
