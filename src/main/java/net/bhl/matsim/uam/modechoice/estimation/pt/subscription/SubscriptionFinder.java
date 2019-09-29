package net.bhl.matsim.uam.modechoice.estimation.pt.subscription;

import org.matsim.api.core.v01.population.Person;
import org.matsim.utils.objectattributes.ObjectAttributes;

/**
 * This class is used to get public transport subscription information from the
 * passengers.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class SubscriptionFinder {
	final private ObjectAttributes personAttributes;

	public SubscriptionFinder(ObjectAttributes personAttributes) {
		this.personAttributes = personAttributes;
	}

	public SubscriptionInformation getSubscriptions(Person person) {
		boolean hasSubscription = false;
		if (person.getAttributes().getAttribute("ptSubscription") != null)
			hasSubscription = (boolean) person.getAttributes().getAttribute("ptSubscription");

		return new SubscriptionInformation(hasSubscription);
	}
}
