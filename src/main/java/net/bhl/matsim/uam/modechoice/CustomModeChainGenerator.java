package net.bhl.matsim.uam.modechoice;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.framework.utils.DefaultModeChainGenerator;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGenerator;
import ch.ethz.matsim.mode_choice.framework.utils.ModeChainGeneratorFactory;
import org.matsim.core.router.TripStructureUtils.Trip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomModeChainGenerator implements ModeChainGenerator {
	static public Object lock = new Object();
	final private ModeChainGenerator delegate;
	final private List<Integer> variableIndices;
	final private List<String> initialModes;

	public CustomModeChainGenerator(ModeChainGenerator delegate, List<Integer> variableIndices,
									List<String> initialModes) {
		this.variableIndices = variableIndices;
		this.delegate = delegate;
		this.initialModes = initialModes;
	}

	@Override
	public boolean hasNext() {
		return delegate.hasNext();
	}

	@Override
	public List<String> next() {
		List<String> result = new ArrayList<>(initialModes);
		List<String> variableModes = delegate.next();

		for (int i = 0; i < variableModes.size(); i++) {
			result.set(variableIndices.get(i), variableModes.get(i));
		}

		return result;
	}

	@Override
	public int getNumberOfAlternatives() {
		return delegate.getNumberOfAlternatives();
	}

	static public class Factory implements ModeChainGeneratorFactory {
		@Override
		public ModeChainGenerator createModeChainGenerator(Collection<String> modes, List<ModeChoiceTrip> trips) {
			List<String> availableModes = modes.stream().filter(m -> !m.equals("outside")).collect(Collectors.toList());

			List<Integer> variableIndices = new LinkedList<>();

			List<String> initialModes = trips.stream().map(ModeChoiceTrip::getInitialMode).collect(Collectors.toList());

			for (int i = 0; i < trips.size(); i++) {
				boolean isFixed = false;
				Trip trip = trips.get(i).getTripInformation();

				if (trip.getOriginActivity().getType().equals("outside")) {
					isFixed = true;
				}

				if (trip.getDestinationActivity().getType().equals("outside")) {
					isFixed = true;
				}

				if (trip.getLegsOnly().get(0).getMode().equals("outside")) {
					isFixed = true;
				}

				if (!isFixed) {
					variableIndices.add(i);
				}
			}

			DefaultModeChainGenerator delegate = new DefaultModeChainGenerator(availableModes, variableIndices.size());
			return new CustomModeChainGenerator(delegate, variableIndices, initialModes);
		}
	}
}
