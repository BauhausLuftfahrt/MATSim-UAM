package net.bhl.matsim.uam.events;

import org.matsim.core.events.handler.EventHandler;

public interface NoUAMLandingSpaceEventHandler extends EventHandler {
	void handleEvent(NoUAMLandingSpaceEvent event);

}
