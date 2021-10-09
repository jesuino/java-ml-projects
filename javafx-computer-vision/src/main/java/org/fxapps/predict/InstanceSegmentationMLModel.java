package org.fxapps.predict;

import ai.djl.Application;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;

/**
 * An example of inference using an object detection model.
 *
 * <p>See this <a
 * href="https://github.com/deepjavalibrary/djl/blob/master/examples/docs/object_detection.md">doc</a>
 * for information about this example.
 */
public final class InstanceSegmentationMLModel extends ObjectDetectionMLModel {

    @Override
    protected Criteria<Image, DetectedObjects> buildCriteria() {
        return Criteria.builder()
                       .optApplication(Application.CV.INSTANCE_SEGMENTATION)
                       .setTypes(Image.class, DetectedObjects.class)
                       .optFilter("backbone", "resnet18")
                       .optFilter("flavor", "v1b")
                       .optFilter("dataset", "coco")
                       .optArgument("threshold", "0.4")
                       .optProgress(new ProgressBar())
                       .build();
    }

    @Override
    public String getName() {
        return "Instance Segmentation";
    }

}