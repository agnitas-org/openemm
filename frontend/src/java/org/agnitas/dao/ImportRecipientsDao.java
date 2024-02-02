/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.service.ProfileImportException.ReasonCode;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.json.JsonObject;

public interface ImportRecipientsDao {
	String VALIDATOR_RESULT_RESERVED = "VALIDATOR_RESULT_RESERVED";
	String ERROR_EDIT_RECIPIENT_EDIT_RESERVED = "ERROR_EDIT_RECIPIENT_EDIT_RESERVED";
	String ERROR_EDIT_REASON_KEY_RESERVED = "ERROR_EDIT_REASON_KEY_RESERVED";
	String TYPE = "type";

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
	 * @param companyId	company id
	 * @param keyColumn	key column name
	 * @return is key column indexed
	 */
	boolean isKeyColumnIndexed( int companyId, List<String> keyColumns);
		
	String createTemporaryCustomerImportTable(int companyID, String destinationTableName, int datasourceID, List<String> keyColumns, String sessionId, String description) throws Exception;

	String addIndexedIntegerColumn(int companyID, String tableName, String baseColumnName, String indexName) throws Exception;
	
	String addIndexedStringColumn(int companyID, String tableName, String baseColumnName, String indexName) throws Exception;

	void dropTemporaryCustomerImportTable(int companyID, String tempTableName);

	DataSource getDataSource();

	int markDuplicatesEntriesCrossTable(int companyID, String destinationTableName, String sourceTableName, List<String> keyColumns, String duplicateSignColumn);

	int markDuplicatesEntriesSingleTable(int companyID, String temporaryImportTableName, List<String> keyColumns, String importIndexColumn, String duplicateIndexColumn);
	
	int removeNewCustomersWithInvalidNullValues(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, List<ColumnMapping> columnMapping) throws Exception;

	int insertNewCustomers(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, int datasourceId, int defaultMailType, List<ColumnMapping> columnMappingForDefaultValues, int companyId);

	int updateFirstExistingCustomers(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception;
	int updateFirstExistingCustomersImproved(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId2) throws Exception;

	int getNumberOfEntriesForInsert(String temporaryImportTableName, String duplicateIndexColumn);

	int assignNewCustomerToMailingList(int companyId, int datasourceId, int mailingListId, MediaTypes mediatype, UserStatus status);

	int assignExistingCustomerWithoutBindingToMailingList(String temporaryImportTableName, int companyId, int mailingListId, MediaTypes mediatype, UserStatus status);

	int changeStatusInMailingList(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, MediaTypes mediatype, int currentStatus, int updateStatus, String remark) throws Exception;
	
	int importInBlackList(String temporaryImportTableName, int companyId);

	int updateAllExistingCustomersByKeyColumn(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception;
	int updateAllExistingCustomersByKeyColumnImproved(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception;

	String createTemporaryCustomerErrorTable(int companyId, int datasourceId, List<String> columns, String sessionId) throws Exception;

	void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName);
	
	void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<Integer> importedCsvFileColumnIndexes, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName);

	void addErroneousJsonObject(int companyID, String temporaryErrorTableName, Map<String, ColumnMapping> columnMappingByDbColumn, List<String> importedDBColumns, JsonObject jsonDataObject, int jsonObjectCount, ReasonCode reasonCode, String jsonAttributeName);
	
	void addErroneousDataItem(int companyID, String temporaryErrorTableName, Map<String, ColumnMapping> columnMappingByDbColumn, List<String> importedDBColumns, Map<String, Object> dataItem, int dataItemCount, ReasonCode reasonCode, String dataAttributeName);

	Map<ImportErrorType, Integer> getReasonStatistics(String temporaryErrorTableName);

	boolean hasRepairableErrors(String temporaryErrorTableName);

	int dropLeftoverTables(int companyID, String hostName);
	
	int dropLeftoverTables(String hostName);

	PaginatedListImpl<Map<String, Object>> getInvalidRecipientList(String temporaryErrorTableName, List<String> columns, String sort, String direction, int page, int rownums, int previousFullListSize) throws Exception;

	List<Integer> updateTemporaryErrors(int companyID, String temporaryErrorTableName, List<String> importedCsvFileColumns, Map<String, String> changedValues);

	Map<String, Object> getErrorLine(String temporaryErrorTableName, int csvIndex);

	int getResultEntriesCount(String selectIntStatement);

	void markErrorLineAsRepaired(int companyID, String temporaryErrorTableName, int csvIndex);

	List<Integer> getImportedCustomerIdsWithoutBindingToMailinglist(String temporaryImportTableName, int companyId, int datasourceId, int mailinglistId);

	int removeBlacklistedEmails(String tempTableName, int companyID);

	CaseInsensitiveMap<String, DbColumnType> getCustomerDbFields(int companyId) throws Exception;

	boolean checkUnboundCustomersExist(int companyID);

	int getAllRecipientsCount(int companyId);

	int changeStatusInMailingListNotIncludedInTempData(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, MediaTypes mediatype, int currentStatus, int updateStatus, String remark) throws Exception;

	void removeFromBlackListNotIncludedInTempData(String temporaryImportTableName, int companyId);

	void gatherTableStats(String tableName);

	void changeEmailColumnCollation(String temporaryImportTableName, String collation);

	void updateColumnOfTemporaryCustomerImportTable(String temporaryImportTableName, String columnName, Object value);

	int changeStatusInMailingListNotFoundInData(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, int currentStatus, int updateStatus, List<UserType> inclusiveUserTypes, List<UserType> exclusiveUserTypes, String remark) throws Exception;
}
