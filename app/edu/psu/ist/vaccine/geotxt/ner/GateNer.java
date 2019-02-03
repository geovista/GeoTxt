package edu.psu.ist.vaccine.geotxt.ner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.psu.ist.vaccine.geotxt.annie.ANNIEAdvExtractor;
import edu.psu.ist.vaccine.geotxt.annie.GATEExtractor;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import gate.Corpus;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

public class GateNer extends AbstractNer {

	private static ANNIEAdvExtractor annie = null;

	public GateNer(String gatehome) {

		if (annie == null) {
			try {
				GATEExtractor.getInstance(gatehome);
				annie = new ANNIEAdvExtractor();
				annie.initAnnie();
			} catch (GateException e) {
				Logger.getLogger(GateNer.class.getName()).log(Level.SEVERE, "Could not load OpenNlps model files. Check the OPENNLPDIR address in the app.config file");
				Logger.getLogger(GateNer.class.getName()).log(Level.SEVERE, null, e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Logger.getLogger(GateNer.class.getName()).log(Level.SEVERE, "Could not load OpenNlps model files. Check the OPENNLPDIR address in the app.config file");
				Logger.getLogger(GateNer.class.getName()).log(Level.SEVERE, null, e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// public String tagAllJsonString(String text) {
	// Document d = tagAlltoDoc(text);
	// return xstream.toXML(d);
	// }
	
	@Override
	public String tagAlltoGeoJson(String text, boolean includeAlternates, int maxCandidates, boolean includeHierarchy, boolean includeDetails) {
		NamedEntities d = tagAlltoDoc(text);
		return GeoJsonWriter.docToGeoJson(d, includeAlternates, maxCandidates, includeHierarchy, includeDetails);
	}

	@Override
	public NamedEntities tagAlltoDoc(String text) {
		if (text.equalsIgnoreCase("")) {
			return null;
		}
		NamedEntities d = new NamedEntities();
		Corpus corpus = null;
		try {
			corpus = annie.processText(text);
			annie.setCorpus(corpus);
			annie.execute();
			d.text = text;
			d.locs = annie.getLocations(corpus);
			d.orgs = annie.getOrganizations(corpus);
			d.pers = annie.getPersons(corpus);
		} catch (ResourceInstantiationException e) {
			e.printStackTrace();
		} catch (GateException e) {
			e.printStackTrace();
		}

		/*
		 * I think we should find a way to change this so that the cleanup does not happen every single document. We should have something for the API that adds the documents to a batch, processes them in this batch, and them clean them up in batch. Right now, every single document is processed and then cleaned up one by one and that doesn't seem
		 * efficient for an API. For the GeoTxt UI, this seems fine. It was actually AJ's post from 2011 I found about cleaning up the corpus that has seemed to cut the memory usage substantially. http://anujjaiswal.wordpress.com/2011/06/01/removing-out-of-memory-errors-in-gate/
		 */

		annie.cleanUp();

		return d;
	}

	public static void main(String args[]) {
		GateNer gate = new GateNer("C:\\Programs\\gate-8.4-build5748-BIN");
		NamedEntities results = gate.tagAlltoDoc(
				"The White House has denied a report in the New York Times saying that Iran had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting unnamed officials, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(results.toString());

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(gate.tagAlltoGeoJson("__", false, 0, false, false));

	}
}
