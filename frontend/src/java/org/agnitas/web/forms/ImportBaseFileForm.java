/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.agnitas.web.ImportBaseFileAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;

/**
 * Base form that handles file panel
 */
public class ImportBaseFileForm extends StrutsFormBase {
	private static final long serialVersionUID = 6568670303380973540L;

	/**
     * property that stores current uploaded csv file name that will
     * be displayed in file panel
     */
    protected String currentFileName;

    /**
     * the uploaded csv-file
     */
    protected FormFile csvFile;

    /**
     *  Holds value of property attachmentCsvFileID.
     */
    private int attachmentCsvFileID;

  /**
     *  Holds value of property useCsvUpload.
     */

    private boolean useCsvUpload;

    public FormFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(FormFile csvFile) {
        this.csvFile = csvFile;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    /**
     * @return true if there's a csv file uploaded
     */
    public boolean getHasFile() {
        return !StringUtils.isEmpty(currentFileName);
    }

    @Override
    public ActionErrors formSpecificValidate(ActionMapping actionMapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        if (AgnUtils.parameterNotEmpty(request, "upload_file")
        		&& !AgnUtils.parameterNotEmpty(request, "remove_file")
        		&& !fileExists(request)) {
            try {
                if (!this.useCsvUpload) {
                    if (csvFile == null || csvFile.getFileSize() <= 0) {
                        errors.add("csvFile", new ActionMessage("error.import.no_file"));
                    } else {
                        if (csvFile.getFileName().contains(File.separator)) {
                            errors.add("csvFile", new ActionMessage("error.import.datafilename", File.separator));
                        }
                    }
                }
            } catch (Exception e) {
                errors.add("csvFile", new ActionMessage("error.import.cannotOpenFile", e.getMessage()));
            }
        }
        
        return errors;
    }

    private boolean fileExists(HttpServletRequest request) {
        return request.getSession().getAttribute(ImportBaseFileAction.CSV_ORIGINAL_FILE_NAME_KEY) != null;
    }

    /** Getter for property attachmentCsvFileID.
     * @return Value of property attachmentCsvFileID.
     */
    public int getAttachmentCsvFileID() {
        return attachmentCsvFileID;
    }

    /** Setter for property attachmentCsvFileID.
     * @param attachmentCsvFileID New value of property attachmentPdfFileID.
     */
    public void setAttachmentCsvFileID(int attachmentCsvFileID) {
        this.attachmentCsvFileID = attachmentCsvFileID;
    }

    /** Getter for property useCsvUpload.
     * @return Value of property useCsvUpload.
     */
    public boolean isUseCsvUpload() {
        return useCsvUpload;
    }

    /** Setter for property useCsvUpload.
     * @param useCsvUpload New value of property useCsvUpload.
     */
    public void setUseCsvUpload(boolean useCsvUpload) {
        this.useCsvUpload = useCsvUpload;
    }

}
