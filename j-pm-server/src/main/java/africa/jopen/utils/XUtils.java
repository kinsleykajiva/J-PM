package africa.jopen.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XUtils {
	
	
	public static String getCurrentDateTime() {
		LocalDateTime     now       = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return now.format(formatter);
	}
}
