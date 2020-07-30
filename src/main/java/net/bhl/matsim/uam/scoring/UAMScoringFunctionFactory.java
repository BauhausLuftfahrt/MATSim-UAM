package net.bhl.matsim.uam.scoring;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.bhl.matsim.uam.router.UAMModes;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelScoringFunctionFactory;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.core.scoring.functions.SubpopulationScoringParameters;

/**
 * Create new scoring functions for UAM mode.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Singleton
public class UAMScoringFunctionFactory implements ScoringFunctionFactory {
	final private ScoringFunctionFactory standardFactory;
	final private ScoringParametersForPerson params;
	final private Network network;

	@Inject
	public UAMScoringFunctionFactory(Scenario scenario, @Named(UAMModes.UAM_MODE) Network network) {
		params = new SubpopulationScoringParameters(scenario);
		standardFactory = new CharyparNagelScoringFunctionFactory(scenario);
		this.network = network;
	}

	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		SumScoringFunction sf = (SumScoringFunction) standardFactory.createNewScoringFunction(person);
		return sf;
	}
}