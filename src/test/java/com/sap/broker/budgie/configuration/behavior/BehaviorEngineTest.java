package com.sap.broker.budgie.configuration.behavior;

import com.sap.broker.budgie.configuration.ApplicationConfiguration;
import com.sap.broker.budgie.domain.Catalog;
import com.sap.broker.budgie.domain.Plan;
import com.sap.broker.budgie.domain.Service;
import com.sap.broker.budgie.domain.ServiceInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BehaviorEngineTest {

    private static final Plan PLAN = new Plan(UUID.randomUUID(), "plan1", "plan description");
    private static final Service SERVICE = new Service(UUID.randomUUID(), "service1", "service description", true, Arrays.asList(PLAN));

    private static final ServiceInstance INSTANCE = new ServiceInstance(UUID.randomUUID(), SERVICE.getId(), PLAN.getId());
    private static final Catalog CATALOG = new Catalog(Arrays.asList(SERVICE));

    private static final ApplicationConfiguration APP_CONFIG = mock(ApplicationConfiguration.class);
    private BehaviorEngine behaviorEngine;

    @BeforeAll
    public static void init() {
        when(APP_CONFIG.getCatalog()).thenReturn(CATALOG);
    }

    @BeforeEach
    public void setUp() {
        behaviorEngine = new BehaviorEngine(APP_CONFIG);
    }

    @Test
    public void testShouldCreateFailWithPlanName() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setFailConfigurations(Arrays.asList(getFailConfigForPlanName(FailConfiguration.OperationType.CREATE, 400)));
        behaviorEngine.addConfiguration("test1", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test1", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldUpdateFailWithPlanId() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setFailConfigurations(Arrays.asList(getFailConfigForPlanId(FailConfiguration.OperationType.UPDATE, 404)));
        behaviorEngine.addConfiguration("test2", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test2", FailConfiguration.OperationType.UPDATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(404), optionalStatus.get());
    }

    @Test
    public void testShouldDeleteFailWithServiceId() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setFailConfigurations(Arrays.asList(getFailConfigForServiceId(FailConfiguration.OperationType.DELETE, 410)));
        behaviorEngine.addConfiguration("test3", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test3", FailConfiguration.OperationType.DELETE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(410), optionalStatus.get());
    }

    @Test
    public void testShouldBindFailWithServiceName() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setFailConfigurations(Arrays.asList(getFailConfigForServiceName(FailConfiguration.OperationType.BIND, 400)));
        behaviorEngine.addConfiguration("test4", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test4", FailConfiguration.OperationType.BIND, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldUnbindFailWithInstanceId() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setFailConfigurations(Arrays.asList(getFailConfigForInstanceId(FailConfiguration.OperationType.UNBIND, 404)));
        behaviorEngine.addConfiguration("test5", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test5", FailConfiguration.OperationType.UNBIND, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(404), optionalStatus.get());
    }

    @Test
    public void testShouldCreateFailAll() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        FailConfiguration failConfiguration = new FailConfiguration();
        failConfiguration.setAll(true);
        failConfiguration.setStatus(400);
        failConfiguration.setOperationType(FailConfiguration.OperationType.CREATE);
        behaviorConfig.setFailConfigurations(Arrays.asList(failConfiguration));
        behaviorEngine.addConfiguration("test6", behaviorConfig);
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test6", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertTrue(optionalStatus.isPresent());
        assertEquals(new Integer(400), optionalStatus.get());
    }

    @Test
    public void testShouldCreateFailWhenNoBehaviorConfiguration() {
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test", FailConfiguration.OperationType.CREATE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldUpdateFailWhenNoBehaviorConfiguration() {
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test", FailConfiguration.OperationType.UPDATE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldDeleteFailWhenNoBehaviorConfiguration() {
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test", FailConfiguration.OperationType.DELETE, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testShouldBindFailWhenNoBehaviorConfiguration() {
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test", FailConfiguration.OperationType.BIND, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test public void testShouldUnbindFailWhenNoBehaviorConfiguration() {
        Optional<Integer> optionalStatus = behaviorEngine.shouldOperationFail("test", FailConfiguration.OperationType.UNBIND, INSTANCE);
        assertFalse(optionalStatus.isPresent());
    }

    @Test
    public void testGetTimeoutWhenAsync() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setAsyncDuration(200);
        behaviorEngine.addConfiguration("test7", behaviorConfig);
        assertEquals(new Integer(200), behaviorEngine.getDuration("test7"));
        assertTrue(behaviorEngine.isAsync("test7"));
    }

    @Test
    public void testGetTimeoutWhenSync() {
        BehaviorConfiguration behaviorConfig = new BehaviorConfiguration();
        behaviorConfig.setSyncDuration(300);
        behaviorEngine.addConfiguration("test8", behaviorConfig);
        assertEquals(new Integer(300), behaviorEngine.getDuration("test8"));
        assertFalse(behaviorEngine.isAsync("test8"));
    }

    @Test
    public void testGetTimeoutWhenNoBehaviorConfiguration() {
        assertEquals(new Integer(0), behaviorEngine.getDuration("test"));
    }

    @Test
    public void testBeSynchronousWhenNoBehaviorConfiguration() {
        assertFalse(behaviorEngine.isAsync("test"));
    }

    private FailConfiguration getFailConfigForPlanName(FailConfiguration.OperationType operationType, int status) {
        FailConfiguration failConfig = new FailConfiguration();
        failConfig.setPlanNames(Arrays.asList(PLAN.getName()));
        failConfig.setStatus(status);
        failConfig.setOperationType(operationType);
        return failConfig;
    }

    private FailConfiguration getFailConfigForPlanId(FailConfiguration.OperationType operationType, int status) {
        FailConfiguration failConfig = new FailConfiguration();
        failConfig.setPlanIds(Arrays.asList(PLAN.getId()));
        failConfig.setStatus(status);
        failConfig.setOperationType(operationType);
        return failConfig;
    }

    private FailConfiguration getFailConfigForServiceName(FailConfiguration.OperationType operationType, int status) {
        FailConfiguration failConfig = new FailConfiguration();
        failConfig.setServiceNames(Arrays.asList(SERVICE.getName()));
        failConfig.setStatus(status);
        failConfig.setOperationType(operationType);
        return failConfig;
    }

    private FailConfiguration getFailConfigForServiceId(FailConfiguration.OperationType operationType, int status) {
        FailConfiguration failConfig = new FailConfiguration();
        failConfig.setServiceIds(Arrays.asList(SERVICE.getId()));
        failConfig.setStatus(status);
        failConfig.setOperationType(operationType);
        return failConfig;
    }

    private FailConfiguration getFailConfigForInstanceId(FailConfiguration.OperationType operationType, int status) {
        FailConfiguration failConfig = new FailConfiguration();
        failConfig.setInstanceIds(Arrays.asList(INSTANCE.getId()));
        failConfig.setStatus(status);
        failConfig.setOperationType(operationType);
        return failConfig;
    }

}
