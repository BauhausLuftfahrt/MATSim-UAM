package net.bhl.matsim.uam.run;

import ch.ethz.matsim.baseline_scenario.config.CommandLine;
import ch.ethz.matsim.baseline_scenario.config.CommandLine.ConfigurationException;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;
import ch.ethz.matsim.baseline_scenario.transit.simulation.BaselineTransitModule;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.infrastructure.UAMStations;
import net.bhl.matsim.uam.infrastructure.readers.UAMXMLReader;
import net.bhl.matsim.uam.modechoice.CustomModeChoiceModuleMinTravelTime;
import net.bhl.matsim.uam.modechoice.utils.LongPlanFilter;
import net.bhl.matsim.uam.qsim.UAMQsimModule;
import net.bhl.matsim.uam.qsim.UAMSpeedModule;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * The RunUAMScenario program start a MATSim run including Urban Air Mobility
 * capabilities.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class RunUAMScenario {

	private static UAMConfigGroup uamConfigGroup;
	private static CommandLine cmd;
	private static String path;
	private static Config config;
	private static Controler controler;
	private static Scenario scenario;

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
		controler = new Controler(scenario);

		// Initiate Urban Air Mobility XML reading and parsing
		Network network = controler.getScenario().getNetwork();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		Set<String> modes = new HashSet<>();
		modes.add(UAMModes.UAM_MODE);
		Network networkUAM = NetworkUtils.createNetwork();
		filter.filter(networkUAM, modes);

		filter = new TransportModeNetworkFilter(network);
		Set<String> modesCar = new HashSet<>();
		modesCar.add(TransportMode.car);
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

		// sets transit modules in case of simulating/not pT
		controler.getConfig().transit().setUseTransit(uamConfigGroup.getPtSimulation());
		if (uamConfigGroup.getPtSimulation()) {
			controler.addOverridingModule(new SwissRailRaptorModule());
			controler.addOverridingModule(new BaselineTransitModule());
		}

		controler.addOverridingModule(new CustomModule()); //taxi
		controler.addOverridingModule(new UAMModule(uamManager, networkUAM, networkCar, uamReader));
		controler.addOverridingModule(new UAMSpeedModule(uamReader.getMapVehicleVerticalSpeeds(),
				uamReader.getMapVehicleHorizontalSpeeds()));
		controler.addOverridingModule(new DvrpTravelTimeModule());

		controler.configureQSimComponents(configurator -> {UAMQsimModule.configureComponents(configurator);});

		return controler;
	}
}
