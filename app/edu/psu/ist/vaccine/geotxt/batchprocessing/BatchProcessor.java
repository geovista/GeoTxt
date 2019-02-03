package edu.psu.ist.vaccine.geotxt.batchprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import edu.psu.ist.vaccine.geotxt.utils.Analyzer;
import edu.psu.ist.vaccine.analyzers.HierarchyAnalyzer;
import edu.psu.ist.vaccine.geotxt.entities.Location;
import edu.psu.ist.vaccine.geotxt.hierarchy.MapHierarchyPlaces;
import edu.psu.ist.vaccine.geotxt.ner.NamedEntities;
import edu.psu.ist.vaccine.geotxt.ner.NerEngines;
import edu.psu.ist.vaccine.geotxt.utils.Config;
import edu.psu.ist.vaccine.geotxt.utils.GeoJsonWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads in messages from a comma-delimited csv file and adds geocoding information. Just a first version, not documented and probably not robust yet.
 *
 * @author jow
 *
 */
public class BatchProcessor {

	public static final char separator = ',';
	public static final char quotechar = '"';
	public static final char escape = '\\';
	public static final int INITIAL_READ_SIZE = 128;
	private final boolean strictQuotes = false;
	private boolean inField = false;
	private String pending;
	private int columnWithText = 13;
	protected Analyzer analyzer;
	protected BufferedReader reader;
	Map<String, Object> context = new HashMap<String, Object>();

	public BatchProcessor(String filename) {
		// load configuration parameters from property file
		Config config = null;
		try {
			config = new Config();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BatchProcessor.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BatchProcessor.class.getName()).log(Level.SEVERE, null, ex);
		}

		MapHierarchyPlaces.username = "jow11"; // put your geonames user name here
		// System.out.println(config.getStanford_ner());
		analyzer = new HierarchyAnalyzer(config.getStanford_ner(), 50, 30);

		// analyzer = new StanfordHierarchyAnalyzer("/Volumes/Untitled/tmp/vaccine-project-GeoTextWeb/conf/classifiers/english.all.3class.caseless.distsim.crf.ser.gz", 50, 30);

		try {
			reader = new BufferedReader(new FileReader(filename));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Reads through csv file and produces new csv file with name given by filename.
	 */
	public void run(String filename) {
		BufferedWriter writer = null;

		// open output file
		try {
			writer = new BufferedWriter(new FileWriter(filename));

			// read header line
			String header = reader.readLine();
			writer.write(header + ",geojson\r\n");

			// line = reader.readLine();
			String line = "";
			while (line != null) {
				String[] result = null;
				String entireLine = "";
				do {
					line = reader.readLine();

					if (line == null) {
						break;
					}
					entireLine += line;

					String[] r = parseLine(line);
					if (r.length > 0) {
						System.out.println(r[0]);
					}
					if (r.length > 0) {
						if (result == null) {
							result = r;
						} else {
							String[] t = new String[result.length + r.length];
							System.arraycopy(result, 0, t, 0, result.length);
							System.arraycopy(r, 0, t, result.length, r.length);
							result = t;
						}
					}
				} while (pending != null);

				if (line != null) {
					System.out.println(result[columnWithText]);
					NamedEntities doc = analyzer.analyze(result[columnWithText], NerEngines.STANFORD, context);
					entireLine += ",\"" + toCSVString(GeoJsonWriter.docToGeoJson(doc, false, 0, false, false)) + "\"";

					if (doc != null && doc.locs != null) {
						for (Location l : doc.locs) {
							String x = "?";
							String y = "?";
							if (l.getGeometry() != null) {
								x = "" + l.getGeometry().getCoordinates()[0];
								y = "" + l.getGeometry().getCoordinates()[1];
							}
							entireLine += ",\"" + l.getName() + "\"," + x + "," + y;
						}
					}

					writer.write(entireLine + "\r\n");

					System.out.println("finished");
				}
			}
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	protected String toCSVString(String s) {
		return s.replace("\"", "\"\"");
	}

	protected String[] parseLine(String line) throws IOException {
		boolean multi = true;

		if (!multi && pending != null) {
			pending = null;
		}

		if (line == null) {
			if (pending != null) {
				String s = pending;
				pending = null;
				return new String[] { s };
			} else {
				return null;
			}
		}

		List<String> tokensOnThisLine = new ArrayList<String>();
		StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
		boolean inQuotes = false;
		if (pending != null) {
			sb.append(pending);
			pending = null;
			inQuotes = true;
		}
		for (int i = 0; i < line.length(); i++) {

			char c = line.charAt(i);
			if (c == this.escape) {
				if (isNextCharacterEscapable(line, inQuotes || inField, i)) {
					sb.append(line.charAt(i + 1));
					i++;
				}
			} else if (c == quotechar) {
				if (isNextCharacterEscapedQuote(line, inQuotes || inField, i)) {
					sb.append(line.charAt(i + 1));
					i++;
				} else {
					// inQuotes = !inQuotes;

					// the tricky case of an embedded quote in the middle: a,bc"d"ef,g
					if (!strictQuotes) {
						if (i > 2 // not on the beginning of the line
								&& line.charAt(i - 1) != this.separator // not at the beginning of an escape sequence
								&& line.length() > (i + 1) && line.charAt(i + 1) != this.separator // not at the end of an escape sequence
						) {

							// if (false && sb.length() > 0 && isAllWhiteSpace(sb)) {
							// sb.setLength(0); //discard white space leading up to quote
							// } else {
							sb.append(c);
							// continue;
							// }

						}
					}

					inQuotes = !inQuotes;
				}
				inField = !inField;
			} else if (c == separator && !inQuotes) {
				tokensOnThisLine.add(sb.toString());
				sb.setLength(0); // start work on next token
				inField = false;
			} else {
				if (!strictQuotes || inQuotes) {
					sb.append(c);
					inField = true;
				}
			}
		}
		// line is done - check status
		if (inQuotes) {
			if (multi) {
				// continuing a quoted section, re-append newline
				sb.append("\n");
				pending = sb.toString();
				sb = null; // this partial content is not to be added to field list yet
			} else {
				throw new IOException("Un-terminated quoted field at end of CSV line");
			}
		}
		if (sb != null) {
			tokensOnThisLine.add(sb.toString());
		}
		return tokensOnThisLine.toArray(new String[tokensOnThisLine.size()]);

	}

	protected boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
		return inQuotes // we are in quotes, therefore there can be escaped quotes in here.
				&& nextLine.length() > (i + 1) // there is indeed another character to check.
				&& nextLine.charAt(i + 1) == quotechar;
	}

	protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
		return inQuotes // we are in quotes, therefore there can be escaped quotes in here.
				&& nextLine.length() > (i + 1) // there is indeed another character to check.
				&& (nextLine.charAt(i + 1) == quotechar || nextLine.charAt(i + 1) == this.escape);
	}

	/*
	 * Read from phoenix text file
	 */
	public void run2(String filename) {
		BufferedWriter writer = null;

		// open output file
		try {
			writer = new BufferedWriter(new FileWriter(filename));

			String line = reader.readLine();
			writer.write(line + "\tGeoTxt-GeoJSON\tGeoTxt-Readable\r\n");

			line = reader.readLine();
			while (line != null) {
				String output = line + "\t";

				int index = line.indexOf("\t");
				int number = Integer.parseInt(line.substring(0, index));

				if (number <= 131592) {
					line = reader.readLine();
					continue;
				}

				String text = line.substring(index + 1);

				if (!text.trim().equals("")) {
					NamedEntities doc = analyzer.analyze(line, NerEngines.STANFORD, context);
					String result = GeoJsonWriter.docToGeoJson(doc, false, 0, false, false);
					output += result + "\t";

					if (doc != null && doc.locs != null) {
						String clear = "";
						for (Location l : doc.locs) {
							String x = "?";
							String y = "?";
							if (l.getGeometry() != null) {
								x = "" + l.getGeometry().getCoordinates()[0];
								y = "" + l.getGeometry().getCoordinates()[1];
								clear += l.getName() + " -> " + l.getGeometry().getToponym() + " (" + l.getGeometry().getGeoNameId() + ";" + x + "," + y + "), ";

							}
						}
						output += clear;

					}

				} else {
					output += "skipped empty input";
				}

				writer.write(output + "\r\n");
				line = reader.readLine();
			}

			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Read from uncc text file
	 */
	public void run3(String filename) {
		BufferedWriter writer = null;
		columnWithText = 5;

		// open output file
		try {
			writer = new BufferedWriter(new FileWriter(filename));

			// read header line
			String header = reader.readLine();
			writer.write(header + ",\"GeoTxt-GeoJSON\",\"GeoTxt-Readable\"\r\n");

			// line = reader.readLine();
			String line = "";
			while (line != null) {
				String[] result = null;
				String entireLine = "";
				do {
					line = reader.readLine();

					if (line == null) {
						break;
					}
					entireLine += line;

					String[] r = parseLine(line);
					if (r.length > 0) {
						System.out.println(r[0]);
					}
					if (r.length > 0) {
						if (result == null) {
							result = r;
						} else {
							String[] t = new String[result.length + r.length];
							System.arraycopy(result, 0, t, 0, result.length);
							System.arraycopy(r, 0, t, result.length, r.length);
							result = t;
						}
					}
				} while (pending != null);

				if (line != null) {
					System.out.println(result[columnWithText]);
					NamedEntities doc = analyzer.analyze(result[columnWithText], NerEngines.STANFORD, context);
					entireLine += ",\"" + toCSVString(GeoJsonWriter.docToGeoJson(doc, false, 0, false, false)) + "\"";
					entireLine += ",\"";

					if (doc != null && doc.locs != null) {
						String clear = "";
						for (Location l : doc.locs) {
							String x = "?";
							String y = "?";
							if (l.getGeometry() != null) {
								x = "" + l.getGeometry().getCoordinates()[0];
								y = "" + l.getGeometry().getCoordinates()[1];
								clear += l.getName() + " -> " + l.getGeometry().getToponym() + " (" + l.getGeometry().getGeoNameId() + ";" + x + "," + y + "), ";
							}

						}

						entireLine += clear;
					}

					entireLine += "\"";

					writer.write(entireLine + "\r\n");

					System.out.println("finished");
				}
			}
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Read from uncc text file
	 */
	public void run4(String filename) {
		BufferedWriter writer = null;
		columnWithText = 1;

		// open output file
		try {
			writer = new BufferedWriter(new FileWriter(filename));

			// read header line
			String header = reader.readLine();
			writer.write(header + ",\"GeoTxt-GeoJSON\",\"GeoTxt-Readable\"\r\n");

			// line = reader.readLine();
			String line = "";
			int count = 0;
			while (line != null && count < 50) {
				String[] result = null;
				String entireLine = "";
				do {
					line = reader.readLine();

					if (line == null) {
						break;
					}
					entireLine += line;

					String[] r = parseLine(line);
					if (r.length > 0) {
						System.out.println(r[0]);
					}
					if (r.length > 0) {
						if (result == null) {
							result = r;
						} else {
							String[] t = new String[result.length + r.length];
							System.arraycopy(result, 0, t, 0, result.length);
							System.arraycopy(r, 0, t, result.length, r.length);
							result = t;
						}
					}
				} while (pending != null);

				if (line != null) {
					System.out.println(result[columnWithText]);
					NamedEntities doc = analyzer.analyze(result[columnWithText], NerEngines.STANFORD, context);
					entireLine += ",\"" + toCSVString(GeoJsonWriter.docToGeoJson(doc, false, 0, false, false)) + "\"";
					entireLine += ",\"";

					if (doc != null && doc.locs != null) {
						String clear = "";
						for (Location l : doc.locs) {
							String x = "?";
							String y = "?";
							if (l.getGeometry() != null) {
								x = "" + l.getGeometry().getCoordinates()[0];
								y = "" + l.getGeometry().getCoordinates()[1];
								clear += l.getName() + " -> " + l.getGeometry().getToponym() + " (" + l.getGeometry().getGeoNameId() + ";" + x + "," + y + "), ";
							}

						}

						entireLine += clear;
					}

					entireLine += "\"";

					writer.write(entireLine + "\r\n");
					count++;
					System.out.println("finished");
				}
			}
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BatchProcessor bp = new BatchProcessor("/Users/jow/Downloads/data.csv");

		// bp.run("Maps_forum_posts+thread_titles_geocoded_3March14.csv");
		bp.run4("first50_geocoded.csv");
	}
}
