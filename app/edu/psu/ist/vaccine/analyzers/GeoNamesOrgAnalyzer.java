/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.analyzers;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.AbstractNer;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.ner.StanfordNer;
import edu.psu.ist.vaccine.geocoder.GeonamesGeoCoder;
import edu.psu.ist.vaccine.geotxt.utils.Analyzer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author MortezaKarimzadeh
 */
// This class uses GeoNames.org web service API to query places.
public class GeoNamesOrgAnalyzer implements Analyzer {

	public static Map<NerEngines, Object> nerMap = null;

	//
	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;

		NamedEntities doc = null;

		doc = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);

		if (doc == null) {
			return null;
		}

		for (Location l : doc.locs) {
			try {
				l.setGeometry(GeonamesGeoCoder.geoCode(l.getName()));
			} catch (Exception ex) {
				java.util.logging.Logger.getLogger(GeoNamesOrgAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		// GeoCogind Organizations is phased out for now.
		/*
		 * for (Organization o : doc.orgs) { o.setGeometry(GeoCoder.geoCodeOrg(o.getName())); }
		 */

		return doc;
	}
	
	

	public GeoNamesOrgAnalyzer(String stanfordAddress) {

		StanfordNer st = new StanfordNer(stanfordAddress);
		GeoNamesOrgAnalyzer.nerMap = new HashMap<NerEngines, Object>();
		GeoNamesOrgAnalyzer.nerMap.put(NerEngines.STANFORD, st);

	}

	public GeoNamesOrgAnalyzer(Map<NerEngines, Object> nerMap) {

		GeoNamesOrgAnalyzer.nerMap = nerMap;

	}

	public static void main(String args[]) throws IllegalArgumentException, URISyntaxException, IOException {
		GeoNamesOrgAnalyzer geoNamesOrg = new GeoNamesOrgAnalyzer("C:/Programs/Stanford/english.all.3class.distsim.crf.ser.gz");
		
		 // temporarily pass an empty Map object------------------------------------------------------
		 Map<String, Object> context = new HashMap<String, Object>();
		 // --------------------------------------------------------------------------------------
		
		 NamedEntities results = geoNamesOrg.analyze("I live in London, Ontario", NerEngines.STANFORD, context);
		
		 System.out.println(results);

	}
}
