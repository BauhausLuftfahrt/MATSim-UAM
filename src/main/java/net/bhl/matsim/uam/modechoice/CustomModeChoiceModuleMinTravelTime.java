package net.bhl.matsim.uam.modechoice;

import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.matsim.baseline_scenario.config.CommandLine;
import ch.ethz.matsim.baseline_scenario.config.CommandLine.ConfigurationException;
import ch.ethz.matsim.mode_choice.constraints.CompositeTourConstraintFactory;
import ch.ethz.matsim.mode_choice.constraints.CompositeTripConstraintFactory;
import ch.ethz.matsim.mode_choice.constraints.TourConstraintFromTripConstraint;
import ch.ethz.matsim.mode_choice.estimation.ModeAwareTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TourEstimatorFromTripEstimator;
import ch.ethz.matsim.mode_choice.estimation.TripEstimatorCache;
import ch.ethz.matsim.mode_choice.framework.ModeAvailability;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceModel;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceModel.FallbackBehaviour;
import ch.ethz.matsim.mode_choice.framework.tour_based.ActivityTourFinder;
import ch.ethz.matsim.mode_choice.framework.tour_based.TourFinder;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourCandidate;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourEstimator;
import ch.ethz.matsim.mode_choice.framework.trip_based.constraints.TripConstraintFactory;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelectorFactory;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGeneratorFactory;
import ch.ethz.matsim.mode_choice.prediction.TeleportationPredictor;
import ch.ethz.matsim.mode_choice.replanning.ModeChoiceModelStrategy;
import ch.ethz.matsim.mode_choice.replanning.NonSelectedPlanSelector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.config.UAMConfigGroup;
import net.bhl.matsim.uam.data.UAMStationConnectionGraph;
import net.bhl.matsim.uam.data.WaitingStationData;
import net.bhl.matsim.uam.dispatcher.UAMManager;
import net.bhl.matsim.uam.modechoice.constraints.*;
import net.bhl.matsim.uam.modechoice.estimation.CustomModeChoiceParameters;
import net.bhl.matsim.uam.modechoice.estimation.car.CustomCarEstimator;
import net.bhl.matsim.uam.modechoice.estimation.car.CustomCarPredictor;
import net.bhl.matsim.uam.modechoice.estimation.other.CustomBikeEstimator;
import net.bhl.matsim.uam.modechoice.estimation.other.CustomCarPassengerEstimator;
import net.bhl.matsim.uam.modechoice.estimation.other.CustomWalkEstimator;
import net.bhl.matsim.uam.modechoice.estimation.pt.CustomPublicTransportEstimator;
import net.bhl.matsim.uam.modechoice.estimation.pt.CustomPublicTransportPredictor;
import net.bhl.matsim.uam.modechoice.estimation.pt.subscription.SubscriptionFinder;
import net.bhl.matsim.uam.modechoice.estimation.uam.CustomUAMEstimator;
import net.bhl.matsim.uam.modechoice.estimation.uam.CustomUAMPredictor;
import net.bhl.matsim.uam.modechoice.model.MinTravelTimeModel;
import net.bhl.matsim.uam.modechoice.tracking.TrackingModeChoiceModel;
import net.bhl.matsim.uam.modechoice.tracking.TravelTimeTracker;
import net.bhl.matsim.uam.modechoice.tracking.TravelTimeTrackerListener;
import net.bhl.matsim.uam.qsim.UAMLinkSpeedCalculator;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.pt.config.TransitConfigGroup;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A MATSim Abstract Module for the classes used by the
 * {@link MinTravelTimeModel}.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */
public class CustomModeChoiceModuleMinTravelTime extends AbstractModule {
	private final CommandLine cmd;
	private final List<String> vehicleModes = new LinkedList<>();
	private final boolean isMinTravelTime;

	public CustomModeChoiceModuleMinTravelTime(CommandLine cmd, boolean isMinTravelTime) {
		this.cmd = cmd;
		this.isMinTravelTime = isMinTravelTime;
		if (!cmd.hasOption("no-vehicle-constraint")) {
			this.vehicleModes.addAll(Arrays.asList("car", "bike"));
		}
	}

	@Override
	public void install() {
		addPlanStrategyBinding("custom").toProvider(ModeChoiceModelStrategy.class);
		bindPlanSelectorForRemoval().to(NonSelectedPlanSelector.class);

		if (cmd.hasOption("track-car-travel-times")) {
			addControlerListenerBinding().to(TravelTimeTrackerListener.class);
			addEventHandlerBinding().to(TravelTimeTrackerListener.class);
		}

		bind(TravelTimeTracker.class);

		addTravelDisutilityFactoryBinding("car").to(CustomCarDisutility.Factory.class);

		// TODO: Probably can be completely kicked out soon
		// addEventHandlerBinding().to(FreeflowTravelTimeValidator.class);

	}

	@Provides
	@Singleton
	public CustomModeChoiceParameters provideCustomModeChoiceParameters() throws ConfigurationException {
		String prefix = "scoring:";

		List<String> scoringOptions = cmd.getAvailableOptions().stream().filter(o -> o.startsWith(prefix))
				.collect(Collectors.toList());

		Map<String, String> rawParameters = new HashMap<>();

		for (String option : scoringOptions) {
			rawParameters.put(option.substring(prefix.length()), cmd.getOptionStrict(option));
		}

		CustomModeChoiceParameters parameters = new CustomModeChoiceParameters();
		parameters.load(rawParameters);

		return parameters;
	}

	@Provides
	@Singleton
	public SubscriptionFinder provideSubscriptionFinder(Population population) {
		return new SubscriptionFinder(population.getPersonAttributes());
	}

	@Provides
	@Named("road")
	@Singleton
	public Network provideRoadNetwork(Network fullNetwork) {
		Network roadNetwork = NetworkUtils.createNetwork();
		new TransportModeNetworkFilter(fullNetwork).filter(roadNetwork, Collections.singleton("car"));
		return roadNetwork;
	}

	@Provides
	@Singleton
	public CustomCarDisutility.Factory provideCustomCarDisutilityFactory(UAMLinkSpeedCalculator speedCalculator) {
		return new CustomCarDisutility.Factory(speedCalculator);
	}

	@Provides
	@Singleton
	public UAMStationConnectionGraph provideUAMStationConnectionGraph(UAMManager uamManager,
																	  CustomModeChoiceParameters parameters, @Named("uam") ParallelLeastCostPathCalculator plcpc) {
		return new UAMStationConnectionGraph(uamManager, parameters, plcpc);
	}

	@Provides
	public ModeChoiceModel provideModeChoiceModel(PlansCalcRouteConfigGroup routeConfig,
												  CustomModeChoiceParameters parameters, TripRouter router, SubscriptionFinder subscriptionFinder,
												  ActivityFacilities facilities, Network network, TravelTimeTracker travelTimeTracker,
												  @Named("car") TravelTime carTravelTime, @Named("car") TravelDisutilityFactory carTravelDisutilityFactory,
												  UAMManager uamManager, Scenario scenario, TransitConfigGroup transitConfig, UAMConfigGroup uamConfig,
												  Map<String, TravelDisutilityFactory> travelDisutilityFactories, Map<String, TravelTime> travelTimes,
												  LeastCostPathCalculatorFactory lcpcf, @Named("car") Network networkCar,
												  @Named("uam") ParallelLeastCostPathCalculator plcpc, WaitingStationData waitingData,
												  UAMStationConnectionGraph stationConnectionutilities) throws ConfigurationException {
		ModeAvailability modeAvailability = new CarModeAvailability(parameters.getModes());

		double crowflyDistanceFactorWalk = routeConfig.getModeRoutingParams().get("walk").getBeelineDistanceFactor();
		double speedWalk = routeConfig.getModeRoutingParams().get("walk").getTeleportedModeSpeed();

		TeleportationPredictor teleportationPredictorWalk = new TeleportationPredictor(crowflyDistanceFactorWalk,
				speedWalk);
		CustomWalkEstimator walkEstimator = new CustomWalkEstimator(parameters, teleportationPredictorWalk,
				isMinTravelTime);

		double crowflyDistanceFactorBike = routeConfig.getModeRoutingParams().get("bike").getBeelineDistanceFactor();
		double speedBike = routeConfig.getModeRoutingParams().get("bike").getTeleportedModeSpeed();

		TeleportationPredictor teleportationPredictorBike = new TeleportationPredictor(crowflyDistanceFactorBike,
				speedBike);
		CustomBikeEstimator bikeEstimator = new CustomBikeEstimator(parameters, teleportationPredictorBike,
				isMinTravelTime);

		CustomPublicTransportPredictor publicTransportPredictor = new CustomPublicTransportPredictor(router, facilities,
				scenario);
		CustomPublicTransportEstimator publicTransportEstimator = new CustomPublicTransportEstimator(parameters,
				publicTransportPredictor, subscriptionFinder, isMinTravelTime);

		CustomCarPredictor carPredictor = new CustomCarPredictor(router, facilities,
				carTravelDisutilityFactory.createTravelDisutility(carTravelTime), network);
		CustomCarEstimator carEstimator = new CustomCarEstimator(parameters, carPredictor, isMinTravelTime);

//		CustomTaxiEstimator taxiEstimator = new CustomTaxiEstimator(parameters, carPredictor);

		Set<String> modes = uamConfig.getAvailableAccessModes();
		TravelTime travelTime = travelTimes.get(TransportMode.car);

		TravelDisutility travelDisutility = travelDisutilityFactories.get(TransportMode.car)
				.createTravelDisutility(travelTime);

		LeastCostPathCalculator pathCalculator = lcpcf.createPathCalculator(networkCar, travelDisutility, travelTime);

		CustomUAMPredictor uamPredictor = new CustomUAMPredictor(uamManager, scenario, waitingData, uamConfig,
				transitConfig, modes, networkCar, pathCalculator, router, parameters, stationConnectionutilities,
				subscriptionFinder);

		CustomUAMEstimator uamEstimator = new CustomUAMEstimator(parameters, uamPredictor, isMinTravelTime);

		ModeAwareTripEstimator modeAwareEstimator = new ModeAwareTripEstimator();
		modeAwareEstimator.addEstimator("walk", walkEstimator);
		modeAwareEstimator.addEstimator("bike", bikeEstimator);
		modeAwareEstimator.addEstimator("pt", publicTransportEstimator);
		modeAwareEstimator.addEstimator("car", carEstimator);
		modeAwareEstimator.addEstimator("car_passenger", new CustomCarPassengerEstimator());

		modeAwareEstimator.addEstimator("uam", uamEstimator);

		TripEstimatorCache estimator = new TripEstimatorCache(modeAwareEstimator, parameters.getCachedModes());

		// CONSTRAINTS
		double shortDistance = cmd.getOption("short-distance").map(Double::parseDouble)
				.orElse(ShortDistanceConstraint.DEFAULT_SHORT_DISTANCE);

		CompositeTourConstraintFactory tourConstraintFactory = new CompositeTourConstraintFactory();
		tourConstraintFactory
				.addFactory(new TourConstraintFromTripConstraint.Factory((new AvoidOnlyWalkConstraint.Factory())));
		tourConstraintFactory.addFactory(
				new TourConstraintFromTripConstraint.Factory((new ShortDistanceConstraint.Factory(shortDistance))));
		tourConstraintFactory.addFactory(new VehicleTourConstraint.Factory(vehicleModes));
		tourConstraintFactory
				.addFactory(new TourConstraintFromTripConstraint.Factory(new CarPassangerTripConstraint.Factory()));

		tourConstraintFactory.addFactory(
				new TourConstraintFromTripConstraint.Factory(new UAMTripConstraint.Factory(uamManager, uamConfig)));

		CompositeTripConstraintFactory tripConstraintFactory = new CompositeTripConstraintFactory();
		tripConstraintFactory.addFactory((new AvoidOnlyWalkConstraint.Factory()));
		tripConstraintFactory.addFactory(new CarPassangerTripConstraint.Factory());

		boolean useAdvancedVehicleTripConstraint = cmd.hasOption("use-advanced-vehicle-trip-constraint");
		TripConstraintFactory shortDistanceConstraintFactory = (new ShortDistanceConstraint.Factory(shortDistance));

		if (useAdvancedVehicleTripConstraint) {
			tripConstraintFactory.addFactory(new CustomHybridConstraint.Factory(
					new AdvancedVehicleTripConstraint.Factory(vehicleModes), shortDistanceConstraintFactory));
		} else {
			tripConstraintFactory.addFactory(shortDistanceConstraintFactory);
			tripConstraintFactory.addFactory(new VehicleTripConstraint.Factory(vehicleModes));
		}

		TourEstimator tourEstimator = new TourEstimatorFromTripEstimator(estimator);
		ModeChainGeneratorFactory modeChainGeneratorFactory = new CustomModeChainGenerator.Factory();

		TourFinder tourFinder = new ActivityTourFinder("home");

		UtilitySelectorFactory<TourCandidate> tourSelectorFactory = new MultinomialSelector.Factory<>(700.0);

		FallbackBehaviour fallbackBehaviour = cmd.getOption("fallback-behaviour").map(FallbackBehaviour::valueOf)
				.orElse(FallbackBehaviour.INITIAL_CHOICE);

		ModeChoiceModel model;

		model = new MinTravelTimeModel(tourEstimator, modeAvailability, tourConstraintFactory, tourFinder,
				tourSelectorFactory, modeChainGeneratorFactory, fallbackBehaviour);

		if (cmd.hasOption("track-car-travel-times")) {
			model = new TrackingModeChoiceModel(model, travelTimeTracker);
		}

		return model;
	}

}
