package africa.jopen.utils;

import java.io.File;

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
}
