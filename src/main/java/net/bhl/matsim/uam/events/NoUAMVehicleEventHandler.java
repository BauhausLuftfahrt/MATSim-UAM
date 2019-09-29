package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

/**
 * Event Handler for the {@link NoUAMVehicleEvent} event.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public interface NoUAMVehicleEventHandler extends EventHandler {
	void handleEvent(NoUAMVehicleEvent event);
}
