package edu.psu.ist.vaccine.geotxt.benchmark;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
 * Class for storing a problem instance.
 * @author jow
 *
 */
public class ProblemInstance {
	
	/**
	 * short name of the problem instance
	 */
	protected String name;
	
	/**
	 * list of places appearing in the text of this problem instance
	 */
	protected List<Place> places = new ArrayList<Place>();
	
	
	/**
	 * text of this problem instance
	 */
	protected String text = "";
	
	
	/**
	 * constructs a new instance by reading and parsing the description from is.
	 * This method will most likely raise an exception if the format of the input
	 * is not exactly as expected.
	 * @param is
	 */
	public ProblemInstance(InputStream is) {
		Scanner scanner = new Scanner(is);
		name = scanner.nextLine();
		String t = "";
		
		// read all location into instances of Place
		boolean location = false;
		do {
			location = false;
			t = scanner.nextLine();
			if (t.startsWith("location:\t")) {
				StringTokenizer st = new StringTokenizer(t.substring(10),"\t");
				places.add(new Place(st.nextToken(),
									 Integer.parseInt(st.nextToken()),
									 Integer.parseInt(st.nextToken()),
									 Double.parseDouble(st.nextToken()),
									 Double.parseDouble(st.nextToken()),
									 st.nextToken(),
									 st.nextToken()));
				location = true;
			}
		} while(location);
	
		text += t.substring("text:\t".length());
		
		while (scanner.hasNext()) {
			text += t;
		} 
		System.out.println("done");
	}
	
	public String toString() {
		String s = "ProblemInstance " + name + "\n";
		for (Place p : places) {
			s += "Location: " + p.toString() + "\n";
		}
		s += "text: " +  text + "\n";
		return s;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
