package net.bhl.matsim.uam.modechoice.model;

import ch.ethz.matsim.mode_choice.framework.ModeAvailability;
import ch.ethz.matsim.mode_choice.framework.plan_based.PlanTourFinder;
import ch.ethz.matsim.mode_choice.framework.tour_based.constraints.TourConstraintFactory;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourCandidate;
import ch.ethz.matsim.mode_choice.framework.tour_based.estimation.TourEstimator;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelectorFactory;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGeneratorFactory;

public class PlanBasedModel extends TourBasedModel {
	public PlanBasedModel(TourEstimator estimator, ModeAvailability modeAvailability,
						  TourConstraintFactory constraintFactory, UtilitySelectorFactory<TourCandidate> selectorFactory,
						  ModeChainGeneratorFactory modeChainGeneratorFactory, FallbackBehaviour fallbackBehaviour) {
		super(estimator, modeAvailability, constraintFactory, new PlanTourFinder(), selectorFactory,
				modeChainGeneratorFactory, fallbackBehaviour);
	}
}