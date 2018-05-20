package org.fxapps.microservices.imageclassifier;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import org.apache.commons.io.IOUtils;
import org.fxapps.microservices.imageclassifier.service.ImageClassifierServiceImpl;
import org.fxapps.microservices.imageclassifier.service.ServiceProperties;
import org.junit.Test;

import io.thorntail.Thorntail;

public class ImageClassifierTest {
	
	private static final String MNIST = "Mnist";

	@Test
	public void testDefaultModel() throws Exception {
		Thorntail.run();
		byte[] myBike = IOUtils.toByteArray(getClass().getResourceAsStream("/my_bike.jpg"));
		given().body(myBike).when().post("/").then().statusCode(200).body(containsString("mountain_bike"));
		when().get("/").then().statusCode(200).body(containsString(ImageClassifierServiceImpl.DEFAULT_MODEL_TYPE));
		Thorntail.current().stop();
	}
	
	@Test
	public void testCustomModel() throws Exception {
		System.setProperty(ServiceProperties.MODEL_PATH, "/mnist-model.zip");
		System.setProperty(ServiceProperties.MODEL_LABELS, "0,1,2,3,4,5,6,7,8,9");
		System.setProperty(ServiceProperties.MODEL_TYPE, MNIST);
		System.setProperty(ServiceProperties.MODEL_INPUT_WIDTH, "28");
		System.setProperty(ServiceProperties.MODEL_INPUT_HEIGHT, "28");
		System.setProperty(ServiceProperties.MODEL_INPUT_CHANNELS, "1");
		Thorntail.run();
		byte[] myBike = IOUtils.toByteArray(getClass().getResourceAsStream("/mnist2.png"));
		given().body(myBike).when().post("/").then().statusCode(200).body(containsString("2"));
		when().get("/").then().statusCode(200).body(containsString(MNIST));
		Thorntail.current().stop();
	}

}
