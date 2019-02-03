package edu.psu.ist.vaccine.geotxt.benchmark.tester;

import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;
import edu.psu.ist.vaccine.geotxt.benchmark.TestResult;

/**
 * Interface for wrapper classes for applying and comparing different NER / geocoding approaches
 * in a benchmark
 * @author jow
 *
 */
public interface Tester {
	/**
	 * 
	 * @param p problem instance that the approach should be applied to
	 * @return result describing the performance of the approach for the instance
	 */
	public TestResult run(ProblemInstance p);
	
	
	/**
	 * yields true if approach allows for a comparison of distance to the
	 * ground truth coordinates in which case each place in the result returned by run(...)
	 * has to contain lon and lat coordinates (pure NER approaches typically don't but
	 * geocoding approaches do). 
	 * @return
	 */
	public boolean supportsDistanceComparison();
	
	/**
	 * yields true if approach allows for a comparison of locations in the
	 * problem instance
	 * @return
	 */
	public boolean supportsLocationRecognition();
	
	/**
	 * yields true if approach allows for a comparison of geonames ID in the
	 * @return
	 */
	public boolean supportsGeonamesIDs();
	
	public String getName();
}
