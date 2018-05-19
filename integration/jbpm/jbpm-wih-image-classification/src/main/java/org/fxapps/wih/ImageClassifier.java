package org.fxapps.wih;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;

public class ImageClassifier {

	Model dl4jModel;
	private List<String> labels;
	private int width;
	private int height;
	private int channels;
	private DataNormalization normalization;

	public ImageClassifier(InputStream modelStream, List<String> labels, int width, int height, int channels,
			DataNormalization normalization) {
		this(loadModel(modelStream), labels, width, height, channels, normalization);
	}

	public ImageClassifier(Model model, List<String> labels, int width, int height, int channels,
			DataNormalization normalization) {
		this.labels = labels;
		this.width = width;
		this.height = height;
		this.channels = channels;
		this.normalization = normalization;
		this.dl4jModel = model;
	}

	public Map<String, Double> classifyImage(InputStream imageIS) {
		INDArray imageArray = imageToArray(imageIS, width, height, channels);
		INDArray output = getOutput(imageArray);
		Map<String, Double> predictions = new HashMap<>();
		// this may not be compatible with all kind of results
		float eps = 0.1f;
		for (int i = 0; i < output.columns(); i++) {
			Float v = output.getFloat(i);
			if (Math.abs(v - eps) > eps) {
				String label = "pos " + i;
				if (i < labels.size()) {
					label = labels.get(i);
				}
				predictions.put(label, v.doubleValue());
			}
		}
		return predictions;
	}

	private INDArray imageToArray(InputStream imageIS, int height, int width, int channels) {
		NativeImageLoader loader = new NativeImageLoader(height, width, channels, true);
		INDArray imageArray = null;
		try {
			if (channels == 1) {
				imageArray = loader.asRowVector(imageIS);
			} else {
				imageArray = loader.asMatrix(imageIS);
			}
		} catch (Exception e) {
			throw new Error("Not able to convert image input stream to array.", e);
		}
		if (normalization != null) {
			normalization.transform(imageArray);
		}
		return imageArray;
	}

	private INDArray getOutput(INDArray image) {
		if (dl4jModel instanceof MultiLayerNetwork) {
			MultiLayerNetwork multiLayerNetwork = (MultiLayerNetwork) dl4jModel;
			multiLayerNetwork.init();
			return multiLayerNetwork.output(image);
		} else {
			ComputationGraph graph = (ComputationGraph) dl4jModel;
			graph.init();
			return graph.output(image)[0];
		}
	}

	private static org.deeplearning4j.nn.api.Model loadModel(InputStream is) {
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("restore", "model");
			org.deeplearning4j.nn.api.Model model;
			tmpFile.deleteOnExit();
			FileUtils.copyInputStreamToFile(is, tmpFile);
			try {
				model = ModelSerializer.restoreMultiLayerNetwork(tmpFile, true);
			} catch (Exception e) {
				model = ModelSerializer.restoreComputationGraph(tmpFile, true);
			}
			return model;
		} catch (Exception e) {
			System.out.println("Error loading model. " + e.getMessage());
			throw new Error("Error loading model.", e);
		} finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
		}
	}

}
