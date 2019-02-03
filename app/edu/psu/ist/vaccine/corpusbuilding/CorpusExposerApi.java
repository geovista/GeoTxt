/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.corpusbuilding;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * @author muk229admin
 */
public class CorpusExposerApi {

	public static final Logger log = Logger.getLogger(CorpusExposerApi.class
			.getName());

	// create a constructor, depending on how many parameters you need for your
	// class
	// This method takes two parameters "geocoder" and "stage", and reads thw
	// tweet from the database. For stages 1 and 2
	public static String getTweetFromDb(String geoCoder, String role)
			throws SQLException {

		// TODO make sure that geoCoder and role equal the allowed values -
		// maybe something stored in the database.

		String packagedResponse = null;
		String query = null;

		// -------------- 0 --------------If this the first time this tweet is
		// being geocoded ---------------
		// if (stage == null || "GC0".equalsIgnoreCase(stage) ||
		// "1".equalsIgnoreCase(stage)) {
		if (("CoderA".equalsIgnoreCase(role) || "CoderB".equalsIgnoreCase(role))
				|| ("CoderARevisit".equalsIgnoreCase(role) || "CoderBRevisit"
						.equalsIgnoreCase(role))) {
			// Read a tweet from the database that has zero geocodings and has
			// CorpusStatus=ALternatesGenerated
			// simple "read a tweet from the database and just write it to
			// console.
			// pack it in a json and send it back.

			try {

				if ("CoderA".equalsIgnoreCase(role)) {
					query = "SELECT \"text\",\"GeotxtAlternates\",\"PIResult\",\"id_str\",\"rawJSON\",\"created_at\",\"AllCodings\",\"PIContainsUnresolved\",\"PIManualResolution\"  FROM \""
							+ DatabaseConnection.TWEET_TABLE_NAME
							+ "\" WHERE (\"CorpusStatus\" = 'AlternatesGenerated') AND ((\"GCCount\" IS null) OR  (\"GCCount\" = 0)) AND (\"PIResult\" != '' or (\"PIUnresolved\" != '' AND \"PIContainsUnresolved\" = 'YES' )) AND (\"GCStatus\" IS null or \"GCStatus\" = '' or \"GCStatus\" = 'GCRequired') ORDER BY RANDOM() LIMIT 1;";

				} else if ("CoderB".equalsIgnoreCase(role)) {
					// query =
					// "SELECT \"text\",\"GeotxtAlternates\",\"PIResult\",\"id_str\",\"rawJSON\",\"created_at\",\"AllCodings\"  FROM \""
					// + DatabaseConnection.TWEET_TABLE_NAME
					// + "\" INNER JOIN \""
					// + DatabaseConnection.GC_TABLE_NAME
					// + "\" ON \""
					// + DatabaseConnection.TWEET_TABLE_NAME
					// + "\".\"id_str\" =  \""
					// + DatabaseConnection.GC_TABLE_NAME
					// + "\".\"TweetId\" WHERE (\""
					// + DatabaseConnection.TWEET_TABLE_NAME
					// + "\".\"GCCount\" = 1) AND (\""
					// + DatabaseConnection.GC_TABLE_NAME
					// + "\".\"GeoCoder\" <> '" + geoCoder + "') LIMIT 1;";
					query = "SELECT \"text\",\"GeotxtAlternates\",\"PIResult\",\"id_str\",\"rawJSON\",\"created_at\",\"AllCodings\",\"PIContainsUnresolved\",\"PIManualResolution\"   FROM \""
							+ DatabaseConnection.TWEET_TABLE_NAME
							+ "\" WHERE (\"GCCount\" = 1 AND \"CoderA\" <> '"
							+ geoCoder
							+ "' AND \"CoderB\" IS null AND \"GCStatus\" = 'GCInProgress') ORDER BY RANDOM() LIMIT 1;";
				}

				else if ("CoderARevisit".equalsIgnoreCase(role)) {
					query = "SELECT \"text\",\"GeotxtAlternates\",\"PIResult\",\"id_str\",\"rawJSON\",\"created_at\",\"AllCodings\",\"PIContainsUnresolved\",\"PIManualResolution\"   FROM \""
							+ DatabaseConnection.TWEET_TABLE_NAME
							+ "\" WHERE ((\"GCCount\" % 2 = 0) AND \"CoderA\" = '"
							+ geoCoder
							+ "' AND \"GCStatus\" = 'GCInProgress') LIMIT 1;";
				} else if ("CoderBRevisit".equalsIgnoreCase(role)) {
					query = "SELECT \"text\",\"GeotxtAlternates\",\"PIResult\",\"id_str\",\"rawJSON\",\"created_at\",\"AllCodings\",\"PIContainsUnresolved\",\"PIManualResolution\"   FROM \""
							+ DatabaseConnection.TWEET_TABLE_NAME
							+ "\" WHERE ((\"GCCount\" % 2 = 1) AND \"CoderB\" = '"
							+ geoCoder
							+ "' AND \"GCStatus\" = 'GCInProgress') LIMIT 1;";
				}

				DatabaseConnection.connect();
				Statement stmt = DatabaseConnection.c.createStatement();
				ResultSet rs = stmt.executeQuery(query);

				// ALthough we are using rs.next(), but there should be only one
				// result because we used LIMIT 1 in the query above

				if (!rs.isBeforeFirst()) {

					log.info("No documents returned for geocoder " + geoCoder
							+ " at role " + role);

					return "{\"success\":false,\"cause\":\"No documents returned for "
							+ geoCoder
							+ " at role "
							+ role
							+ ". Try another role. \"}";

				}

				while (rs.next()) {

					String tweet = rs.getString(1);

					// log.info(tweet);
					// TODO is this tokenization and retokenization correct? Are
					// we producing the right place names?
					String[] tokens = tweet.split(" ");

					String piResult = rs.getString(3);

					// log.info(piResult);
					String[] places = piResult.split(";");

					JSONArray placeNames = new JSONArray();

					for (String p : places) {
						if (!p.trim().isEmpty()) {
							String[] parts = p.split("-");
							int p1 = Integer.parseInt(parts[0]);
							int p2 = Integer.parseInt(parts[1]);

							// String name = "";
							// for (int i = p1; i <= p2; i++) {
							// name += tokens[i - 1] + " ";
							// }

							String name = tweet.substring(p1 - 1, p2);
							placeNames.add(name);
							// log.info(name);

						}

						JSONObject jsonObj = new JSONObject();

						JSONParser parser = new JSONParser();

						Object rawFullTweetJsonObj = parser.parse(rs
								.getString(5));
						JSONObject rawJsonJson = (JSONObject) rawFullTweetJsonObj;
						JSONObject userJson = (JSONObject) rawJsonJson
								.get("user");
						String profileLoc = (String) userJson.get("location");
						String screenName = (String) userJson
								.get("screen_name");
						String userName = (String) userJson.get("name");
						String userId = (String) userJson.get("id_str");
						String description = (String) userJson
								.get("description");

						JSONArray inputTexts = new JSONArray();
						inputTexts.add(profileLoc);
						inputTexts.add(rs.getString(1));
						jsonObj.put("inputTexts", inputTexts);

						String createdAtString = rs.getString(6);
						JSONObject metaDataJson = new JSONObject();
						metaDataJson.put("createdAt", createdAtString);
						metaDataJson.put("screenName", screenName);
						metaDataJson.put("name", userName);
						metaDataJson.put("userId", userId);
						metaDataJson.put("description", description);

						JSONArray geoTxtRespArray = new JSONArray();
						JSONObject geotxtRespJson = (JSONObject) parser
								.parse(rs.getString(2));
						geoTxtRespArray.add(new JSONObject()); // add an empty
																// JSON
						geoTxtRespArray.add(geotxtRespJson);
						jsonObj.put("generatedAlternates", geoTxtRespArray);

						if (rs.getString(7) != null) {
							Object allCodingsObj = parser
									.parse(rs.getString(7));
							JSONArray allCodingsJsonArray = (JSONArray) allCodingsObj;
							jsonObj.put("allCodings", allCodingsJsonArray);
						} else {
							jsonObj.put("allCodings", new JSONArray());
						}
						//PIContainsUnresolved
						if (rs.getString(8) != null && rs.getString(8).equalsIgnoreCase("yes")) {
							metaDataJson.put("resolutionType", "PIContainsUnresolved");
						} 
						//PIManualResolution
						if (rs.getString(9) != null && rs.getString(9).equalsIgnoreCase("yes")) {
							metaDataJson.put("resolutionType", "PIManualResolution");
						} 
						
						jsonObj.put("metaData", metaDataJson);

						
						// JSONObject userJson = (JSONObject)
						// rawJsonJson.get("user");
						// String profileLoc = (String)
						// userJson.get("location");

						jsonObj.put("geoCoder", geoCoder);

						jsonObj.put("role", role);

						// jsonObj.put("stage", role);

						jsonObj.put("identifiedPlaces", placeNames);

						jsonObj.put("documentId", rs.getString(4));

						packagedResponse = jsonObj.toJSONString();

						// log.info(packagedResponse);
					}
				}
				rs.close();
				stmt.close();
				// TODO: do I need this commit? We are not writing anything to
				// the database
				DatabaseConnection.c.commit();
				DatabaseConnection.c.close();

			} catch (SQLException e) {

				// TODO make the exceptions more specialized, get rid of the
				// THROW of the method.
				System.out
						.println("Something went wrong; rollback and terminating");
				DatabaseConnection.c.rollback();
				DatabaseConnection.c.close();
				// e.printStackTrace();
				// System.exit(1);
			} catch (ParseException ex) {
				Logger.getLogger(CorpusExposerApi.class.getName()).log(
						Level.SEVERE, null, ex);
			}

		} else {
			log.info("The \"role\" variable value is not allowable");
		}

		// log.info(packagedResponse);

		return packagedResponse;
	}

	public static void main(String args[]) {
		try {
			CorpusExposerApi.getTweetFromDb("Morteza", "CoderARevisit");
		} catch (SQLException ex) {
			Logger.getLogger(CorpusExposerApi.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}
}
