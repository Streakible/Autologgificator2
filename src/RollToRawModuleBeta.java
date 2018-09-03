import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RollToRawModuleBeta {

	private Scanner scan;
	private String current;
	private String output;
	
	public RollToRawModuleBeta(Scanner scan) {
		this.scan = scan;
		current = "";
		output = "";
	}
	
	public String toString() {
		return current;
	}
	
	public void saveOutputToFile(String filename) {
		try {
			PrintWriter out = new PrintWriter(filename);
			out.print(output);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String createOutput() {
		//Continue scanning input until there is none left.
		while (scan.hasNext()) {
			processBaseMessage();
		}
		return output;
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
			//Get name, write, then get message and write.
			current = scan.next();
			output += "--" + clean(current) + " ";
			current = scan.next();
			current = scan.next();
			output += clean(current);
			current = scan.next();
			//If the message is a macro, process it and add it to output.
			if (current.contains("div class=\"sheet-rolltemplate")) {
				output += processMacro();
			} 
			//Otherwise, process the rest of the message, checking for any inline rolls.
			else {
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
				//System.out.println("**current is '" + current + "', found case 'div class=\"sheet-roll-cell', adding '###'");
				counter++;
				result += "###";
			}
			else if (current.contains("span class=\"inlinerollresult")) {
				System.out.println("getInlineRoll with current '"+current+"'");
				String roll = getInlineRoll(current);
				System.out.println("Adding +{[" + roll + "] = ");
				result += "+{[" + roll + "] = ";
				current = scan.next();
				System.out.println("adding '"+roll(current)+"'");
				result += roll(current);
				current = scan.next();
				result += roll(current);
				System.out.println("adding '"+roll(current)+"'");
				current = scan.next();
				result += roll(current);
				System.out.println("skipping '"+current+"'");
				current = scan.next();
				roll = getInlineModifierOrResult(current);
				System.out.println("adding '"+roll+"'");
				result += roll;
				current = scan.next();
				if (!current.contains("span class")) {
					System.out.println("adding ' = " + roll(current) + "}'");
					result += " = " + roll(current) + "}";
				}
				else {
					System.out.println("skipping '"+current+"'");
					result += roll(current);
					current = scan.next();
					System.out.println("adding ' = " + roll(current) + "}'");
					result += " = " + roll(current) + "}";
				}
				current = scan.next();
			}
			else if (current.contains("div class")) {
				//System.out.println("current is '" + current + "', found case 'div class', adding nothing");
				counter++;
			}
			else if (current.contains("div style")) {
				//System.out.println("current is '" + current + "', found case 'div style', adding nothing");
				counter++;
			}
			else if (current.contains("/div")) {
				//System.out.println("current is '" + current + "', found case '/div', adding nothing");
				counter--;
			}
			else if (current.contains("span class=\"sheet-right\"")) {
				//System.out.println("**current is '" + current + "', found case 'span class=\"sheet-right\"', adding ':'");
				result += ":";
			}
			else if (checkForMiscTags()) {
				//System.out.println("current is '" + current + "', found misc tag, adding nothing");
				//Do nothing
			}
			else if (current.equals("br") || current.equals("/label") 
					|| current.equals("/h4") || current.equals("/h3")) {
				//System.out.println("**current is '" + current + "', found case 'br, /label, /h4, /h3', adding '###'");
				result += "###";
			}
			else {
				//System.out.println("**current is '" + current + "', else case, adding '" + clean(current) + "'");
				result += clean(current);
			}
		}
		result += "\n";
		return result;
	}
	
	private String roll(String s) {
		System.out.println("######Processing a Roll...current is '"+s+"'");
		String out = "";
		int index = 0;
		while (index < s.length()) {
			char c = s.charAt(index);
			if (Character.isDigit(c) || c == '+' || c == '-') {
				out += c;
			}
			index++;
		}
		System.out.println("Roll result was '"+out+"'");
		return out;
	}

	/**
	 * A helper method for macro processing. This method defines which tokens are
	 * tags, since the <> brackets are consumed during tokenisation, this has to be hardcoded.
	 * @return true if the current token is a tag, false otherwise
	 */
	private boolean checkForMiscTags() {
		if (current.contains("span class")) {
			System.out.println("Misc tag found: span class");
			return true;
		} 
		else if (current.contains("a href")) {
			System.out.println("Misc tag found: a href");
			return true;
		}
		else if (current.contains("span data")) {
			System.out.println("Misc tag found: span data");
			return true;
		}
		else if (current.contains("span class")) {
			System.out.println("Misc tag found: span class");
			return true;
		}
		else if (current.equals("strong")) {
			System.out.println("Misc tag found: strong");
			return true;
		}
		else if (current.equals("div")) {
			System.out.println("Misc tag found: div");
			return true;
		}
		else if (current.contains("/a")) {
			System.out.println("Misc tag found: /a");
			return true;
		}
		else if (current.contains("/span")) {
			System.out.println("Misc tag found: /span");
			return true;
		}
		else if (current.contains("/strong")) {
			System.out.println("Misc tag found: /strong");
			return true;
		}
		else if (current.contains("img src")) {
			System.out.println("Misc tag found: img src");
			return true;
		}
		else if (current.equals("h3")) {
			System.out.println("Misc tag found: h3");
			return true;
		}
		else if (current.contains("h3 class")) {
			System.out.println("Misc tag found: h3 class");
			return true;
		}
		else if (current.equals("h4")) {
			System.out.println("Misc tag found: h4");
			return true;
		}
		else if (current.contains("h4 data")){
			System.out.println("Misc tag found: h4 data");
			return true;
		}
		else if (current.equals("label")) {
			System.out.println("Misc tag found: label");
			return true;
		}
		else if (current.contains("label style")) {
			System.out.println("Misc tag found: label style");
			return true;
		}
//		else if (!current.isEmpty() && current.charAt(0) == ')' && current.charAt(current.length()-1) == '\"') {
//			System.out.println("Misc tag found: )....\"");
//			current = scan.next();
//			return true;
//		}
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
				String roll = getInlineRoll(clean(current));
				output += "+{[" + roll + "] = ";
				current = scan.next();
				current = scan.next();
				output += clean(current);
				current = scan.next();
				current = scan.next();
				roll = getInlineModifierOrResult(clean(current));
				output += roll;
				current = scan.next();
				output += " = " + clean(current) + "}";
				current = scan.next();
			}
			else if (current.equals("em")) {
				output += "<i>";
			}
			else if (current.equals("/em")) {
				output += "</i>";
			}
			else {
				output += clean(current);
			}
			current = scan.next();
		}
		output += "\n";
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
		while (c != ' ' && c != 'c') {
			index++;
			if (index == s.length())
				break;
			c = s.charAt(index);
		}
		return s.substring(start, index);
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
				while (index+1 < s.length() && s.charAt(index) != '\"'){
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
	
}
