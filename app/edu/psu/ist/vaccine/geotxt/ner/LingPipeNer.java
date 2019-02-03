package edu.psu.ist.vaccine.geotxt.ner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.entities.Organization;
import edu.psu.ist.vaccine.geotxt.entities.Person;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;



public class LingPipeNer extends AbstractNer {

	private Chunker chunker;

	public LingPipeNer(String modelPath) {

		File modelFile = new File(modelPath);

		try {
			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			Logger.getLogger(LingPipeNer.class.getName()).log(Level.SEVERE, "Could not load LingPipe model file. Check the LINGPIPEMODEL path in the app.config file");
			Logger.getLogger(LingPipeNer.class.getName()).log(Level.SEVERE, null, e.getMessage());
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

		Chunking chunking = chunker.chunk(text);
		Set<Chunk> chunkingSet = chunking.chunkSet();

		for (Chunk chunk : chunkingSet) {

			String tag = chunk.type();
			int start = chunk.start();
			String name = text.substring(chunk.start(), chunk.end());
			
			if (tag.equals("LOCATION")) {
				nes.addLoc(new Location(name, start));
			} else if (tag.equals("ORGANIZATION")) {
				nes.addOrg(new Organization(name, start));
			} else if (tag.equals("PERSON")) {
				nes.addPer(new Person(name, start));
			}
		}

		return nes;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		Config config = new Config();
		LingPipeNer lingPipe = new LingPipeNer(config.getLingPipeDir());

		NamedEntities results = lingPipe.tagAlltoDoc(
				"The White House has denied a report in the New York Times saying that Iran had agreed to one-on-one negotiations over its nuclear programme with the US. The report, quoting unnamed officials, said Iran had agreed to the talks for the first time but would not hold them until after US elections on 6 November.The White House said it was prepared to meet Iran bilaterally, but that there was no plan to do so.Western states think Iran is seeking nuclear weapons, something it denies.Iran has been a key foreign policy topic in the US election campaign.President Barack Obama and Republican challenger Mitt Romney will hold their third and final campaign debate on Monday, on the subject of foreign policy.");

		System.out.println(GeoJsonWriter.docToGeoJson(results, false, 0, false, false));
		System.out.println(lingPipe.tagAlltoGeoJson("", false, 0, false, false));
	}

}
