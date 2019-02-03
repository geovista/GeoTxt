package edu.psu.ist.vaccine.geotxt.benchmark;

import edu.psu.ist.vaccine.geotxt.entities.Location;



/**
 * Class for storing the results of applying a NER / geocoding approach to 
 * a problem instance.
 * @author jow
 *
 */
public class TestResult {
	
	/**
	 * number of places found
	 */
	protected int placesFound = 0;
	
	/**
	 * number of places in the text that have been identified correctly
	 */
	protected int placesIdentifiedCorrectly = 0;
	
	/**
	 * number of places in the text that have not been recognized
	 */
	protected int placesMissed = 0;
	
	/**
	 * states whether place has been identified correctly for each place in instance
	 */
	protected boolean[] identificationStatus;
	
	/**
	 * corresponding locations identified for each place in the instance
	 */
	protected Location[] locations;
	
	
	/**
	 * computation time required
	 */
	protected double cTime = 0.0;
	
	public TestResult(int size) {
		identificationStatus = new boolean[size];
		locations = new Location[size];
//		distanceDeviations = new double[size];
//		geonamesID = new String[size];
	}
	
	public boolean[] getIdentificationStatus() {
		return identificationStatus;
	}
	public void setIdentificationStatus(boolean[] identificationStatus) {
		this.identificationStatus = identificationStatus;
	}
//	public double[] getDistanceDeviations() {
//		return distanceDeviations;
//	}
//	public void setDistanceDeviations(double[] distanceDeviations) {
//		this.distanceDeviations = distanceDeviations;
//	}
//	public String[] getGeonamesID() {
//		return geonamesID;
//	}
//	public void setGeonamesID(String[] geonamesID) {
//		this.geonamesID = geonamesID;
//	}
	public double getcTime() {
		return cTime;
	}
	public void setcTime(double cTime) {
		this.cTime = cTime;
	}
	public int getLocationsIdentifiedCorrectly() {
		return placesIdentifiedCorrectly;
	}
	public void setLocationsIdentifiedCorrectly(int locationsIdentifiedCorrectly) {
		this.placesIdentifiedCorrectly = locationsIdentifiedCorrectly;
	}
	public int getLocationsMissed() {
		return placesMissed;
	}
	public void setLocationsMissed(int locationsMissed) {
		this.placesMissed = locationsMissed;
	}
	public Location[] getLocations() {
		return locations;
	}
	public void setLocations(Location[] locations) {
		this.locations = locations;
	}
	public int getPlacesFound() {
		return placesFound;
	}

	public void setPlacesFound(int placesFound) {
		this.placesFound = placesFound;
	}

}
