/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.service.GenericImportException.ReasonCode;
import org.agnitas.util.ImportUtils.ImportErrorType;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface ImportStatus {
    public static final int DOUBLECHECK_FULL = 0;

    public static final int DOUBLECHECK_CSV = 1;

    public static final int DOUBLECHECK_NONE = 2;

    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    void setId(int id);

   /**
    * Setter for property companyID.
    *
    * @param company New value of property companyID.
    */
    void setCompanyID( int company);

    /**
    * Setter for property adminID.
    *
    * @param admin New value of property adminID.
    */
    void setAdminID(int admin);
    
    /**
    * Setter for property datasourceID.
    *
    * @param datasource New value of property datasourceID.
    */
    void setDatasourceID(int datasource);

    /**
    * Setter for property mode.
    *
    * @param mode New value of property mode.
    */
    void setMode(int mode);

    /**
    * Setter for property doubleCheck.
    *
    * @param doubleCheck New value of property doubleCheck.
    */
    void setDoubleCheck(int doubleCheck);

    /**
    * Setter for property ignoreNull.
    *
    * @param ignoreNull New value of property ignoreNull.
    */
    void setIgnoreNull(int ignoreNull);

    /**
    * Setter for property separator.
    *
    * @param separator New value of property separator.
    */
    void setSeparator(char separator);
    
    /**
    * Setter for property delimiter.
    *
    * @param delimiter New value of property delimiter.
    */
    void setDelimiter(String delimiter);

    /**
    * Setter for property keycolumn.
    *
    * @param keycolumn New value of property keycolumn.
    */
    void setKeycolumn(String keycolumn);

    /**
    * Setter for property charset.
    *
    * @param charset New value of property charset.
    */
    void setCharset(String charset);

    /**
    * Setter for property recordsBefore.
    *
    * @param recordsBefore New value of property recordsBefore.
    */
    void setRecordsBefore(int recordsBefore);
    
    void setFields(int fields);

    /**
    * Setter for property inserted.
    *
    * @param inserted New value of property inserted.
    */
    void setInserted(int inserted);

    /**
    * Setter for property updated.
    *
    * @param updated New value of property updated.
    */
    void setUpdated(int updated);
    
    /**
     * Setter for property errors.
     *
     * @param errors New value of property errors.
     */
    void setErrors(Map<ImportErrorType, Integer> errors);

    /**
     * Setter for property error.
     *
     * @param id New value of property id.
     * @param value New value of property value.
     */
    void setError(ImportErrorType id, Integer value);

    /**
     * Getter for property id.
     * 
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    int getCompanyID();
    
    /**
     * Getter for property adminID.
     * 
     * @return Value of property adminID.
     */
    int getAdminID();

    /**
     * Getter for property datasourceID.
     * 
     * @return Value of property datasourceID.
     */
    int getDatasourceID();

    /**
     * Getter for property mode.
     * 
     * @return Value of property mode.
     */
    int getMode();

    /**
     * Getter for property doubleCheck.
     * 
     * @return Value of property doubleCheck.
     */
    int getDoubleCheck();

    /**
     * Getter for property ignoreNull.
     * 
     * @return Value of property ignoreNull.
     */
    int getIgnoreNull();

    /**
     * Getter for property separator.
     * 
     * @return Value of property separator.
     */
    char getSeparator();

    /**
     * Getter for property delimiter.
     * 
     * @return Value of property delimiter.
     */
    String getDelimiter();
    
    /**
     * Getter for property keycolumn.
     * 
     * @return Value of property keycolumn.
     */
    String getKeycolumn();

    /**
     * Getter for property charset.
     * 
     * @return Value of property charset.
     */
    String getCharset();

    /**
     * Getter for property recordsBefore.
     * 
     * @return Value of property recordsBefore.
     */
    int getRecordsBefore();
    
    int getFields();

    /**
     * Getter for property inserted.
     * 
     * @return Value of property inserted.
     */
    int getInserted();

    /**
     * Getter for property updated.
     * 
     * @return Value of property updated.
     */
    int getUpdated();

    /**
     * Getter for property errors.
     * 
     * @return Value of property errors.
     */
    Map<ImportErrorType, Integer> getErrors();

    /**
     * Getter for property error.
     * 
     * @param id ID of Error.
     * @return Value of property error.
     */
    Object getError(ImportErrorType importErrorType);

	/**
	 * Getter Method used only within JSPs
	 * @throws Exception
	 */
	Object getError(String idString);

    /**
     * Adds an error with id.
     * 
     * @param id ID of the error.
     */
    void addError(ImportErrorType importErrorType);

    int getAlreadyInDb();

    void setAlreadyInDb(int alreadyInDb);

	Date getChangeDate();

	void setChangeDate(Date changeDate);

	Date getCreationDate();

	void setCreationDate(Date creationDate);

	String getFatalError();

	void setFatalError(String fatalError);

	void addError(ImportErrorType importErrorType, int numberOfErrors);

	int getCsvLines();

	void setCsvLines(int csvLines);
	
	int getBlacklisted();

	void setBlacklisted(int emailsMarkedAsBlacklisted);

	int getInvalidNullValues();

	void setInvalidNullValues(int invalidNullValueEntries);

	long getFileSize();

	void setFileSize(long fileSize);

	void setImportedRecipientsCsv(File file);

	File getImportedRecipientsCsv();

	void setInvalidRecipientsCsv(File file);

	File getInvalidRecipientsCsv();

	void setFixedByUserRecipientsCsv(File file);

	File getFixedByUserRecipientsCsv();

	void setDuplicateInCsvOrDbRecipientsCsv(File file);

	File getDuplicateInCsvOrDbRecipientsCsv();

	void addErrorColumn(String columnName);
	
	Set<String> getErrorColumns();

	Map<MediaTypes, Map<Integer, Integer>> getMailinglistStatistics();

	void setMailinglistStatistics(Map<MediaTypes, Map<Integer, Integer>> mailinglistStatistics);
	
	void setNearLimit(boolean nearLimit);

	boolean isNearLimit();

	List<Integer> getFirstErrorLineNumbers();

	void addToFirstErrors(int lineNumber, ReasonCode importErrorType, String columnName);

	List<String> getFirstErrorTypes();

	List<String> getFirstErrorColumns();

	void clearFirstErrors();

	void setDeletedEntries(int deletedEntries);

	int getDeletedEntries();
}
