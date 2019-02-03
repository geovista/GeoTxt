/*
 * This is a test class used to inspect the OutofMemory exception. It will be used to fetch tweets from twitter and feed them to GeoTxt, Gate ANNIE NER or Stanford NER to find the potential memory leaks. 
 */
package edu.psu.ist.vaccine.geotxt.utils;

import edu.psu.ist.vaccine.geotxt.api.GeoTxtApi;
import twitter4j.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author Morteza Karimzadeh
 */
public class TwitterStreamCollection {

    static ConfigurationBuilder cb = new ConfigurationBuilder();

    public static void main(String[] args) throws TwitterException, IllegalArgumentException, URISyntaxException, IOException {



        //get the physical address of Stanford an Gate
        final Config config = new Config();

        //MapHierarchyPlaces.username = "siddhartha"; // put your geonames user name here

        //Create an instance of GeoTxt
        //final GeoTxtApi geoTxtApi = new GeoTxtApi(Config.properties.getProperty("gate_home"), Config.properties.getProperty("stanford_ner_classifier"));

        //create basic Gate Analyzer
        //final BasicGateAnalyzer gate = new BasicGateAnalyzer(Config.properties.getProperty("gate_home"));


        //Twitter Authentication (OAuth)
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("z9V9ODs2145XfgWc7gBhxQ")
                .setOAuthConsumerSecret("QRgRGpgNWgLATp3aNFYvC7PyUfltYxzKUpEBSD2w")
                .setOAuthAccessToken("904552788-A6e3LFOZxyf8grexUyOl53kkaCSOlrkne8EjENpP")
                .setOAuthAccessTokenSecret("X1iQKwWCEYgC7OskIR0CZXThJ5cMVLM4Un9bsNXhBI");




        StatusListener listener = new StatusListener() {
            //create basic Stanford Analyzer
            //            BasicStanfordAnalyzer stanford = new BasicStanfordAnalyzer(config.getStanford_ner());
            //            BasicGateAnalyzer gate = new BasicGateAnalyzer(config.getGate_home());
            GeoTxtApi geoTxtApi = new GeoTxtApi(config.getGate_home(), config.getStanford_ner(), true, config.getOpenNlpDir(), config.getLingPipeDir(), config.getMit_dir());
            FileWriter fw = new FileWriter("tweets.txt");
            //Overriding StatusListener several abstract methods.
            //Use this part to process incoming tweets. You can use either GeoTxt, Stanford NER or Gate NER

            @Override
            public void onStatus(Status status) {


                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());

                try {
                    //fw.write(">>>>>" + status.getText() + "\n");
                    //test GeoTxt API
                    //                    System.out.println(gate.gate.tagAlltoGeoJson(status.getText()));
                    //                    System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "gate"));
                    //System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "gateh", false, 0, false,false));
                    System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "cogcomp", true, 100, true,true));
                    //                    System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "stanford"));
                    //                    System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "stanfordh"));
                    //                    System.out.println(geoTxtApi.geoCodeToGeoJson(status.getText(), "stanfords"));
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(TwitterStreamCollection.class.getName()).log(Level.SEVERE, null, ex);
                    //                }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(TwitterStreamCollection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TwitterStreamCollection.class.getName()).log(Level.SEVERE, null, ex);
                }


                //test basic Stanford NER
                //                System.out.println(stanford.st.tagAlltoDoc(status.getText()));

                //test basic Gate ANNIE
                //                System.out.println(gate.gate.tagAlltoDoc(status.getText()));

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
                try {
                    fw.close();
                } catch (Exception e) {
                }
            }
        };



        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);
        twitterStream.sample();


    }
}
