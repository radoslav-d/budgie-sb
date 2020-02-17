package com.sap.broker.budgie.helpers;

import java.util.function.Supplier;

public class AsyncOperationExecutor {

    public static AsyncOperation execute(Supplier<AsyncOperationState> operation, int milliseconds) {
        AsyncOperation asyncOperation = new AsyncOperation(operation, milliseconds);
        new Thread(asyncOperation).start();
        return asyncOperation;
    }
}
