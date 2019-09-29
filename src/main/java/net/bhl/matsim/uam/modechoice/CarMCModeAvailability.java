package net.bhl.matsim.uam.modechoice;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.mode_availability.DefaultModeAvailability;
import org.matsim.api.core.v01.population.Person;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class CarMCModeAvailability extends DefaultModeAvailability {
	final private String CAR_MODE = "car";
	final private String MC_MODE = "mc";

	public CarMCModeAvailability(Collection<String> modes) {
		super(modes);
	}

	@Override
	public Collection<String> getAvailableModes(List<ModeChoiceTrip> trips) {
		if (trips.size() > 0) {
			Person person = trips.get(0).getPerson();

			Collection<String> modes = new HashSet<>();
			for (String m : super.getAvailableModes(trips)) {

				modes.add(m);
			}
			if (person.getAttributes().getAttribute("carAvailability").equals("none")) {

				modes.remove("car");
			}

			if (person.getAttributes().getAttribute("motorcycleAvailability").equals("none")) {
				modes.remove("mc");
			}
			return modes;
		}

		return super.getAvailableModes(trips);
	}

}
