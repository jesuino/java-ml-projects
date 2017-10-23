package org.fxapps.datasetcollector.datasetcollector.conf;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.logging.Logger;

@Dependent
public class Producers {

	private static final String LABELS_PROPERTY = "dataset.labels";
	private static final String OUTPUT_DIR = "dataset.output";

	@Inject
	Logger logger;

	@Produces
	@DatasetLabels
	public List<String> produceLabels() {
		String labelsProperty = System.getProperty(LABELS_PROPERTY);
		if (labelsProperty == null) {
			throw new Error("Provide comma separated values for the dataset labels using the system property "
					+ LABELS_PROPERTY);
		}
		return Stream.of(labelsProperty.split("\\,")).collect(Collectors.toList());
	}

	@Produces
	@OutputDir
	public String produceOutputDir() {
		String outputDir = System.getProperty(OUTPUT_DIR);
		if (outputDir == null) {
			outputDir = System.getProperty("user.home");
			logger.info("Using user home dir since no output dir was set: " + outputDir);
		}
		return outputDir;
	}



}
