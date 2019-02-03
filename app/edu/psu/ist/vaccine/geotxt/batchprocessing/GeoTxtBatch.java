package edu.psu.ist.vaccine.geotxt.batchprocessing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.fasterxml.jackson.databind.JsonNode;

import edu.psu.ist.vaccine.geotxt.api.GeoTxtApi;
import play.libs.Json;
import play.mvc.Results;

public class GeoTxtBatch {

	private GeoTxtApi geoTxt;
	private int max = 10;

	public GeoTxtBatch(GeoTxtApi geoTxt) {
		this.geoTxt = geoTxt;
	}

	public ArrayList<String[]> batchProcess(String dt, ArrayList<String[]> items) {
		ArrayList<String[]> ret = new ArrayList<>();
		items.stream().parallel().forEach(s -> ret.add(runGeoTxt(s)));
		return ret;
	}
//
//	private JSONObject runGeoTxt(JsonNode obj) {
//		String m = (String) obj.get("m");
//		String q = (String) obj.get("q");
//		if ((m.equals("stanford") || m.equals("gate")) || (m.equals("stanfordh") || m.equals("gateh"))
//				|| (m.equals("stanfords") || m.equals("gates") || m.equals("none"))) {
//			try {
//				String r = geoTxt.geoCodeToGeoJson(q, m, true, 100, true, true);
//				obj.put("r", JSONValue.parse(r));
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			obj.put("r", "Wrong Method");
//		}
//		return obj;
//	}

	private String[] runGeoTxt(String[] item) {
		String m = item[1];
		String q = item[2];
		if ((m.equals("stanford") || m.equals("gate")) || (m.equals("stanfordh") || m.equals("gateh"))
				|| (m.equals("stanfords") || m.equals("gates") || m.equals("none"))) {
			try {
				String r = geoTxt.geoCodeToGeoJson(q, m, true, 100, true, true);
				item[3] = r;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			item[3] = "Wrong Method";
		}
		return item;
	}

	public void batchJSON(JSONArray arr) {
		for (int i = 0; i < arr.size(); i++) {
			JSONObject obj = (JSONObject) arr.get(i);
			String m = (String) obj.get("m");
			String q = (String) obj.get("q");
			if ((m.equals("stanford") || m.equals("gate")) || (m.equals("stanfordh") || m.equals("gateh"))
					|| (m.equals("stanfords") || m.equals("gates") || m.equals("none"))) {
				try {
					String r = geoTxt.geoCodeToGeoJson(q, m, true, 100, true, true);
					obj.put("r", JSONValue.parse(r));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				obj.put("r", "Wrong Method");
			}
		}
	}

}

class ItemCompare implements Comparator<String[]> {

	@Override
	public int compare(String[] o1, String[] o2) {
		// write comparison logic here like below , it's just a sample
		return (new Integer(o1[0])).compareTo(new Integer(o2[0]));
	}
}
