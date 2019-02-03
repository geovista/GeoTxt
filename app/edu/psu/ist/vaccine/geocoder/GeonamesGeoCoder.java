/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geocoder;

import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.math.BigInteger;
import org.apache.log4j.Logger;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

/**
 *
 * @author Morteza KZ
 * This class supports geocoding using new geonames API (api.geonames.org). It is used in basic analyzers, and uses the modified
 * ranking  scheme based on population and string closeness. 
 * 
 */
public class GeonamesGeoCoder {

    final static int maxRows = 5;
    public static String username = "geopa";
    public static Logger log = Logger.getLogger(GeonamesGeoCoder.class);

    //GeoCodes location names using the new GeoNames WS API. Fetches topmost maxRows results, and assigns the one with the most population. 
    public static PointGeometry geoCode(String locationName) throws Exception {

        WebService.setUserName(username);
        Toponym toponym = null;
        //  Long[] pop = new Long[maxRows];
        Long maxPop = Long.MIN_VALUE;
        int maxPopIndex = -1;
        Long maxPopGoodFit = Long.MIN_VALUE;
        int maxPopGoodFitIndex = -1;

        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setMaxRows(maxRows);
        searchCriteria.setQ(locationName);
        searchCriteria.setStyle(Style.FULL);
        ToponymSearchResult searchResult = WebService.search(searchCriteria);

        if (searchResult.getToponyms().size() <= 1) {
            System.out.println("WARNING: geonames did not return any results!");
        }

        for (int c = 0; c < searchResult.getToponyms().size(); c++) {
            toponym = searchResult.getToponyms().get(c);

            Long pop = toponym.getPopulation() != null ? toponym.getPopulation() : 0; // may cause problems with entries that dont have a population specified

            boolean found = false;
            if (toponym.getName().equalsIgnoreCase(locationName)) {
                found = true;
            }
            if (!found) {
                String[] names = toponym.getAlternateNames().split(",");
                for (String n : names) {
                    if (n.equalsIgnoreCase(locationName)) {
                        found = true;
                        break;
                    }
                }

            }
            if (found) {
                if (pop > maxPopGoodFit) {
                    maxPopGoodFit = pop;
                    maxPopGoodFitIndex = c;
                }
            } else {
                if (pop > maxPop) {
                    maxPop = pop;
                    maxPopIndex = c;
                }
            }

            //log.info("toponym for " + locationName + ":" + toponym + " pop=" + toponym.getPopulation());
        }

        if (maxPopGoodFitIndex >= 0) {
            toponym = searchResult.getToponyms().get(maxPopGoodFitIndex);
        } else if (maxPopIndex >= 0) {
            toponym = searchResult.getToponyms().get(maxPopIndex);
        } else {
            return null;
        }

        PointGeometry point = new PointGeometry(toponym.getName(), toponym.getLongitude(), toponym.getLatitude(), BigInteger.valueOf(toponym.getGeoNameId()));

        return point;
    }
    
    
     public static String geoCodetoGeoJson(String locationName) throws Exception  {
        PointGeometry geometry = geoCode(locationName);
        return GeoJsonWriter.pointGeometryToGeoJson(geometry, locationName);
    }
}
