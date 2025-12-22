/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

/**
 * Bean containing data of import profile (import profile contains recipient
 * import information that can be reused during several imports)
 */
public interface ImportProfile {

    /**
     * Getter for property id (id of import profile in database)
     *
     * @return value of property id for this ImportProfile
     */
	int getId();

    /**
     * Setter for property id
     *
     * @param id the new value for the id
     */
	void setId(int id);

    /**
     * Getter for property adminId
     *
     * @return value of property for adminId this ImportProfile
     */
	int getAdminId();

    /**
     * Setter for property adminId
     *
     * @param adminId the new value for the adminId
     */
	void setAdminId(int adminId);

    /**
     * Getter for property companyId
     *
     * @return value of property companyId for this ImportProfile
     */
	int getCompanyId();

    /**
     * Setter for property companyId
     *
     * @param companyId the new value for the companyId
     */
	void setCompanyId( int companyId);

    /**
     * Getter for property name
     *
     * @return value of property name for this ImportProfile
     */
	String getName();

    /**
     * Setter for property name
     *
     * @param name the new value for the name
     */
	void setName(String name);

    /**
     * Getter for property separator (character that separates columns in
     * csv-file)
     * See possible values in {@link com.agnitas.util.importvalues.Separator}
     *
     * @return value of property separator for this ImportProfile
     */
	int getSeparator();

    /**
     * Setter for property separator
     * See possible values in {@link com.agnitas.util.importvalues.Separator}
     *
     * @param separator the new value for the separator
     */
	void setSeparator(int separator);

    /**
     * Getter for property textRecognitionChar (character that is used to wrap
     * text values in csv-file)
     * See possible values in {@link com.agnitas.util.importvalues.TextRecognitionChar}
     *
     * @return value of property textRecognitionChar for this ImportProfile
     */
	int getTextRecognitionChar();

    /**
     * Setter for property textRecognitionChar
     * See possible values in {@link com.agnitas.util.importvalues.TextRecognitionChar}
     *
     * @param textRecognitionChar the new value for the textRecognitionChar
     */
	void setTextRecognitionChar(int textRecognitionChar);

    /**
     * Getter for property charset (the character-set of csv-file)
     * See possible values in {@link com.agnitas.util.importvalues.Charset}
     *
     * @return value of property charset for this ImportProfile
     */
	int getCharset();

    /**
     * Setter for property charset
     * See possible values in {@link com.agnitas.util.importvalues.Charset}
     *
     * @param charset the new value for the charset
     */
	void setCharset(int charset);

    /**
     * Getter for property dateFormat (the format of date columns that will
     * be used when parsing csv-file)
     * See possible values in {@link com.agnitas.util.importvalues.DateFormat}
     *
     * @return value of property dateFormat for this ImportProfile
     */
	int getDateFormat();

    /**
     * Setter for property dateFormat
     * See possible values in {@link com.agnitas.util.importvalues.DateFormat}
     *
     * @param dateFormat the new value for the dateFormat
     */
	void setDateFormat(int dateFormat);

    /**
     * Getter for property importMode
     * See possible values in {@link com.agnitas.util.importvalues.ImportMode}
     *
     * @return value of property importMode for this ImportProfile
     */
	int getImportMode();

    /**
     * Setter for property importMode
     * See possible values in {@link com.agnitas.util.importvalues.ImportMode}
     *
     * @param importMode the new value for the importMode
     */
	void setImportMode(int importMode);

    /**
     * Getter for property checkForDuplicates
     * See possible values in {@link com.agnitas.util.importvalues.CheckForDuplicates}
     *
     * @return value of property checkForDuplicates for this ImportProfile
     */
	int getCheckForDuplicates();

    /**
     * Setter for property checkForDuplicates
     * See possible values in {@link com.agnitas.util.importvalues.CheckForDuplicates}
     *
     * @param checkForDuplicates the new value for the checkForDuplicates
     */
	void setCheckForDuplicates(int checkForDuplicates);

    /**
     * Getter for property nullValuesAction (action that will be performed to
     * null values)
     * See possible values in {@link com.agnitas.util.importvalues.NullValuesAction}
     *
     * @return value of property nullValuesAction for this ImportProfile
     */
	int getNullValuesAction();

    /**
     * Setter for property nullValuesAction
     * See possible values in {@link com.agnitas.util.importvalues.NullValuesAction}
     *
     * @param nullValuesAction the new value for the nullValuesAction
     */
	void setNullValuesAction(int nullValuesAction);

    /**
     * Setter for property keyColumn
     *
     * @param keyColumn the new value for the keyColumn
     */
	void setKeyColumn(String keyColumn);

    /**
     * Gets gender mappings for current ImportProfile (the binding between
     * text-gender and integer-gender)
     *
     * @return gender mappings for current ImportProfile
     */
	Map<String, Integer> getGenderMapping();
    
    /**
     * Get Gendermappings for display purpose
     * 
     * @return
     */
    Map<String, Integer> getGenderMappingJoined();

    /**
     * Sets gender mappings for current ImportProfile
     *
     * @param genderMapping gender mappings for the current ImportProfile
     */
    void setGenderMapping(Map<String, Integer> genderMapping);

    /**
     * Gets list of column mapping for the current ImportProfile
     *
     * @return column mapping for this ImportProfile
     */
    List<ColumnMapping> getColumnMapping();

    /**
     * Sets the list of column mapping
     *
     * @param columnMapping the new value for the column mapping list
     */
    void setColumnMapping(List<ColumnMapping> columnMapping);

    /**
     * Getter for property mailForReport (the report will be sent to this
     * e-mail when recipient import is finished)
     *
     * @return value of property mailForReport for this ImportProfile
     */
    String getMailForReport();

    /**
     * Setter for property mailForReport
     *
     * @param mailForReport the new value for the mailForReport
     */
    void setMailForReport(String mailForReport);

    /**
     * Getter for property defaultMailType (maill type that will be set to
     * recipients if there is no column mapped to mailtype db column)
     *
     * @return value of property defaultMailType for this ImportProfile
     */
    int getDefaultMailType();

    /**
     * Setter for property defaultMailType
     *
     * @param defaultMailType new defaultMailType value
     */
    void setDefaultMailType(int defaultMailType);

    /**
     * Get integer value of geder field in DB
     *
     * @param fieldValue string value from csv file
     * @return integer value of gender
     */
    Integer getGenderValueByFieldValue(String fieldValue);

    /**
     *
     * @return
     */
    boolean getUpdateAllDuplicates();

    /**
     * Setter for property updateAllDuplicates
     * @param updateAllDuplicates
     */
    void setUpdateAllDuplicates(boolean updateAllDuplicates);

	int getImportId();

	void setImportId(int importId);

    List<String> getKeyColumns();

    void setKeyColumns(List<String> keyColumns);

    String getFirstKeyColumn();

    void setFirstKeyColumn(String keyColumn);

    boolean keyColumnsContainsCustomerId();
    
	int getImportProcessActionID();

	void setImportProcessActionID(int importProcessActionID);

	void setDecimalSeparator(char c);

	char getDecimalSeparator();

	void setActionForNewRecipients(int actionForNewRecipients);

	int getActionForNewRecipients();

	boolean isNoHeaders();

	void setNoHeaders(boolean noHeaders);

	String getZipPassword();

	void setZipPassword(String zipPassword);

	ColumnMapping getMappingByDbColumn(String string);

	String getMailForError();

	void setMailForError(String mailForError);

	boolean isAutoMapping();

	void setAutoMapping(boolean autoMapping);

	List<Integer> getMailinglistIds();

	void setMailinglists(List<Integer> mailinglists);

	Set<MediaTypes> getMediatypes();
	
	void setMediatypes(Set<MediaTypes> mediatype);
	
	String getDatatype();

	void setDatatype(String datatype);

	void setMailinglistsAll(boolean allMailinglists);
	
	boolean isMailinglistsAll();

    Map<Integer, String> getGenderMappingsToSave();

    void setGenderMappingsToSave(Map<Integer, String> genderMappingsToSave);

	void setReportLocale(Locale reportLocale);
	
	Locale getReportLocale();
	
	void setReportTimezone(String reportTimezone);

	String getReportTimezone();

	DateTimeFormatter getReportDateTimeFormatter();
}
