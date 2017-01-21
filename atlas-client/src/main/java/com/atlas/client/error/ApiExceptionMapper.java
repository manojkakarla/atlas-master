package com.atlas.client.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper extends AppExceptionMapper<WebApplicationException> {

    @Override
    public Response.Status getResponseStatus(WebApplicationException exception) {
        return Response.Status.fromStatusCode(exception.getResponse().getStatus());
    }
}