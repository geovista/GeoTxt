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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * 
 * @author MortezaKarimzadeh
 */
// This analyzer just does NER, with no geocoding. 
public class NerAnalyzer implements Analyzer {

	public static Map<NerEngines, Object> nerMap = null;

	//
	public NamedEntities analyze(String QueryText, NerEngines ner, Map<String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException {

		// String strippedQuery = StripStrings.strip(QueryText);
		String strippedQuery = QueryText;
		
		HashtagProcessor processed = new HashtagProcessor();
		processed = HashtagProcessor.processHashTags(StringEscapeUtils.unescapeHtml4(strippedQuery));
		strippedQuery = processed.getHashtagRemoved();		

		NamedEntities doc = null;

		// if (nerMap.get(ner)==null){
		// return doc;
		// }

		doc = ((AbstractNer) nerMap.get(ner)).tagAlltoDoc(strippedQuery);

		if (doc == null) {
			return null;
		}
		
		doc.adjustCharIndexesForHashtags(processed);

		return doc;
	}

	//

	public NerAnalyzer(String stanfordAddress) {
		StanfordNer st = new StanfordNer(stanfordAddress);
		NerAnalyzer.nerMap = new HashMap<NerEngines, Object>();
		NerAnalyzer.nerMap.put(NerEngines.STANFORD, st);

	}

	public NerAnalyzer(Map<NerEngines, Object> nerMap) {
		NerAnalyzer.nerMap = nerMap;
	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalArgumentException, URISyntaxException, IOException {
		NerAnalyzer nerAnalyizer = new NerAnalyzer("C:/Programs/Stanford/english.all.3class.distsim.crf.ser.gz");

		// temporarily pass an empty Map
		// object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// --------------------------------------------------------------------------------------

		NamedEntities results = nerAnalyizer.analyze("I live in London, Ontario", NerEngines.STANFORD, context);

		System.out.println(results);

	}
}
