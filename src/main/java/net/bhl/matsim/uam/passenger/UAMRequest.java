package net.bhl.matsim.uam.passenger;

import net.bhl.matsim.uam.dispatcher.Dispatcher;
import net.bhl.matsim.uam.router.UAMModes;
import net.bhl.matsim.uam.schedule.UAMDropoffTask;
import net.bhl.matsim.uam.schedule.UAMPickupTask;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

/**
 * This class defines the UAM Request and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMRequest implements PassengerRequest {
	final private Id<Request> id;
	final private double submissionTime;
	final private Link originLink;
	final private Link destinationLink;
	final private MobsimPassengerAgent passengerAgent;
	private final double quantity;
	private final double earliestStartTime;
	private final double latestStartTime;
	private UAMDropoffTask dropoffTask;
	private UAMPickupTask pickupTask;
	private Dispatcher dispatcher;
	private double distance;

	public UAMRequest(Id<Request> id, MobsimPassengerAgent passengerAgent, Link originLink, Link destinationLink,
					  double pickupTime, double submissionTime, Dispatcher dispatcher, double distance) {
		this.id = id;
		this.submissionTime = submissionTime;
		this.quantity = 1.0;
		this.originLink = originLink;
		this.destinationLink = destinationLink;
		this.earliestStartTime = pickupTime;
		this.latestStartTime = pickupTime;
		this.passengerAgent = passengerAgent;
		this.dispatcher = dispatcher;
		this.distance = distance;
	}

	public double getQuantity() {
		return this.quantity;
	}

	@Override
	public double getEarliestStartTime() {
		return this.earliestStartTime;
	}

	@Override
	public double getLatestStartTime() {
		return this.earliestStartTime;
	}

	@Override
	public double getSubmissionTime() {
		return this.submissionTime;
	}

	@Override
	public boolean isRejected() {
		return false;
	}

	@Override
	public void setRejected(boolean rejected) {
		// TODO Auto-generated method stub
	}

	@Override
	public Id<Request> getId() {
		return this.id;
	}

	@Override
	public Id<Person> getPassengerId() {
		return passengerAgent.getId();
	}

	@Override
	public Link getFromLink() {
		return this.originLink;
	}

	@Override
	public Link getToLink() {
		return this.destinationLink;
	}

	public MobsimPassengerAgent getPassenger() {
		return this.passengerAgent;
	}

	@Override
	public String getMode() {
		return UAMModes.UAM_MODE;
	}

	public UAMDropoffTask getDropoffTask() {
		return dropoffTask;
	}

	public void setDropoffTask(UAMDropoffTask dropoffTask) {
		this.dropoffTask = dropoffTask;
	}

	public UAMPickupTask getPickupTask() {
		return pickupTask;
	}

	public void setPickupTask(UAMPickupTask pickupTask) {
		this.pickupTask = pickupTask;
	}

	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

}
