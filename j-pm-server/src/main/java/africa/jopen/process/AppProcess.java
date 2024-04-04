package africa.jopen.process;

import africa.jopen.utils.XLogger;
import africa.jopen.utils.XSystemUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static africa.jopen.utils.XSystemUtils.getPIDRAMUsage;

public class AppProcess {
	Logger log = Logger.getLogger(AppProcess.class.getName());
	private       XLogger        xLogger;
	private       Integer        id             = 0;
	private       String         name           = "--";
	private       String         version        = "0.0";
	private       String         description    = "No description";
	private       List<String>   tags           = new ArrayList<>();
	private       String         exeCommand     = "";
	private       String         sdkPath     = "";
	private final ProcessBuilder processBuilder = new ProcessBuilder();
	private       Long           pid            = null;
	private       Instant startTime      = null;
	private Thread parentThread;
	private Thread childOutputLoggerThread;
	private Thread childErrorOutputLoggerThread;
	public void runApp( String appFileName, String args ) {
		
		xLogger = new XLogger(name);
		String os = System.getProperty("os.name").toLowerCase();
		
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
	
	
	
	public boolean stop() {
		
		ProcessHandle process = ProcessHandle.of(pid).orElseThrow(() ->
				new IllegalArgumentException("Process with PID " + pid + " not found"));
		// Destroy the process forcibly
		boolean isDestroyed = process.destroyForcibly();
		if (isDestroyed) {
			System.out.println("Process with PID " + pid + " has been killed successfully.");
			return  true;
		} else {
			System.out.println("Failed to kill process with PID " + pid);
			if(parentThread == null){
				log.info("App " + getName() + " version " + getVersion() + " is not running");
				return false;
			}
			interruptThreads();
			return true;
		}
		
	}
	public void restart() {
		if(parentThread == null){
			log.info("App " + getName() + " version " + getVersion() + " is not running");
			return;
		}
		interruptThreads();
		
		
		xLogger.log("Restarting app " + getName() + " version " + getVersion());
		if (pid != null) {
			// SUCCESS: The process with PID 3084 has been terminated.
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("win")) {
				var res = XSystemUtils.bashExecute("taskkill /PID " + pid + " /F");
				if (!res.isEmpty()) {
					for (String line : res) {
						if (line.isEmpty()) {
							continue;
						}
						log.info(line);
						if (line.contains("SUCCESS: The process with PID " + pid + " has been terminated.")) {
							executeProcess();
						}
					}
				}
			} else if (os.startsWith("linux") || os.startsWith("mac")) {
				List<String> res = XSystemUtils.bashExecute("kill -9 " + pid);
				if (!res.isEmpty()) {
					for (String line : res) {
						if (line.isEmpty()) {
							continue;
						}
						log.info(line);
						if (line.contains("No such process") || line.contains("Operation not permitted")) {
							log.severe("Failed to kill process with PID " + pid + ": " + line);
						} else {
							executeProcess();
							
						}
					}
				}
			}
			
		} else {
			log.severe("PID is null");
		}
		
	}
	
	
	
	public JSONObject toJsonObject() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("id", id);
		jsonObject.put("name", name);
		jsonObject.put("version", getVersion());
		jsonObject.put("description", getDescription());
		jsonObject.put("tags", tags);
		jsonObject.put("exeCommand", exeCommand);
		jsonObject.put("status", "online");
		jsonObject.put("cpu", "0%");
		jsonObject.put("sdkPath", sdkPath);
		
		jsonObject.put("mem", getProcessRamUse());
		jsonObject.put("user", "jpm");
		// Skip adding processBuilder as it's not directly convertible to JSON
		jsonObject.put("pid", getPid());
		// Convert Instant startTime to string before adding to JSON
		jsonObject.put("uptime", getProcessUpTime());
		jsonObject.put("startTime", startTime != null ? startTime.toString() : null);
		System.out.println(jsonObject.toString());
		// Skip adding threads as they're not directly convertible to JSON
		return jsonObject;
	}
	
	
	private void startChildThreads( Process process) {
		childOutputLoggerThread = Thread.ofVirtual().start(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					xLogger.log(line);
				}
			} catch (IOException e) {
				log.severe("Error reading process input stream: " + e.getMessage());
			}
		});
		childErrorOutputLoggerThread = Thread.ofVirtual().start(() -> {
			
			try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String errorLine;
				while ((errorLine = errorReader.readLine()) != null) {
					
					xLogger.logError(errorLine);
				}
			} catch (IOException e) {
				log.severe("Error reading process error stream: " + e.getMessage());
			}
		});
	}
	
	
	
	private void executeProcess() {
		
		parentThread = Thread.ofVirtual().start(() -> {
			
			try {
				Process process = processBuilder.start();
				xLogger.log("Running app " + getName() + " version " + getVersion());
				startChildThreads(process);
				
				getProcessMetadata(process);
				int exitCode = process.waitFor();
				if (exitCode != 0) {
					xLogger.log("Process was terminated by an external program with exit code: " + exitCode);
					xLogger.logError("Process was terminated by an external program with exit code: " + exitCode);
					interruptThreads();
					
				} else {
					xLogger.log("Process exited normally");
					interruptThreads();
				}
				
			} catch (IOException | InterruptedException e) {
				log.severe("Error running app " + getName() + ": " + e.getMessage());
			}
		});
	}
	
	public Long getPid() {
		return pid;
	}
	
	public String getProcessRamUse() {
		return getPIDRAMUsage(String.valueOf(pid));
	}
	
	public String getProcessUpTime() {
		if (startTime == null) {
			return null;
		}
		
		Duration uptime  = Duration.between(startTime, Instant.now());
		long     years   = uptime.toDaysPart() / 365;
		long     months  = (uptime.toDaysPart() % 365) / 30;
		long     weeks   = (uptime.toDaysPart() % 365) % 30 / 7;
		long     days    = (uptime.toDaysPart() % 365) % 30 % 7;
		long     hours   = uptime.toHoursPart() % 24;
		long     minutes = uptime.toMinutesPart() % 60;
		long     seconds = uptime.toSecondsPart();
		
		StringBuilder uptimeString = new StringBuilder();
		
		if (years > 0) {
			uptimeString.append(years).append(" year").append(years > 1 ? "s" : "");
			months = months % 12; // Subtract the months already included in years
			if (months > 0) {
				uptimeString.append(", ").append(months).append(" month").append(months > 1 ? "s" : "");
			}
		} else if (months > 0) {
			uptimeString.append(months).append(" month").append(months > 1 ? "s" : "");
		}
		
		if (weeks > 0) {
			uptimeString.append(", ").append(weeks).append(" week").append(weeks > 1 ? "s" : "");
		}
		
		if (days > 0) {
			uptimeString.append(", ").append(days).append(" day").append(days > 1 ? "s" : "");
		}
		
		if (hours > 0) {
			uptimeString.append(", ").append(hours).append(" hour").append(hours > 1 ? "s" : "");
		}
		
		if (minutes > 0) {
			uptimeString.append(" ").append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
		}
		
		if (seconds > 0 || uptimeString.isEmpty()) {
			uptimeString.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
		}
		
		System.out.println("Process uptime: " + uptimeString);
		return uptimeString.toString();
	}
	
	private void getProcessMetadata( Process process ) {
		
		process.info().totalCpuDuration();
		pid = process.pid();
		if (process.info().startInstant().isPresent()) {
			startTime = process.info().startInstant().get();
		}
	}
	private void interruptThreads() {
		if (parentThread != null) {
			parentThread.interrupt();
		}
		if (childOutputLoggerThread != null) {
			childOutputLoggerThread.interrupt();
		}
		if (childErrorOutputLoggerThread != null) {
			childErrorOutputLoggerThread.interrupt();
		}
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
	
	public String getSdkPath() {
		return sdkPath;
	}
	
	public void setSdkPath( String sdkPath ) {
		this.sdkPath = sdkPath;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	public String getVersion() {
		return version == null ? "0" : version;
	}
	
	public void setVersion( String version ) {
		this.version = version;
	}
	
	public String getDescription() {
		return description == null ? "--":description;
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
		return exeCommand == null ? "": exeCommand;
	}
	
	public void setExeCommand( String exeCommand ) {
		this.exeCommand = exeCommand;
	}
}
