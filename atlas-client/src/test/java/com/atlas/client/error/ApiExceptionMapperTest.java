package com.atlas.client.error;

import org.junit.Test;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ApiExceptionMapperTest {

    private ApiExceptionMapper testObj = new ApiExceptionMapper();

    @Test
    public void testBadReq() throws Exception {
        Response.Status status = testObj.getResponseStatus(new BadRequestException("test bad req"));
        assertThat(status, is(Response.Status.BAD_REQUEST));

    }

    @Test
    public void testUnAuthorised() throws Exception {
        Response.Status status = testObj.getResponseStatus(new NotAuthorizedException("test Unauthorised"));
        assertThat(status, is(Response.Status.UNAUTHORIZED));

    }

    @Test
    public void testGetResponseStatus() throws Exception {
        Response.Status status = testObj.getResponseStatus(new BadRequestException("Bad request error", new RuntimeException("Test error")));
        assertThat(status, is(Response.Status.BAD_REQUEST));
    }

    @Test
    public void testToResponse() throws Exception {
        Response response = testObj.toResponse(new ServerErrorException("generic error", 500, new RuntimeException("error")));
        assertThat(response.getStatus(), is(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    }

    @Test
    public void testGetErrorEntity() throws Exception {
        NotAuthorizedException exception = new NotAuthorizedException("error", new RuntimeException("Unauthorised"));
        Map.Entry errorEntity = testObj.getErrorEntity(exception);
        assertThat(errorEntity.getValue(), is(exception.getMessage()));
    }
}