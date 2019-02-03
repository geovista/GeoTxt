/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.psu.ist.vaccine.geotxt.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;

/**
 *
 * @author Morteza Karimzadeh
 */
public interface Analyzer {

    NamedEntities analyze(String text, NerEngines engine, Map <String, Object> context) throws IllegalArgumentException, URISyntaxException, URISyntaxException, IOException;
    
}


