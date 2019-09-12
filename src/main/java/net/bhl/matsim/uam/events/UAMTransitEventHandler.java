package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

import net.bhl.matsim.uam.schedule.UAMTransitEvent;

public interface UAMTransitEventHandler extends EventHandler {
	public void handleEvent(UAMTransitEvent event);
}