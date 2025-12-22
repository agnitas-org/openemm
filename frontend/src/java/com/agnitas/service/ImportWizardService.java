/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps;
import org.json.JSONArray;
import com.agnitas.service.impl.ImportWizardContentParseException;
import com.agnitas.util.CsvColInfo;

public interface ImportWizardService {

    ImportWizardHelper createHelper();

    /**
     * Tries to read csv file Reads database column structure reads first line
     * splits line into tokens
     */
    ServiceResult<List<CsvColInfo>> parseFirstLine(ImportWizardHelper helper);

    /**
     * check in the columnMapping for the key column, and eventually for gender
     * and mailtype read first csv line again; do not parse (allready parsed in
     * parseFirstLine) prepare download-files for errors and parsed data read
     * the rest of the csv-file
     */
    void parseContent(ImportWizardHelper helper) throws ImportWizardContentParseException;

    SimpleServiceResult checkAndReadCsvFile(ImportWizardSteps steps, Admin admin) throws IOException;

    int getLinesOKFromFile(ImportWizardHelper helper) throws Exception;

    JSONArray getParsedContentJson(ImportWizardHelper helper, Admin admin);

    Map<Integer, String> getCsvUploads(Admin admin);
}
