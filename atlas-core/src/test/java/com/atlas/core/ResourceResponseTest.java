package com.atlas.core;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ResourceResponseTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TestResource()).build();

    @Test
    public void testBadRequest() throws Exception {
        String message = "BAD_DATA";
        assertThatThrownBy(() -> getResource("/test?code=400&message=" + message, String.class))
                .isInstanceOf(BadRequestException.class).hasMessage("HTTP 400 Bad Request");
    }

    @Test
    public void testNotAuthorized() throws Exception {
        String message = "NOT_AUTHORIZED";
        assertThatThrownBy(() -> getResource("/test?code=401&message=" + message, String.class))
                .isInstanceOf(NotAuthorizedException.class).hasMessage("HTTP 401 Unauthorized");
    }

    @Test
    public void testNotFound() throws Exception {
        String message = "NOT_FOUND";
        assertThatThrownBy(() -> getResource("/test?code=404&message=" + message, String.class))
                .isInstanceOf(NotFoundException.class).hasMessage("HTTP 404 Not Found");
    }
    @Test
    public void testServerError() throws Exception {
        String message = "SERVER_ERROR";
        assertThatThrownBy(() -> getResource("/test?code=500&message=" + message, String.class))
                .isInstanceOf(InternalServerErrorException.class).hasMessage("HTTP 500 Internal Server Error");
    }

    private <T> T getResource(String path, Class<T> aClass) {
        return resources.client().target(path).request().get(aClass);
    }
}