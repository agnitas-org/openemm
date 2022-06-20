/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ComWorkflowDataParser {
    private static final transient Logger logger = LogManager.getLogger(ComWorkflowDataParser.class);

    private ObjectMapper mapper;

    public ComWorkflowDataParser() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<WorkflowIcon> deSerializeWorkflowIconsList(String contentJson) {
        TypeReference<List<WorkflowIcon>> type = new TypeReference<>(){
			// nothing to do
        };

        if (contentJson == null) {
            return null;
        }

        try {
            return mapper.readValue(contentJson, type);
        } catch (IOException e) {
            logger.error("deSerializeWorkflowIconsList: " + e, e);
            return Collections.emptyList();
        }
    }

    public String serializeWorkflowIcons(List<WorkflowIcon> icons) {
        try {
            return mapper.writeValueAsString(icons);
        } catch (IOException e) {
            logger.error("serializeWorkflowIcons: " + e, e);
            return null;
        }
    }
}
