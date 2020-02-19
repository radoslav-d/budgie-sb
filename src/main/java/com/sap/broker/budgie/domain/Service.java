package com.sap.broker.budgie.domain;

import java.util.List;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class Service {

    private UUID id;
    private String name;
    private String description;
    private List<String> tags;
    private List<String> requires;
    private Boolean bindable;
    @SerializedName("plan_updateable")
    private Boolean planUpdateable;
    @SerializedName("instances_retrievable")
    private Boolean instancesRetrievable;
    @SerializedName("bindings_retrievable")
    private Boolean bindingsRetrievable;
    private List<Plan> plans;

    public Service(UUID id, String name, String description, boolean bindable, List<Plan> plans) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bindable = bindable;
        this.plans = plans;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getRequires() {
        return requires;
    }

    public Boolean isBindable() {
        return bindable;
    }

    public Boolean isPlanUpdateable() {
        return planUpdateable;
    }

    public List<Plan> getPlans() {
        return plans;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public void setBindable(Boolean bindable) {
        this.bindable = bindable;
    }

    public Boolean getBindable() {
        return bindable;
    }

    public Boolean getPlanUpdateable() {
        return planUpdateable;
    }

    public Boolean getInstancesRetrievable() {
        return instancesRetrievable;
    }

    public Service setInstancesRetrievable(Boolean instancesRetrievable) {
        this.instancesRetrievable = instancesRetrievable;
        return this;
    }

    public Boolean getBindingsRetrievable() {
        return bindingsRetrievable;
    }

    public Service setBindingsRetrievable(Boolean bindingsRetrievable) {
        this.bindingsRetrievable = bindingsRetrievable;
        return this;
    }

    public void setPlanUpdateable(Boolean planUpdateable) {
        this.planUpdateable = planUpdateable;
    }

    public void setPlans(List<Plan> plans) {
        this.plans = plans;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
        for (Plan plan : getPlans()) {
            plan.accept(this, visitor);
        }
    }

}
