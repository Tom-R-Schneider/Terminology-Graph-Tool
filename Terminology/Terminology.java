package Terminology;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Terminology {
	
	private String term;
	private String id;
	private String gender;
	private String domain;
	private String last_changed;
	private String definition;
	private String definition_source;
	private String example;
	private String example_source;
	private String image_link;
	
	public Terminology(Map<String, String> map) {
		
		this.term = map.get("term");
		this.id = map.get("id");
		this.gender = map.get("gender");
		this.domain = map.get("domain");
		this.last_changed = map.get("last_changed");
		this.definition = map.get("definition");
		this.definition_source = map.get("definition_source");
		this.example = map.get("example");
		this.example_source = map.get("example_source");
		this.image_link = map.get("image_link");		
	}
	
	public Map<String, String> get_all_term_information() {
		
		Map<String, String> map = new HashMap<>();
		map.put("term", this.term);
		map.put("id", this.id);
		map.put("gender", this.gender);
		map.put("domain", this.domain);
		map.put("last_changed", this.last_changed);
		map.put("definition", this.definition);
		map.put("definition_source", this.definition_source);
		map.put("example", this.example);
		map.put("example_source", this.example_source);
		map.put("image_link", this.image_link);
		
		return map;
	}
	
	public String get_term() {
		
		return this.term;
	}
	
	public void update_term_info(Map<String, String> term_information) {
		
	}
	
	public void update_domain_structure(String graph_position) {
		
	}
}
