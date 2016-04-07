package com.fererlab.service;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;

@Path("/service")
@Produces({"application/json"})
@Consumes({"*/*"})
public interface Service {

    @POST
    @Path("/handle/")
    Object handle(HashMap<String, String> event);

}
