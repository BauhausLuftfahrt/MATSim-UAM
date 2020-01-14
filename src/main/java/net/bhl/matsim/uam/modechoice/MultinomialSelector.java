package net.bhl.matsim.uam.modechoice;

import ch.ethz.matsim.mode_choice.framework.utilities.UtilityCandidate;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelector;
import ch.ethz.matsim.mode_choice.framework.utilities.UtilitySelectorFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class selects trip candidates for the mode choice models.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class MultinomialSelector<T extends UtilityCandidate> implements UtilitySelector<T> {
	final private List<T> candidates = new LinkedList<>();
	final private double utilityCutoff;

	public MultinomialSelector(double utilityCutoff) {
		this.utilityCutoff = utilityCutoff;
	}

	@Override
	public void addCandidate(T candidate) {
		candidates.add(candidate);
	}

	@Override
	public int getNumberOfCandidates() {
		return candidates.size();
	}

	@Override
	public Optional<T> select(Random random) {
		if (candidates.size() == 0) {
			return Optional.empty();
		}

		List<T> filteredCandidates = candidates.stream() //
				.filter(c -> c.getUtility() > -utilityCutoff) //
				.collect(Collectors.toList());

		if (filteredCandidates.size() == 0) {
			return Optional.empty();
		}

		List<Double> density = filteredCandidates.stream() //
				.map(UtilityCandidate::getUtility) //
				.map(u -> Math.min(u, utilityCutoff)) //
				.map(Math::exp) //
				.collect(Collectors.toList());

		List<Double> cumulativeDensity = new ArrayList<>(density.size());
		double totalDensity = 0.0;

		for (int i = 0; i < density.size(); i++) {
			totalDensity += density.get(i);
			cumulativeDensity.add(totalDensity);
		}

		double pointer = random.nextDouble() * totalDensity;

		int selection = (int) cumulativeDensity.stream().filter(f -> f < pointer).count();
		return Optional.of(filteredCandidates.get(selection));
	}

	public static class Factory<TF extends UtilityCandidate> implements UtilitySelectorFactory<TF> {
		final private double utilityCutoff;

		public Factory(double utilityCutoff) {
			this.utilityCutoff = utilityCutoff;
		}

		@Override
		public UtilitySelector<TF> createUtilitySelector() {
			return new MultinomialSelector<>(utilityCutoff);
		}
	}
}
