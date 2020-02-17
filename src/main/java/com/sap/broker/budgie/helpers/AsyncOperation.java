package com.sap.broker.budgie.helpers;

import java.util.function.Supplier;

public class AsyncOperation implements Runnable {

    private int milliseconds;
    private Supplier<AsyncOperationState> operation;
    private AsyncOperationState state;

    public AsyncOperation(Supplier<AsyncOperationState> operation, int milliseconds) {
        this.operation = operation;
        this.milliseconds = milliseconds;
    }

    @Override
    public void run() {
        try {
            state = new AsyncOperationState(AsyncOperationState.State.IN_PROGRESS);
            Thread.sleep(milliseconds);
            state = operation.get();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public AsyncOperationState getOperationState() {
        return state;
    }
}
