package org.fxapps.microservices.imageclassifier.model;

import java.util.Map;

public class ClassificationResults {
	
	private String prediction;
	private Map<String, Double> predictions;
	
	public String getPrediction() {
		return prediction;
	}
	
	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}
	
	public Map<String, Double> getPredictions() {
		return predictions;
	}
	
	public void setPredictions(Map<String, Double> predictions) {
		this.predictions = predictions;
	}

}