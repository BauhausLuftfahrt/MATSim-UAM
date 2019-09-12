package net.bhl.matsim.uam.scoring;

import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.SumScoringFunction;

import net.bhl.matsim.uam.passenger.UAMRequest;
import net.bhl.matsim.uam.schedule.UAMTransitEvent;

public class UAMScoringFunction implements SumScoringFunction.ArbitraryEventScoring {

	boolean uamTrip = false;
	private Person person;
	double score = 0.0;
	private double marginalDisutilityOfDistance;

	public UAMScoringFunction(Person person, double marginalDisutilityOfDistance, double marginalUtilityOfTraveling,
			Network network) {
		this.person = person;
		this.marginalDisutilityOfDistance = marginalDisutilityOfDistance;
	}

	@Override
	public void finish() {

	}

	@Override
	public double getScore() {
		return this.score;
	}

	@Override
	public void handleEvent(Event event) {

		if (event instanceof UAMTransitEvent) {
			if (((UAMTransitEvent) event).getPersonId() == this.person.getId()) {
				UAMRequest uamRequest = ((UAMTransitEvent) event).getRequest();
				scoreUAMLeg(uamRequest.getDistance());
			}
		}

	}

	private void scoreUAMLeg(double distance) {
		this.score += distance * this.marginalDisutilityOfDistance;
		
	}

}
