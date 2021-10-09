package org.fxapps.predict.model;

import java.util.Optional;

import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.output.Rectangle;

public record PoseEstimationPrediction(Optional<Rectangle> personRect, Joints joints) {

}