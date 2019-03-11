package org.fxapps.microservices.imageclassifier;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

import org.apache.commons.io.IOUtils;
import org.fxapps.microservices.imageclassifier.service.ServiceProperties;

public class ImageClassifierWithCustomModelTest {

    private static final String MNIST = "Mnist";
    
    static {
        System.setProperty(ServiceProperties.MODEL_PATH, "/mnist-model.zip");
        System.setProperty(ServiceProperties.MODEL_LABELS, "0,1,2,3,4,5,6,7,8,9");
        System.setProperty(ServiceProperties.MODEL_TYPE, MNIST);
        System.setProperty(ServiceProperties.MODEL_INPUT_WIDTH, "28");
        System.setProperty(ServiceProperties.MODEL_INPUT_HEIGHT, "28");
        System.setProperty(ServiceProperties.MODEL_INPUT_CHANNELS, "1");
    }
        
// TODO: find a way to set the system properties to make this test valid again - otherwise this will always fail
    //@Test
    public void testCustomModel() throws Exception {
        byte[] myBike = IOUtils.toByteArray(getClass().getResourceAsStream("/my_bike.jpg"));
        given().body(myBike).when().post("/").then().statusCode(200).body(containsString("2"));
        when().get("/").then().statusCode(200).body(containsString(MNIST));
    }
}
