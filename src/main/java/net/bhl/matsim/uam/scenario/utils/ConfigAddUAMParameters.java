package net.bhl.matsim.uam.scenario.utils;

import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.router.strategy.UAMStrategy;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;

import java.util.Collection;

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
		Config config = ConfigUtils.loadConfig(args[i++], new DvrpConfigGroup());
		addUAMParameters(config, args[i++], args[i++], Integer.parseInt(args[i++]), Integer.parseInt(args[i++]),
				Integer.parseInt(args[i++]), UAMStrategy.UAMStrategyType.valueOf(args[i++].toUpperCase()),
				Boolean.parseBoolean(args[i]));
		ConfigWriter configWriter = new ConfigWriter(config);
		configWriter.write(args[0] + "." + UAMModes.UAM_MODE + ".xml");
	}

	public static void addUAMParameters(Config config, String inputUAMFile, String modes, int threads, int searchRadius,
										int walkDistance, UAMStrategy.UAMStrategyType routingStrategy, boolean ptSimulation) {
		config.getModules().put(UAMModes.UAM_MODE, new UAMConfigGroup());
		config.getModules().get(UAMModes.UAM_MODE).addParam("inputUAMFile", inputUAMFile);
		config.getModules().get(UAMModes.UAM_MODE).addParam("availableAccessModes", modes);
		config.getModules().get(UAMModes.UAM_MODE).addParam("parallelRouters", "" + threads);
		config.getModules().get(UAMModes.UAM_MODE).addParam("searchRadius", "" + searchRadius);
		config.getModules().get(UAMModes.UAM_MODE).addParam("walkDistance", "" + walkDistance);
		config.getModules().get(UAMModes.UAM_MODE).addParam("routingStrategy", routingStrategy.toString());
		config.getModules().get(UAMModes.UAM_MODE).addParam("ptSimulation", String.valueOf(ptSimulation));

		// If PT simulation is desired (true), PT may not be listed under teleportedModeParameters, else it must be
		ConfigGroup planscalcroute = config.getModules().get("planscalcroute");
		Collection<? extends ConfigGroup> teleportedModeParameters = planscalcroute.getParameterSets("teleportedModeParameters");

		boolean ptParamsExistent = false;
		for (ConfigGroup cg : teleportedModeParameters) {
			if (!(cg instanceof PlansCalcRouteConfigGroup.ModeRoutingParams))
				continue;

			PlansCalcRouteConfigGroup.ModeRoutingParams mrp = (PlansCalcRouteConfigGroup.ModeRoutingParams) cg;

			if (mrp.getMode().equals(TransportMode.pt)) {
				ptParamsExistent = true;
				if (ptSimulation)
					mrp.setMode("disabled-" + TransportMode.pt);
				break;
			}
		}

		if (!ptSimulation && !ptParamsExistent) {
			PlansCalcRouteConfigGroup.ModeRoutingParams modeRoutingParams =
					new PlansCalcRouteConfigGroup.ModeRoutingParams();
			modeRoutingParams.setMode(TransportMode.pt);
			modeRoutingParams.setTeleportedModeSpeed(10.0);
			modeRoutingParams.setBeelineDistanceFactor(1.0);
			planscalcroute.addParameterSet(modeRoutingParams);
		}

		// UAM planCalcScore activities
		ConfigGroup uamInteractionParam = config.getModules().get("planCalcScore").createParameterSet("activityParams");
		uamInteractionParam.addParam("activityType", UAMModes.UAM_INTERACTION);
		uamInteractionParam.addParam("scoringThisActivityAtAll", "false");
		config.getModules().get("planCalcScore").addParameterSet(uamInteractionParam);

		String mainMode = config.getModules().get("qsim").getParams().get("mainMode");
		config.getModules().get("qsim").addParam("mainMode",
				mainMode + "," + UAMModes.UAM_ACCESS + "," + UAMModes.UAM_EGRESS);

		// UAM planCalcScore modes
		String[] modeScores = {UAMModes.UAM_MODE,
				UAMModes.UAM_ACCESS + "walk", UAMModes.UAM_EGRESS + "walk",
				UAMModes.UAM_ACCESS + "car", UAMModes.UAM_EGRESS + "car",
				UAMModes.UAM_ACCESS + "bike", UAMModes.UAM_EGRESS + "bike"};
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
