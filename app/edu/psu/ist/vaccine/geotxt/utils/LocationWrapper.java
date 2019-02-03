package edu.psu.ist.vaccine.geotxt.utils;

import java.util.List;

public interface LocationWrapper {
	public Long getPopulation();
	public String getName();
	public String[] getAlternateNames();
	public Double getLatitude(); 
	public Double getLongitude();
	public Float getScore();
	public Long getGeoNameId();
	public List<LocationWrapper> getHierarchy();
	public String getCountryCode();
        public String getFeatureCode();
        public String getFeatureClass();
}
