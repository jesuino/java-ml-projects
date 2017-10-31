package org.fxapps.deeplearning;

import java.io.IOException;
import java.nio.file.Paths;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

public class TrainedModelApp {

	static String MODEL_FILE = "/home/wsiqueir/workspace-jbds9/brazilian-coin-classification/brazilian-coin-model.zip";
	
	public static void main(String[] args) throws IOException {
		String[] labels = { "cinco_centavos", "cinquenta_centavos", "dez_centavos", "um_real", "vinte_cinco_centavos"} ;

		MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(Paths.get(MODEL_FILE).toFile());
		model.init();
		NativeImageLoader loader1 = new NativeImageLoader(128, 128, 3);
		NativeImageLoader loader2 = new NativeImageLoader(128, 128, 3);
		INDArray img1 = loader1.asMatrix(Paths.get("/home/wsiqueir/Downloads/moeda_test.jpg").toFile());
		INDArray img2 = loader2.asMatrix(Paths.get("/home/wsiqueir/Downloads/moeda_test2.jpg").toFile());
		INDArray output1 = model.output(img1);
		System.out.println("Output for IMG1:");
		for (int i = 0; i < labels.length; i++) {
			float double1 = Math.abs(output1.getFloat(i));
			if(double1 > 0.0)
			System.out.println(labels[i] + ": " + double1 + "%");
		}
		System.out.println("Output for IMG2:");
		INDArray output2 = model.output(img2);
		for (int i = 0; i < labels.length; i++) {
			float double1 = Math.abs(output2.getFloat(i));
			if(double1 > 0.0)
			System.out.println(labels[i] + ": " + double1 + "%");
		}
		int[] predict = model.predict(img1);
		for (int i = 0; i < predict.length; i++) {
			System.out.println(predict[i]);
		}
		predict = model.predict(img2);
		for (int i = 0; i < predict.length; i++) {
			System.out.println(predict[i]);
		}
		System.out.println(predict);
	}

}
