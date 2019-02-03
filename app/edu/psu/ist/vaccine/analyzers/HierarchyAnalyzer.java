/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.analyzers;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.hierarchy.MapHierarchyPlaces;
import edu.psu.ist.vaccine.geotxt.ner.AbstractNer;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.ner.StanfordNer;
import edu.psu.ist.vaccine.geotxt.utils.Analyzer;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.TextPreprocessing;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import edu.psu.ist.vaccine.geotxt.utils.GeocodingUtils;
import edu.psu.ist.vaccine.geotxt.utils.HashtagProcessor;

import java.util.logging.Level;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * @author MortezaKarimzadeh
 */
// This is the simplest basic GeoTxt approach reimplemented with Stanford as the
// Entity Extractor Engine
public class HierarchyAnalyzer implements Analyzer {

	/** maximal number of results we want for geonames queries */
	protected int maxRows = 50;

	/**
	 * maximal number of best matches per place name that will be handed over to the MapHierarchyPlaces.geoCode
	 */
	protected int nBestMatches = 5;

	/**
	 * determines whether solr index should be used instead of geonames web service
	 */
	public boolean useSolr = true; // TODO: this is just a quick first solution

	public static Map<NerEngines, Object> nerMap = null;

	public HierarchyAnalyzer(String stanfordAddress, int maxRows, int nBestMatches) {

		StanfordNer st = new StanfordNer(stanfordAddress);
		HierarchyAnalyzer.nerMap = new HashMap<NerEngines, Object>();
		HierarchyAnalyzer.nerMap.put(NerEngines.STANFORD, st);
		this.maxRows = maxRows;
		this.nBestMatches = nBestMatches;
	}

	public HierarchyAnalyzer(Map<NerEngines, Object> nerMap, int maxRows, int nBestMatches) {

		HierarchyAnalyzer.nerMap = nerMap;
		this.maxRows = maxRows;
		this.nBestMatches = nBestMatches;
	}

	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;

		HashtagProcessor processed = new HashtagProcessor();
		processed = HashtagProcessor.processHashTags(StringEscapeUtils.unescapeHtml4(strippedQuery));
		strippedQuery = processed.getHashtagRemoved();

		NamedEntities doc = null;

		doc = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);

		if (doc == null) {
			return null;
		}

		// determine best candidates for each identified place name and set
		// initial geometry based on best match
		SolrQuerying sq = null;
		if (useSolr)
			sq = new SolrQuerying();

		for (Location l : doc.locs) { // go over all identified place names
			// System.out.println("Searching for "+ l.getName());
			List<LocationWrapper> searchResult = null;

			// get best matches from solr / web service
			if (useSolr) {
				searchResult = sq.getToponymsFromSolr(l.getName(), maxRows, "General");
			} else {
				searchResult = GeocodingUtils.getToponymsFromGeonames(l.getName(), maxRows);
			}

			/*
			 * // TODO: this is just debug output for (LocationWrapper lw : searchResult) { System.out.println("*Search Results[i]: "+lw.getName()+ " " +lw.getGeoNameId()); }
			 */

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

				} catch (Exception ex) {
					java.util.logging.Logger.getLogger(HierarchyAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		if (useSolr)
			sq.closeConnection();

		// GeoCogind Organizations is phased out for now.
		/*
		 * for (Organization o : doc.orgs) { o.setGeometry(GeoCoder.geoCodeOrg(o.getName())); }
		 */

		// if we have multiple place names, we use the hierarchical approach

		if (doc.locs.size() > 1) {
			try {
				doc = MapHierarchyPlaces.resolveMultiLocationNames(doc, useSolr);
			} catch (Exception ex) {
				java.util.logging.Logger.getLogger(HierarchyAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		doc.adjustCharIndexesForHashtags(processed);

		return doc;
	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalArgumentException, URISyntaxException, IOException {
		HierarchyAnalyzer basicStanfordAnalyizer = new HierarchyAnalyzer("C:/Programs/Stanford/english.all.3class.distsim.crf.ser.gz", 50, 5);

		// temporarily pass an empty Map
		// object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// --------------------------------------------------------------------------------------

		NamedEntities results = basicStanfordAnalyizer.analyze(
				"Westfield, NJ- commuters gather on the platform to catch an early train to New York during another storm that could bring 10\" of snow.\n" + "By Henry Austin, NBC News contributor\n" + "It looks like the rodent was right.\n"
						+ "Punxsutawney Phil predicted six more weeks of winter, and just a day after the Super Bowl when temperatures in the Northeast soared into the 50s, forecasters warned that a series of storms would pound much of the country over the next week.\n"
						+ "“Across the U.S. we’re going to have a very snowy situation with at least three winter storms over the next week, and these are high-impact storms,” said Guy Walton, a forecaster with The Weather Channel. “There is a very active storm pattern across the country.”\n"
						+ "Yet another winter storm will bring snowfall and ice to parts of the Midwest and South. \n"
						+ "Up to 8 inches of wet and heavy snow is expected to hit the greater New York City area only one day after the region enjoyed balmy weather for Super Sunday and the game in East Rutherford, N.J. The National Weather Service has issued a winter storm warning for the city, Long Island, and most of New Jersey from 5 a.m. through 7 p.m. Monday.\n"
						+ "Advertise | AdChoices\n" + "“They’ll have a break Tuesday and then the next storm will bear down on them Wednesday morning,” Walton said. He said that the second storm is set to streak across the country, dumping heavy snow on Chicago, Detroit, Kansas City and St Louis en route.\n"
						+ "Nebraska, Kansas, Illinois, Indiana and Michigan could also all get 6 or more inches of snow between Tuesday and Wednesday, The Weather Channel predicted, and Walton said the Mississippi Valley could also see some of the white stuff.\n" + " \n" + "Nam Y. Huh / AP\n"
						+ "A man jogs during snowy day at North Avenue Beach in Chicago, on Saturday. January was the third snowiest January on record since 1884 and the 10th coldest January on record since 1872.\n"
						+ "Starting Tuesday, ice could also accumulate and threaten power outages in western Pennsylvania, western Maryland, northwest Virginia, and eastern West Virginia.\n" + "A third winter storm could also have a high impact on the Rockies on Friday, with a wintry mix across the Tennessee Valley, Walton said",
				NerEngines.STANFORD, context);
		System.out.println(results);

	}
}
