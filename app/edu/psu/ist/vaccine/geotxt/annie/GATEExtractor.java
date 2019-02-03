/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package edu.psu.ist.vaccine.geotxt.annie;

import gate.Gate;
import gate.util.GateException;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author ajaiswal
 */
public class GATEExtractor {

    private static GATEExtractor instance = null;
    private static Gate gate = null;
    private static Logger logger = Logger.getRootLogger();

    /**
     * Add parameter gatehome to initial the Gate
     */
    protected GATEExtractor(String gatehome) throws GateException, IOException{
        logger.info("Initializing GATE");
        logger.setLevel(Level.FATAL);
        logger.setLevel(Level.INFO);
        //logger.setLevel(Level.DEBUG);
        //System.setProperty("gate.home", "/r2/opt/gate6"); 
        System.setProperty("gate.home", gatehome); //TODO:
        // System.setProperty("gate.home", "/opt/gate6");
        //System.setProperty("gate.home", "C:/gate6");
        gate = new Gate();
        gate.init();
        logger.info("Loading ANNIE Plugin");
        File gateHome = gate.getGateHome();
        File pluginsHome = new File(gateHome, "plugins");
        gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURI().toURL());
        //gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tagger_MetaMap").toURL());
        //gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tagger_Chemistry").toURL());
        //gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tagger_Abner").toURL());
        gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Tools").toURI().toURL());
        //gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Gazetteer_LKB").toURL());
        //gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "LingPipe").toURL());
        logger.info("Done Initializing GATE");
    }

    public static Gate getGateInstance(){
        return gate;
    }

    public static GATEExtractor getInstance(String gatehome) throws GateException, IOException{
        if(instance==null){
            logger.info("Creating Gate Loader");
            instance = new GATEExtractor(gatehome);
        }
        logger.info("Returning Gate Loader Instance");
        return instance;
    }

    public static void destroy(){
        gate = null;
        instance = null;
    }
}

