package net.bhl.matsim.uam.scenario;

import org.matsim.api.core.v01.Scenario;

//Adjusted from RunPopulationDownsamplingExample.java by matsim-code-examples

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;

public class RunSamplePopulation {
	void run(final String[] args) {
		String inputPopFilename = null;
		String outputPopFilename = null;
		String netFilename = null;
		Double percentage = 0.1;

		if (args != null) {
			if (!(args.length == 3 || args.length == 4)) {
				System.err.println("Usage: cmd inputPop.xml.gz outputPop.xml.gz 0.X [network.xml.gz]");
			} else {
				inputPopFilename = args[0];
				outputPopFilename = args[1];
				percentage = Double.parseDouble(args[2]);
				
				if (args.length == 4) {
					netFilename = args[3];
				}
			}
		}

		Config config = ConfigUtils.createConfig();
		config.plans().setInputFile(inputPopFilename);
		if (args.length == 4)
			config.network().setInputFile(netFilename);
		

		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
		    new DefaultEnrichedTransitRouteFactory());
		ScenarioUtils.loadScenario(scenario);

		Population pop = scenario.getPopulation();
		
		Scenario newScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		newScenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
		    new DefaultEnrichedTransitRouteFactory());
		ScenarioUtils.loadScenario(newScenario);

		Population newPop = newScenario.getPopulation();
		
		// if input percentage is >0 (i.e. not a percentage but a defined number of agents to sub-sample)
		if (percentage > 1) {
			double totalPop = pop.getPersons().values().size();
			percentage = percentage / totalPop;
			System.err.println("Adjusting percentage to: " + percentage + System.lineSeparator());
		}
		
		for (Person person : pop.getPersons().values()) {
			if (Math.random() < percentage) {
				newPop.addPerson(person);
			}
		}

		PopulationWriter popwriter;
		if (args.length == 4)
			popwriter = new PopulationWriter(newPop, ScenarioUtils.loadScenario(config).getNetwork());
		else
			popwriter = new PopulationWriter(newPop);

		popwriter.write(outputPopFilename);

		System.out.println("done.");
	}

	public static void main(final String[] args) {
		RunSamplePopulation app = new RunSamplePopulation();
		app.run(args);
	}

}
