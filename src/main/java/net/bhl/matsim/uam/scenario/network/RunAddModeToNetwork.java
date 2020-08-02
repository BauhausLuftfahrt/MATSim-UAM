package net.bhl.matsim.uam.scenario.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

// Adjusted from RunCreateNetworkSHP.java by matsim-code-examples

/**
 * This script adds specified mode(s) to an existing network if other specified
 * mode(s) is (are) present.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class RunAddModeToNetwork {

	private static final String searchMode = TransportMode.car;
	private static final String addedMode = TransportMode.car + "_passenger";

	public static void main(String[] args) throws Exception {
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(args[0]);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();

		for (Link l : network.getLinks().values()) {
			Set<String> modes = new HashSet<>(l.getAllowedModes());
			if (modes.contains(searchMode)) {
				modes.add(addedMode);
				l.setAllowedModes(modes);
			}
		}

		new NetworkWriter(network).write(args[0]);
		System.out.println("done.");
	}
}