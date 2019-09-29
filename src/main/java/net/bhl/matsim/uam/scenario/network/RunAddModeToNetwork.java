package net.bhl.matsim.uam.scenario.network;

import java.util.HashSet;

// Adjusted from RunCreateNetworkSHP.java by matsim-code-examples

import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class RunAddModeToNetwork {
	
	private static final String searchMode = "car";
	private static final String addedMode = "car_passenger";

	public static void main(String[] args) throws Exception {		
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(args[0]);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();

		for (Link l : network.getLinks().values()) {
			Set<String> modes = new HashSet<String>();
			modes.addAll(l.getAllowedModes());
			if (modes.contains(searchMode)) {
				modes.add(addedMode);
				l.setAllowedModes(modes);
			}
		}
		
		new NetworkWriter(network).write(args[0]);
		System.out.println("done.");
	}
}