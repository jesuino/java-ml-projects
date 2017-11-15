package org.fxapps.deeplearning;

import java.io.File;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class App extends Application {

	private static final int APP_WIDHT = 1200;
	private static final int APP_HEIGHT = 800;

	private static final int CANVAS_WIDTH = 1000;
	private static final int CANVAS_HEIGHT = 2200;

	// the zoom is actually the number of squares
	private static final int MAX_ZOOM = 1;
	private static final int MIN_ZOOM = 60;

	private ObjectProperty<DLModel> selectedModel;
	private BooleanProperty outputReady;
	private BooleanProperty showActivationGrid;
	private BooleanProperty showChannelGrid;
	private IntegerProperty zoomValue;
	private GraphicsContext gc;
	private MultipleSelectionModel<String> layerSelection;
	private Scene scene;
	private ChangeListener<? super Boolean> showLayerListener;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = initApp();
		scene = new Scene(root, APP_WIDHT, APP_HEIGHT);
		stage.setScene(scene);
		stage.show();
	}

	private Parent initApp() {
		initVariables();
		return buildUI();
	}

	private void initVariables() {
		selectedModel = new SimpleObjectProperty<>();
		outputReady = new SimpleBooleanProperty();
		zoomValue = new SimpleIntegerProperty(5);
		showActivationGrid = new SimpleBooleanProperty();
		showChannelGrid = new SimpleBooleanProperty();
		showLayerListener = (b, o, n) -> showLayer();
		showActivationGrid.addListener(showLayerListener);
		showChannelGrid.addListener(showLayerListener);
	}

	private Parent buildUI() {
		BorderPane root = new BorderPane();
		Insets margin = new Insets(10);
		Node leftPane = buildLeftPane();
		Node bottomPane = buildBottomPane();
		Node centerPane = buildCenterPane();
		root.setLeft(leftPane);
		root.setBottom(bottomPane);
		root.setCenter(centerPane);
		BorderPane.setMargin(bottomPane, margin);
		BorderPane.setMargin(centerPane, margin);
		return root;
	}

	private Node buildCenterPane() {
		Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		ScrollPane spCanvas = new ScrollPane(canvas);
		gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.LIGHTGOLDENRODYELLOW);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		return spCanvas;
	}

	private Node buildBottomPane() {
		Button btnSelectModel = new Button("Select Model");
		Button btnSelectImage = new Button("Select Image");
		TextField txtImgInfo = new TextField("224,224,3");
		Node zoomBox = buildZoomBox();
		CheckBox cbShowChannelGrid = new CheckBox("Channels Grid");
		CheckBox cbShowActivationGrid = new CheckBox("Activation Grid");
		Label lblInfo = new Label();
		Button btnShowInfo = new Button("Layer Information");
		zoomBox.disableProperty().bind(outputReady.not());
		cbShowChannelGrid.selectedProperty().bindBidirectional(showChannelGrid);
		cbShowActivationGrid.selectedProperty().bindBidirectional(showActivationGrid);
		lblInfo.setFont(Font.font(Font.getDefault().getFamily(), FontPosture.ITALIC, 10));

		txtImgInfo.setPromptText("heigth,width,channels");
		FileChooser fcModel = new FileChooser();
		FileChooser fcImage = new FileChooser();
		fcModel.getExtensionFilters().add(new ExtensionFilter("ZIP files", "*.zip"));
		fcImage.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.jpg", "*.png"));
		btnSelectModel.setOnAction(e -> {
			Scene scene = btnSelectModel.getScene();
			File file = fcModel.showOpenDialog(scene.getWindow());
			if (file != null) {
				AppUtils.doBlockingAsyncWork(scene, () -> {
					outputReady.set(false);
					try {
						return DLModel.fromFile(file);
					} catch (Exception e1) {
						throw new Error(e1);
					}
				}, r -> {
					selectedModel.set(r);
					lblInfo.setText(r.getInfo());
				}, exp -> {
					exp.printStackTrace();
					AppUtils.showErrorDialog("Not able to open model: " + file.getAbsolutePath());
				});

			}
		});
		btnSelectImage.setOnAction(e -> {
			Scene scene = btnSelectModel.getScene();
			File file = fcImage.showOpenDialog(scene.getWindow());
			try {
				if (file != null) {
					try {
						String imgInfo = txtImgInfo.getText();
						String[] info = imgInfo.split("\\,");
						int h = Integer.parseInt(info[0]);
						int w = Integer.parseInt(info[1]);
						int c = Integer.parseInt(info[2]);
						AppUtils.doBlockingAsyncWork(scene, () -> {
							try {
								return selectedModel.get().outputForImageFile(file, h, w, c);
							} catch (Exception e1) {
								throw new Error(e1);
							}
						}, r -> {
							outputReady.set(true);
						}, exp -> {
							exp.printStackTrace();
							AppUtils.showErrorDialog("Not able to load image: " + file.getAbsolutePath());
						});

					} catch (Exception e1) {
						AppUtils.showErrorDialog("Error loading image...",
								"Please provide valid values for the input image: " + e1.getMessage());
						return;
					}
				}
			} catch (Exception e1) {
				AppUtils.showErrorDialog("Not able to open model: " + file.getAbsolutePath());
			}
		});
		btnShowInfo.setOnAction(e -> {
			int selectedLayer = layerSelection.getSelectedIndex();
			AppUtils.showSuccessDialog(selectedModel.get().getLayerInfo(selectedLayer));
		});
		btnSelectImage.disableProperty().bind(selectedModel.isNull());
		btnShowInfo.disableProperty().bind(layerSelection.selectedItemProperty().isNull());
		HBox hbInput = new HBox(10, btnSelectModel, txtImgInfo, btnSelectImage, new Separator(Orientation.VERTICAL),
				zoomBox, cbShowActivationGrid, cbShowChannelGrid, btnShowInfo, lblInfo);
		hbInput.setAlignment(Pos.CENTER_LEFT);
		hbInput.setTranslateX(10);
		return hbInput;
	}

	private Node buildLeftPane() {
		ListView<String> lstLayers = new ListView<>();
		lstLayers.disableProperty().bind(outputReady.not());
		selectedModel.addListener((b, o, n) -> lstLayers.getItems().setAll(n.getLayersName()));
		layerSelection = lstLayers.getSelectionModel();
		layerSelection.selectedIndexProperty().addListener((b, o, n) -> showLayer());
		return lstLayers;
	}

	private Node buildZoomBox() {
		Button btnIncreaseZoom = new Button("+");
		Button btnDecreaseZoom = new Button("-");
		btnIncreaseZoom.disableProperty().bind(zoomValue.isEqualTo(MAX_ZOOM));
		btnDecreaseZoom.disableProperty().bind(zoomValue.isEqualTo(MIN_ZOOM));
		btnIncreaseZoom.setOnAction(e -> {
			if (zoomValue.get() > MAX_ZOOM) {
				zoomValue.set(zoomValue.get() - 1);
			}
			showLayer();
		});
		btnDecreaseZoom.setOnAction(e -> {
			if (zoomValue.get() < MIN_ZOOM) {
				zoomValue.set(zoomValue.get() + 1);
			}
			showLayer();
		});
		HBox hbZoomButtons = new HBox(2, btnDecreaseZoom, btnIncreaseZoom);
		hbZoomButtons.setAlignment(Pos.CENTER);
		return hbZoomButtons;
	}

	private void showLayer() {
		int selectedIndex = layerSelection.getSelectedIndex();
		if (selectedIndex == -1) {
			return;
		}
		selectedModel.get().displayLayer(selectedIndex, gc, zoomValue.get(), showChannelGrid.get(),
				showActivationGrid.get());
	}

}
