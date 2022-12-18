package net.bhl.matsim.uam.events;

import org.matsim.api.core.v01.events.Event;

import net.bhl.matsim.uam.infrastructure.UAMVehicle;

public class ChargingStartEvent extends Event{

	private final static String EVENT_TYPE = "UAMStartChargingEvent";
	
	private UAMVehicle uamVehicle;
	public ChargingStartEvent(double time, UAMVehicle uamVehicle) {
		super(time);		
		this.uamVehicle = uamVehicle;
	}

	public UAMVehicle getUamVehicle() {
		return uamVehicle;
	}

	@Override
	public String getEventType() {		
		return ChargingStartEvent.EVENT_TYPE;
	}

}
