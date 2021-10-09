package org.fxapps.predict;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import org.fxapps.predict.model.PoseEstimationPrediction;

public final class PoseEstimationMLModel extends MLModel<PoseEstimationPrediction> {

    private Predictor<Image, DetectedObjects> objectsPredictor;
    private Predictor<Image, Joints> posePredictor;

    private static Joints EMPTY_JOINTS = new Joints(Collections.emptyList());

    public PoseEstimationMLModel() {
        super();

        try {
            // more precision
//            objectsPredictor =
//                    Criteria.builder()
//                            .optApplication(Application.CV.OBJECT_DETECTION)
//                            .setTypes(Image.class, DetectedObjects.class)
//                            .optFilter("size", "512")
//                            .optFilter("backbone", "resnet50")
//                            .optFilter("flavor", "v1")
//                            .optFilter("dataset", "voc")
//                            .optProgress(new ProgressBar())
//                            .build()
//                            .loadModel()
//                            .newPredictor();
            // faster
            objectsPredictor = Criteria.builder()
                                       .optEngine("TensorFlow")
                                       .optApplication(Application.CV.OBJECT_DETECTION)
                                       .setTypes(Image.class, DetectedObjects.class)
                                       .optFilter("backbone", "mobilenet_v2")
                                       .optArgument("threshold", "0.1")
                                       .optProgress(new ProgressBar())
                                       .build()
                                       .loadModel()
                                       .newPredictor();
        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            throw new RuntimeException("Not able to load object detector model.");
        }

        try {
            posePredictor = Criteria.builder()
                                    .optApplication(Application.CV.POSE_ESTIMATION)
                                    .setTypes(Image.class, Joints.class)
                                    .optFilter("backbone", "resnet18")
                                    .optFilter("flavor", "v1b")
                                    .optFilter("dataset", "imagenet")
                                    .build().loadModel().newPredictor();
        } catch (ModelNotFoundException | MalformedModelException | IOException e) {
            throw new RuntimeException("Not able to load pose estimatino model.");
        }

    }

    @Override
    public String getName() {
        return "Pose Estimation";
    }

    @Override
    protected PoseEstimationPrediction predict(Image image) {
        try {
            var personPosOp = retrievePerson(image);
            var personJoints = personPosOp.map(rect -> predictPose(image, rect))
                                          .orElse(EMPTY_JOINTS);
            return new PoseEstimationPrediction(personPosOp, personJoints);
        } catch (TranslateException e) {
            e.printStackTrace();
            return new PoseEstimationPrediction(Optional.empty(), EMPTY_JOINTS);
        }
    }

    private Optional<Rectangle> retrievePerson(Image img) throws TranslateException {
        var detectedBoxes = objectsPredictor.predict(img);
        // use to draw the predicted objects
        //         img.drawBoundingBoxes(detectedBoxes);
        return detectedBoxes.items()
                            .stream()
                            .map(i -> (DetectedObjects.DetectedObject) i)
                            .filter(item -> "person".equalsIgnoreCase(item.getClassName()))
                            .findFirst()
                            .map(box -> extractPersonBounds(img, box));
    }

    private Rectangle extractPersonBounds(Image img, DetectedObjects.DetectedObject box) {
        var rect = box.getBoundingBox().getBounds();
        int width = img.getWidth();
        int height = img.getHeight();

        int personX = (int) (rect.getX() * width);
        int personY = (int) (rect.getY() * height);
        int personWidth = (int) (rect.getWidth() * width);
        int personHeight = (int) (rect.getHeight() * height);
        if (personX > personWidth) {
            personX = personWidth;
        }

        if (personY > personHeight) {
            personY = personHeight;
        }

        if (personX < 0) {
            personX = 0;
        }

        if (personY < 0) {
            personY = 0;
        }

        int rectXBound = personX + personWidth;
        int rectYBound = personY + personHeight;

        if (rectXBound > img.getWidth()) {
            personWidth = personWidth - (rectXBound - img.getWidth());
        }

        if (rectYBound > img.getHeight()) {
            personHeight = personHeight - (rectYBound - img.getHeight());
        }
        return new Rectangle(personX, personY, personWidth, personHeight);
    }

    private Joints predictPose(Image image, Rectangle rect) {
        try {
            var personImage = image.getSubimage((int) rect.getX(),
                                                (int) rect.getY(),
                                                (int) rect.getWidth(),
                                                (int) rect.getHeight());
            return posePredictor.predict(personImage);
        } catch (TranslateException e) {
            e.printStackTrace();
            return EMPTY_JOINTS;
        }
    }

    // use this for debug purpose
    protected static void saveJointsImage(Image img, Joints joints) {
        Path outputDir = Paths.get("build/output");
        try {
            Files.createDirectories(outputDir);
            img.drawJoints(joints);
            Path imagePath = outputDir.resolve("joints.png");
            // Must use png format because you can't save as jpg with an alpha channel
            img.save(Files.newOutputStream(imagePath), "png");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}