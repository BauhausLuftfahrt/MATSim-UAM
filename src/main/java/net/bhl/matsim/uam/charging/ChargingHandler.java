package net.bhl.matsim.uam.charging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.events.MobsimAfterSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimAfterSimStepListener;

import com.google.inject.Inject;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.events.ChargingStartEvent;
import net.bhl.matsim.uam.infrastructure.UAMStation;
import net.bhl.matsim.uam.infrastructure.UAMStationWithChargers;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import net.bhl.matsim.uam.schedule.UAMChargingTask;

public class ChargingHandler implements MobsimAfterSimStepListener {

	private Map<Id<UAMStation>, Set<UAMVehicle>> chargingVehicles = new HashMap<>();
	private Map<Id<UAMStation>, Queue<UAMVehicle>> queueingVehicles = new HashMap<>();
	private Map<Id<UAMStation>, Integer> freeSlots = new HashMap<>();
	private final UAMStations uamStations;
	private final UAMManager uamManager;
	private final EventsManager  eventsManager;

	@Inject
	public ChargingHandler(UAMStations uamStations, UAMManager uamManager, EventsManager eventsManager) {
		for (UAMStation uamStation : uamStations.getUAMStations().values()) {
			this.freeSlots.put(uamStation.getId(), ((UAMStationWithChargers) uamStation).getNumberOfChargers());
		}
		this.uamStations = uamStations;
		this.uamManager = uamManager;
		this.eventsManager = eventsManager;
	}

	@Override
	public void notifyMobsimAfterSimStep(MobsimAfterSimStepEvent e) {

		// charge vehicle
		// if vehicle full remove from chargingVehicles and add a new one

		for (Id<UAMStation> stationId : this.chargingVehicles.keySet()) {
			Set<UAMVehicle> vehiclesToRemove = new HashSet<>();
			for (UAMVehicle vehicle : this.chargingVehicles.get(stationId)) {
				double chargeSpeed = ((UAMStationWithChargers) this.uamStations.getUAMStations().get(stationId))
						.getChargingSpeed();
				double currentCharge = this.uamManager.getChargeVehicle().get(vehicle.getId());
				double charge = currentCharge + chargeSpeed < vehicle.getVehicleType().getMaximumCharge()
						? currentCharge + chargeSpeed
						: vehicle.getVehicleType().getMaximumCharge();
				this.uamManager.getChargeVehicle().put(vehicle.getId(), charge);
				if (charge == vehicle.getVehicleType().getMaximumCharge()) {
					// we remove this vehicle from charging and if there are queueing
					// vehicles add them to charging vehicles
					vehiclesToRemove.add(vehicle);					
					
				}
			}
			
			for (UAMVehicle vehicle : vehiclesToRemove) {
				
				this.chargingVehicles.get(stationId).remove(vehicle);
				if (this.queueingVehicles.get(stationId) != null
						&& this.queueingVehicles.get(stationId).size() > 0) {
					UAMVehicle newChargingVehicle = this.queueingVehicles.get(stationId).poll();
					this.chargingVehicles.get(stationId).add(newChargingVehicle);
					this.eventsManager.processEvent(new ChargingStartEvent(e.getSimulationTime(), newChargingVehicle));
				} else
					this.freeSlots.put(stationId, this.freeSlots.get(stationId) + 1);
			}
		}
	}

	public void addVehicleToCharge(UAMVehicle dvrpVehicle, UAMChargingTask task) {
		UAMVehicle vehicle = this.uamManager.getVehicles().get(dvrpVehicle.getId());
		if (this.freeSlots.get(task.getUamStationId()) > 0) {
			if (this.chargingVehicles.get(task.getUamStationId()) != null) {
				this.chargingVehicles.get(task.getUamStationId()).add(vehicle);
				this.freeSlots.put(task.getUamStationId(), this.freeSlots.get(task.getUamStationId()) - 1);
				
			} else {
				Set<UAMVehicle> vehicles = new HashSet<>();
				vehicles.add(vehicle);
				this.chargingVehicles.put(task.getUamStationId(), vehicles);
				this.freeSlots.put(task.getUamStationId(), this.freeSlots.get(task.getUamStationId()) - 1);
			}
			this.eventsManager.processEvent(new ChargingStartEvent(task.getBeginTime(), vehicle));
		} else {
			if (this.queueingVehicles.get(task.getUamStationId()) != null) {
				this.queueingVehicles.get(task.getUamStationId()).add(vehicle);
			} else {
				Queue<UAMVehicle> vehicles = new LinkedList<>();
				vehicles.add(vehicle);
				this.queueingVehicles.put(task.getUamStationId(), vehicles);
			}
		}

	}

}
