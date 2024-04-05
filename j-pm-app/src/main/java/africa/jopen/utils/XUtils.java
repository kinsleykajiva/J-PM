package africa.jopen.utils;

import java.util.logging.Logger;

import java.util.List;
import java.util.logging.Logger;

public class XUtils {
	
	static Logger log = Logger.getLogger(XUtils.class.getName());
	
	private static boolean checkServiceExists(String serviceName) {
		List<String> result = XSystemUtils.bashExecute("sc.exe query " + serviceName);
		for (String line : result) {
			if (line.isEmpty()) {
				continue;
			}
			log.info(line);
			if (line.contains("SERVICE_NAME:")) {
				return true;
			}
			if (line.contains("[SC] EnumQueryServicesStatus:OpenService FAILED 1060:")) {
				return false;
			}
			if (line.contains("Access is denied") || line.contains("[SC] OpenSCManager FAILED 5")) {
				log.severe("Access is denied to create service, Run App as Admin");
				return false;
			}
		}
		return false;
	}
	
	public static boolean ifServiceExists(String serviceName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			return checkServiceExists(serviceName);
		}
		return false;
	}
	
	public static boolean ifServiceIsRunning(String serviceName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
				List<String> result = XSystemUtils.bashExecute("sc.exe query " + serviceName);
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					if (line.contains("RUNNING:")) {
						return true;
					}
					if (line.contains("Access is denied") || line.contains("[SC] OpenSCManager FAILED 5")) {
						log.severe("Access is denied to create service, Run App as Admin");
						return false;
					}
				}
			}
			return false;
		}
		return false;
	}
	
	public static void printSuccessMessage(String message){
		System.out.println("\u001B[32m\u2713 "+message+"\u001B[0m");
	}
	public static void printErrorMessage(String message){
		System.out.println("\u001B[31m❌ "+message+"\u001B[0m");
	}
	
	public static boolean deleteService(String serviceName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			if (ifServiceExists(serviceName)) {
				List<String> result = XSystemUtils.bashExecute("sc.exe delete " + serviceName);
				for (String line : result) {
					if (line.isEmpty()) {
						continue;
					}
					log.info(line);
					if (line.contains("[SC] DeleteService SUCCESS")) {
						return true;
					}
					if (line.contains("Access is denied") || line.contains("[SC] OpenSCManager FAILED 5")) {
						log.severe("Access is denied to create service, Run App as Admin");
						return false;
					}
				}
			}
			return false;
		}
		return false;
	}
	
	public static boolean createRunAppAsService(String filePath, String serviceName) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) {
			List<String> result = XSystemUtils.bashExecute("sc.exe create " + serviceName + " binPath= \"" + filePath + "\"");
			for (String line : result) {
				log.info(line);
				if (line.contains("[SC] CreateService SUCCESS")) {
					return true;
				}
				if (line.contains("Access is denied") || line.contains("[SC] OpenSCManager FAILED 5")) {
					log.severe("Access is denied to create service, Run App as Admin");
					return false;
				}
			}
			return false;
		}
		return false;
	}
}

