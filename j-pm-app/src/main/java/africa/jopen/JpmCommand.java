package africa.jopen;

import africa.jopen.utils.*;
import io.micronaut.configuration.picocli.PicocliRunner;
import org.json.JSONArray;
import org.json.JSONObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Command(name = "jpm", description = "Java Process Manager",
		mixinStandardHelpOptions = true)
public class JpmCommand implements Runnable {
	
	@Option(names = {"-v", "--version"}, description = "Print the version information")
	boolean printVersion;
	
	
	//	@CommandLine.Parameters(description = "Your name or command")
	@Option(names = { "-c", "--command" }, description = "Direct command to do")
	String command;
	
	@Option(names = { "-n", "--name" }, description = "Your name")
	String name;
	
	@Option(names = { "-a", "--app" }, description = "The application")
	String appFile;
	
	
	public static void main(String[] args) {
		XSystemUtils.checkForSDKs();
		Path currentDirectory = Paths.get(System.getProperty("user.dir"));
		Arrays.asList(args).forEach(System.out::println);
		if (args.length > 0) {
			String[] exec = handleCommandLineArguments(args, currentDirectory);
			int exitCode = new CommandLine(new JpmCommand()).execute(exec);
			System.exit(exitCode);
		} else {
			
			PicocliRunner.run(JpmCommand.class, args);
			
		}
	}
	
	private static String[] handleCommandLineArguments(String[] args, Path currentDirectory) {
		String[] exec = {"-v", "-c", "install", "--name", "", "--app", ""};
		
		/*if (args[0].equals("-v") || args[0].equals("--version")) {
			printVersion();
			System.exit(0);
		} else */if (args[0].equals("ls")) {
			exec[2] = "ls";
			exec[6] = "";
		} else if (args[0].equals("restart")) {
			exec[2] = "restart";
			exec[6] = "";
			String appNameId = args.length == 1 ? "all" : args[1];
			exec[4] = appNameId;
		} else if (args[0].equals("stop")) {
			exec[2] = "stop";
			exec[6] = "";
			String appNameId = args.length == 1 ? "all" : args[1];
			exec[4] = appNameId;
		} else if (args[0].equals("start")) {
			String file = args[1];
			System.out.println("file: " + file);
			System.out.println("file: " + currentDirectory + File.separator + file);
			exec[4] = file;
			exec[2] = "start";
			exec[6] = currentDirectory + File.separator + file;
		}
		
		return exec;
	}
	
	private static void printVersion() {
		// Print the version information here
		System.out.println("Java Process Manager (jpm) version 1.0.0");
	}
	private String getProgressString( int percent, int terminalWidth ) {
		int           numOfChars     = (int) ((percent / 100.0) * (terminalWidth - 13)); // 13 is the length of "Downloading: [] %"
		StringBuilder progressString = new StringBuilder();
		for (int i = 0; i < numOfChars; i++) {
			progressString.append("#");
		}
		for (int i = numOfChars; i < terminalWidth - 13; i++) {
			progressString.append(" ");
		}
		return progressString.toString();
	}
	
	private static int getTerminalWidth() {
		try {
			String         osName = System.getProperty("os.name").toLowerCase();
			ProcessBuilder processBuilder;
			if (osName.contains("win")) {
				// Windows


//                processBuilder = new ProcessBuilder("cmd.exe", "/c", "mode con");
				processBuilder = new ProcessBuilder("powershell.exe", "-Command", "$Host.UI.RawUI.WindowSize.Width");
			} else {
				// Unix-based systems
				processBuilder = new ProcessBuilder("bash", "-c", "tput cols");
			}
			processBuilder.redirectErrorStream(true); // Merge error stream with input stream
			
			Process        process = processBuilder.start();
			BufferedReader reader  = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String         line;
			while ((line = reader.readLine()) != null) {
				System.out.println("line " + line);
				int width = Integer.parseInt(line.trim());
				return width;
				
			}
		} catch (IOException | NumberFormatException e) {
			// If unable to get terminal width, return a reasonable default value
			e.printStackTrace();
			return 80;
		}
		// If unable to determine the terminal width, return a default value
		return 80;
	}
	
	public void run() {
		if (printVersion) {
			System.out.println("Java Process Manager (jpm) version 0.1.0");
			return;
		}
		if (command == null) {
			System.out.println("Please provide a command or use flags to perform actions.");
			CommandLine.usage(this, System.out);
			System.out.println("  start <app_file>          Start the specified application");
			System.out.println("  stop <app_name>           Stop the specified application");
			System.out.println("  ls                        List all applications");
			System.out.println("  install                   Check install is properly done");
			return;
		}
		
		switch (command.toLowerCase()) {
			case "restart":
				reStartApp();
				break;
			case "start":
				startApp();
				break;
			case "stop":
				stopApp();
				break;
			case "ls":
				listApps();
				break;
			case "install":
				installApp();
				break;
			default:
				System.out.println("Unsupported command: " + command);
				CommandLine.usage(this, System.out);
		}
	}
	
	private void reStartApp() {
		System.out.println("reStarting..." + name);
		String response = null;
		if (name.equals("all")) {
			response = XHttpUtils.postRequest("run",
					new JSONObject()
							.put("appName", "")
							.put("id", -1)
							.put("isRestart", true)
							.toString());
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean("success")) {
				JSONArray apps = jsonObject.getJSONObject("data").getJSONArray("apps");
				if (apps.length() > 0) {
					String[][] data1 = new String[apps.length()][];
					for (int i = 0; i < apps.length(); i++) {
						JSONObject app = apps.getJSONObject(i);
						String[] rowData = {
								String.valueOf(app.getInt("id")),
								app.getString("name"),
								app.getString("version"),
								String.valueOf(app.getLong("pid")),
								app.getString("uptime"),
								app.getString("status"),
								app.getString("cpu"),
								app.getString("mem"),
								app.getString("user")
						};
						data1[i] = rowData;
					}
					new TablePrinter(data1);
				} else {
					System.out.println("\u001B[31m\u2713 No apps found/Running\u001B[0m");
				}
			} else {
				printErrorMessage("Failed to get apps: " + jsonObject.getString("message"));
			}
		} else {
			response = XHttpUtils.postRequest("run",
					new JSONObject()
							.put("appName", name)
							.put("id", name)
							.put("isRestart", true)
							.toString());
			
			
			if (response == null || response.isEmpty()) {
				System.err.println("Failed to get data");
				return;
			}
			
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean("success")) {
				XUtils.printSuccessMessage(jsonObject.getString("message"));
				JSONObject data = jsonObject.getJSONObject("data");
				JSONObject app1 = data.getJSONObject("app");
				printAppDetails(app1);
			} else {
				System.out.println("Failed to start app: " + jsonObject.getString("message"));
			}
		}
	}
	
	private void startApp() {
		if (name == null || name.isEmpty()) {
			System.out.println("Hi!");
		} else {
			System.out.println("Hi, " + name + "!");
		}
		
		if (appFile == null || appFile.isEmpty()) {
			System.err.println("Missing app file!");
			return;
		}
		
		System.out.println("Starting..." + name);
		String appFilePath = "C:\\Users\\Kinsl\\IdeaProjects\\jar-demo\\target\\test-app.js";
		var response = XHttpUtils.postRequest("run",
				new JSONObject()
						.put("appName", name)
						/*.put("filePath", appFilePath)*/
						.put("filePath", appFile)
						.toString());
		
		if (response == null || response.isEmpty()) {
			System.err.println("Failed to get data");
			return;
		}
		
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			XUtils.printSuccessMessage(jsonObject.getString("message"));
			JSONObject data = jsonObject.getJSONObject("data");
			JSONObject app1 = data.getJSONObject("app");
			printAppDetails(app1);
		} else {
			System.out.println("Failed to start app: " + jsonObject.getString("message"));
		}
	}
	
	private void printAppDetails( JSONObject app ) {
		String[][] data1 = new String[1][];
		String[] rowData = {
				String.valueOf(app.getInt("id")),
				app.getString("name"),
				app.getString("version"),
				String.valueOf(app.getLong("pid")),
				app.getString("uptime"),
				app.getString("status"),
				app.getString("cpu"),
				app.getString("mem"),
				app.getString("user")
		};
		data1[0] = rowData;
		new TablePrinter(data1);
	}
	
	private void listApps() {
		System.out.println("Listing apps...");
		String response = XHttpUtils.getRequest("");
		
		if (response == null || response.isEmpty()) {
			printErrorMessage("Failed to get apps. Please check Health Setup.");
			return;
		}
		
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			JSONArray apps = jsonObject.getJSONObject("data").getJSONArray("apps");
			if (apps.length() > 0) {
				String[][] data1 = new String[apps.length()][];
				for (int i = 0; i < apps.length(); i++) {
					JSONObject app = apps.getJSONObject(i);
					String[] rowData = {
							String.valueOf(app.getInt("id")),
							app.getString("name"),
							app.getString("version"),
							String.valueOf(app.getLong("pid")),
							app.getString("uptime"),
							app.getString("status"),
							app.getString("cpu"),
							app.getString("mem"),
							app.getString("user")
					};
					data1[i] = rowData;
				}
				new TablePrinter(data1);
			} else {
				System.out.println("\u001B[31m\u2713 No apps found/Running\u001B[0m");
			}
		} else {
			printErrorMessage("Failed to get apps: " + jsonObject.getString("message"));
		}
	}
	
	private void stopApp() {
		System.out.println("Stopping...App");
		// Add logic to stop the app
		var response = XHttpUtils.postRequest("stop-delete",
				new JSONObject()
						.put("appName", name)
						.put("id", name)
						.toString());
		if (response == null || response.isEmpty()) {
			XUtils.printErrorMessage("Failed to get data");
			return;
		}
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			XUtils.printSuccessMessage(jsonObject.getString("message"));
			/*JSONObject data = jsonObject.getJSONObject("data");
			JSONObject app1 = data.getJSONObject("app");
			printAppDetails(app1);*/
			JSONArray apps = jsonObject.getJSONObject("data").getJSONArray("apps");
			if (apps.length() > 0) {
				String[][] data1 = new String[apps.length()][];
				for (int i = 0; i < apps.length(); i++) {
					JSONObject app = apps.getJSONObject(i);
					String[] rowData = {
							String.valueOf(app.getInt("id")),
							app.getString("name"),
							app.getString("version"),
							String.valueOf(app.getLong("pid")),
							app.getString("uptime"),
							app.getString("status"),
							app.getString("cpu"),
							app.getString("mem"),
							app.getString("user")
					};
					data1[i] = rowData;
				}
				new TablePrinter(data1);
			} else {
				System.out.println("\u001B[31m\u2713 No apps found/Running\u001B[0m");
			}
		} else {
			//System.out.println("Failed to start app: " + jsonObject.getString("message"));
			XUtils.printErrorMessage(jsonObject.getString("message"));
		}
	}
	
	private void installApp() {
		System.out.println("Setting up app");
		XFilesUtils.getAppFolderPath();
		XFilesUtils.getCacheFile();
		// Add logic to download and install the app
	}
	
	private void printErrorMessage( String message ) {
		
		System.err.println("\u001B[31m  ❌ " + message);
		
	}
	
	public static void printAlignedErrorMessage( String message ) {
		// Determine the length of the longest line in the message
		int      maxLength = 0;
		String[] lines     = message.split("\\n");
		for (String line : lines) {
			maxLength = Math.max(maxLength, line.length());
		}
		
		// Print the error message with proper alignment
		System.err.println("\u001B[31m╔" + "═".repeat(maxLength + 2) + "╗");
		for (String line : lines) {
			int    paddingLength = maxLength - line.length();
			String leftPadding   = " ".repeat(paddingLength / 2);
			String rightPadding  = " ".repeat(paddingLength - paddingLength / 2);
			System.err.println("║ " + leftPadding + line + rightPadding + " ║");
		}
		System.err.println("╚" + "═".repeat(maxLength + 2) + "╝\u001B[0m");
	}
	
	
}
