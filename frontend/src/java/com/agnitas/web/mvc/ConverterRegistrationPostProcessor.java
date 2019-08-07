/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

public class ConverterRegistrationPostProcessor implements BeanPostProcessor {
    private final Logger logger = Logger.getLogger(ConverterRegistrationPostProcessor.class);

    private ConversionService conversionService;

    public ConverterRegistrationPostProcessor(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof Converter<?,?>) {
            try {
                GenericConversionService genericConversionService = (GenericConversionService) conversionService;
                genericConversionService.addConverter((Converter<?, ?>) bean);
                logger.debug("registered converter: " + beanName);
            } catch (ClassCastException e){
                logger.warn("didn't registered converter: " + beanName);
            }
        }
        return bean;
    }


}
