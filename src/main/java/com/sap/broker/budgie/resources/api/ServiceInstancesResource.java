package com.sap.broker.budgie.resources.api;

import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.broker.budgie.configuration.behavior.BehaviorEngine;
import com.sap.broker.budgie.configuration.behavior.FailConfiguration;
import com.sap.broker.budgie.domain.Binding;
import com.sap.broker.budgie.domain.ServiceInstance;
import com.sap.broker.budgie.exception.InterruptedOperationException;
import com.sap.broker.budgie.helpers.AsyncOperation;
import com.sap.broker.budgie.helpers.AsyncOperationState;
import com.sap.broker.budgie.impl.AsyncOperationManager;
import com.sap.broker.budgie.impl.ServiceBroker;

@Path("/{configId}/v2/service_instances")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceInstancesResource {

    private ServiceBroker serviceBroker;
    private BehaviorEngine behaviorEngine;
    private AsyncOperationManager asyncOperationManager;

    @Inject
    public ServiceInstancesResource(ServiceBroker serviceBroker, BehaviorEngine behaviorEngine, AsyncOperationManager asyncOperationManager) {
        this.serviceBroker = serviceBroker;
        this.behaviorEngine = behaviorEngine;
        this.asyncOperationManager = asyncOperationManager;
    }

    @GET
    public Collection<ServiceInstance> getAll() {
        return serviceBroker.getAll();
    }

    @GET
    @Path("/{id}")
    public ServiceInstance get(@PathParam("configId") String configId, @PathParam("id") UUID id) {
        return serviceBroker.get(id);
    }

    @GET
    @Path("/{id}/last_operation")
    public Response getLastOperation(@PathParam("configId") String configId, @PathParam("id") UUID id) {
        AsyncOperation asyncOperation = asyncOperationManager.getOperation(id);
        if (asyncOperation == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        return Response.ok(asyncOperation.getOperationState()).build();
    }

    @PUT
    @Path("/{id}")
    public Response create(@PathParam("configId") String configId, @PathParam("id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete, ServiceInstance serviceInstance) {
        if (behaviorEngine.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(422);
            }
            return createAsync(configId, id, serviceInstance);
        }
        return createSync(configId, id, serviceInstance);
    }

    @PATCH
    @Path("/{id}")
    public Response update(@PathParam("configId") String configId, @PathParam("id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete, ServiceInstance serviceInstance) {
        if (behaviorEngine.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(422);
            }
            return updateAsync(configId, id, serviceInstance);
        }
        return updateSync(configId, id, serviceInstance);
    }

    @DELETE
    public Response deleteAll(@PathParam("configId") String configId) {
        serviceBroker.deleteAll();
        return emptyBodyResponse(Status.OK);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("configId") String configId, @PathParam("id") UUID id, @QueryParam("accepts_incomplete") boolean acceptIncomplete) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.GONE);
        }
        if (behaviorEngine.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(422);
            }
            return deleteAsync(configId, id, serviceInstance);
        }
        return deleteSync(configId, id, serviceInstance);
    }

    @GET
    @Path("/{id}/service_bindings/{binding_id}/last_operation")
    public Response getLastBindOperation(@PathParam("configId") String configId, @PathParam("id") UUID id, @PathParam("binding_id") UUID bindingId) {
        AsyncOperation asyncOperation = asyncOperationManager.getOperation(bindingId);
        if (asyncOperation == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        return Response.ok(asyncOperation.getOperationState()).build();
    }

    @GET
    @Path("/{id}/service_bindings/{binding_id}")
    public Response getBinding(@PathParam("configId") String configId, @PathParam("id") UUID id, @PathParam("binding_id") UUID bindingId) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.NOT_FOUND);
        }
        Binding binding = serviceInstance.getBinding(bindingId);
        if (binding == null) {
            return emptyBodyResponse(Status.NOT_FOUND);
        }
        return Response.ok(binding).build();
    }

    @PUT
    @Path("/{id}/service_bindings/{binding_id}")
    public Response bind(@PathParam("configId") String configId, @PathParam("id") UUID id, @PathParam("binding_id") UUID bindingId,
        @QueryParam("accepts_incomplete") boolean acceptIncomplete, Binding binding) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        if (behaviorEngine.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(422);
            }
            return bindAsync(configId, bindingId, serviceInstance, binding);
        }
        return bindSync(configId, bindingId, serviceInstance, binding);
    }

    @DELETE
    @Path("/{id}/service_bindings/{binding_id}")
    public Response unbind(@PathParam("configId") String configId, @PathParam("id") UUID id, @PathParam("binding_id") UUID bindingId,
        @QueryParam("accepts_incomplete") boolean acceptIncomplete) {
        ServiceInstance serviceInstance = serviceBroker.get(id, false);
        if (serviceInstance == null) {
            return emptyBodyResponse(Status.BAD_REQUEST);
        }
        if (behaviorEngine.isAsync(configId)) {
            if (!acceptIncomplete) {
                return emptyBodyResponse(422);
            }
            return unbindAsync(configId, bindingId, serviceInstance);
        }
        return unbindSync(configId, bindingId, serviceInstance);
    }

    private Response createSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepIfWanted(configId);
        Optional<Integer> optionalStatusCode = behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.CREATE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceInstance.setId(id);
        return create(serviceInstance);
    }

    private Response createAsync(String configId, UUID id, ServiceInstance serviceInstance) {
        AsyncOperation asyncOperation = new AsyncOperation(() -> {
            if (behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.CREATE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.setId(id);
            create(serviceInstance);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        }, behaviorEngine.getDuration(configId));
        asyncOperationManager.addOperation(id, asyncOperation);
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
        AsyncOperation asyncOperation = new AsyncOperation(() -> {
            if (behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.UPDATE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.setId(id);
            serviceBroker.update(serviceInstance);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        }, behaviorEngine.getDuration(configId));
        asyncOperationManager.addOperation(id, asyncOperation);
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response updateSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepIfWanted(configId);
        Optional<Integer> optionalStatusCode = behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.UPDATE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceInstance.setId(id);
        serviceBroker.update(serviceInstance);
        return emptyBodyResponse(Status.OK);
    }

    private Response deleteSync(String configId, UUID id, ServiceInstance serviceInstance) {
        sleepIfWanted(configId);
        Optional<Integer> optionalStatusCode = behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.DELETE, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        serviceBroker.delete(id);
        return emptyBodyResponse(Status.OK);
    }

    private Response deleteAsync(String configId, UUID id, ServiceInstance serviceInstance) {
        AsyncOperation asyncOperation = new AsyncOperation(() -> {
            if (behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.DELETE, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceBroker.delete(id);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        }, behaviorEngine.getDuration(configId));
        asyncOperationManager.addOperation(id, asyncOperation);
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response bindSync(String configId, UUID bindingId, ServiceInstance serviceInstance, Binding binding) {
        sleepIfWanted(configId);
        Optional<Integer> optionalStatusCode = behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.BIND, serviceInstance);
        if (optionalStatusCode.isPresent()) {
            return emptyBodyResponse(optionalStatusCode.get());
        }
        if (serviceInstance.getBinding(bindingId) != null) {
            return emptyBodyResponse(Status.CONFLICT);
        }
        serviceInstance.bind(bindingId, binding);
        return Response.status(Status.CREATED).entity(binding).build();
    }

    private Response bindAsync(String configId, UUID bindingId, ServiceInstance serviceInstance, Binding binding) {
        AsyncOperation asyncOperation = new AsyncOperation(() -> {
            if (behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.BIND, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            if (serviceInstance.getBinding(bindingId) != null) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.bind(bindingId, binding);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        }, behaviorEngine.getDuration(configId));
        asyncOperationManager.addOperation(bindingId, asyncOperation);
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private Response unbindSync(String configId, UUID bindingId, ServiceInstance serviceInstance) {
        sleepIfWanted(configId);
        Optional<Integer> optionalStatusCode = behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.UNBIND, serviceInstance);
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
        AsyncOperation asyncOperation = new AsyncOperation(() -> {
            if (behaviorEngine.shouldOperationFail(configId, FailConfiguration.OperationType.UNBIND, serviceInstance).isPresent()) {
                return new AsyncOperationState(AsyncOperationState.State.FAILED);
            }
            serviceInstance.unbind(bindingId);
            return new AsyncOperationState(AsyncOperationState.State.SUCCEEDED);
        }, behaviorEngine.getDuration(configId));
        asyncOperationManager.addOperation(bindingId, asyncOperation);
        return emptyBodyResponse(Status.ACCEPTED);
    }

    private void sleepIfWanted(String configId) {
        try {
            if (behaviorEngine.getDuration(configId) > 0) {
                Thread.sleep(behaviorEngine.getDuration(configId));
            }
        } catch (InterruptedException e) {
            throw new InterruptedOperationException(e);
        }
    }

    private Response emptyBodyResponse(Status httpStatus) {
        return emptyBodyResponse(httpStatus.getStatusCode());
    }

    private Response emptyBodyResponse(int httpStatus) {
        return Response.status(httpStatus).entity(new Object()).build();
    }
}
