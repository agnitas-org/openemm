/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ActionOperationParametersParser {
	
    private static final Logger logger = LogManager.getLogger(ActionOperationParametersParser.class);

    private final ObjectMapper mapper;

    public ActionOperationParametersParser() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<AbstractActionOperationParameters> deSerializeActionModulesList(String contentJson) {
        TypeReference<List<AbstractActionOperationParameters>> type = new TypeReference<>(){
			// nothing to do
        };

        if (contentJson == null) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(contentJson, type);
        } catch (IOException e) {
            logger.error("deSerializeActionOperationParametersList: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public String serializeActionModules(List<? extends AbstractActionOperationParameters> modules) {
        try {
            return mapper.writeValueAsString(modules);
        } catch (IOException e) {
            logger.error("serializeActionOperationParameters: {}", e.getMessage(), e);
            return null;
        }
    }
}
