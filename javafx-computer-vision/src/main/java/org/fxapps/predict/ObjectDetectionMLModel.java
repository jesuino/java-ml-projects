package org.fxapps.predict;

import java.io.IOException;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

public class ObjectDetectionMLModel extends MLModel<DetectedObjects> {

    private Predictor<Image, DetectedObjects> predictor;

    public ObjectDetectionMLModel() {
        try {
            predictor = buildCriteria().loadModel().newPredictor();
        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            throw new RuntimeException("Not able to load detect object models", e);
        }
    }

    protected Criteria<Image, DetectedObjects> buildCriteria() {
        return Criteria.builder()
                       .optEngine("TensorFlow")
                       .optApplication(Application.CV.OBJECT_DETECTION)
                       .setTypes(Image.class, DetectedObjects.class)
                       .optFilter("backbone", "mobilenet_v2")
                       .optArgument("threshold", "0.2")
                       .optProgress(new ProgressBar())
                       .build();
    }

    @Override
    public String getName() {
        return "Object Detection";
    }

    @Override
    public DetectedObjects predict(Image image) {
        try {
            return predictor.predict(image);
        } catch (TranslateException e) {
            throw new RuntimeException("Not able to detect objects", e);
        }
    }

}