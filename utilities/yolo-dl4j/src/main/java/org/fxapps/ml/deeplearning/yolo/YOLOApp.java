package org.fxapps.ml.deeplearning.yolo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.compress.utils.IOUtils;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class YOLOApp extends Application {

	// TODO: buttons to select intersection or union

	private static final float DEFAULT_THRESHOLD = 0.45f;

	Map<String, Paint> colors = new HashMap<>();

	private GraphicsContext ctx;
	private Scene scene;
	private Slider sldThreshold;
	private URL DEFAULT_IMG_URL = YOLOApp.class.getResource("/images/defaultImage.jpg");
	private YOLOModel yoloModel;
	private File currentImage;
	private Image currentImageFXImage;

	public static void main(String[] args) throws IOException {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		yoloModel = new YOLOModel();
		BorderPane root = buildRoot();
		scene = new Scene(root, 1200, 800);
		stage.setScene(scene);
		stage.setTitle("Testing YOLO in a JavaFX application");
		stage.show();
		AppUtils.doBlockingAsyncWork(scene, () -> {
			yoloModel.init();
			ctx.getCanvas().setWidth(yoloModel.getInputWidth());
			ctx.getCanvas().setHeight(yoloModel.getInputHeight());
			for (int i = 0; i < yoloModel.getModelClasses().length; i++) {
				colors.put(yoloModel.getModelClasses()[i], Color.hsb((i + 1) * 20, 0.5, 1.0));
			}
		}, () -> checkAndLoadImage(DEFAULT_IMG_URL));
	}

	private BorderPane buildRoot() {
		BorderPane root = new BorderPane();
		Parent spCanvas = buildCenterPane();
		HBox hbBottom = buildBottomPane();
		root.setCenter(spCanvas);
		root.setBottom(hbBottom);
		return root;
	}

	private Parent buildCenterPane() {
		Canvas canvas = new Canvas(yoloModel.getInputWidth(), yoloModel.getInputHeight());
		Parent spCanvas = ZoomPane.createZoomPane(new Group(canvas));
		ctx = canvas.getGraphicsContext2D();
		ctx.setTextAlign(TextAlignment.CENTER);
		ctx.fillText("Loading the model. This may take a while...", canvas.getWidth() / 2, canvas.getHeight() / 2);
		return spCanvas;
	}

	private HBox buildBottomPane() {
		sldThreshold = new Slider(0.1f, 1.0f, DEFAULT_THRESHOLD);
		sldThreshold.setShowTickLabels(true);
		sldThreshold.setMajorTickUnit(0.1);
		sldThreshold.setBlockIncrement(0.01);
		sldThreshold.setShowTickMarks(true);
		sldThreshold.valueProperty().addListener(v -> update());
		MenuButton mbLoadImage = new MenuButton("Load Image");
		MenuItem mnLocalImage = new MenuItem("From Computer");
		mbLoadImage.getItems().add(mnLocalImage);
		MenuItem mnUrl = new MenuItem("Enter URL");
		mnUrl.setOnAction(e -> {
			Optional<String> imgUrl = AppUtils.askInputFromUser("Enter an URL", "Enter an image URL:");
			AppUtils.doBlockingAsyncWork(scene, () -> {
				imgUrl.ifPresent(this::checkAndLoadImage);
			});

		});
		mnLocalImage.setOnAction(e -> {
			FileChooser fc = new FileChooser();
			fc.getExtensionFilters().add(new ExtensionFilter("PNG Files", "*.png"));
			fc.getExtensionFilters().add(new ExtensionFilter("JPG Files", "*.jpg"));
			File selectedFile = fc.showOpenDialog(sldThreshold.getScene().getWindow());
			if (selectedFile != null) {
				checkAndLoadImage(selectedFile);
			}
		});
		mbLoadImage.getItems().add(mnUrl);
		HBox hbBottom = new HBox(10, new Label("Threshold: "), sldThreshold, mbLoadImage);
		hbBottom.setAlignment(Pos.CENTER);
		return hbBottom;
	}

	private void checkAndLoadImage(String urlStr) {
		try {
			URL url = new URL(urlStr);
			checkAndLoadImage(url);
		} catch (MalformedURLException e) {
			AppUtils.showExceptionDialog("URL is not valid: " + urlStr, e);
		}
	}

	private void checkAndLoadImage(URL url) {
		try {
			File imgFile = File.createTempFile("yolo", "");
			IOUtils.copy(url.openStream(), new FileOutputStream(imgFile));
			checkAndLoadImage(imgFile);
		} catch (IOException e) {
			AppUtils.showExceptionDialog("Error loading image", e);
		}
	}

	private void checkAndLoadImage(File imgFile) {
		try {
			currentImage = imgFile;
			currentImageFXImage = new Image(new FileInputStream(currentImage), yoloModel.getInputWidth(),
					yoloModel.getInputHeight(), false, false);
			update();
		} catch (IOException e) {
			AppUtils.showExceptionDialog("Error loading image: ", e);
		}
	}

	private void update() {
		AppUtils.doBlockingAsyncWork(scene, () -> {
			ctx.clearRect(0, 0, yoloModel.getInputWidth(), yoloModel.getInputHeight());
			ctx.drawImage(currentImageFXImage, 0, 0);
			List<DetectedObject> detectedObjects = yoloModel.run(currentImage, sldThreshold.getValue());
			drawBoxes(detectedObjects);
		});
	}

	private void drawBoxes(List<DetectedObject> predictedObjects) {
		ctx.setLineWidth(3);
		int w = yoloModel.getInputWidth();
		int h = yoloModel.getInputHeight();
		int gridW = yoloModel.getGridW();
		int gridH = yoloModel.getGridH();
		ctx.setTextAlign(TextAlignment.CENTER);
		for (DetectedObject obj : predictedObjects) {
			String cl = yoloModel.getModelClasses()[obj.getPredictedClass()];
			double[] xy1 = obj.getTopLeftXY();
			double[] xy2 = obj.getBottomRightXY();
			int x1 = (int) Math.round(w * xy1[0] / gridW);
			int y1 = (int) Math.round(h * xy1[1] / gridH);
			int x2 = (int) Math.round(w * xy2[0] / gridW);
			int y2 = (int) Math.round(h * xy2[1] / gridH);
			int rectW = x2 - x1;
			int rectH = y2 - y1;
			ctx.setStroke(colors.get(cl));
			ctx.strokeRect(x1, y1, rectW, rectH);
			ctx.strokeText(cl, x1 + (rectW / 2), y1 - 2);
			ctx.setFill(Color.WHITE);
			ctx.fillText(cl, x1 + (rectW / 2), y1 - 2);
		}
	}
}