package net.bhl.matsim.uam.run;

import org.matsim.run.gui.Gui;

/**
 * @author raoul.rothfeld
 */
@Deprecated
public class RunUAMScenarioGUI {

	public static void main(String[] args) {
		Gui.show("MATSim GUI for UAM Extension", RunUAMScenario.class);
	}
}
