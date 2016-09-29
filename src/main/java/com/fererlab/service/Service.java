package com.fererlab.service;

import com.fererlab.event.Event;

import javax.ws.rs.*;

@Path("/api")
@Produces({"application/json"})
@Consumes({"*/*"})
public interface Service {

    @GET
    @POST
    @PUT
    @DELETE
    @HEAD
    @OPTIONS
    @Path("/handle/")
    Object handle(Event event);

}
