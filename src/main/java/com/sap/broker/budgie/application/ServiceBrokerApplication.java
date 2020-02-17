package com.sap.broker.budgie.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.sap.broker.budgie.providers.GsonMessageBodyReader;
import com.sap.broker.budgie.providers.GsonMessageBodyWriter;
import com.sap.broker.budgie.providers.ServiceBrokerExceptionMapper;
import com.sap.broker.budgie.resources.api.CatalogResource;
import com.sap.broker.budgie.resources.api.ConfigurationResource;
import com.sap.broker.budgie.resources.api.ServiceInstancesResource;

@ApplicationPath("/")
public class ServiceBrokerApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(CatalogResource.class);
        classes.add(ServiceInstancesResource.class);
        classes.add(GsonMessageBodyWriter.class);
        classes.add(GsonMessageBodyReader.class);
        classes.add(ServiceBrokerExceptionMapper.class);
        classes.add(ConfigurationResource.class);
        return classes;
    }

}
