package edu.psu.ist.vaccine.geotxt.utils;
import java.util.ArrayList;

/**
 * THIS IS DIFFERENT FROM THE ONE IN GEOTXT. ENHANCED BASED ON THAT. Provides
 * auxiliary methods to pre-process a text message before feeding it into a NER
 * tool.
 * 
 * @author jow
 * @author Morteza
 */

public class HashtagProcessor {

	private String hashtagRemoved;
	private ArrayList<Integer> hashtagCharIndexes;
	private ArrayList<Integer> capitalCharIndexes;

	public HashtagProcessor( ){
		this.hashtagRemoved ="";
		this.hashtagCharIndexes = new ArrayList<Integer>();
		this.capitalCharIndexes = new ArrayList<Integer>();
	}

	public HashtagProcessor(String hashtagsRemoved, ArrayList<Integer> hashtagCharIndexes, ArrayList<Integer> capitalCharIndexes) {
		this.hashtagRemoved = hashtagsRemoved;
		this.hashtagCharIndexes = hashtagCharIndexes;
		this.capitalCharIndexes = capitalCharIndexes;
	}

	public String getHashtagRemoved() {
		return hashtagRemoved;
	}

	public ArrayList<Integer> getHashtagCharIndexes() {
		return hashtagCharIndexes;
	}

	public ArrayList<Integer> getCapitalCharIndexes() {
		return capitalCharIndexes;
	}

	/**
	 * Filters out # and splits hash tags where an upper-case letter follow an
	 * lower-case letter in a hash tag. returns the resulting string, charindex
	 * of hashtags and charindex of capital letters.
	 * 
	 * @param text
	 *            message to be pre-processed
	 * @return message with # removed and split hash tags
	 */
	public static HashtagProcessor processHashTags(String text) {
		
		int capsCount = 0;

		ArrayList<Integer> hashtagCharIndexes = new ArrayList<Integer>();
		ArrayList<Integer> capitalCharIndexes = new ArrayList<Integer>();

		boolean newWord = true;
		boolean hashtag = false;
		boolean previousIsLowerCase = false;

		char[] newString = new char[text.length() * 2];

		int count = 0;
		for (int i = 0; i < text.length(); i++) {
			
			char c = text.charAt(i);

			if (Character.isWhitespace(c)) { // new words starts
				newWord = true;
				hashtag = false;
				newString[count] = c;
				count++;
			} else {
				if (newWord && c == '#') { // new hashtag starts here; skip #
					hashtag = true;
					hashtagCharIndexes.add(i);
				} else if (hashtag && previousIsLowerCase && Character.isUpperCase(c)) { // split
					capitalCharIndexes.add(i);
					capsCount++;
					newString[count] = ' ';
					newString[count + 1] = c;
					count += 2;
				} else {
					newString[count] = c;
					count++;
				}
				newWord = false;
			}
			previousIsLowerCase = Character.isLowerCase(c);
		}

//		System.out.println(capsCount);
//		System.out.println(hashtagCharIndexes);
//		System.out.println(capitalCharIndexes);

		HashtagProcessor processed = new HashtagProcessor(new String(newString, 0, count), hashtagCharIndexes, capitalCharIndexes);

		return processed;

	}

	public static final void main(String[] args) {

		System.out.println(">" + processHashTags("RT @shadihamid: <LOCATION>US</LOCATION> strikes in #<LOCATION>Syria</LOCATION> have badly undermined").getHashtagCharIndexes() + "<");
	}
}
