package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

public interface NoUAMVehicleEventHandler extends EventHandler {
	void handleEvent(NoUAMVehicleEvent event);
}
