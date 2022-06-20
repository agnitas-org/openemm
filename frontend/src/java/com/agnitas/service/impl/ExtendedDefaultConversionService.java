/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Service;

import com.agnitas.service.ExtendedConversionService;

@Service("conversionService")
public class ExtendedDefaultConversionService extends DefaultConversionService implements ExtendedConversionService {
    public ExtendedDefaultConversionService() {
        super();
    }

    @Autowired(required = false)
    public void setGenericConverters(Set<GenericConverter> genericConverters) {
        if (genericConverters != null) {
            for (GenericConverter converter : genericConverters) {
                addConverter(converter);
            }
        }
    }

    @Autowired(required = false)
    public void setConverters(Set<Converter<?, ?>> converters) {
        if (converters != null) {
            for (Converter<?, ?> converter : converters) {
                addConverter(converter);
            }
        }
    }

    @Autowired(required = false)
    public void setConverterFactories(Set<ConverterFactory<?, ?>> converterFactories) {
        if (converterFactories != null) {
            for (ConverterFactory<?, ?> converterFactory : converterFactories) {
                addConverterFactory(converterFactory);
            }
        }
    }
}
