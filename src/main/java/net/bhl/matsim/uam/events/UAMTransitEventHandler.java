package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

import net.bhl.matsim.uam.schedule.UAMTransitEvent;

/**
 * An interface for the UAMTransit events.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public interface UAMTransitEventHandler extends EventHandler {
	public void handleEvent(UAMTransitEvent event);
}