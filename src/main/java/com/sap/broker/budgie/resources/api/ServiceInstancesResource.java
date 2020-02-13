package com.sap.broker.budgie.resources.api;

import java.util.*;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.broker.budgie.configuration.behavior.ConfigurationManager;
import com.sap.broker.budgie.configuration.behavior.FailConfiguration;
import com.sap.broker.budgie.domain.BindingMetadata;
import com.sap.broker.budgie.domain.ServiceInstance;
import com.sap.broker.budgie.helpers.AsyncOperation;
import com.sap.broker.budgie.helpers.AsyncOperationExecutor;
import com.sap.broker.budgie.helpers.AsyncOperationState;
import com.sap.broker.budgie.impl.AsyncOperationManager;
import com.sap.broker.budgie.impl.ServiceBroker;
import org.springframework.http.HttpStatus;

@Path("/configurations/{config_id}/v2/service_instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceInstancesResource {

    private static final String EMPTY_RESPONSE_BODY = "{}";
    private ServiceBroker serviceBroker;
    private ConfigurationManager configurationManager;
    private AsyncOperationManager asyncOperationManager;

    @Inject
    public ServiceInstancesResource(ServiceBroker serviceBroker, ConfigurationManager configurationManager, AsyncOperationManager asyncOperationManager) {
        this.serviceBroker = serviceBroker;
        this.configurationManager = configurationManager;
        this.asyncOperationManager = asyncOperationManager;
    }

    @GET
    public Collection<ServiceInstance> getAll() {
        return serviceBroker.getAll();
    }

    @GET
    @Path("/{instance_id}")
    public ServiceInstance get(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id) {
        return serviceBroker.get(id);
    }

    @GET
    @Path("/{instance_id}/last_operation")
    public Response getLastOperation(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id) {
        AsyncOperation asyncOperation = asyncOperationManager.getOperation(id);
        if (asyncOperation == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        return Response.ok(asyncOperation.getOperationState()).build();
    }

    @PUT
    @Path("/{instance_id}")
    public Response create(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete, ServiceInstance serviceInstance) {
        if (configurationManager.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            return createAsync(configId, id, serviceInstance);
        }
        return createSync(configId, id, serviceInstance);
    }

    @PATCH
    @Path("/{instance_id}")
    public Response update(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete, ServiceInstance serviceInstance) {
        if (configurationManager.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            return updateAsync(configId, id, serviceInstance);
        }
        return updateSync(configId, id, serviceInstance);
    }

    @DELETE
    public Response deleteAll(@PathParam("config_id") String configId) {
        serviceBroker.deleteAll();
        return emptyBodyResponse(Status.OK);
    }

    @DELETE
    @Path("/{instance_id}")
    public Response delete(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.GONE);
        }
        if (configurationManager.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            return deleteAsync(configId, id, serviceInstance);
        }
        return deleteSync(configId, id, serviceInstance);
    }

    @GET
    @Path("/{instance_id}/service_bindings/{binding_id}/last_operation")
    public Response getLastBindOperation(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @PathParam("binding_id") UUID bindingId) {
        AsyncOperation asyncOperation = asyncOperationManager.getOperation(bindingId);
        if (asyncOperation == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        return Response.ok(asyncOperation.getOperationState()).build();
    }

    @GET
    @Path("/{instance_id}/service_bindings/{binding_id}")
    public Response getBinding(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @PathParam("binding_id") UUID bindingId) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.NOT_FOUND);
        }
        BindingMetadata binding = serviceInstance.getBinding(bindingId);
        if (binding == null) {
            return emptyBodyResponse(Status.NOT_FOUND);
        }
        return Response.ok(binding).build();
    }

    @PUT
    @Path("/{instance_id}/service_bindings/{binding_id}")
    public Response bind(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @PathParam("binding_id") UUID bindingId,
        @QueryParam("accepts_incomplete") boolean acceptIncomplete, BindingMetadata binding) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        if (configurationManager.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            return bindAsync(configId, bindingId, serviceInstance, binding);
        }
        return bindSync(configId, bindingId, serviceInstance, binding);
    }

    @DELETE
    @Path("/{instance_id}/service_bindings/{binding_id}")
    public Response unbind(@PathParam("config_id") String configId, @PathParam("instance_id") UUID id, @PathParam("binding_id") UUID bindingId,
        @QueryParam("accepts_incomplete") boolean acceptIncomplete) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        if (configurationManager.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            return unbindAsync(configId, bindingId, serviceInstance);
        }
        return unbindSync(configId, bindingId, serviceInstance);
    }

    private Response createSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepForSpecifiedDuration(configId);
        Optional<Integer> optionalStatusCode = configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.CREATE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceInstance.setId(id);
        return create(serviceInstance);
    }

    private Response createAsync(String configId, UUID id, ServiceInstance serviceInstance) {
        async(configId, id, () -> {
            if (configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.CREATE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.setId(id);
            create(serviceInstance);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        });
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response create(ServiceInstance serviceInstance) {
        if (existsIdentical(serviceInstance)) {
            return emptyBodyResponse(Status.OK);
        }
        serviceBroker.create(serviceInstance);
        return emptyBodyResponse(Status.CREATED);
    }

    private boolean existsIdentical(ServiceInstance serviceInstance) {
        ServiceInstance existingServiceInstance = serviceBroker.get(serviceInstance.getId(), false);
        return serviceInstance.equals(existingServiceInstance);
    }

    private Response updateAsync(String configId, UUID id, ServiceInstance serviceInstance) {
        async(configId, id, () -> {
            if (configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.UPDATE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.setId(id);
            serviceBroker.update(serviceInstance);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        });
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response updateSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepForSpecifiedDuration(configId);
        Optional<Integer> optionalStatusCode = configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.UPDATE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceInstance.setId(id);
        serviceBroker.update(serviceInstance);
        return emptyBodyResponse(Status.OK);
    }

    private Response deleteSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepForSpecifiedDuration(configId);
        Optional<Integer> optionalStatusCode = configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.DELETE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceBroker.delete(id);
        return emptyBodyResponse(Status.OK);
    }

    private Response deleteAsync(String configId, UUID id, ServiceInstance serviceInstance) {
        async(configId, id, () -> {
            if (configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.DELETE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceBroker.delete(id);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        });
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response bindSync(String configId, UUID bindingId, ServiceInstance serviceInstance, BindingMetadata binding) {
        sleepForSpecifiedDuration(configId);
        Optional<Integer> optionalStatusCode = configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.BIND, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        if (serviceInstance.getBinding(bindingId) != null) {
            return emptyBodyResponse(Status.CONFLICT);
        }
        serviceInstance.bind(bindingId, binding);
        return Response.status(Status.CREATED).entity(binding).build();
    }

    private Response bindAsync(String configId, UUID bindingId, ServiceInstance serviceInstance, BindingMetadata binding) {
        async(configId, bindingId, () -> {
            if (configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.BIND, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            if (serviceInstance.getBinding(bindingId) != null) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.bind(bindingId, binding);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        });
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response unbindSync(String configId, UUID bindingId, ServiceInstance serviceInstance) {
        sleepForSpecifiedDuration(configId);
        Optional<Integer> optionalStatusCode = configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.UNBIND, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        if (serviceInstance.getBinding(bindingId) == null) {
            return emptyBodyResponse(Status.GONE);
        }
        serviceInstance.unbind(bindingId);
        return emptyBodyResponse(Status.OK);
    }

    private Response unbindAsync(String configId, UUID bindingId, ServiceInstance serviceInstance) {
        async(configId, bindingId, () -> {
            if (configurationManager.shouldOperationFail(configId, FailConfiguration.OperationType.UNBIND, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.unbind(bindingId);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        });
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private void async(String configId, UUID id, Supplier<AsyncOperationState> operation) {
        AsyncOperation asyncOperation = AsyncOperationExecutor.execute(operation, configurationManager.getDuration(configId));
        asyncOperationManager.addOperation(id, asyncOperation);
    }

    private void sleepForSpecifiedDuration(String configId) {
        try {
            if (configurationManager.getDuration(configId) > 0) {
                Thread.sleep(configurationManager.getDuration(configId));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Response emptyBodyResponse(Status httpStatus) {
        return emptyBodyResponse(httpStatus.getStatusCode());
    }

    private Response emptyBodyResponse(int httpStatus) {
        return Response.status(httpStatus).entity(EMPTY_RESPONSE_BODY).build();
    }
}
