/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.dto;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class TriggerOperationsJsonValidation {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final Map<ActionOperationType, List<String>> REQUIRED_STRING_FIELDS = Map.of(
            ActionOperationType.CONTENT_VIEW, List.of("tagName"),
            ActionOperationType.EXECUTE_SCRIPT, List.of("script"),
            ActionOperationType.IDENTIFY_CUSTOMER, List.of("keyColumn", "passColumn"),
            ActionOperationType.SERVICE_MAIL, List.of("subjectLine", "fromAddress", "toAddress"),
            ActionOperationType.SUBSCRIBE_CUSTOMER, List.of("keyColumn"),
            ActionOperationType.UPDATE_CUSTOMER, List.of("columnName", "updateValue")
    );

    private static final Map<ActionOperationType, List<String>> REQUIRED_POSITIVE_INT_FIELDS = Map.of(
            ActionOperationType.GET_ARCHIVE_LIST, List.of("campaignID"),
            ActionOperationType.SEND_MAILING, List.of("mailingID"),
            ActionOperationType.UPDATE_CUSTOMER, List.of("updateType")
    );

    private TriggerOperationsJsonValidation() {
    }

    static void validate(String operationsJson) {
        if (operationsJson == null) {
            return;
        }

        JsonNode root;
        try {
            root = JSON_MAPPER.readTree(operationsJson);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("operations: Invalid operations JSON: " + e.getOriginalMessage());
        }
        if (!root.isArray()) {
            throw new IllegalArgumentException("operations: must be a JSON array");
        }

        for (JsonNode element : root) {
            JsonNode typeNode = element.get("type");
            String type = typeNode != null && typeNode.isTextual() ? typeNode.asText() : null;
            if (type == null) {
                throw new IllegalArgumentException("operations: each element must have a \"type\" field");
            }

            ActionOperationType operationType = ActionOperationType.fromTypeName(type);
            if (operationType == null) {
                throw new IllegalArgumentException("operations: unknown operation type \"" + type
                        + "\"; allowed values: " + ActionOperationType.getAllowedValuesStr());
            }

            for (String field : REQUIRED_STRING_FIELDS.getOrDefault(operationType, List.of())) {
                JsonNode value = element.get(field);
                if (value == null || !value.isTextual() || value.asText().isBlank()) {
                    throw new IllegalArgumentException("operations: " + type + " requires a non-blank \""
                            + field + "\" field");
                }
            }

            for (String field : REQUIRED_POSITIVE_INT_FIELDS.getOrDefault(operationType, List.of())) {
                JsonNode value = element.get(field);
                if (value == null || !value.isNumber() || value.asInt() <= 0) {
                    throw new IllegalArgumentException("operations: " + type + " requires a positive \""
                            + field + "\" field");
                }
            }
        }
    }

}
