package com.agnitas.testsupport.initializer;

import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class MockBeansInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static Map<String, Object> mocks;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (mocks == null || mocks.isEmpty()) {
            return;
        }

        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        mocks.forEach(beanFactory::registerSingleton);
    }

}

