package com.sap.broker.budgie.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class BindingMetadata {

    private UUID id;

    @SerializedName("service_id")
    private UUID serviceId;

    @SerializedName("plan_id")
    private UUID planId;

    private Map<String, String> parameters;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
