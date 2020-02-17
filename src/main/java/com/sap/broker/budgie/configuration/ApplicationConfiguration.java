package com.sap.broker.budgie.configuration;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sap.broker.budgie.domain.Plan;
import com.sap.broker.budgie.domain.Service;
import org.springframework.stereotype.Component;

import com.sap.broker.budgie.Messages;
import com.sap.broker.budgie.domain.Catalog;
import com.sap.broker.budgie.domain.Visitor;

@Component
public class ApplicationConfiguration {

    static final String CFG_CATALOG = "CATALOG";

    private Environment environment;
    private List<Visitor> catalogVisitors;
    private Catalog catalog;

    @Inject
    public ApplicationConfiguration(Environment environment, List<Visitor> catalogVisitors) {
        this.environment = environment;
        this.catalogVisitors = catalogVisitors;
    }

    public Catalog getCatalog() {
        if (catalog == null) {
            catalog = getCatalogFromEnvironment();
            process(catalog);
        }
        return catalog;
    }

    public Stream<Service> getServices() {
        return getCatalog().getServices().stream();
    }

    public Stream<Plan> getPlans() {
        return getServices().flatMap(service -> service.getPlans().stream());
    }

    private Catalog getCatalogFromEnvironment() {
        Catalog catalog = environment.getJsonVariable(CFG_CATALOG, Catalog.class);
        if (catalog == null) {
            throw new IllegalStateException(MessageFormat.format(Messages.ENVIRONMENT_VARIABLE_0_IS_NOT_SET, CFG_CATALOG));
        }
        return catalog;
    }

    private void process(Catalog catalog) {
        catalogVisitors.forEach(catalog::accept);
    }

}
