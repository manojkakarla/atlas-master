package com.atlas.core;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/test")
public class TestResource {

    @GET
    @Path("/")
    public Response testRequest(@QueryParam("code") int code,
                                @QueryParam("message") @DefaultValue("test response") String message) {
        return Response.status(code).entity(message).build();
    }

}
