package com.atlas.core.resource;


import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
public class InformationResource {

    private final Object env;

    public InformationResource(Object env) {
        this.env = env;
    }

    @GET
    @Path("/version")
    public Response getVersion() {
        return Response.ok(getClass().getPackage().getImplementationVersion()).build();
    }

    @GET
    @Path("/properties")
    public Response getProperties() {
        return Response.ok(env).build();
    }

}
