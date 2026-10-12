/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.imports.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.data.DataProvider;
import com.agnitas.service.ServiceResult;

public interface RecipientImportService {

    ServiceResult<List<List<String>>> parseFileContent(File importFile, ImportProfile profile) throws Exception;

    DataProvider getDataProvider(ImportProfile importProfile, File importFile) throws Exception;

    ImportStatus addAndUpdate(
            List<Map<String, Object>> recipients,
            List<Integer> mailinglistIds,
            MediaTypes mediatype,
            Admin admin
    );

    ImportStatus add(
            List<Map<String, Object>> recipients,
            List<Integer> mailinglistIds,
            MediaTypes mediatype,
            Admin admin
    );
}
