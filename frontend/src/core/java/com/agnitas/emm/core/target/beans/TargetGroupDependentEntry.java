/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.beans;

public class TargetGroupDependentEntry {

    private int id;
    private int companyId;
    private TargetGroupDependencyType type;
    private boolean isActual;

    public TargetGroupDependentEntry(int id, TargetGroupDependencyType type, int companyId) {
        this(id, type, true, companyId);
    }

    public TargetGroupDependentEntry(int id, TargetGroupDependencyType type, boolean isActual, int companyId) {
        this.id = id;
        this.type = type;
        this.isActual = isActual;
        this.companyId = companyId;
    }

    public int getId() {
        return id;
    }

    public TargetGroupDependencyType getType() {
        return type;
    }

    public boolean isActual() {
        return isActual;
    }

    public int getCompanyId() {
        return companyId;
    }
}
