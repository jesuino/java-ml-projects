import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.fxapps.predict.InstanceSegmentationMLModel;
import org.fxapps.predict.MLModel;
import org.fxapps.predict.ObjectDetectionMLModel;
import org.fxapps.predict.PoseEstimationMLModel;

/**
 * Captures WebCam and apply a ML model.
 * 
 * @author Rakesh Bhatt (rakeshbhatt10)
 * @author wsiqueir (github.com/jesuino)
 */
public class WebCamAppLauncher extends Application {

    private static final int IN_WIDTH = 1200;
    private static final int IN_HEIGHT = 800;
    private static final double APP_WIDTH = 1500;
    private static final double APP_HEIGHT = 1200;

    record WebCamInfo(String webCamName, int webCamIndex) {

        @Override
        public String toString() {
            return webCamName;
        }
    }

    private FlowPane bottomCameraControlPane;
    private HBox topPane;
    private BorderPane root;
    private String cameraListPromptText = "Choose Camera";
    private ImageView imgWebCamCapturedImage;
    private Webcam webCam = null;
    private boolean stopCamera = false;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private BorderPane webCamPane;
    private Button btnCamreaStop;
    private Button btnCamreaStart;
    private Button btnCameraDispose;

    ObjectProperty<MLModel<?>> selectedModel = new SimpleObjectProperty<>();

    List<MLModel<?>> models = List.of(new ObjectDetectionMLModel(),
                                      new InstanceSegmentationMLModel(),
                                      new PoseEstimationMLModel());

    AtomicBoolean runningPrediction;

    @Override
    public void start(Stage primaryStage) {
        runningPrediction = new AtomicBoolean();
        primaryStage.setTitle("Connecting Camera Device Using Webcam Capture API");

        root = new BorderPane();

        topPane = new HBox(20);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPrefHeight(40);
        root.setTop(topPane);

        webCamPane = new BorderPane();
        webCamPane.setStyle("-fx-background-color: #ccc;");
        imgWebCamCapturedImage = new ImageView();
        webCamPane.setCenter(imgWebCamCapturedImage);
        root.setCenter(webCamPane);
        createTopPanel();

        bottomCameraControlPane = new FlowPane();
        bottomCameraControlPane.setOrientation(Orientation.HORIZONTAL);
        bottomCameraControlPane.setAlignment(Pos.CENTER);
        bottomCameraControlPane.setHgap(20);
        bottomCameraControlPane.setVgap(10);
        bottomCameraControlPane.setPrefHeight(40);
        bottomCameraControlPane.setDisable(true);
        createCameraControls();
        root.setBottom(bottomCameraControlPane);

        primaryStage.setScene(new Scene(root));
        primaryStage.setHeight(APP_HEIGHT);
        primaryStage.setWidth(APP_WIDTH);
        primaryStage.centerOnScreen();
        primaryStage.show();

        Platform.runLater(this::setImageViewSize);

    }

    protected void setImageViewSize() {
        var height = webCamPane.getHeight();
        var width = webCamPane.getWidth();

        imgWebCamCapturedImage.setFitWidth(IN_WIDTH);
        imgWebCamCapturedImage.setFitHeight(IN_HEIGHT);
        imgWebCamCapturedImage.prefHeight(height);
        imgWebCamCapturedImage.prefWidth(width);
        imgWebCamCapturedImage.setPreserveRatio(true);

    }

    private void createTopPanel() {
        int webCamCounter = 0;

        var cameraOptions = new ComboBox<WebCamInfo>();
        var modelOptions = new ComboBox<MLModel<?>>();

        for (var webcam : Webcam.getWebcams()) {
            var webCamInfo = new WebCamInfo(webcam.getName(), webCamCounter);
            cameraOptions.getItems().add(webCamInfo);
            webCamCounter++;
        }

        cameraOptions.setPromptText(cameraListPromptText);
        cameraOptions.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            if (arg2 != null) {
                initializeWebCam(arg2.webCamIndex());
            }
        });

        selectedModel.bind(modelOptions.getSelectionModel().selectedItemProperty());
        modelOptions.getItems().addAll(models);
        modelOptions.getSelectionModel().select(0);

        topPane.getChildren().addAll(new Label("Select WebCam"), cameraOptions,
                                     new Label("Select Model"), modelOptions);
    }

    protected void initializeWebCam(final int webCamIndex) {
        var webCamTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                if (webCam != null) {
                    disposeWebCamCamera();
                }

                webCam = Webcam.getWebcams().get(webCamIndex);
                webCam.open();

                startWebCamStream();

                return null;
            }
        };

        var webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();

        bottomCameraControlPane.setDisable(false);
        btnCamreaStart.setDisable(true);
    }

    protected void startWebCamStream() {
        stopCamera = false;
        var task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                final var ref = new AtomicReference<WritableImage>();
                BufferedImage img = null;
                while (!stopCamera) {
                    try {
                        if ((img = webCam.getImage()) != null) {

                            if (!runningPrediction.get()) {
                                runningPrediction.set(true);
                                img = selectedModel.get().predictAndDraw(img);
                                runningPrediction.set(false);
                            }

                            img.flush();
                            ref.set(SwingFXUtils.toFXImage(img, ref.get()));
                            Platform.runLater(() -> imageProperty.set(ref.get()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };

        var th = new Thread(task);
        th.setDaemon(true);
        th.start();
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);
    }

    private void createCameraControls() {
        btnCamreaStop = new Button();
        btnCamreaStop.setOnAction(e -> stopWebCamCamera());
        btnCamreaStop.setText("Stop Camera");

        btnCamreaStart = new Button();
        btnCamreaStart.setOnAction(e -> startWebCamCamera());
        btnCamreaStart.setText("Start Camera");

        btnCameraDispose = new Button();
        btnCameraDispose.setText("Dispose Camera");
        btnCameraDispose.setOnAction(e -> disposeWebCamCamera());

        bottomCameraControlPane.getChildren().addAll(btnCamreaStart, btnCamreaStop, btnCameraDispose);
    }

    protected void disposeWebCamCamera() {
        stopCamera = true;
        webCam.close();
        btnCamreaStart.setDisable(true);
        btnCamreaStop.setDisable(true);
    }

    protected void startWebCamCamera() {
        stopCamera = false;
        startWebCamStream();
        btnCamreaStop.setDisable(false);
        btnCamreaStart.setDisable(true);
    }

    protected void stopWebCamCamera() {
        stopCamera = true;
        btnCamreaStart.setDisable(false);
        btnCamreaStop.setDisable(true);
    }

    protected void runPrediction(BufferedImage img) {
        if (!runningPrediction.get()) {
            runningPrediction.set(true);
            var task = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    System.out.println(selectedModel.get().predict(img));
                    img.flush();
                    runningPrediction.set(false);
                    return null;
                }
            };

            var th = new Thread(task);
            th.setDaemon(true);
            th.start();
        } else {
            img.flush();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}