package edu.psu.ist.vaccine.geotxt.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.geonames.InsufficientStyleException;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import edu.psu.ist.vaccine.geotxt.hierarchy.MapHierarchyPlaces;

public class GeocodingUtils {

	/** Comparator for sorting toponyms by decreasing population size **/
	private  static class ToponymPopulationComparator implements Comparator<LocationWrapper> {

		public int compare(LocationWrapper x, LocationWrapper y) {
			double px = 0, py = 0;

	
				px = x.getPopulation();
			
				py = y.getPopulation();

			if (px < py) return 1;
			else if (px > py) return -1;

			return 0;
		}
	}


	
	/** Queries Geonames to get up to MAX_ROWS toponyms for name */
	public static List<LocationWrapper> getToponymsFromGeonames(String name, int MAX_ROWS) {

		ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		searchCriteria.setMaxRows(MAX_ROWS);
		searchCriteria.setQ(name);
		searchCriteria.setStyle(Style.FULL);
		WebService.setUserName(MapHierarchyPlaces.username);

		ArrayList<LocationWrapper> matches = new ArrayList<LocationWrapper>();
		
		ToponymSearchResult searchResult = null;
		try {
			searchResult = WebService.search(searchCriteria);
	
			for (int i = 0; i < searchResult.getToponyms().size(); i++) {
				matches.add(new LocationWrapperGeonamesToponym(searchResult.getToponyms().get(i)));
			}
		} catch(Exception e) {
			e.printStackTrace(); 
		}

		return matches;
	}

	/**  determines the NUM_BEST_MATCHES best results from those returned by geonames based on how well the names match and 
	 *   population size 
	 */
	public static List<LocationWrapper> getBestCandidates(String name,List<LocationWrapper> searchResult, int NUM_BEST_MATCHES) {

		/* best candidates from geonames entities whose name matches exactly */
		PriorityQueue<LocationWrapper> bestExactMatchingToponyms = new PriorityQueue<LocationWrapper>(NUM_BEST_MATCHES, new ToponymPopulationComparator() );

		/* best candidates from geonames entities who have an alternative name that matches exactly */
		PriorityQueue<LocationWrapper> bestExactMatchingAlternativeNamesToponyms = new PriorityQueue<LocationWrapper>(NUM_BEST_MATCHES, new ToponymPopulationComparator() );

		/* best candidates from geonames entities that dont fall into the first two categories */
		PriorityQueue<LocationWrapper> bestOtherToponyms = new PriorityQueue<LocationWrapper>(NUM_BEST_MATCHES, new ToponymPopulationComparator() );

		ArrayList<LocationWrapper> best = new ArrayList<LocationWrapper>(NUM_BEST_MATCHES);

		if (searchResult != null) {
			for (LocationWrapper t : searchResult) { // go through all query results
				
				if (t.getName().equalsIgnoreCase(name)) { // first category?

					bestExactMatchingToponyms.offer(t);    

				} else if (bestExactMatchingToponyms.size() < NUM_BEST_MATCHES) {
					String[] names = null;
		
						if (t.getAlternateNames() != null) {
							names = t.getAlternateNames();
						}
		

					boolean found = false;
					if (names != null) {
						for (String n : names) {
							if (n.equalsIgnoreCase(name)) { // second category?
								bestExactMatchingAlternativeNamesToponyms.offer(t);
								found = true;
								break;
							}
						}
					}

					if (!found && (bestExactMatchingToponyms.size() + bestExactMatchingAlternativeNamesToponyms.size() < NUM_BEST_MATCHES)) { // third category?
						bestOtherToponyms.offer(t);
					}
				}
			}

			// if we have multiple place names, draw from the categories get the best candidates to use in the hierarchical approach

			while (!bestExactMatchingToponyms.isEmpty() && best.size() < NUM_BEST_MATCHES) {
				best.add(bestExactMatchingToponyms.poll());
			}
			while (!bestExactMatchingAlternativeNamesToponyms.isEmpty() && best.size() < NUM_BEST_MATCHES) {
				best.add(bestExactMatchingAlternativeNamesToponyms.poll());
			}
			while (!bestOtherToponyms.isEmpty() && best.size() < NUM_BEST_MATCHES) {
				best.add(bestOtherToponyms.poll());
			}
		}

		return best;
	}
}
