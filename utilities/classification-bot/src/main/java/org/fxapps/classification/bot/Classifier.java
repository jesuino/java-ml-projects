package org.fxapps.classification.bot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

public class Classifier {


	private static String[] labels;
	private static ComputationGraph model;
	private static NativeImageLoader loader;
	
	
	public static void init() throws IOException {
		String modelPath = Properties.classifierModelPath(); 
		labels = Properties.classifierLabels();
		int[] format = Properties.classifierInputFormat();
		loader = new NativeImageLoader(format[0], format[1], format[2]);
		model = ModelSerializer.restoreComputationGraph(modelPath);
		model.init();
	}

	public static String classify(InputStream is) throws IOException {
		INDArray imageMatrix = loader.asMatrix(is);
		INDArray[] output = model.output(imageMatrix);
		StringBuffer sb = new StringBuffer();
		Map<String, Double> values = new HashMap<>();
		for (int i = 0; i < labels.length; i++) {
			double result = (double) output[0].getFloat(i);
			if (result > 0.005) {
				values.put(labels[i], result * 100);
			}
		}
		Map<String, Double> sortedMap = MapUtil.sortByValue(values);
		sortedMap.forEach((label, value) -> sb.append(label + ": " + String.format("%.2f", value) + "%\n"));
		return sb.toString();
	}

}
