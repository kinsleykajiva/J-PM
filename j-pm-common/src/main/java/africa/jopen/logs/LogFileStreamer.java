package africa.jopen.logs;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static africa.jopen.utils.XUtils.printErrorMessage;


/**
 * The LogFileStreamer class is responsible for streaming log files. It provides
 * functionality to either tail a log file (monitor for changes and print new lines)
 * or read the entire log file.
 */
public class LogFileStreamer implements Runnable {
	private final Path logFilePath;
	private final boolean tailFile;
	private final Consumer<String> logConsumer;
	private volatile boolean          canRun   = true;
	
	/**
	 * Constructs a new LogFileStreamer instance.
	 *
	 * @param logFilePath The path of the log file to stream.
	 * @param tailFile    A boolean indicating whether to tail the log file (true)
	 *                    or read the entire file (false).
	 * @param logConsumer A Consumer<String> instance that will be called with each
	 *                    line read from the log file.
	 */
	public LogFileStreamer(String logFilePath, boolean tailFile, Consumer<String> logConsumer) {
		this.logFilePath = Paths.get(logFilePath);
		this.tailFile = tailFile;
		this.logConsumer = logConsumer;
		
		if (Files.notExists(this.logFilePath)) {
			canRun = false;
			printErrorMessage("[X] Log file does not exist");
			//throw new IllegalArgumentException("Log file does not exist");
		}
	}
	
	/**
	 * Runs the log file streaming process.
	 * If canRun is false, this method will return immediately.
	 * If tailFile is true, it tails the log file and processes new lines as they are added.
	 * If tailFile is false, it reads the entire log file.
	 */
	@Override
	public void run() {
		if (!canRun) return;
		try {
			if (tailFile) {
				tailFile();
			} else {
				readEntireFile();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Tails the log file by continuously monitoring it for changes using a WatchService.
	 * When changes are detected, it processes the new lines in the log file.
	 *
	 * @throws IOException            if an I/O error occurs while processing the log file.
	 * @throws InterruptedException   if the thread is interrupted while waiting for
	 *                                file changes.
	 */
	private void tailFile() throws IOException, InterruptedException {
		try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
			logFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
			
			while (true) {
				WatchKey key = watchService.poll(2, TimeUnit.SECONDS);
				if (key != null) {
					for (WatchEvent<?> event : key.pollEvents()) {
						if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						processLogFile();
					}
					key.reset();
				}
			}
		}
	}
	/**
	 * Reads the entire log file and processes each line using the provided logConsumer.
	 *
	 * @throws IOException if an I/O error occurs while reading the log file.
	 */
	private void readEntireFile() throws IOException {
		Files.lines(logFilePath)
				.forEach(logConsumer);
	}
	/**
	 * Processes the log file by reading new lines since the last known position
	 * and passing them to the logConsumer.
	 *
	 * @throws IOException if an I/O error occurs while reading the log file.
	 */
	private void processLogFile() throws IOException {
		Files.lines(logFilePath)
				.skip(lastKnownPosition)
				.forEach(logConsumer);
		lastKnownPosition = Files.lines(logFilePath).count();
	}
	
	private long lastKnownPosition = 0;
}