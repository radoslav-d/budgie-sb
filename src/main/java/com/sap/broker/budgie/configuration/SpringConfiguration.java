package com.sap.broker.budgie.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.sap.broker.budgie.SpringComponentScanMarker;
import com.sap.broker.budgie.domain.Visitor;
import com.sap.broker.budgie.helpers.CatalogValidatingVisitor;
import com.sap.broker.budgie.helpers.GuidInsertingVisitor;

@Configuration
@ComponentScan(basePackageClasses = SpringComponentScanMarker.class)
public class SpringConfiguration {

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public List<Visitor> catalogVisitors() {
        return Arrays.asList(new CatalogValidatingVisitor(), new GuidInsertingVisitor());
    }
}
