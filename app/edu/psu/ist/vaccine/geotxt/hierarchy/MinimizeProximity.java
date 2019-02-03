package edu.psu.ist.vaccine.geotxt.hierarchy;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MinimizeProximity {

	final static int maxRows = 5;
	public static Logger log = Logger.getLogger(MinimizeProximity.class);

	public static boolean DEBUG = false;

	public static NamedEntities resolveMultiLocationNames(NamedEntities doc, boolean useSolr) throws Exception {

		MultiKeyMap distz = new MultiKeyMap();

		for (int i = 0; i < doc.locs.size(); i++) {
			for (int j = i + 1; j < doc.locs.size(); j++) {
				for (int k = 0; k < doc.locs.get(i).getCandidates().size(); k++) {
					for (int l = 0; l < doc.locs.get(j).getCandidates().size(); l++) {
						if (!distz.containsKey(doc.locs.get(i).getCandidates().get(k).getGeoNameId(), doc.locs.get(j).getCandidates().get(l).getGeoNameId()) && !distz.containsKey(doc.locs.get(j).getCandidates().get(l).getGeoNameId(), doc.locs.get(i).getCandidates().get(k).getGeoNameId())) {
							LocationWrapper lw1 = doc.locs.get(i).getCandidates().get(k);
							LocationWrapper lw2 = doc.locs.get(j).getCandidates().get(l);
							double distance = calculateDistance(lw1.getLatitude(), lw1.getLongitude(), lw2.getLatitude(), lw2.getLongitude());
							distz.put(doc.locs.get(i).getCandidates().get(k).getGeoNameId(), doc.locs.get(j).getCandidates().get(l).getGeoNameId(), distance);
							// TODO NOTE we are putting duplicates in the dictionary.
							distz.put(doc.locs.get(j).getCandidates().get(l).getGeoNameId(), doc.locs.get(i).getCandidates().get(k).getGeoNameId(), distance);
						}
					}
				}

			}
		}

		// System.out.println("Finished calculating distances between pairs");

		// ArrayList<ArrayList<Long>> listOfAllLists = new ArrayList<ArrayList<Long>>();
		LinkedList<ArrayList<Long>> listOfAllLists = new LinkedList<ArrayList<Long>>();
		for (int i = 0; i < doc.locs.size(); i++) {
			listOfAllLists.add(new ArrayList<Long>());
			for (int j = 0; j < doc.locs.get(i).getCandidates().size(); j++) {
				listOfAllLists.get(i).add(doc.locs.get(i).getCandidates().get(j).getGeoNameId());
			}
		}
		ArrayList<ArrayList<Long>> permutations = new ArrayList<ArrayList<Long>>();
		// GeneratePermutations(listOfAllLists, permutations, 0, new ArrayList<Long>());

		permutations = GeneratePermutationsList(listOfAllLists);

		// System.out.println("Finished generating permutations");

		// TODO TreeMap sorts every time at insertion. Maybe use a regular map and then sort only once, maybe affects efficiency.
		TreeMap<Double, ArrayList<Long>> footPrint = new TreeMap<Double, ArrayList<Long>>();

		// TODO this seems to be very expensive, something in the loop. Maybe getting info from the multikeymap?
		for (ArrayList<Long> permutation : permutations) {

			double dist = 0;
			int count = 0;
			double totalDist = 0;

			for (int i = 0; i < permutation.size(); i++) {
				for (int j = i + 1; j < permutation.size(); j++) {
					dist = (double) distz.get(permutation.get(i), permutation.get(j));
					totalDist += dist;
					count++;
				}
			}
			// calculate average distance.
			footPrint.put(totalDist / count, permutation);
		}

		// System.out.println("Finished populating the footprint TreeMap");

		for (int i = 0; i < doc.locs.size(); i++) {

			Location l = doc.locs.get(i);

			for (LocationWrapper lw1 : doc.locs.get(i).getCandidates()) {
				// TODO make sure first entry is not null or emply.
				if (lw1.getGeoNameId() != null && footPrint.firstEntry() != null && lw1.getGeoNameId() == footPrint.firstEntry().getValue().get(i)) {
					l.setGeometry(new PointGeometry(lw1.getName(), lw1.getLongitude(), lw1.getLatitude(), BigInteger.valueOf(lw1.getGeoNameId())));
					l.setHierarchy(lw1.getHierarchy());
					l.setCountryCode(lw1.getCountryCode());
				}
			}
		}

		return doc;
	}

	// Replaced with the below function, since it was very expensive. Recursive function to generate all permutations of candidates.
	private static void GeneratePermutations(ArrayList<ArrayList<Long>> listOfAllLists, ArrayList<ArrayList<Long>> permutations, int depth, ArrayList<Long> current) {
		if (depth == listOfAllLists.size()) {
			permutations.add(current);
			return;
		}

		for (int i = 0; i < listOfAllLists.get(depth).size(); ++i) {
			ArrayList<Long> temp = cloneList(current);
			temp.add(listOfAllLists.get(depth).get(i));
			GeneratePermutations(listOfAllLists, permutations, depth + 1, temp);
		}
	}

	public static ArrayList<ArrayList<Long>> GeneratePermutationsList(LinkedList<ArrayList<Long>> listsOfAllLists) {

		ArrayList<ArrayList<Long>> combinations = new ArrayList<ArrayList<Long>>();
		ArrayList<ArrayList<Long>> newCombinations;

		for (Long l : listsOfAllLists.removeFirst()) {
			combinations.add(new ArrayList<Long>(Arrays.asList(l)));
		}

		while (!listsOfAllLists.isEmpty()) {
			ArrayList<Long> next = listsOfAllLists.removeFirst();
			newCombinations = new ArrayList<ArrayList<Long>>();
			for (ArrayList<Long> c : combinations) {
				for (Long n : next) {
					ArrayList<Long> copy = (ArrayList<Long>) c.clone();
					copy.add(n);
					newCombinations.add(copy);
				}
			}
			combinations = newCombinations;
		}
		return combinations;
	}

	// TODO move to a util class, and make it work on our candidates instead of lat long
	public static float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000; // meters
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		float dist = (float) (earthRadius * c);

		return dist;
	}

	public static ArrayList<Long> cloneList(ArrayList<Long> list) {
		ArrayList<Long> clone = new ArrayList<Long>(list.size());
		for (Long item : list) {
			clone.add(item);
		}
		return clone;
	}
}
