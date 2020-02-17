package com.sap.broker.budgie.configuration.behavior;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

public class FailConfiguration {

    private Boolean failAll;
    private Integer status;
    private OperationType operationType;
    private List<UUID> serviceIds;
    private List<UUID> planIds;
    private List<UUID> instanceIds;
    private List<String> serviceNames;
    private List<String> planNames;


    public Boolean getFailAll() {
        return failAll;
    }

    public FailConfiguration setFailAll(Boolean failAll) {
        this.failAll = failAll;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public FailConfiguration setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public FailConfiguration setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public List<UUID> getServiceIds() {
        return serviceIds;
    }

    public FailConfiguration setServiceIds(List<UUID> serviceIds) {
        this.serviceIds = serviceIds;
        return this;
    }

    public List<UUID> getPlanIds() {
        return planIds;
    }

    public FailConfiguration setPlanIds(List<UUID> planIds) {
        this.planIds = planIds;
        return this;
    }

    public List<UUID> getInstanceIds() {
        return instanceIds;
    }

    public FailConfiguration setInstanceIds(List<UUID> instanceIds) {
        this.instanceIds = instanceIds;
        return this;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public FailConfiguration setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
        return this;
    }

    public List<String> getPlanNames() {
        return planNames;
    }

    public FailConfiguration setPlanNames(List<String> planNames) {
        this.planNames = planNames;
        return this;
    }

    public enum OperationType {

        @SerializedName("create")
        CREATE,

        @SerializedName("update")
        UPDATE,

        @SerializedName("delete")
        DELETE,

        @SerializedName("bind")
        BIND,

        @SerializedName("unbind")
        UNBIND
    }
}
