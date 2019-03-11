package org.fxapps.microservices.imageclassifier;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import org.apache.commons.io.IOUtils;
import org.fxapps.microservices.imageclassifier.service.ImageClassifierServiceImpl;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ImageClassifierTest {
	
	public void setup() {
	    
	}

	@Test
	public void testDefaultModel() throws Exception {
		byte[] myBike = IOUtils.toByteArray(getClass().getResourceAsStream("/my_bike.jpg"));
		given().body(myBike).when().post("/").then().statusCode(200).body(containsString("mountain_bike"));
		when().get("/").then().statusCode(200).body(containsString(ImageClassifierServiceImpl.DEFAULT_MODEL_TYPE));
	}
	
}
