package com.sap.broker.budgie.configuration.behavior;

import com.sap.broker.budgie.configuration.ApplicationConfiguration;
import com.sap.broker.budgie.domain.Plan;
import com.sap.broker.budgie.domain.Service;
import com.sap.broker.budgie.domain.ServiceInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigurationManagerTest {

    private static final Plan PLAN = new Plan(UUID.randomUUID(), "plan1", "plan description");
    private static final Service SERVICE = new Service(UUID.randomUUID(), "service1", "service description", true, Arrays.asList(PLAN));
    private static final ServiceInstance INSTANCE = new ServiceInstance(UUID.randomUUID(), SERVICE.getId(), PLAN.getId());
    private static final ApplicationConfiguration APP_CONFIG = mock(ApplicationConfiguration.class);

    private ConfigurationManager configurationManager;

    @BeforeAll
    public static void init() {
        when(APP_CONFIG.getServices()).thenReturn(Stream.of(SERVICE));
        when(APP_CONFIG.getPlans()).thenReturn(Stream.of(PLAN));
    }

    @BeforeEach
    public void setUp() {
        configurationManager = new ConfigurationManager(APP_CONFIG);
    }

    @Test
    public void testShouldCreateFailWithPlanName() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setFailConfigurations(Arrays.asList(getFailConfigForPlanName(FailConfiguration.OperationType.CREATE, 400)));
        configurationManager.addConfiguration("test1", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test1", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldUpdateFailWithPlanId() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setFailConfigurations(Arrays.asList(getFailConfigForPlanId(FailConfiguration.OperationType.UPDATE, 404)));
        configurationManager.addConfiguration("test2", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test2", FailConfiguration.OperationType.UPDATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(404), optionalStatus.get());
    }

    @Test
    public void testShouldDeleteFailWithServiceId() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setFailConfigurations(Arrays.asList(getFailConfigForServiceId(FailConfiguration.OperationType.DELETE, 410)));
        configurationManager.addConfiguration("test3", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test3", FailConfiguration.OperationType.DELETE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(410), optionalStatus.get());
    }

    @Test
    public void testShouldBindFailWithServiceName() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setFailConfigurations(Arrays.asList(getFailConfigForServiceName(FailConfiguration.OperationType.BIND, 400)));
        configurationManager.addConfiguration("test4", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test4", FailConfiguration.OperationType.BIND, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldUnbindFailWithInstanceId() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setFailConfigurations(Arrays.asList(getFailConfigForInstanceId(FailConfiguration.OperationType.UNBIND, 404)));
        configurationManager.addConfiguration("test5", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test5", FailConfiguration.OperationType.UNBIND, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(404), optionalStatus.get());
    }

    @Test
    public void testShouldCreateFailAll() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        FailConfiguration failConfiguration = new FailConfiguration().setFailAll(true).setStatus(400)
            .setOperationType(FailConfiguration.OperationType.CREATE);
        config.setFailConfigurations(Arrays.asList(failConfiguration));
        configurationManager.addConfiguration("test6", config);
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test6", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldCreateFailWhenNoServiceBrokerConfiguration() {
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldUpdateFailWhenNoServiceBrokerConfiguration() {
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test", FailConfiguration.OperationType.UPDATE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldDeleteFailWhenNoServiceBrokerConfiguration() {
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test", FailConfiguration.OperationType.DELETE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldBindFailWhenNoServiceBrokerConfiguration() {
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test", FailConfiguration.OperationType.BIND, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test public void testShouldUnbindFailWhenNoServiceBrokerConfiguration() {
        Optional<Integer> optionalStatus = configurationManager.shouldOperationFail("test", FailConfiguration.OperationType.UNBIND, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testGetTimeoutWhenAsync() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setAsyncDuration(200);
        configurationManager.addConfiguration("test7", config);
        assertEquals(new Integer(200), configurationManager.getDuration("test7"));
        assertTrue(configurationManager.isAsync("test7"));
    }

    @Test
    public void testGetTimeoutWhenSync() {
        ServiceBrokerConfiguration config = new ServiceBrokerConfiguration();
        config.setSyncDuration(300);
        configurationManager.addConfiguration("test8", config);
        assertEquals(new Integer(300), configurationManager.getDuration("test8"));
        assertFalse(configurationManager.isAsync("test8"));
    }

    @Test
    public void testGetTimeoutWhenNoServiceBrokerConfiguration() {
        assertEquals(new Integer(0), configurationManager.getDuration("test"));
    }

    @Test
    public void testBeSynchronousWhenNoServiceBrokerConfiguration() {
        assertFalse(configurationManager.isAsync("test"));
    }

    private FailConfiguration getFailConfigForPlanName(FailConfiguration.OperationType operationType, int status) {
        return new FailConfiguration()
                        .setPlanNames(Arrays.asList(PLAN.getName()))
                        .setStatus(status)
                        .setOperationType(operationType);
    }

    private FailConfiguration getFailConfigForPlanId(FailConfiguration.OperationType operationType, int status) {
        return new FailConfiguration()
                        .setPlanIds(Arrays.asList(PLAN.getId()))
                        .setStatus(status)
                        .setOperationType(operationType);
    }

    private FailConfiguration getFailConfigForServiceName(FailConfiguration.OperationType operationType, int status) {
        return new FailConfiguration()
                        .setServiceNames(Arrays.asList(SERVICE.getName()))
                        .setStatus(status)
                        .setOperationType(operationType);
    }

    private FailConfiguration getFailConfigForServiceId(FailConfiguration.OperationType operationType, int status) {
        return new FailConfiguration()
                        .setServiceIds(Arrays.asList(SERVICE.getId()))
                        .setStatus(status)
                        .setOperationType(operationType);
    }

    private FailConfiguration getFailConfigForInstanceId(FailConfiguration.OperationType operationType, int status) {
        return new FailConfiguration()
                        .setInstanceIds(Arrays.asList(INSTANCE.getId()))
                        .setStatus(status)
                        .setOperationType(operationType);
    }

}
