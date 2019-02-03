package edu.psu.ist.vaccine.geotxt.test;

import edu.psu.ist.vaccine.geotxt.annie.ANNIEAdvExtractor;
import edu.psu.ist.vaccine.geotxt.annie.GATEExtractor;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.OtherEntity;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import gate.Corpus;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class EntityExtractionTest {
	public static ANNIEAdvExtractor annie = null;

	public static ArrayList<String> input(String infile) {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(infile));
			String line = null;
			while ((line = br.readLine()) != null) {
				ret.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void processText(String text) {
		try {
			Corpus corpus = annie.processText(text);
			annie.setCorpus(corpus);
			annie.execute();
			ArrayList<OtherEntity> entities = annie.getOthers(corpus);
			ArrayList<Location> locs = annie.getLocations(corpus);
			ArrayList<Organization> orgs = annie.getOrganizations(corpus);
			ArrayList<Person> pers = annie.getPersons(corpus);
			if (!locs.isEmpty()) {
				for (Location loc : locs) {
					System.out.print(loc.getName() + "\t");
				}
			}
			System.out.println(locs.size());
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (GateException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		try {
			GATEExtractor.getInstance("C:/Users/wzh112/workspace/gate-7.1-build4485-BIN");
			annie = new ANNIEAdvExtractor();
			annie.initAnnie();
			ArrayList<String> tweets = input("tweets");
			for (String str : tweets) {
				processText(str);
			}
		} catch (GateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
