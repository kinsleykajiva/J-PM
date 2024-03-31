package africa.jopen.process;

import africa.jopen.utils.XLogger;
import africa.jopen.utils.XSystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AppProcess {
	 Logger log = Logger.getLogger(AppProcess.class.getName());
	private XLogger xLogger ;
	private Integer id      = 0;
	private String       name        ="--";
	private String       version     ="0.0";
	private String       description = "No description";
	private List<String> tags        = new ArrayList<>();
	private String       exeCommand  = "";
	
	
	public void runApp(String appFileName,String args){
		System.out.println("Running app " + getName() + " version " + getVersion());
		xLogger = new XLogger(appFileName);
		ProcessBuilder processBuilder = new ProcessBuilder();
		String os = System.getProperty("os.name").toLowerCase();
		
		if(appFileName.endsWith("jar")){
			if(XSystemUtils.isSystemJavaFound()) {
				if (os.startsWith("win")) {
					processBuilder.command("cmd.exe", "/c", "java -jar " + appFileName + " " + args);
				} else {
					processBuilder.command("bash", "-c", "java -jar " + appFileName + " " + args);
				}
			}
		}
		
		processBuilder.redirectErrorStream(true);
		try {
			Process process = processBuilder.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
//					System.out.println(line);
					xLogger.log(line);
				}
			}
			
			try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				String errorLine;
				while ((errorLine = errorReader.readLine()) != null) {
					
					xLogger.logError(errorLine);
				}
			}
			
			int exitCode = process.waitFor();
		} catch (IOException | InterruptedException e) {
			log.severe("Error running app " + getName() + ": " + e.getMessage());
		}
		
		
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
