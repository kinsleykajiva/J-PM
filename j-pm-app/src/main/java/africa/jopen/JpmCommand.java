package africa.jopen;

import africa.jopen.utils.XFilesUtils;
import africa.jopen.utils.XSystemUtils;
import io.micronaut.configuration.picocli.PicocliRunner;
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

@Command(name = "jpm", description = "...",
		mixinStandardHelpOptions = true)
public class JpmCommand implements Runnable {
	
	@Option(names = { "-v", "--verbose" }, description = "...")
	boolean verbose;
	
	//	@CommandLine.Parameters(description = "Your name or command")
	@Option(names = { "-c", "--command" }, description = "Direct command to do")
	String command;
	
	@Option(names = { "-n", "--name" }, description = "Your name")
	String name;
	
	@Option(names = { "-a", "--app" }, description = "The application")
	String appFile;
	
	
	public static void main( String[] args ) {
		XSystemUtils.checkForSDKs();
		Path currentDirectory = Paths.get(System.getProperty("user.dir"));
		Arrays.asList(args).forEach(System.out::println);
		
		
		if (args.length > 0) {
			String[] exec = { "-v", "-c", "install", "--name", "", "--app", "" };
			if (args[0].equals("start")) { // start app.js or start app.jar
				String file = args[1];
				// get current directory
				System.out.println("file: " + file);
				System.out.println("file: " + currentDirectory + File.separator + file);
				exec[4] = file;
				exec[6] = currentDirectory + File.separator + file;
				
			}
			System.out.println("::1:::" + currentDirectory);
			
			
			// Parse command line arguments and run the command
//        int exitCode = new CommandLine(new JpmCommand()).execute("-v", "nameOrCommandValue", "--name", "John", "--app", "exampleAppFile.txt");
			// int exitCode = new CommandLine(new JpmCommand()).execute("-v", "install", "--name", "John", "--app", "exampleAppFile.txt");
			int exitCode = new CommandLine(new JpmCommand()).execute(exec);
			
			// Exit with the exit code
			System.exit(exitCode);
		} else {
			PicocliRunner.run(JpmCommand.class, args);
		}
		
	}
	
	public static void main1( String[] args ) throws Exception {
		//System.out.println(args);
		JpmCommand command = new JpmCommand();
		Arrays.asList(args).forEach(System.out::println);
		PicocliRunner.run(JpmCommand.class, args);
		CommandLine.run(command, args);
		
	}
	
	private void animateDownload() {
		boolean success       = true; // Simulating successful completion, modify this according to your actual logic
		int     terminalWidth = getTerminalWidth();
		Thread thread = new Thread(() -> {
			try {
				int percent = 0;
				while (percent <= 100) {
					System.out.print("\rDownloading: [" + getProgressString(percent, terminalWidth) + "] " + percent + "%");
					Thread.sleep(200); // Adjust the speed of animation here
					percent += 5; // Adjust the step size here
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		thread.start();
		
		// Simulate some work
		try {
			Thread.sleep(2000); // Simulate download time
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			success = false; // Error occurred
		}
		
		thread.interrupt(); // Stop the animation thread
		System.out.print("\r"); // Move cursor to the beginning of the line
		if (success) {
			System.out.println("Downloading: [" + getProgressString(100, terminalWidth) + "] 100% \u2713"); // Green tick for success
		} else {
			System.out.println("Downloading: [" + getProgressString(100, terminalWidth) + "] 100% \u274C"); // Red cross for error
		}
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
		// business logic here
		if (verbose) {
		
		}
		if (name != null && !name.isEmpty()) {
			System.out.println("Hi, " + name + "!");
		} else {
			System.out.println("Hi!");
		}
		
		if (command.equalsIgnoreCase("start")) {
			if (appFile == null || appFile.isEmpty()) {
				System.out.println(" --app or -a is required to know what to run");
			}
		}
		
		
		if (command.equalsIgnoreCase("stop")) {
			System.out.println("Stopping...");
		}
		
		
		if (command.equalsIgnoreCase("install")) {
			System.out.println("Setting up app");
			XFilesUtils.getAppFolderPath();
			XFilesUtils.getCacheFile();
            /*try{
                XHttpUtils.downloadFile("https://cdn.pixabay.com/photo/2023/08/19/13/42/water-8200502_1280.jpg",XFilesUtils.getAppFolderPath());
            }catch (Exception e){
                e.printStackTrace();
            }*/
		}
		// animateDownload();
       
        
        
        /*else {
            System.out.println("Hi, " + nameOrCommand + "!");
        }*/
	}
}
