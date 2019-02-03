package edu.psu.ist.vaccine.geotxt.utils;

/**
 * Not in use?
 * Provides auxiliary methods to pre-process a text message before feeding it into a NER tool.
 * 
 * @author jow
 */
public class TextPreprocessing {

	/**
	 * Filters out # and splits hash tags where an upper-case letter follow an lower-case letter
	 * in a hash tag.
	 * 
	 * @param text message to be pre-processed
	 * @return message with # removed and split hash tags
	 */
	public static String processHashTags(String text) {

		boolean newWord = true;
		boolean hashtag = false;
		boolean previousIsLowerCase = false;

		char[] newString = new char[text.length()*2];
		
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
				} else if (hashtag && previousIsLowerCase && Character.isUpperCase(c) ) { // split
					newString[count] = ' ';
					newString[count+1] = c;
					count += 2;
				} else { 
					newString[count] = c;
					count++;
				}
				newWord = false;
			}
			previousIsLowerCase = Character.isLowerCase(c);
		}

		return new String(newString,0,count);
	}

	public static final void main(String[] args) {
		
		// some test cases
		
		System.out.println(">" + processHashTags("I live in #NewYork, I like it there.") + "<");
		System.out.println(">" + processHashTags("#NewYork is great.") + "<");
		System.out.println(">" + processHashTags("#YaYaYa #") + "<");
		System.out.println(">" + processHashTags("#NY #") + "<");
		System.out.println(">" + processHashTags("#PrayForGaza") + "<");
	}
}
