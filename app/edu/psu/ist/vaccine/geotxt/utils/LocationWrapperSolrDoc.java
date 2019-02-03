package edu.psu.ist.vaccine.geotxt.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;

public class LocationWrapperSolrDoc implements LocationWrapper {

    protected SolrDocument doc;
    protected List<LocationWrapper> hierarchy = new ArrayList<LocationWrapper>();

    public LocationWrapperSolrDoc(SolrDocument doc) {
        this.doc = doc;
        setHierarchy(this.getGeoNameId());
    }

    public LocationWrapperSolrDoc(String name, Long population, Long geoNameId) {
        this.doc = new SolrDocument();
        doc.addField("name", name);
        doc.addField("population", population);
        doc.addField("geonameid", geoNameId);
        setHierarchy(this.getGeoNameId());
    }

    @Override
    public Long getPopulation() {
        return (Long) doc.getFieldValue("population");
    }

    @Override
    public String getName() {
        return (String) doc.getFieldValue("name");
    }

    @Override
    public String[] getAlternateNames() {
    	
    	List<Object> objects=  (List<Object>) doc.getFieldValues("alternatenames");
    	
    	String[] alternateNames = new String[objects.size()];
    	
    	int index = 0;
    	for (Object value : objects) {
    		alternateNames[index] = (String) value;
    	  index++;
    	}
    	
        return  alternateNames;
        		//Arrays.toString((String[])doc.getFieldValue("alternatenames"));
    }

    @Override
    public Double getLatitude() {
        return (Double) doc.getFieldValue("latitude");
    }

    @Override
    public Double getLongitude() {
        return (Double) doc.getFieldValue("longitude");
    }

    @Override
    public Long getGeoNameId() {
        return (Long) doc.getFieldValue("geonameid");
    }

    @Override
    public List<LocationWrapper> getHierarchy() {
        return hierarchy;
    }

    public String getAdmin1Name() {
        return (String) doc.getFieldValue("admin1name");
    }

    public String getAdmin2Name() {
        return (String) doc.getFieldValue("admin2name");
    }

    public String getAdmin3Name() {
        return (String) doc.getFieldValue("admin3name");
    }

    public String getAdmin4Name() {
        return (String) doc.getFieldValue("admin4name");
    }

    public String getCountryName() {
        return (String) doc.getFieldValue("countryname");
    }

    public String getContinentName() {
        return (String) doc.getFieldValue("continentname");
    }

    public Long getAdmin1GeoNameId() {
        return (Long) doc.getFieldValue("admin1geonameid");
    }

    public Long getAdmin2GeoNameId() {
        return (Long) doc.getFieldValue("admin2geonameid");
    }

    public Long getAdmin3GeoNameId() {
        return (Long) doc.getFieldValue("admin3geonameid");
    }

    public Long getAdmin4GeoNameId() {
        return (Long) doc.getFieldValue("admin4geonameid");
    }

    public Long getCountryGeoNameId() {
        return (Long) doc.getFieldValue("countrygeonameid");
    }

    public Long getContinentGeoNameId() {
        return (Long) doc.getFieldValue("continentgeonameid");
    }

    @Override
    public Float getScore() {
        return (Float) doc.getFieldValue("score");
    }

    public void setHierarchy(Long geonamesID) {
        hierarchy = new ArrayList<LocationWrapper>();

        if (this.getAdmin4Name() != null && this.getAdmin4GeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getAdmin4Name(), -1L, this.getAdmin4GeoNameId()));
            if (this.getAdmin4GeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
        if (this.getAdmin3Name() != null && this.getAdmin3GeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getAdmin3Name(), -1L, this.getAdmin3GeoNameId()));
            if (this.getAdmin3GeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
        if (this.getAdmin2Name() != null && this.getAdmin2GeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getAdmin2Name(), -1L, this.getAdmin2GeoNameId()));
            if (this.getAdmin2GeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
        if (this.getAdmin1Name() != null && this.getAdmin1GeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getAdmin1Name(), -1L, this.getAdmin1GeoNameId()));
            if (this.getAdmin1GeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
        if (this.getCountryName() != null && this.getCountryGeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getCountryName(), -1L, this.getCountryGeoNameId()));
            if (this.getCountryGeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
        if (this.getContinentName() != null && this.getContinentGeoNameId() != null) {
            hierarchy.add(new LocationWrapperSolrDoc(this.getContinentName(), -1L, this.getContinentGeoNameId()));
            if (this.getContinentGeoNameId().equals(geonamesID)) {
                hierarchy.clear();  // workaround for some weird info in the solr database
            }
        }
    }

    @Override
    public String getCountryCode() {
        return (String) doc.getFieldValue("countrycode");
    }

    @Override
    public String getFeatureCode() {
        return (String) doc.getFieldValue("featurecode");
    }

    @Override
    public String getFeatureClass() {
        return (String) doc.getFieldValue("featureclass");
    }
}
