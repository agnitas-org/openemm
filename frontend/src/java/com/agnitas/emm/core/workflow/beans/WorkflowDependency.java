/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class WorkflowDependency {
    private final WorkflowDependencyType type;
    private final int entityId;
    private final String entityName;

    public static WorkflowDependency from(WorkflowDependencyType type, int entityId) {
        if (entityId <= 0) {
            throw new IllegalArgumentException("entityId <= 0");
        }
        return new WorkflowDependency(type, entityId, null);
    }

    public static WorkflowDependency from(WorkflowDependencyType type, String entityName) {
        if (StringUtils.isEmpty(entityName)) {
            throw new IllegalArgumentException("entityName is empty");
        }
        return new WorkflowDependency(type, 0, entityName);
    }

    public static WorkflowDependency from(WorkflowDependencyType type, int entityId, String entityName) {
        if (entityId <= 0 && StringUtils.isEmpty(entityName)) {
            throw new IllegalArgumentException("entityId <= 0 && entityName is empty");
        }
        return new WorkflowDependency(type, entityId, entityName);
    }

    private WorkflowDependency(WorkflowDependencyType type, int entityId, String entityName) {
        this.type = type;
        this.entityId = entityId;
        this.entityName = entityName;

        Objects.requireNonNull(type, "Dependency type is required");
    }

    public WorkflowDependencyType getType() {
        return type;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public boolean equals(Object o) {
        if (getClass().isInstance(o)) {
            WorkflowDependency other = (WorkflowDependency) o;

            return type == other.type
                && entityId == other.entityId
                && StringUtils.equals(entityName, other.entityName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, entityId, entityName);
    }
}
