package net.bhl.matsim.uam.modechoice;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PersonUtils;

import ch.ethz.matsim.mode_choice.framework.ModeChoiceTrip;
import ch.ethz.matsim.mode_choice.mode_availability.DefaultModeAvailability;

/**
 * This class is used to filter trips according to the person availability of
 * car and bike modes.
 * 
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CarModeAvailability extends DefaultModeAvailability {

	public CarModeAvailability(Collection<String> modes) {
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

			if (!PersonUtils.hasLicense(person)) {
				modes.remove("car");
			}
			
			if (person.getAttributes().getAttribute("carAvailability") != null) {
				if (person.getAttributes().getAttribute("carAvailability").equals("none") ||person.getAttributes().getAttribute("carAvailability").equals("never")) {
					modes.remove("car");
				}
			}
			
			if (person.getAttributes().getAttribute("bikeAvailability") != null) {
				if (person.getAttributes().getAttribute("bikeAvailability").equals("none")) {
					modes.remove("bike");
				}
			}
			
			
			return modes;
		}

		return super.getAvailableModes(trips);
	}

}
