package africa.jopen.process;

import africa.jopen.utils.XLogger;
import africa.jopen.utils.XSystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AppProcess {
	Logger log = Logger.getLogger(AppProcess.class.getName());
	private XLogger      xLogger;
	private Integer      id          = 0;
	private String       name        = "--";
	private String       version     = "0.0";
	private String       description = "No description";
	private List<String> tags        = new ArrayList<>();
	private       String         exeCommand     = "";
	private final ProcessBuilder processBuilder = new ProcessBuilder();
	
	public void runApp( String appFileName, String args ) {
	
		xLogger = new XLogger(name);
		String         os             = System.getProperty("os.name").toLowerCase();
		
		if (appFileName.endsWith("jar")) {
			if (XSystemUtils.isSystemJavaFound()) {
				if (os.startsWith("win")) {
					processBuilder.command("cmd.exe", "/c", "java -jar " + appFileName + " " + args);
				} else {
					processBuilder.command("bash", "-c", "java -jar " + appFileName + " " + args);
				}
			}
		}
		if (appFileName.endsWith("js")) {
			if (XSystemUtils.isSystemNodeFound()) {
				if (os.startsWith("win")) {
					processBuilder.command("cmd.exe", "/c", "node.exe " + appFileName + " " + args);
				} else {
					processBuilder.command("bash", "-c", "node " + appFileName + " " + args);
				}
			}
		}
		
		processBuilder.redirectErrorStream(true);
		executeProcess();
	}
	
	private void executeProcess() {
		try {
			Process process = processBuilder.start();
			xLogger.log("Running app " + getName() + " version " + getVersion());
			
			var outputLoggerThread = Thread.ofVirtual().start(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						xLogger.log(line);
					}
				} catch (IOException e) {
					log.severe("Error reading process input stream: " + e.getMessage());
				}
			});
			var errorOutputLoggerThread = Thread.ofVirtual().start(() -> {
				
				try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
					String errorLine;
					while ((errorLine = errorReader.readLine()) != null) {
						
						xLogger.logError(errorLine);
					}
				} catch (IOException e) {
					log.severe("Error reading process error stream: " + e.getMessage());
				}
			});
			getProcessMetadata(process);
			getMemoryUsage();
			int exitCode = process.waitFor();
			if (exitCode != 0) {
				System.out.println("Process was terminated by an external program with exit code: " + exitCode);
				xLogger.log("Process was terminated by an external program with exit code: " + exitCode);
				xLogger.logError("Process was terminated by an external program with exit code: " + exitCode);
				outputLoggerThread.interrupt(); // cancel input reader thread
				errorOutputLoggerThread.interrupt(); // cancel input reader thread
				Thread.currentThread().interrupt();
				
			} else {
				System.out.println("Process exited normally");
				xLogger.log("Process exited normally");
				Thread.currentThread().interrupt();
			}
			
		} catch (IOException | InterruptedException e) {
			log.severe("Error running app " + getName() + ": " + e.getMessage());
		}
	}
	
	private void getProcessMetadata( Process process ) {
		long pid = process.pid();
		process.info().totalCpuDuration();
		System.out.println("Process ID: " + pid);
		System.out.println("Process process.info().totalCpuDuration(): " + process.info().totalCpuDuration());
		System.out.println("Process process.info().commandLine(): " + process.info().commandLine());
		System.out.println("Process process.info().startInstant(): " + process.info().startInstant());
		// You can retrieve more metadata here as needed
	}
	
	private void getMemoryUsage() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		System.out.println("Heap Memory Usage: " + memoryBean.getHeapMemoryUsage());
		System.out.println("Non-Heap Memory Usage: " + memoryBean.getNonHeapMemoryUsage());
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId( Integer id ) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion( String version ) {
		this.version = version;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription( String description ) {
		this.description = description;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public void setTags( List<String> tags ) {
		this.tags = tags;
	}
	
	public String getExeCommand() {
		return exeCommand;
	}
	
	public void setExeCommand( String exeCommand ) {
		this.exeCommand = exeCommand;
	}
}
