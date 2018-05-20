package org.fxapps.microservices.imageclassifier.service;

import java.io.InputStream;

import org.fxapps.microservices.imageclassifier.model.ClassificationResults;
import org.fxapps.microservices.imageclassifier.model.ClassifierInfo;

public interface ImageClassifierService {

	ClassificationResults classify(InputStream imageStream);

	ClassifierInfo info();

}
