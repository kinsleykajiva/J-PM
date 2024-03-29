package africa.jopen.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CacheModel {
	
	private Integer serverPort =50501;
	private List<AppModel> appModelList = new ArrayList<>();
	
	public Integer getServerPort() {
		return serverPort;
	}
	
	public void setServerPort( Integer serverPort ) {
		this.serverPort = serverPort;
	}
	
	public List<AppModel> getAppModelList() {
		return appModelList;
	}
	
	
	public void addAppModel (AppModel appModel) {
		appModel.setId(createAppId());
		this.appModelList.add(appModel);
	}
	
	public int createAppId() {
		if (appModelList.isEmpty()) {
			return 1; // If the list is empty, return 1 as the first ID
		}
		
		// Create a sorted list of existing IDs using streams
		List<Integer> existingIds = appModelList.stream()
				.map(AppModel::getId)
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
	
}
