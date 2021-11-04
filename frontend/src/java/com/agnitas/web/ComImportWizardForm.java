/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.beans.ImportStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportWizardHelper;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;

/**
 * Classic Import
 */
public class ComImportWizardForm extends StrutsFormBase {
    private static final long serialVersionUID = -5578414938033329208L;
	
	private static final transient Logger logger = Logger.getLogger(ComImportWizardForm.class);

	public static final int BLOCK_SIZE = 1000;
	public static final String MAILTYPE_KEY = "mailtype";
	public static final String GENDER_KEY = "gender";

	public static final int MODE_DONT_IGNORE_NULL_VALUES = 0;
	public static final int MODE_IGNORE_NULL_VALUES = 1;

	/**
	 * Holds value of property action.
	 */
	protected int action;

	/**
	 * Holds value of property csvFile.
	 */
	private FormFile csvFile;

	/**
	 * this flag indicates if an import is already running (with that, writing in the
	 * Database is meant.
	 */
	private boolean importIsRunning;

	protected int csvMaxUsedColumn = 0;

	private String csvFileName;

	/**
	 * Validate the properties that have been set from this HTTP request, and
	 * return an <code>ActionMessages</code> object that encapsulates any
	 * validation errors that have been found. If no errors are found, return
	 * <code>null</code> or an <code>ActionMessages</code> object with no
	 * recorded error messages.
	 * 
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 * @return errors
	 */

	protected ImportWizardHelper importWizardHelper;

    /**
     *  Holds value of property attachmentCsvFileID.
     */
    private int attachmentCsvFileID;

    /**
     *  Holds value of property useCsvUpload.
     */

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

	protected void handleWrite(HttpServletRequest request, ActionErrors errors) {
		if (AgnUtils.parameterNotEmpty(request, "mlists_back")) {
			action = ComImportWizardAction.ACTION_PRESCAN;
		} else {
			ComAdmin admin = AgnUtils.getAdmin(request);
			importWizardHelper.setMailingLists(getMailinglistsFromRequest(request));
			if (importWizardHelper.getMailingLists().size() <= 0
				&& getMode() != ImportMode.UPDATE.getIntValue()
				&& !(getMode() == ImportMode.ADD.getIntValue() && admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST))
				&& !(getMode() == ImportMode.ADD_AND_UPDATE.getIntValue() && admin.permissionAllowed(Permission.IMPORT_WITHOUT_MAILINGLIST))) {
				// Error: at least one mailinglist must be selected
                action = ComImportWizardAction.ACTION_MLISTS;
			}
		}
	}

	/**
	 * Getter for property datasourceID.
	 * 
	 * @return Value of property datasourceID.
	 */
	public int getDatasourceID() {
		return importWizardHelper.getStatus().getDatasourceID();
	}

	/**
	 * Sets an error.
	 */
	public void setError(ImportErrorType importErrorType, String desc) {
		importWizardHelper.getStatus().addError(importErrorType);
		if (! importWizardHelper.getErrorData().containsKey(importErrorType)) {
			importWizardHelper.getErrorData().put(importErrorType, new StringBuffer());
		}
		importWizardHelper.getErrorData().get(importErrorType).append(desc + "\n");
		importWizardHelper.getStatus().addError(ImportErrorType.ALL);
	}

	/**
	 * Getter for property error.
	 * 
	 * @return Value of property error.
	 */
	public StringBuffer getError(ImportErrorType id) {
		return importWizardHelper.getErrorData().get(id);
	}

	/**
	 * Getter for property errorMap.
	 * 
	 * @return Value of property errorMap.
	 */
	public Map<ImportErrorType, StringBuffer> getErrorMap() {
		return importWizardHelper.getErrorData();
	}

	/**
	 * Getter for property status.
	 * 
	 * @return Value of property status.
	 */
	public ImportStatus getStatus() {
		return importWizardHelper.getStatus();
	}

	/**
	 * Setter for property charset.
	 * 
	 * @param status
	 *            New value of property status.
	 */
	public void setStatus(ImportStatus status) {
		importWizardHelper.setStatus(status);
	}

	/**
	 * Getter for property action.
	 * 
	 * @return Value of property action.
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Setter for property action.
	 * 
	 * @param action
	 *            New value of property action.
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * Getter for property csvFile.
	 * 
	 * @return Value of property csvFile.
	 */
	public FormFile getCsvFile() {
		return csvFile;
	}

	/**
	 * Setter for property csvFile.
	 * 
	 * @param csvFile
	 *            New value of property csvFile.
	 */
	public void setCsvFile(FormFile csvFile) {
		this.csvFile = csvFile;
	}
	
	protected void checkAndReadCsvFile(HttpServletRequest request, ActionMessages errors) {
        FormFile importFile = null;

        if (!useCsvUpload && csvFile != null) {
            importFile = csvFile;
            attachmentCsvFileID = 0;
        } else if (useCsvUpload && attachmentCsvFileID != 0) {
            try {
                importFile = getFormFileByUploadId(attachmentCsvFileID, "text/csv");
            } catch (CsvDataInvalidItemCountException e) {
                errors.add("global", new ActionMessage("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
            } catch (Exception e) {
                errors.add("global", new ActionMessage("error.import.exception", e.getMessage()));
            }
        }
        
        if (importFile == null) {
        	// bug-fix clean up csvData
			importWizardHelper.setFileData(null);
			csvFileName = null;
			attachmentCsvFileID = 0;
		} else if (checkAllowedImportFileSize(AgnUtils.getCompanyID(request), importFile, errors)) {
			try {
				importWizardHelper.setFileData(importFile.getFileData());
                csvFileName = importFile.getFileName();
			} catch (FileNotFoundException e) {
				logger.error("Error occured: " + e.getMessage(), e);
			} catch (IOException e) {
				logger.error("Error occured: " + e.getMessage(), e);
			}
		}
	}

	private boolean checkAllowedImportFileSize(int companyID, FormFile csvFileToCheck, ActionMessages errors) {
		int maxSizeAllowedForClassicImport = getConfigService().getIntegerValue(ConfigValue.ClassicImportMaxFileSize, companyID);
		if (maxSizeAllowedForClassicImport >= 0) {
            try {
                int csvFileSize = csvFileToCheck.getFileSize();
                if (csvFileSize > maxSizeAllowedForClassicImport) {
                    errors.add("global", new ActionMessage("error.import.maximum_filesize_exceeded", AgnUtils.getHumanReadableNumber(maxSizeAllowedForClassicImport, "Byte", false)));
                    return false;
                }
            } catch (Exception e) {
                errors.add("global", new ActionMessage("error.import.exception", e.getMessage()));
                return false;
            }
		}

		int maxRowsAllowedForClassicImport = getConfigService().getIntegerValue(ConfigValue.ClassicImportMaxRows, companyID);
		if (maxRowsAllowedForClassicImport >= 0) {
			// Also there might be an error within the csv structure (escaped linebreaks in CSV), the linebreaks should be counted as csv entries.
			// This is not fully correct, but allows the user to ignore some invalid csv lines later on in the GUI.
			try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(csvFileToCheck.getInputStream()))) {
				while ((lineNumberReader.readLine()) != null) {
					// Just read through the data to count the number of lines
				}
				
				if (lineNumberReader.getLineNumber() > maxRowsAllowedForClassicImport) {
					errors.add("global", new ActionMessage("error.import.maxlinesexceeded", lineNumberReader.getLineNumber(), maxRowsAllowedForClassicImport));
					return false;
				}
			} catch (Exception e) {
				errors.add("global", new ActionMessage("error.import.exception", e.getMessage()));
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Getter for property csvAllColumns.
	 * 
	 * @return Value of property csvAllColumns.
	 */
	public ArrayList<CsvColInfo> getCsvAllColumns() {
		return importWizardHelper.getCsvAllColumns();
	}

	/**
	 * Setter for property csvAllColumns.
	 * 
	 * @param csvAllColumns
	 *            New value of property csvAllColumns.
	 */
	public void setCsvAllColumns(ArrayList<CsvColInfo> csvAllColumns) {
		importWizardHelper.setCsvAllColumns(csvAllColumns );
	}

	/**
	 * Getter for property mailingLists.
	 * 
	 * @return Value of property mailingLists.
	 * 
	 */
	public Vector<String> getMailingLists() {
		return importWizardHelper.getMailingLists();
	}

	/**
	 * Setter for property mailingLists.
	 * 
	 * @param mailingLists
	 *            New value of property mailingLists.
	 */
	public void setMailingLists(Vector<String> mailingLists) {
		importWizardHelper.setMailingLists(mailingLists);
	}

	/**
	 * Getter for property usedColumns.
	 * 
	 * @return Value of property usedColumns.
	 */
	public ArrayList<String> getUsedColumns() {
		return importWizardHelper.getUsedColumns();
	}

	/**
	 * Setter for property usedColumns.
	 * 
	 * @param usedColumns
	 *            New value of property usedColumns.
	 */
	public void setUsedColumns(ArrayList<String> usedColumns) {
		importWizardHelper.setUsedColumns(usedColumns);
	}

	/**
	 * Getter for property parsedContent.
	 * 
	 * @return Value of property parsedContent.
	 */
	public LinkedList<LinkedList<Object>> getParsedContent() {
		return importWizardHelper.getParsedContent();
	}

	/**
	 * Setter for property parsedContent.
	 * 
	 * @param parsedContent
	 *            New value of property parsedContent.
	 */
	public void setParsedContent(LinkedList<LinkedList<Object>> parsedContent) {
		importWizardHelper.setParsedContent(parsedContent);
	}

	/**
	 * Getter for property emailAdresses.
	 * 
	 * @return Value of property emailAdresses.
	 */
	public Set<String> getUniqueValues() {
		return importWizardHelper.getUniqueValues();
	}

	/**
	 * Setter for property emailAdresses.
	 * 
	 * @param uniqueValues
	 */
	public void setUniqueValues(Set<String> uniqueValues) {
		importWizardHelper.setUniqueValues(uniqueValues);
	}

	/**
	 * Getter for property dbAllColumns.
	 * 
	 * @return Value of property dbAllColumns.
	 */
	public Map<String, CsvColInfo> getDbAllColumns() {
		// we use a TreeMap here because it SORTS the given map.
		// the getDBAllColumns() Map is a Case-Insensitive-Map which is unsorted.
		return new TreeMap<>(importWizardHelper.getDbAllColumns());
	}

	/**
	 * Setter for property dbAllColumns.
	 * 
	 * @param dbAllColumns
	 *            New value of property dbAllColumns.
	 */
	public void setDbAllColumns(Hashtable<String, CsvColInfo> dbAllColumns) {
		importWizardHelper.setDbAllColumns(dbAllColumns);
	}

	/**
	 * Getter for property mode.
	 * 
	 * @return Value of property mode.
	 */
	public int getMode() {
		return importWizardHelper.getMode();
	}

	/**
	 * Setter for property mode.
	 * 
	 * @param mode
	 *            New value of property mode.
	 */
	public void setMode(int mode) {
		importWizardHelper.setMode(mode);
	}

		
	/**
	 * Gets mailing lists from request.
	 */
	protected Vector<String> getMailinglistsFromRequest(HttpServletRequest req) {
		String aParam = null;
		Vector<String> mailingLists = new Vector<>();
		Enumeration<String> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			aParam = e.nextElement();
			if (aParam.startsWith("agn_mlid_")) {
				mailingLists.add(aParam.substring(9));
			}
		}
		return mailingLists;
	}

	/**
	 * Getter for property linesOK.
	 * 
	 * @return Value of property linesOK.
	 */
	public int getLinesOK() {
		return importWizardHelper.getLinesOK();
	}

	/**
	 * Setter for property linesOK.
	 * 
	 * @param linesOK
	 *            New value of property linesOK.
	 */
	public void setLinesOK(int linesOK) {
		importWizardHelper.setLinesOK(linesOK);
	}

	/**
	 * Getter for property dbInsertStatus.
	 * 
	 * @return Value of property dbInsertStatus.
	 */
	public int getDbInsertStatus() {
		return importWizardHelper.getDbInsertStatus();
	}

	/**
	 * Setter for property dbInsertStatus.
	 * 
	 * @param dbInsertStatus
	 *            New value of property dbInsertStatus.
	 */
	public void setDbInsertStatus(int dbInsertStatus) {
		 importWizardHelper.setDbInsertStatus(dbInsertStatus);
	}

	/**
	 * Getter for property parsedData.
	 * 
	 * @return Value of property parsedData.
	 */
	public StringBuffer getParsedData() {
		return importWizardHelper.getParsedData();
	}

	/**
	 * Setter for property parsedData.
	 * 
	 * @param parsedData  New value of property parsedData.
	 */
	public void setParsedData(StringBuffer parsedData) {
		importWizardHelper.setParsedData(parsedData);
	}

	/**
	 * Getter for property downloadName.
	 * 
	 * @return Value of property downloadName.
	 */
	public String getDownloadName() {
		return importWizardHelper.getDownloadName();
	}

	/**
	 * Setter for property downloadName.
	 * 
	 * @param downloadName
	 *            New value of property downloadName.
	 */
	public void setDownloadName(String downloadName) {
		importWizardHelper.setDownloadName(downloadName);
	}

	/**
	 * Getter for property dbInsertStatusMessages.
	 * 
	 * @return Value of property dbInsertStatusMessages.
	 */
	public LinkedList<String> getDbInsertStatusMessages() {
		return importWizardHelper.getDbInsertStatusMessages();
	}

    /**
     * Getter for jsp page for displaying status messages (we need to give the copy of list to avoid concurrent
     * modification problem)
     *
     * @return copy of dbInsertStatusMessages list
     */
    public LinkedList<String> getDbInsertStatusMessagesCopy() {
		return new LinkedList<>(importWizardHelper.getDbInsertStatusMessages());
	}

	/**
	 * Setter for property dbInsertStatusMessages.
	 * 
	 * @param dbInsertStatusMessages
	 *            New value of property dbInsertStatusMessages.
	 */
	public void setDbInsertStatusMessages(LinkedList<String> dbInsertStatusMessages) {
		 importWizardHelper.setDbInsertStatusMessages(dbInsertStatusMessages);
	}

	public void addDbInsertStatusMessage(String message) {
		 importWizardHelper.getDbInsertStatusMessages().add(message);
	}

	/**
	 * Getter for property resultMailingListAdded.
	 * 
	 * @return Value of property resultMailingListAdded.
	 */
	public Map<String, String> getResultMailingListAdded() {
		return importWizardHelper.getResultMailingListAdded();
	}

	/**
	 * Setter for property resultMailingListAdded.
	 * 
	 * @param resultMailingListAdded
	 *            New value of property resultMailingListAdded.
	 */
	public void setResultMailingListAdded(Map<String, String> resultMailingListAdded) {
		importWizardHelper.setResultMailingListAdded(resultMailingListAdded);
	}

	/**
	 * Getter for property previewOffset.
	 * 
	 * @return Value of property previewOffset.
	 */
	public int getPreviewOffset() {
		return importWizardHelper.getPreviewOffset();
	}

	/**
	 * Setter for property previewOffset.
	 * 
	 * @param previewOffset  New value of property previewOffset.
	 */
	public void setPreviewOffset(int previewOffset) {
		importWizardHelper.setPreviewOffset(previewOffset);
	}

	/**
	 * Getter for property dateFormat.
	 * 
	 * @return Value of property dateFormat.
	 */
	public String getDateFormat() {
		return importWizardHelper.getDateFormat();
	}

	/**
	 * Setter for property dateFormat.
	 * 
	 * @param dateFormat New value of property dateFormat.
	 */
	public void setDateFormat(String dateFormat) {
		importWizardHelper.setDateFormat(dateFormat);
	}
	
	public Map<String, CsvColInfo> getColumnMapping() {
		return importWizardHelper.getColumnMapping();
	}

	/**
	 * Setter for property columnMapping.
	 * 
	 * @param columnMapping
	 *            New value of property columnMapping.
	 */
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
	 * @return
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
	protected boolean isParameterExcludedForUnsafeHtmlTagCheck( String parameterName, HttpServletRequest request) {
		return parameterName.equals( "dummy");
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

	public boolean isColumnDuplicate(Map<String, String> mapParametersOnlyMap) {
        Set<String> dbColumnSet = new HashSet<>();
        for (Entry<String, String> entry : mapParametersOnlyMap.entrySet()) {
            if (entry.getKey().startsWith("map_")) {
                String value = entry.getValue();
                if (!"NOOP".equals(value) && dbColumnSet.contains(value)) {
                    return true;
                }
                dbColumnSet.add(value);
            }
        }
        return false;
    }

    public FormFile getFormFileByUploadId(int uploadID, String mime) throws Exception {
    	ComUploadDao uploadDao = (ComUploadDao) getWebApplicationContext().getBean("UploadDao");
        DownloadData downloadData = uploadDao.getDownloadData(uploadID);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            uploadDao.sendDataToStream(uploadID, os);

            final String fileName = downloadData.getFilename();
            final byte[] fileData = os.toByteArray();
            final Integer fileSize = downloadData.getFilesize();
            final String mimeType = mime;

            return new FormFile() {
                @Override
                public void destroy() {
        			// nothing to do
                }

                @Override
                public String getContentType() {
                    return mimeType;
                }

                @Override
                public byte[] getFileData() throws FileNotFoundException, IOException {
                    return fileData;
                }

                @Override
                public String getFileName() {
                    return fileName;
                }

                @Override
                public int getFileSize() {
                    return fileSize;
                }

                @Override
                public InputStream getInputStream() throws FileNotFoundException, IOException {
                    return new ByteArrayInputStream(fileData);
                }

                @Override
                public void setContentType(String contentType) {
        			// nothing to do
                }

                @Override
                public void setFileName(String fileNameUnused) {
        			// nothing to do
                }

                @Override
                public void setFileSize(int fileSizeUnused) {
        			// nothing to do
                }
            };
        } catch (Exception e) {
            throw e;
        }
    }

}
