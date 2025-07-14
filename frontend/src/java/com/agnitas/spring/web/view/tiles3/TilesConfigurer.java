/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.ServletContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.TilesException;
import org.apache.tiles.definition.DefinitionsFactory;
import org.apache.tiles.definition.DefinitionsReader;
import org.apache.tiles.definition.dao.BaseLocaleUrlDefinitionDAO;
import org.apache.tiles.definition.dao.CachingLocaleUrlDefinitionDAO;
import org.apache.tiles.definition.digester.DigesterDefinitionsReader;
import org.apache.tiles.evaluator.AttributeEvaluator;
import org.apache.tiles.evaluator.AttributeEvaluatorFactory;
import org.apache.tiles.evaluator.BasicAttributeEvaluatorFactory;
import org.apache.tiles.evaluator.impl.DirectAttributeEvaluator;
import org.apache.tiles.factory.AbstractTilesContainerFactory;
import org.apache.tiles.factory.BasicTilesContainerFactory;
import org.apache.tiles.impl.mgmt.CachingTilesContainer;
import org.apache.tiles.locale.LocaleResolver;
import org.apache.tiles.preparer.factory.PreparerFactory;
import org.apache.tiles.request.ApplicationContext;
import org.apache.tiles.request.ApplicationContextAware;
import org.apache.tiles.request.ApplicationResource;
import org.apache.tiles.startup.DefaultTilesInitializer;
import org.apache.tiles.startup.TilesInitializer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletContextAware;

public class TilesConfigurer implements ServletContextAware, InitializingBean, DisposableBean {

    private TilesInitializer tilesInitializer;
    private String[] definitions;
    private boolean checkRefresh = false;
    private boolean validateDefinitions = true;
    private Class<? extends DefinitionsFactory> definitionsFactoryClass;
    private Class<? extends PreparerFactory> preparerFactoryClass;
    private boolean useMutableTilesContainer = false;
    private ServletContext servletContext;

    /**
     * Configure Tiles using a custom TilesInitializer, typically specified as an inner bean.
     * <p>Default is a variant of {@link org.apache.tiles.startup.DefaultTilesInitializer},
     * respecting the "definitions", "preparerFactoryClass" etc properties on this configurer.
     * <p><b>NOTE: Specifying a custom TilesInitializer effectively disables all other bean
     * properties on this configurer.</b> The entire initialization procedure is then left
     * to the TilesInitializer as specified.
     */
    public void setTilesInitializer(TilesInitializer tilesInitializer) {
        this.tilesInitializer = tilesInitializer;
    }

    /**
     * Set the Tiles definitions, i.e. the list of files containing the definitions.
     * Default is "/WEB-INF/tiles.xml".
     */
    public void setDefinitions(String... definitions) {
        this.definitions = definitions;
    }

    /**
     * Set whether to check Tiles definition files for a refresh at runtime.
     * Default is "false".
     */
    public void setCheckRefresh(boolean checkRefresh) {
        this.checkRefresh = checkRefresh;
    }

    /**
     * Set whether to validate the Tiles XML definitions. Default is "true".
     */
    public void setValidateDefinitions(boolean validateDefinitions) {
        this.validateDefinitions = validateDefinitions;
    }

    /**
     * Set the {@link org.apache.tiles.definition.DefinitionsFactory} implementation to use.
     * Default is {@link org.apache.tiles.definition.UnresolvingLocaleDefinitionsFactory},
     * operating on definition resource URLs.
     * <p>Specify a custom DefinitionsFactory, e.g. a UrlDefinitionsFactory subclass,
     * to customize the creation of Tiles Definition objects. Note that such a
     * DefinitionsFactory has to be able to handle {@link java.net.URL} source objects,
     * unless you configure a different TilesContainerFactory.
     */
    public void setDefinitionsFactoryClass(Class<? extends DefinitionsFactory> definitionsFactoryClass) {
        this.definitionsFactoryClass = definitionsFactoryClass;
    }

    /**
     * Set the {@link org.apache.tiles.preparer.factory.PreparerFactory} implementation to use.
     * Default is {@link org.apache.tiles.preparer.factory.BasicPreparerFactory}, creating
     * shared instances for specified preparer classes.
     * <p>Specify {@link SimpleSpringPreparerFactory} to autowire
     * {@link org.apache.tiles.preparer.ViewPreparer} instances based on specified
     * preparer classes, applying Spring's container callbacks as well as applying
     * configured Spring BeanPostProcessors. If Spring's context-wide annotation-config
     * has been activated, annotations in ViewPreparer classes will be automatically
     * detected and applied.
     * <p>Specify {@link SpringBeanPreparerFactory} to operate on specified preparer
     * <i>names</i> instead of classes, obtaining the corresponding Spring bean from
     * the DispatcherServlet's application context. The full bean creation process
     * will be in the control of the Spring application context in this case,
     * allowing for the use of scoped beans etc. Note that you need to define one
     * Spring bean definition per preparer name (as used in your Tiles definitions).
     * @see SimpleSpringPreparerFactory
     * @see SpringBeanPreparerFactory
     */
    public void setPreparerFactoryClass(Class<? extends PreparerFactory> preparerFactoryClass) {
        this.preparerFactoryClass = preparerFactoryClass;
    }

    /**
     * Set whether to use a MutableTilesContainer (typically the CachingTilesContainer
     * implementation) for this application. Default is "false".
     * @see org.apache.tiles.mgmt.MutableTilesContainer
     * @see org.apache.tiles.impl.mgmt.CachingTilesContainer
     */
    public void setUseMutableTilesContainer(boolean useMutableTilesContainer) {
        this.useMutableTilesContainer = useMutableTilesContainer;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Creates and exposes a TilesContainer for this web application,
     * delegating to the TilesInitializer.
     * @throws TilesException in case of setup failure
     */
    @Override
    public void afterPropertiesSet() throws TilesException {
        Assert.state(this.servletContext != null, "No ServletContext available");
        ApplicationContext preliminaryContext = new SpringWildcardServletTilesApplicationContext(this.servletContext);
        if (this.tilesInitializer == null) {
            this.tilesInitializer = new SpringTilesInitializer();
        }
        this.tilesInitializer.initialize(preliminaryContext);
    }

    /**
     * Removes the TilesContainer from this web application.
     * @throws TilesException in case of cleanup failure
     */
    @Override
    public void destroy() throws TilesException {
        if (this.tilesInitializer != null) {
            this.tilesInitializer.destroy();
        }
    }

    private class SpringTilesInitializer extends DefaultTilesInitializer {

        @Override
        protected AbstractTilesContainerFactory createContainerFactory(ApplicationContext context) {
            return new SpringTilesContainerFactory();
        }
    }


    private class SpringTilesContainerFactory extends BasicTilesContainerFactory {

        @Override
        protected TilesContainer createDecoratedContainer(TilesContainer originalContainer, ApplicationContext context) {
            return (useMutableTilesContainer ? new CachingTilesContainer(originalContainer) : originalContainer);
        }

        @Override
        protected List<ApplicationResource> getSources(ApplicationContext applicationContext) {
            if (definitions != null) {
                List<ApplicationResource> result = new ArrayList<>();
                for (String definition : definitions) {
                    Collection<ApplicationResource> resources = applicationContext.getResources(definition);
                    if (resources != null) {
                        result.addAll(resources);
                    }
                }
                return result;
            }
            else {
                return super.getSources(applicationContext);
            }
        }

        @Override
        protected BaseLocaleUrlDefinitionDAO instantiateLocaleDefinitionDao(ApplicationContext applicationContext,
                                                                            LocaleResolver resolver) {
            BaseLocaleUrlDefinitionDAO dao = super.instantiateLocaleDefinitionDao(applicationContext, resolver);
            if (checkRefresh && dao instanceof CachingLocaleUrlDefinitionDAO cachingDao) {
                cachingDao.setCheckRefresh(true);
            }
            return dao;
        }

        @Override
        protected DefinitionsReader createDefinitionsReader(ApplicationContext context) {
            DigesterDefinitionsReader reader = (DigesterDefinitionsReader) super.createDefinitionsReader(context);
            reader.setValidating(validateDefinitions);
            return reader;
        }

        @Override
        protected DefinitionsFactory createDefinitionsFactory(ApplicationContext applicationContext,
                                                              LocaleResolver resolver) {

            if (definitionsFactoryClass != null) {
                DefinitionsFactory factory = BeanUtils.instantiateClass(definitionsFactoryClass);
                if (factory instanceof ApplicationContextAware appContextAware) {
                    appContextAware.setApplicationContext(applicationContext);
                }
                BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(factory);
                if (bw.isWritableProperty("localeResolver")) {
                    bw.setPropertyValue("localeResolver", resolver);
                }
                if (bw.isWritableProperty("definitionDAO")) {
                    bw.setPropertyValue("definitionDAO", createLocaleDefinitionDao(applicationContext, resolver));
                }
                return factory;
            }
            else {
                return super.createDefinitionsFactory(applicationContext, resolver);
            }
        }

        @Override
        protected PreparerFactory createPreparerFactory(ApplicationContext context) {
            if (preparerFactoryClass != null) {
                return BeanUtils.instantiateClass(preparerFactoryClass);
            }
            else {
                return super.createPreparerFactory(context);
            }
        }

        @Override
        protected LocaleResolver createLocaleResolver(ApplicationContext context) {
            return new SpringLocaleResolver();
        }

        @Override
        protected AttributeEvaluatorFactory createAttributeEvaluatorFactory(ApplicationContext context,
                                                                            LocaleResolver resolver) {
            AttributeEvaluator evaluator = new DirectAttributeEvaluator();
            return new BasicAttributeEvaluatorFactory(evaluator);
        }
    }

}
