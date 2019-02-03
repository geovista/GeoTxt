package edu.psu.ist.vaccine.geotxt.benchmark.instancereading;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;


/**
 * Class for reading problem instances from a set that is stored as separate files
 * (one per instance) contained in a directory. All files containing problem 
 * instances need to have the extension .inst
 * @author jow
 *
 */
public class DirectoryInstanceReader implements InstanceReader {

	/**
	 * points at the current instance that will be returned by getNextInstance()
	 */
	protected int count = 0;
	
	/**
	 * list of all files containing problem instances
	 */
	protected ArrayList<File> instanceFiles = new ArrayList<File>();

	/**
	 * creates a new instance for the set of problem instances stored in
	 * the directory dir
	 * @param dir
	 */
	public DirectoryInstanceReader(String dir) {
		File fo = new File(dir);
		File[] files = fo.listFiles(); 
		
		Pattern filePattern = Pattern.compile(".*[.]inst$");
		
		for (int i = 0; i < files.length; i++) {
			if (filePattern.matcher(files[i].getName()).matches()) {
				instanceFiles.add(files[i]);
				//System.out.println("adding "+ files[i]);
			}
		}
	}

	@Override
	public ProblemInstance getNextInstance() {	
		
		System.out.println("Trying to read instance file " + count + " " + instanceFiles.get(count).getAbsolutePath());

		ProblemInstance inst = null;
		
		try {
			InputStream is = new FileInputStream(instanceFiles.get(count).getAbsolutePath());
			inst = new ProblemInstance(is);
			is.close();
			
		} catch (Exception e) {
			System.out.println("file operation failed, could not read file");
		}

		count++;
		return inst;
	}

	@Override
	public int getNumOfInstances() {
		return instanceFiles.size();
	}

	@Override
	public boolean hasMoreInstance() {
		return count < instanceFiles.size();
	}
}
