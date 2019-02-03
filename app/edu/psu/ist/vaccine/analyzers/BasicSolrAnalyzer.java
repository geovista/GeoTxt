/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.analyzers;

import edu.psu.ist.vaccine.geotxt.api.GeoTxtApi;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.AbstractNer;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.ner.StanfordNer;
import edu.psu.ist.vaccine.geocoder.SolrModifiedGeoCoder;
import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.utils.Analyzer;
import edu.psu.ist.vaccine.geotxt.utils.BBox;
import edu.psu.ist.vaccine.geotxt.utils.HashtagProcessor;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import edu.psu.ist.vaccine.geotxt.utils.TextPreprocessing;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * @author MortezaKarimzadeh
 */
// This class uses Solr to retreive tooponyms. No particular disambiguation implemented.
public class BasicSolrAnalyzer implements Analyzer {

	public static Map<NerEngines, Object> nerMap = null;
	protected int maxRows = 100;

	//
	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;
		strippedQuery = StringEscapeUtils.unescapeHtml4(strippedQuery);
		 HashtagProcessor processed = new HashtagProcessor();
		 processed = HashtagProcessor.processHashTags(strippedQuery);
		 strippedQuery = processed.getHashtagRemoved();

		NamedEntities doc = null;

		// if (nerMap.get(ner)==null){
		// return doc;
		// }

		doc = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);

		if (doc == null) {
			return null;
		}

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

		doc.adjustCharIndexesForHashtags(processed);

		return doc;
	}

	//

	public NamedEntities analyze(String QueryText, NerEngines ner, String[] contextTypes) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;

		NamedEntities doc = null;

		doc = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);

		if (doc == null) {
			return null;
		}

		// We always need to do an initial pass
		for (Location l : doc.locs) {
			try {
				LocationWrapper t = SolrModifiedGeoCoder.geoCode(l.getName());
				if (t != null) {
					l.setGeometry(new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId())));
					l.setHierarchy(t.getHierarchy());
					l.setCountryCode(t.getCountryCode());
					l.setFeatureClass(t.getFeatureClass());
					l.setFeatureCode(t.getFeatureCode());
				}
			} catch (Exception ex) {
				java.util.logging.Logger.getLogger(BasicSolrAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		for (String contextType : contextTypes) {

			switch (contextType) {

			case GeoTxtApi.CONTEXTTYPESPATIALBBOX:
				for (Location l : doc.locs) {
					BBox bbox = new BBox();
					for (Location l2 : doc.locs) {
						if (!l.equals(l2)) {
							bbox.expand(l2.getGeometry().getCoordinates()[0], l2.getGeometry().getCoordinates()[1]);
						}
					}
					l.setBBox(bbox);
				}
				break;

			case GeoTxtApi.CONTEXTTYPESPATIALHIERARCHY:

				for (Location l : doc.locs) {
					String[] hLevels = new String[doc.locs.size() - 1];
					for (int i = 0; i < doc.locs.size(); i++) {
						Location l2 = doc.locs.get(i);
						if (!l.equals(l2)) {
							hLevels[i] = l2.getSelfHLevel();
						}
					}
					l.setHLevels(hLevels);
				}
				break;
			}
		}

		for (Location l : doc.locs) {
			try {
				LocationWrapper t = SolrModifiedGeoCoder.geoCode(l.getName(), l.getBBox(), l.getHLevels());
				if (t != null) {
					l.setGeometry(new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId())));
					l.setHierarchy(t.getHierarchy());
					l.setCountryCode(t.getCountryCode());
					l.setFeatureClass(t.getFeatureClass());
					l.setFeatureCode(t.getFeatureCode());
				}
			} catch (Exception ex) {
				java.util.logging.Logger.getLogger(BasicSolrAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return doc;
	}

	public BasicSolrAnalyzer(String stanfordAddress, int maxRows) {

		StanfordNer st = new StanfordNer(stanfordAddress);
		BasicSolrAnalyzer.nerMap = new HashMap<NerEngines, Object>();
		BasicSolrAnalyzer.nerMap.put(NerEngines.STANFORD, st);
		this.maxRows = maxRows;

	}

	public BasicSolrAnalyzer(Map<NerEngines, Object> nerMap, int maxRows) {

		BasicSolrAnalyzer.nerMap = nerMap;
		this.maxRows = maxRows;

	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalArgumentException, URISyntaxException, IOException {
		BasicSolrAnalyzer basicStanfordAnalyizer = new BasicSolrAnalyzer("C:/Programs/Stanford/english.all.3class.distsim.crf.ser.gz", 100);

		// temporarily pass an empty Map
		// object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// --------------------------------------------------------------------------------------

		NamedEntities results = basicStanfordAnalyizer.analyze("I live in London, Ontario", NerEngines.STANFORD, context);

		System.out.println(results);

	}
}
