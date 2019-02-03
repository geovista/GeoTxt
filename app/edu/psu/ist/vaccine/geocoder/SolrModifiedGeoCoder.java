/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geocoder;

import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.utils.BBox;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.geonames.Toponym;

/**
 *
 * @author Morteza KZ This class supports geocoding using new geonames API
 * (api.geonames.org). It is used in basic analyzers, and uses the modified
 * ranking scheme based on population and string closeness.
 *
 */
public class SolrModifiedGeoCoder {

    /**
     * maximal number of results we want for geonames queries (made into Solr
     * here)
     */
    final static int maxRows = 10;
    public static Logger log = Logger.getLogger(SolrModifiedGeoCoder.class);

    //GeoCodes location names using the Solr index on GeoNames DB. Uses SolrQuerying class 
    public static LocationWrapper geoCode(String locationName) throws Exception {

        Toponym toponym = null;
        //  Long[] pop = new Long[maxRows];
        Long maxPop = Long.MIN_VALUE;
        int maxPopIndex = -1;
        Long maxPopGoodFit = Long.MIN_VALUE;
        int maxPopGoodFitIndex = -1;



        //ArrayList<List<LocationWrapper>> bestMatches = new ArrayList<List<LocationWrapper>>();

        SolrQuerying sq = new SolrQuerying();

        // go over all identified place names
        List<LocationWrapper> searchResult = null;

        // get best matches from solr / web service

        searchResult = sq.getToponymsFromSolr(locationName, maxRows, "General");

        //TODO: Is this check necessary? Why <=1
        if (searchResult.size() <= 1) {
            log.warn("WARNING: Solr geonames did not return any results!");
        }


        // TODO: this is just debug output
/*        for (LocationWrapper lw : searchResult) {
            log.info(lw.getName() + " " + lw.getGeoNameId());
        }
*/
        //List<LocationWrapper> best = GeocodingUtils.getBestCandidates(l.getName(),searchResult,NUM_BEST_MATCHES);
        List<LocationWrapper> best = searchResult;
        //bestMatches.add(best);

        // set initial geometry based on best match
        LocationWrapper t = null;
        if (best.size() > 0) {
            t = best.get(0);
        }
        
        return t;

        /*PointGeometry point = null;
        if (t != null) {
            try {
                point = new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId()));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SolrModifiedGeoCoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return point;*/
    }
    
  //GeoCodes location names using the Solr index on GeoNames DB. Uses SolrQuerying class 
    public static LocationWrapper geoCode(String locationName, BBox bbox, String[] hLevels) throws Exception {

        Toponym toponym = null;
        //  Long[] pop = new Long[maxRows];
        Long maxPop = Long.MIN_VALUE;
        int maxPopIndex = -1;
        Long maxPopGoodFit = Long.MIN_VALUE;
        int maxPopGoodFitIndex = -1;



        //ArrayList<List<LocationWrapper>> bestMatches = new ArrayList<List<LocationWrapper>>();

        SolrQuerying sq = new SolrQuerying();

        // go over all identified place names
        List<LocationWrapper> searchResult = null;

        // get best matches from solr / web service

        if (bbox != null) {
        	searchResult = sq.getToponymsFromSolr(locationName, maxRows, bbox.getMinx(), bbox.getMiny(), bbox.getMaxx(), bbox.getMaxy());
        } else {
        	searchResult = sq.getToponymsFromSolr(locationName, maxRows, "General");
        }

        //TODO: Is this check necessary? Why <=1
        if (searchResult.size() <= 1) {
            log.warn("WARNING: Solr geonames did not return any results!");
        }


        // TODO: this is just debug output
/*        for (LocationWrapper lw : searchResult) {
            log.info(lw.getName() + " " + lw.getGeoNameId());
        }
*/
        //List<LocationWrapper> best = GeocodingUtils.getBestCandidates(l.getName(),searchResult,NUM_BEST_MATCHES);
        List<LocationWrapper> best = searchResult;
        //bestMatches.add(best);

        // set initial geometry based on best match
        LocationWrapper t = null;
        if (best.size() > 0) {
            t = best.get(0);
        }
        
        return t;

        /*PointGeometry point = null;
        if (t != null) {
            try {
                point = new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId()));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SolrModifiedGeoCoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return point;*/
    }

    public static String geoCodetoGeoJson(String locationName) throws Exception {
        LocationWrapper t = geoCode(locationName);
        PointGeometry point = null;
        if (t != null) {
            try {
                point = new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId()));
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(SolrModifiedGeoCoder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return GeoJsonWriter.pointGeometryToGeoJson(point, locationName);
    }
    
    public static void main(String args[]) throws IllegalArgumentException, URISyntaxException, IOException, Exception {
        SolrModifiedGeoCoder geoCoder = new SolrModifiedGeoCoder();

        //System.out.println(results);
        System.out.println(geoCoder.geoCodetoGeoJson("New York"));


    }
}
