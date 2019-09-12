package net.bhl.matsim.uam.analysis.uamdemand;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

import net.bhl.matsim.uam.infrastructure.UAMStation;

public class UAMDemandItem {
	public Id<Person> personId;
	public Coord origin;
	public Coord originStationCoord;
	public Coord destinationStationCoord;
	public Coord destination;
	public Id<UAMStation> originStationId;
	public Id<UAMStation> destinationStationId;
	public double startTime;
	public double arrivalAtStationTime;
	public double takeOffTime;
	public double landingTime;
	public double departureFromStationTime;
	public double endTime;
	public String vehicleId;
	public String accessMode;
	public String egressMode;
	public boolean uamTrip;
	public UAMDemandItem(Id<Person> personId, Coord origin, Coord originStationCoord, Coord destinationStationCoord,
			Coord destination, Id<UAMStation> originStationId, Id<UAMStation> destinationStationId, double startTime,
			double arrivalAtStationTime, double takeOffTime, double landingTime, double departureFromStationTime,
			double endTime, String vehicleId, String accessMode, String egressMode, boolean uamTrip) {
		super();
		this.personId = personId;
		this.origin = origin;
		this.originStationCoord = originStationCoord;
		this.destinationStationCoord = destinationStationCoord;
		this.destination = destination;
		this.originStationId = originStationId;
		this.destinationStationId = destinationStationId;
		this.startTime = startTime;
		this.arrivalAtStationTime = arrivalAtStationTime;
		this.takeOffTime = takeOffTime;
		this.landingTime = landingTime;
		this.departureFromStationTime = departureFromStationTime;
		this.endTime = endTime;
		this.vehicleId = vehicleId;
		this.accessMode = accessMode;
		this.egressMode = egressMode;
		this.uamTrip = uamTrip;
	}
	
	public void setOriginStationId(Id<UAMStation> originStationId) {
		this.originStationId = originStationId;
	}
	public void setTakeOffTime(double takeOffTime) {
		this.takeOffTime = takeOffTime;
	}

	
}
