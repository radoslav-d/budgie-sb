package com.sap.broker.budgie.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sap.broker.budgie.configuration.ApplicationConfiguration;
import com.sap.broker.budgie.domain.Catalog;
import com.sap.broker.budgie.domain.ServiceInstance;
import com.sap.broker.budgie.exception.NotFoundException;

@Component
public class ServiceBroker {

    private ApplicationConfiguration configuration;
    private Map<UUID, ServiceInstance> serviceInstances = new HashMap<>();

    @Inject
    public ServiceBroker(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    public Catalog getCatalog() {
        return configuration.getCatalog();
    }

    public synchronized Collection<ServiceInstance> getAll() {
        return serviceInstances.values();
    }

    public ServiceInstance get(UUID id) {
        return get(id, true);
    }

    public synchronized ServiceInstance get(UUID id, boolean required) {
        ServiceInstance serviceInstance = serviceInstances.get(id);
        if (serviceInstance == null && required) {
            throw new NotFoundException(MessageFormat.format("Service instance \"{0}\" not found!", id));
        }
        return serviceInstance;
    }

    public synchronized void create(ServiceInstance serviceInstance) {
        serviceInstances.put(serviceInstance.getId(), serviceInstance);
    }

    public synchronized void update(ServiceInstance serviceInstance) {
        serviceInstances.put(serviceInstance.getId(), serviceInstance);
    }

    public synchronized void deleteAll() {
        serviceInstances.clear();
    }

    public synchronized void delete(UUID id) {
        ServiceInstance serviceInstance = serviceInstances.remove(id);
        if (serviceInstance == null) {
            throw new NotFoundException(MessageFormat.format("Service instance \"{0}\" not found!", id));
        }
    }

}
