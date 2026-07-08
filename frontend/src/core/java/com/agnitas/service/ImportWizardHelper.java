/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.agnitas.beans.ImportStatus;
import com.agnitas.emm.core.commons.dto.FileDto;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.messages.Message;
import com.agnitas.util.Blacklist;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.ImportUtils.ImportErrorType;

public interface ImportWizardHelper {

    String MAILTYPE_KEY = "mailtype";
    String GENDER_KEY = "gender";

	/**
	 * Getter for property datasourceID.
	 * 
	 * @return Value of property datasourceID.
	 */
	int getDatasourceID();

	/**
	 * Sets an error.
	 */
	void setError(ImportErrorType id, String desc);

	/**
	 * Getter for property error.
	 * 
	 * @return Value of property error.
	 */
	StringBuffer getError(ImportErrorType id);

	/**
	 * Getter for property status.
	 * 
	 * @return Value of property status.
	 */
	ImportStatus getStatus();

	/**
	 * Setter for property charset.
	 * 
	 * @param status
	 *            New value of property status.
	 */
	void setStatus(ImportStatus status);

	/**
	 * Getter for property csvAllColumns.
	 * 
	 * @return Value of property csvAllColumns.
	 */
	ArrayList<CsvColInfo> getCsvAllColumns();

	/**
	 * Setter for property csvAllColumns.
	 * 
	 * @param csvAllColumns
	 *            New value of property csvAllColumns.
	 */
	void setCsvAllColumns(ArrayList<CsvColInfo> csvAllColumns);

	/**
	 * Getter for property mailingLists.
	 * 
	 * @return Value of property mailingLists.
	 * 
	 */
	Vector<String> getMailingLists();

	/**
	 * Setter for property mailingLists.
	 * 
	 * @param mailingLists
	 *            New value of property mailingLists.
	 */
	void setMailingLists(Vector<String> mailingLists);

    List<Integer> getMailinglists();

    void setMailinglists(List<Integer> mailinglists);

    /**
	 * Getter for property usedColumns.
	 * 
	 * @return Value of property usedColumns.
	 */
	ArrayList<String> getUsedColumns();

	/**
	 * Setter for property usedColumns.
	 * 
	 * @param usedColumns
	 *            New value of property usedColumns.
	 */
	void setUsedColumns(ArrayList<String> usedColumns);

	/**
	 * Getter for property parsedContent.
	 * 
	 * @return Value of property parsedContent.
	 */
	LinkedList<LinkedList<Object>> getParsedContent();

	/**
	 * Setter for property parsedContent.
	 * 
	 * @param parsedContent
	 *            New value of property parsedContent.
	 */
	void setParsedContent(LinkedList<LinkedList<Object>> parsedContent);

	/**
	 * Getter for property emailAdresses.
	 * 
	 * @return Value of property emailAdresses.
	 */
	Set<String> getUniqueValues();

	/**
	 * Setter for property emailAdresses.
	 * 
	 * @param uniqueValues
	 */
	void setUniqueValues(Set<String> uniqueValues);

	/**
	 * Getter for property dbAllColumns.
	 * 
	 * @return Value of property dbAllColumns.
	 */
	Map<String, CsvColInfo> getDbAllColumns();

	/**
	 * Setter for property dbAllColumns.
	 * 
	 * @param dbAllColumns
	 *            New value of property dbAllColumns.
	 */
	void setDbAllColumns(Map<String, CsvColInfo> dbAllColumns);

	/**
	 * Getter for property mode.
	 * 
	 * @return Value of property mode.
	 */
	int getMode();

	/**
	 * Setter for property mode.
	 * 
	 * @param mode
	 *            New value of property mode.
	 */
	void setMode(int mode);

	/**
	 * Creates a simple date format When mapping for a column is found get real
	 * csv column information Checks email / email adress / email adress on
	 * blacklist. ?????
	 */
	LinkedList<Object> parseLine(List<String> inputData);

	void setCsvMaxUsedColumn(int csvMaxUsedColumn);

	void setBlacklistHelper(Blacklist blacklistHelper);

	Blacklist getBlacklistHelper();

	LinkedList<Object> parseLine(List<String> inputData, boolean addErrors);

	/**
	 * Maps columns from database.
	 * Side effects:
	 * -columnMapping will be initialized
	 * -corresponding columns in dbAllColumns will be activated
	 * -csvAllColumns will be updated too
	 */
	void mapColumns(Map<String, String> mapParameters);

	/**
	 * Getter for property linesOK.
	 * 
	 * @return Value of property linesOK.
	 */
	int getLinesOK();

	/**
	 * Setter for property linesOK.
	 * 
	 * @param linesOK
	 *            New value of property linesOK.
	 */
	void setLinesOK(int linesOK);

	/**
	 * Getter for property dbInsertStatus.
	 * 
	 * @return Value of property dbInsertStatus.
	 */
	int getDbInsertStatus();

	/**
	 * Setter for property dbInsertStatus.
	 * 
	 * @param dbInsertStatus
	 *            New value of property dbInsertStatus.
	 */
	void setDbInsertStatus(int dbInsertStatus);

	/**
	 * Getter for property parsedData.
	 * 
	 * @return Value of property parsedData.
	 */
	StringBuffer getParsedData();

	/**
	 * Setter for property parsedData.
	 * 
	 * @param parsedData
	 *            New value of property parsedData.
	 */
	void setParsedData(StringBuffer parsedData);

	/**
	 * Getter for property downloadName.
	 * 
	 * @return Value of property downloadName.
	 */
	String getDownloadName();

	/**
	 * Setter for property downloadName.
	 * 
	 * @param downloadName
	 *            New value of property downloadName.
	 */
	void setDownloadName(String downloadName);

	/**
	 * Getter for property dbInsertStatusMessagesAndParameters.
	 * 
	 * @return Value of property dbInsertStatusMessagesAndParameters.
	 */
	List<Message> getDbInsertStatusMessagesAndParameters();

	void addDbInsertStatusMessageAndParameters(String messageKey, Object... additionalParameters);

	/**
	 * Getter for property resultMailingListAdded.
	 * 
	 * @return Value of property resultMailingListAdded.
	 */
	Map<MediaTypes, Map<String, String>> getResultMailingListAdded();

	/**
	 * Setter for property resultMailingListAdded.
	 * 
	 * @param resultMailingListAdded
	 *            New value of property resultMailingListAdded.
	 */
	void setResultMailingListAdded(Map<MediaTypes, Map<String, String>> resultMailingListAdded);

	/**
	 * Getter for property previewOffset.
	 * 
	 * @return Value of property previewOffset.
	 */
	int getPreviewOffset();

	/**
	 * Setter for property previewOffset.
	 * 
	 * @param previewOffset
	 *            New value of property previewOffset.
	 */
	void setPreviewOffset(int previewOffset);

	/**
	 * Getter for property dateFormat.
	 * 
	 * @return Value of property dateFormat.
	 */
	String getDateFormat();

	/**
	 * Setter for property dateFormat.
	 * 
	 * @param dateFormat
	 *            New value of property dateFormat.
	 */
	void setDateFormat(String dateFormat);

	/**
	 * Getter for property columnMapping.
	 * 
	 * @return Value of property columnMapping.
	 */
	Map<String, CsvColInfo> getColumnMapping();

	/**
	 * Setter for property columnMapping.
	 * 
	 * @param columnMapping   New value of property columnMapping.
	 */
	void setColumnMapping(Map<String, CsvColInfo> columnMapping);

	String getErrorId();

	void setErrorId(String errorId);

	String getManualAssignedMailingType();

	void setManualAssignedMailingType(String manualAssignedMailingType);

	String getManualAssignedGender();

	void setManualAssignedGender(String manualAssignedGender);

	boolean isMailingTypeMissing();

	void setMailingTypeMissing(boolean mailingTypeMissing);

	boolean isGenderMissing();

	void setGenderMissing(boolean genderMissing);

	Locale getLocale();

	void setLocale(Locale locale);

	Map<ImportErrorType, StringBuffer> getErrorData();

	void setErrorData(Map<ImportErrorType, StringBuffer> errorData);

	byte[] getFileData();

	void setFileData(byte[] fileData);

	int getCompanyID();

	void setCompanyID(int companyID);

    void clearDummyColumnsMappings();

    String getKeyColumn();
    
    FileDto getFile();

    void setFile(FileDto file);

	void clearDbInsertStatusMessagesAndParameters();

}
