package com.sap.broker.budgie.helpers;

import com.sap.broker.budgie.configuration.ApplicationConfiguration;
import com.sap.broker.budgie.configuration.behavior.ServiceBrokerConfiguration;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ConfigurationValidator {

    private ApplicationConfiguration appConfiguration;

    @Inject
    public ConfigurationValidator(ApplicationConfiguration configuration) {
        this.appConfiguration = configuration;
    }

    public boolean validate(ServiceBrokerConfiguration configuration) {
        return validateAsyncDuration(configuration) &&
               validateSyncDuration(configuration) &&
               validateOperationType(configuration) &&
               validateStatus(configuration) &&
               validateServiceNames(configuration) &&
               validatePlanNames(configuration) &&
               validateServiceIds(configuration) &&
               validatePlanIds(configuration);
    }

    private boolean validateAsyncDuration(ServiceBrokerConfiguration configuration) {
        return configuration.getAsyncDuration() == null || configuration.getAsyncDuration() >= 0;
    }

    private boolean validateSyncDuration(ServiceBrokerConfiguration configuration) {
        return configuration.getSyncDuration() == null || configuration.getSyncDuration() >= 0;
    }

    private boolean validateOperationType(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream().allMatch(config -> config.getOperationType() != null);
        }
        return true;
    }

    private boolean validateStatus(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream().allMatch(config -> {
                Integer status = config.getStatus();
                return status != null && status >= 400 && status < 600;
            });
        }
        return true;
    }

    private boolean validateServiceNames(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream()
                .filter(failConfiguration -> failConfiguration.getServiceNames() != null)
                .flatMap(failConfiguration -> failConfiguration.getServiceNames().stream())
                .allMatch(serviceName -> appConfiguration.getServices().anyMatch(offering -> offering.getName().equals(serviceName)));
        }
        return true;
    }

    private boolean validatePlanNames(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream()
                .filter(failConfiguration -> failConfiguration.getPlanNames() != null)
                .flatMap(failConfiguration -> failConfiguration.getPlanNames().stream())
                .allMatch(planName -> appConfiguration.getPlans().anyMatch(plan -> plan.getName().equals(planName)));
        }
        return true;
    }

    private boolean validateServiceIds(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream()
                .filter(failConfiguration -> failConfiguration.getServiceIds() != null)
                .flatMap(failConfiguration -> failConfiguration.getServiceIds().stream())
                .allMatch(serviceId -> appConfiguration.getServices().anyMatch(offering -> offering.getId().equals(serviceId)));
        }
        return true;
    }

    private boolean validatePlanIds(ServiceBrokerConfiguration configuration) {
        if (configuration.getFailConfigurations() != null) {
            return configuration.getFailConfigurations().stream()
                .filter(failConfiguration -> failConfiguration.getPlanIds() != null)
                .flatMap(failConfiguration -> failConfiguration.getPlanIds().stream())
                .allMatch(planId -> appConfiguration.getPlans().anyMatch(plan -> plan.getId().equals(planId)));
        }
        return true;
    }
}
