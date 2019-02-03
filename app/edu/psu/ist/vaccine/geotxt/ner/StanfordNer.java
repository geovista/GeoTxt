package edu.psu.ist.vaccine.geotxt.ner;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StanfordNer extends AbstractNer {

	private CRFClassifier<CoreLabel> classifier = null;

	public StanfordNer(String modelPath) {

		classifier = CRFClassifier.getClassifierNoExceptions(modelPath);
	}

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
		d.text = text;

		// Set<String> tags = classifier.labels();
		List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(text);
		for (Triple<String, Integer, Integer> namedEntity : list) {
			String tag = namedEntity.first();
			int start = namedEntity.second();
			String name = text.substring(namedEntity.second(), namedEntity.third());

			if (tag.equals("LOCATION")) {
				d.addLoc(new Location(name, start));
			} else if (tag.equals("ORGANIZATION")) {
				d.addOrg(new Organization(name, start));
			} else if (tag.equals("PERSON")) {
				d.addPer(new Person(name, start));
			}
		}

		return d;
	}

	public static void main(String args[]) throws FileNotFoundException, IOException {
		Config config = new Config();
		StanfordNer st = new StanfordNer(config.getStanford_ner());
		NamedEntities results = st.tagAlltoDoc(
				"The White House has denied a report in the New York Times saying that Iran had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting unnamed officials, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(st.tagAlltoGeoJson("", false, 0, false, false));
	}
}
