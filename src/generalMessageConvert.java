import java.util.Scanner;
import java.util.regex.Pattern;

public class generalMessageConvert {

	public static void main(String args[]) {
		//String input = "<div class=\"message general you\" data-messageid=\"-L3dYCY4RFRFPvAYdgxz\">            <div class=\"spacer\"></div><div class=\"avatar\"><img src=\"./Chat Log for MCA Game_files/30(1)\"></div><span class=\"tstamp\">January 24, 2018 7:02PM</span><span class=\"by\">Streak:</span>      is there some way to pass control of this game to me      </div>                <div class=\"message general\" data-messageid=\"-L3dYEwWUf5UVc1DpN9u\">            <div class=\"spacer\"></div><div class=\"avatar\"><img src=\"./Chat Log for MCA Game_files/30\"></div><span class=\"tstamp\">January 24, 2018 7:02PM</span><span class=\"by\">Drufle (GM):</span>      oh right      </div>                <div class=\"message general\" data-messageid=\"-L3dYFJ-stPS4czE7O-J\">                  uhhhh      </div>                <div class=\"message general\" data-messageid=\"-L3dYFso0KgE1xuJFOoG\">                  one sec      </div>";
		String input = "</div>      </div>                <div class=\"message rollresult you player--KvmYPL0a9UeSuE8q7OX \" data-messageid=\"-LFTttbvkZDsm-RCDo42\" data-playerid=\"-KvmYPL0a9UeSuE8q7OX\">                  <div class=\"formula\" style=\"margin-bottom: 3px;\">                rolling 1d6      </div>      <div class=\"clear\"></div>      <div class=\"formula formattedformula\">        <div class=\"dicegrouping\" data-groupindex=\"0\">(<div data-origindex=\"0\" class=\"diceroll d6 critfail \"><div class=\"dicon\"><div class=\"didroll\">1</div><div class=\"backing\"></div></div></div>)</div>        <div class=\"clear\"></div>      </div>      <div class=\"clear\"></div>      <strong>        =      </strong>      <div class=\"rolled\">        1      </div>            </div>                <div class=\"message rollresult you player--KvmYPL0a9UeSuE8q7OX \" data-messageid=\"-LFTty8iSMfYfWVW7AEC\" data-playerid=\"-KvmYPL0a9UeSuE8q7OX\">            <div class=\"spacer\"></div><div class=\"avatar\"><img src=\"https://s3.amazonaws.com/files.d20.io/images/40261144/fiptn6Ca4HlG6QC63bPNeA/med.png?1507308009\"></div><span class=\"tstamp\">9:34PM</span><span class=\"by\">Blue Blueford:</span>      <div class=\"formula\" style=\"margin-bottom: 3px;\">                rolling 1d6+5      </div>      <div class=\"clear\"></div>      <div class=\"formula formattedformula\">        <div class=\"dicegrouping\" data-groupindex=\"0\">(<div data-origindex=\"0\" class=\"diceroll d6\"><div class=\"dicon\"><div class=\"didroll\">5</div><div class=\"backing\"></div></div></div>)</div>+5        <div class=\"clear\"></div>      </div>      <div class=\"clear\"></div>      <strong>        =      </strong>      <div class=\"rolled\">        10      </div>            </div>                <div class=\"message rollresult you player--KvmYPL0a9UeSuE8q7OX \" data-messageid=\"-LFTu0KDKNW634Z_1_gb\" data-playerid=\"-KvmYPL0a9UeSuE8q7OX\">            <div class=\"spacer\"></div><div class=\"avatar\"><img src=\"https://s3.amazonaws.com/files.d20.io/images/40261127/_wJypEZC2RVQtXya0JDNNQ/med.png?1507307983\"></div><span class=\"tstamp\">9:34PM</span><span class=\"by\">Red Redford:</span>      <div class=\"formula\" style=\"margin-bottom: 3px;\">                rolling 1d6-2      </div>      <div class=\"clear\"></div>      <div class=\"formula formattedformula\">        <div class=\"dicegrouping\" data-groupindex=\"0\">(<div data-origindex=\"0\" class=\"diceroll d6\"><div class=\"dicon\"><div class=\"didroll\">2</div><div class=\"backing\"></div></div></div>)</div>-2        <div class=\"clear\"></div>      </div>      <div class=\"clear\"></div>      <strong>        =      </strong>      <div class=\"rolled\">        0      </div>            </div>                <div class=\"message rollresult you player--KvmYPL0a9UeSuE8q7OX \" data-messageid=\"-LFTuAL4z2N9qmyBe3mF\" data-playerid=\"-KvmYPL0a9UeSuE8q7OX\">                  <div class=\"formula\" style=\"margin-bottom: 3px;\">                rolling 1d6+1d6-4      </div>      <div class=\"clear\"></div>      <div class=\"formula formattedformula\">        <div class=\"dicegrouping\" data-groupindex=\"0\">(<div data-origindex=\"0\" class=\"diceroll d6 critsuccess \"><div class=\"dicon\"><div class=\"didroll\">6</div><div class=\"backing\"></div></div></div>)</div>+<div class=\"dicegrouping\" data-groupindex=\"2\">(<div data-origindex=\"0\" class=\"diceroll d6 critsuccess \"><div class=\"dicon\"><div class=\"didroll\">6</div><div class=\"backing\"></div></div></div>)</div>-4        <div class=\"clear\"></div>      </div>      <div class=\"clear\"></div>      <strong>        =      </strong>      <div class=\"rolled\">        8      </div>            </div>    </div>";
		String output = "";
		
		//Create scanner with input and set delimiter.
		Scanner scan = new Scanner(input);
		scan.useDelimiter(Pattern.compile("[<>]"));
		
		//Continue scanning input until there is none left.
		while (scan.hasNext()) {
			//Scan the next line.
			String current = scan.next();
			//Check if this line is the start of a message.
			if (current.contains("div class=\"message general")) {
				//It is, so scan the next line, determining what
				//kind of message it is.
				current = scan.next();
				//Message type 1, new speaker, plain text.
				if (current.equals("            ")) {
					//Eat up input until the speaker name.
					while(!current.contains("span class=\"by\"")) {
						current = scan.next();
					}
					//Get name, write, then get message and write.
					current = scan.next();
					output += "--" + clean(current) + " ";
					current = scan.next();
					current = scan.next();
					output += clean(current) + "\n";
				}
				//Message type 2, continuing speaker, plain text.
				else {
					//Write message.
					output += "---" + clean(current) + "\n";
				}
			}
			//Check if this line is the start of a roll.
			else if (current.contains("div class=\"message rollresult")) {
				current = scan.next();
				while (clean(current).isEmpty()) {
					current = scan.next();
				}
				System.out.println("Current is '"+ current + "'");
				if (current.contains("div class=\"spacer\"")) {
					while(!current.contains("span class=\"by\"")) {
						current = scan.next();
					}
					current = scan.next();
					System.out.println("Adding name " + clean(current));
					output += "++" + clean(current) + " ";
					while(!current.contains("div class=\"formula\"")) {
						current = scan.next();
					}
					current = scan.next();
					System.out.println("Adding roll " + clean(current));
					output += clean(current) + " = ";
					while(!current.contains("div class=\"rolled\"")) {
						current = scan.next();
						if (current.contains("div class=\"didroll\"")) {
							current = scan.next();
							System.out.println("Adding result " + clean(current));
							output += clean(current);
						}
						else if ((current.contains("+") || current.contains("-")) && clean(current).length() <= 4) {
							System.out.println("Adding misc " + clean(current));
							output += clean(current);
						}
					}
					current = scan.next();
					System.out.println("Adding final " + clean(current));
					output += " = " + clean(current) + "\n";
				} else if (current.contains("div class=\"formula\"")) {
					System.out.println("Found new message");
					while(!current.contains("div class=\"formula\"")) {
						current = scan.next();
					}
					current = scan.next();
					System.out.println("Adding roll " + clean(current));
					output += "+++" + clean(current) + " = ";
					while(!current.contains("div class=\"rolled\"")) {
						current = scan.next();
						if (current.contains("div class=\"didroll\"")) {
							current = scan.next();
							System.out.println("Adding result " + clean(current));
							output += clean(current);
						}
						else if ((current.contains("+") || current.contains("-")) && clean(current).length() <= 4) {
							System.out.println("Adding misc " + clean(current));
							output += clean(current);
						}
					}
					current = scan.next();
					System.out.println("Adding final " + clean(current));
					output += " = " + clean(current) + "\n";
				}
			}
		}
		
		System.out.println("Output ---");
		System.out.print(output);
		scan.close();
	}
	
	private static String clean(String in) {
		String out = in;
		out = out.replaceAll("\\s{2,}","");
		return out;
	}
	
}
