package org.fxapps.tensorflow;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class App extends Application {
	
	final String PATH_TO_DATA_DIR = "/opt/tensorflow/labeled-data/";
	
	LabelImage labelImage;
	
	private ImageView loadedImage;
	private ListView<String> lstLabels;
	private FileChooser fc;
	private Stage stage;

	public static void main(String[] args) {
		launch();	
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		List<String> labels = Files.lines(Paths.get(PATH_TO_DATA_DIR, "imagenet_comp_graph_label_strings.txt")).collect(Collectors.toList());
		byte[] graphDef = Files.readAllBytes(Paths.get(PATH_TO_DATA_DIR, "tensorflow_inception_graph.pb"));
		labelImage = new LabelImage(graphDef, labels);
		Parent root = buildUI();
		this.stage.setScene(new Scene(root, 800, 600));
		stage.setTitle("TensorFlow Hello World from a JavaFX Application");
		stage.show();
	}

	private Parent buildUI() {
		fc = new FileChooser();
		fc.getExtensionFilters().clear();
		ExtensionFilter jpgFilter = new ExtensionFilter("JPG, JPEG images", "*.jpg", "*.jpeg", "*.JPG", ".JPEG");
		fc.getExtensionFilters().add(jpgFilter);
		fc.setSelectedExtensionFilter(jpgFilter);
		fc.setTitle("Select a JPG image");
		lstLabels = new ListView<>();
		lstLabels.setPrefHeight(200);
		Button btnLoad = new Button("Select an Image");
		btnLoad.setOnAction(e -> validateUrlAndLoadImg());

		HBox hbBottom = new HBox(10, btnLoad);
		hbBottom.setAlignment(Pos.CENTER);

		loadedImage = new ImageView();
		loadedImage.setFitWidth(300);
		loadedImage.setFitHeight(250);
		
		Label lblTitle = new Label("Label image using TensorFlow");
		lblTitle.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 40));
		VBox root = new VBox(10,lblTitle, loadedImage, new Label("Results:"), lstLabels, hbBottom);
		root.setAlignment(Pos.TOP_CENTER);
		return root;
	}

	private void validateUrlAndLoadImg() {
		File selectedFile = fc.showOpenDialog(stage);
		if(selectedFile == null) { 
			return;
		}
		URI imgPath = selectedFile.toURI();
		try {
			Image img = new Image(imgPath.toString(), false);
			if(img.isError()) {
				showError("Can't load image", "Not able to load image. Check if the image exists");
				return;
			}
			byte[] b =  Files.readAllBytes(Paths.get(imgPath));
			
			Map<String, Float> imageLabels = labelImage.labelImage(b);
			lstLabels.getItems().clear();
			imageLabels.forEach((k, v) ->  lstLabels.getItems().add(k + " (" + v  + "%)"));
			loadedImage.setImage(img);
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			showError("URL not valid", "The URL you entered is not valid or points to an invalid image (it must be JPG)");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showError(String title, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	

}
