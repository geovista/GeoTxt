package edu.psu.ist.vaccine.geocoder.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapperSolrDoc;

public class SolrQuerying {

	/** url of our solr geonames database */
	static String url = "http://zeus.geog.psu.edu:8988/solr/geotxt_geonames";
	// static String url = "http://zeus.geog.psu.edu:8080/solr/geonames";
	// static String url =
	// "http://zeus.geog.psu.edu:8988/solr/geotxt_geonames_20150807";

	protected SolrClient server;

	public SolrQuerying() {
		server = new HttpSolrClient(url);
		if (server == null)
			System.out.println("WARNING: could not connect to server");
	}

	/** Queries SOLR database to get up to MAX_ROWS toponyms for name */
	public List<LocationWrapper> getToponymsFromSolr(String name, int MAX_ROWS,
			String boostType) {
		// System.out.println("Querying for "+name);
		SolrDocumentList docs = null;
		ArrayList<LocationWrapper> matches = new ArrayList<LocationWrapper>();

		SolrQuery query = new SolrQuery();
		
		query.set("q", "\"" + name + "\"");

		query.set("defType", "edismax");
		query.set("rows", "" + MAX_ROWS);
		query.set("fl", "*,score");

		

		if (boostType.equalsIgnoreCase("general")) {

			query.set("q", "\"" + name + "\"");
			
			//query.set("qf","name");
			
			//query.set("qf","name^0.001 alternatenamesStr^10000 alternatenames^0.0001");
			////Golden //query.set("qf","name^0.01 alternatenamesStr^1000 alternatenames^0.001"); 

			query.set("qf","name^0.1 alternatenamesStr^1000 alternatenames^0.01");

			
		
		
			if (name.length()==2){
				//what about length == 3 for oldder abbreviations?
				query.set("bf", "population^0.00000005 alternatenamescount^0.0000005");//added two zeros to population
				
				query.set("bq", "featurecode:PCLI^3" 
						+" featurecode:ADM1^8"//used to be 8
						+" countrycode:US^3" //changed from 2 to 3
						+" continentname:North America^2" //added
//						+ " featurecode:PPLC^2"//
//						+ " featurecode:PPLG^2"
//						//+ " featurecode:PCLD^4.1"
//						+ " featurecode:PCLD^2"
//						+ " featurecode:PPLA^5"
//						+ " featurecode:PPLA2^4"
//						+ " featurecode:PPL^1" 
						//boost on continent.
					
						//+ " featurecode:PPLG^1"
						//+ " featurecode:PPLA^1"
						//what about countrycodes? They are not us based. What about LA?
						);
				} 
				
				else{
					//query.set("bf", "population^0.00000005 alternatenamescount^0.0000005");
					query.set("bf", "population^0.00000005 alternatenamescount^0.000005");//

					
			query.set("bq", "featurecode:PCLI^5" 
					+ " featurecode:PPLC^4.5"//used to be 5
					+ " featurecode:PCL^4.9"
					+ " featurecode:PPLG^3"
					//+ " featurecode:PCLD^4.1"
					+ " featurecode:PCLD^3"
					+ " featurecode:PPLA^4.1"//used to be 3.9 change to 4.1
					+ " featurecode:ADM1^4.5"//used to be 4					
					+ " featurecode:ADM1H"
					+ " featurecode:PPLA2^6"//used to be 3 change to 4.5 then change to 6
					+ " featurecode:ADM2^2.5"//used to be 2
					+ " featurecode:ADM2H"
					+ " featurecode:PPLA3^1.9"
					+ " featurecode:ADM3^1.1"
					+ " featurecode:ADM3H"
					+ " featurecode:PPLCH^2"
					+ " featurecode:PPL^5.5" //used to be 1.8	
					+ " featurecode:PPLS^1.7"	
					+ " featurecode:OCN^4"
					+ " featurecode:ISL^1.5" //*took 0.5 off.
					+ " featurecode:MT^1.9"
					+ " featurecode:AIRP^0.5" //*took 0.5 off
					+ " featurecode:RGN^1.75"//used to be 1
					+ " featurecode:RGNE^1" 
					+ " featurecode:CONT^2"
					+ " featurecode:PCLS^1.5"
					+ " featurecode:RD^0.5"
//					+ " featurecode:PRK^1"
//					+ " featurecode:PPLX^1"
//					+ " featurecode:STM^0.25"
//					+ " featurecode:UNIV^0.5"
//					+ " featurecode:HSP^0.25"
					);
					
		} 
			

		
			

		} else if (boostType.equalsIgnoreCase("landmarks")) {

			query.set("q", "\"" + name + "\"");
			query.set("qf",
					"name^0.001 alternatenamesStr^10000 alternatenames^0.0001");

			query.set("bf", "population^0.005");
	
		}
		

		try {
			// System.out.println("Server: "+server);
			QueryResponse rsp = server.query(query);
			docs = rsp.getResults();

			// System.out.println("Results:");
			for (int i = 0; i < docs.size(); i++) {
				SolrDocument d = docs.get(i);
				// System.out.println(d);
				matches.add(new LocationWrapperSolrDoc(d));
				// toponyms.add(t);
				// //System.out.println(t.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches;
	}

	/**
	 * Queries SOLR database to get up to MAX_ROWS toponyms for name The
	 * Bounding box is determined previously and gives us an estimate of where
	 * the place is and our confidence.
	 * */
	public List<LocationWrapper> getToponymsFromSolr(String name, int MAX_ROWS,
			double minx, double miny, double maxx, double maxy) {

		SolrDocumentList docs = null;
		ArrayList<LocationWrapper> matches = new ArrayList<LocationWrapper>();

		SolrQuery query = new SolrQuery();
		query.set("q", "\"" + name + "\"");
		query.set("qf",
				"name^0.001 alternatenamesStr^10000 alternatenames^0.0001");

		String centroid = ((miny + maxy) / 2) + "," + ((minx + maxx) / 2);
		// About 110 kilometers in a degree at the Equator. Use this as a rough
		// estimation for the d parameter.
		// Find the width of the bbox, multiply it by 1.1 to expand it slightly,
		// then multiply by 110 to get meters.
		double distance = (maxx - ((minx + maxx) / 2)) * 2 * 1.1 * 110;
		// distance = (distance > 1) ? distance : 1;

		query.set("defType", "edismax");
		query.set("bf", "population^0.005 recip(geodist(),2,200,20)");
		// query.set("mysq",
		// "{!geofilt sfield=point filter=false score=recipDistance pt=" +
		// centroid + " d=" + distance +"}");
		query.set("fq", "{!geofilt}");
		query.set("pt", centroid);
		query.set("d", "" + distance);
		query.set("sfield", "point");
		query.set("rows", "" + MAX_ROWS);
		query.set("fl", "*,score");

		try {
			QueryResponse rsp = server.query(query);
			docs = rsp.getResults();

			for (int i = 0; i < docs.size(); i++) {
				SolrDocument d = docs.get(i);
				matches.add(new LocationWrapperSolrDoc(d));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches;
	}
	
	public void closeConnection() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
