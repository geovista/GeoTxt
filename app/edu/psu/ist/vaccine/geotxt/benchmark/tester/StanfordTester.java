package edu.psu.ist.vaccine.geotxt.benchmark.tester;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import edu.psu.ist.vaccine.geotxt.benchmark.Place;
import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;
import edu.psu.ist.vaccine.geotxt.benchmark.TestResult;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.StanfordNer;

/**
 * Wrapper class to use Stanford NER in benchmarks
 * @author jow
 *
 */
public class StanfordTester implements Tester {

	StanfordNer st;
	
	public StanfordTester(String pathToStanfordClassifier) {
		 st = new StanfordNer(pathToStanfordClassifier);
	}
	
	@Override
	public TestResult run(ProblemInstance p) {
		TestResult result = new TestResult(p.getPlaces().size());

		System.out.println("Stanford says...");

		ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
		long startS = threadMX.getCurrentThreadUserTime();
		
		NamedEntities doc = st.tagAlltoDoc(p.getText());
	
		long endS = threadMX.getCurrentThreadUserTime();	
		result.setcTime((endS - startS) / 1000000.0);
		
		System.out.println(doc);
		
		result.setPlacesFound(doc.locs.size());

		// compare all locations identified
		
		boolean found = false;
		
		int count = 0;
		for (Place pl : p.getPlaces()) {
			found = false;
			
			for (Location l : doc.locs) {
				if (l.getPositions().contains(pl.getStartIndex())) {
					if (pl.getNameInText().equalsIgnoreCase(l.getName())) {
						result.setLocationsIdentifiedCorrectly(result.getLocationsIdentifiedCorrectly()+1);
						result.getIdentificationStatus()[count] = true;
						result.getLocations()[count] = l;
					} 
					found = true;
					break;
				}
			}
			
			if (!found) result.setLocationsMissed(result.getLocationsMissed()+1);
			count++;
		}
		
		return result;
	}

	@Override
	public boolean supportsDistanceComparison() {
		return false;
	}

	@Override
	public boolean supportsLocationRecognition() {
		return true;
	}

	@Override
	public boolean supportsGeonamesIDs() {
		return false;
	}

	@Override
	public String getName() {
		return "Stanford-NER";
	}
}
