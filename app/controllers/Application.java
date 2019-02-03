package controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.csv.*;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import play.Play;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.*;
import edu.psu.ist.vaccine.geocoder.GeonamesGeoCoder;
import edu.psu.ist.vaccine.geocoder.solr.SolrAddDoc5;
import edu.psu.ist.vaccine.geotxt.api.GeoTxtApi;
import edu.psu.ist.vaccine.geotxt.batchprocessing.GeoTxtBatch;
import edu.psu.ist.vaccine.geotxt.utils.FileWriter;
import edu.psu.ist.vaccine.corpusbuilding.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Application extends Controller {
	static String STANFORDMODEL = Play.application().configuration().getString("STANFORDMODEL");
	static String GATEHOME = Play.application().configuration().getString("GATEHOME");
	static String OPENNLPDIR = Play.application().configuration().getString("OPENNLPDIR");
	static String LINGPIPEMODEL = Play.application().configuration().getString("LINGPIPEMODEL");
	static String MITMODEL = Play.application().configuration().getString("MITMODEL");
	static String LOCATIONFILES = Play.application().configuration().getString("LOCATIONFILES");

	static Logger log = org.apache.log4j.Logger.getRootLogger();
	// static Logger log = Logger.getLogger(Application.class);

	//static GeoTxtApi geoTxt = new GeoTxtApi(GATEHOME, STANFORDMODEL, true, OPENNLPDIR, LINGPIPEMODEL, MITMODEL);
	static GeoTxtApi geoTxt = new GeoTxtApi(null, STANFORDMODEL, false, null, null, MITMODEL);

	public static Result index() {
		return ok(index.render("GeoTxt"));
	}

	public static Result corpusBuildingUi() {
		return ok(corpusBuildingUi.render());
	}

	public static Result codingHistoryUi() {
		return ok(codingHistoryUi.render("Coding History-GeoCorpora"));
	}

	public static Result document() {
		return ok(document.render("GeoTxt API documentation"));
	}

	public static Result geovista() {
		return ok(geoVistaUsers.render("GeoTxt"));
	}

	public static Result geocode(String q) {
		JsonNode result = Json.newObject();
		String ret = null;
		try {
			ret = GeonamesGeoCoder.geoCodetoGeoJson(q);
		} /*
			 * catch (IllegalArgumentException e) { e.printStackTrace(); } catch (URISyntaxException e) { e.printStackTrace(); }
			 */catch (Exception e) {
			e.printStackTrace();
		}
		result = Json.parse(ret);
		return ok(result);
	}

	// TODO: This can be removed and instead the NerAnalyzer in the analyzers be used.
	public static Result extract(String m, String q) {
		JsonNode result = Json.newObject();
		// String ret = null;
		// if (m.equals("gate")) {
		// ret = GeoTxtApi.basicGateAnalyzer.gate.tagAlltoGeoJson(q, false, 0, false, false);
		// } else if (m.equals("stanford")) {
		// ret = GeoTxtApi.hierarchyAnalyzer.st.tagAlltoGeoJson(q, false, 0, false, true);
		// } else {
		// return Results.badRequest("Wrong Method");
		// }
		// result = Json.parse(ret);
		return ok(result);
	}

	public static Result geotxt(String m, String q) {
		JsonNode result = Json.newObject();
		String ret = null;
		if ((m.equals("stanford") || m.equals("inline") || m.equals("mit")|| m.equals("gate")) || (m.equals("stanfordh") || m.equals("cogcomph") || m.equals("cogcomppr") || m.equals("stanfordpr") || m.equals("cogcompsh")|| m.equals("stanfordsh") || m.equals("gateh") || m.equals("cogcomph")) || (m.equals("cogcomp") || m.equals("opennlp") || m.equals("lingpipe") || m.equals("stanfords") || m.equals("gates") || m.equals("geocoder"))) {
			try {
				ret = geoTxt.geoCodeToGeoJson(q, m, true, 100, true, true);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			return Results.badRequest("Wrong Method");
		}
		result = Json.parse(ret);
		return ok(result);
	}

	// Just a post method to GeoTxt for long pieces of text
	@BodyParser.Of(BodyParser.Json.class)
	public static Result geotxtPst() {

		JsonNode data = request().body().asJson();
		String m = data.get("m").asText();
		String q = data.get("q").asText();
		return geotxt(m, q);
	}

	public static Result geotxtBatch(String d, String w, String z) throws IOException {
		GeoTxtBatch geoTxtBatch;
		JsonNode result = Json.newObject();
		ArrayList<String[]> items;
		ArrayList<String[]> itemsOut = new ArrayList<>();
		int counter = 0;
		switch (d.toLowerCase()) {
		case "json":
			JsonNode data = request().body().asJson();
			geoTxtBatch = new GeoTxtBatch(geoTxt);
			Iterator<JsonNode> iter = data.iterator();
			items = new ArrayList<>();
			counter = 0;
			while (iter.hasNext()) {
				JsonNode obj = iter.next();
				String m = obj.get("m").asText();
				String q = obj.get("q").asText();
				items.add(new String[] { counter + "", m, q, "" });
				counter++;
			}
			itemsOut = geoTxtBatch.batchProcess(d, items);
		case "csv":
			String body = request().body().asText();
			InputStreamReader in = new InputStreamReader(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
			Iterable<CSVRecord> parser = null;
			try {
				parser = CSVFormat.DEFAULT.parse(in);
			} catch (IOException ex) {
				Results.badRequest("".replace("Error: ", ""));
			}
			geoTxtBatch = new GeoTxtBatch(geoTxt);
			items = new ArrayList<>();
			counter = 0;
			for (CSVRecord record : parser) {
				String m = record.get(0);
				String q = record.get(1);
				items.add(new String[] { counter + "", m, q, "" });
				counter++;
			}
			itemsOut = geoTxtBatch.batchProcess(d, items);
		}

		Map<Integer, String[]> map = new TreeMap();
		for (String[] item : itemsOut) {
			if (item != null) {
				map.put(Integer.valueOf(item[0]), item);
			}
		}

		switch (w.toLowerCase()) {
		case "json":
			JSONArray arr = new JSONArray();
			for (String[] item : map.values()) {
				JSONObject obj = new JSONObject();
				obj.put("m", item[1]);
				obj.put("q", item[2]);
				obj.put("r", item[3]);
				arr.add(obj);
			}
			if (Boolean.parseBoolean(z)) {
				File file = new File("result.zip");
				FileOutputStream fos = new FileOutputStream(file);
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				zipOut.setLevel(ZipOutputStream.DEFLATED);
				ZipEntry entry = new ZipEntry("result.json");
				zipOut.putNextEntry(entry);
				zipOut.write(arr.toJSONString().getBytes("UTF-8"));
				zipOut.closeEntry();
				zipOut.close();
				return ok(file);
			} else {
				result = Json.parse(arr.toJSONString());
			}
			return ok(result);
		case "csv":
			String out = "";
			StringWriter stringWriter = new StringWriter();
			CSVFormat csvStringFormat = CSVFormat.DEFAULT;
			CSVPrinter csvStringPrinter = null;
			try {
				csvStringPrinter = new CSVPrinter(stringWriter, csvStringFormat);
				for (String[] item : map.values()) {
					csvStringPrinter.printRecord((Object[]) item);
				}
				csvStringPrinter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			out = stringWriter.toString();
			if (Boolean.parseBoolean(z)) {
				File file = new File("result.zip");
				FileOutputStream fos = new FileOutputStream(file);
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				zipOut.setLevel(ZipOutputStream.DEFLATED);
				ZipEntry entry = new ZipEntry("result.csv");
				zipOut.putNextEntry(entry);
				zipOut.write(out.getBytes("UTF-8"));
				zipOut.closeEntry();
				zipOut.close();
				return ok(file);
			}
			return ok(out);
		}
		return Results.badRequest("".replace("Error: ", ""));
	}

	public static Result geotxtAddEntry() throws SolrServerException, IOException {
		String result = "";
		String data = request().body().asText();
		if (data != null) {
			String lines[] = data.split("\\r?\\n");
			Map<String, String> entries = new HashMap<>();
			for (String line : lines) {
				if (!line.trim().equals("")) {
					String[] pair = line.split("=");
					entries.put(pair[0], pair[1]);
				}
			}

			if (entries.size() > 0) {
				SolrAddDoc5 addLocation = new SolrAddDoc5();
				long docId = addLocation.addSolrDoc(entries);
				result += "docid: " + String.valueOf(docId) + ",";

				try {
					FileWriter.writeFile(data, LOCATIONFILES + "/" + docId + ".txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return ok(result);
	}

	public static Result exposeCorpus(String geocoder, String role) throws SQLException {

		JsonNode result = Json.newObject();

		String ret = null;

		ret = CorpusExposerApi.getTweetFromDb(geocoder, role);

		result = Json.parse(ret);

		return ok(result);

	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result submitGcResults() throws SQLException {

		JsonNode data = request().body().asJson();

		String annotation = data.get("annotation").toString();

		String responseStatus = ResultSubmitter.submitGcResults(annotation);

		JsonNode result = Json.parse(responseStatus);

		return ok(result);

	}

	public static Result addLocationUi() {
		return ok(addLocationUi.render("Add Location"));
	}

	public static Result addLocation(String d) {
		JsonNode result = Json.newObject();
		String ret = null;
		SolrAddDoc5 sad = new SolrAddDoc5();
		result = Json.parse(ret);
		return ok(result);
	}
}
