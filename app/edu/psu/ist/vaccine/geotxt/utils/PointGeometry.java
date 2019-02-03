/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.utils;

import java.math.BigInteger;

/**
 * this is not used as the output of Geocoder. Also, the Location class and
 * Organization class have a member of this class, which is populated by GeoTxt.
 *
 * @author MortezaKarimzadeh
 */
public class PointGeometry {

    //  GeoJSON should be similar to { "type": "Point", "coordinates": [100.0, 0.0] }
    protected String type = "Point";
    protected String toponym = null;
    protected BigInteger geoNameId = null;
    protected double[] coordinates = new double[2];

    public String getType() {
        return type;
    }

//    public void setType(String type) {
//        this.type = type;
//    }
    public String getToponym() {
        return toponym;
    }

    public void setToponym(String type) {
        this.toponym = type;
    }

    public BigInteger getGeoNameId() {
        return geoNameId;
    }

    public void setGeoNameId(BigInteger geoNameId) {
        this.geoNameId = geoNameId;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public PointGeometry(double lon, double lat, BigInteger geoNameId) {
        this.coordinates[0] = lon;
        this.coordinates[1] = lat;
        this.geoNameId = geoNameId;

    }

    public PointGeometry(String toponym, double lon, double lat, BigInteger geoNameId) {
        this(lon, lat, geoNameId);
        this.toponym = toponym;
    }

    @Override
    public String toString() {
        return "Point with toponym " + toponym + ", and GeoNameId of " + geoNameId + " at Lat, Long (" + coordinates[1] + " " + coordinates[0] + ")";

        //"GeoLocated" + type + " -->" + " Longitude: " + coordinates[0] + ", Latitude: " + coordinates[1] + ", GeoNameId: " + geoNameId.toString();
    }
}
