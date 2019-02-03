package edu.psu.ist.vaccine.geotxt.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory class to provide access to configuration parameters.
 *
 * @author jow
 *
 */
public class Config {

	/**
	 * Property set to get configuration parameters. Properties include:
	 * gate_home - path to home folder of gate stanford_ner_classifier - path to
	 * classifier used for stanford NER benchmark_instance_set - path to file or
	 * folder containing the instance set for benchmarking
	 */
	private String gate_home = "";
	private String stanford_ner = "";
	private String opennlp_dir = "";
	private String lingpipe_dir ="";
	private String mit_model = "";

	/**
	 * new Properties() Loads properties from file propertyFile.
	 *
	 * @param propertyFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Config() throws FileNotFoundException, IOException {
		// String propertyFile = ".properties";
		String propertyFile = System.getProperty("user.dir") + "\\conf\\application.conf";

		FileInputStream fis = new FileInputStream(propertyFile);
		// InputStream fis = Config.class.getResourceAsStream(propertyFile);

		Properties properties = new Properties();
		properties.load(fis);
		fis.close();

		// checking to see if the address has quotation marks in the beginning
		// or end and then remove them.
		// the application.conf of play requires the quotation marks if there is
		// a : in the addresses.
		gate_home = properties.getProperty("GATEHOME").replace("\"", "");
		stanford_ner = properties.getProperty("STANFORDMODEL").replace("\"", "");
		opennlp_dir = properties.getProperty("OPENNLPDIR").replace("\"", "");
		lingpipe_dir = properties.getProperty("LINGPIPEMODEL").replace("\"", "");
		setMit_dir(properties.getProperty("MITMODEL").replace("\"", ""));

	}

	public Config(String gateAddress, String stanfordAddress, String openNlpDir, String lingPipeDir, String mitDir) {

		gate_home = gateAddress;
		stanford_ner = stanfordAddress;
		opennlp_dir = openNlpDir;
		lingpipe_dir = lingPipeDir;
		setMit_dir(mitDir);
	}

	public Config(boolean localAddress) {

		gate_home = "C:/Programs/gate-8.1-build5169-BIN";
		stanford_ner = "C:/Programs/Stanford/geovista-ner-model.ser.gz";
		opennlp_dir = "C:/Programs/openNlp";
		lingpipe_dir = "C:/Programs/lingpipe/ne-en-news-muc6.AbstractCharLmRescoringChunker";
		setMit_dir("C:/Programs/mit/ner_model.dat");
	}

	/**
	 * @return the gate_home
	 */
	public String getGate_home() {
		return gate_home;
	}

	/**
	 * @return the stanford_ner
	 */
	public String getStanford_ner() {
		return stanford_ner;
	}
	
	/**
	 * @return the opennlp_dir
	 */
	public String getOpenNlpDir() {
		return opennlp_dir;
	}
	
	/**
	 * @return the LINGPIPEHOME set in app.conf
	 */
	public String getLingPipeDir() {
		return lingpipe_dir;
	}
	
	public String getMit_dir() {
		return mit_model;
	}

	public void setMit_dir(String mit_dir) {
		this.mit_model = mit_dir;
	}

	// Test to see if you get the right addresses here.
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Config config = new Config();
		System.out.println(config.getGate_home());
		System.out.println(config.getStanford_ner());
	}

	
}
