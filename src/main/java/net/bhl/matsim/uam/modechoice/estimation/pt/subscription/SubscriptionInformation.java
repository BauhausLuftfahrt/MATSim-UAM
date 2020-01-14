package net.bhl.matsim.uam.modechoice.estimation.pt.subscription;

/**
 * This class stores the logical value of a passenger Public transport
 * subscription.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class SubscriptionInformation {
	final public boolean hasSubscription;

	public SubscriptionInformation(boolean hasSubscription) {
		this.hasSubscription = hasSubscription;

	}
}
