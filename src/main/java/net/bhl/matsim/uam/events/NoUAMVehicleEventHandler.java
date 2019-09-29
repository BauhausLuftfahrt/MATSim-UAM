package net.bhl.matsim.uam.events;
import org.matsim.core.events.handler.EventHandler;

public interface NoUAMVehicleEventHandler extends EventHandler {
	public void handleEvent (NoUAMVehicleEvent event);
}
