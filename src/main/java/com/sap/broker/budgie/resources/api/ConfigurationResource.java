package com.sap.broker.budgie.resources.api;

import com.sap.broker.budgie.configuration.behavior.BehaviorConfiguration;
import com.sap.broker.budgie.configuration.behavior.BehaviorEngine;
import com.sap.broker.budgie.helpers.BehaviorConfigurationValidator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v2/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationResource {

    private BehaviorEngine behaviorEngine;
    private BehaviorConfigurationValidator behaviorConfigurationValidator;

    @Inject
    public ConfigurationResource(BehaviorEngine behaviorEngine, BehaviorConfigurationValidator behaviorConfigurationValidator) {
        this.behaviorEngine = behaviorEngine;
        this.behaviorConfigurationValidator = behaviorConfigurationValidator;
    }

    @GET
    public Response getAll() {
        return Response.ok(behaviorEngine.getConfigurations()).build();
    }

    @GET
    @Path("/{configId}")
    public Response getConfiguration(@PathParam("configId") String configId) {
        BehaviorConfiguration configuration = behaviorEngine.getConfiguration(configId);
        if (configuration == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(configuration).build();
    }

    @PUT
    @Path("/{configId}")
    public Response configure(@PathParam("configId") String configId, BehaviorConfiguration configuration) {
        if (!behaviorConfigurationValidator.validate(configuration)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        behaviorEngine.addConfiguration(configId, configuration);
        return Response.ok(configuration).build();
    }

    @DELETE
    @Path("/{configId}")
    public Response reset(@PathParam("configId") String configId) {
        if (behaviorEngine.removeConfiguration(configId) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }
}
