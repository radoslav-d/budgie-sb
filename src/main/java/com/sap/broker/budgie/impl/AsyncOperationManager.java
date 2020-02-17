package com.sap.broker.budgie.impl;

import com.sap.broker.budgie.helpers.AsyncOperation;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsyncOperationManager {

    private Map<UUID, AsyncOperation> asyncOperations = new ConcurrentHashMap<>();

    public synchronized AsyncOperation getOperation(UUID id) {
        return asyncOperations.get(id);
    }

    public synchronized void addOperation(UUID id, AsyncOperation asyncOperation) {
        asyncOperations.put(id, asyncOperation);
    }
}
