package com.sap.broker.budgie.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class ServiceInstance {

    private UUID id;
    @SerializedName("service_id")
    private UUID serviceId;
    @SerializedName("plan_id")
    private UUID planId;
    private Map<String, Object> parameters;
    private Map<UUID, BindingMetadata> bindings;

    public ServiceInstance(UUID id, UUID serviceId, UUID planId) {
        this.id = id;
        this.serviceId = serviceId;
        this.planId = planId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<UUID, BindingMetadata> getBindings() {
        return bindings;
    }

    public void setBindings(Map<UUID, BindingMetadata> bindings) {
        this.bindings = bindings;
    }

    public void bind(UUID bindingId, BindingMetadata binding) {
        validateBindingMap();
        binding.setId(bindingId);
        bindings.put(id, binding);
    }

    public BindingMetadata getBinding(UUID bindingId) {
        validateBindingMap();
        return bindings.get(bindingId);
    }

    public void unbind(UUID bindingId) {
        validateBindingMap();
        bindings.remove(bindingId);
    }

    private void validateBindingMap() {
        if (bindings == null) {
            setBindings(new HashMap<>());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        result = prime * result + ((planId == null) ? 0 : planId.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((bindings == null) ? 0 : bindings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        ServiceInstance other = (ServiceInstance) object;
        if (!Objects.equals(id, other.id)) {
            return false;
        }
        if (!Objects.equals(serviceId, other.serviceId)) {
            return false;
        }
        if (!Objects.equals(planId, other.planId)) {
            return false;
        }
        if (!Objects.equals(parameters, other.parameters)) {
            return false;
        }
        if (!Objects.equals(bindings, other.bindings)) {
            return false;
        }
        return true;
    }

}
