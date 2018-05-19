package org.fxapps.ml.deeplearning.yolo;

import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2RGB;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.transform.ColorConversionTransform;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.model.YOLO2;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;

public class YOLOModel {
	private String modelPath = System.getProperty("model.path");
	private String classes = System.getProperty("model.classes");
	private String inputInfo = System.getProperty("model.input.info");
	private String grid = System.getProperty("model.grid");

	private final String[] COCO_CLASSES = { "person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train",
			"truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
			"dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag",
			"tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove",
			"skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl",
			"banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
			"sofa", "pottedplant", "bed", "diningtable", "toilet", "tvmonitor", "laptop", "mouse", "remote", "keyboard",
			"cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors",
			"teddy bear", "hair drier", "toothbrush" };

	private final int INPUT_WIDTH = 416;
	private final int INPUT_HEIGHT = 416;
	private final int INPUT_CHANNELS = 3;
	private final int GRID_W = 13;
	private final int GRID_H = 13;

	private String[] modelClasses;

	private ComputationGraph yoloModel;

	private int inputWidth = INPUT_WIDTH;
	private int inputHeight = INPUT_HEIGHT;
	private int inputChannels = INPUT_CHANNELS;
	private int gridW = GRID_W;
	private int gridH = GRID_H;

	private NativeImageLoader imageLoader;

	public void init() {
		try {
			if (Objects.isNull(modelPath)) {

				yoloModel = (ComputationGraph) YOLO2.builder().build().initPretrained();
				setModelClasses(COCO_CLASSES);
			} else {
				yoloModel = ModelSerializer.restoreComputationGraph(modelPath);
				if (!(yoloModel.getOutputLayer(0) instanceof Yolo2OutputLayer)) {
					throw new Error("The model is not an YOLO model (output layer is not Yolo2OutputLayer)");
				}
				setModelClasses(classes.split("\\,"));
			}
			imageLoader = new NativeImageLoader(getInputWidth(), getInputHeight(), getInputChannels(),
					new ColorConversionTransform(COLOR_BGR2RGB));
			loadInputParameters();
		} catch (IOException e) {
			throw new Error("Not able to init the model", e);
		}
	}

	private void loadInputParameters() {
		if (inputInfo != null) {
			try {
				String[] imgInfo = inputInfo.split("\\,");
				setInputWidth(Integer.parseInt(imgInfo[0]));
				setInputHeight(Integer.parseInt(imgInfo[1]));
				setInputChannels(Integer.parseInt(imgInfo[2]));

			} catch (Exception e) {
				throw new Error("Error reading input info", e);
			}
		}

		if (grid != null) {
			try {
				String[] gridInfo = grid.split("\\,");
				setGridW(Integer.parseInt(gridInfo[0]));
				setGridH(Integer.parseInt(gridInfo[1]));
			} catch (Exception e) {
				System.err.println("Error reading grid information. Using default values: " + GRID_W + " x " + GRID_H);
				setGridH(GRID_H);
				setGridW(GRID_W);
			}
		}
	}

	public List<DetectedObject> run(File input, double threshold) {
		INDArray img;
		try {
			img = loadImage(input);
		} catch (IOException e) {
			throw new Error("Not able to load image from: " + input.getAbsolutePath(), e);
		}
		INDArray output = yoloModel.outputSingle(img);
		Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) yoloModel.getOutputLayer(0);
		return outputLayer.getPredictedObjects(output, threshold);
	}

	private INDArray loadImage(File imgFile) throws IOException {
		INDArray image = imageLoader.asMatrix(imgFile);
		ImagePreProcessingScaler scaler = new ImagePreProcessingScaler(0, 1);
		scaler.transform(image);
		return image;
	}

	public String[] getModelClasses() {
		return modelClasses;
	}

	private void setModelClasses(String[] modelClasses) {
		this.modelClasses = modelClasses;
	}

	public int getInputWidth() {
		return inputWidth;
	}

	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}

	public int getInputHeight() {
		return inputHeight;
	}

	public void setInputHeight(int inputHeight) {
		this.inputHeight = inputHeight;
	}

	public int getInputChannels() {
		return inputChannels;
	}

	public void setInputChannels(int inputChannels) {
		this.inputChannels = inputChannels;
	}

	public int getGridH() {
		return gridH;
	}

	public void setGridH(int gridH) {
		this.gridH = gridH;
	}

	public int getGridW() {
		return gridW;
	}

	public void setGridW(int gridW) {
		this.gridW = gridW;
	}

}
