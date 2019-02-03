package edu.psu.ist.vaccine.geocoder.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public class SolrAddDoc5 {

	protected SolrClient solr;
	private ArrayList<String> solrFields;
	private static final long LOWER_RANGE = 0; // assign lower range value
	private static final long UPPER_RANGE = Long.MAX_VALUE; // assign upper
															// range value
	private static Random random = new Random();
	static String commitUrl = "http://zeus.geog.psu.edu:8988/solr/geotxt_geonames";

	public SolrAddDoc5() {
		solr = new HttpSolrClient(SolrAddDoc5.commitUrl);
		if (solr == null)
			System.out.println("WARNING: could not connect to server");
	}

	/**
	 * Queries SOLR database to get up to MAX_ROWS toponyms for name
	 * 
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public long addSolrDoc(Map<String, String> props) throws SolrServerException, IOException {
		long id = LOWER_RANGE + (long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
		if (props.size() > 0) {
			Iterator<String> iter = props.keySet().iterator();
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", id);
			String latitude = "";
			String longitude = "";
			while (iter.hasNext()) {
				String key = iter.next();
				String value = props.get(key);
				doc.addField(key, value);
				if (key.equals("latitude")) {
					latitude = value;
				}
				if (key.equals("longitude")) {
					longitude = value;
				}
			}
			
			String point = latitude + "," + longitude;
			String geom = "POINT(" + longitude + " " + latitude + ")";
			doc.addField("point", point);
			doc.addField("geom", geom);

			try {
				UpdateResponse rsp = solr.add(doc);
			} catch (Exception e) {
				e.printStackTrace();
			}
			solr.commit();
		}
		return id;
	}
}