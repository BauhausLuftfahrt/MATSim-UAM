package net.bhl.matsim.uam.analysis.uamdemand.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Link;

import com.google.inject.Inject;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.ChargingStartEvent;
import net.bhl.matsim.uam.events.ChargingStartEventHandler;

/**
 * This class should record when the charging happens * 
 * 
 * @author balacm
 *
 */
public class UAMChargingHandler
		implements ActivityStartEventHandler, ActivityEndEventHandler, ChargingStartEventHandler {

	@Inject
	private UAMManager uamManager;
	@Inject
	private Scenario scenario;
	private Map<String, ChargingItem> mapVehicleChargingInfo = new HashMap<>();
	private Set<ChargingItem> allChargingData = new HashSet<>();

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (event.getActType().equals("Charging")) {
			ChargingItem chargingItem = this.mapVehicleChargingInfo.get(event.getPersonId().toString());
			chargingItem.endTime = event.getTime();
			this.allChargingData.add(chargingItem);
			this.mapVehicleChargingInfo.remove(event.getPersonId().toString());
		}
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {

		if (event.getActType().equals("Charging")) {
			if (this.mapVehicleChargingInfo.containsKey(event.getPersonId().toString())) {
				Id<Link> linkId = event.getLinkId();
				Link link = this.scenario.getNetwork().getLinks().get(linkId);
				ChargingItem chargingItem = this.mapVehicleChargingInfo.get(event.getPersonId().toString());
				chargingItem.stationId = this.uamManager.getStations().getNearestUAMStation(link).getId().toString();
			}
			else {
				Id<Link> linkId = event.getLinkId();
				Link link = this.scenario.getNetwork().getLinks().get(linkId);
				ChargingItem chargingItem = new ChargingItem(event.getPersonId().toString(), event.getTime(), -1, -1,
						 this.uamManager.getStations().getNearestUAMStation(link).getId().toString());
				this.mapVehicleChargingInfo.put(event.getPersonId().toString(), chargingItem);
			}
		}
	}

	@Override
	public void handleEvent(ChargingStartEvent event) {
		
		if (this.mapVehicleChargingInfo.get(event.getUamVehicle().getId().toString()) == null) {
			
			ChargingItem chargingItem = new ChargingItem(event.getUamVehicle().getId().toString(), event.getTime(), -1, event.getTime(),
					null);
			this.mapVehicleChargingInfo.put(event.getUamVehicle().getId().toString(), chargingItem);
		}
		else {
			ChargingItem chargingItem = this.mapVehicleChargingInfo.get(event.getUamVehicle().getId().toString());
			chargingItem.startingCharge = event.getTime();
		}

	}
	
	@Override
	public void reset(int iteration) {
		this.mapVehicleChargingInfo.clear();
		this.allChargingData.clear();
	}

	public Set<ChargingItem> getAllChargingData() {
		return allChargingData;
	}

}
