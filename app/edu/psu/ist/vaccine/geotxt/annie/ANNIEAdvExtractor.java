/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.annie;

import edu.psu.ist.vaccine.geotxt.entities.*;
import edu.psu.ist.vaccine.geotxt.utils.SortedAnnotationList;
import gate.*;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author ajaiswal
 */
public class ANNIEAdvExtractor {

    private static SerialAnalyserController annieController;
    private static Logger logger = Logger.getRootLogger();
    private static ProcessingResource annotpr;
    private static ProcessingResource split;
    private static ProcessingResource tokeniser;
    private static ProcessingResource postagger;
    private static ProcessingResource morpho;
    private static ProcessingResource bgazetteer;
    private static ProcessingResource transducer;
    private static ProcessingResource orthoMatcher;

    public void initAnnie() throws GateException {
        logger.info("Initialising ANNIE...");
        // create a serial analyser controller to run ANNIE with
        annieController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController", Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym());

        //Initialize AnnotatioNDeletePR
        annotpr = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR", Factory.newFeatureMap());
        annieController.add(annotpr);

        // Load sentence splitter
        split = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter", Factory.newFeatureMap());
        annieController.add(split);

        // Load tokenizer
        tokeniser = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser", Factory.newFeatureMap());
        annieController.add(tokeniser);

        // Load POS tagger
        postagger = (ProcessingResource) Factory.createResource("gate.creole.POSTagger", Factory.newFeatureMap());
        annieController.add(postagger);

        // Load Morpho
        morpho = (ProcessingResource) Factory.createResource("gate.creole.morph.Morph", Factory.newFeatureMap());
        annieController.add(morpho);

        //Load Baseline Gazetteer
        bgazetteer = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer");
        annieController.add(bgazetteer);

        transducer = (ProcessingResource) Factory.createResource("gate.creole.ANNIETransducer", Factory.newFeatureMap());
        annieController.add(transducer);

        // Load Ortho Matcher
        orthoMatcher = (ProcessingResource) Factory.createResource("gate.creole.orthomatcher.OrthoMatcher", Factory.newFeatureMap());
        annieController.add(orthoMatcher);
        logger.info("...ANNIE loaded");
    } // initAnnie()

    public void setCorpus(Corpus corpus) {
        annieController.setCorpus(corpus);
    } // setCorpus

    public void resetCorpus() throws ResourceInstantiationException {
        annieController.setCorpus(null);
        annieController.reInit();
    }

    /**
     * Run ANNIE
     */
    public void execute() throws GateException {
        //logger.info("Running ANNIE...");
        annieController.execute();
        //logger.info("...ANNIE complete");
    } // execute()

    public Corpus processText(String string) throws ResourceInstantiationException {
        Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
        Document doc = Factory.newDocument(string);
        corpus.add(doc);
        return corpus;
    }
    /*
     * Unloads Resources
     */

    public void cleanUp() {
        Corpus corp = annieController.getCorpus();
        if (!corp.isEmpty()) {
            for (int i = 0; i < corp.size(); i++) {
                Document doc1 = (Document) corp.remove(i);
                corp.unloadDocument(doc1);
                Factory.deleteResource(corp);
                Factory.deleteResource(doc1);
            }
        }
    }

    public void getNamedEntities(Corpus corpus) {
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            //logger.info(doc.getNamedAnnotationSets());
            logger.info(defaultAnnotSet.toString());
            //logger.info(defaultAnnotSet.toString());
            break;
        }
    }

    public ArrayList<Location> getLocations(Corpus corpus) {
        ArrayList<Location> locations = new ArrayList<Location>();
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            //logger.info(defaultAnnotSet);
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Location");
            annotTypesRequired.add("Facility");
            Set<Annotation> places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
            Iterator it = places.iterator();
            Annotation currAnnot;
            SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotations.addSortedExclusive(currAnnot);
            } // while
            //FeatureMap features = doc.getFeatures();
            String originalContent = doc.getContent().toString();
            //logger.info(originalContent);
            long posEnd;
            long posStart;
            for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                currAnnot = (Annotation) sortedAnnotations.get(i);
                posStart = currAnnot.getStartNode().getOffset().longValue();
                posEnd = currAnnot.getEndNode().getOffset().longValue();
                if (posEnd != -1 && posStart != -1) {
                    String name = originalContent.substring((new Long(posStart)).intValue(), (new Long(posEnd)).intValue());
                    String type = (String) currAnnot.getFeatures().get("locType");
                    String loc = currAnnot.getType();
                    int position = (new Long(posStart)).intValue();
                    if (type == null || type.isEmpty()) {
                        type = "";
                    }
                    Location location = new Location(name, type, position);
                    if (!locations.contains(location)) {
                        locations.add(location);
                    } else {
                        int index = locations.indexOf(location);
                        locations.get(index).addPosition(position);
                    }
                }
            }
            break;
        }
        /*
         * Clean up code
         */
        return locations;
    }

    public ArrayList<Organization> getOrganizations(Corpus corpus) {
        ArrayList<Organization> orgs = new ArrayList<Organization>();
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Organization");
            Set<Annotation> places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
            Iterator it = places.iterator();
            Annotation currAnnot;
            SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotations.addSortedExclusive(currAnnot);
            } // while
            String originalContent = doc.getContent().toString();
            long posEnd;
            long posStart;
            for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                currAnnot = (Annotation) sortedAnnotations.get(i);
                posStart = currAnnot.getStartNode().getOffset().longValue();
                posEnd = currAnnot.getEndNode().getOffset().longValue();
                if (posEnd != -1 && posStart != -1) {
                    String name = originalContent.substring((new Long(posStart)).intValue(), (new Long(posEnd)).intValue());
                    String type = currAnnot.getType();
                    int position = (new Long(posStart)).intValue();
                    String orgType = (String) currAnnot.getFeatures().get("orgType");
                    if (orgType == null || orgType.isEmpty()) {
                        orgType = type;
                    }
                    Organization org = new Organization(name, orgType, position);
                    if (!orgs.contains(org)) {
                        orgs.add(org);
                    } else {
                        int index = orgs.indexOf(org);
                        orgs.get(index).addPosition(position);
                    }
                }
            }
            break;
        }
        return orgs;
    }

    public ArrayList<Person> getPersons(Corpus corpus) {
        ArrayList<Person> persons = new ArrayList<Person>();
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set annotTypesRequired = new HashSet();
            //annotTypesRequired.add("Username");
            annotTypesRequired.add("Person");
            //annotTypesRequired.add("JobTitle");
            //annotTypesRequired.add("Title");
            Set<Annotation> places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
            Iterator it = places.iterator();
            Annotation currAnnot;
            SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotations.addSortedExclusive(currAnnot);
            } // while
            String originalContent = doc.getContent().toString();
            long posEnd;
            long posStart;
            for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                currAnnot = (Annotation) sortedAnnotations.get(i);
                posStart = currAnnot.getStartNode().getOffset().longValue();
                posEnd = currAnnot.getEndNode().getOffset().longValue();
                if (posEnd != -1 && posStart != -1) {
                    String name = originalContent.substring((new Long(posStart)).intValue(), (new Long(posEnd)).intValue());
                    int position = (new Long(posStart)).intValue();
                    String personType = currAnnot.getType();
                    String kind = (String) currAnnot.getFeatures().get("kind");
                    String sex = (String) currAnnot.getFeatures().get("gender");
                    if (kind == null) {
                        kind = "";
                    }
                    if (sex == null) {
                        sex = "";
                    }
                    Person person = new Person(name, sex, kind, position);
                    if (!persons.contains(person) && !person.getName().equals("@")) {
                        persons.add(person);
                    }
                }
            }
            break;
        }
        return persons;
    }

    public ArrayList<OtherEntity> getOthers(Corpus corpus) {
        ArrayList<OtherEntity> entities = new ArrayList<OtherEntity>();
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Time");
            annotTypesRequired.add("Greeting");
            annotTypesRequired.add("Date");
            annotTypesRequired.add("Year");
            annotTypesRequired.add("Money");
            annotTypesRequired.add("Address");
            annotTypesRequired.add("Weapon");
            annotTypesRequired.add("Legal");
            annotTypesRequired.add("Section");
            annotTypesRequired.add("Percent");
            annotTypesRequired.add("Sport");

            Set<Annotation> places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
            Iterator it = places.iterator();
            Annotation currAnnot;
            SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotations.addSortedExclusive(currAnnot);
            } // while
            String originalContent = doc.getContent().toString();
            long posEnd;
            long posStart;
            for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                currAnnot = (Annotation) sortedAnnotations.get(i);
                posStart = currAnnot.getStartNode().getOffset().longValue();
                posEnd = currAnnot.getEndNode().getOffset().longValue();
                if (posEnd != -1 && posStart != -1) {
                    String name = originalContent.substring((new Long(posStart)).intValue(), (new Long(posEnd)).intValue());
                    String type = currAnnot.getType();
                    OtherEntity entity = new OtherEntity(name, type);
                    if (!entities.contains(entity)) {
                        entities.add(entity);
                    }
                }
            }
            break;
        }
        return entities;
    }

    public ArrayList<Hashtag> getHashtag(Corpus corpus) {
        ArrayList<Hashtag> entities = new ArrayList<Hashtag>();
        for (Document doc : corpus) {
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Hashtag");
            //annotTypesRequired.add("Unknown");

            Set<Annotation> places = new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
            Iterator it = places.iterator();
            Annotation currAnnot;
            SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
            while (it.hasNext()) {
                currAnnot = (Annotation) it.next();
                sortedAnnotations.addSortedExclusive(currAnnot);
            } // while
            String originalContent = doc.getContent().toString();
            long posEnd;
            long posStart;
            for (int i = sortedAnnotations.size() - 1; i >= 0; --i) {
                currAnnot = (Annotation) sortedAnnotations.get(i);
                posStart = currAnnot.getStartNode().getOffset().longValue();
                posEnd = currAnnot.getEndNode().getOffset().longValue();
                if (posEnd != -1 && posStart != -1) {
                    String name = originalContent.substring((new Long(posStart)).intValue(), (new Long(posEnd)).intValue());
                    String type = currAnnot.getType();
                    Hashtag entity = new Hashtag(name, type);
                    if (!entities.contains(entity)) {
                        entities.add(entity);
                    }
                }
            }
            break;
        }
        return entities;
    }
}
