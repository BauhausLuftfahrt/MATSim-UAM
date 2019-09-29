package net.bhl.matsim.uam.modechoice.model;

/**
 * This class provides a mode choice model that selects trips based on their travel time.
 *
 * @author Aitanm (Aitan Militao), RRothfeld (Raoul Rothfeld)
 */

import ch.ethz.matsim.mode_choice.framework.ModeAvailability;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceModel;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceResult;
import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.tour_based.TourBasedModeChoiceResult;
import ch.ethz.matsim.mode_choice.framework.tour_based.TourFinder;
import ch.ethz.matsim.mode_choice.framework.tour_based.constraints.TourConstraint;
import ch.ethz.matsim.mode_choice.framework.tour_based.constraints.TourConstraintFactory;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourCandidate;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourEstimator;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelector;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelectorFactory;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGenerator;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGeneratorFactory;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;

import java.util.*;
import java.util.stream.Collectors;

public class MinTravelTimeModel implements ModeChoiceModel {
	final private static Logger logger = Logger.getLogger(MinTravelTimeModel.class);

	final private TourFinder tourFinder;
	final private TourEstimator estimator;
	final private ModeAvailability modeAvailability;
	final private TourConstraintFactory constraintFactory;
	final private UtilitySelectorFactory<TourCandidate> selectorFactory;
	final private ModeChainGeneratorFactory modeChainGeneratorFactory;
	final private FallbackBehaviour fallbackBehaviour;

	public MinTravelTimeModel(TourEstimator estimator, ModeAvailability modeAvailability,
							  TourConstraintFactory constraintFactory, TourFinder tourFinder,
							  UtilitySelectorFactory<TourCandidate> selectorFactory, ModeChainGeneratorFactory modeChainGeneratorFactory,
							  FallbackBehaviour fallbackBehaviour) {
		this.estimator = estimator;
		this.modeAvailability = modeAvailability;
		this.constraintFactory = constraintFactory;
		this.tourFinder = tourFinder;
		this.selectorFactory = selectorFactory;
		this.modeChainGeneratorFactory = modeChainGeneratorFactory;
		this.fallbackBehaviour = fallbackBehaviour;
	}

	@Override
	public ModeChoiceResult chooseModes(List<ModeChoiceTrip> trips, Random random) throws NoFeasibleChoiceException {
		List<String> modes = new ArrayList<>(modeAvailability.getAvailableModes(trips));
		TourConstraint constraint = constraintFactory.createConstraint(trips, modes);

		List<TourCandidate> tourCandidates = new LinkedList<>();
		List<List<String>> tourCandidateModes = new LinkedList<>();
		boolean ignoreAgentRequested = false;

		for (List<ModeChoiceTrip> tourTrips : tourFinder.findTours(trips)) {
			ModeChainGenerator generator = modeChainGeneratorFactory.createModeChainGenerator(modes, tourTrips);
			UtilitySelector<TourCandidate> selector = selectorFactory.createUtilitySelector();

			while (generator.hasNext()) {
				List<String> tourModes = generator.next();

				if (!constraint.validateBeforeEstimation(tourModes, tourCandidateModes)) {
					continue;
				}

				// Estimates the utility of a whole tour with given trips and modes with which
				// they should be performed.
				// utility = travel time for this model
				TourCandidate candidate = estimator.estimateTour(tourModes, tourTrips, tourCandidates);

				if (!constraint.validateAfterEstimation(candidate, tourCandidates)) {
					continue;
				}
				selector.addCandidate(candidate);
			}

			TourCandidate selectedCandidate = null;
			if (selector.getNumberOfCandidates() > 0) {
				Optional<TourCandidate> cand = selector.select(random);
				if (cand.isPresent())
					selectedCandidate = cand.get();
				else {
					if (fallbackBehaviour.equals(FallbackBehaviour.INITIAL_CHOICE)) {
						selectedCandidate = handleInitialChoiceFallback(tourTrips, tourCandidates);
					} else if (fallbackBehaviour.equals(FallbackBehaviour.EXCEPTION)) {
						throw new NoFeasibleChoiceException(
								buildFallbackMessage(tourTrips.get(0).getPerson(), "Throwing exception."));
					} else if (fallbackBehaviour.equals(FallbackBehaviour.IGNORE_AGENT)) {
						ignoreAgentRequested = true;
						break;
					}
				}
			} else if (fallbackBehaviour.equals(FallbackBehaviour.INITIAL_CHOICE)) {
				selectedCandidate = handleInitialChoiceFallback(tourTrips, tourCandidates);
			} else if (fallbackBehaviour.equals(FallbackBehaviour.EXCEPTION)) {
				throw new NoFeasibleChoiceException(
						buildFallbackMessage(tourTrips.get(0).getPerson(), "Throwing exception."));
			} else if (fallbackBehaviour.equals(FallbackBehaviour.IGNORE_AGENT)) {
				ignoreAgentRequested = true;
				break;
			}

			tourCandidates.add(selectedCandidate);
			tourCandidateModes.add(
					selectedCandidate.getTripCandidates().stream().map(c -> c.getMode()).collect(Collectors.toList()));
		}

		if (ignoreAgentRequested) {
			tourCandidates = handleIgnoreAgentFallback(trips);
		}

		return new TourBasedModeChoiceResult(tourCandidates);
	}

	private String buildFallbackMessage(Person person, String appendix) {
		return String.format("No feasible mode choice candidate for agent %s. %s", person.getId().toString(), appendix);
	}

	private TourCandidate handleInitialChoiceFallback(List<ModeChoiceTrip> tourTrips,
													  List<TourCandidate> tourCandidates) {
		logger.warn(buildFallbackMessage(tourTrips.get(0).getPerson(), "Using fallback."));

		List<String> initialModes = tourTrips.stream().map(ModeChoiceTrip::getInitialMode).collect(Collectors.toList());

		TourCandidate fallbackCandidate = estimator.estimateTour(initialModes, tourTrips, tourCandidates);
		fallbackCandidate.setFallback(true);

		return fallbackCandidate;
	}

	private List<TourCandidate> handleIgnoreAgentFallback(List<ModeChoiceTrip> trips) {
		logger.warn(buildFallbackMessage(trips.get(0).getPerson(), "Ignoring agent."));

		List<TourCandidate> tourCandidates = new LinkedList<>();

		for (List<ModeChoiceTrip> tourTrips : tourFinder.findTours(trips)) {
			List<String> initialmodes = tourTrips.stream().map(ModeChoiceTrip::getInitialMode)
					.collect(Collectors.toList());

			tourCandidates.add(estimator.estimateTour(initialmodes, tourTrips, tourCandidates));
		}

		tourCandidates.forEach(c -> c.setFallback(true));

		return tourCandidates;
	}

}
