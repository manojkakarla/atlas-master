package com.atlas.client.error;

import com.google.common.collect.Maps;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AppExceptionMapper <E extends Throwable> implements ExceptionMapper<E> {

    public static final String ERROR = "error";

    public abstract Response.StatusType getResponseStatus(E exception);

    @Override
    public Response toResponse(E exception) {
        Response.StatusType responseStatus = getResponseStatus(exception);
        String responseSummary = responseStatus.getStatusCode() + " response. " + exception.getMessage();
        if (responseStatus == Response.Status.INTERNAL_SERVER_ERROR) {
            log.error(exception.getMessage());
        } else {
            log.info(responseSummary);
        }
        if (log.isDebugEnabled()) {
            log.debug(responseSummary, exception);
        }
        return Response.status(responseStatus).entity(getErrorEntity(exception)).type(MediaType.APPLICATION_JSON).build();
    }

    protected Map.Entry getErrorEntity(E exception) {
        return Maps.immutableEntry(ERROR, exception.getMessage());
    }
}

