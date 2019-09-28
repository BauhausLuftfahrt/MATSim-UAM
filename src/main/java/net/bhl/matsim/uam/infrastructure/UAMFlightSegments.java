package net.bhl.matsim.uam.infrastructure;

/**
 * Provides flight segment identifiers for MATSim network links. Currently uses two flight segments: horizontal (i.e.
 * cruise flight), which is the default flight segment, and vertical (i.e. take-off and landings). Can be extended to
 * various flight segments in the future.
 *
 * @author RRothfeld (Raoul Rothfeld)
 */
public class UAMFlightSegments {
    public static final String ATTRIBUTE = "flight";

    public static final String HORIZONTAL = "horizontal";
    public static final String VERTICAL = "vertical";
}
