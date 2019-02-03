package edu.psu.ist.vaccine.geotxt.ner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;

public class OpenNlpNer extends AbstractNer {

	private TokenizerME tokenizer;
	private NameFinderME locFinder;
	private NameFinderME orgFinder;
	private NameFinderME perFinder;

	public OpenNlpNer(String openNlpDir) {

		// Loading the tokenizer model
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(openNlpDir + "en-token.bin");

			TokenizerModel tokenModel = new TokenizerModel(inputStream);

			// Instantiating the TokenizerME class
			tokenizer = new TokenizerME(tokenModel);

			// Loading the NER models
			inputStream = new FileInputStream(openNlpDir + "en-ner-location.bin");
			TokenNameFinderModel locModel = new TokenNameFinderModel(inputStream);
			inputStream = new FileInputStream(openNlpDir + "en-ner-organization.bin");
			TokenNameFinderModel orgModel = new TokenNameFinderModel(inputStream);
			inputStream = new FileInputStream(openNlpDir + "en-ner-person.bin");
			TokenNameFinderModel perModel = new TokenNameFinderModel(inputStream);

			// Instantiating the NameFinderME class
			locFinder = new NameFinderME(locModel);
			orgFinder = new NameFinderME(orgModel);
			perFinder = new NameFinderME(perModel);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.getLogger(OpenNlpNer.class.getName()).log(Level.SEVERE, "Could not load OpenNlps model files. Check the OPENNLPDIR address in the app.config file");
			Logger.getLogger(OpenNlpNer.class.getName()).log(Level.SEVERE, null, e.getMessage());
			System.exit(-1);
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
		NamedEntities nes = new NamedEntities();
		nes.text = text;

		Span tokenSpans[] = tokenizer.tokenizePos(text);
		String tokenStrings[] = tokenizer.tokenize(text);

		// Finding the names in the sentence
		Span locSpans[] = locFinder.find(tokenStrings);
		Span orgSpans[] = orgFinder.find(tokenStrings);
		Span perSpans[] = perFinder.find(tokenStrings);

		// Printing the names and their spans in a sentence
		for (Span s : locSpans) {
			nes.addLoc(new Location(tokenStrings[s.getStart()], tokenSpans[s.getStart()].getStart()));
		}

		for (Span s : orgSpans) {
			nes.addOrg(new Organization(tokenStrings[s.getStart()], tokenSpans[s.getStart()].getStart()));
		}

		for (Span s : perSpans) {
			nes.addPer(new Person(tokenStrings[s.getStart()], tokenSpans[s.getStart()].getStart()));
		}

		return nes;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Config config = new Config();
		OpenNlpNer openNlp = new OpenNlpNer(config.getOpenNlpDir());

		// String sentence = "I love Brack Obama and live in Pennsylvania. I would love to move to Ohio, especially Columbus, OH, to work at Starbucks.";

		NamedEntities results = openNlp.tagAlltoDoc(
				"The White House has denied a report in the New York Times saying that Iran had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting unnamed officials, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(openNlp.tagAlltoGeoJson("", false, 0, false, false));

	}

}
