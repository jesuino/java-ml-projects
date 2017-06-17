package org.fxapps.ml;

import java.io.IOException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FirstTests extends Application {

	public static void main(String[] args) throws IOException {
		launch();
		// CSVLoader loader = new CSVLoader();
		// loader.setFile(new File(""));

	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Scatter Chart Sample");
		final NumberAxis xAxis = new NumberAxis(0, 10, 1);
		final NumberAxis yAxis = new NumberAxis(-100, 500, 100);
		final ScatterChart<Number, Number> sc = new ScatterChart<Number, Number>(xAxis, yAxis);
		xAxis.setLabel("Age (years)");
		yAxis.setLabel("Returns to date");
		sc.setTitle("Investment Overview");

		XYChart.Series<Number, Number> series1 = new XYChart.Series<>();

		series1.setName("Option 1");
		series1.getData().add(new XYChart.Data<>(4.2, 193.2));
		series1.getData().add(new XYChart.Data<>(2.8, 33.6));
		series1.getData().add(new XYChart.Data<>(6.2, 24.8));
		series1.getData().add(new XYChart.Data<>(1, 14));
		series1.getData().add(new XYChart.Data<>(1.2, 26.4));
		series1.getData().add(new XYChart.Data<>(4.4, 114.4));
		series1.getData().add(new XYChart.Data<>(8.5, 323));
		series1.getData().add(new XYChart.Data<>(6.9, 289.8));
		series1.getData().add(new XYChart.Data<>(9.9, 287.1));
		series1.getData().add(new XYChart.Data<>(0.9, -9));
		series1.getData().add(new XYChart.Data<>(3.2, 150.8));
		series1.getData().add(new XYChart.Data<>(4.8, 20.8));
		series1.getData().add(new XYChart.Data<>(7.3, -42.3));
		series1.getData().add(new XYChart.Data<>(1.8, 81.4));
		series1.getData().add(new XYChart.Data<>(7.3, 110.3));
		series1.getData().add(new XYChart.Data<>(2.7, 41.2));

		sc.setPrefSize(500, 400);
		sc.getData().add(series1);
		Scene scene = new Scene(new Group());
		final VBox vbox = new VBox();
		final HBox hbox = new HBox();

		final Button add = new Button("Add Series");
		final Button remove = new Button("Remove Series");

		hbox.setSpacing(10);
		hbox.getChildren().addAll(add, remove);

		vbox.getChildren().addAll(sc, hbox);
		hbox.setPadding(new Insets(10, 10, 10, 50));

		((Group) scene.getRoot()).getChildren().add(vbox);
		stage.setScene(scene);
		stage.show();

		add.setOnAction(e -> {
			if (sc.getData() == null)
				sc.setData(FXCollections.<XYChart.Series<Number, Number>> observableArrayList());
			ScatterChart.Series<Number, Number> series = new ScatterChart.Series<Number, Number>();
			series.setName("Option " + (sc.getData().size() + 1));
			for (int i = 0; i < 100; i++)
				series.getData().add(new ScatterChart.Data<Number, Number>(Math.random() * 100, Math.random() * 500));
			sc.getData().add(series);
		});
		remove.setOnAction(e -> {
			if (!sc.getData().isEmpty())
				sc.getData().remove((int) (Math.random() * (sc.getData().size() - 1)));
		});
	}
}
