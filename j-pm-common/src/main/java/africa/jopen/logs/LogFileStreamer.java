package africa.jopen.logs;

import java.io.*;

public class LogFileStreamer implements Runnable {
	
	private final    String           logFilePath;
	private volatile boolean          canRun   = true;
	private          boolean          tailFile = false;
	//private          RandomAccessFile randomAccessFile;
	
	public LogFileStreamer( String logFilePath, boolean tailFile ) {
		this.logFilePath = logFilePath;
		this.tailFile = tailFile;
		var file = new File(logFilePath);
		if (!file.exists()) {
			canRun = false;
			System.err.println("[X] Log file does not exist");
			//throw new IllegalArgumentException("Log file does not exist");
		}
		
	}
	
	private long lastKnownPosition = 0;
	//private        boolean shouldIRun       = true;
	
	private static int     crunchifyCounter = 0;
	@Override
	public void run() {
		if (!canRun) return;
		if (tailFile) {
			try {
				File    crunchifyFile = new File(logFilePath);
				while (tailFile) {
					int crunchifyRunEveryNSeconds = 2000;
					Thread.sleep(crunchifyRunEveryNSeconds);
					long fileLength = crunchifyFile.length();
					if (fileLength > lastKnownPosition) {
						// Reading and writing file
						RandomAccessFile readWriteFileAccess = new RandomAccessFile(crunchifyFile, "rw");
						readWriteFileAccess.seek(lastKnownPosition);
						String crunchifyLine = null;
						while ((crunchifyLine = readWriteFileAccess.readLine()) != null) {
							System.out.println(crunchifyLine);
							crunchifyCounter++;
						}
						lastKnownPosition = readWriteFileAccess.getFilePointer();
						readWriteFileAccess.close();
					} else {
					
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void stop() {
		canRun = false;
	}
}
