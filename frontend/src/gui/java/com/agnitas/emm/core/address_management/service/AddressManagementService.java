/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.address_management.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.address_management.dto.AddressManagementDTOBase;
import com.agnitas.emm.core.address_management.dto.AddressManagementEntry;
import com.agnitas.emm.core.address_management.enums.AddressManagementCategory;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;

import java.util.List;
import java.util.Map;

public interface AddressManagementService {

    Map<AddressManagementCategory, List<? extends AddressManagementDTOBase>> findEntries(String email, int companyId);

    List<AddressManagementCategory> getAvailableCategories();

    ServiceResult<List<AddressManagementEntry>> deleteEntries(String emailPart, List<AddressManagementEntry> entries, Admin admin);

    ServiceResult<List<AddressManagementEntry>> replaceEmails(String emailPart, String newEmail, List<AddressManagementEntry> entries, Admin admin);

    SimpleServiceResult deleteAll(String emailPart, Admin admin);

    void replaceEmails(String emailPart, String newEmail, Admin admin);
}
