/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;


import com.agnitas.beans.Admin;

public interface CsvImportExportDescriptionService {
	CsvImportExportDescription getCsvImportExportDescription(int companyId, String cvsDescriptionName);

	CsvImportExportDescription getCsvImportExportDescription(int companyId, int cvsDescriptionID);

	List<String> getCsvImportExportDescriptionNames(int companyId, String tableName);

	List<CsvImportExportDescription> getCsvImportExportDescriptions(int companyId, String tableName, boolean forImport);

	boolean deleteCsvImportExportDescription(int id);

	ServiceResult<CsvImportExportDescription> getForDeletion(Admin admin, int id, boolean forImport);
	
	boolean deleteCsvImportExportDescription(int companyId, String cvsDescriptionName);

	SimpleServiceResult save(Admin admin, CsvImportExportDescription definition);

	boolean addConstantToMappingData(int descriptionID);
}
