/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.dto;

public class UserAddressManagementDTO extends AddressManagementDTOBase {

    private final String username;
    private final String firstname;
    private final String lastname;
    private final long lastLoginTimeMs;

    public UserAddressManagementDTO(int id, int companyId, String username, String firstname, String lastname, long lastLoginTimeMs) {
        super(id, companyId);
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.lastLoginTimeMs = lastLoginTimeMs;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public long getLastLoginTimeMs() {
        return lastLoginTimeMs;
    }
}
