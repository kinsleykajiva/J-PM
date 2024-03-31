package africa.jopen.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static africa.jopen.utils.XFilesUtils.getAppFolderPath;

public class XLogger {
	private static final Logger logger = Logger.getLogger(XLogger.class.getName());
	private final        String logFileName;
	private final        String logFileErrorName;
	
	public XLogger( String appNameLabel ) {
		
		this.logFileName = getAppFolderPath() + "logs" + File.separator + appNameLabel + "_output.log";
		this.logFileErrorName = getAppFolderPath() + "logs" + File.separator + appNameLabel + "_error.log";
		
		initializeLogFile(logFileName, "General LogFile");
		initializeLogFile(logFileErrorName, "Error LogFile");
	}
	
	private void initializeLogFile( String fileName, String initialContent ) {
		Path path = Paths.get(fileName);
		if(path.toFile().exists()){
			return;
		}
		try {
			Files.createDirectories(path.getParent());
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
			writer.write(initialContent);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Failed to initialize log file: " + fileName, e);
		}
	}
	
	public synchronized void log( String message ) {
		logToFile(logFileName, message);
	}
	
	public synchronized void logError( String message ) {
		logToFile(logFileErrorName, message);
	}
	
	private void logToFile( String fileName, String message ) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
			String formattedMessage = XUtils.getCurrentDateTime() + " - " + message + "\n";
			writer.write(formattedMessage);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error writing to log file: " + fileName, e);
		}
	}
}
