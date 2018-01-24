package org.fxapps.ml.deeplearning.yolo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

// got this from Java forums: https://t.co/3ELLONUvcK
public class ZoomPane {

	final static double SCALE_DELTA = 1.1;

	final static double MIN_SCALE = 0.5;

	final static double MAX_SCALE = 5;

	public static Parent createZoomPane(final Group group) {
		final StackPane zoomPane = new StackPane();

		zoomPane.getChildren().add(group);

		final ScrollPane scroller = new ScrollPane();
		final Group scrollContent = new Group(zoomPane);
		scroller.setContent(scrollContent);

		scroller.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
				zoomPane.setMinSize(newValue.getWidth(), newValue.getHeight());
			}
		});

		scroller.setPrefViewportWidth(256);
		scroller.setPrefViewportHeight(256);

		zoomPane.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				event.consume();

				if (event.getDeltaY() == 0) {
					return;
				}

				double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
				// amount of scrolling in each direction in scrollContent coordinate
				// units
				Point2D scrollOffset = figureScrollOffset(scrollContent, scroller);
				double scaleX = group.getScaleX() * scaleFactor;
				double scaleY = group.getScaleY() * scaleFactor;
				
				if (scaleX > MAX_SCALE || scaleY > MAX_SCALE) {
					scaleX = MAX_SCALE;
					scaleY = MAX_SCALE;
					return;
				}

				if (scaleX < MIN_SCALE || scaleY < MIN_SCALE) {
					scaleX = MIN_SCALE;
					scaleY = MIN_SCALE;
					return;
				}

				group.setScaleX(scaleX);
				group.setScaleY(scaleY);

				// move viewport so that old center remains in the center after the
				// scaling
				repositionScroller(scrollContent, scroller, scaleFactor, scrollOffset);

			}
		});

		// Panning via drag....
		final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
		scrollContent.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
			}
		});

		scrollContent.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double deltaX = event.getX() - lastMouseCoordinates.get().getX();
				double extraWidth = scrollContent.getLayoutBounds().getWidth()
						- scroller.getViewportBounds().getWidth();
				double deltaH = deltaX * (scroller.getHmax() - scroller.getHmin()) / extraWidth;
				double desiredH = scroller.getHvalue() - deltaH;
				if (!Double.isNaN(desiredH)) {
					scroller.setHvalue(Math.max(0, Math.min(scroller.getHmax(), desiredH)));
				}

				double deltaY = event.getY() - lastMouseCoordinates.get().getY();
				double extraHeight = scrollContent.getLayoutBounds().getHeight()
						- scroller.getViewportBounds().getHeight();
				double deltaV = deltaY * (scroller.getHmax() - scroller.getHmin()) / extraHeight;
				double desiredV = scroller.getVvalue() - deltaV;
				if (!Double.isNaN(desiredV)) {
					scroller.setVvalue(Math.max(0, Math.min(scroller.getVmax(), desiredV)));
				}
			}
		});

		return scroller;
	}

	private static Point2D figureScrollOffset(Node scrollContent, ScrollPane scroller) {
		double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
		double hScrollProportion = (scroller.getHvalue() - scroller.getHmin())
				/ (scroller.getHmax() - scroller.getHmin());
		double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
		double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
		double vScrollProportion = (scroller.getVvalue() - scroller.getVmin())
				/ (scroller.getVmax() - scroller.getVmin());
		double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
		return new Point2D(scrollXOffset, scrollYOffset);
	}

	private static void repositionScroller(Node scrollContent, ScrollPane scroller, double scaleFactor,
			Point2D scrollOffset) {
		double scrollXOffset = scrollOffset.getX();
		double scrollYOffset = scrollOffset.getY();
		double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
		if (extraWidth > 0) {
			double halfWidth = scroller.getViewportBounds().getWidth() / 2;
			double newScrollXOffset = (scaleFactor - 1) * halfWidth + scaleFactor * scrollXOffset;
			scroller.setHvalue(
					scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
		} else {
			scroller.setHvalue(scroller.getHmin());
		}
		double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
		if (extraHeight > 0) {
			double halfHeight = scroller.getViewportBounds().getHeight() / 2;
			double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
			scroller.setVvalue(
					scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
		} else {
			scroller.setHvalue(scroller.getHmin());
		}
	}
}
