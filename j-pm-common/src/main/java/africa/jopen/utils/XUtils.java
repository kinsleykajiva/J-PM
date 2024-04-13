package africa.jopen.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.lang.System.setErr;
import static java.util.prefs.Preferences.systemRoot;

public class XUtils {
	
	static              Logger  log = Logger.getLogger(XUtils.class.getName());
	public static final boolean IS_RUNNING_AS_ADMINISTRATOR;
	
	static {
		IS_RUNNING_AS_ADMINISTRATOR = isRunningAsAdministrator();
	}
	
	private static boolean isRunningAsAdministrator() {
		Preferences preferences = systemRoot();
		
		synchronized (System.err) {
			setErr(new PrintStream(new OutputStream() {
				@Override
				public void write( int b ) {
				}
			}));
			
			try {
				preferences.put("foo", "bar"); // SecurityException on Windows
				preferences.remove("foo");
				preferences.flush(); // BackingStoreException on Linux
				return true;
			} catch (Exception exception) {
				return false;
			} finally {
				setErr(System.err);
			}
		}
	}
	public static byte[] stringToUTF8(String inputString) {
		try {
			// Convert string to UTF-8 bytes
			return inputString.getBytes(StandardCharsets.UTF_8);
		} catch (Exception e) {
			// Handle encoding exception
			System.err.println("Error converting string to UTF-8: " + e.getMessage());
			return null; // Or handle the error as appropriate for your application
		}
	}
	private static boolean checkServiceExists( String serviceName ) {
		List<String> result = XSystemUtils.bashExecute("nssm status " + serviceName);
		for (String line : result) {
			if (line.isEmpty()) {
				continue;
			}
			byte[] utf8Bytes = stringToUTF8(line);
			if (utf8Bytes != null) {
				// Print the UTF-8 bytes as hexadecimal
				//System.out.print("UTF-8 Bytes: ");
				for (byte b : utf8Bytes) {
				//	System.out.printf("%02X ", b); // Print byte as hexadecimal
				}
			//	System.out.println(); // Newline
			}
			//log.info(line);
			//System.out.println("TTTTTTTT :: " + line);
			log.info(line);
			if (line.contains("OpenService(): The specified service does not exist as an installed service")) {
				return false;
			}
			if (line.contains("Can't open service!")) {
				return false;
			}
			if (line.contains("SERVICE_STOPPED") || line.contains("SERVICE_RUNNING")) {
				return true;
			}
			
		}
		return false;
	}
	
	public static boolean ifServiceExists( String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			//System.out.println("QQQQQQQ");
			//log.info("xxxxxxxWindows");
			return checkServiceExists(serviceName);
		}
		return false;
	}
	
	public static String getCurrentDateTime() {
		LocalDateTime     now       = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return now.format(formatter);
	}
	
	
	public static boolean checkIfNSSMIsInstalled() {
		List<String> result = XSystemUtils.bashExecute("nssm.exe");
		for (String line : result) {
			if (line.isEmpty()) {
				continue;
			}
			log.info(line);
			if (line.contains("NSSM: The non-sucking service manager") || line.contains("Version") || line.contains("Usage:")) {
				return true;
			}
		}
		return false;
	}
	
	/* nssm status j-pm-server
Can't open service!
OpenService(): The specified service does not exist as an installed service.*/
	
	/* nssm status j-pm-server
SERVICE_STOPPED*/
	/* nssm status j-pm-server
SERVICE_RUNNING*/
	public static boolean ifServiceIsRunning( String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
				List<String> result = XSystemUtils.bashExecute("nssm status " + serviceName);
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					if (line.contains("SERVICE_RUNNING")) {
						return true;
					}
					
				}
			}
			return false;
		}
		return false;
	}
	
	public static void printSuccessMessage( String message ) {
		System.out.println("\u001B[32m\u2713 " + message + "\u001B[0m");
	}
	
	public static void printErrorMessage( String message ) {
		System.out.println("\u001B[31m❌ " + message + "\u001B[0m");
	}
	
	public static boolean restartService( String serviceName ) {
		// Stop the service
		if (!stopService(serviceName)) {
			log.info("Failed to stop the service: " + serviceName);
			return false;
		}
		
		// Delete the service
		if (!deleteService(serviceName)) {
			log.info("Failed to delete the service: " + serviceName);
			return false;
		}
		
		// Create a new instance of the service
		if (!ifServiceIsRunning(serviceName)) {
			log.info("Failed to create a new instance of the service: " + serviceName);
			return false;
		}
		
		return true;
	}
	
	public static boolean stopService( String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
//				List<String> result = XSystemUtils.bashExecute("sc.exe stop " + serviceName);
				List<String> result = XSystemUtils.bashExecute("nssm stop " + serviceName);
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					if (line.contains("STOP: The operation completed successfully")) {
						
						return true;
					}
				}
				
			}
			
		}
		return false;
	}
	
	public static boolean startService( String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
				List<String> result = XSystemUtils.bashExecute("nssm start " + serviceName);
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					if (line.contains("START: The operation completed successfully")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	public static boolean deleteService( String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
//				List<String> result = XSystemUtils.bashExecute("sc.exe delete " + serviceName);
				stopService(serviceName);
				List<String> result = XSystemUtils.bashExecute("nssm remove " + serviceName + " confirm");
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					
					if (line.contains("removed successfully")) {
						return true;
					}
					
				}
			} else {
				log.info("Service does not exist: " + serviceName);
			}
			
		}
		return false;
	}
	/* nssm install j-pm-server "C:\Users\Kinsl\.jpm\j-pm-server.exe"
Service "j-pm-server" installed successfully!*/
	/* nssm stop j-pm-server
j-pm-server: STOP: The operation completed successfully.*/
	
	/* nssm remove j-pm-server confirm
Service "j-pm-server" removed successfully!*/
	public static boolean createRunAppAsService( String filePath, String serviceName ) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
				log.info("Service already exists: " + serviceName);
				return true;
			}
			var cmdDescription = "nssm set " + serviceName + " Description \"JPM background service\"";
			
			List<String> result = XSystemUtils.bashExecute("nssm install " + serviceName + "  \"" + filePath + "\"");
			for (String line : result) {
				log.info(line);
				/* nssm install j-pm-server "C:\Users\Kinsl\.jpm\j-pm-server.exe"
				   Service "j-pm-server" installed successfully!*/
				/* nssm start j-pm-server
					j-pm-server: START: The operation completed successfully.*/
				if (line.contains(" OpenService(): Access is denied.")) {
					log.info(line);
					return false;
				}
				if (line.contains(" installed successfully")) {
					XSystemUtils.bashExecute(cmdDescription);
					/*nssm set j-pm-server Description "Description for your service goes here"
						Set parameter "Description" for service "j-pm-server".*/
					return true;
				}
			}
			return false;
		}
		return false;
	}
}

