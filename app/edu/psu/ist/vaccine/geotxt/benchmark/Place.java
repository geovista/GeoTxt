package edu.psu.ist.vaccine.geotxt.benchmark;

/**
 * Class for representing a location in a problem instance including where in the 
 * text message the location is mentioned in the text and, if available, coordinates and geonamesID.
 * @author jow
 *
 */
public class Place {

	/**
	 * name of place at it appears in the text
	 */
	protected String nameInText;
	
	/**
	 * index at which the first character of the place name appears in the text 
	 */
	protected int startIndex;
	
	/**
	 * index at which the last character of the place name appears in the text
	 */
	protected int endIndex;
	
	/**
	 * name under which the place is listed in geonames
	 */
	protected String geonamesName;
		
	/**
	 * geonames ID of the place; "" if ID is unknown
	 */
	protected String geonamesId = "";
	
	/**
	 * longitude of the location of the place
	 */
	protected double lon;
	
	/**
	 * latitude of the location of the place
	 */
	protected double lat;
	
	
	/**
	 * constructor to generate and initialize all instance variables of a new place
	 * 
	 * @param nameInText
	 * @param startIndex
	 * @param endIndex
	 * @param lat
	 * @param lon
	 * @param geonamesName
	 * @param geonamesId
	 */
	public Place(String nameInText, int startIndex, int endIndex,
			     double lat, double lon, String geonamesName, 
			     String geonamesId) {
		
		this.nameInText = nameInText;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.lon = lon;
		this.lat = lat;
		this.geonamesName = geonamesName;
		this.geonamesId = geonamesId;
	}
	
	public String getNameInText() {
		return nameInText;
	}

	public void setNameInText(String nameInText) {
		this.nameInText = nameInText;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public String getGeonamesName() {
		return geonamesName;
	}

	public void setGeonamesName(String geonamesName) {
		this.geonamesName = geonamesName;
	}

	public String getGeonamesId() {
		return geonamesId;
	}

	public void setGeonamesId(String geonamesId) {
		this.geonamesId = geonamesId;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public String toString() {
		return "Place " + this.getNameInText() + "[" + this.lat + "," + this.lon + ";" + this.getGeonamesName() + "," + this.getGeonamesId() + "]";
	}
}
