/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.datasource.bean.DataSource;

import net.sf.json.JSONArray;

public interface DataSourceService {

    int createDataSource(int companyId, int dsGroup, String dsDescription, String uri);

    boolean rolloutCreationDataSource(int dataSourceId, String username, int companyId);

    PaginatedListImpl<DataSource> getPaginatedDataSources(@VelocityCheck int companyId, String sortColumn, int pageNumber, int pageSize, String direction);

    JSONArray getDataSourcesJson(final int companyId);
}
