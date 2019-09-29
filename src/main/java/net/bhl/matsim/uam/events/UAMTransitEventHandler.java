package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.schedule.UAMTransitEvent;
import org.matsim.core.events.handler.EventHandler;

public interface UAMTransitEventHandler extends EventHandler {
	void handleEvent(UAMTransitEvent event);
}