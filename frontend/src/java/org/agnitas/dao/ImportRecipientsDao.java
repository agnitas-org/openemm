/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.ProfileImportCsvException.ReasonCode;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.json.JsonObject;

public interface ImportRecipientsDao {
    String VALIDATOR_RESULT_RESERVED = "VALIDATOR_RESULT_RESERVED";
    String ERROR_EDIT_RECIPIENT_EDIT_RESERVED = "ERROR_EDIT_RECIPIENT_EDIT_RESERVED";
    String ERROR_EDIT_REASON_KEY_RESERVED = "ERROR_EDIT_REASON_KEY_RESERVED";
    String TYPE = "type";
        
    /**
     * Get information about database columns.
     * The information will be gathered from the DatabaseMetaData of the table
     * and the infoTable. The give infoTable should be the name of a table
     * which holds the following fields:
     * <dl>
     * <dt>col_name
     * <dd>Primary key to identify the column
     * <dt>shortname
     * <dd>Textfield for a short descriptive name of the column
     * <dt>default_value
     * <dd>the value which should be used for the column, when no other is given.
     * </dl>
     * The resulting list contains one row for each found column.
     * Each row is a Map consists of:
     * <dl>
     * <dt>column
     * <dd>the name of the column in Database
     * <dt>type
     * <dd>the typename as in java.sql.Types
     * (eg. VARCHAR for a java.sql.Types.VARCHAR
     * <dt>length
     * <dd>the size of the column as in DatabaseMetaData.getColumns()
     * <dt>nullable
     * <dd>inidcates whather the column can contain NULL values (1) or not (0).
     * <dt>shortname (optional)
     * <dd>a descriptive name for the column
     * <dt>default (optional)
     * <dd>value that should be used, when no value is given.
     * <dt>description (optional)
     * <dd>descriptive text for the column
     * </ul>
     *
     * @param companyId company id
     * @param column    The description of all columns from csv file which are imported
     * @return TreeMap containing column informations
     */
    Map<String, Map<String, Object>> getColumnInfoByColumnName( @VelocityCheck int companyId, String column);

    /**
	 * get list of table names whuch are still doesn't remove
	 *
	 * @param sessionId
	 * @return list of tables name
	 */
    List<String> getTemporaryTableNamesBySessionId(String sessionId);

	/**
	 * Method checks if key column is indexed in database
	 *
	 * @param companyId    company id
	 * @param keyColumn    key column name
	 * @return is key column indexed
	 */
    boolean isKeyColumnIndexed( @VelocityCheck int companyId, List<String> keyColumns);
    
	String createTemporaryCustomerImportTable(int companyID, String destinationTableName, int adminID, int datasourceID, List<String> keyColumns, String sessionId, String description) throws Exception;

	String addIndexedIntegerColumn(String tableName, String baseColumnName, String indexName) throws Exception;
	
	String addIndexedStringColumn(String tableName, String baseColumnName, String indexName) throws Exception;

	void dropTemporaryCustomerImportTable(String tempTableName);

	DataSource getDataSource();

	int markDuplicatesEntriesCrossTable(String destinationTableName, String sourceTableName, List<String> keyColumns, String duplicateSignColumn);

	int markDuplicatesEntriesSingleTable(String temporaryImportTableName, List<String> keyColumns, String importIndexColumn, String duplicateIndexColumn);
	
	int removeNewCustomersWithInvalidNullValues(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, List<ColumnMapping> columnMapping) throws Exception;

	int insertNewCustomers(String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, int datasourceId, int defaultMailType, List<ColumnMapping> columnMappingForDefaultValues);

	int updateFirstExistingCustomers(String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception;

	int getNumberOfEntriesForInsert(String temporaryImportTableName, String duplicateIndexColumn);

	int assignNewCustomerToMailingList(int companyId, int datasourceId, int mailingListId, UserStatus status);

	int assignExistingCustomerWithoutBindingToMailingList(String temporaryImportTableName, int companyId, int mailingListId, UserStatus status);

	int changeStatusInMailingList(String temporaryImportTableName, List<String> keyColumns, int companyId, int  mailingListId, int currentStatus, int updateStatus, String remark) throws Exception;
	
	int importInBlackList(String temporaryImportTableName, int companyId);

	int updateAllExistingCustomersByKeyColumn(String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception;

	String createTemporaryCustomerErrorTable(int companyId, int adminId, int datasourceId, List<String> columns, String sessionId) throws Exception;

	void addErrorneousCsvEntry(String temporaryErrorTableName, List<Integer> importedCsvFileColumnIndexes, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String errorneousFieldName);

	Map<ImportErrorType, Integer> getReasonStatistics(String temporaryErrorTableName);

	boolean hasRepairableErrors(String temporaryErrorTableName);

	int dropLeftoverTables(String hostName);

	PaginatedListImpl<Map<String, Object>> getInvalidRecipientList(String temporaryErrorTableName, List<String> columns, String sort, String direction, int page, int rownums, int previousFullListSize) throws Exception;

	List<Integer> updateTemporaryErrors(String temporaryErrorTableName, List<String> importedCsvFileColumns, Map<String, String> changedValues);

	Map<String, Object> getErrorLine(String temporaryErrorTableName, int csvIndex);

	void addErrorneousCsvEntry(String temporaryErrorTableName, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String errorneousFieldName);

	int getResultEntriesCount(String selectIntStatement);

	void markErrorLineAsRepaired(String temporaryErrorTableName, int csvIndex);

	List<Integer> getImportedCustomerIdsWithoutBindingToMailinglist(String temporaryImportTableName, int companyId, int datasourceId, int mailinglistId);

	int removeBlacklistedEmails(String tempTableName, int companyID);

	CaseInsensitiveMap<String, DbColumnType> getCustomerDbFields(int companyId) throws Exception;

	void addErrorneousJsonObject(String temporaryErrorTableName, Map<String, ColumnMapping> columnMappingByDbColumn, List<String> importedDBColumns, JsonObject jsonDataObject, int jsonObjectCount, ReasonCode reasonCode, String jsonAttributeName);

	boolean checkUnboundCustomersExist(int companyID);

	int getAllRecipientsCount(int companyId);
}
