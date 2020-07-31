package net.bhl.matsim.uam.router;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.MainModeIdentifier;

import java.util.List;

/**
 * This class identifies if a trip is using UAM or not.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMMainModeIdentifier implements MainModeIdentifier {
	private final MainModeIdentifier defaultModeIdentifier;

	public UAMMainModeIdentifier(final MainModeIdentifier defaultModeIdentifier) {
		this.defaultModeIdentifier = defaultModeIdentifier;
	}

	@Override
	public String identifyMainMode(List<? extends PlanElement> tripElements) {
		for (PlanElement pe : tripElements) {
			if (pe instanceof Leg && (((Leg) pe).getMode().equals(UAMModes.uam)
					|| ((Leg) pe).getMode().equals(UAMModes.access + TransportMode.walk)
					|| ((Leg) pe).getMode().equals(UAMModes.egress + TransportMode.walk)
					|| ((Leg) pe).getMode().equals(UAMModes.access + TransportMode.bike)
					|| ((Leg) pe).getMode().equals(UAMModes.egress + TransportMode.bike))) {
				return UAMModes.uam;
			}
		}
		// if the trip doesn't contain a uam leg,
		// fall back to the default identification method.
		return defaultModeIdentifier.identifyMainMode(tripElements);
	}
}