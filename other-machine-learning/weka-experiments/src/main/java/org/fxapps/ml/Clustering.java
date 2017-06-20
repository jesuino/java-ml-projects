package org.fxapps.ml;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import weka.classifiers.trees.J48;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class Clustering extends Application {

	private static final int NUMBER_OF_CLASSES = 3;

	private static final String DATA_SET = "/iris.2D.arff";

	private ScatterChart<Number, Number> clusteredChart;
	private ScatterChart<Number, Number> realDataChart;
	private ScatterChart<Number, Number> noClassificationChart;

	private static int swapIndex = 0;
	private int[][] swapColorsCombinations = { { 0, 1 }, { 0, 2 }, { 1, 2 } };

	private J48 tree;
	private Instances data;

	public static void main(String[] args) throws Exception {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		loadData();
		tree = new J48();
		tree.buildClassifier(data);

		noClassificationChart = buildChart("No Classification (click to add new data)", buildSingleSeries());
		clusteredChart = buildChart("Clustered", buildClusteredSeries());
		realDataChart = buildChart("Real Data (+ Decision Tree classification for new data)", buildLabeledSeries());

		noClassificationChart.setOnMouseClicked(e -> {
			Axis<Number> xAxis = noClassificationChart.getXAxis();
			Axis<Number> yAxis = noClassificationChart.getYAxis();
			Point2D mouseSceneCoords = new Point2D(e.getSceneX(), e.getSceneY());
			double x = xAxis.sceneToLocal(mouseSceneCoords).getX();
			double y = yAxis.sceneToLocal(mouseSceneCoords).getY();
			Number xValue = xAxis.getValueForDisplay(x);
			Number yValue = yAxis.getValueForDisplay(y);
			reloadSeries(xValue, yValue);
		});

		Label lblDecisionTreeTitle = new Label("Decision Tree generated for the Iris dataset:");
		Text txtTree = new Text(tree.toString());
		String graph = tree.graph();
		SwingNode sw = new SwingNode();
		SwingUtilities.invokeLater(() -> {
			TreeVisualizer treeVisualizer = new TreeVisualizer(null, graph, new PlaceNode2());
			treeVisualizer.setPreferredSize(new Dimension(600, 500));
			sw.setContent(treeVisualizer);
		});

		Button btnRestore = new Button("Restore original data");
		Button btnSwapColors = new Button("Swap clustered chart colors");
		StackPane spTree = new StackPane(sw);
		spTree.setPrefWidth(300);
		spTree.setPrefHeight(350);
		VBox vbDecisionTree = new VBox(5, lblDecisionTreeTitle, new Separator(), spTree,
				new HBox(10, btnRestore, btnSwapColors));
		btnRestore.setOnAction(e -> {
			loadData();
			reloadSeries();
		});
		btnSwapColors.setOnAction(e -> swapClusteredChartSeriesColors());
		lblDecisionTreeTitle.setTextFill(Color.DARKRED);
		lblDecisionTreeTitle.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, FontPosture.ITALIC, 16));
		txtTree.setTranslateX(100);
		txtTree.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, FontPosture.ITALIC, 14));
		txtTree.setLineSpacing(1);
		txtTree.setTextAlignment(TextAlignment.LEFT);
		vbDecisionTree.setTranslateY(20);
		vbDecisionTree.setTranslateX(20);

		GridPane gpRoot = new GridPane();
		gpRoot.add(realDataChart, 0, 0);
		gpRoot.add(clusteredChart, 1, 0);
		gpRoot.add(noClassificationChart, 0, 1);
		gpRoot.add(vbDecisionTree, 1, 1);

		stage.setScene(new Scene(gpRoot));
		stage.setTitle("Íris dataset clustering and visualization");
		stage.show();
	}

	private void loadData() {
		BufferedReader datafile;
		try {
			InputStream dataSetIs = getClass().getResource(DATA_SET).openStream();
			datafile = new BufferedReader(new InputStreamReader(dataSetIs));
			data = new Instances(datafile);
			data.setClassIndex(data.numAttributes() - 1);
		} catch (Exception e) {
			System.out.println("Exception loading data... Leaving");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void reloadSeries(Number xValue, Number yValue) {
		try {
			Instance instance = new DenseInstance(NUMBER_OF_CLASSES);
			instance.setDataset(data);
			instance.setValue(0, xValue.doubleValue());
			instance.setValue(1, yValue.doubleValue());
			double predictedClass = tree.classifyInstance(instance);
			instance.setValue(2, predictedClass);
			data.add(instance);
			reloadSeries();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reloadSeries() {
		try {
			noClassificationChart.getData().clear();
			clusteredChart.getData().clear();
			realDataChart.getData().clear();
			noClassificationChart.getData().addAll(buildSingleSeries());
			clusteredChart.getData().addAll(buildClusteredSeries());
			realDataChart.getData().addAll(buildLabeledSeries());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void swapClusteredChartSeriesColors() {
		List<Series<Number, Number>> clusteredSeries = new ArrayList<>();
		// we have to copy the original data to swap the series
		clusteredChart.getData().forEach(serie -> {
			Series<Number, Number> series = new Series<>();
			series.setName(serie.getName());
			serie.getData().stream().map(d -> new Data<Number, Number>(d.getXValue(), d.getYValue()))
					.forEach(series.getData()::add);
			clusteredSeries.add(series);
		});
		int i = swapColorsCombinations[swapIndex][0];
		int j = swapColorsCombinations[swapIndex][1];
		Collections.swap(clusteredSeries, i, j);
		clusteredChart.getData().clear();
		clusteredChart.getData().addAll(clusteredSeries);
		swapIndex = swapIndex == NUMBER_OF_CLASSES - 1 ? 0 : swapIndex + 1;
	}

	private List<XYChart.Series<Number, Number>> buildSingleSeries() {
		XYChart.Series<Number, Number> singleSeries = new XYChart.Series<>();
		data.stream().map(this::instancetoChartData).forEach(singleSeries.getData()::add);
		singleSeries.setName("no classification");
		return Arrays.asList(singleSeries);
	}

	private List<Series<Number, Number>> buildLabeledSeries() {
		List<XYChart.Series<Number, Number>> realSeries = new ArrayList<>();
		Attribute irisClasses = data.attribute(2);
		data.stream().collect(Collectors.groupingBy(d -> {
			int i = (int) d.value(2);
			return irisClasses.value(i);
		})).forEach((e, instances) -> {
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			series.setName(e);
			instances.stream().map(this::instancetoChartData).forEach(series.getData()::add);
			realSeries.add(series);
		});
		return realSeries;
	}

	private List<Series<Number, Number>> buildClusteredSeries() throws Exception {
		List<XYChart.Series<Number, Number>> clusteredSeries = new ArrayList<>();

		// to build the cluster we remove the class information
		Remove remove = new Remove();
		remove.setAttributeIndices("3");
		remove.setInputFormat(data);
		Instances dataToBeClustered = Filter.useFilter(data, remove);

		SimpleKMeans kmeans = new SimpleKMeans();
		kmeans.setSeed(10);
		kmeans.setPreserveInstancesOrder(true);
		kmeans.setNumClusters(3);
		kmeans.buildClusterer(dataToBeClustered);

		IntStream.range(0, 3).mapToObj(i -> {
			Series<Number, Number> newSeries = new XYChart.Series<>();
			newSeries.setName(String.valueOf(i));
			return newSeries;
		}).forEach(clusteredSeries::add);

		int[] assignments = kmeans.getAssignments();
		for (int i = 0; i < assignments.length; i++) {
			int clusterNum = assignments[i];
			clusteredSeries.get(clusterNum).getData().add(instancetoChartData(data.get(i)));
		}

		return clusteredSeries;
	}

	private XYChart.Data<Number, Number> instancetoChartData(Instance i) {
		return new XYChart.Data<Number, Number>(i.value(0), i.value(1));
	}

	private ScatterChart<Number, Number> buildChart(String chartName, List<XYChart.Series<Number, Number>> series) {
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		final ScatterChart<Number, Number> sc = new ScatterChart<Number, Number>(xAxis, yAxis);
		sc.setTitle(chartName);
		sc.setPrefHeight(450);
		sc.setPrefWidth(600);
		xAxis.getValueForDisplay(1);
		yAxis.getValueForDisplay(2);
		sc.getData().addAll(series);
		return sc;
	}

}