/*
 * This class is implemented for
 * evaluating Clavin using the GeoTxt evaluation framework.
 */
package edu.psu.ist.vaccine.analyzers;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.AbstractNer;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geocoder.solr.SolrQuerying;
import edu.psu.ist.vaccine.geotxt.utils.HashtagProcessor;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.StringEscapeUtils;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.query.Gazetteer;
import com.bericotech.clavin.gazetteer.query.LuceneGazetteer;
import com.bericotech.clavin.resolver.ClavinLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;

/**
 * 
 * @author MortezaKarimzadeh
 */
// This class uses Solr to retreive tooponyms. No particular disambiguation implemented.
public class ClavinAnalyzer {

	public static Map<NerEngines, Object> nerMap = null;
	// protected int maxRows = 100;
	GeoParser clavinParser = null;
	ClavinLocationResolver clavinResolver = null;

	//
	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws Exception {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;
		//strippedQuery = StringEscapeUtils.unescapeHtml4(strippedQuery);
		//HashtagProcessor processed = new HashtagProcessor();
		//processed = HashtagProcessor.processHashTags(strippedQuery);
		//strippedQuery = processed.getHashtagRemoved();


		NamedEntities doc = new NamedEntities();

		// if (nerMap.get(ner)==null){
		// return doc;
		// }
		
		/*
		// <GeoTxt NER>
		NamedEntities doc2 = new NamedEntities();
		doc2 = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);
		List<LocationOccurrence> locOcList = new ArrayList<LocationOccurrence>();
		for (Location l : doc2.locs) {
			for (int pos : l.getPositions()) {
				LocationOccurrence locOc = new LocationOccurrence(l.getName(), pos);
				locOcList.add(locOc);
			}
		}
		// </GeoTxt NER>	 
		 */

//		// <Clavin resolve to loc using GeoTxt NER>
//		List<ResolvedLocation> resolvedLocations = clavinResolver.resolveLocations(locOcList, false);
//		for (ResolvedLocation resolvedLocation : resolvedLocations) {
//			PointGeometry g = new PointGeometry(resolvedLocation.getGeoname().getLongitude(), resolvedLocation.getGeoname().getLatitude(), BigInteger.valueOf(resolvedLocation.getGeoname().getGeonameID()));
//			Location l = new Location(resolvedLocation.getLocation().getText(), "clavinLoc", resolvedLocation.getLocation().getPosition(), g);
//			doc.addLoc(l);
//		}
//		// </Clavin resolve to loc using GeoTxt NER>
		 
	

		// if (doc == null) {
		// return null;
		// }
		
		

		// <Calvin full pipeline out of box>
		List<ResolvedLocation> resolvedLocations = clavinParser.parse(strippedQuery);

		for (ResolvedLocation resolvedLocation : resolvedLocations) {
			PointGeometry g = new PointGeometry(resolvedLocation.getGeoname().getLongitude(), resolvedLocation.getGeoname().getLatitude(), BigInteger.valueOf(resolvedLocation.getGeoname().getGeonameID()));
			Location l = new Location(resolvedLocation.getLocation().getText(), "clavinLoc", resolvedLocation.getLocation().getPosition(), g);
			doc.addLoc(l);
		}
		// </Calvin full pipeline out of box>
		 
	

		
//		// <GeoTxt Solr>>
//		SolrQuerying sq = new SolrQuerying();
//
//		for (Location l : doc.locs) {
//
//			// go over all identified place names
//			List<LocationWrapper> searchResult = null;
//
//			// get best matches from solr / web service
//			searchResult = sq.getToponymsFromSolr(l.getName(), 20, "General");
//
//			// filter based on relative score
//			if (searchResult.size() > 0) {
//				double bestScorePercentage = searchResult.get(0).getScore() * 0.1;
//				double bestScorePercentage2 = searchResult.get(0).getScore() * 0.2;
//
//			}
//
//			l.setCandidates(searchResult);
//
//			// set initial geometry based on best match
//			LocationWrapper t = null;
//			if (searchResult.size() > 0)
//				t = searchResult.get(0);
//
//			if (t != null) {
//				try {
//					l.setGeometry(new PointGeometry(t.getName(), t.getLongitude(), t.getLatitude(), BigInteger.valueOf(t.getGeoNameId())));
//					l.setHierarchy(t.getHierarchy());
//					l.setCountryCode(t.getCountryCode());
//					l.setFeatureClass(t.getFeatureClass());
//					l.setFeatureCode(t.getFeatureCode());
//					l.setAlternateNames(t.getAlternateNames());
//				} catch (Exception ex) {
//					java.util.logging.Logger.getLogger(HierarchyAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
//				}
//			}
//
//		}
//
//		sq.closeConnection();
//
//		//</GeoTxt Solr>
		 
	

		//doc.adjustCharIndexesForHashtags(processed);

		return doc;
	}

	//

	public ClavinAnalyzer(Map<NerEngines, Object> nerMap) {

		ClavinAnalyzer.nerMap = nerMap;

		try {
			// set the address to Clavin's index directory here.
			this.clavinParser = GeoParserFactory.getDefault("E:/clavingeonames/CLAVIN/IndexDirectory");
			Gazetteer gazetteer = new LuceneGazetteer(new File("E:/clavingeonames/CLAVIN/IndexDirectory"));
			this.clavinResolver = new ClavinLocationResolver(gazetteer);
		} catch (ClavinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String args[]) throws Exception {
		ClavinAnalyzer clavinAnalyzer = new ClavinAnalyzer(new HashMap<NerEngines, Object>());

		// temporarily pass an empty Map
		// object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// --------------------------------------------------------------------------------------

		NamedEntities results = clavinAnalyzer.analyze("I live in London, Ontario", NerEngines.NONE, context);

		System.out.println(results);

	}
}
