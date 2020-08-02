package net.bhl.matsim.uam.run;

/**
 * Provides UAM trip segment identifiers. The following trips are currently being modelled: UAM access trip (via
 * traditional mode (e.g. walk, bike, car), UAM station interaction, flight segments, UAM station interaction,
 * UAM egress trip.
 *
 * @author RRothfeld (Raoul Rothfeld)
 */
public class UAMConstants {
	public static final String uam = "uam";

	public static final String access = "access_" + uam + "_";
	public static final String egress = "egress_" + uam + "_";

	public static final String interaction = uam + "_interaction";

	public static final String vehicle = uam + "_vh_";
}