/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.agnitas.emm.core.commons.dto.FileDto;
import org.agnitas.beans.ImportStatus;
import org.agnitas.util.Blacklist;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.struts.action.ActionMessage;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface ImportWizardHelper {

	/**
	 * Getter for property datasourceID.
	 * 
	 * @return Value of property datasourceID.
	 */
	public abstract int getDatasourceID();

	/**
	 * Sets an error.
	 */
	public abstract void setError(ImportErrorType id, String desc);

	/**
	 * Getter for property error.
	 * 
	 * @return Value of property error.
	 */
	public abstract StringBuffer getError(ImportErrorType id);

	/**
	 * Getter for property status.
	 * 
	 * @return Value of property status.
	 */
	public abstract ImportStatus getStatus();

	/**
	 * Setter for property charset.
	 * 
	 * @param status
	 *            New value of property status.
	 */
	public abstract void setStatus(ImportStatus status);

	/**
	 * Getter for property csvAllColumns.
	 * 
	 * @return Value of property csvAllColumns.
	 */
	public abstract ArrayList<CsvColInfo> getCsvAllColumns();

	/**
	 * Setter for property csvAllColumns.
	 * 
	 * @param csvAllColumns
	 *            New value of property csvAllColumns.
	 */
	public abstract void setCsvAllColumns(ArrayList<CsvColInfo> csvAllColumns);

	/**
	 * Getter for property mailingLists.
	 * 
	 * @return Value of property mailingLists.
	 * 
	 */
	public abstract Vector<String> getMailingLists();

	/**
	 * Setter for property mailingLists.
	 * 
	 * @param mailingLists
	 *            New value of property mailingLists.
	 */
	public abstract void setMailingLists(Vector<String> mailingLists);

    List<Integer> getMailinglists();

    void setMailinglists(List<Integer> mailinglists);

    /**
	 * Getter for property usedColumns.
	 * 
	 * @return Value of property usedColumns.
	 */
	public abstract ArrayList<String> getUsedColumns();

	/**
	 * Setter for property usedColumns.
	 * 
	 * @param usedColumns
	 *            New value of property usedColumns.
	 */
	public abstract void setUsedColumns(ArrayList<String> usedColumns);

	/**
	 * Getter for property parsedContent.
	 * 
	 * @return Value of property parsedContent.
	 */
	public abstract LinkedList<LinkedList<Object>> getParsedContent();

	/**
	 * Setter for property parsedContent.
	 * 
	 * @param parsedContent
	 *            New value of property parsedContent.
	 */
	public abstract void setParsedContent(LinkedList<LinkedList<Object>> parsedContent);

	/**
	 * Getter for property emailAdresses.
	 * 
	 * @return Value of property emailAdresses.
	 */
	public abstract Set<String> getUniqueValues();

	/**
	 * Setter for property emailAdresses.
	 * 
	 * @param uniqueValues
	 */
	public abstract void setUniqueValues(Set<String> uniqueValues);

	/**
	 * Getter for property dbAllColumns.
	 * 
	 * @return Value of property dbAllColumns.
	 */
	public abstract Map<String, CsvColInfo> getDbAllColumns();

	/**
	 * Setter for property dbAllColumns.
	 * 
	 * @param dbAllColumns
	 *            New value of property dbAllColumns.
	 */
	public abstract void setDbAllColumns(Map<String, CsvColInfo> dbAllColumns);

	/**
	 * Getter for property mode.
	 * 
	 * @return Value of property mode.
	 */
	public abstract int getMode();

	/**
	 * Setter for property mode.
	 * 
	 * @param mode
	 *            New value of property mode.
	 */
	public abstract void setMode(int mode);

	/**
	 * Creates a simple date format When mapping for a column is found get real
	 * csv column information Checks email / email adress / email adress on
	 * blacklist. ?????
	 */
	public abstract LinkedList<Object> parseLine(List<String> inputData);

	void setCsvMaxUsedColumn(int csvMaxUsedColumn);

	void setBlacklistHelper(Blacklist blacklistHelper);

	Blacklist getBlacklistHelper();

	void setReadlines(int readlines);

	public LinkedList<Object> parseLine(List<String> inputData, boolean addErrors);

	/**
	 * Maps columns from database.
	 * Side effects:
	 * -columnMapping will be initialized
	 * -corresponding columns in dbAllColumns will be activated
	 * -csvAllColumns will be updated too
	 */
	public abstract void mapColumns(Map<String, String> mapParameters);

	/**
	 * Getter for property linesOK.
	 * 
	 * @return Value of property linesOK.
	 */
	public abstract int getLinesOK();

	/**
	 * Setter for property linesOK.
	 * 
	 * @param linesOK
	 *            New value of property linesOK.
	 */
	public abstract void setLinesOK(int linesOK);

	/**
	 * Getter for property dbInsertStatus.
	 * 
	 * @return Value of property dbInsertStatus.
	 */
	public abstract int getDbInsertStatus();

	/**
	 * Setter for property dbInsertStatus.
	 * 
	 * @param dbInsertStatus
	 *            New value of property dbInsertStatus.
	 */
	public abstract void setDbInsertStatus(int dbInsertStatus);

	/**
	 * Getter for property parsedData.
	 * 
	 * @return Value of property parsedData.
	 */
	public abstract StringBuffer getParsedData();

	/**
	 * Setter for property parsedData.
	 * 
	 * @param parsedData
	 *            New value of property parsedData.
	 */
	public abstract void setParsedData(StringBuffer parsedData);

	/**
	 * Getter for property downloadName.
	 * 
	 * @return Value of property downloadName.
	 */
	public abstract String getDownloadName();

	/**
	 * Setter for property downloadName.
	 * 
	 * @param downloadName
	 *            New value of property downloadName.
	 */
	public abstract void setDownloadName(String downloadName);

	/**
	 * Getter for property dbInsertStatusMessagesAndParameters.
	 * 
	 * @return Value of property dbInsertStatusMessagesAndParameters.
	 */
	public abstract List<ActionMessage> getDbInsertStatusMessagesAndParameters();

	public abstract void addDbInsertStatusMessageAndParameters(String messageKey, Object... additionalParameters);

	/**
	 * Getter for property resultMailingListAdded.
	 * 
	 * @return Value of property resultMailingListAdded.
	 */
	public abstract Map<MediaTypes, Map<String, String>> getResultMailingListAdded();

	/**
	 * Setter for property resultMailingListAdded.
	 * 
	 * @param resultMailingListAdded
	 *            New value of property resultMailingListAdded.
	 */
	public abstract void setResultMailingListAdded(Map<MediaTypes, Map<String, String>> resultMailingListAdded);

//	/**
//	 * Getter for property blacklist.
//	 *
//	 * @return Value of property blacklist.
//	 */
//	public abstract HashSet getBlacklist();
//
//	/**
//	 * Setter for property blacklist.
//	 *
//	 * @param blacklist
//	 *            New value of property blacklist.
//	 */
//	public abstract void setBlacklist(HashSet blacklist);

	/**
	 * Getter for property previewOffset.
	 * 
	 * @return Value of property previewOffset.
	 */
	public abstract int getPreviewOffset();

	/**
	 * Setter for property previewOffset.
	 * 
	 * @param previewOffset
	 *            New value of property previewOffset.
	 */
	public abstract void setPreviewOffset(int previewOffset);

	/**
	 * Getter for property dateFormat.
	 * 
	 * @return Value of property dateFormat.
	 */
	public abstract String getDateFormat();

	/**
	 * Setter for property dateFormat.
	 * 
	 * @param dateFormat
	 *            New value of property dateFormat.
	 */
	public abstract void setDateFormat(String dateFormat);

	/**
	 * Getter for property columnMapping.
	 * 
	 * @return Value of property columnMapping.
	 */
	public abstract Map<String, CsvColInfo> getColumnMapping();

	/**
	 * Setter for property columnMapping.
	 * 
	 * @param columnMapping   New value of property columnMapping.
	 */
	public abstract void setColumnMapping(Map<String, CsvColInfo> columnMapping);

	public abstract String getErrorId();

	public abstract void setErrorId(String errorId);

	public abstract String getManualAssignedMailingType();

	public abstract void setManualAssignedMailingType(String manualAssignedMailingType);

	public abstract String getManualAssignedGender();

	public abstract void setManualAssignedGender(String manualAssignedGender);

	public abstract boolean isMailingTypeMissing();

	public abstract void setMailingTypeMissing(boolean mailingTypeMissing);

	public abstract boolean isGenderMissing();

	public abstract void setGenderMissing(boolean genderMissing);

	public abstract int getReadlines();

	/**
	 * read all lines of the file
	 * @return
	 * @throws IOException
	 */
	public abstract int getLinesOKFromFile() throws Exception;

	public abstract Locale getLocale();

	public abstract void setLocale(Locale locale);

	public abstract Map<ImportErrorType, StringBuffer> getErrorData();

	public abstract void setErrorData(Map<ImportErrorType, StringBuffer> errorData);

	public abstract byte[] getFileData();

	public abstract void setFileData(byte[] fileData);

	public abstract int getCompanyID();

	public abstract void setCompanyID(int companyID);

    public void clearDummyColumnsMappings();

    public abstract String getKeyColumn();
    
    FileDto getFile();

    void setFile(FileDto file);

	void clearDbInsertStatusMessagesAndParameters();
}
