package net.bhl.matsim.uam.events;

import net.bhl.matsim.uam.schedule.UAMTransitEvent;
import org.matsim.core.events.handler.EventHandler;

/**
 * An interface for the UAMTransit events.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public interface UAMTransitEventHandler extends EventHandler {
	void handleEvent(UAMTransitEvent event);
}