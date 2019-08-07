/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConversionServiceFactory;

import com.agnitas.service.ExtendedConversionService;

public class ExtendedConversionServiceFactoryBean implements FactoryBean<ExtendedConversionService>, InitializingBean {

    private Set<?> converters;
    private ExtendedDefaultConversionService conversionService;

    public ExtendedConversionServiceFactoryBean() {
    }

    public void setConverters(Set<?> converters) {
        this.converters = converters;
    }

    @Override
	public void afterPropertiesSet() {
        this.conversionService = this.createConversionService();
        ConversionServiceFactory.registerConverters(this.converters, this.conversionService);
    }

    protected ExtendedDefaultConversionService createConversionService() {
        return new ExtendedDefaultConversionService();
    }

    @Override
	public ExtendedConversionService getObject() {
        return this.conversionService;
    }

    @Override
	public Class<? extends ConversionService> getObjectType() {
        return ExtendedDefaultConversionService.class;
    }

    @Override
	public boolean isSingleton() {
        return true;
    }
}
