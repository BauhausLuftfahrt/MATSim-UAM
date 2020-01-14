package net.bhl.matsim.uam.modechoice.estimation;

import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionInformation;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class stores and provides the parameters used by the mode choice models.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomModeChoiceParameters {
	private static List<String> MODES = Arrays.asList("car", "pt", "bike", "walk", "car_passenger", "uam");
	private static List<String> CACHED_MODES = Arrays.asList("car", "pt", "bike", "walk", "uam");
	private static Logger logger = Logger.getLogger(CustomModeChoiceParameters.class);
	// Cost
	public double betaCostBase = -0.923;
	public double betaCostHighIncome = 0.0;
	public double averageDistance_km = 20.0; // not used currently
	public double topTenIncomeSaoPaulo = 0.0;
	public double lambdaDistanceCost = 0.0; // not used currently
	public double averageIncome = 3005.0;
	public double lambdaIncome = -0.15;
	// Walk
	public double alphaWalk = 1.6;
	public double betaTravelTimeWalk_min = -0.507;
	// Bike
	public double alphaBike = -0.544;
	public double betaTravelTimeBike_min = -0.2305;
	public double betaAgeBike_yr = -0.0496;
	public double averageAge = 35;
	// Car
	public double alphaCar = 0.0251;
	public double betaTravelTimeCar_min = -0.153;
	public double betaTravelTImeCarMale = 0.0283;
	public double distanceCostCar_km = 0.2;
	public double parkingSearchTimeCar_min = 4.0;
	public double accessEgressWalkTimeCar_min = 4.0;
	public double betaParking = 1.38;
	public double betaParkingCity = -0.0;
	public double betaShortCar = -1.0;
	public double betaResidenceRuralCar = 0.694;
	// Public Transport
	public double alphaPublicTransport = 0.0;
	public double betaNumberOfTransfersPublicTransport = -0.0;
	public double betaInVehicleTimePublicTransport_min = -0.0330;
	public double betaInVehicleTimeOldPublicTransport = 0.00559;
	public double betaTransferTimePublicTransport_min = -0.0397;
	public double betaAccessEgressTimePublicTransport_min = -0.0885;
	public double betaLongPublicTransport = 0.305;
	public double betaShortPublicTransport = -0.737;
	public double betaResidenceRuralPublicTransport = 0.111;
	// Taxi
	public double alphaTaxi = 1.227;
	public double betaTravelTimeTaxi_min = -0.0667;
	public double betaWaitingTimeTaxi_min = -0.0484;
	public double initialCostTaxi = 5.0;
	public double distanceCostTaxi_km = 1.3;
	public double timeCostTaxi_min = 0.5;
	public double waitingTimeTaxi_min = 5.0;
	// Motorcycle
	public double alphaMC = 1.227;
	public double betaTravelTimeMC_min = -0.1;
	public double distanceCostMC_km = 0.07;
	public double accessEgressWalkTimeMC_min = 2.0;
	// uam
	public double alphaUAM = 0.0;
	public double betaTravelTimeUAM_min = -0.0329;
	public double distanceCostUAM_km = 1.8;
	public double betaWaitUAM_min = -0.0396;
	public double betaUAMTransfer = 0.0;
	public double storeUAMUtilities = 0; // 0 false; 1 true;
	public double betaWalkUAM_min = -0.0667;
	public double distanceCostUAMCar_km = 1.0;
	public double defaultWaitTime_sec = 0.0;

	public CustomModeChoiceParameters() {

	}

	public List<String> getModes() {
		return MODES;
	}

	public List<String> getCachedModes() {
		return CACHED_MODES;
	}

	public double betaCost(double distance_km, double income) {
		distance_km = Math.max(distance_km, 0.001);
		income = Math.max(income, 1.0);
		return betaCostBase * Math.pow(income / averageIncome, lambdaIncome)
				* Math.pow(distance_km / averageDistance_km, lambdaDistanceCost);
	}

	public double costPublicTransport(SubscriptionInformation subscriptions, double crowflyDistance_km) {
		if (crowflyDistance_km < 10.0)
			return 2.5;
		return (int) Math.ceil(crowflyDistance_km / 5.0) * 2.0;
	}

	public void load(Map<String, String> parameters) {
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			try {
				Field field = CustomModeChoiceParameters.class.getField(entry.getKey());
				field.set(this, Double.valueOf(entry.getValue()));
				logger.info(String.format("Set %s = %f", entry.getKey(), Double.valueOf(entry.getValue())));
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
