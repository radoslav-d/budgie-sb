package com.sap.broker.budgie.providers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.sap.broker.budgie.domain.ServiceBrokerError;
import com.sap.broker.budgie.exception.ConflictException;
import com.sap.broker.budgie.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Provider
public class ServiceBrokerExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Response toResponse(Throwable throwable) {
        LOGGER.error(throwable.getMessage(), throwable);
        if (throwable instanceof WebApplicationException) {
            return fromWebApplicationException((WebApplicationException) throwable);
        }
        if (throwable instanceof NotFoundException) {
            return fromNotFoundException((NotFoundException) throwable);
        }
        if (throwable instanceof ConflictException) {
            return fromConflictException((ConflictException) throwable);
        }
        return fromThrowable(throwable);
    }

    private Response fromWebApplicationException(WebApplicationException e) {
        Response generatedResponse = e.getResponse();
        return fromThrowable(e, Status.fromStatusCode(generatedResponse.getStatus()));
    }

    private Response fromNotFoundException(NotFoundException e) {
        return fromThrowable(e, Status.NOT_FOUND);
    }

    private Response fromConflictException(ConflictException e) {
        return fromThrowable(e, Status.CONFLICT);
    }

    private Response fromThrowable(Throwable t) {
        return fromThrowable(t, Status.INTERNAL_SERVER_ERROR);
    }

    private Response fromThrowable(Throwable throwable, Status status) {
        return Response.status(status)
            .entity(createServiceBrokerError(throwable))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private ServiceBrokerError createServiceBrokerError(Throwable throwable) {
        return new ServiceBrokerError(null, getErrorDescription(throwable));
    }

    private String getErrorDescription(Throwable throwable) {
        return throwable.getMessage() != null ? throwable.getMessage() : "An unknown error occurred!";
    }

}
