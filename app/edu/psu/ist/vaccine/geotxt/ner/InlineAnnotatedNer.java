package edu.psu.ist.vaccine.geotxt.ner;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.io.IOException;

public class InlineAnnotatedNer extends AbstractNer {

	public InlineAnnotatedNer() {

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
		String labeledText = text;
		/*
		 * String labeledText = classifier.classifyWithInlineXML(text); Set<String> tags = classifier.labels(); String background = classifier.backgroundSymbol(); String tagPattern = ""; for (String tag : tags) { if (background.equals(tag)) { continue; } if (tagPattern.length() > 0) { tagPattern += "|"; } tagPattern += tag; }
		 */

		String tagPattern = "LOCATION|PERSON|ORGANIZATION";

		Pattern startPattern = Pattern.compile("<(" + tagPattern + ")>");
		Pattern endPattern = Pattern.compile("</(" + tagPattern + ")>");

		String finalText = labeledText;
		Matcher m = startPattern.matcher(finalText);
		while (m.find()) {
			int start = m.start();
			finalText = m.replaceFirst("");
			m = endPattern.matcher(finalText);
			if (m.find()) {
				int end = m.start();
				String tag = m.group(1);
				finalText = m.replaceFirst("");
				String name = finalText.substring(start, end);

				if (tag.equals("LOCATION")) {
					d.addLoc(new Location(name, start));
				} else if (tag.equals("ORGANIZATION")) {
					d.addOrg(new Organization(name, start));
				} else if (tag.equals("PERSON")) {
					d.addPer(new Person(name, start));
				}
			}
			m = startPattern.matcher(finalText);
		}
		return d;
	}

	public static void main(String args[]) throws FileNotFoundException, IOException {
		InlineAnnotatedNer st = new InlineAnnotatedNer();
		NamedEntities results = st.tagAlltoDoc(
				"The <ORGANIZATION>White House</ORGANIZATION> has denied a report in the <LOCATION>New York</LOCATION> Times saying that <LOCATION>Iran</LOCATION> had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting <PERSON>unnamed officials</PERSON>, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(st.tagAlltoGeoJson("", false, 0, false, false));
	}
}
