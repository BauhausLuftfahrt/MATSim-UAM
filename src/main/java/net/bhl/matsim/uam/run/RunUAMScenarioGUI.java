package net.bhl.matsim.uam.run;

import org.matsim.run.gui.Gui;

/**
 * start a MATSim run including Urban Air Mobility capabilities using a
 * Graphical user interface.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
@Deprecated
public class RunUAMScenarioGUI {

	public static void main(String[] args) {
		Gui.show("MATSim GUI for UAM Extension", RunUAMScenario.class);
	}
}
