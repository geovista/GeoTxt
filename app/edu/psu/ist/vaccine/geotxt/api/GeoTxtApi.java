/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.api;

import edu.psu.ist.vaccine.analyzers.BasicSolrAnalyzer;
import edu.psu.ist.vaccine.analyzers.ClavinAnalyzer;
import edu.psu.ist.vaccine.analyzers.GeoCoderAnalyzer;
import edu.psu.ist.vaccine.analyzers.LandmarkGeoCoderAnalyzer;
import edu.psu.ist.vaccine.analyzers.SimpleHierarchyAnalyzer;
import edu.psu.ist.vaccine.analyzers.proximityAnalyzer;
import edu.psu.ist.vaccine.analyzers.HierarchyAnalyzer;
import edu.psu.ist.vaccine.analyzers.GeoNamesOrgAnalyzer;
import edu.psu.ist.vaccine.geotxt.hierarchy.MapHierarchyPlaces;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.ner.OpenNlpNer;
import edu.psu.ist.vaccine.geotxt.ner.GateNer;
import edu.psu.ist.vaccine.geotxt.ner.InlineAnnotatedNer;
import edu.psu.ist.vaccine.geotxt.ner.LingPipeNer;
import edu.psu.ist.vaccine.geotxt.ner.MitNer;
import edu.psu.ist.vaccine.geotxt.ner.CogCompNer;
import edu.psu.ist.vaccine.geotxt.ner.StanfordNer;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 *
 * @author MortezaKarimzadeh
 */
public class GeoTxtApi {

	// TODO Rewrite how analyzers are called now that NER and Analyzer are two separate items. Also make NERs accessible separately, maybe through NER analyzer.
	Map<NerEngines, Object> nerMap = null;
	public static HierarchyAnalyzer hierarchyAnalyzer = null;
	public static SimpleHierarchyAnalyzer simpleHierarchyAnalyzer = null;
	public static proximityAnalyzer proximityAnalyzer = null;
	public static GeoNamesOrgAnalyzer geoNamesOrgAnalyzer = null;
	public static BasicSolrAnalyzer basicSolrAnalyzer = null;
	public static GeoCoderAnalyzer geoCoderAnalyzer = null;
	public static LandmarkGeoCoderAnalyzer landmarkGeoCoderAnalyzer = null;
	// public static ClavinAnalyzer clavinAnalyzer = null;

	public static final String CONTEXTTYPESPATIALBBOX = "bbox";
	public static final String CONTEXTTYPESPATIALHIERARCHY = "hierarchy";

	public static Logger log = Logger.getLogger(GeoTxtApi.class);
	/**
	 * determines whether result will include alternate toponyms and scores
	 */
	public boolean includeAlternates = false;

	/**
	 * determines maximum number of alternate toponyms that will be included in result
	 */
	public int maxAlternates = 0;

	/**
	 * determines whether result will include hierarchy toponyms
	 */
	public boolean includeHierarchy = false;

	/**
	 * determines whether additional details (currently countrycode) will be included in result
	 */
	public boolean includeDetails = true;

	public String geoCodeToGeoJson(String QueryText, String EntityExtractorEngine, boolean includeAlternates, int maxAlternates, boolean includeHierarchy, boolean includeDetails) throws IllegalArgumentException, URISyntaxException, IOException {

		// temporarily pass an empty Map object------------------------------------------------------
		Map<String, Object> context = new HashMap<String, Object>();
		// ------------------------------------------------------------------------------
		// Note: inline is just mapped to one analyzer for now.
		if (EntityExtractorEngine.equalsIgnoreCase("inline")) {

			if (proximityAnalyzer != null && nerMap.containsKey(NerEngines.INLINE)) {
				NamedEntities doc = proximityAnalyzer.analyze(QueryText, NerEngines.INLINE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}
		}

		if (EntityExtractorEngine.equalsIgnoreCase("gateh")) {

			if (hierarchyAnalyzer != null && nerMap.containsKey(NerEngines.GATE)) {
				NamedEntities doc = hierarchyAnalyzer.analyze(QueryText, NerEngines.GATE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("stanfordh")) {

			if (hierarchyAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = hierarchyAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("cogcomph")) {

			if (hierarchyAnalyzer != null && nerMap.containsKey(NerEngines.COGCOMP)) {
				NamedEntities doc = hierarchyAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("cogcompsh")) {

			if (simpleHierarchyAnalyzer != null && nerMap.containsKey(NerEngines.COGCOMP)) {
				NamedEntities doc = simpleHierarchyAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		}

		else if (EntityExtractorEngine.equalsIgnoreCase("stanfordsh")) {

			if (simpleHierarchyAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = simpleHierarchyAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		}

		else if (EntityExtractorEngine.equalsIgnoreCase("stanfordpr")) {

			if (proximityAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = proximityAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("cogcomppr")) {

			if (proximityAnalyzer != null && nerMap.containsKey(NerEngines.COGCOMP)) {
				NamedEntities doc = proximityAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("cogcomph")) {
			if (hierarchyAnalyzer != null && nerMap.containsKey(NerEngines.COGCOMP)) {
				NamedEntities doc = hierarchyAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("gate")) {

			if (geoNamesOrgAnalyzer != null && nerMap.containsKey(NerEngines.GATE)) {
				NamedEntities doc = geoNamesOrgAnalyzer.analyze(QueryText, NerEngines.GATE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("stanford")) {

			if (geoNamesOrgAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = geoNamesOrgAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("gates")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.GATE)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.GATE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("stanfords")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("stanfordsspatial")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("geocoder")) {
			if (geoCoderAnalyzer != null) {
				NamedEntities doc = geoCoderAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("landmarkgeocoder")) {

			if (landmarkGeoCoderAnalyzer != null) {
				NamedEntities doc = landmarkGeoCoderAnalyzer.analyze(QueryText, NerEngines.NONE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("cogcomp")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.COGCOMP)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.COGCOMP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		}

		else if (EntityExtractorEngine.equalsIgnoreCase("opennlp")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.OPENNLP)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.OPENNLP, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		}

		else if (EntityExtractorEngine.equalsIgnoreCase("lingpipe")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.LINGPIPE)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.LINGPIPE, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else if (EntityExtractorEngine.equalsIgnoreCase("mit")) {

			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.MIT)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.MIT, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}
		}

		else if (EntityExtractorEngine.equalsIgnoreCase("extract")) {
			// TODO for now this is only using Stanford, but should really translate the NER in the query into the analyzer param.
			if (basicSolrAnalyzer != null && nerMap.containsKey(NerEngines.STANFORD)) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.STANFORD, context);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}
		} else {
			return ("Bad Request. Set the Entity Extractor Engine parameter appropriately.");
		}
	}

	public String geoCodeToGeoJson(String QueryText, String EntityExtractorEngine, boolean includeAlternates, int maxAlternates, boolean includeHierarchy, boolean includeDetails, String[] contextTypes) throws IllegalArgumentException, URISyntaxException, IOException {

		if (EntityExtractorEngine.equalsIgnoreCase("stanfords")) {

			if (basicSolrAnalyzer != null) {
				NamedEntities doc = basicSolrAnalyzer.analyze(QueryText, NerEngines.STANFORD, contextTypes);
				return GeoJsonWriter.docToGeoJson(doc, includeAlternates, maxAlternates, includeHierarchy, includeDetails);
			} else {
				return ("Not initialized.");
			}

		} else {

			return ("Bad Request. The Entity Extractor Engine parameter must be either \"Gate\" or \"Stanford\". ");

		}
	}

	public GeoTxtApi(String gateAddress, String stanfordAddress, boolean initializeCogComp, String openNlpAddress, String lingPipeAddress, String mitAddress) {

		nerMap = new HashMap<>();

		geoCoderAnalyzer = new GeoCoderAnalyzer(100);
		landmarkGeoCoderAnalyzer = new LandmarkGeoCoderAnalyzer(100);

		nerMap.put(NerEngines.INLINE, new InlineAnnotatedNer());

		if (gateAddress != null) {
			GateNer gate = new GateNer(gateAddress);
			nerMap.put(NerEngines.GATE, gate);
		}
		if (stanfordAddress != null) {
			StanfordNer st = new StanfordNer(stanfordAddress);
			nerMap.put(NerEngines.STANFORD, st);
		}
		if (initializeCogComp) {
			CogCompNer il = new CogCompNer("CONLL");
			nerMap.put(NerEngines.COGCOMP, il);
		}
		if (openNlpAddress != null) {
			OpenNlpNer openNlp = new OpenNlpNer(openNlpAddress);
			nerMap.put(NerEngines.OPENNLP, openNlp);
		}
		if (lingPipeAddress != null) {
			LingPipeNer lingPipe = new LingPipeNer(lingPipeAddress);
			nerMap.put(NerEngines.LINGPIPE, lingPipe);
		}
		if (mitAddress != null) {
			MitNer mit = new MitNer(mitAddress);
			nerMap.put(NerEngines.MIT, mit);
		}

		basicSolrAnalyzer = new BasicSolrAnalyzer(nerMap, 100);
		hierarchyAnalyzer = new HierarchyAnalyzer(nerMap, 100, 30);
		simpleHierarchyAnalyzer = new SimpleHierarchyAnalyzer(nerMap, 50, 30);
		proximityAnalyzer = new proximityAnalyzer(nerMap, 15, 5);
		// clavinAnalyzer = new ClavinAnalyzer(nerMap);

	}

	public static void main(String args[]) throws IllegalArgumentException, IllegalArgumentException, URISyntaxException, IOException {
		// load configuration parameters from property file
		Config config = new Config();
		MapHierarchyPlaces.username = "scottpez"; // put your geonames user name here

		// GeoTxtApi geoTxtApi = new GeoTxtApi(config.getGate_home(), config.getStanford_ner());
		// without stanford
		// GeoTxtApi geoTxtApi = new GeoTxtApi(config.getGate_home(), null);
		// without gate
		// GeoTxtApi geoTxtApi = new GeoTxtApi(config.getGate_home(), config.getStanford_ner(), false, config.getOpenNlpDir(), config.getLingPipeDir());

		GeoTxtApi geoTxtApi = new GeoTxtApi(null, config.getStanford_ner(), false, null, null, config.getMit_dir());

		// String[] contextTypes = new String[]{GeoTxtApi.CONTEXTTYPESPATIALBBOX, GeoTxtApi.CONTEXTTYPESPATIALHIERARCHY};
		// String[] contextTypes = new String[]{};
		// String myStr = "RT @shadihamid: <LOCATION>US</LOCATION> strikes in #<LOCATION>Syria</LOCATION> have badly undermined";

		String myStr = "I live in the United States. I love London.";

		System.out.println(geoTxtApi.geoCodeToGeoJson(myStr, "geocoder", true, 100, true, true));

	}
}
