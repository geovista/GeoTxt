/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.entities;

import edu.psu.ist.vaccine.geotxt.utils.BBox;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author mortezakz
 */
public class Location {

	protected String name;
	protected String locType;
	protected String countryCode = null;
	protected String featureClass = null;
	protected String featureCode = null;
	protected String[] alternateNames = null;

	protected ArrayList<Integer> positions = null;
	protected PointGeometry geometry = null;

	protected List<LocationWrapper> candidates = new ArrayList<LocationWrapper>();
	protected List<LocationWrapper> hierarchy = new ArrayList<LocationWrapper>();

	private BBox bbox = null;
	private String[] hLevels = null;

	public List<LocationWrapper> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<LocationWrapper> candidates) {
		this.candidates = candidates;
	}

	public List<LocationWrapper> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(List<LocationWrapper> hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String[] getAlternateNames() {
		return alternateNames;
	}

	public void setAlternateNames(String[] alternateNames) {
		this.alternateNames = alternateNames;
	}

	public Location(String name) {
		// this.name = StripStrings.strip(name);
		this.name = name;
		this.positions = new ArrayList<Integer>();
	}

	public Location(String name, String locType) {
		// this.name = StripStrings.strip(name);
		this.name = name;
		this.locType = locType.toLowerCase();
		this.positions = new ArrayList<Integer>();
	}

	public Location(String name, int position) {
		// this.name = StripStrings.strip(name);
		this.name = name;
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public Location(String name, String locType, int position) {
		// this.name = StripStrings.strip(name);
		this.name = name;
		this.locType = locType.toLowerCase();
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public Location(String name, String locType, int position, PointGeometry geometry) {
		// this.name = StripStrings.strip(name).toLowerCase();
		this.name = name;
		this.locType = locType.toLowerCase();
		this.geometry = geometry;
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (this.positions != null && !this.positions.contains(position)) {
			this.positions.add(position);
		}
	}

	public void addPosition(int position) {
		if (this.positions == null) {
			this.positions = new ArrayList<Integer>();
		}
		if (!this.positions.contains(position)) {
			this.positions.add(position);
		}
		Collections.sort(positions);
	}

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		this.positions = positions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocType() {
		return locType;
	}

	public void setLocType(String locType) {
		this.locType = locType.toLowerCase();
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getFeatureClass() {
		return featureClass;
	}

	public void setFeatureClass(String featureClass) {
		this.featureClass = featureClass;
	}

	public String getFeatureCode() {
		return featureCode;
	}

	public void setFeatureCode(String featureCode) {
		this.featureCode = featureCode;
	}

	public PointGeometry getGeometry() {
		return geometry;
	}

	public void setGeometry(PointGeometry geometry) {
		this.geometry = geometry;
	}

	public BBox getBBox() {
		return bbox;
	}

	public void setBBox(BBox bbox) {
		this.bbox = bbox;
	}

	public String getSelfHLevel() {
		String selfHLevel = "";
		// TODO - We will have different data sources so how do we associate a similar hierarchy level for each.
		selfHLevel = featureClass;
		return selfHLevel;
	}

	public String[] getHLevels() {
		return hLevels;
	}

	public void setHLevels(String[] hLevels) {
		this.hLevels = hLevels;
	}

	@Override
	public boolean equals(Object loc1) {
		boolean state = false;
		Location loct1 = (Location) loc1;
		if (loct1.getName().toLowerCase().equals(this.getName().toLowerCase())) {
			state = true;
		}
		return state;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		if (geometry != null) {
			return "Name: " + this.name + " Position:" + this.positions + " Geometry: " + this.geometry.toString();
		} else {
			return "Name: " + this.name + " Position:" + this.positions;
		}
	}
}
