package africa.jopen;

import africa.jopen.process.AppProcess;
import io.helidon.config.Config;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.*;
import jakarta.json.stream.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static africa.jopen.utils.XSystemUtils.checkIfProcessExists;

public class AppsRunnerServiceRoute implements HttpService {
	private static final JsonBuilderFactory JSON         = Json.createBuilderFactory(Collections.emptyMap());
	private final        List<AppProcess>   appModelList = new ArrayList<>();
	Logger log = Logger.getLogger(AppProcess.class.getName());
	public void addAppModel( AppProcess appModel ) {
		appModel.setId(createAppId());
		this.appModelList.add(appModel);
	}
	
	public int createAppId() {
		if (appModelList.isEmpty()) {
			return 1; // If the list is empty, return 1 as the first ID
		}
		
		// Create a sorted list of existing IDs using streams
		List<Integer> existingIds = appModelList.stream()
				.map(AppProcess::getId)
				.sorted()
				.toList();
		
		// Find the first missing ID using streams
		int lastId = 0;
		for (int id : existingIds) {
			if (id - lastId > 1) {
				return lastId + 1;
			}
			lastId = id;
		}
		
		// If no missing number found, return the next number after the highest existing ID
		return existingIds.getLast() + 1;
	}
	
	AppsRunnerServiceRoute() {
		this(Config.global().get("app"));
	}
	
	AppsRunnerServiceRoute( Config appConfig ) {
		//greeting.set(appConfig.get("greeting").asString().orElse("Ciao"));
	}
	
	@Override
	public void routing( HttpRules rules ) {
		rules.get("/", this::index)
				.post("/run", this::runApp)
				.post("/stop-delete", this::stopDeleteApp)
		;
		
	}
	
	/**
	 * Return a worldly greeting message.
	 *
	 * @param request  the server request
	 * @param response the server response
	 */
	private void index( ServerRequest request, ServerResponse response ) {
		JSONObject responseObj = new JSONObject();
		JSONArray apps=new JSONArray();
		appModelList.forEach(x->{
			apps.put(x.toJsonObject());
		});
		responseObj.put("apps", apps);
		sendSuccessResponse(response, "running apps",responseObj.toString());
	}
	
	private void runApp(ServerRequest request, ServerResponse response) {
		String bodyText = request.content().as(String.class);
		log.info("Event bodyText: " + bodyText);
		
		JSONObject responseObj = new JSONObject();
		JSONObject body = new JSONObject(bodyText);
		
		int id;
		if (body.has("id") && !body.isNull("id")) {
			Object idObj = body.get("id");
			if (idObj instanceof Number) {
				id = ((Number) idObj).intValue();
			} else {
				id = -1;
			}
		} else {
			id = -1;
		}
		
		//int id = body.optInt("id", -1);// this is an update to restart
		boolean isRestart = body.optBoolean("isRestart", false);
		String appName = body.optString("appName", null);
		Long pidValue = body.optLong("pid", 0);
		String filePath = body.optString("filePath", null);
		String sdkPath = body.optString("sdkPath", null);
		String description = body.optString("description", null);
		String version = body.optString("version", null);
	
		
		if((appName != null && appName.isEmpty())
		&& (id == -1)  && isRestart
		){
			// means restart all apps
			appModelList.forEach(appProcess->{
				
					updateAppProcess(appProcess, appName, version, description, sdkPath);
					restartAppProcess(appProcess);
					long pid = waitForProcessStart(appProcess);
					if (pid != 0) {
						appProcess.toJsonObject();
					} else {
						log.warning("Process failed to start after multiple attempts.");
					}
				
			});
			index(request,response);
			return;
		}
		
		
		boolean appExists = appModelList.stream().anyMatch(appModel ->
				appModel.getName().equals(appName) || appModel.getId()== id
		);
		/*var file = new File(filePath);
		if(!file.exists()){
			sendErrorResponse(response, "File not found");
			return;
		}*/
		
		if (appExists && !isRestart) {
			Optional<AppProcess> existingAppProcess = appModelList.stream()
					.filter(x -> x.getName().equals(appName))
					.findFirst();
			if(existingAppProcess.isPresent()){
				responseObj.put("app",existingAppProcess.get().toJsonObject());
				sendSuccessResponse(response, appName + " App Already Running", responseObj.toString());
				return;
			}
		}
		if (appExists && isRestart) {
			Optional<AppProcess> existingAppProcess = appModelList.stream()
					.filter(x -> x.getName().equals(appName))
					.findFirst();
			
			existingAppProcess.ifPresent(appProcess -> {
				updateAppProcess(appProcess, appName, version, description, sdkPath);
				restartAppProcess(appProcess);
				long pid = waitForProcessStart(appProcess);
				if (pid != 0) {
					//populateResponse(responseObj, appProcess, pid);
					responseObj.put("app", appProcess.toJsonObject());
				} else {
					log.warning("Process failed to start after multiple attempts.");
				}
			});
			
		} else {
			AppProcess appProcess = new AppProcess();
			updateAppProcess(appProcess, appName, version, description, sdkPath);
			appProcess.runApp(filePath, "");
			Long pid = waitForProcessStart(appProcess);
			if (pid != 0) {
				System.out.println("xxx");
				//populateResponse(responseObj, appProcess, pid);
				responseObj.put("app", appProcess.toJsonObject());
				appModelList.add(appProcess);
			} else {
				log.warning("Process failed to start after multiple attempts.");
			}
		}
		
		sendSuccessResponse(response,"Started App " + appName + " successfully", responseObj.toString());
	}
	
	private void updateAppProcess(AppProcess appProcess, String appName, String version, String description, String sdkPath) {
		if( appName != null  && !appName.isEmpty()) {
			appProcess.setName(appName);
		}
		
		if( version != null  && !version.isEmpty()) {
			appProcess.setVersion(version);
		}
		if( description != null  && !description.isEmpty()) {
			appProcess.setDescription(description);
		}
		if( sdkPath != null  && !sdkPath.isEmpty()) {
			appProcess.setSdkPath(sdkPath);
		}
	}
	
	private void restartAppProcess(AppProcess appProcess) {
		appProcess.restart();
	}
	
	private long waitForProcessStart(AppProcess appProcess) {
		int attempts = 0;
		Long pid = null;
		int MAX_ATTEMPTS = 5;
		long DELAY_SECONDS = 1;
		
		while (pid == null && attempts < MAX_ATTEMPTS) {
			pid = appProcess.getPid();
			if (pid != null) {
				log.info("Process started with PID: " + pid);
				break;
			}
			attempts++;
			log.info("Attempt " + attempts + ": PID is null");
			try {
				TimeUnit.SECONDS.sleep(DELAY_SECONDS);
			} catch (InterruptedException e) {
				log.warning("Thread sleep interrupted.");
				Thread.currentThread().interrupt();
			}
		}
		return pid;
	}
	
	
	/*private void sendSuccessResponse(ServerResponse response, String responseData) {
		JsonObject returnObject = JSON.createObjectBuilder()
				.add("message", responseData)
				.build();
		response.send(returnObject);
	}*/
	
	private void stopDeleteApp( ServerRequest request, ServerResponse response ) {
		String bodyText = request.content().as(String.class);
		log.info("Event bodyText: " + bodyText);
		
		JSONObject responseObj = new JSONObject();
		JSONObject body = new JSONObject(bodyText);
		
		String appName = body.optString("appName", null);
		Long pid = body.optLong("pid", 0);
		
		if (appName == null || pid == 0) {
			sendErrorResponse(response, "Missing required fields in the request body");
			return;
		}
		try{
			Optional<AppProcess> appProcess = appModelList.stream()
					.filter(x -> x.getName().equals(appName) || x.getPid().equals(pid))
					.findFirst();
			appProcess.ifPresent(AppProcess::stop);
			responseObj.put("IsRunning",checkIfProcessExists(appProcess.get().getPid()));
			appModelList.remove(appProcess.get());
			sendSuccessResponse(response,"Stopped App", responseObj.toString());
		}catch (Exception e){
			log.warning("App not found");
			sendErrorResponse(response, "Failed to stop app");
		}
		
		
	}
	
	private void sendSuccessResponse( ServerResponse response, String message,String data ) {
	
		JsonReader jsonReader = Json.createReader(new StringReader(data));
		JsonObject jsonData = jsonReader.readObject();
		JsonObjectBuilder builder    = Json.createObjectBuilder();

		builder.add("success", true);
		builder.add("message", message);
		builder.add("data", jsonData);
		JsonObject successObject = builder.build();
		response.send(successObject);
	}
	
	private void sendErrorResponse( ServerResponse response, String message ) {
		JsonObject errorObject = Json.createObjectBuilder()
				.add("success", false)
				.add("error", message)
				.add("message", message)
				.build();
		response.send(errorObject);
	}
	
}
