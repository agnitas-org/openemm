/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.dto;

import com.agnitas.emm.core.address_management.enums.AddressManagementCategory;

import java.util.Objects;

public class AddressManagementEntry {

    private int id;
    private int companyId;
    private AddressManagementCategory category;

    public AddressManagementEntry() {
    }

    public AddressManagementEntry(int id, int companyId, AddressManagementCategory category) {
        this.id = id;
        this.companyId = companyId;
        this.category = category;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public void setCategory(AddressManagementCategory category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public AddressManagementCategory getCategory() {
        return category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressManagementEntry entry = (AddressManagementEntry) o;
        return id == entry.id && companyId == entry.companyId && category == entry.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, category);
    }
}
