package edu.psu.ist.vaccine.geotxt.benchmark;

import java.io.FileInputStream;

import java.io.FileOutputStream;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.psu.ist.vaccine.analyzers.HierarchyAnalyzer;
import edu.psu.ist.vaccine.geotxt.benchmark.instancereading.InstanceReader;
import edu.psu.ist.vaccine.geotxt.benchmark.instancereading.ZipInstanceReader;
import edu.psu.ist.vaccine.geotxt.benchmark.tester.GenericAnalyzerTester;
//import edu.psu.ist.vaccine.geotxt.benchmark.tester.GeoTxtTester;
import edu.psu.ist.vaccine.geotxt.benchmark.tester.Tester;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to run a benchmark. See main method for an example how to use this
 * class.
 *
 * @author jow
 *
 */
public class Benchmark {

    /**
     * provides access to the problem instances that will be used in the
     * benchmark
     */
    protected InstanceReader iReader;
    /**
     * approaches that will be compared in the benchmark
     */
    protected List<Tester> testers;

    /**
     * Sets up the benchmark with an InstanceReader and a list of Tester classes
     * for different approaches that will be compared
     *
     * @param reader
     * @param testers
     */
    public Benchmark(InstanceReader reader, List<Tester> testers) {
        this.iReader = reader;
        this.testers = testers;
    }

    /**
     * Runs the benchmark and writes a summary to ps. No output will be
     * generated if ps is null. The output uses tabs to delimit cells in a row.
     *
     * @param ps
     */
    public void run(PrintStream ps) {


        BigDecimal[] numPlaces = new BigDecimal[testers.size()];
        BigDecimal[] correctlyIdentified = new BigDecimal[testers.size()];
        BigDecimal[] placesFound = new BigDecimal[testers.size()];

        for (int i = 0; i < testers.size(); i++) {
            numPlaces[i] = BigDecimal.valueOf(0);
            correctlyIdentified[i] = BigDecimal.valueOf(0);
            placesFound[i] = BigDecimal.valueOf(0);
        }

        double[] times = new double[testers.size()];
        double[] scoreIDs = new double[testers.size()];

        long count = 0;

        // generate header
        if (ps != null) {
            ps.print("problem instance\tplaces");

            for (int i = 0; i < testers.size(); i++) {
                ps.print("\t" + testers.get(i).getName() + "\t \t \t ");
            }
            ps.print("\n");
        }

        // run testers over all problem instances
        while (iReader.hasMoreInstance()) {
            ProblemInstance p = iReader.getNextInstance();

            if (ps != null) {
                ps.print(p.getText() + "\t" + p.places.size());
            }

            for (int i = 0; i < testers.size(); i++) {
                Tester t = testers.get(i);
                if (ps != null) {
                    ps.print("\t");
                }

                TestResult res = t.run(p);

                // process identified locations

                double precision = -1.0;
                double recall = -1.0;

                if (t.supportsLocationRecognition()) {
                    if (res.getPlacesFound() == 0) {
                        precision = 0;
                    } else {
                        precision = res.getLocationsIdentifiedCorrectly() / ((double) res.getPlacesFound());
                    }

                    if (p.places.size() == 0) {
                        recall = 1;
                    } else {
                        recall = res.getLocationsIdentifiedCorrectly() / ((double) p.places.size());
                    }
                    System.out.println("places found: " + res.getPlacesFound());
                    System.out.println("getLocationsIdentifiedCorrectly: " + res.getLocationsIdentifiedCorrectly());
                    System.out.println("places: " + p.places.size());


                    placesFound[i] = placesFound[i].add(BigDecimal.valueOf(res.getPlacesFound()));
                    correctlyIdentified[i] = correctlyIdentified[i].add(BigDecimal.valueOf(res.getLocationsIdentifiedCorrectly()));
                    numPlaces[i] = numPlaces[i].add(BigDecimal.valueOf(p.places.size()));
                }

                System.out.println(t.getName() + ": " + precision);
                System.out.println(t.getName() + ": " + recall);

                if (ps != null) {
                    ps.print(precision + "\t" + recall + "\t");
                }

                // process computation times

                System.out.println(t.getName() + ": " + res.getcTime());
                if (ps != null) {
                    ps.print(res.getcTime() + "\t");
                }

                //	if (count == 0) scores[i] = score;
                //	else scores[i] = scores[i] + (score - scores[i]) / (count+1);

                if (count == 0) {
                    times[i] = res.getcTime();
                } else {
                    times[i] = times[i] + (res.getcTime() - times[i]) / (count + 1);
                }

                // process geonames IDs

                double scoreID = -1.0;
                int correctIDs = 0;

                if (t.supportsGeonamesIDs()) {

                    for (int n = 0; n < res.getIdentificationStatus().length; n++) {

                        if (res.getIdentificationStatus()[n]) {
                            System.out.println(res.getLocations()[n].getName());
                            System.out.println(p.getPlaces().get(n).getNameInText());
                            if (res.getLocations()[n].getGeometry() != null) {
                                System.out.println("from location: " + res.getLocations()[n].getGeometry().getGeoNameId().toString());
                                System.out.println("ground truth: " + p.getPlaces().get(n).getGeonamesId());
                                if (res.getLocations()[n].getGeometry().getGeoNameId().toString().equals(p.getPlaces().get(n).getGeonamesId())) {
                                    correctIDs++;
                                }
                            }
                        }
                    }

                    if (p.getPlaces().size() == 0) {
                        scoreID = 1;
                    } else {
                        scoreID = correctIDs / ((double) p.places.size());
                    }
                }

                if (ps != null) {
                    ps.print(scoreID);
                }

                if (count == 0) {
                    scoreIDs[i] = scoreID;
                } else {
                    scoreIDs[i] = scoreIDs[i] + (scoreID - scoreIDs[i]) / (count + 1);
                }

                System.out.println(t.getName() + ": " + scoreID);
            }

            if (ps != null) {
                ps.print("\n");
            }

            count++;
        }

        // print averages

        if (ps != null) {
            ps.print("average:\t ");
            for (int i = 0; i < testers.size(); i++) {
                BigDecimal precision = BigDecimal.valueOf(-1);
                BigDecimal recall = BigDecimal.valueOf(-1);
                if (testers.get(i).supportsLocationRecognition()) {
                    precision = correctlyIdentified[i].divide(placesFound[i], 3, RoundingMode.HALF_UP);
                    recall = correctlyIdentified[i].divide(numPlaces[i], 3, RoundingMode.HALF_UP);
                }
                ps.print("\t" + precision + "\t" + recall + "\t" + times[i] + "\t" + scoreIDs[i]);
            }
        }
    }

    /**
     * Auxiliary method that can be used to translate the output of the run()
     * method into an html file.
     *
     * @param summary stream to access the output of the run method
     * @param output stream to which the html output will be written
     */
    public static void generateReport(InputStream summary, PrintStream output) {
        Scanner sc = new Scanner(summary);
        output.print("<html>\n<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n</head>\n<body>\n<table>");

        String l = sc.nextLine();
        String[] sts = l.split("\t");
        String other = "<tr><td></td><td> </td>";

        output.print("<tr>");
        for (int i = 0; i < sts.length; i++) {
            output.print("<td align=\"center\">" + sts[i] + "</td>");
            if (i > 1 && (i - 2) % 4 == 0) {
                other += "<td align=\"center\">location recognition precision</td>";
            }
            if (i > 1 && (i - 2) % 4 == 1) {
                other += "<td align=\"center\">location recognition recall</td>";
            }
            if (i > 1 && (i - 2) % 4 == 2) {
                other += "<td align=\"center\">computation time</td>";
            }
            if (i > 1 && (i - 2) % 4 == 3) {
                other += "<td align=\"center\">geonamesID correspondence rate</td><td></td>";
                output.print("<td></td>");
            }
        }
        output.print("</tr>\n<tr>\n" + other + "\n");

        while (sc.hasNextLine()) {
            l = sc.nextLine();
            sts = l.split("\t");
            output.print("<tr><td>" + sts[0] + "</td><td align=\"center\">" + sts[1]);

            for (int i = 2; i < sts.length; i += 4) {
                output.print("</td><td align=\"center\" bgcolor=\""
                        + ((Double.parseDouble(sts[i]) == 1.0) ? "#CCEECC" : "#EECCCC") + "\">" + ((Double.parseDouble(sts[i])) * 100) + "%</td>");
                output.print("</td><td align=\"center\" bgcolor=\""
                        + ((Double.parseDouble(sts[i + 1]) == 1.0) ? "#CCEECC" : "#EECCCC") + "\">" + ((Double.parseDouble(sts[i + 1])) * 100) + "%</td>");
                output.print("</td><td bgcolor=\"#CCCCCC\" align=\"center\">" + sts[i + 2] + "[ms]</td>");
                output.print("</td><td bgcolor=\"#CCCCCC\" align=\"center\">" + (sts[i + 3].equals("-1.0") ? "n/a" : sts[i + 3]) + "</td><td></td>");
            }

            output.print("</tr>\n");
        }

        output.print("</table>\n</body>\n</html>");
    }

    public static final void main(String[] args) {
        Config config = null;
        try {
            // load configuration parameters from property file
            config = new Config();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Benchmark.class.getName()).log(Level.SEVERE, null, ex);
        }

        //MapHierarchyPlaces.username = "jow11"; // put your geonames user name here

        // comment out previous try-catch block and put hard-coded paths here if you dont want to use property file
        String pathToGate = config.getGate_home();
        String pathToStanfordClassifier = config.getStanford_ner();
        String pathToInstanceZipFile = "instances3_part1.zip";

        // set up Tester classes that will be compared

        ArrayList<Tester> ts = new ArrayList<Tester>();

        //ts.add(new GateTester(pathToGate)); // just the gate NER component
        //ts.add(new StanfordTester(pathToStanfordClassifier)); // just the stanford NER component

        //ts.add(new GenericAnalyzerTester(new BasicGateAnalyzer(pathToGate),"BasicGateAnalyzer",true,true,true)); 
        //ts.add(new GenericAnalyzerTester(new BasicStanfordAnalyzer(pathToStanfordClassifier),"BasicStanfordAnalyzer",true,true,true));

        //ts.add(new GenericAnalyzerTester(new GateHierarchyAnalyzer(pathToGate,50,5),"GateHierarchyAnalyzer",true,true,true));
        ts.add(new GenericAnalyzerTester(new HierarchyAnalyzer(pathToStanfordClassifier, 50, 30), "StanfordHierarchyAnalyzer", true, true, true));

        // run the benchmark

        Benchmark gb = new Benchmark(new ZipInstanceReader(pathToInstanceZipFile), ts);


        // run benchmark and write output to file, then generate report file in html format 
        try {
            PrintStream bw = new PrintStream(new FileOutputStream("test-summary"));
            gb.run(bw);
            bw.close();

            PrintStream bw2 = new PrintStream(new FileOutputStream("test-report.html"));
            FileInputStream fis = new FileInputStream("test-summary");
            generateReport(fis, bw2);
            bw2.close();
            fis.close();
        } catch (Exception e) {
            System.out.println("exception while trying to generate reports: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
