package africa.jopen.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class XFilesUtils {
	private static final String appBaseFolder = ".jpm";
	public static String getAppFolderPath() {
		String folder= System.getProperty("user.home") + File.separator+ appBaseFolder +File.separator;
		
		try{
			if(!new File(folder).exists()) {
				
				var res =new File(folder).mkdirs();
				if(res) {
					System.out.println(folder + " was created");
				}else{
					System.out.println(folder + " already exists");
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return  folder;
	}
	
	public static String getCacheFile() {
		String cacheFilePath = getAppFolderPath() + "apps-cache.json";
		File file = new File(cacheFilePath);
		
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs(); // Create parent directories if they don't exist
				file.createNewFile(); // Create the file
				Files.write(Paths.get(cacheFilePath), "{}".getBytes()); // Write empty JSON object
			} catch (IOException e) {
				e.printStackTrace(); // Handle file creation error
			}
		}
		return cacheFilePath;
	}
}
