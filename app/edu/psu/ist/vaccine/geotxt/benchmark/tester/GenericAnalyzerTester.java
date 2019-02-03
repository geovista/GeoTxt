package edu.psu.ist.vaccine.geotxt.benchmark.tester;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;

import edu.psu.ist.vaccine.geotxt.benchmark.Place;
import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;
import edu.psu.ist.vaccine.geotxt.benchmark.TestResult;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.utils.Analyzer;

/**
 * Wrapper class to run benchmark with any Analyzer class
 * @author jow
 *
 */
public class GenericAnalyzerTester implements Tester {

	protected String name;
	protected Analyzer analyzer;
	protected boolean supportsDistanceComparison;
	protected boolean supportsLocationRecognition;
	protected boolean supportsGeonamesIDs;
	
	
	public GenericAnalyzerTester(Analyzer analyzer, String name, boolean supportsDistanceComparison,
			boolean supportsLocationRecognition, boolean supportsGeonamesIDs) {
		this.analyzer = analyzer;
		this.name = name;
		this.supportsDistanceComparison = supportsDistanceComparison;
		this.supportsLocationRecognition = supportsLocationRecognition;
		this.supportsGeonamesIDs = supportsGeonamesIDs;
	}
	
	@Override
	public TestResult run(ProblemInstance p) {
		TestResult result = new TestResult(p.getPlaces().size());


		ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
		long startS = threadMX.getCurrentThreadUserTime();
		
		NamedEntities doc = null;
		
		try {
			doc = analyzer.analyze(p.getText(), NerEngines.STANFORD, new HashMap<String, Object>());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
			
		long endS = threadMX.getCurrentThreadUserTime();	
		result.setcTime((endS - startS) / 1000000.0);
		
		result.setPlacesFound(doc.locs.size());
		
		System.out.println("doc: "+doc);
		
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
		return supportsDistanceComparison;
	}

	@Override
	public boolean supportsLocationRecognition() {
		return supportsLocationRecognition;
	}

	@Override
	public boolean supportsGeonamesIDs() {
		return supportsGeonamesIDs;
	}

	@Override
	public String getName() {
		return name;
	}

}
