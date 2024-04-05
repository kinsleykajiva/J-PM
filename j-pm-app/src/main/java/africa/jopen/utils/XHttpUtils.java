package africa.jopen.utils;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.LongConsumer;

public class XHttpUtils {
	
	private static String url = "http://localhost:8080/app";
	
	public static String getRequest( String endpoint ) {
		try {
			System.out.println(url + "/" + endpoint);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/" + endpoint))
					.header("Authorization", "Bearer qwYTYRTYRTYRTYT12")
					.header("Content-Type", "application/json")
					
					.GET()
					.build();
			HttpResponse<String> response;
			try (HttpClient client = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(120))
					.build()) {
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
			}
			//System.out.println(response.statusCode());
			//System.out.println(response.body());
			return response.body();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public static String postRequest( String endpoint, String jsonPayload ) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url + "/" + endpoint))
					.header("Authorization", "Bearer qwYTYRTYRTYRTYT12")
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
					.build();
			HttpResponse<String> response;
			try (HttpClient client = HttpClient.newBuilder()
					.version(HttpClient.Version.HTTP_1_1)
					.followRedirects(HttpClient.Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(120))
					.build()) {
				response = client.send(request, HttpResponse.BodyHandlers.ofString());
			}
			//System.out.println(response.statusCode());
			//System.out.println(response.body());
			return response.body();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public static void downloadFile( String url, String saveDir ) throws IOException, InterruptedException {
		Path               saveFilePath;
		HttpResponse<Path> response;
		try (HttpClient client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(java.time.Duration.ofSeconds(20))
				.build()) {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.build();
			
			String fileName = getFileNameFromUrl(url);
			saveFilePath = Paths.get(saveDir, fileName);
			
			HttpResponse.BodyHandler<Path> responseHandler = callbackBodyHandler(
					(1_000_000 / 1_000), // Interval for progress update (e.g., every 1 MB)
					nrBytesReceived -> System.out.print("#"),
					HttpResponse.BodyHandlers.ofFile(saveFilePath)
			);
			
			response = client.send(request, responseHandler);
		}
		
		// The program continues after the download is finished
		System.out.println("\nDownload completed!");
		if (response.statusCode() == 200) {
			System.out.println("\nFile downloaded to: " + saveFilePath);
		} else {
			System.out.println("\nFailed to download file. Server replied with status code: " + response.statusCode());
		}
	}
	
	private static String getFileNameFromUrl( String url ) {
		try {
			return new File(new URL(url).getPath()).getName();
		} catch (MalformedURLException e) {
			// Handle the exception appropriately
			e.printStackTrace();
			return "downloaded-file";
		}
	}
	
	private static <T> HttpResponse.BodyHandler<T> callbackBodyHandler(
			int interval, LongConsumer callback, HttpResponse.BodyHandler<T> handler ) {
		return info -> new HttpResponse.BodySubscriber<T>() {
			private final HttpResponse.BodySubscriber<T> delegateSubscriber = handler.apply(info);
			private       long                           receivedBytes      = 0;
			private       long                           calledBytes        = 0;
			
			@Override
			public void onSubscribe( Flow.Subscription subscription ) {
				delegateSubscriber.onSubscribe(subscription);
			}
			
			@Override
			public void onNext( List<ByteBuffer> item ) {
				receivedBytes += item.stream().mapToLong(ByteBuffer::capacity).sum();
				if (receivedBytes - calledBytes > interval) {
					callback.accept(receivedBytes);
					calledBytes = receivedBytes;
				}
				delegateSubscriber.onNext(item);
			}
			
			@Override
			public void onError( Throwable throwable ) {
				delegateSubscriber.onError(throwable);
			}
			
			@Override
			public void onComplete() {
				delegateSubscriber.onComplete();
			}
			
			@Override
			public CompletionStage<T> getBody() {
				return delegateSubscriber.getBody();
			}
		};
	}
	
	private static void printDownloadProgress( long bytesRead, long contentLength, int totalChunks ) {
		int progress        = (int) (bytesRead * 100 / contentLength);
		int chunkSize       = progress / totalChunks;
		int completedChunks = chunkSize;
		
		// Build progress bar
		StringBuilder progressbar = new StringBuilder("[");
		for (int i = 0; i < totalChunks; i++) {
			if (i < completedChunks) {
				progressbar.append("=");
			} else {
				progressbar.append("-");
			}
		}
		progressbar.append("]");
		
		System.out.print("\r" + progressbar + " " + progress + "%");
	}
	
	
}
