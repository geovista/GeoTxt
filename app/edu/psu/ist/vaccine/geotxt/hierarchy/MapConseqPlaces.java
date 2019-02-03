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

public class MapConseqPlaces {

	final static int maxRows = 5;
	public static String username = "siddhartha";
	public static Logger log = Logger.getLogger(MapConseqPlaces.class);

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
			if (DEBUG)
				System.out.println("hypotheses for " + doc.locs.get(i).getName());

			setCandidateToponymHierarchies(doc.locs.get(i).getCandidates(), useSolr);

			if (DEBUG) {
				for (int j = 0; j < doc.locs.get(i).getCandidates().size(); j++) {
					System.out.println(doc.locs.get(i).getCandidates().get(j).getName() + "-" + doc.locs.get(i).getCandidates().get(j).getGeoNameId() + " (score:" + doc.locs.get(i).getCandidates().get(j).getScore() + ")");
				}
			}
		}

		List<LocationWrapper> lwl1 = null;
		List<LocationWrapper> lwl2 = null;

		for (int clwl1 = 0; clwl1 < doc.locs.size(); clwl1++) {
			lwl1 = doc.locs.get(clwl1).getCandidates();

			if (DEBUG)
				System.out.println(clwl1);

			int bestDepth = depths[clwl1];
			int bestRelDepth = relDepths[clwl1];
			int bestRank = ranks[clwl1];

			for (int clwl2 = 0; clwl2 < doc.locs.size(); clwl2++) {
				//TODO this check should happen probably before anything else, to make sure extra operations are done only if this relationship exists.
				//This is the core difference of this class. Only look at place, place or place place patterns, for the first occurance.
				if (doc.locs.get(clwl2).getPositions().get(0) == doc.locs.get(clwl1).getPositions().get(0) + doc.locs.get(clwl1).getName().length() + 2 
						|| doc.locs.get(clwl2).getPositions().get(0) == doc.locs.get(clwl1).getPositions().get(0) + doc.locs.get(clwl1).getName().length() + 1) {

					lwl2 = doc.locs.get(clwl2).getCandidates();

					if (lwl1 != lwl2) {

						int tcount = 0;
						for (LocationWrapper lw1 : lwl1) {

							if (DEBUG)
								System.out.println("lw1: >" + lw1.getName() + "-" + lw1.getGeoNameId() + "<");
							List<LocationWrapper> hierarchy = lw1.getHierarchy();

							boolean found = false;

							int depth = hierarchy.size();
							int relDepth = 1;

							for (LocationWrapper hw : hierarchy) {
								if (bestDepth != -1 && (bestRank < tcount || (bestRank == tcount && relDepth > bestRelDepth || (relDepth == bestRelDepth && depth <= bestDepth))))
									break;

								if (DEBUG)
									System.out.println("hw: " + hw.getName() + "-" + hw.getGeoNameId());

								int tcount2 = 0;
								for (LocationWrapper lw2 : lwl2) {

									if (DEBUG)
										System.out.println("lw2: >" + lw2.getName() + "-" + lw2.getGeoNameId() + "<");

									if (lw2.getGeoNameId().equals(hw.getGeoNameId())) {

										Location l = doc.locs.get(clwl1);

										l.setGeometry(new PointGeometry(lw1.getName(), lw1.getLongitude(), lw1.getLatitude(), BigInteger.valueOf(lw1.getGeoNameId())));
										l.setHierarchy(lw1.getHierarchy());
										l.setCountryCode(lw1.getCountryCode());
										log.info(lw1.getGeoNameId());
										if (DEBUG)
											System.out.println(">>>" + lw1.getName() + "-" + lw1.getGeoNameId() + "(" + doc.locs.get(clwl1).getName() + ")" + "->" + hw.getName() + "-" + hw.getGeoNameId() + "," + lw2.getName() + "-" + lw2.getGeoNameId());
										if (DEBUG)
											System.out.println(lw1.getLatitude() + "," + lw1.getLongitude());
										if (DEBUG)
											System.out.println("depth: " + depth + ", rank: " + tcount + ", reldepth: " + relDepth);
										depths[clwl1] = depth;
										ranks[clwl1] = tcount;
										relDepths[clwl1] = relDepth;
										bestDepth = depth;
										bestRank = tcount;
										bestRelDepth = relDepth;

										Location l2 = doc.locs.get(clwl2);
										l2.setGeometry(new PointGeometry(lw2.getName(), lw2.getLongitude(), lw2.getLatitude(), BigInteger.valueOf(lw2.getGeoNameId())));
										l2.setHierarchy(lw2.getHierarchy());
										l2.setCountryCode(lw2.getCountryCode());
										if (DEBUG)
											System.out.println(">>>" + lw2.getName() + "-" + lw2.getGeoNameId() + "(" + doc.locs.get(clwl2).getName() + ")" + "->");
										depths[clwl2] = depth;
										ranks[clwl2] = tcount2;

										found = true;
										break;
									}
									tcount2++;
								}
								depth--;
								relDepth++;
								if (found)
									break;
							}
							tcount++;
						}
					}
				}
			}

		}

		return doc;
	}

	//this method has a duplicate in MapHierarchyPlaces.java
	public static void setCandidateToponymHierarchies(List<LocationWrapper> bestMatches, boolean useSolr) throws Exception {

		List<List<LocationWrapper>> candidateToponymHierarchies = new ArrayList<List<LocationWrapper>>();

		if (useSolr) {
			for (int c = 0; c < bestMatches.size(); c++) {
				((LocationWrapperSolrDoc) bestMatches.get(c)).setHierarchy(bestMatches.get(c).getGeoNameId());
			}

		} else {
			WebService.setUserName(username);
			for (int c = 0; c < bestMatches.size(); c++) {
				List<LocationWrapper> l = new ArrayList<LocationWrapper>();
				List<Toponym> qResult = WebService.hierarchy((int) (long) (bestMatches.get(c).getGeoNameId()), "en", Style.SHORT);

				for (Toponym t : qResult) {
					l.add(new LocationWrapperGeonamesToponym(t));
				}
				candidateToponymHierarchies.add(l);

				((LocationWrapperGeonamesToponym) bestMatches.get(c)).setHierarchy(l);
			}
		}

	}

}
