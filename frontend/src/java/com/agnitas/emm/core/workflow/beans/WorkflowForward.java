/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public enum WorkflowForward {

    MAILING_CREATE_STANDARD("/mailing/templates.action"),
    MAILING_CREATE_EMC("/layoutbuilder/released.action"),
    MAILING_IMPORT("/import/file.action?type=MAILING"),
    MAILING_COPY("/mailing/{id}/copy.action"),
    MAILING_EDIT("/mailing/{id}/settings.action"),
    AUTO_IMPORT_CREATE("/auto-import/create.action"),
    AUTO_IMPORT_EDIT("/auto-import/{id}/view.action"),
    AUTO_EXPORT_EDIT("/auto-export/{id}/view.action"),
    AUTO_EXPORT_CREATE("/auto-export/create.action"),
    TARGET_GROUP_CREATE("/target/create.action"),
    TARGET_GROUP_EDIT("/target/{id}/view.action"),
    ARCHIVE_CREATE("/mailing/archive/create.action");

    private final String url;

    WorkflowForward(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
    
    public String getUrl(String itemId) {
        return url.replace("{id}", itemId);
    }

    public static String asJson() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(Arrays.stream(WorkflowForward.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        forward -> Map.of("name", forward.name(), "url", forward.getUrl()))));
    }

    public static WorkflowForward from(String name) {
        return Arrays.stream(WorkflowForward.values())
                .filter(e -> e.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid WorkflowForward name: " + name));
    }
}
