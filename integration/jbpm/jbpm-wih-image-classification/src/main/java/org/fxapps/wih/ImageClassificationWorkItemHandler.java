package org.fxapps.wih;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.deeplearning4j.zoo.model.ResNet50;
import org.deeplearning4j.zoo.util.imagenet.ImageNetLabels;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.process.workitem.core.util.Wid;
import org.jbpm.process.workitem.core.util.WidMavenDepends;
import org.jbpm.process.workitem.core.util.WidParameter;
import org.jbpm.process.workitem.core.util.WidResult;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.Cacheable;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;

/**
 * @author wsiqueir
 *
 */
@Wid(widfile = "CustomDefinitions.wid", name = "CustomDefinitions", displayName = "ImageClassification", icon = "CustomIcon.png", 
		defaultHandler = "mvel: new org.fxapps.wih.ImageClassificationWorkItemHandler()", 
		parameters = { @WidParameter(name = ImageClassificationWorkItemHandler.PARAM_IMAGE) }, 
		results = { @WidResult(name = ImageClassificationWorkItemHandler.RESULT_PREDICTIONS) }, 
		mavenDepends = {@WidMavenDepends(group = "org.fxapps.wih", artifact = "jbpm-wih-image-classification", version = "1.0") })
public class ImageClassificationWorkItemHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

	public final static String PARAM_IMAGE = "image";
	public final static String RESULT_PREDICTIONS = "predictions";
	public static final String RESULT_PREDICTION = "prediction";
	
//	private static final String DEFAULT_MODEL = "/vgg16_dl4j_cifar10_inference.v1.zip";
	private static final String DEFAULT_MODEL = "/resnet50_dl4j_inference.v3.zip";

	private static final String LABELS_SEPARATOR = "\\,";

	private ImageClassifier classifier;

	public ImageClassificationWorkItemHandler(ClassLoader cl, String model, String labels, int width, int height, int channels) {
		this(cl.getResourceAsStream(model), labels, width, height, channels);
	}

	public ImageClassificationWorkItemHandler(File model, String labels, int width, int height, int channels)
			throws FileNotFoundException {
		this(new FileInputStream(model), labels, width, height, channels);
	}

	public ImageClassificationWorkItemHandler(URL model, String labels, int width, int height, int channels)
			throws IOException {
		this(model.openStream(), labels, width, height, channels);
	}

	public ImageClassificationWorkItemHandler(InputStream model, String labels, int width, int height, int channels) {
		this(model, getLabels(labels), width, height, channels, null);
	}
	
	public ImageClassificationWorkItemHandler(InputStream model, String labels, int width, int height, int channels, DataNormalization normalization) {
		this(model, getLabels(labels), width, height, channels, normalization);
	}
	
	/**
	 * By default it offers Resnet50 trained on CIFAR-10 dataset
	 * 
	 * @throws IOException
	 *  
	 */
	public ImageClassificationWorkItemHandler() {
		List<String> labels = getLabels(ImageClassificationWorkItemHandler.class.getResourceAsStream("/imagenet.txt"));
		try {
			classifier = new ImageClassifier(ResNet50.builder().build().initPretrained(), labels, 
					224, 224, 3, null);
		} catch (IOException e) {
			throw new Error("Not able to initialize default model", e);
		}
	}
	
	private ImageClassificationWorkItemHandler(InputStream model, List<String> labels, int width, int height,
			int channels, DataNormalization normalization) {
		classifier = new ImageClassifier(model, labels, width, height, channels, normalization);
	}

	private static List<String> getLabels(InputStream labelsIS) {
		String labels = "";
		try {
			labels = new String(IOUtils.toByteArray(labelsIS));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Arrays.stream(labels.split(LABELS_SEPARATOR)).collect(Collectors.toList());
	}
	private static List<String> getLabels(String labels) {
		return Arrays.stream(labels.split(LABELS_SEPARATOR)).collect(Collectors.toList());
	}

	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			InputStream imageIS = null;
			Object inputImage = workItem.getParameter(PARAM_IMAGE);
			if (inputImage instanceof InputStream) {
				imageIS = (InputStream) inputImage;
			} else if (inputImage instanceof File) {
				imageIS = new FileInputStream((File) inputImage);
			} else if (inputImage instanceof URL) {
				imageIS = ((URL) inputImage).openStream();
			} else {
				throw new IllegalArgumentException("Input Parameter " + PARAM_IMAGE + " has an invalid type.");
			}
			Map<String, Object> results = new HashMap<String, Object>();
			Map<String, Double> predictions = classifier.classifyImage(imageIS);
			results.put(RESULT_PREDICTIONS, predictions);
			results.put(RESULT_PREDICTION, getPrediction(predictions));
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Throwable cause) {
			System.out.println("Error running WIH: " + cause.getMessage());
			handleException(cause);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	
	private String getPrediction(Map<String, Double> predictions) {
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
}
