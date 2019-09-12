package net.bhl.matsim.uam.scenario.population;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;

//Adjusted from RunPopulationDownsamplingExample.java by matsim-code-examples

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.households.Household;
import org.matsim.households.Households;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.households.Income;

import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;

@Deprecated
public class RunCreateAirportPassengers {
	private static double popPrct = 0.5;
	
	private static int earliestHour = 5;
	private static int latestHour = 22;

	private static int earliestAge = 23;
	private static int oldestAge = 75;
	
	private static String currency = "CHF";
	
	void run(final String[] args) {
		// ARGS: population* households percent
		// * required
		
		if (args.length > 1)
			popPrct = Double.parseDouble(args[1]);
		System.out.println("Population Sample Size: " + popPrct);
		
		Set<Airport> airports = new HashSet<Airport>();
		
		// Paris (DO NOT USE UAM LINK! USE NORMAL ROAD LINK, since if population is used on a different UAM
		// network without said UAM link, the simulation will not run!)

//		airports.add(new Airport(Id.create("290668", Link.class),
//				new Coord(666209.35437217, 6878858.46579744),
//				"CDG",
//				(int) Math.max(1, Math.round(89819 * popPrct)), // departing
//				(int) Math.max(1, Math.round(89772 * popPrct)))); // arriving
//		
//		airports.add(new Airport(Id.create("352822", Link.class),
//				new Coord(653070.361381401, 6847964.98858385),
//				"ORY",
//				(int) Math.max(1, Math.round(43784 * popPrct)), // departing
//				(int) Math.max(1, Math.round(43755 * popPrct)))); // arriving
		
		// Sao Paulo
		airports.add(new Airport(Id.create("281285", Link.class),
		new Coord(332618.0847, 7385146.449),
		"CGH",
		(int) Math.max(1, Math.round(29944 * popPrct)), // departing per day
		(int) Math.max(1, Math.round(29944 * popPrct)))); // arriving per day
		
		airports.add(new Airport(Id.create("230341", Link.class),
		new Coord(349128.3068, 7408896.666),
		"GRU",
		(int) Math.max(1, Math.round(51803 * popPrct)), // departing per day
		(int) Math.max(1, Math.round(51803 * popPrct)))); // arriving per day
		
		// Jakarta
//		airports.add(new Airport(Id.create("28318", Link.class),
//		new Coord(3528658.934, 223298.16),
//		"CGK",
//		(int) Math.max(1, Math.round(86323 * popPrct)), // departing per day
//		(int) Math.max(1, Math.round(86323 * popPrct)))); // arriving per day
//		
//		airports.add(new Airport(Id.create("602678", Link.class),
//		new Coord(3554288.905, 208004.207),
//		"HLP",
//		(int) Math.max(1, Math.round(7690 * popPrct)), // departing per day
//		(int) Math.max(1, Math.round(7690 * popPrct)))); // arriving per day

		// Usage: cmd inputPop.xml.gz inputHH.xml.gz
		String inputPopFilename = args[0];
		String inputHhFilename = null;
		boolean ifHouseholds = args.length > 2;
		if (ifHouseholds)
			inputHhFilename = args[2];

		Config config = ConfigUtils.createConfig();
		config.plans().setInputFile(inputPopFilename);
		if (ifHouseholds)
			config.households().setInputFile(inputHhFilename);

		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
		    new DefaultEnrichedTransitRouteFactory());
		ScenarioUtils.loadScenario(scenario);
		
		Population pop = scenario.getPopulation();
		PopulationFactory factory = pop.getFactory();

		// Read home coords + link + facility from input popfile
		Stack<Home> homes = new Stack<Home>();
		for (Person person : pop.getPersons().values()) {
			List<PlanElement> elems = person.getSelectedPlan().getPlanElements();
			for (PlanElement elem : elems) {
				if (elem instanceof Activity && ((Activity) elem).getType() == "home") {
					homes.add(new Home(((Activity) elem).getLinkId(), ((Activity) elem).getCoord()));
					break;
				}
			}
		}
		Collections.shuffle(homes);
		
		Households households = null;
		double minIncome = 0;
		double maxIncome = 0;
		if (ifHouseholds) {
			households = scenario.getHouseholds();
			
			for (Household h : households.getHouseholds().values()) {
				minIncome = Math.min(h.getIncome().getIncome(), minIncome);
				maxIncome = Math.max(h.getIncome().getIncome(), maxIncome);
			}
		}
		
		// create defined number of agents with new IDs, random attributes and
		for (Airport airport : airports) {
			for (int i = 0; i < airport.arrivingPax; i++) {
				
				String id = airport.code + "ARR" + i;
				Person p = createPerson(id, factory, homes.pop(), airport, false);
				pop.addPerson(p);
				
				if (ifHouseholds) {
					Household h = createHousehold(id, households, maxIncome, minIncome, p);
					households.getHouseholds().put(h.getId(), h);
				}
			}
			
			for (int i = 0; i < airport.departingPax; i++) {
				String id = airport.code + "DEP" + i;
				Person p = createPerson(id, factory, homes.pop(), airport, true);
				pop.addPerson(p);
				
				if (ifHouseholds) {
					Household h = createHousehold(id, households, maxIncome, minIncome, p);
					households.getHouseholds().put(h.getId(), h);
				}
			}
		}

		// add pax to total pop and print new population file
		PopulationWriter popwriter = new PopulationWriter(pop);
		String[] inputPop = inputPopFilename.split(".xml");
		popwriter.write(inputPop[0] + "_pax.xml" + inputPop[1]);
		
		if (ifHouseholds) {
			String[] inputHh = inputHhFilename.split(".xml");
			HouseholdsWriterV10 hhwriter = new HouseholdsWriterV10(households);
			hhwriter.writeFile(inputHh[0] + "_pax.xml" + inputHh[1]);
		}
		
		System.out.println("done.");
	}
	
	private static Household createHousehold(String id, Households households, double max, double min, Person p) {
		Household h = households.getFactory().createHousehold(Id.create(id + "HH", Household.class));
		Income inc = households.getFactory().createIncome(
				new Random().nextInt((int) ((max - (min + ((max - min) / 2)) + 1) + min)),
				Income.IncomePeriod.month);
		inc.setCurrency(currency);
		h.setIncome(inc);
		h.getMemberIds().add(p.getId());
		
		h.getAttributes().putAttribute("numberOfCars", p.getAttributes().getAttribute("carAvailability") == "all" ? 1 : 0);
		h.getAttributes().putAttribute("carAvailability", p.getAttributes().getAttribute("carAvailability"));
		h.getAttributes().putAttribute("bikeAvailability", p.getAttributes().getAttribute("bikeAvailability"));
		h.getAttributes().putAttribute("residenceZoneCategory",2); // TODO

		return h;
	}
	
	private static Person createPerson(String id, PopulationFactory factory, Home home, Airport airport, Boolean departing) {
		Person p = factory.createPerson(Id.createPersonId(id));
		
		Plan plan = factory.createPlan();
		
		double time = ((new Random().nextInt((latestHour - earliestHour) + 1) + earliestHour) * 3600) + (new Random().nextInt(3599));
		
		Activity firstAct;
		if (departing) {
			firstAct = factory.createActivityFromCoord("home", home.coord);
			firstAct.setLinkId(home.link);
			firstAct.setEndTime(time);
		} else {
			firstAct = factory.createActivityFromCoord("work", airport.coord);
			firstAct.setLinkId(airport.link);
			firstAct.setEndTime(time);
		}

		Activity scndAct;
		if (!departing) {
			scndAct = factory.createActivityFromCoord("home", home.coord);
			scndAct.setLinkId(home.link);
			scndAct.setStartTime(time + 3600);
		} else {
			scndAct = factory.createActivityFromCoord("work", airport.coord);
			scndAct.setLinkId(airport.link);
			scndAct.setStartTime(time + 3600);
		}
			
		Leg l = factory.createLeg("walk");
		l.setDepartureTime(time);
		l.setTravelTime(3600);
		
		Route r = factory.getRouteFactories().createRoute(Route.class, firstAct.getLinkId(), scndAct.getLinkId());
		r.setTravelTime(3600);
		r.setDistance(CoordUtils.calcEuclideanDistance(firstAct.getCoord(), scndAct.getCoord()));
		l.setRoute(r);
		
		plan.addActivity(firstAct);
		plan.addLeg(l);
		plan.addActivity(scndAct);
		p.addPlan(plan);
		p.setSelectedPlan(plan);
		
		// attributes		
		p.getAttributes().putAttribute("age", new Random().nextInt((oldestAge - earliestAge) + 1) + earliestAge);
		p.getAttributes().putAttribute("employed", new Random().nextBoolean());		
		p.getAttributes().putAttribute("ptSubscription", new Random().nextBoolean());
		p.getAttributes().putAttribute("sex", new Random().nextBoolean() ? "m" : "f");
		p.getAttributes().putAttribute("bikeAvailability", "none");
		p.getAttributes().putAttribute("income", 4500.0); // TODO
		p.getAttributes().putAttribute("isPassenger", false);
		p.getAttributes().putAttribute("residence", 2); // TODO
		
		Double rand = new Random().nextDouble();
		Double airportAccessCar = 0.3;
		p.getAttributes().putAttribute("hasLicense", rand < airportAccessCar ? "yes" : "no");
		p.getAttributes().putAttribute("carAvailability", rand < airportAccessCar ? "all" : "none");
		
		return p;	
	}

	public static void main(final String[] args) {
		RunCreateAirportPassengers app = new RunCreateAirportPassengers();
		app.run(args);
	}

	private class Location {
		public Id<Link> link;
		public Coord coord;

		public Location(Id<Link> link, Coord coord) {
			this.link = link;
			this.coord = coord;
		}
	}

	private class Home extends Location {
		public Home(Id<Link> link, Coord coord) {
			super(link, coord);
		}
	}

	private class Airport extends Location {
		public String code;
		public int departingPax;
		public int arrivingPax;

		public Airport(Id<Link> link, Coord coord, String code, int departingPax, int arrivingPax) {
			super(link, coord);
			this.code = code;
			this.departingPax = departingPax;
			this.arrivingPax = arrivingPax;
		}
	}
}
