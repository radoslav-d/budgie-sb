package com.sap.broker.budgie.helpers;

import com.google.gson.annotations.SerializedName;

public class AsyncOperationState {

    private State state;

    private String description;

    public AsyncOperationState() {
    }

    public AsyncOperationState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum State {
        @SerializedName("succeeded")
        SUCCEEDED,

        @SerializedName("failed")
        FAILED,

        @SerializedName("in progress")
        IN_PROGRESS
    }
}
