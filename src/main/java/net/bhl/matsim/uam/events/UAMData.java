package net.bhl.matsim.uam.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import net.bhl.matsim.uam.infrastructure.UAMStation;

/**
 * Object of this class contains all the information describing one uam trip.
 * 
 * @author balacm
 *
 */
public class UAMData {
	
	Link originLink;
	Link originStationLink;
	Link destinationStationLink;
	Link destinationLink;
	Id<UAMStation> originStationId;
	Id<UAMStation> destinationStationId;
	double startTime;
	double arrivalAtStationTime;
	double takeOffTime;
	double landingTime;
	double departureFromStationTime;
	double endTime;
	String vehicleId = null;
	String accessMode;
	String egressMode;
	boolean uamTrip = false;
		
	public String toString() {
		if (uamTrip && originLink != null && originStationLink != null && destinationLink != null  && destinationStationLink != null ) {
			return originLink.getCoord().getX() + "," + originLink.getCoord().getY() + "," + originStationLink.getCoord().getX()
					+ "," + originStationLink.getCoord().getY() + "," + destinationStationLink.getCoord().getX() + ","
					+ destinationStationLink.getCoord().getY() + "," + destinationLink.getCoord().getX() + "," + destinationLink.getCoord().getY() + "," 
					+ startTime + "," + arrivalAtStationTime + "," + takeOffTime + "," + landingTime + "," + departureFromStationTime + "," + endTime + ","
					+ vehicleId + "," + originStationId + "," + destinationStationId + "," + accessMode + "," + egressMode + "," + uamTrip;
		}
		else if (uamTrip && originLink != null && originStationLink != null && destinationStationLink != null) {
			
			return originLink.getCoord().getX() + "," + originLink.getCoord().getY() + "," + originStationLink.getCoord().getX()
					+ "," + originStationLink.getCoord().getY() + "," + destinationStationLink.getCoord().getX() + "," +
					destinationStationLink.getCoord().getY() + "," + "," + ","
					+ startTime + "," + arrivalAtStationTime + "," + takeOffTime + "," + landingTime + "," + departureFromStationTime + "," + endTime + ","
					+ vehicleId + "," + originStationId + "," + destinationStationId + "," + accessMode + "," + egressMode + "," + uamTrip;
		}
		else
			return originLink.getCoord().getX() + "," + originLink.getCoord().getY() + ",,,,,,,"+ startTime + ",,,,,,," + originStationId + ",," + accessMode + ",," + uamTrip;
	}
}