package africa.jopen.models;

import java.util.ArrayList;
import java.util.List;

public class AppModel {
	private Integer id = 0;
	private String name="--";
	private String version="0.0";
	private String description = "No description";
	private List<String> tags = new ArrayList<>();
	private String exeCommand = "";
	
	public void setId( Integer id ) {
		this.id = id;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	public void setVersion( String version ) {
		this.version = version;
	}
	
	public void setDescription( String description ) {
		this.description = description;
	}
	
	public void setTags( List<String> tags ) {
		this.tags = tags;
	}
	
	public void setExeCommand( String exeCommand ) {
		this.exeCommand = exeCommand;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	public String getExeCommand() {
		return exeCommand;
	}
}
