package net.bhl.matsim.uam.data;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;

import java.util.List;

public class UAMAccessLeg extends UAMFlightLeg {
    public UAMAccessLeg(double travelTime, double distance, List<Link> links) {
        super(travelTime, distance, links);
    }
}