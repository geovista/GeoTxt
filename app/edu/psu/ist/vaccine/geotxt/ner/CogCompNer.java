package edu.psu.ist.vaccine.geotxt.ner;

import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.nlp.utility.TokenizerTextAnnotationBuilder;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import edu.illinois.cs.cogcomp.annotation.TextAnnotationBuilder;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.ner.NERAnnotator;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import java.io.IOException;
//now using the 3.1.25 which is CONLL + Enron, used to be 3.1.10 that only used CoNLL apparently and was faster. 
public class CogCompNer extends AbstractNer{

	private NERAnnotator nerAn = null;
	// Create a TextAnnotation using the LBJ sentence splitter and tokenizers.
	private TextAnnotationBuilder tab = null;
	// don't split on hyphens, as NER models are trained this way
	private boolean splitOnHyphens = false;

	public CogCompNer(String viewName) {

		if (viewName != null && viewName != "") {
			if (viewName == "CONLL") {
				tab = new TokenizerTextAnnotationBuilder(new StatefulTokenizer(splitOnHyphens));
				try {
					nerAn = new NERAnnotator(ViewNames.NER_CONLL);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Could not Read NER_CONLL" + e.getMessage());
				}
			}
			nerAn.doInitialize();
		}

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

		TextAnnotation ta = tab.createTextAnnotation(text);

		nerAn.addView(ta);

		for (Constituent c : ta.getView(ViewNames.NER_CONLL).getConstituents()) {

			// double score = c.getConstituentScore();
			int start = c.getStartCharOffset();
			String name = c.getSurfaceForm();
			String tag = c.getLabel();

			if (tag.equals("LOC")) {
				d.addLoc(new Location(name, start));
			} else if (tag.equals("ORG")) {
				d.addOrg(new Organization(name, start));
			} else if (tag.equals("PER")) {
				d.addPer(new Person(name, start));
			}
		}

		return d;

	}

	public static void main(String[] args) throws IOException {
		CogCompNer il = new CogCompNer("CONLL");
		NamedEntities results = il.tagAlltoDoc(
				"The White House has denied a report in the New York Times saying that Iran had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting unnamed officials, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(il.tagAlltoGeoJson("", false, 0, false, false));

	}
}