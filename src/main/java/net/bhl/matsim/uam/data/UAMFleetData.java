package net.bhl.matsim.uam.data;

import com.google.common.collect.ImmutableMap;
import net.bhl.matsim.uam.infrastructure.UAMVehicle;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.fleet.FleetImpl;

import java.util.Map;

/**
 * An implementation of {@link FleetImpl} for a UAM vehicles fleet.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class UAMFleetData implements Fleet {
	private final Map<Id<DvrpVehicle>, UAMVehicle> vehicles;

	public UAMFleetData(Map<Id<DvrpVehicle>, UAMVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	@Override
	public Map<Id<DvrpVehicle>, ? extends DvrpVehicle> getVehicles() {
		return ImmutableMap.copyOf(vehicles);
	}

}
