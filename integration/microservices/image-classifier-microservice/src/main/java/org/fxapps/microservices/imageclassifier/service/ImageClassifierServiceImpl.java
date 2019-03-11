package org.fxapps.microservices.imageclassifier.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.model.ResNet50;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fxapps.microservices.imageclassifier.model.ClassificationResults;
import org.fxapps.microservices.imageclassifier.model.ClassifierInfo;
import org.nd4j.linalg.api.ndarray.INDArray;

@Default
@ApplicationScoped
public class ImageClassifierServiceImpl implements ImageClassifierService {

	public static String DEFAULT_MODEL_TYPE = "Resnet50 trained on Imagenet database";
	
	private static final String LABELS_SEPARATOR = "\\,";

	/**
	 * A path to the model in the classpath.
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_PATH, defaultValue= "")
	String modelPath;

	/**
	 * Labels for the model. It should be comma separated
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_LABELS, defaultValue="")
	String labelsStr;
	
	/**
	 * The model type
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_TYPE, defaultValue="CUSTOM")
	String modelType;


	/**
	 * The input image width
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_INPUT_WIDTH, defaultValue= "224")
	int inputWidth;

	/**
	 * The input image height
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_INPUT_HEIGHT, defaultValue= "224")
	int inputHeight;

	/**
	 * The number of channels
	 */
	@Inject
	@ConfigProperty(name=ServiceProperties.MODEL_INPUT_CHANNELS, defaultValue= "3")
	private int inputChannels;
	
	private Model model;

	private float EPS = 0.025f;

	private ClassifierInfo classifierInfo;

	// equivalent of startup bean as described in
	// https://rmannibucau.wordpress.com/2015/03/10/cdi-and-startup/
	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		classifierInfo = new ClassifierInfo();
		try {
			if (!Objects.isNull(modelPath) && !modelPath.trim().isEmpty()) {
				System.out.println("Loading model from classpath: " + modelPath);
				loadCustomModel();
			} else {
				System.out.println("Loading default model... It may take a while if you are running it for the first time.");
				loadDefaultModel();
			}
		} catch (Exception e) {
			System.out.println("Erro loading model " + e.getMessage());
			throw new Error("Error loading model and labels.", e);
		}
	}

	public ClassificationResults classify(InputStream imageStream) {
		INDArray imageArray = imageToArray(imageStream, inputWidth, inputHeight, inputChannels);
		INDArray output = getOutput(model, imageArray);
		ClassificationResults results = new ClassificationResults();
		Map<String, Double> predictions = new HashMap<>();
		for (int i = 0; i < output.columns(); i++) {
			Float v = output.getFloat(i);
			if (Math.abs(v - EPS) > EPS) {
				String label = "label " + i;
				if (i < classifierInfo.getLabels().size()) {
					label = classifierInfo.getLabels().get(i);
				}
				predictions.put(label, v.doubleValue());
			}
		}
		results.setPredictions(predictions);
		results.setPrediction(highestScorePrediction(predictions));
		return results;
	}

	public ClassifierInfo info() {
		return classifierInfo;
	}

	private void loadDefaultModel() throws IOException {
		model = ResNet50.builder().build().initPretrained();
		InputStream isLabels = getClass().getResourceAsStream("/labels/imagenet.txt");
		classifierInfo.setLabels(getLabels(isLabels));
		classifierInfo.setType(DEFAULT_MODEL_TYPE);
	}

	private void loadCustomModel() {
		InputStream modelIS = ImageClassifierServiceImpl.class.getResourceAsStream(modelPath);
		model = loadModel(modelIS);
		classifierInfo.setLabels(getLabels(labelsStr));
		classifierInfo.setType(modelType);
	}
	
	// utility methods
	private Model loadModel(InputStream is) {
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
		return imageArray;
	}

	private INDArray getOutput(Model model, INDArray image) {
		if (model instanceof MultiLayerNetwork) {
			MultiLayerNetwork multiLayerNetwork = (MultiLayerNetwork) model;
			multiLayerNetwork.init();
			return multiLayerNetwork.output(image);
		} else {
			ComputationGraph graph = (ComputationGraph) model;
			graph.init();
			return graph.output(image)[0];
		}
	}
	
	private String highestScorePrediction(Map<String, Double> predictions) {
		Double predictionValue = null;
		String prediction = null;
		for (Map.Entry<String, Double> e : 	predictions.entrySet()) {
			if(predictionValue == null) {
				prediction = e.getKey();
				predictionValue = e.getValue();
			}
			if(Double.compare(e.getValue(), predictionValue) > 0) {
				predictionValue = e.getValue();
				prediction = e.getKey();
			}
		}
		return prediction;
	}

	private static List<String> getLabels(InputStream labelsIS) {
		String labelsStr = "";
		try {
			labelsStr = new String(IOUtils.toByteArray(labelsIS));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getLabels(labelsStr);
	}
	
	private static List<String> getLabels(String labels) {
		return Arrays.stream(labels.split(LABELS_SEPARATOR)).collect(Collectors.toList());
	}
	
}
