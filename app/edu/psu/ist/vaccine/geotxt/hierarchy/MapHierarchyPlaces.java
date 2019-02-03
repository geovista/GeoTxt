package edu.psu.ist.vaccine.geotxt.hierarchy;

import org.apache.log4j.Logger;
import org.geonames.Toponym;
import org.geonames.WebService;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapper;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapperGeonamesToponym;
import edu.psu.ist.vaccine.geotxt.utils.LocationWrapperSolrDoc;
import edu.psu.ist.vaccine.geotxt.utils.PointGeometry;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.geonames.Style;

public class MapHierarchyPlaces {

	final static int maxRows = 5;
	public static String username = "siddhartha";
	public static Logger log = Logger.getLogger(MapHierarchyPlaces.class);

	public static boolean DEBUG = false;

	public static NamedEntities resolveMultiLocationNames(NamedEntities doc, boolean useSolr) throws Exception {
		
		int[] depths = new int[doc.locs.size()]; // keeps track at which level a supporting hierarchy relationship has been found
		int[] relDepths = new int[doc.locs.size()]; // keeps track at which level a supporting hierarchy relationship has been found
		int[] ranks = new int[doc.locs.size()]; // keeps track at which rank we currently have a supporting hierarchy relationship
		
		for (int i = 0; i < doc.locs.size(); i++) {
			depths[i] = -1;
			relDepths[i] = -1;
			ranks[i] = -1;
		}
		
		for (int i = 0; i < doc.locs.size(); i++) {
			if (DEBUG) System.out.println("hypotheses for "+doc.locs.get(i).getName());

			setCandidateToponymHierarchies(doc.locs.get(i).getCandidates(),useSolr);

			if (DEBUG) {
				for (int j = 0; j < doc.locs.get(i).getCandidates().size(); j++) {
					System.out.println(doc.locs.get(i).getCandidates().get(j).getName()+"-"+doc.locs.get(i).getCandidates().get(j).getGeoNameId()+" (score:"+doc.locs.get(i).getCandidates().get(j).getScore()+")");
				}
			}
		}
		

		List<LocationWrapper> lwl1 = null;
		List<LocationWrapper> lwl2 = null;
		
		int count = 0; //TODO not needed anymore can be replaced by clwl1
		for (int clwl1 = 0; clwl1 < doc.locs.size(); clwl1++) {
			lwl1 = doc.locs.get(clwl1).getCandidates();
				
			if (DEBUG)  System.out.println(count);

			int bestDepth = depths[count];
			int bestRelDepth = relDepths[count];
			int bestRank = ranks[count];
			
			int count2 = 0; //TODO not needed anymore can be replaced by clwl2
			for (int clwl2 = 0; clwl2 < doc.locs.size(); clwl2++) {
				lwl2 = doc.locs.get(clwl2).getCandidates();
		
				if (lwl1 != lwl2) {
				
					int bestDepth2 = depths[count2];	
					int bestRank2 = depths[count2];
					int bestRelDepth2 = relDepths[count2];
				
					int tcount = 0;
					for (LocationWrapper lw1 : lwl1) {
						
						if (DEBUG) System.out.println("lw1: >"+lw1.getName()+"-"+lw1.getGeoNameId()+"<");
						List<LocationWrapper> hierarchy = lw1.getHierarchy();

						boolean found = false;

						int depth = hierarchy.size();
						int relDepth = 1;
						
						for (LocationWrapper hw : hierarchy) {
							if ( bestDepth != -1 && (bestRank < tcount || (bestRank == tcount && relDepth > bestRelDepth || (relDepth == bestRelDepth && depth <= bestDepth )))) break;

							if (DEBUG) System.out.println("hw: "+hw.getName()+"-"+hw.getGeoNameId());

							int tcount2 = 0;
							for (LocationWrapper lw2 : lwl2) {
								
								if (DEBUG)  System.out.println("lw2: >"+lw2.getName()+"-"+lw2.getGeoNameId()+"<");

								if (lw2.getGeoNameId().equals(hw.getGeoNameId())) {

									Location l = doc.locs.get(count);

									l.setGeometry(new PointGeometry(lw1.getName(), lw1.getLongitude(), lw1.getLatitude(), BigInteger.valueOf(lw1.getGeoNameId())));
									l.setHierarchy(lw1.getHierarchy());
									l.setCountryCode(lw1.getCountryCode());
									log.info(lw1.getGeoNameId());
									if (DEBUG)  System.out.println(">>>"+lw1.getName()+"-"+lw1.getGeoNameId() +"("+ doc.locs.get(count).getName() +")"+"->" + hw.getName()+"-"+hw.getGeoNameId() + "," + lw2.getName()+"-"+lw2.getGeoNameId() );
									if (DEBUG)  System.out.println(lw1.getLatitude()+","+lw1.getLongitude());
									if (DEBUG) System.out.println("depth: "+depth+", rank: "+tcount+", reldepth: "+relDepth);
									depths[count] = depth;
									ranks[count] = tcount;
									relDepths[count] = relDepth;
									bestDepth = depth;
									bestRank = tcount;
									bestRelDepth = relDepth;
									
									Location l2 = doc.locs.get(count2);
									l2.setGeometry(new PointGeometry(lw2.getName(), lw2.getLongitude(), lw2.getLatitude(), BigInteger.valueOf(lw2.getGeoNameId())));
									l2.setHierarchy(lw2.getHierarchy());
									l2.setCountryCode(lw2.getCountryCode());
									if (DEBUG)  System.out.println(">>>"+lw2.getName()+"-"+lw2.getGeoNameId() +"("+ doc.locs.get(count2).getName() +")"+"->" );
									depths[count2] = depth;
									ranks[count2] = tcount2;
									bestRelDepth2 = relDepth;
									
									found = true;
									break;
								}
								tcount2++;
							}
							depth--;
							relDepth++;
							if (found) break;
						}
						tcount++;
					}
				}
				count2++;
			}

			count++;
		}


		return doc;
	}
	
	//this method has a duplicate in MapConseqPlaces.java
	public static void setCandidateToponymHierarchies(List<LocationWrapper> bestMatches, boolean useSolr) throws Exception {

		List<List<LocationWrapper>> candidateToponymHierarchies = new ArrayList<List<LocationWrapper>>();


		if (useSolr) {
			for (int c = 0; c < bestMatches.size(); c++) {
				((LocationWrapperSolrDoc)bestMatches.get(c)).setHierarchy(bestMatches.get(c).getGeoNameId());
			}

		} else {
			WebService.setUserName(username);
			for (int c = 0; c < bestMatches.size(); c++) {
				List<LocationWrapper> l = new ArrayList<LocationWrapper>();
				List<Toponym> qResult = WebService.hierarchy((int)(long)(bestMatches.get(c).getGeoNameId()), "en", Style.SHORT);

				for (Toponym t : qResult) {
					l.add(new LocationWrapperGeonamesToponym(t));
				}
				candidateToponymHierarchies.add(l);

				((LocationWrapperGeonamesToponym)bestMatches.get(c)).setHierarchy(l);
			}
		}
		//    	
		//    	
		//    	
		//    	
		//        //Create a Map object with Toponyms as keys and their respective hierarchy toponyms for each as value.
		//        for (int c = 0; c < bestMatches.size(); c++) {
		//            candidateToponymHierarchies.put(bestMatches.get(c), WebService.hierarchy(bestMatches.get(c).getGeoNameId(), "en", Style.SHORT));
		//        }
		//
		//        return candidateToponymHierarchies;
	}

	// the following method is now obsolete
	//GeoCodes location names using the new GeoNames WS API. Fetches topmost maxRows results, and assigns the one with the most population. 
	/* public static PointGeometry geoCode(String locationName) throws Exception {

        WebService.setUserName(username);
        Toponym toponym = null;
      //  Long[] pop = new Long[maxRows];
        Long maxPop = Long.MIN_VALUE;
        int maxPopIndex = -1;
        Long maxPopGoodFit = Long.MIN_VALUE;
        int maxPopGoodFitIndex = -1;

        ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
        searchCriteria.setMaxRows(maxRows);
        searchCriteria.setQ(locationName);
        searchCriteria.setStyle(Style.FULL);
        ToponymSearchResult searchResult = WebService.search(searchCriteria);

        if (searchResult.getToponyms().size() <= 1) {
        	System.out.println("WARNING: geonames did not return any results!");
        }

        for (int c = 0; c < searchResult.getToponyms().size(); c++) {
            toponym = searchResult.getToponyms().get(c);

            Long pop = toponym.getPopulation() != null ? toponym.getPopulation() : 0; // may cause problems with entries that dont have a population specified

            boolean found = false;
            if (toponym.getName().equalsIgnoreCase(locationName)) found = true;
            if (!found) {
            	String[] names = toponym.getAlternateNames().split(",");
            	for (String n : names) {
            		if (n.equalsIgnoreCase(locationName)) {
            			found = true;
            			break;
            		}
            	}

            }
            if (found) { 
            	if (pop > maxPopGoodFit) {
                	maxPopGoodFit = pop;
                    maxPopGoodFitIndex = c;
                }
            } else {         
            	if (pop > maxPop) {
            		maxPop = pop;
            		maxPopIndex = c;
            	}
            }

            System.out.println("toponym for "+locationName+":"+toponym+" pop="+toponym.getPopulation());
        }

        if (maxPopGoodFitIndex >= 0) {
        	toponym = searchResult.getToponyms().get(maxPopGoodFitIndex);
        } else if (maxPopIndex >= 0) {
        	toponym = searchResult.getToponyms().get(maxPopIndex);
        } else {
        	return null;
        }

        PointGeometry point = new PointGeometry(toponym.getName(), toponym.getLongitude(), toponym.getLatitude(), BigInteger.valueOf(toponym.getGeoNameId()));

        return point;
    }*/
}
