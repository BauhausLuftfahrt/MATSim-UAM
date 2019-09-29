package net.bhl.matsim.uam.run;

import net.bhl.matsim.uam.router.TaxiRoutingModuleProvider;
import org.matsim.core.controler.AbstractModule;

/**
 * A MATSim Abstract Module for the Taxi mode routing module.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class CustomModule extends AbstractModule {

	@Override
	public void install() {

		addRoutingModuleBinding("taxi").toProvider(TaxiRoutingModuleProvider.class);

	}

}
