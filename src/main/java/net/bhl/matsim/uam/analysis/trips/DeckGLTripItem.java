package net.bhl.matsim.uam.analysis.trips;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * This class stores information about a trip for deck.gl format.
 *
 * @author balacmi (Milos Balac), RRothfeld (Raoul Rothfeld)
 */
public class DeckGLTripItem {
	/*
	 * public Coord location; public long time; public long timeShift;
	 * 
	 * public DeckGLTripItem(Coord location, long time) { this(location, time, 0); }
	 * 
	 * public DeckGLTripItem(Coord location, long time, long timeShift) {
	 * this.location = location; this.time = time; this.timeShift = timeShift; }
	 * 
	 * @Override public String toString() { return "[" + location.getX() + "," +
	 * location.getY() + "," + time + "]"; }
	 * 
	 * public String convert(String inputEPSGCode, String outputEPSGCode, long
	 * timeMultiplier) { Coordinate convertedSource = null;
	 * 
	 * CoordinateReferenceSystem crsIn = null; CoordinateReferenceSystem crsOut =
	 * null; try { crsIn = MGC.getCRS(inputEPSGCode); // EPSG:code crsOut =
	 * MGC.getCRS(outputEPSGCode); // EPSG:code MathTransform transform =
	 * CRS.findMathTransform(crsIn, crsOut); Coordinate source = new
	 * Coordinate(location.getX(), location.getY()); convertedSource =
	 * JTS.transform(source, null, transform);
	 * 
	 * } catch (IllegalArgumentException e) {
	 * System.err.println("Old geotools version is not compatible with Java 9");
	 * e.printStackTrace(); System.exit(1); } catch (FactoryException e) {
	 * e.printStackTrace(); } catch (TransformException e) { e.printStackTrace(); }
	 * 
	 * return "[" + convertedSource.x + "," + convertedSource.y + "," + ((time -
	 * timeShift) * timeMultiplier) + "]"; }
	 */
}
