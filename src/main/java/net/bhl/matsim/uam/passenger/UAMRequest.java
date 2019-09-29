package net.bhl.matsim.uam.passenger;

import net.bhl.matsim.uam.dispatcher.Dispatcher;
import net.bhl.matsim.uam.schedule.UAMDropoffTask;
import net.bhl.matsim.uam.schedule.UAMPickupTask;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.RequestImpl;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

/**
 * This class defines the UAM Request and its properties.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMRequest implements PassengerRequest {

	final private Link originLink;
	final private Link destinationLink;
	final private MobsimPassengerAgent passengerAgent;
	final private Request delegate;
	private UAMDropoffTask dropoffTask;
	private UAMPickupTask pickupTask;
	private Dispatcher dispatcher;
	private double distance;

	public UAMRequest(Id<Request> id, MobsimPassengerAgent passengerAgent, Link originLink, Link destinationLink,
					  double pickupTime, double submissionTime, Dispatcher dispatcher, double distance) {
		this.delegate = new RequestImpl(id, 1.0, pickupTime, pickupTime, submissionTime);

		this.passengerAgent = passengerAgent;
		this.originLink = originLink;
		this.destinationLink = destinationLink;
		this.dispatcher = dispatcher;
		this.distance = distance;
	}

	@Override
	public double getQuantity() {
		return delegate.getQuantity();
	}

	@Override
	public double getEarliestStartTime() {
		return delegate.getEarliestStartTime();
	}

	@Override
	public double getLatestStartTime() {
		return delegate.getLatestStartTime();
	}

	@Override
	public double getSubmissionTime() {
		return delegate.getSubmissionTime();
	}

	@Override
	public boolean isRejected() {
		return false;
	}

	@Override
	public Id<Request> getId() {
		return delegate.getId();
	}

	@Override
	public Link getFromLink() {
		return this.originLink;
	}

	@Override
	public Link getToLink() {
		return this.destinationLink;
	}

	@Override
	public MobsimPassengerAgent getPassenger() {
		return this.passengerAgent;
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
