package net.bhl.matsim.uam.run;

import net.bhl.matsim.uam.router.TaxiRoutingModuleProvider;
import org.matsim.core.controler.AbstractModule;

public class CustomModule extends AbstractModule {


	@Override
	public void install() {

		addRoutingModuleBinding("taxi").toProvider(TaxiRoutingModuleProvider.class);

	}

}
