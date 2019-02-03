package edu.psu.ist.vaccine.geotxt.utils;

import java.util.ArrayList;
import java.util.List;

import org.geonames.InsufficientStyleException;
import org.geonames.Toponym;

public class LocationWrapperGeonamesToponym implements LocationWrapper {

    protected Toponym toponym;

    public LocationWrapperGeonamesToponym(Toponym toponym) {
        this.toponym = toponym;
    }

    @Override
    public Long getPopulation() {
        Long population = null;
        try {
            population = toponym.getPopulation();
        } catch (InsufficientStyleException e) {
        }
        if (population == null) {
            population = -1L;
        }
        return population;
    }

    @Override
    public String getName() {
        return toponym.getName();
    }

    @Override
    public String[] getAlternateNames() {
        String[] alternateName = null;
        try {
            alternateName = toponym.getAlternateNames().split("\\s*,\\s*");
        } catch (InsufficientStyleException e) {
        }
        if (alternateName == null) {
            alternateName = new String[0];
        }
        return alternateName;
    }

    @Override
    public Double getLatitude() {
        return toponym.getLatitude();
    }

    @Override
    public Double getLongitude() {
        return toponym.getLongitude();
    }

    @Override
    public Long getGeoNameId() {
        return (long) (toponym.getGeoNameId());
    }

    @Override
    public List<LocationWrapper> getHierarchy() {
        return this.getHierarchy();
    }

    public void setHierarchy(List<LocationWrapper> hierarchy) {
        this.setHierarchy(hierarchy);
    }

    @Override
    public Float getScore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCountryCode() {
        return toponym.getCountryCode();
    }

    @Override
    public String getFeatureCode() {
        return toponym.getFeatureCodeName();

    }

    @Override
    public String getFeatureClass() {
        return toponym.getFeatureClassName();
    }
}
