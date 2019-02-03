/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.utils;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * 
 * @author MortezaKarimzadeh
 */
public class GeoJsonWriter {

	public static String docToGeoJson(NamedEntities doc, boolean includeAlternates, int maxAlternates,
			boolean includeHierarchy, boolean includeDetails) {

		JSONObject respJson = new JSONObject();

		respJson.put("type", "FeatureCollection");

		JSONArray featuresArray = new JSONArray();

		if (doc == null || (doc.locs.isEmpty() && doc.orgs.isEmpty() && doc.pers.isEmpty())) {
			respJson.put("features", featuresArray);
			return respJson.toJSONString();
		}

		for (Location l : doc.locs) {
			featuresArray.add(
					encodeFeatureFromLocation(l, includeAlternates, maxAlternates, includeHierarchy, includeDetails));
		}

		for (Organization l : doc.orgs) {

			JSONObject featureJson = new JSONObject();

			featureJson.put("type", "Feature");

			JSONObject featurePropJson = new JSONObject();

			featurePropJson.put("type", "organization");

			if (l.getName() != null && !l.getName().equalsIgnoreCase("")) {
				featurePropJson.put("name", l.getName());
			} else {
				featurePropJson.put("name", null);
			}
			if (l.getGeometry() != null) {
				featurePropJson.put("toponym", l.getGeometry().getToponym());
			} else {
				featurePropJson.put("toponym", null);
			}
			if (l.getGeometry() != null) {
				featurePropJson.put("geoNameId", l.getGeometry().getGeoNameId());
			} else {
				featurePropJson.put("geoNameId", null);
			}
			if (l.getOrgType() != null && !l.getOrgType().equalsIgnoreCase("")) {
				featurePropJson.put("orgType", l.getOrgType().toLowerCase());
			} else {
				//featurePropJson.put("orgType", null);
			}
			if (l.getPositions() != null) {
				JSONArray positionsArray = new JSONArray();
				for (int p : l.getPositions()) {
					positionsArray.add(p);
				}
				featurePropJson.put("positions", positionsArray);
			} else {
				featurePropJson.put("positions", null);
			}

			featureJson.put("properties", featurePropJson);

			JSONObject featureGeometryJson = new JSONObject();

			if (l.getGeometry() != null) {

				featureGeometryJson.put("type", "Point");

				JSONArray coordinatesArray = new JSONArray();

				coordinatesArray.add(l.getGeometry().getCoordinates()[0]);
				coordinatesArray.add(l.getGeometry().getCoordinates()[1]);

				featureGeometryJson.put("coordinates", coordinatesArray);

				featureJson.put("geometry", featureGeometryJson);

			} else {
				featureJson.put("geometry", null);
			}

			// TODO: We are not returning organizations at the moment - to help
			// corpus
			// building
			// featuresArray.add(featureJson);

		}

		for (Person l : doc.pers) {

			JSONObject featureJson = new JSONObject();

			featureJson.put("type", "Feature");

			JSONObject featurePropJson = new JSONObject();

			featurePropJson.put("type", "person");

			if (l.getName() != null && !l.getName().equalsIgnoreCase("")) {
				featurePropJson.put("name", l.getName());
			} else {
				featurePropJson.put("name", null);
			}
			if (l.getGender() != null && !l.getGender().equalsIgnoreCase("")) {
				featurePropJson.put("gender", l.getGender().toLowerCase());
			} else {
				featurePropJson.put("gender", null);
			}
			if (l.getKind() != null && !l.getKind().equalsIgnoreCase("")) {
				featurePropJson.put("kind", l.getKind().toLowerCase());
			} else {
				featurePropJson.put("kind", null);
			}
			if (l.getPositions() != null) {
				JSONArray positionsArray = new JSONArray();
				for (int p : l.getPositions()) {
					positionsArray.add(p);
				}
				featurePropJson.put("positions", positionsArray);
			} else {
				featurePropJson.put("positions", null);
			}

			featureJson.put("properties", featurePropJson);

			featureJson.put("geometry", null);

			// TODO: We are not returning people at the moment - to help corpus
			// building
			// featuresArray.add(featureJson);

		}

		respJson.put("features", featuresArray);

		return respJson.toJSONString();

	}

	protected static JSONObject encodeFeatureFromLocation(Location l, boolean includeAlternates, int maxAlternates,
			boolean includeHierarchy, boolean includeDetails) {

		JSONObject featureJson = new JSONObject();

		featureJson.put("type", "Feature");

		JSONObject featurePropJson = new JSONObject();

		featurePropJson.put("type", "location");

		if (l.getName() != null && !l.getName().equalsIgnoreCase("")) {
			featurePropJson.put("name", l.getName());
		} else {
			featurePropJson.put("name", null);
		}
		if (l.getGeometry() != null && l.getGeometry().getToponym() != null) {
			featurePropJson.put("toponym", l.getGeometry().getToponym());
		} else {
			featurePropJson.put("toponym", null);
		}
		if (l.getGeometry() != null && l.getGeometry().getGeoNameId() != null) {
			featurePropJson.put("geoNameId", l.getGeometry().getGeoNameId());
		} else {
			featurePropJson.put("geoNameId", null);
		}
		if (l.getLocType() != null && !l.getLocType().equalsIgnoreCase("")) {
			featurePropJson.put("locationType", l.getLocType().toLowerCase());
		} else {
			//featurePropJson.put("locationType", null);
		}
		if (l.getPositions() != null) {
			JSONArray positionsArray = new JSONArray();
			for (int p : l.getPositions()) {
				positionsArray.add(p);
			}
			featurePropJson.put("positions", positionsArray);
		} else {
			featurePropJson.put("positions", null);
		}

		if (includeDetails) {
			if (l.getCountryCode() != null) {
				featurePropJson.put("countryCode", l.getCountryCode());
			} else {
				featurePropJson.put("countryCode", null);
			}
			if (l.getFeatureClass() != null) {
				featurePropJson.put("featureClass", l.getFeatureClass());
			} else {
				featurePropJson.put("featureClass", null);
			}
			if (l.getFeatureCode() != null) {
				featurePropJson.put("featureCode", l.getFeatureCode());
			} else {
				featurePropJson.put("featureCode", null);
			}
			if (l.getAlternateNames() != null && l.getAlternateNames().length != 0) {
				JSONArray alternateNames = new JSONArray();
				for (String name : l.getAlternateNames()) {
					alternateNames.add(name);
				}
				featurePropJson.put("alternateNames", alternateNames);
			} else {
				featurePropJson.put("alternateNames", null);
			}
		}

		if (includeHierarchy && l.getHierarchy() != null) {

			JSONObject hierarchyJson = new JSONObject();

			hierarchyJson.put("type", "FeatureCollection");

			JSONArray hierarchyFeaturesArray = new JSONArray();

			int index = 0;
			while (index < l.getHierarchy().size()) {
				LocationWrapper cl = l.getHierarchy().get(index);
				hierarchyFeaturesArray.add(encodeFeatureFromLocationWrapper(l, cl, false, false));
				index++;
			}

			hierarchyJson.put("features", hierarchyFeaturesArray);

			featurePropJson.put("hierarchy", hierarchyJson);

		} else {

			featurePropJson.put("hierarchy", null);
		}

		if (includeAlternates && l.getCandidates() != null) {

			JSONObject alternatesJson = new JSONObject();

			alternatesJson.put("type", "FeatureCollection");

			JSONArray alternatesFeaturesArray = new JSONArray();

			int count = 0;
			int index = 0;

			alternatesFeaturesArray.add(encodeFeatureFromGeometry(l, l.getName(), includeHierarchy, includeDetails));

			count = 1;
			while (count < maxAlternates && index < l.getCandidates().size()) {
				LocationWrapper lw = l.getCandidates().get(index);
				if (!(l.getGeometry().getGeoNameId().toString().equals(lw.getGeoNameId().toString()))) {
					alternatesFeaturesArray
							.add(encodeFeatureFromLocationWrapper(l, lw, includeHierarchy, includeDetails));
					count++;
				}
				index++;
			}
			alternatesJson.put("features", alternatesFeaturesArray);
			featurePropJson.put("alternates", alternatesJson);
		} else {
			featurePropJson.put("alternates", null);
		}

		featureJson.put("properties", featurePropJson);

		JSONObject featureGeometryJson = new JSONObject();

		// if (l.getGeometry() != null) (originally)
		if (l.getGeometry() != null && l.getGeometry().getCoordinates() != null) {

			featureGeometryJson.put("type", "Point");

			JSONArray coordinatesArray = new JSONArray();

			coordinatesArray.add(l.getGeometry().getCoordinates()[0]);
			coordinatesArray.add(l.getGeometry().getCoordinates()[1]);

			featureGeometryJson.put("coordinates", coordinatesArray);

			featureJson.put("geometry", featureGeometryJson);

		} else {
			featureJson.put("geometry", null);
		}

		// featuresArray.add(featureJson);
		// respJson.put("features", featuresArray);
		return featureJson;

	}

	// TODO is there really a need to have two similar methods for encoding
	// features?
	protected static JSONObject encodeFeatureFromLocationWrapper(Location l, LocationWrapper lw,
			boolean includeHierarchy, boolean includeDetails) {

		JSONObject featureJson = new JSONObject();

		featureJson.put("type", "Feature");

		JSONObject featurePropJson = new JSONObject();

		featurePropJson.put("type", "location");

		if (l.getName() != null && !l.getName().equalsIgnoreCase("")) {
			featurePropJson.put("name", l.getName());
		} else {
			featurePropJson.put("name", null);
		}
		if (lw.getName() != null) {
			featurePropJson.put("toponym", lw.getName());
		} else {
			featurePropJson.put("toponym", null);
		}
		if (lw.getGeoNameId() != null) {
			featurePropJson.put("geoNameId", lw.getGeoNameId());
		} else {
			featurePropJson.put("geoNameId", null);
		}

		if (l.getPositions() != null) {
			JSONArray positionsArray = new JSONArray();
			for (int p : l.getPositions()) {
				positionsArray.add(p);
			}
			featurePropJson.put("positions", positionsArray);
		} else {
			featurePropJson.put("positions", null);
		}

		// TODO do we need this?
		featurePropJson.put("locationType", null);

		if (includeHierarchy && lw.getHierarchy() != null) {

			JSONObject hierarchyJson = new JSONObject();

			hierarchyJson.put("type", "FeatureCollection");

			JSONArray hierarchyFeaturesArray = new JSONArray();

			int index = 0;

			while (index < lw.getHierarchy().size()) {
				LocationWrapper cl = lw.getHierarchy().get(index);
				hierarchyFeaturesArray.add(encodeFeatureFromLocationWrapper(l, cl, includeHierarchy, false));
				index++;
			}

			// hierarchyJson.put("features", hierarchyFeaturesArray);
			hierarchyJson.put("features", hierarchyFeaturesArray);

			featurePropJson.put("hierarchy", hierarchyJson);

		} else {

			featurePropJson.put("hierarchy", null);
		}

		// added
		if (includeDetails) {
			if (lw.getCountryCode() != null) {
				featurePropJson.put("countryCode", lw.getCountryCode());
			} else {
				featurePropJson.put("countryCode", null);
			}
			if (lw.getFeatureClass() != null) {
				featurePropJson.put("featureClass", lw.getFeatureClass());
			} else {
				featurePropJson.put("featureClass", null);
			}
			if (lw.getFeatureCode() != null) {
				featurePropJson.put("featureCode", lw.getFeatureCode());
			} else {
				featurePropJson.put("featureCode", null);
			}
			if (lw.getAlternateNames() != null && lw.getAlternateNames().length != 0) {
				JSONArray alternateNames = new JSONArray();
				for (String name : lw.getAlternateNames()) {
					alternateNames.add(name);
				}
				featurePropJson.put("alternateNames", alternateNames);
			} else {
				featurePropJson.put("alternateNames", new JSONArray());
			}
		}

		featureJson.put("properties", featurePropJson);

		JSONObject featureGeometryJson = new JSONObject();

		if (lw.getLongitude() != null && lw.getLatitude() != null) {

			featureGeometryJson.put("type", "Point");

			JSONArray coordinatesArray = new JSONArray();

			coordinatesArray.add(lw.getLongitude());
			coordinatesArray.add(lw.getLatitude());

			featureGeometryJson.put("coordinates", coordinatesArray);

			featureJson.put("geometry", featureGeometryJson);

		} else {
			featureJson.put("geometry", null);
		}

		// featuresArray.add(featureJson);
		// respJson.put("features", featuresArray);
		return featureJson;

	}

	public static JSONObject encodeFeatureFromGeometry(Location l, String queryText, boolean includeHierarchy,
			boolean includeDetails) {

		JSONObject featureJson = new JSONObject();

		featureJson.put("type", "Feature");

		JSONObject featurePropJson = new JSONObject();

		featurePropJson.put("type", "location");

		if (queryText != null) {
			featurePropJson.put("name", queryText);
		} else {
			featurePropJson.put("name", null);
		}
		if (l.getGeometry() != null && l.getGeometry().getToponym() != null) {
			featurePropJson.put("toponym", l.getGeometry().getToponym());
		} else {
			featurePropJson.put("toponym", null);
		}
		if (l.getGeometry() != null && l.getGeometry().getGeoNameId() != null) {
			featurePropJson.put("geoNameId", l.getGeometry().getGeoNameId());
		} else {
			featurePropJson.put("geoNameId", null);
		}
		if (l.getPositions() != null) {
			JSONArray positionsArray = new JSONArray();
			for (int p : l.getPositions()) {
				positionsArray.add(p);
			}
			featurePropJson.put("positions", positionsArray);
		} else {
			featurePropJson.put("positions", null);
		}

		if (includeHierarchy && l.getHierarchy() != null) {

			JSONObject hierarchyJson = new JSONObject();

			hierarchyJson.put("type", "FeatureCollection");

			JSONArray hierarchyFeaturesArray = new JSONArray();

			int index = 0;
			while (index < l.getHierarchy().size()) {
				LocationWrapper cl = l.getHierarchy().get(index);
				hierarchyFeaturesArray.add(encodeFeatureFromLocationWrapper(l, cl, false, false));
				index++;
			}

			// hierarchyJson.put("features", hierarchyFeaturesArray);
			hierarchyJson.put("features", hierarchyFeaturesArray);

			featurePropJson.put("hierarchy", hierarchyJson);

		} else {

			featurePropJson.put("hierarchy", null);
		}

		// added
		if (includeDetails) {
			if (l.getCountryCode() != null) {
				featurePropJson.put("countryCode", l.getCountryCode());
			} else {
				featurePropJson.put("countryCode", null);
			}
			if (l.getFeatureClass() != null) {
				featurePropJson.put("featureClass", l.getFeatureClass());
			} else {
				featurePropJson.put("featureClass", null);
			}
			if (l.getFeatureCode() != null) {
				featurePropJson.put("featureCode", l.getFeatureCode());
			} else {
				featurePropJson.put("featureCode", null);
			}
		}

		featureJson.put("properties", featurePropJson);

		JSONObject featureGeometryJson = new JSONObject();

		if (l.getGeometry() != null && l.getGeometry().getCoordinates() != null) {

			featureGeometryJson.put("type", "Point");

			JSONArray coordinatesArray = new JSONArray();

			coordinatesArray.add(l.getGeometry().getCoordinates()[0]);
			coordinatesArray.add(l.getGeometry().getCoordinates()[1]);

			featureGeometryJson.put("coordinates", coordinatesArray);

			featureJson.put("geometry", featureGeometryJson);

		} else {
			featureJson.put("geometry", null);
		}

		// featuresArray.add(featureJson);
		// respJson.put("features", featuresArray);
		return featureJson;

	}

	public static String pointGeometryToGeoJson(PointGeometry p, String queryText) {

		JSONObject respJson = new JSONObject();

		respJson.put("type", "FeatureCollection");

		JSONArray featuresArray = new JSONArray();

		if (p == null) {
			respJson.put("features", featuresArray);
			return respJson.toJSONString();
		}

		JSONObject featureJson = new JSONObject();

		featureJson.put("type", "Feature");

		JSONObject featurePropJson = new JSONObject();

		featurePropJson.put("type", "location");

		if (queryText != null) {
			featurePropJson.put("name", queryText);
		} else {
			featurePropJson.put("name", null);
		}
		if (p.getToponym() != null) {
			featurePropJson.put("toponym", p.getToponym());
		} else {
			featurePropJson.put("toponym", null);
		}
		if (p.getGeoNameId() != null) {
			featurePropJson.put("geoNameId", p.getGeoNameId());
		} else {
			featurePropJson.put("geoNameId", null);
		}

		featureJson.put("properties", featurePropJson);

		JSONObject featureGeometryJson = new JSONObject();

		if (p.getCoordinates() != null) {

			featureGeometryJson.put("type", "Point");

			JSONArray coordinatesArray = new JSONArray();

			coordinatesArray.add(p.getCoordinates()[0]);
			coordinatesArray.add(p.getCoordinates()[1]);

			featureGeometryJson.put("coordinates", coordinatesArray);

			featureJson.put("geometry", featureGeometryJson);

		} else {
			featureJson.put("geometry", null);
		}

		featuresArray.add(featureJson);

		respJson.put("features", featuresArray);

		return respJson.toJSONString();

	}
}
