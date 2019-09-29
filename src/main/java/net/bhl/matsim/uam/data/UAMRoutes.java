package net.bhl.matsim.uam.data;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

/**
 * Storage class for UAM leg chains (routes) of access mode, origin and destination stations, and egress mode
 * @author Raoul
 *
 * TODO rework, with faster internal workings than massive HashMap!
 *
 */
public class UAMRoutes {
	
    private Map<String, UAMRoute> routes;
    
    private static UAMRoutes instance;
    
    private UAMRoutes() {
    	routes = new HashMap<>();
    }
    
    public static synchronized UAMRoutes getInstance() {
        if(instance == null) {
            instance = new UAMRoutes();
        }
        return instance;
    }
	
	public void add(Id<Person> person, double time, UAMRoute route) {
		routes.put(id(person, time), route);
	}
	
	public UAMRoute get(Id<Person> person, double time) {
		return  routes.get(id(person, time));
	}
	
	public boolean contains(Id<Person> person, double time) {
		return routes.containsKey(id(person, time));
	}
	
	public static String id(Id<Person> personId, double time) {
		return "" + personId.toString() + "_" + time;
	}
}
