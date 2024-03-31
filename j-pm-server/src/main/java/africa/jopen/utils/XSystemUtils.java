package africa.jopen.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XSystemUtils {
	static         Logger                   log       = Logger.getLogger(XSystemUtils.class.getName());
	
	private static boolean IS_SYSTEM_JAVA_FOUND = false;
	private static boolean IS_SYSTEM_NODE_FOUND = false;
	
	
	public static boolean isSystemJavaFound() {
		return IS_SYSTEM_JAVA_FOUND;
	}
	
	public static void setSystemJavaFound( boolean isSystemJavaFound ) {
		IS_SYSTEM_JAVA_FOUND = isSystemJavaFound;
	}
	
	public static boolean isSystemNodeFound() {
		return IS_SYSTEM_NODE_FOUND;
	}
	
	public static void setSystemNodeFound( boolean isSystemNodeFound ) {
		IS_SYSTEM_NODE_FOUND = isSystemNodeFound;
	}
	
	/**
	 * Executes the given command in the bash or cmd.exe shell and returns the output as a list of strings.
	 * If the command is empty, an empty list will be returned.
	 * The method will throw an IOException if there is an error reading or writing to the process.
	 * The method will throw an InterruptedException if the current thread is interrupted while waiting for the process to complete.
	 *
	 * @param command The command to execute.
	 * @return A list of strings representing the output of the command.
	 * @throws IOException              If there is an error reading or writing to the process.
	 * @throws InterruptedException     If the current thread is interrupted while waiting for the process to complete.
	 * @throws RuntimeException         If the execution fails with a non-zero exit code.
	 */
	public static List<String> bashExecute( final String command)  {
		if(command.isEmpty()) {
			return new ArrayList<>();
		}
		log.info("Executing command: " +command);
		
		List<String>   output         = new ArrayList<>();
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			processBuilder.command("cmd.exe", "/c", command);
		} else {
			processBuilder.command("bash", "-c", command);
		}
		
		processBuilder.redirectErrorStream(true); // Redirect error stream to input stream
		
		Process process = null;
		try {
			process = processBuilder.start();
		
		// Process standard output
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.add(line);
			}
		}
		
		int exitCode = process.waitFor();
		// convert to seconds time taken
		
		if (exitCode != 0) {
			//throw new RuntimeException("Execution failed with error code " + exitCode + "\n for command: " + command);
			log.severe("Execution failed with error code " + exitCode + "\n for command: " + command);
		}
		
		return output;
		} catch (IOException | InterruptedException e) {
			log.severe("Error executing command: " + command + " " + e);
		}
		return output;
	}
	public static void checkForSDKs(){
		
		List<String>  javaCheckOutput = bashExecute("java -version");
		if (!javaCheckOutput.isEmpty()) {
			javaCheckOutput.forEach(line->{
				if(line.contains("Runtime Environment") || line.contains(" version")){
					setSystemJavaFound(true);
					log.info("Java found " );
				}
			});
			
		}
		List<String>  nodeJSCheckOutput = bashExecute(System.getProperty("os.name").toLowerCase().startsWith("win")? "node.exe -v" :"node -v");
		log.info("nodeJSCheckOutput " + nodeJSCheckOutput);
		if (!nodeJSCheckOutput.isEmpty()) {
			nodeJSCheckOutput.forEach(line->{
				if(line.startsWith("v")){
					
					String  regex   = "^v\\d+\\.\\d+\\.\\d+$";
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(line);
					if(matcher.matches()){
					setSystemNodeFound(true);
					log.info("Nodejs found " );
					}
				}
			});
			
		}
		
	}
	
}