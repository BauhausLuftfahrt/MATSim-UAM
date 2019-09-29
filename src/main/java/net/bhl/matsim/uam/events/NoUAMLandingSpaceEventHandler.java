package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

/**
 * Event Handler for the {@link NoUAMLandingSpaceEvent} event.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public interface NoUAMLandingSpaceEventHandler extends EventHandler {
	void handleEvent(NoUAMLandingSpaceEvent event);

}
