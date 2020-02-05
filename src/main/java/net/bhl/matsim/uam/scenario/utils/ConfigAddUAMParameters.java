package net.bhl.matsim.uam.scenario.utils;

import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.router.strategy.UAMStrategy;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.*;

/**
 * This class adds UAM parameters to the Config file.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class ConfigAddUAMParameters {

	public static void main(String[] args) {

		System.out.println("args: path to config, UAM input file, modes, number of threads, search radius, " +
				"walk distance, routing strategy, PT Simulation used");

		int i = 0;
		Config config = ConfigUtils.loadConfig(args[i++], new UAMConfigGroup(), new DvrpConfigGroup());
		addUAMParameters(config, args[i++], args[i++], Integer.parseInt(args[i++]), Integer.parseInt(args[i++]),
				Integer.parseInt(args[i++]), UAMStrategy.UAMStrategyType.valueOf(args[i++].toUpperCase()), Boolean.parseBoolean(args[i]));
		ConfigWriter configWriter = new ConfigWriter(config);
		configWriter.write(args[0] + ".uam.xml");
	}

	public static void addUAMParameters(Config config, String inputUAMFile, String modes, int threads, int searchRadius,
										int walkDistance, UAMStrategy.UAMStrategyType routingStrategy, boolean ptSimulation) {
		config.getModules().get("uam").addParam("inputUAMFile", inputUAMFile);
		config.getModules().get("uam").addParam("availableAccessModes", modes);
		config.getModules().get("uam").addParam("parallelRouters", "" + threads);
		config.getModules().get("uam").addParam("searchRadius", "" + searchRadius);
		config.getModules().get("uam").addParam("walkDistance", "" + walkDistance);
		// Possible strategies: MAXUTILITY, MAXACCESSUTILITY, MINTRAVELTIME,
		// MINACCESSTRAVELTIME, MINDISTANCE,
		// MINACCESSDISTANCE, PREDEFINED
		config.getModules().get("uam").addParam("routingStrategy", routingStrategy.toString());
		config.getModules().get("uam").addParam("ptSimulation", String.valueOf(ptSimulation));

		// UAM planCalcScore activities
		ConfigGroup uamInteractionParam = config.getModules().get("planCalcScore").createParameterSet("activityParams");
		uamInteractionParam.addParam("activityType", "uam_interaction");
		uamInteractionParam.addParam("scoringThisActivityAtAll", "false");
		config.getModules().get("planCalcScore").addParameterSet(uamInteractionParam);

		// UAM planCalcScore modes
		String[] modeScores = {"uam", "access_uam_walk", "egress_uam_walk", "access_uam_car", "egress_uam_car",
				"access_uam_bike", "egress_uam_bike"};
		for (String modeScore : modeScores) {
			ConfigGroup modeParam = config.getModules().get("planCalcScore").createParameterSet("modeParams");
			modeParam.addParam("mode", modeScore);
			modeParam.addParam("constant", "0.0");
			modeParam.addParam("marginalUtilityOfDistance_util_m", "0.0");
			modeParam.addParam("marginalUtilityOfTraveling_util_hr", "0.0");
			modeParam.addParam("monetaryDistanceRate", "0.0");
			config.getModules().get("planCalcScore").addParameterSet(modeParam);
		}
	}
}
