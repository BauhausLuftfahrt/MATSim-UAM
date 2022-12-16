package net.bhl.matsim.uam.charging;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;

public class DischargingHandler implements LinkLeaveEventHandler {

	private UAMManager uamManager;
	private Scenario scenario;
	
	public DischargingHandler(UAMManager uamManager, Scenario scenario) {
		this.uamManager = uamManager;
		this.scenario = scenario;
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {

		if (event.getVehicleId().toString().contains("uam")) {
			UAMVehicle uamVehicle = this.uamManager.getVehicles()
					.get(Id.create(event.getVehicleId().toString(), DvrpVehicle.class));

			Link link = this.scenario.getNetwork().getLinks().get(event.getLinkId());
			double dischargedAmount = link.getAttributes().getAttribute("type").equals("uam_vertical")
					? uamVehicle.getVehicleType().getEnergyConsumptionVertical() * link.getLength()
					: uamVehicle.getVehicleType().getEnergyConsumptionHorizontal() * link.getLength();

			uamVehicle.setCurrentCharge(uamVehicle.getCurrentCharge() - dischargedAmount);

		}

	}

}
