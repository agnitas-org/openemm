package com.agnitas.testsupport.extension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.testsupport.EmmEasyMockSupport;
import com.agnitas.testsupport.annotation.MockBean;
import com.agnitas.testsupport.initializer.MockBeansInitializer;
import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class EmmControllerExtension extends SpringExtension {

    private static class MockWrapper {

        String beanName;
        Object mock;
        IMocksControl control;

        public MockWrapper(Object mock, IMocksControl control, String beanName) {
            this.mock = mock;
            this.control = control;
            this.beanName = beanName;
        }

        public Object getMock() {
            return mock;
        }

        public IMocksControl getControl() {
            return control;
        }

        public String getBeanName() {
            return beanName;
        }

    }

    private Map<Class<?>, MockWrapper> mocksCache;

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        super.afterTestExecution(context);
        getTestInstance(context).verifyAll();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        super.afterEach(context);
        getTestInstance(context).resetAll();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        var mockFields = getMockFields(testInstance);
        if (mocksCache == null) {
            initCache(mockFields);
            registerBeans();
        }

        injectMocks(testInstance, mockFields);
        super.postProcessTestInstance(testInstance, context);
    }

    private void initCache(Map<String, Field> mockFields) {
        mocksCache = new HashMap<>();
        mockFields.forEach((key, value) -> {
            IMocksControl control = EasyMock.createControl();
            Object mock = control.createMock(value.getType());

            mocksCache.put(value.getType(), new MockWrapper(mock, control, key));
        });
    }

    private void registerBeans() {
        MockBeansInitializer.mocks = mocksCache.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getValue().getBeanName(),
                        e -> e.getValue().getMock()
                ));
    }

    private void injectMocks(Object testInstance, Map<String, Field> mockFields) {
        mockFields.values().forEach(field -> {
            try {
                var mockWrapper = mocksCache.get(field.getType());

                field.setAccessible(true);
                field.set(testInstance, mockWrapper.getMock());

                ((EmmEasyMockSupport) testInstance).addMockControl(mockWrapper.getControl());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Map<String, Field> getMockFields(Object testInstance) {
        Map<String, Field> fields = new HashMap<>();

        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MockBean.class)) {
                String beanName = field.getAnnotation(MockBean.class).value();
                fields.put(StringUtils.defaultIfEmpty(beanName, field.getType().getSimpleName()), field);
            }
        }

        return fields;
    }

    private EmmEasyMockSupport getTestInstance(ExtensionContext context) {
        return context.getRequiredTestInstances()
                .findInstance(EmmEasyMockSupport.class)
                .orElseThrow(() -> new IllegalStateException("Test class or its enclosing class must extend EmmEasyMockSupport"));
    }

}
