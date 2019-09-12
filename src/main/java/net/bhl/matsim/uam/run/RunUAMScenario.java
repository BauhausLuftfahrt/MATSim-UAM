package net.bhl.matsim.uam.run;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.PtConstants;

import ch.ethz.matsim.baseline_scenario.config.CommandLine;
import ch.ethz.matsim.baseline_scenario.config.CommandLine.ConfigurationException;
import ch.ethz.matsim.baseline_scenario.transit.BaselineTransitModule;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;

import net.bhl.matsim.uam.modechoice.CustomModeChoiceModuleMinTravelTime;

import net.bhl.matsim.uam.modechoice.utils.LongPlanFilter;
import net.bhl.matsim.uam.qsim.UAMSpeedModule;

/**
 * The RunUAMScenario program start a MATSim run including Urban Air Mobility
 * capabilities.
 *
 * @author Milos Balac, Raoul Rothfeld
 * @version 1.0
 * @since 2019-01-15
 */
public class RunUAMScenario {

	private static UAMConfigGroup uamConfigGroup;
	private static CommandLine cmd;
	private static String path;
	private static double delay;
	private static Config config;
	private static Controler controler;
	private static Scenario scenario;
	private final static boolean useMinTravelTimeModeChoice = true;

	public static void main(String[] args) {
		parseArguments(args);
		setConfig(path);
		createScenario();
		createControler().run();
	}

	public static void parseArguments(String[] args) {
		try {
			cmd = new CommandLine.Builder(args) //
					.allowPositionalArguments(false) //
					.allowOptions("config-path", "model-type", "no-vehicle-constraint", "track-car-travel-times",
							"use-advanced-vehicle-trip-constraint", "fallback-behaviour", "short-distance",
							"remove-routes", "use-custom-travel-time", "stuck-penalty", "delay-intersection") //
					.allowPrefixes("scoring") //
					.build();

			if (cmd.hasOption("config-path"))
				path = cmd.getOption("config-path").get();
			else
				path = "./config.xml";

			delay = 3.0;
			if (cmd.hasOption("delay-intersection"))
				delay = Double.parseDouble(cmd.getOptionStrict("delay-intersection"));

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		uamConfigGroup = new UAMConfigGroup();
	}

	public static Config createConfig() {
		return config = ConfigUtils.createConfig(uamConfigGroup, new DvrpConfigGroup());
	}

	public static Config setConfig(String path) {
		return config = ConfigUtils.loadConfig(path, uamConfigGroup, new DvrpConfigGroup());
	}

	public static Scenario createScenario() {
		scenario = ScenarioUtils.createScenario(config);
		ScenarioUtils.loadScenario(scenario);
		return scenario;
	}

	public static Scenario setScenario(Scenario scenario) {
		return RunUAMScenario.scenario = scenario;
	}

	public static Controler createControler() {
		try {
			cmd.applyConfiguration(config);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory());
		new LongPlanFilter(8, new StageActivityTypesImpl(PtConstants.TRANSIT_ACTIVITY_TYPE))
				.run(scenario.getPopulation());

		controler = new Controler(scenario);

		// Initiate Urban Air Mobility XML reading and parsing
		Network network = controler.getScenario().getNetwork();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Set<String> modes = new HashSet<>();
		modes.add("uam");
		Network networkUAM = NetworkUtils.createNetwork();
		filter.filter(networkUAM, modes);

		filter = new TransportModeNetworkFilter(network);
		Set<String> modesCar = new HashSet<>();
		modesCar.add("car");
		Network networkCar = NetworkUtils.createNetwork();
		filter.filter(networkCar, modesCar);

		// set up the UAM infrastructure
		UAMXMLReader uamReader = new UAMXMLReader(networkUAM);

		uamReader.readFile(ConfigGroup.getInputFileURL(controler.getConfig().getContext(), uamConfigGroup.getUAM())
				.getPath().replace("%20", " "));
		final UAMStations uamStations = new UAMStations(uamReader.getStations(), network);
		final UAMManager uamManager = new UAMManager(network);

		// populate UAMManager
		uamManager.setStations(uamStations);
		uamManager.setVehicles(uamReader.getVehicles());

		controler.addOverridingModule(new CustomModeChoiceModuleMinTravelTime(cmd, useMinTravelTimeModeChoice));

		// sets transit modules in case of simulating/not pT
		controler.getConfig().transit().setUseTransit(uamConfigGroup.getPtSimulation());
		if (uamConfigGroup.getPtSimulation()) {
			controler.addOverridingModule(new SwissRailRaptorModule());
			controler.addOverridingModule(new BaselineTransitModule());
		}
		controler.addOverridingModule(new CustomModule());
		controler.addOverridingModule(new UAMModule(uamManager, scenario, networkUAM, networkCar));
		controler.addOverridingModule(new UAMSpeedModule(delay, uamReader.getMapVehicleVerticalSpeeds(),
				uamReader.getMapVehicleHorizontalSpeeds()));
		controler.addOverridingModule(new DvrpTravelTimeModule());

		return controler;
	}
}
