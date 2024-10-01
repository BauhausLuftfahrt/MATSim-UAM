package net.bhl.matsim.uam.data;

import com.google.inject.Inject;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import org.matsim.api.core.v01.Id;
import org.matsim.core.config.Config;
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

	@Inject
	private Config config;

	public Map<Id<UAMStation>, WaitingData> getWaitingData() {
		return waitingData;
	}

	public WaitingData getWaitingDataForStation(Id<UAMStation> stationId) {
		return this.waitingData.get(stationId);
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		waitingData.clear();

		int simulationEndTime = Integer.parseInt(config.getModules().get("qsim").getParams().get("endTime").substring(0,2));
		double waitingTimeBinSize = Double.parseDouble(config.getModules().get("travelTimeCalculator").getParams().get("travelTimeBinSize"));
		for (UAMStation station : uamManager.getStations().getUAMStations().values())
			waitingData.put(station.getId(), new WaitingData(simulationEndTime * 3600,
					waitingTimeBinSize,	station.getDefaultWaitTime()));
	}
}
