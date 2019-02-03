package edu.psu.ist.vaccine.geotxt.ner;


public abstract class AbstractNer {
	
	public abstract String tagAlltoGeoJson(String text, boolean includeAlternates, int maxCandidates, boolean includeHierarchy, boolean includeDetails);

	public abstract NamedEntities tagAlltoDoc(String text);

}
