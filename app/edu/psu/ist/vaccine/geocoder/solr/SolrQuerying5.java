package edu.psu.ist.vaccine.geocoder.solr;

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

public class SolrQuerying5 {

	/** url of our solr geonames database */
	static String url = "http://www.zeus.geovista.psu.edu/geotxtsolr";

	protected SolrClient solr;

	public SolrQuerying5() {
		solr = new HttpSolrClient(url);
		if (solr == null) System.out.println("WARNING: could not connect to server");
	}

	/** Queries SOLR database to get up to MAX_ROWS toponyms for name */
	public  List<LocationWrapper> getToponymsFromSolr (String name, int MAX_ROWS) {
		//System.out.println("Querying for "+name);
		SolrDocumentList docs = null;
		ArrayList<LocationWrapper> matches = new ArrayList<LocationWrapper>();

		SolrQuery query = new SolrQuery();
		query.set("q", "\""+name+"\"");
		query.set("qf","name^0.001 alternatenamesStr^10000 alternatenames^0.0001");
				
			//	"name:\""+name+"\"^0.001 OR alternatenamesStr:\""+name+"\"^10000 OR alternatenames:\""+name+"\"^0.0001" ); 
		
		//query.set("q", "name:\""+name+"\"^0.001"  ); 
		/*String qu = "nameStr:\""+name+"\"^10000 OR (alternatenamesStr:\""+name+"\"^10000 NOT nameStr:\""+name+"\") "+
				  " OR (name:\""+name+"\"^0.01 NOT nameStr:\""+name+"\" NOT alternatenamesStr:\""+name+"\") "+
			      " OR (alternatenames:\""+name+"\"^0.0001 NOT nameStr:\""+name+"\" NOT alternatenamesStr:\""+name+"\" NOT name:\""+name+"\")";
		System.out.println(qu);
		query.set("q", qu); */
		
		
		query.set("defType","edismax");
		query.set("bf", "population^0.005");
		query.set("rows",""+MAX_ROWS);
		query.set("fl", "*,score");

		try {
			//System.out.println("Server: "+server);
			QueryResponse rsp = solr.query(query);
			docs = rsp.getResults();

			//		System.out.println("Results:");
			for (int i = 0; i < docs.size(); i++) {
				SolrDocument d = docs.get(i);
				//			System.out.println(d);
				matches.add(new LocationWrapperSolrDoc(d));
				//				toponyms.add(t);
				//				//System.out.println(t.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches;
	}
	
	/** Queries SOLR database to get up to MAX_ROWS toponyms for name 
	 * The Bounding box is determined previously and gives us an estimate of where the place is and our confidence.
	 * */
	public  List<LocationWrapper> getToponymsFromSolr (String name, int MAX_ROWS, double minx, double miny, double maxx, double maxy) {
		//System.out.println("Querying for "+name);
		SolrDocumentList docs = null;
		ArrayList<LocationWrapper> matches = new ArrayList<LocationWrapper>();

		SolrQuery query = new SolrQuery();
		query.set("q", "\""+name+"\"");
		query.set("qf","name^0.001 alternatenamesStr^10000 alternatenames^0.0001");
				
			//	"name:\""+name+"\"^0.001 OR alternatenamesStr:\""+name+"\"^10000 OR alternatenames:\""+name+"\"^0.0001" ); 
		
		//query.set("q", "name:\""+name+"\"^0.001"  ); 
		/*String qu = "nameStr:\""+name+"\"^10000 OR (alternatenamesStr:\""+name+"\"^10000 NOT nameStr:\""+name+"\") "+
				  " OR (name:\""+name+"\"^0.01 NOT nameStr:\""+name+"\" NOT alternatenamesStr:\""+name+"\") "+
			      " OR (alternatenames:\""+name+"\"^0.0001 NOT nameStr:\""+name+"\" NOT alternatenamesStr:\""+name+"\" NOT name:\""+name+"\")";
		System.out.println(qu);
		query.set("q", qu); */
		
		String centroid = ((miny + maxy) / 2) + "," + ((minx + maxx) / 2);
		// About 110 kilometers in a degree at the Equator. Use this as a rough estimation for the d parameter.
		// Find the width of the bbox, multiply it by 1.1 to expand it slightly, then multiply by 110 to get meters.
		double distance = (maxx - ((minx + maxx) / 2)) * 2 * 1.1 * 110;
		
		query.set("defType","edismax");
		query.set("bf", "population^0.005");
		query.set("sfield", "latlng_geo");
		query.set("pt", centroid);
		query.set("d", ""+distance);
		query.set("bf", "recip(geodist(),2,200,20)");
		query.set("rows",""+MAX_ROWS);
		query.set("fl", "*,score");

		try {
			//System.out.println("Server: "+server);
			QueryResponse rsp = solr.query(query);
			docs = rsp.getResults();

			//		System.out.println("Results:");
			for (int i = 0; i < docs.size(); i++) {
				SolrDocument d = docs.get(i);
				//			System.out.println(d);
				matches.add(new LocationWrapperSolrDoc(d));
				//				toponyms.add(t);
				//				//System.out.println(t.getName());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return matches;
	}
}
