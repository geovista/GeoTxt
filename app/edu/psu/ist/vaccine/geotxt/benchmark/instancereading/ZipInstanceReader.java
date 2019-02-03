package edu.psu.ist.vaccine.geotxt.benchmark.instancereading;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.psu.ist.vaccine.geotxt.benchmark.ProblemInstance;


/**
 * Class for reading problem instances from a zip file. All files containing problem 
 * instances need to have the extension .inst. 
 * @author jow
 *
 */
public class ZipInstanceReader implements InstanceReader {

	/**
	 * points at the current instance that will be returned by getNextInstance()
	 */
	protected int count = 0;

	/**
	 * list of all files containing problem instances
	 */
	protected ArrayList<ZipEntry> instanceFiles = new ArrayList<ZipEntry>();
	
	
	/**
	 * stream for zip file from which instances will be read
	 */
	protected ZipInputStream zis;
	
	/**
	 * creates a new instance for the set of problem instances stored in
	 * the zip file pathToZipFile
	 * @param pathToZipFile
	 */
	public ZipInstanceReader(String pathToZipFile) {
		Pattern filePattern = Pattern.compile(".*[.]inst$");
	
		try {
			zis = new ZipInputStream(new FileInputStream(pathToZipFile));

			ZipEntry entry;
			while((entry = zis.getNextEntry())!=null) {
				if (filePattern.matcher(entry.getName()).matches()) {	
					instanceFiles.add(entry);
				}
			}
			
			zis.close();
			
			zis = new ZipInputStream(new FileInputStream(pathToZipFile));

			
		} catch (Exception e) {
			System.out.println("file operation failed while reading file: "+e);
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public ProblemInstance getNextInstance() {	

		System.out.println("Trying to read instance " + count + " " + instanceFiles.get(count).getName());
		ProblemInstance inst = null;

		try {
			ZipEntry ze = zis.getNextEntry();
			while (!ze.getName().equals(this.instanceFiles.get(count).getName())) {
				ze = zis.getNextEntry();
			}
			
			System.out.println("zip entry: "+ze.getName());
			
			StringBuilder sb = new StringBuilder();
			for (int c = zis.read(); c != -1; c = zis.read()) {
			    sb.append((char)c);
			}
			
			inst = new ProblemInstance(new ByteArrayInputStream(sb.toString().getBytes("UTF-8"))); 
			
		} catch (Exception e) {
			System.out.println("file operation failed, could not read entry: "+e);
			e.printStackTrace();
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
