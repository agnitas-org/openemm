/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.IOException;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.ComImportWizardForm;
import net.sf.json.JSONArray;
import org.agnitas.service.impl.ImportWizardContentParseException;
import org.agnitas.util.CsvColInfo;
import org.apache.struts.upload.FormFile;

public interface ImportWizardService {

    ImportWizardHelper createHelper();

    /**
     * Tries to read csv file Reads database column structure reads first line
     * splits line into tokens
     */
    void parseFirstLine(ImportWizardHelper helper) throws ImportWizardContentParseException;

    ServiceResult<List<CsvColInfo>> parseFirstLineNew(ImportWizardHelper helper) throws IOException;

    /**
     * check in the columnMapping for the key column, and eventually for gender
     * and mailtype read first csv line again; do not parse (allready parsed in
     * parseFirstLine) prepare download-files for errors and parsed data read
     * the rest of the csv-file
     */
    void parseContent(ImportWizardHelper helper) throws ImportWizardContentParseException;

    void parseContentNew(ImportWizardHelper helper) throws ImportWizardContentParseException;

    //TODO check usage and remove after GWUA-5173 has been successfully tested
    void doParse(ImportWizardHelper helper) throws ImportWizardContentParseException;

    FormFile getFormFileByUploadId(int uploadID, String mime) throws Exception;
    
    SimpleServiceResult checkAndReadCsvFile(int companyId, ComImportWizardForm form);

    SimpleServiceResult checkAndReadCsvFile(ImportWizardSteps steps, Admin admin) throws IOException;
    
    int getLinesOKFromFile(ImportWizardHelper helper) throws Exception;

    JSONArray getParsedContentJson(ImportWizardHelper helper, Admin admin);
}
