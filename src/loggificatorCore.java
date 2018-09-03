import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Notes:
 * 	- We need to examine inline rolls and figure out how to deal with those, but first...
 *  - Macros are indicated as a normal message, so we'll have to intercept that and scan for a macro
 *    first, then deal with it accordingly.
 *  - We have to make it output to a text file, as well.
 *  - Then we need to build our final HTML.
 * @author Streak
 */

public class loggificatorCore {

	private String campaignName = "wotf";
	private String sessionName = "The Happening";
	private int sessionNumber = 1;
	private Scanner scan;
	private String current;
	private String output;
	private String htmlOutput;
	private boolean verbose = false;
	
	/**
	 * The constructor sets up the scanner, opening input.txt, and then
	 * starts processing the output. It will then write the output to a
	 * file, then convert this output to a final log.
	 */
	public loggificatorCore() {
		try {
			String r = readFile("input.txt", Charset.defaultCharset());
			scan = new Scanner(r);
		} catch (IOException e) {
			e.printStackTrace();
		}
		scan.useDelimiter(Pattern.compile("[<>]"));
		current = "";
		output = "";
		System.out.println("Converting input to raw...");
		createOutput();
		System.out.println("Saving raw...");
		saveRaw();
		scan.close();
		scan = new Scanner(output);
		htmlOutput = "";
		System.out.println("Converting raw to HTML...");
		createHTML();
		System.out.println("Saving HTML...");
		saveHTML();
		System.out.println("Complete!");
	}
	
	private void saveHTML() {
		try {
			PrintWriter out = new PrintWriter("output-html.html");
			out.print(htmlOutput);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts raw text log into a HTML log. 
	 */
	private void createHTML() {
		addHTMLHeader();
		processRaw();
		addHTMLFooter();
	}

	/**
	 * 
	 */
	private void processRaw() {
		boolean firstMessage = true;
		boolean macroInProgress = false;
		int i = 0;
		while (scan.hasNextLine()) {
			i++;
			current = scan.nextLine();
			if (current.length() < 3) {
				continue;
			}
			if (current.substring(0, 3).equals("---")) {
				if (verbose)
					System.out.println("Processing ---, Current = '"+current+"'");
//				if (macroInProgress) {
//					macroInProgress = false;
//					htmlOutput += "</table></div>";
//				}
				processContinuingMessage();
			}
			else if (current.substring(0, 2).equals("--")) {
				if (verbose)
					System.out.println("Processing --, Current = '"+current+"'");
				if (!firstMessage) {
					htmlOutput += "\t\t</div>\n";
					htmlOutput += "\t</div>\n";
				}
				processNewMessage();
				firstMessage = false;
			}
			else if (current.substring(0, 2).equals("-#")) {
				if (!firstMessage) {
					htmlOutput += "\t\t</div>\n";
					htmlOutput += "\t</div>\n";
				}
				if (verbose)
					System.out.println("Processing ###, Current = '"+current+"'");
				processPreMacro();
				macroInProgress = true;
			}
			else if (current.substring(0, 3).equals("###")) {
				if (verbose)
					System.out.println("Processing ###, Current = '"+current+"'");
				processContinuingMacro();
				macroInProgress = true;
			}
			else if (current.substring(0, 2).equals("##")) {
				if (verbose)
					System.out.println("Processing ##, Current = '"+current+"'");
				processNewMacro();
				macroInProgress = true;
			}
			else if (current.substring(0, 2).equals("@@")) {
				if (verbose)
					System.out.println("Processing @@, Current = '"+current+"'");
				
				processEmote();
			}
			else if (current.substring(0, 3).equals("+++")) {
				if (verbose)
					System.out.println("Processing +++, Current = '"+current+"'");
				
				processContinuingRoll();
			}
			else if (current.substring(0, 2).equals("++")) {
				if (verbose)
					System.out.println("Processing ++, Current = '"+current+"'");
				
				if (!firstMessage) {
					htmlOutput += "\t\t</div>\n";
					htmlOutput += "\t</div>\n";
				}
				processNewRoll();
				firstMessage = false;
			}
		}
	}

	private void processPreMacro() {
		String name = getSpeakerName();
		setUpNewSpeaker(name);
	}

	private void processNewMacro() {
		if (current.contains(":")) {
			int i = 2;
			while (current.charAt(i) != ':') {
				i++;
			}
			htmlOutput += "<br><table class=\"macro-table\"><tr><td class=\"macro-header\">";
			htmlOutput += current.substring(2, i+1);
			htmlOutput += "</td><td class=\"macro-data\">";
			htmlOutput += current.substring(i+1);
			htmlOutput += "</td></tr>";
		} 
		else {
			htmlOutput += "<br><table class=\"macro-table\"><tr><td colspan=\"2\" class=\"macro-data\">" + current + "</td></tr>";
		}
	}

	private void processContinuingMacro() {
		if (current.contains(":")) {
			int i = 3;
			while (current.charAt(i) != ':') {
				i++;
			}
			htmlOutput += "<tr><td class=\"macro-header\">";
			htmlOutput += current.substring(3, i+1);
			htmlOutput += "</td><td class=\"macro-data\">";
			htmlOutput += current.substring(i+1);
			htmlOutput += "</td></tr>";
		} 
		else {
			htmlOutput += "<tr><td colspan=\"2\" class=\"macro-data\">" + current.substring(3) + "</td></tr>";
		}
	}

	private void processContinuingRoll() {
		htmlOutput += "\t\t\t<br><span class=\"roll\">" + current.substring(3) + "</span>\n";
		
	}

	private void processNewRoll() {
		String name = getSpeakerName();
		int i = 2;
		while (current.charAt(i) != ':') {
			i++;
		}
		i++;
		setUpNewSpeaker(name);
		htmlOutput += "\t\t<div class=\"message\">\n";
		htmlOutput += "\t\t\t<span class=\"roll\">" + current.substring(i) + "</span>\n";
	}

	private void setUpNewSpeaker(String name) {
		htmlOutput += "\t<div class=\"message-body\">\n";
		htmlOutput += "\t\t<div class=\"ava-box\">\n";
		htmlOutput += "\t\t\t<div class=\"avatar\">\n";
		htmlOutput += "\t\t\t\t<img src=\"img/" + name + ".png\" onerror=\"this.src='noHead.png';\"/>\n";
		htmlOutput += "\t\t\t</div>";
		htmlOutput += "\t\t\t<div class=\"speaker-name\">\n";
		htmlOutput += "\t\t\t\t<span>" + name + "</span>\n";
		htmlOutput += "\t\t\t</div>\n";
		htmlOutput += "\t\t</div>\n";
		
	}

	private void processEmote() {
		htmlOutput += "\t\t\t<br><span class=\"emote\">" + current.substring(2) + "</span>\n";
	}

	private void processNewMessage() {
		String name = getSpeakerName();
		String inlinedCurrent = convertInlineRolls(2);
		int i = 2;
		while (inlinedCurrent.charAt(i) != ':') {
			i++;
		}
		i++;
		String macrodCurrent = convertMacros(inlinedCurrent.substring(i));
		setUpNewSpeaker(name);
		htmlOutput += "\t\t<div class=\"message\">\n";
		htmlOutput += "\t\t\t<span class=\"normal\">" + macrodCurrent + "</span>\n";
	}

	private String convertMacros(String s) {
		int i = 0;
		String htmlOutput = "";
		while (i < s.length()) {
			if (s.charAt(i) == '#' && s.charAt(i+1) == '#' && s.charAt(i+2) == '#') {
				i = i+3;
				String segment = "";
				while (i < s.length() && s.charAt(i) != '#') {
					segment += s.charAt(i);
					i++;
				}
				if (segment.contains(":")) {
					int j = 0;
					while (segment.charAt(j) != ':') {
						j++;
					}
					htmlOutput += "<tr><td class=\"macro-header\">";
					htmlOutput += segment.substring(0, j+1);
					htmlOutput += "</td><td class=\"macro-data\">";
					htmlOutput += segment.substring(j+1);
					htmlOutput += "</td></tr>";
				}
				else {
					htmlOutput += "<tr><td colspan=\"2\" class=\"macro-data\">" + segment + "</td></tr>";
				}
			}
			else if (s.charAt(i) == '#' && s.charAt(i+1) == '#'){
				htmlOutput += "<table class=\"macro-table\">";
				i = i+2;
				String segment = "";
				while (i < s.length() && s.charAt(i) != '#') {
					segment += s.charAt(i);
					i++;
				}
				if (segment.contains(":")) {
					int j = 0;
					while (segment.charAt(j) != ':') {
						j++;
					}
					htmlOutput += "<tr><td class=\"macro-header\">";
					htmlOutput += segment.substring(0, j+1);
					htmlOutput += "</td><td class=\"macro-data\">";
					htmlOutput += segment.substring(j+1);
					htmlOutput += "</td></tr>";
				}
				else {
					htmlOutput += "<tr><td colspan=\"2\" class=\"macro-data\">" + segment + "</td></tr>";
				}
			}
			
			else {
				htmlOutput += s.charAt(i);
				i++;
			}
		}
		htmlOutput += "</table>";
		return htmlOutput;
	}

	private String getSpeakerName() {
		String name = "";
		int i = 2;
		while (current.charAt(i) != ':') {
			name += current.charAt(i);
			i++;
		}
		return name;
	}

	private void processContinuingMessage() {
		String inlinedCurrent = convertInlineRolls(3);
		String macrodCurrent = convertMacros(inlinedCurrent);
		htmlOutput += "\t\t\t<br><span class=\"normal\">" + macrodCurrent + "</span>\n";
	}

	private String convertInlineRolls(int prefixSize) {
		String s = current;
		String out = "";
		int i = prefixSize;
		while (i < s.length()) {
			if (s.charAt(i) == '+' && s.charAt(i+1) == '{' && s.charAt(i+2) == '[') {
				out += "<span class=\"inline-roll\">";
				int j = i+2;
				while (s.charAt(j) != '}') {
					out += s.charAt(j);
					j++;
				}
				out += "</span>";
				i = j+1;
			}
			else {
				out += s.charAt(i);
				i++;
			}
		}
		return out;
	}

	private void addHTMLHeader() {
		htmlOutput += "<html>\n";
		htmlOutput += "<head>\n";
		htmlOutput += "\t<meta charset=\"UTF-8\">\n";
		htmlOutput += "\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0\"/>\n";
		htmlOutput += "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"normalise.css\"/>";
		htmlOutput += "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + campaignName + ".css\"/>";
		htmlOutput += "\t<title>Session "+ sessionNumber + ": "+ sessionName +"</title>";
		htmlOutput += "</head>";
		htmlOutput += "<body>";
	}
	
	private void addHTMLFooter() {
		htmlOutput += "</body>";
		htmlOutput += "</html>";
	}

	private void saveRaw() {
		try {
			PrintWriter out = new PrintWriter("output-raw.txt");
			out.print(output);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the processing, processing the file until there is nothing
	 * left to process. When this completes, the output field will contain
	 * the fully processed output.
	 */
	public void createOutput() {
		//Continue scanning input until there is none left.
		while (scan.hasNext()) {
			processBaseMessage();
		}
		
		//TODO: Test output outputting. Isn't that a tongue twister?
//		System.out.println("Output ---");
//		System.out.print(output);
	}
	
	/**
	 * This is the top level of message processing, making further processing
	 * decisions based on what it finds.
	 */
	private void processBaseMessage() {
		//Scan the next line.
		current = scan.next();
		//Check if this line is the start of a normal message.
		if (current.contains("div class=\"message general")) {
			processNormalMessage();
		}	
		//Check if this line is the start of a roll message.
		else if (current.contains("div class=\"message rollresult")) {
			processRollMessage();
		}
		//Check if this line is the start of an emote message.
		else if (current.contains("div class=\"message emote")) {
			processEmoteMessage();
		}
	}

	/**
	 * Processes a /me message. This is pretty simple. If affects output, adding something
	 * like
	 * @@Speaker spoke.
	 */
	private void processEmoteMessage() {
		current = scan.next();
		while (!current.contains("div class=\"spacer")) {
			current = scan.next();
		}
		current = scan.next();
		current = scan.next();
		current = scan.next();
		output += "@@" + clean(current) + "\n";
	}

	/**
	 * Processes a roll message. This deals with a message that is just
	 * a roll. This affects output, adding something like
	 * ++Speaker: rolling 1d6 = 3 = 3
	 * or 
	 * +++rolling 1d6+2 = 3+2 = 5
	 */
	private void processRollMessage() {
		//Get the next line.
		current = scan.next();
		//Consume input until we get a non-empty line.
		while (clean(current).isEmpty()) {
			current = scan.next();
		}
		//If input indicates a name needs read...
		if (current.contains("div class=\"spacer\"")) {
			//Consume input until we reach the name.
			while(!current.contains("span class=\"by\"")) {
				current = scan.next();
			}
			//Grab the name line and add it to output.
			current = scan.next();
			output += "++" + clean(current) + " ";
			//Now process the roll itself.
			processRoll();
		} 
		//Else we can just process the roll itself.
		else if (current.contains("div class=\"formula\"")) {
			//Gotta add that prefix first, though.
			output += "+++";
			processRoll();
		}
	}

	/**
	 * Processes a roll. This is part of the processRollMessage process,
	 * only dealing with the actual roll part of the message. This affects
	 * output, adding something like
	 * rolling 1d6+5 = 3+5 = 8
	 */
	private void processRoll() {
		//Consume input until we hit the roll declaration.
		while(!current.contains("div class=\"formula\"")) {
			current = scan.next();
		}
		//Get the roll declaration and add it to input.
		current = scan.next();
		output += clean(current) + " = ";
		//Process input until we hit the final roll result
		while(!current.contains("div class=\"rolled\"")) {
			current = scan.next();
			//If we find a dice roll...
			if (current.contains("div class=\"didroll\"")) {
				//Grab the number and add it to output.
				current = scan.next();
				output += clean(current);
			}
			//If we find a modifier...
			else if ((current.contains("+") || current.contains("-")) && clean(current).length() <= 4) {
				//Add it to output.
				output += clean(current);
			}
		}
		//Grab the roll result and add it to output.
		current = scan.next();
		output += " = " + clean(current) + "\n";
	}

	/**
	 * Processes a normal message. This is a message that is just text,
	 * devoid of any rolls. However, it may be a macro, so that is checked and dealt with.
	 * The output will be updated, creating a line of the format
	 * --Speaker: message
	 * or
	 * ---message
	 * or if it is a macro, it will be split into multiple lines, with ## or ### prefixes.
	 */
	private void processNormalMessage() {
		//Get the next line.
		current = scan.next();
		//Consume input until we get a non-empty line.
		while (clean(current).isEmpty()) {
			current = scan.next();
		}
		//If we have to process a name...
		if (current.contains("div class=\"spacer\"")) {
			//Consume input until we reach the name.
			while(!current.contains("span class=\"by\"")) {
				current = scan.next();
			}
			String tempOutput = "";
			//Get name, write, then get message and write.
			current = scan.next();
			output += "--" + clean(current) + " ";
			//tempOutput += clean(current) + " ";
			current = scan.next();
			current = scan.next();
			output += clean(current);
			//tempOutput += clean(current);
			current = scan.next();
			//If the message is a macro, process it and add it to output.
			if (current.contains("div class=\"sheet-rolltemplate")) {
				//output += "-#" + tempOutput;
				output += processMacro();
			} 
			//Otherwise, process the rest of the message, checking for any inline rolls.
			else {
				//output += "--" + tempOutput;
				processInlineRolls();
			}
		}
		//Otherwise, check if the message is a macro, and process it if it is.
		else if (current.contains("div class=\"sheet-rolltemplate")) {
			output += "---";
			output += processMacro();
		}
		//Else, write the message, checking for any inline rolls.
		else {
			output += "---";
			processInlineRolls();
		}
	}

	/**
	 * Processes a macro. This is very specific to the standard roll20 sheet's macro, so it's
	 * prone to failure if it's used on anything else. In any case, it will scan through the macro
	 * block, extracting any text that isn't the interior of a tag. It creates a newline where it
	 * believes it should, as well. This returns a string of multiple lines that look something like
	 * ##Speaker: Spell Name
	 * ###Damage: 15
	 * ###Attack: 21
	 * @return
	 */
	private String processMacro() {
		int counter = 1;
		String result = "##";
		while (counter != 0) {
			current = scan.next();
			if (current.contains("div class=\"sheet-roll-cell")) {
				counter++;
				result += "###";
			}
			else if (current.contains("div class")) {
				counter++;
			}
			else if (current.contains("div style")) {
				counter++;
			}
			else if (current.contains("/div")) {
				counter--;
			}
			else if (current.contains("span class=\"sheet-right\"")) {
				result += ":";
			}
			else if (current.equals("em")) {
				result += "<i>";
			}
			else if (current.equals("/em")) {
				result += "</i>";
			}
			else if (checkForMiscTags()) {
				//Do nothing
			}
			else if (current.equals("br") || current.equals("/label") 
					|| current.equals("/h4") || current.equals("/h3")) {
				result += "###";
			}
			else {
				result += clean(current);
			}
		}
		result += "\n";
		return result;
	}

	/**
	 * A helper method for macro processing. This method defines which tokens are
	 * tags, since the <> brackets are consumed during tokenisation, this has to be hardcoded.
	 * @return true if the current token is a tag, false otherwise
	 */
	private boolean checkForMiscTags() {
		if (current.contains("span class")) {
			return true;
		} 
		else if (current.contains("a href")) {
			return true;
		}
		else if (current.contains("span data")) {
			return true;
		}
		else if (current.equals("strong")) {
			return true;
		}
		else if (current.equals("div")) {
			return true;
		}
		else if (current.contains("/a")) {
			return true;
		}
		else if (current.contains("/span")) {
			return true;
		}
		else if (current.contains("/strong")) {
			return true;
		}
		else if (current.contains("img src")) {
			return true;
		}
		else if (current.equals("h3")) {
			return true;
		}
		else if (current.contains("h3 class")) {
			return true;
		}
		else if (current.equals("h4")) {
			return true;
		}
		else if (current.contains("h4 data")){
			return true;
		}
		else if (current.equals("label")) {
			return true;
		}
		else if (current.contains("label style")) {
			return true;
		}
		else if (!current.isEmpty() && current.charAt(0) == ')' && current.charAt(current.length()-1) == '\"') {
			current = scan.next();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Processes a normal message, looking for any inline rolls, until it finds the end of the message.
	 * If any inline rolls are found, they are scanned and formatted accordingly.
	 * -{[1d6+5] = 3+5 = 8}
	 * These are placed in the middle of the current line of input, so no newlines or prefixes are added here.
	 */
	private void processInlineRolls() {
		while (!current.contains("/div")) {
			if (current.contains("span class=\"inlinerollresult")) {
				String roll = getInlineRoll(current);
				output += "+{[" + roll + "] = ";
				current = scan.next();
				current = scan.next();
				output += current;
				current = scan.next();
				current = scan.next();
				roll = getInlineModifierOrResult(current);
				output += roll;
				current = scan.next();
				output += " = " + current + "}";
				current = scan.next();
			}
			else {
				output += clean(current);
			}
			current = scan.next();
		}
		output += "\n";
	}

	/**
	 * Further helper function for inline processing. This gets the section after the [1d6] area,
	 * up until before the '='. This is seperated as it requires more specific scanning of the current
	 * token.
	 * @param s The current token.
	 * @return The processed output to add.
	 */
	private String getInlineModifierOrResult(String s) {
		int index = 0;
		char c = s.charAt(index);
		String result = "";
		while (index < s.length()) {
			c = s.charAt(index);
			if (c == '+' || c == '-') {
				result += s.charAt(index);
				while (s.charAt(index) != '\"'){
					index++;
					if (s.charAt(index) != '\"')
						result += s.charAt(index);
				}
			}
			index++;
		}
		return result;
	}

	/**
	 * Helper function for inline rolls. This scans the token more in-depth to get the roll used.
	 * This results in something like '1d6+6'.
	 * @param s The current token.
	 * @return The processed output to add.
	 */
	private String getInlineRoll(String s) {
		int index = 0;
		char c = s.charAt(index);
		while (!Character.isDigit(c)) {
			index++;
			c = s.charAt(index);
		}
		int start = index;
		while (c != ' ') {
			index++;
			c = s.charAt(index);
		}
		return s.substring(start, index);
	}

	/**
	 * Takes a string and removes all whitespace from it. Whitespace is defined
	 * as a sequence of space characters that is at least 2 in length, so spaces
	 * between words aren't affected.
	 * @param in input to clean up
	 * @return lovely polished output
	 */
	private String clean(String in) {
		String out = in;
		out = out.replaceAll("\\s{2,}","");
		if (out.contains("&nbsp;")) {
			out = out.replace("&nbsp;", "");
		}
		return out;
	}
	
	/**
	 * Reads a file and turns it into a single string.
	 * @param path Path to the file
	 * @param encoding encoding to use
	 * @return a String containing the file's contents
	 * @throws IOException if the file can't be found, usually.
	 */
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
		
}
