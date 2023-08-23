/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.agnitas.beans.ImportStatus;
import org.agnitas.service.ImportWizardHelper;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Classic Import
 */
public class ComImportWizardForm extends StrutsFormBase {

    private static final long serialVersionUID = -5578414938033329208L;

    public static final int BLOCK_SIZE = 1000;
    public static final String MAILTYPE_KEY = "mailtype";
    public static final String GENDER_KEY = "gender";

    public static final int MODE_DONT_IGNORE_NULL_VALUES = 0;
    public static final int MODE_IGNORE_NULL_VALUES = 1;

    protected int action;

    private FormFile csvFile;

    /**
     * this flag indicates if an import is already running (with that, writing in the
     * Database is meant.
     */
    private boolean importIsRunning;

    protected int csvMaxUsedColumn = 0;

    private String csvFileName;

    protected ImportWizardHelper importWizardHelper;

    private int attachmentCsvFileID;

    private boolean useCsvUpload;

    private boolean updateAllDuplicates = false;

    private boolean writeContentRunning = false;
    private volatile boolean futureIsRunning = false;

    public String getPreviousForward() {
        return previousForward;
    }

    public void setPreviousForward(String previousForward) {
        this.previousForward = previousForward;
    }

    private String previousForward;

    public boolean isWriteContentRunning() {
        return writeContentRunning;
    }

    public void setWriteContentRunning(boolean writeContentRunning) {
        this.writeContentRunning = writeContentRunning;
    }

    public boolean isFutureIsRunning() {
        return futureIsRunning;
    }

    public void setFutureIsRuning(boolean futureIsRunning) {
        this.futureIsRunning = futureIsRunning;
    }

    @Override
    public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        return new ActionErrors();
    }

    public boolean verifyMissingFieldsNeeded() {
        return (getMode() == ImportMode.ADD.getIntValue() || getMode() == ImportMode.ADD_AND_UPDATE.getIntValue() ||
                getMode() == ImportMode.UPDATE.getIntValue()) && (isGenderMissing() || isMailingTypeMissing());
    }

    public int getDatasourceID() {
        return importWizardHelper.getStatus().getDatasourceID();
    }

    public void setError(ImportErrorType importErrorType, String desc) {
        importWizardHelper.getStatus().addError(importErrorType);
        if (!importWizardHelper.getErrorData().containsKey(importErrorType)) {
            importWizardHelper.getErrorData().put(importErrorType, new StringBuffer());
        }
        importWizardHelper.getErrorData().get(importErrorType).append(desc).append("\n");
        importWizardHelper.getStatus().addError(ImportErrorType.ALL);
    }

    public StringBuffer getError(ImportErrorType id) {
        return importWizardHelper.getErrorData().get(id);
    }

    public Map<ImportErrorType, StringBuffer> getErrorMap() {
        return importWizardHelper.getErrorData();
    }

    public ImportStatus getStatus() {
        return importWizardHelper.getStatus();
    }

    public void setStatus(ImportStatus status) {
        importWizardHelper.setStatus(status);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public FormFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(FormFile csvFile) {
        this.csvFile = csvFile;
    }

    public ArrayList<CsvColInfo> getCsvAllColumns() {
        return importWizardHelper.getCsvAllColumns();
    }

    public void setCsvAllColumns(ArrayList<CsvColInfo> csvAllColumns) {
        importWizardHelper.setCsvAllColumns(csvAllColumns);
    }

    public Vector<String> getMailingLists() {
        return importWizardHelper.getMailingLists();
    }

    public void setMailingLists(Vector<String> mailingLists) {
        importWizardHelper.setMailingLists(mailingLists);
    }

    public ArrayList<String> getUsedColumns() {
        return importWizardHelper.getUsedColumns();
    }

    public void setUsedColumns(ArrayList<String> usedColumns) {
        importWizardHelper.setUsedColumns(usedColumns);
    }

    public LinkedList<LinkedList<Object>> getParsedContent() {
        return importWizardHelper.getParsedContent();
    }

    public void setParsedContent(LinkedList<LinkedList<Object>> parsedContent) {
        importWizardHelper.setParsedContent(parsedContent);
    }

    public Set<String> getUniqueValues() {
        return importWizardHelper.getUniqueValues();
    }

    public void setUniqueValues(Set<String> uniqueValues) {
        importWizardHelper.setUniqueValues(uniqueValues);
    }

    public Map<String, CsvColInfo> getDbAllColumns() {
        // we use a TreeMap here because it SORTS the given map.
        // the getDBAllColumns() Map is a Case-Insensitive-Map which is unsorted.
        return new TreeMap<>(importWizardHelper.getDbAllColumns());
    }

    public void setDbAllColumns(Hashtable<String, CsvColInfo> dbAllColumns) {
        importWizardHelper.setDbAllColumns(dbAllColumns);
    }

    public int getMode() {
        return importWizardHelper.getMode();
    }

    public void setMode(int mode) {
        importWizardHelper.setMode(mode);
    }

    public int getLinesOK() {
        return importWizardHelper.getLinesOK();
    }

    public void setLinesOK(int linesOK) {
        importWizardHelper.setLinesOK(linesOK);
    }

    public int getDbInsertStatus() {
        return importWizardHelper.getDbInsertStatus();
    }

    public void setDbInsertStatus(int dbInsertStatus) {
        importWizardHelper.setDbInsertStatus(dbInsertStatus);
    }

    public StringBuffer getParsedData() {
        return importWizardHelper.getParsedData();
    }

    public void setParsedData(StringBuffer parsedData) {
        importWizardHelper.setParsedData(parsedData);
    }

    public String getDownloadName() {
        return importWizardHelper.getDownloadName();
    }

    public void setDownloadName(String downloadName) {
        importWizardHelper.setDownloadName(downloadName);
    }

    /**
     * Getter for jsp page for displaying status messages (we need to give the copy of list to avoid concurrent
     * modification problem)
     *
     * @return copy of dbInsertStatusMessagesAndParameters list, to avoid concurrent list changes
     */
    public List<ActionMessage> getDbInsertStatusMessagesAndParameters() {
        return importWizardHelper.getDbInsertStatusMessagesAndParameters();
    }

    public void clearDbInsertStatusMessagesAndParameters() {
        importWizardHelper.clearDbInsertStatusMessagesAndParameters();
    }

    public void addDbInsertStatusMessageAndParameters(String messageKey, Object... additionalParameters) {
        importWizardHelper.addDbInsertStatusMessageAndParameters(messageKey, additionalParameters);
    }

    public Map<MediaTypes, Map<String, String>> getResultMailingListAdded() {
        return importWizardHelper.getResultMailingListAdded();
    }

    public void setResultMailingListAdded(Map<MediaTypes, Map<String, String>> resultMailingListAdded) {
        importWizardHelper.setResultMailingListAdded(resultMailingListAdded);
    }

    public int getPreviewOffset() {
        return importWizardHelper.getPreviewOffset();
    }

    public void setPreviewOffset(int previewOffset) {
        importWizardHelper.setPreviewOffset(previewOffset);
    }

    public String getDateFormat() {
        return importWizardHelper.getDateFormat();
    }

    public void setDateFormat(String dateFormat) {
        importWizardHelper.setDateFormat(dateFormat);
    }

    public Map<String, CsvColInfo> getColumnMapping() {
        return importWizardHelper.getColumnMapping();
    }

    public void setColumnMapping(Hashtable<String, CsvColInfo> columnMapping) {
        importWizardHelper.setColumnMapping(columnMapping);
    }

    public String getErrorId() {
        return importWizardHelper.getErrorId();
    }

    public void setErrorId(String errorId) {
        importWizardHelper.setErrorId(errorId);
    }

    public String getManualAssignedMailingType() {
        return importWizardHelper.getManualAssignedMailingType();
    }

    public void setManualAssignedMailingType(String manualAssignedMailingType) {
        importWizardHelper.setManualAssignedMailingType(manualAssignedMailingType);
    }

    public String getManualAssignedGender() {
        return importWizardHelper.getManualAssignedGender();
    }

    public void setManualAssignedGender(String manualAssignedGender) {
        importWizardHelper.setManualAssignedGender(manualAssignedGender);
    }

    public boolean isMailingTypeMissing() {
        return importWizardHelper.isMailingTypeMissing();
    }

    public void setMailingTypeMissing(boolean mailingTypeMissing) {
        importWizardHelper.setMailingTypeMissing(mailingTypeMissing);
    }

    public boolean isGenderMissing() {
        return importWizardHelper.isGenderMissing();
    }

    public void setGenderMissing(boolean genderMissing) {
        importWizardHelper.setGenderMissing(genderMissing);
    }

    public int getReadlines() {
        return importWizardHelper.getReadlines();
    }

    /**
     * returns true, if the import wizard is wir
     */
    public boolean isImportIsRunning() {
        return importIsRunning;
    }

    public void setImportIsRunning(boolean importIsRunning) {
        this.importIsRunning = importIsRunning;
    }

    public ImportWizardHelper getImportWizardHelper() {
        return importWizardHelper;
    }

    public void setImportWizardHelper(ImportWizardHelper importWizardHelper) {
        this.importWizardHelper = importWizardHelper;
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    @Override
    protected boolean isParameterExcludedForUnsafeHtmlTagCheck(String parameterName, HttpServletRequest request) {
        return parameterName.equals("dummy");
    }

    public int getAttachmentCsvFileID() {
        return attachmentCsvFileID;
    }

    public void setAttachmentCsvFileID(int attachmentCsvFileID) {
        this.attachmentCsvFileID = attachmentCsvFileID;
    }

    public boolean isUseCsvUpload() {
        return useCsvUpload;
    }

    public void setUseCsvUpload(boolean useCsvUpload) {
        this.useCsvUpload = useCsvUpload;
    }

    public boolean isUpdateAllDuplicates() {
        return updateAllDuplicates;
    }

    public void setUpdateAllDuplicates(boolean updateAllDuplicates) {
        this.updateAllDuplicates = updateAllDuplicates;
    }
}
