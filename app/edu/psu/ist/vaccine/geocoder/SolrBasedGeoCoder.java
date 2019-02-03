/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geocoder;

import edu.psu.ist.vaccine.analyzers.HierarchyAnalyzer;
import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Morteza KZ This class is built to support profile location extraction
 * in SP2. It has a GeoCode Method which accepts text and returns a (single)
 * best result.
 */
public class SolrBasedGeoCoder {

    public static SolrQuerying sq = new SolrQuerying();

    public static PointGeometry geoCode(String locationName) {

        PointGeometry geoCodedPoint = new PointGeometry(0D, 0D, BigInteger.valueOf(0));
        double[] coord = {0, 0};

        System.out.println("Searching for " + locationName);
        List<LocationWrapper> searchResult = null;

        // get best matches from solr / web service

        searchResult = sq.getToponymsFromSolr(locationName, 1, "General");
        

        // set initial geometry based on best match
        LocationWrapper t = null;
        if (searchResult.size() > 0) {
            t = searchResult.get(0);
        }

        if (t != null) {
            try {
                coord[0] = t.getLongitude();
                coord[1] = t.getLatitude();
                geoCodedPoint.setCoordinates(coord);
                geoCodedPoint.setGeoNameId(BigInteger.valueOf(t.getGeoNameId()));
                geoCodedPoint.setToponym(t.getName());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(HierarchyAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return geoCodedPoint;
    }

    public static void main(String[] args) {

        PointGeometry p = SolrBasedGeoCoder.geoCode("New Jersey");
        System.out.print(p);
    }
}
