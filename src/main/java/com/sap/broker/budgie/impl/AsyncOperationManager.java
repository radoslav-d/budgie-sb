package com.sap.broker.budgie.impl;

import com.sap.broker.budgie.helpers.AsyncOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AsyncOperationManager {

    private Map<UUID, AsyncOperation> asyncOperations = new HashMap<>();

    public synchronized AsyncOperation getOperation(UUID id) {
        return asyncOperations.get(id);
    }

    public synchronized void addOperation(UUID id, AsyncOperation asyncOperation) {
        asyncOperations.put(id, asyncOperation);
    }
}
