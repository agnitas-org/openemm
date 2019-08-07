/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exceptionresolver;

import java.util.HashMap;
import java.util.Map;

import org.agnitas.emm.springws.exceptionresolver.CommonExceptionResolver;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

public class ConfigurableCommonExceptionResolver extends CommonExceptionResolver {

    private static final Logger logger = Logger.getLogger(ConfigurableCommonExceptionResolver.class);

    private final Map<Class<? extends Exception>, String> exceptionMessageMappings = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ConfigurableCommonExceptionResolver(Map<String, String> exceptionMessageMappings) {
        exceptionMessageMappings.forEach((key, value) -> {
            try {
                Class<Exception> exceptionClass = (Class<Exception>) Class.forName(key);
                this.exceptionMessageMappings.put(exceptionClass, value);
            } catch (ClassNotFoundException e) {
                logger.error("Specified exception cannot be found.", e);
            } catch (ClassCastException e) {
                logger.error("Specified class name is not en exception.", e);
            }
        });
    }

    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        if (exceptionMessageMappings.containsKey(ex.getClass())) {
            String message = exceptionMessageMappings.get(ex.getClass());
            if (StringUtils.isBlank(message)) {
                message = ex.getMessage();
            }
            SoapFaultDefinition definition = getDefaultDefinition(ex);
            definition.setFaultStringOrReason(message);
            return definition;
        }
        return super.getFaultDefinition(endpoint, ex);
    }
}
