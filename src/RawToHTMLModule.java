import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RawToHTMLModule {

	private Scanner scan;
	private String current;
	private String htmlOutput;
	private String campaignName;
	private String sessionName;
	private String sessionNumber;
	private boolean verbose = false;
	
	public RawToHTMLModule(Scanner scan, String campaignName, String sessionNumber, String sessionName) {
		this.scan = scan;
		current = "";
		htmlOutput = "";
		this.campaignName = campaignName;
		this.sessionName = sessionName;
		this.sessionNumber = sessionNumber;
	}
	
	public String toString() {
		return current;
	}
	
	/**
	 * Converts raw text log into a HTML log. 
	 */
	public void createHTML() {
		addHTMLHeader();
		processRaw();
		addHTMLFooter();
	}
	
	public void saveHTML(String filename) {
		try {
			PrintWriter out = new PrintWriter(filename);
			out.print(htmlOutput);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
	
	/**
	 * 
	 */
	private void processRaw() {
		boolean firstMessage = true;
		while (scan.hasNextLine()) {
			current = scan.nextLine();
			if (current.length() < 3) {
				continue;
			}
			if (current.substring(0, 3).equals("---")) {
				if (verbose)
					System.out.println("Processing ---, Current = '"+current+"'");
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
			if (i+2 < s.length() && s.charAt(i) == '+' && s.charAt(i+1) == '{' && s.charAt(i+2) == '[') {
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
	
	private String getSpeakerName() {
		String name = "";
		int i = 2;
		while (current.charAt(i) != ':') {
			name += current.charAt(i);
			i++;
		}
		return name;
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
	
}
