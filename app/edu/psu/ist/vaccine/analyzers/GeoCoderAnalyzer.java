/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.analyzers;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.utils.Analyzer;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * 
 * @author MortezaKarimzadeh
 * 
 *         This class GeoCodes the input text without passing it through any NER
 */

public class GeoCoderAnalyzer implements Analyzer {

	protected int maxRows = 100;

	//
	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;

		Location loc = new Location(QueryText);
		NamedEntities doc = new NamedEntities();
		doc.locs.add(loc);

		SolrQuerying sq = new SolrQuerying();

		for (Location l : doc.locs) {

			// go over all identified place names
			List<LocationWrapper> searchResult = null;

			// get best matches from solr / web service
			searchResult = sq.getToponymsFromSolr(l.getName(), maxRows, "General");

			// filter based on relative score
			if (searchResult.size() > 0) {
				double bestScorePercentage = searchResult.get(0).getScore() * 0.1; // could
																					// make
																					// this
																					// a
																					// parameter
																					// /
																					// constant
				double bestScorePercentage2 = searchResult.get(0).getScore() * 0.2; // could
																					// make
																					// this
																					// a
																					// parameter
																					// /
																					// constant

				// for (int i = 1; i < searchResult.size(); i++) {
				// double score = searchResult.get(i).getScore();
				// if (score < bestScorePercentage
				// || (score < bestScorePercentage2 && i > 5)) {
				// for (int j = searchResult.size() - 1; j >= i; j--) {
				// searchResult.remove(j);
				// }
				// break;
				// }
				// }
			}

			l.setCandidates(searchResult);

			// set initial geometry based on best match
			LocationWrapper t = null;
			if (searchResult.size() > 0)
				t = searchResult.get(0);

			if (t != null) {
				try {
					l.setGeometry(new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId())));
					l.setHierarchy(t.getHierarchy());
					l.setCountryCode(t.getCountryCode());
					l.setFeatureClass(t.getFeatureClass());
					l.setFeatureCode(t.getFeatureCode());
					l.setAlternateNames(t.getAlternateNames());
				} catch (Exception ex) {
					java.util.logging.Logger.getLogger(HierarchyAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			/*
			 * try { LocationWrapper t = SolrModifiedGeoCoder.geoCode(l.getName()); if (t != null) { l.setGeometry(new PointGeometry(t.getName(), t .getLongitude(), t.getLatitude(), BigInteger .valueOf(t.getGeoNameId()))); l.setHierarchy(t.getHierarchy()); l.setCountryCode(t.getCountryCode()); l.setFeatureClass(t.getFeatureClass());
			 * l.setFeatureCode(t.getFeatureCode());
			 * 
			 * } } catch (Exception ex) { java.util.logging.Logger.getLogger( BasicSolrStanfordAnalyzer.class.getName()).log( Level.SEVERE, null, ex); }
			 */

		}

		sq.closeConnection();

		// GeoCogind Organizations is phased out for now.
		/*
		 * for (Organization o : doc.orgs) { o.setGeometry(GeoCoder.geoCodeOrg(o.getName())); }
		 */

		return doc;
	}

	//

	public GeoCoderAnalyzer(int maxRows) {
		this.maxRows = maxRows;
	}

	public GeoCoderAnalyzer() {

		this.maxRows = 10;
	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalArgumentException, URISyntaxException, IOException {
		GeoCoderAnalyzer basicStanfordAnalyizer = new GeoCoderAnalyzer(30);

		// temporarily pass an empty Map
		// object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// --------------------------------------------------------------------------------------

		NamedEntities results = basicStanfordAnalyizer.analyze("Iran",NerEngines.NONE, context);

		System.out.println(results);

	}
}
