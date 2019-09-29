package net.bhl.matsim.uam.data;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage class for UAM leg chains (routes) of access mode, origin and destination stations, and egress mode
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 * <p>
 * TODO rework, with faster internal workings than massive HashMap!
 */
public class UAMRoutes {

	private static UAMRoutes instance;
	private Map<String, UAMRoute> routes;

	private UAMRoutes() {
		routes = new HashMap<>();
	}

	public static synchronized UAMRoutes getInstance() {
		if (instance == null) {
			instance = new UAMRoutes();
		}
		return instance;
	}

	public static String id(Id<Person> personId, double time) {
		return "" + personId.toString() + "_" + time;
	}

	public void add(Id<Person> person, double time, UAMRoute route) {
		routes.put(id(person, time), route);
	}

	public UAMRoute get(Id<Person> person, double time) {
		return routes.get(id(person, time));
	}

	public boolean contains(Id<Person> person, double time) {
		return routes.containsKey(id(person, time));
	}
}
