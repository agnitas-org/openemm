/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.service;

import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;

public interface SalutationService {

    PaginatedListImpl<SalutationEntry> paginatedList(int companyId, String sort, String order, int page, int rowsCount);

    Title get(int salutationId, int companyId);

    void save(Title title) throws Exception;

    boolean delete(int salutationId, int companyId);
}
