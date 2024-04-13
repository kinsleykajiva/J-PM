package africa.jopen;

import africa.jopen.utils.*;
import africa.jopen.logs.LogFileStreamer;
import io.micronaut.configuration.picocli.PicocliRunner;
import org.json.JSONArray;
import org.json.JSONObject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static africa.jopen.utils.XUtils.printErrorMessage;
import static africa.jopen.utils.XUtils.printSuccessMessage;

@Command(name = "jpm", description = "Java Process Manager",mixinStandardHelpOptions = true)
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
	
	
	
	
	private static void appendData(String filePath, boolean shouldIRun, int crunchifyRunEveryNSeconds) {
		FileWriter fileWritter;
		try {
			while (shouldIRun) {
				Thread.sleep(crunchifyRunEveryNSeconds);
				fileWritter = new FileWriter(filePath, true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				String data = "\nCrunchify.log file content: " + Math.random();
				bufferWritter.write(data);
				bufferWritter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		XSystemUtils.checkForSDKs();
		Path currentDirectory = Paths.get(System.getProperty("user.dir"));
//		Arrays.asList(args).forEach(System.out::println);
		if (args.length > 0) {
			String[] exec = handleCommandLineArguments(args, currentDirectory);
			if(exec.length==0){
//				PicocliRunner.run(JpmCommand.class, args);
				JpmCommand.logDefaultMessage(null);
				return;
			}
			//Arrays.asList(exec).forEach(System.out::println);
			System.out.println( String.join(", ", exec));
			/*System.out.println("||"+exec[0]);
			System.out.println("||"+exec[1]);
			System.out.println("||"+exec[3]);*/
			System.out.println("pano");
			int exitCode = new CommandLine(new JpmCommand()).execute(exec);
			System.exit(exitCode);
		} else {
			PicocliRunner.run(JpmCommand.class, args);
		}
	}
	
	private static String[] handleCommandLineArguments(String[] args, Path currentDirectory) {
		String[] exec = {"-v", "-c", "", "--name", "", "--app", ""};
		
		/*if (args[0].equals("-v") || args[0].equals("--version")) {
			printVersion();
			System.exit(0);
		} else */
		if (args[0].equals("install")) {
			exec[2] = "install";
			exec[6] = "";
		}if (args[0].equals("logs")) {
			
				exec[2] = "logs";
				exec[6] = "";
				String appNameId = args.length == 1 ? "all" : args[1];
				exec[4] = appNameId;
			
		}if (args[0].equals("ls")) {
			exec[2] = "ls";
			exec[6] = "";
		} else if (args[0].equals("restart")) {
			if(args.length > 1) {
				exec[2] = "restart";
				exec[6] = "";
				String appNameId = args[1];
				exec[4] = appNameId;
			}else{
				return new String[]{};
			}
		} else if (args[0].equals("stop")) {
			exec[2] = "stop";
			exec[6] = "";
			String appNameId = args.length == 1 ? "all" : args[1];
			exec[4] = appNameId;
		} else if (args[0].equals("start")) {
			if(args.length > 1) {
				String file = args[1];
				System.out.println("file: " + file);
				System.out.println("file: " + currentDirectory + File.separator + file);
				exec[4] = file;
				exec[2] = "start";
				exec[6] = currentDirectory + File.separator + file;
			}else{
				return new String[]{};
			}
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
	
	
	public static void logDefaultMessage(JpmCommand tis){
		System.out.println("Please provide a command or use flags to perform actions.");
		if(tis != null) {
			CommandLine.usage(tis, System.out);
		}
		System.out.println("  start <app_file>          Start the specified application");
		System.out.println("  stop <app_name>           Stop the specified application");
		System.out.println("  ls                        List all applications");
		System.out.println("  install                   Check install is properly done");
		System.out.println("  logs <app_name|id>        Show logs for all or for app or process");
	}
	private boolean allowListing = true;
	
	public void run() {
		
		if (printVersion && Objects.isNull(command)) {
			System.out.println("Java Process Manager (jpm) version 0.1.0");
			return;
		}
		System.out.println(command);
		if (command == null) {
			logDefaultMessage(this);
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
			case "logs":
				allowListing = false;
				listApps();
				getLogs();
				break;
			case "install":
				installApp();
				break;
			default:
				logDefaultMessage(this);
		}
		
	}
	final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private void getLogs() {
		System.out.println("Getting logs");
		
		//System.out.println("APP_CACHE ." + APP_CACHE);
		if(name == null || name.isEmpty() || name.equals("all")){
			if(APP_CACHE_JSONArray == null){
				System.out.println("No logs found.");
				return;
			}
			
			/*List<Future<?>> futures = IntStream.range(0, APP_CACHE_JSONArray.length())
					.mapToObj(APP_CACHE_JSONArray::getJSONObject)
					.filter(app -> app.has("log"))
					.map(app -> app.getString("log"))
					.map(File::new)
					.filter(File::exists)
					.map(logFile -> executorService.submit(new LogFileStreamer(logFile.getPath(), false)))
					.collect(Collectors.toList());*/
			
			List<Future<?>> futures = IntStream.range(0, APP_CACHE_JSONArray.length())
					.mapToObj(APP_CACHE_JSONArray::getJSONObject)
					.filter(app -> app.has("log"))
					.map(app -> app.getString("log"))
					.map(File::new)
					.filter(File::exists)
					.map(logFile -> executorService.submit(new LogFileStreamer(logFile.getPath(), false)))
					.collect(Collectors.toList());
			
			
			futures.forEach(future -> {
				try {
					future.get();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Error: " + e.getMessage());
				}
			});
			
		}else {
			System.out.println("--->name " + name);
			if (APP_CACHE_JSONArray == null) {
				System.out.println("No logs found.");
				return;
			}
			
			JSONObject app = IntStream.range(0, APP_CACHE_JSONArray.length())
					.mapToObj(APP_CACHE_JSONArray::getJSONObject)
					.filter(a -> a.getString("name").equals(name) || String.valueOf(a.getInt("id")).equals(name))
					.findFirst()
					.orElse(null);
			
			if (app == null || !app.has("log")) {
				System.out.println("No logs found.");
				return;
			}
			Consumer<String> logConsumer = line -> System.out.println(line);
			String           fileLog     = app.getString("log");
			File logFile = new File(fileLog);
			if (logFile.exists()) {
				//new LogFileStreamer(fileLog, true,System.out::println).run();
				LogFileStreamer logFileStreamer = new LogFileStreamer(fileLog, true);
				logFileStreamer.run();
			} else {
				System.out.println("No logs found.");
			}
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
				printSuccessMessage(jsonObject.getString("message"));
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
		String appFilePath = "C:\\Users\\Kinsl\\IdeaProjects\\jar-demo\\target\\test-app2.js";
		var response = XHttpUtils.postRequest("run",
				new JSONObject()
						.put("appName", name)
						.put("filePath", appFilePath)
						/*.put("filePath", appFile)*/
						.toString());
		
		if (response == null || response.isEmpty()) {
			System.err.println("Failed to get data");
			return;
		}
		
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			printSuccessMessage(jsonObject.getString("message"));
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
	private JSONArray APP_CACHE_JSONArray;
	private void listApps() {
		
		String response = XHttpUtils.getRequest("");
		
		if (response == null || response.isEmpty()) {
			printErrorMessage("Failed to get apps. Please check Health Setup.");
			return;
		}
		
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			JSONArray apps = jsonObject.getJSONObject("data").getJSONArray("apps");
			if (apps.length() > 0) {
				APP_CACHE_JSONArray = apps;
				if(allowListing) {
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
				}
			} else {
				APP_CACHE_JSONArray = null;
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
			printErrorMessage("Failed to get data");
			return;
		}
		JSONObject jsonObject = new JSONObject(response);
		if (jsonObject.getBoolean("success")) {
			printSuccessMessage(jsonObject.getString("message"));
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
			printErrorMessage(jsonObject.getString("message"));
		}
	}
	private boolean downloadServerUtil(String serverFileName){
		try {
			XHttpUtils.downloadFile(serverFileName, XFilesUtils.getAppFolderPath());
			return true;
		} catch (IOException | InterruptedException e) {
			System.err.println("Failed install utils please try again");
		}
		return false;
	}
	private void installAppx() {
		System.out.println("Setting up app");
		if(!XUtils.IS_RUNNING_AS_ADMINISTRATOR){
			printErrorMessage("Please run as admin user");
			return;
		}
		XFilesUtils.getAppFolderPath();
		XFilesUtils.getCacheFile();
		String server = "j-pm-server";
		String serverFileName = server+".exe";
		var serverFile = new File(XFilesUtils.getAppFolderPath()  + serverFileName);
		if(!serverFile.exists()){
			// download this file
			var wasSuccessful=downloadServerUtil(serverFileName);
			if(wasSuccessful){
				if(!XUtils.ifServiceExists(server)){
					var cratedService =XUtils.createRunAppAsService(serverFile.getAbsolutePath(),server);
					if(!cratedService){
						printErrorMessage("Failed to run utils as service,Please try or run shell again as Admin user");
						return;
					}
					if(!XUtils.ifServiceIsRunning(server)){
						printErrorMessage("Service was created but failed to keep service alive");
					}
				}
				printSuccessMessage("Service running");
			}
		}else{
			
			if(!XUtils.ifServiceExists(server)){
				var cratedService =XUtils.createRunAppAsService(serverFile.getAbsolutePath(),server);
				if(!cratedService){
					printErrorMessage("Failed to run utils as service,Please try or run shell again as Admin user");
					return;
				}
				if(!XUtils.ifServiceIsRunning(server)){
					XUtils.startService(server);
					System.out.println("Service is running now");
				}
			}else{
				
				if(!XUtils.ifServiceIsRunning(server)){
					XUtils.startService(server);
					System.out.println("Service is running now");
				}
			}
			
			
			printSuccessMessage("Service running");
		}
	}
	
	private void installApp() {
		System.out.println("Setting up app");
		if (!XUtils.IS_RUNNING_AS_ADMINISTRATOR) {
			printErrorMessage("Please run as admin user");
			return;
		}
		
		String appFolderPath = XFilesUtils.getAppFolderPath();
		XFilesUtils.getCacheFile();
		String server = "j-pm-server";
		String serverFileName = server + ".exe";
		Path serverFilePath = Paths.get(appFolderPath, serverFileName);
		
		boolean isServiceRunning = XUtils.ifServiceExists(server) && XUtils.ifServiceIsRunning(server);
		
		if (Files.notExists(serverFilePath) || !isServiceRunning) {
			if (Files.notExists(serverFilePath)) {
				boolean wasSuccessful = downloadServerUtil(serverFileName);
				if (!wasSuccessful) {
					printErrorMessage("Failed to download server utility");
					return;
				}
			}
			
			if (!XUtils.ifServiceExists(server)) {
				boolean cratedService = XUtils.createRunAppAsService(serverFilePath.toString(), server);
				if (!cratedService) {
					printErrorMessage("Failed to run utils as service, Please try or run shell again as Admin user");
					return;
				}
			}
			
			if (!XUtils.ifServiceIsRunning(server)) {
				XUtils.startService(server);
				if (!XUtils.ifServiceIsRunning(server)) {
					printErrorMessage("Service was created but failed to keep service alive");
					return;
				}
			}
		}
		if (XUtils.ifServiceIsRunning(server)) {
			printSuccessMessage("Service running");
		}
	}
	
	
	
	
	
}
