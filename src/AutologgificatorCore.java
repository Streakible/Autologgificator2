import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

public class AutologgificatorCore {
	
	Scanner rawScan;
	Scanner htmlScan;
	
	public AutologgificatorCore() {
		boolean loop = true;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Welcome to the Autologgificator 2.0! Where all your dreams come true. ~");
		System.out.println("Choose an option!");
		System.out.println("\t1 - Process Roll20 HTML to raw format.");
		System.out.println("\t2 - Process raw format to Final HTML.");
		System.out.println("\t3 - Exit.");
		while (loop) {
			System.out.print("Option: ");
			String input = scanner.nextLine();
			if (input.equals("1")) {
				System.out.println("Which txt file should be loaded? Enter it's name, minus the extension.");
				String fileIn = scanner.nextLine();
				System.out.println("Enter the output file's name, minus the extension.");
				String fileOut = scanner.nextLine();
				doRollToRaw(fileIn, fileOut);
			}
			else if (input.equals("2")) {
				System.out.println("Which raw txt file should be loaded? Enter it's name, minus the extension.");
				String fileIn = scanner.nextLine();
				System.out.println("Enter the output file's name, minus the extension.");
				String fileOut = scanner.nextLine();
				System.out.println("What campaign is this?");
				String campaignName = scanner.nextLine();
				System.out.println("Which session number is this?");
				String sessionNumber = scanner.nextLine();
				System.out.println("What's the session's name?");
				String sessionName = scanner.nextLine();
				doRawToHTML(fileIn, fileOut, campaignName, sessionNumber, sessionName);
			}
			else if (input.equals("3")) {
				loop = false;
				scanner.close();
				System.out.println("Bye bye, then!");
			}
			else {
				System.out.println("That ain't right! Try again.");
			}
		}
	}
	
	private void doRollToRaw(String fileIn, String fileOut) {
		rawScan = loadFileIntoScanner(fileIn + ".txt");
		rawScan.useDelimiter(Pattern.compile("[<>]"));
		RollToRawModule raw = new RollToRawModule(rawScan);
		System.out.println("Creating Raw Output...");
		try {
			raw.createOutput();
		} catch (Exception ex) {
			System.out.println("Error! Current was '" + raw.toString() + "'");
			ex.printStackTrace();
		}
		System.out.println("Saving Raw Output...");
		raw.saveOutputToFile(fileOut + ".txt");
		System.out.println("Raw output saved!");
	}
	
	private void doRawToHTML(String fileIn, 
							 String fileOut, 
							 String campaignName, 
							 String sessionName, 
							 String sessionNumber) {
		htmlScan = loadFileIntoScanner(fileIn + ".txt");
		RawToHTMLModule html = new RawToHTMLModule(htmlScan, campaignName, sessionNumber, sessionName);
		System.out.println("Creating HTML Output...(This might take a while!)");
		try {
			html.createHTML();
		} catch (Exception ex) {
			System.out.println("Error! Current was '" + html.toString() + "'");
			ex.printStackTrace();
		}
		System.out.println("Saving HTML Output...");
		html.saveHTML(fileOut + ".html");
		System.out.println("HTML output saved!");
	}

	private Scanner loadFileIntoScanner(String file) {
		Scanner scan = null;
		try {
			String r = readFile(file, Charset.defaultCharset());
			scan = new Scanner(r);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scan;
	}
	
	/**
	 * Reads a file and turns it into a single string.
	 * @param path Path to the file
	 * @param encoding encoding to use
	 * @return a String containing the file's contents
	 * @throws IOException if the file can't be found, usually.
	 */
	private String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
}
