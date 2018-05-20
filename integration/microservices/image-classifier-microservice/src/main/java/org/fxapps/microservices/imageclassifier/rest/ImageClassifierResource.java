package org.fxapps.microservices.imageclassifier.rest;

import java.io.IOException;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fxapps.microservices.imageclassifier.service.ImageClassifierService;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ImageClassifierResource {
	
	@Inject
	ImageClassifierService service;

	@POST
	public Response classifiy(DataSource imageStream) throws IOException {
		return Response.ok(service.classify(imageStream.getInputStream())).build();
	}

	@GET
	public Response info() {
		return Response.ok(service.info()).build();
	}

}