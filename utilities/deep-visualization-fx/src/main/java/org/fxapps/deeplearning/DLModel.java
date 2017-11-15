package org.fxapps.deeplearning;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import com.sun.istack.internal.logging.Logger;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DLModel {

	Logger logger = Logger.getLogger(DLModel.class);
	
	private Model model;
	private Map<Integer, INDArray> activationsCache;
	private String info;
	
	private DLModel(Model model) {
		this.model = model;
		activationsCache = new HashMap<>();
		buildInfo();
	}

	private void buildInfo() {
		String modelName = "";
		String when = LocalTime.now().toString();
		if(model instanceof ComputationGraph) {
			modelName = "Computation Graph";
		} 
		if(model instanceof MultiLayerNetwork) {
			modelName = "MultiLayer Network";
		}
		info = modelName + " loaded at " + when;
		
	}

	public static DLModel fromFile(File file) throws Exception {
		Model model = null;
		try {
			System.out.println("Trying to load file as computation graph: " + file);
			model = ModelSerializer.restoreComputationGraph(file);
			System.out.println("Loaded Computation Graph.");
		} catch (Exception e) {
			try {
				System.out.println("Failed to load computation graph. Trying to load model.");
				model = ModelSerializer.restoreMultiLayerNetwork(file);
				System.out.println("Loaded Multilayernetwork");
			} catch (Exception e1) {
				System.out.println("Give up trying to load file: " + file);
				throw e;
			}
		}
		return new DLModel(model);
	}

	public String[] getLayersName() {
		Layer[] layers = getLayers();
		return Stream.of(layers).map(l -> l.conf().getLayer().getLayerName()).toArray(String[]::new);
	}

	public String outputForImageFile(File file, int h, int w, int channels) throws IOException {
		NativeImageLoader loader = new NativeImageLoader(h, w, channels);
		INDArray img1 = loader.asMatrix(file);
		if (model instanceof ComputationGraph) {
			((ComputationGraph) model).output(img1);
		} else if (model instanceof MultiLayerNetwork) {
			((MultiLayerNetwork) model).output(img1);
		}
		activationsCache.clear();
		return null;
	}

	public void displayLayer(int layerIndex, GraphicsContext gc, int zoom, boolean showChannelGrid, boolean showActivationGrid) {
		System.out.println(layerIndex + " - zoom:" + zoom);
		activationsCache.putIfAbsent(layerIndex, getLayers()[layerIndex].activate());
		INDArray activation = activationsCache.get(layerIndex);
		System.out.println(activation.shapeInfoToString());
		int rank = activation.rank();
		int[] shape = activation.shape();
		int height = 1, width = 1, numberOfChannels = 1, pos = 0;
		ArrayList<double[][]> activations = new ArrayList<>();
		double max = 0, min = 0;
		if (rank == 4) {
			height = shape[3];
			width = shape[2];
			numberOfChannels = shape[1];
		}
		if (rank == 3) {
			height = shape[2];
			width = shape[1];
		}
		if (rank == 2) {
			height = shape[1];
		}
		for (int n = 0; n < numberOfChannels; n++) {
			double[][] layerActivation = new double[height][width];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					double value = activation.getDouble(pos);
					if (pos == 0) {
						max = value;
						min = value;
					}
					if (value > max) {
						max = value;
					}
					if (value < min) {
						min = value;
					}
					layerActivation[j][i] = value;
					pos++;
				}
			}
			activations.add(layerActivation);
		}
		gc.setFill(Color.LIGHTGOLDENRODYELLOW);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		double nSquare = zoom, pSize = 0.1, pXSize = 0.1, posX = 0, posY = 0;
		double squareSize = gc.getCanvas().getWidth() / nSquare;
		int spacing = 5;
		for (double[][] ds : activations) {
			pXSize = squareSize / ds.length;
			for (int i = 0; i < ds.length; i++) {
				pSize = squareSize / ds[i].length;
				for (int j = 0; j < ds[i].length; j++) {
					double value = ds[i][j];
					double pixelValue = AppUtils.map(value, min, max, 0.0, 1.0);
					gc.setFill(Color.gray(pixelValue));
					double x = posX + (i * pXSize);
					double y = posY + (j * pSize);
					gc.fillRect(x, y, pXSize, pSize);
					if(showActivationGrid) {
						gc.setLineWidth(0.5);
						gc.setStroke(Color.YELLOW);
						gc.strokeRect(x, y, pXSize, pSize);
					}
				}
			}
			if(showChannelGrid){
				gc.setLineWidth(2);
				gc.setStroke(Color.RED);
				gc.strokeRect(posX, posY, squareSize, squareSize);
			}
			posX += squareSize + spacing;
			if (posX > gc.getCanvas().getWidth()) {
				posX = 0;
				posY += squareSize + 5;
			}
		}
	}
	
	public String getInfo() {
		return this.info;
	}

	public String getLayerInfo(int layerIndex) {
		NeuralNetConfiguration layerConf = getLayerConf(layerIndex);
		return layerConf.toString();
	}
	
	private Layer[] getLayers() {
		if (model instanceof ComputationGraph) {
			return ((ComputationGraph) model).getLayers();
		} else if (model instanceof MultiLayerNetwork) {
			return ((MultiLayerNetwork) model).getLayers();
		}
		// should never get here
		return null;
	}
	
	private NeuralNetConfiguration getLayerConf(int layerIndex) {
		return getLayers()[layerIndex].conf();
	}

}
