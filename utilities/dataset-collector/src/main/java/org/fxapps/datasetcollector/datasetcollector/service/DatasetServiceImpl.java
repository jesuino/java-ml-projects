package org.fxapps.datasetcollector.datasetcollector.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.fxapps.datasetcollector.datasetcollector.conf.DatasetLabels;
import org.fxapps.datasetcollector.datasetcollector.conf.OutputDir;

@Default
@RequestScoped
public class DatasetServiceImpl implements DatasetService {

	@Inject
	@DatasetLabels
	private List<String> labels;
	
	@Inject
	@OutputDir
	private String outputDir;
	

	@Override
	public List<String> listLabels() {
		return labels;
	}

	@Override
	public void store(InputStream image, String extension, String label) {
		String parentDirName = outputDir + "/" + label;
		String fileName = parentDirName + "/" + System.currentTimeMillis() + "." + extension;
		Path parentDir = Paths.get(parentDirName);
		Path newFile = Paths.get(fileName);
		try {
			Files.createDirectories(parentDir);
			Files.copy(image, newFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
