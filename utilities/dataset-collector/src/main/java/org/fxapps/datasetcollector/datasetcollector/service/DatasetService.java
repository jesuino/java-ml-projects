package org.fxapps.datasetcollector.datasetcollector.service;

import java.io.InputStream;
import java.util.List;

public interface DatasetService {

	void store(InputStream image, String extension, String label);

	List<String> listLabels();

}
