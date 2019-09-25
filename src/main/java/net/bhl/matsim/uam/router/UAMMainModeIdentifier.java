package net.bhl.matsim.uam.router;

import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.MainModeIdentifier;

public class UAMMainModeIdentifier implements MainModeIdentifier {
	private final MainModeIdentifier defaultModeIdentifier;

	public UAMMainModeIdentifier(final MainModeIdentifier defaultModeIdentifier) {
		this.defaultModeIdentifier = defaultModeIdentifier;
	}

	@Override
	public String identifyMainMode(List<? extends PlanElement> tripElements) {
		for ( PlanElement pe : tripElements ) {
			if ( pe instanceof Leg && (((Leg) pe).getMode().equals( UAMIntermodalRoutingModule.TELEPORTATION_UAM_LEG_MODE ) 
					|| ((Leg)pe).getMode().equals(UAMIntermodalRoutingModule.UAM_ACCESS_WALK) 
					|| ((Leg)pe).getMode().equals(UAMIntermodalRoutingModule.UAM_EGRESS_WALK)
					|| ((Leg)pe).getMode().equals(UAMIntermodalRoutingModule.UAM_ACCESS_BIKE)
					|| ((Leg)pe).getMode().equals(UAMIntermodalRoutingModule.UAM_EGRESS_BIKE)))  {
				return UAMIntermodalRoutingModule.TELEPORTATION_UAM_LEG_MODE;
			}
		}
		// if the trip doesn't contain a uam leg,
		// fall back to the default identification method.
		return defaultModeIdentifier.identifyMainMode( tripElements );
	}
}