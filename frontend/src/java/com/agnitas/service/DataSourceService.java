/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.beans.Admin;
import org.json.JSONArray;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;

public interface DataSourceService {

    int save(DatasourceDescription dsDescription);

    int createDataSource(int companyId, SourceGroupType sourceGroupType, String dsDescription, String uri);

    boolean rolloutCreationDataSource(int dataSourceId, String username, int companyId);

    JSONArray getDataSourcesJson(Admin admin);

    DatasourceDescription getDatasourceDescription(int datasourceId);

    DatasourceDescription getDatasourceDescription(int datasourceId, int companyId);

    DatasourceDescription getByDescription(SourceGroupType sourceGroupType, String description, int companyID);
}
