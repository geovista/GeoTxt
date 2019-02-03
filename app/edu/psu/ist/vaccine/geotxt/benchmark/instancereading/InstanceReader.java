package edu.psu.ist.vaccine.geotxt.benchmark.instancereading;

import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;

/**
 * Interface for defining classes provide access to a set of problem instances, e.g.
 * by reading it from a file, directory, or database.
 * 
 * @author jow
 *
 */
public interface InstanceReader {
	
	/**
	 * returns the next instance in the set of problem instances
	 * @return next instance in the set of instances
	 */
	public ProblemInstance getNextInstance();
	
	/**
	 * returns the number of instances in the problem instance set
	 * @return number of instance in the instance set
	 */
	public int getNumOfInstances();
	
	/**
	 * returns whether there are still instances in the set that have not been accessed
	 * @return 
	 */
	public boolean hasMoreInstance();
}
