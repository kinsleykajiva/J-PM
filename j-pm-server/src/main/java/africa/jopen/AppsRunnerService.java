package africa.jopen;

import io.helidon.config.Config;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

import java.util.Collections;

public class AppsRunnerService implements HttpService {
	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());
	
	
	AppsRunnerService() {
		this(Config.global().get("app"));
	}
	AppsRunnerService(Config appConfig) {
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
	private void index( ServerRequest request, ServerResponse response) {
		sendSuccessResponse(response, "World");
	}
	
	private void runApp( ServerRequest request, ServerResponse response) {
		String req = request.content().as(String.class);
		
		
		sendSuccessResponse(response, "World");
	}
	private void stopDeleteApp( ServerRequest request, ServerResponse response) {
		sendSuccessResponse(response, "World");
	}
	
	private void sendSuccessResponse(ServerResponse response, String message) {
		JsonObject successObject = Json.createObjectBuilder()
				.add("success", true)
				.add("message", message)
				.build();
		response.send(successObject);
	}
	
	private void sendErrorResponse(ServerResponse response, String message) {
		JsonObject errorObject = Json.createObjectBuilder()
				.add("success", false)
				.add("error", message)
				.build();
		response.send(errorObject);
	}
	
}
