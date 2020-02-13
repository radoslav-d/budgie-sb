package com.sap.broker.budgie.resources.api;

import com.sap.broker.budgie.configuration.behavior.ServiceBrokerConfiguration;
import com.sap.broker.budgie.configuration.behavior.ConfigurationManager;
import com.sap.broker.budgie.helpers.ConfigurationValidator;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/configurations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigurationResource {

    private ConfigurationManager configurationManager;
    private ConfigurationValidator configurationValidator;

    @Inject
    public ConfigurationResource(ConfigurationManager configurationManager, ConfigurationValidator configurationValidator) {
        this.configurationManager = configurationManager;
        this.configurationValidator = configurationValidator;
    }

    @GET
    public Response getAllConfigurations() {
        return Response.ok(configurationManager.getConfigurations()).build();
    }

    @GET
    @Path("/{configId}")
    public Response getConfiguration(@PathParam("configId") String configId) {
        ServiceBrokerConfiguration configuration = configurationManager.getConfiguration(configId);
        if (configuration == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(configuration).build();
    }

    @PUT
    @Path("/{configId}")
    public Response configure(@PathParam("configId") String configId, ServiceBrokerConfiguration configuration) {
        if (!configurationValidator.validate(configuration)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        configurationManager.addConfiguration(configId, configuration);
        return Response.ok(configuration).build();
    }

    @DELETE
    @Path("/{configId}")
    public Response reset(@PathParam("configId") String configId) {
        if (configurationManager.removeConfiguration(configId) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }
}
