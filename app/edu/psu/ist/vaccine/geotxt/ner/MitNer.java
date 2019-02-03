package edu.psu.ist.vaccine.geotxt.ner;

import edu.mit.ll.mitie.EntityMention;
import edu.mit.ll.mitie.EntityMentionVector;
import edu.mit.ll.mitie.NamedEntityExtractor;
import edu.mit.ll.mitie.StringVector;
import edu.mit.ll.mitie.TokenIndexVector;
import edu.mit.ll.mitie.global;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MitNer extends AbstractNer {

	private NamedEntityExtractor classifier = null;

	public MitNer(String modelPath) {
		classifier = new NamedEntityExtractor(modelPath);
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
		System.out.println(text);

		// StringVector words = global.tokenize(text);
		TokenIndexVector offsets = global.tokenizeWithOffsets(text);
		EntityMentionVector entities = classifier.extractEntities(offsets);

		for (int i = 0; i < entities.size(); ++i) {
			EntityMention entity = entities.get(i);
			int start = Math.toIntExact(offsets.get(entity.getStart()).getIndex());
			int end = Math.toIntExact(offsets.get(entity.getEnd() - 1).getIndex() + offsets.get(entity.getEnd() - 1).getToken().length());
			while (end > text.length()) {
				// seems to be a problem with MIT tokenization
				end--;
			}

			String name = text.substring(start, end);
			if (entity.getTag() == 1) {
				d.addLoc(new Location(name, start));
			} else if (entity.getTag() == 2) {
				d.addOrg(new Organization(name, start));
			} else if (entity.getTag() == 0) {
				d.addPer(new Person(name, start));
			}
		}
		return d;
	}

	public static void main(String args[]) throws FileNotFoundException, IOException {
		Config config = new Config();
		MitNer mit = new MitNer(config.getMit_dir());
		NamedEntities results = mit.tagAlltoDoc("RT @nytimeshealth: Nigeria’s Ebola success was in part due to the existence of an emergency command center paid for by the Iran…");
		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(mit.tagAlltoGeoJson("", false, 0, false, false));
	}
}
