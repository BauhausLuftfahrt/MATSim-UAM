package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

public interface ChargingStartEventHandler extends EventHandler {
	void handleEvent(ChargingStartEvent event);

}
