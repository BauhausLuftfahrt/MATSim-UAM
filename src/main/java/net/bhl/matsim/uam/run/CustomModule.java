package net.bhl.matsim.uam.run;

import org.matsim.core.controler.AbstractModule;

import net.bhl.matsim.uam.router.TaxiRoutingModuleProvider;

public class CustomModule extends AbstractModule {

	

	@Override
	public void install() {

		addRoutingModuleBinding("taxi").toProvider(TaxiRoutingModuleProvider.class);

	}

}
