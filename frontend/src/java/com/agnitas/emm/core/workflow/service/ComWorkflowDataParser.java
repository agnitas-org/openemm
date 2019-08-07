/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;

public class ComWorkflowDataParser {
    private static final transient Logger logger = Logger.getLogger(ComWorkflowDataParser.class);

    private ObjectMapper mapper;

    public ComWorkflowDataParser() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<WorkflowIcon> deSerializeWorkflowIconsList(String contentJson) {
        TypeReference<List<WorkflowIcon>> type = new TypeReference<List<WorkflowIcon>>(){
			// nothing to do
        };

        List<WorkflowIcon> icons = Collections.emptyList();
        return deserialize(contentJson, type, icons, "deSerializeWorkflowIconsList");
    }

    public String serializeWorkflowIcons(List<WorkflowIcon> icons) {
        return serialize(icons, "serializeWorkflowIcons");
    }

    public String serializeRules(List<WorkflowRule> startRules) {
        return serialize(startRules, "serializeRules");
    }

    public List<WorkflowRule> deSerializeRules(String contentJson) {
        TypeReference<List<WorkflowRule>> type = new TypeReference<List<WorkflowRule>>(){
			// nothing to do
        };

        List<WorkflowRule> startRules = Collections.emptyList();
        return deserialize(contentJson, type, startRules, "deSerializeRules");
    }

    private <T> String serialize(T value, String errorMessage) {
        try {
            return mapper.writeValueAsString(value);
        } catch (IOException e) {
            logger.error(errorMessage + ": " + e, e);
            return null;
        }
    }

    private <T> T deserialize(String contentJson, TypeReference<T> type, T defaultValue, String errorMessage) {
        if (contentJson == null) {
            return null;
        }

        try {
            return mapper.readValue(contentJson, type);
        } catch (IOException e) {
            logger.error(errorMessage + ": " + e, e);
            return defaultValue;
        }
    }
}
