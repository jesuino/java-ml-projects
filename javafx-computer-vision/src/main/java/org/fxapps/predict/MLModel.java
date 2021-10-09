package org.fxapps.predict;

import java.awt.image.BufferedImage;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import org.fxapps.predict.model.PoseEstimationPrediction;

public abstract class MLModel<T> {

    public abstract String getName();

    public T predict(BufferedImage image) {
        var img = ImageFactory.getInstance().fromImage(image);
        return predict(img);
    }

    protected abstract T predict(Image image);

    public BufferedImage predictAndDraw(BufferedImage image) {
        var img = ImageFactory.getInstance().fromImage(image);
        T result = predict(img);

        if (result instanceof Joints joints) {
            img.drawJoints(joints);
        }

        if (result instanceof PoseEstimationPrediction poseEstimation) {
            poseEstimation.personRect().ifPresent(rect -> {
                img.getSubimage((int) rect.getX(),
                                (int) rect.getY(),
                                (int) rect.getWidth(),
                                (int) rect.getHeight())
                   .drawJoints(poseEstimation.joints());
            });
        }

        if (result instanceof DetectedObjects objects) {
            img.drawBoundingBoxes(objects);
        }
        // do not work on Android due hard code of BufferedImage
        // change the use of BufferedImage so it should work on Android as well
        return (BufferedImage) img.getWrappedImage();
    }

    @Override
    public String toString() {
        return getName();
    }

}
