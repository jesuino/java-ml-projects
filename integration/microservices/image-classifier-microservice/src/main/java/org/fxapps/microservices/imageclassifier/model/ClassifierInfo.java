package org.fxapps.microservices.imageclassifier.model;

import java.util.List;

public class ClassifierInfo {
	
	private String type;
	private List<String> labels;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	
	

}
