package net.bhl.matsim.uam.modechoice.estimation;

/**
 * This class provides the costs for public transport.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 *
 */
public class PublicTransportCostStructure {
	public double distanceCost_km(double crowflyDistance_km) {
		if (crowflyDistance_km <= 1.0) {
			return 4.51;
		} else if (crowflyDistance_km <= 2.0) {
			return 1.799;
		} else if (crowflyDistance_km <= 5.0) {
			return 1.31;
		} else if (crowflyDistance_km <= 10.0) {
			return 0.878;
		} else if (crowflyDistance_km <= 20.0) {
			return 0.685;
		} else if (crowflyDistance_km <= 30.0) {
			return 0.619;
		} else if (crowflyDistance_km <= 40.0) {
			return 0.676;
		} else if (crowflyDistance_km <= 50.0) {
			return 0.655;
		} else {
			return 0.5898;
		}
	}

	public double distanceCostZVV_km(double crowflyDistance_km) {
		if (crowflyDistance_km <= 30.0) {
			return 0;
		} else if (crowflyDistance_km <= 40) {
			return 0.437;
		} else if (crowflyDistance_km <= 50) {
			return 0.564;
		} else {
			return 0.491;
		}
	}
}
