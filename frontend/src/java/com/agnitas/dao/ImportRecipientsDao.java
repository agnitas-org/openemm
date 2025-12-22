/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.service.ProfileImportException.ReasonCode;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.ImportUtils.ImportErrorType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

public interface ImportRecipientsDao {

	String VALIDATOR_RESULT_RESERVED = "VALIDATOR_RESULT_RESERVED";
	String ERROR_EDIT_RECIPIENT_EDIT_RESERVED = "ERROR_EDIT_RECIPIENT_EDIT_RESERVED";
	String ERROR_EDIT_REASON_KEY_RESERVED = "ERROR_EDIT_REASON_KEY_RESERVED";

	/**
	 * Method checks if key column is indexed in database
	 *
	 * @param companyId	 company id
	 * @param keyColumns key columns names
	 * @return is key column indexed
	 */
	boolean isKeyColumnIndexed(int companyId, List<String> keyColumns);
		
	String createTemporaryCustomerImportTable(int companyID, String destinationTableName, int datasourceID, List<String> keyColumns, String sessionId, String description);

	String addIndexedIntegerColumn(int companyID, String tableName, String baseColumnName, String indexName);
	
	void dropTemporaryCustomerImportTable(int companyID, String tempTableName);

	DataSource getDataSource();

	int markDuplicatesEntriesCrossTable(int companyID, String destinationTableName, String sourceTableName, List<String> keyColumns, String duplicateSignColumn);

	int markDuplicatesEntriesSingleTable(int companyID, String temporaryImportTableName, List<String> keyColumns, String importIndexColumn, String duplicateIndexColumn);
	
	int removeNewCustomersWithInvalidNullValues(int companyID, String temporaryImportTableName, String destinationTableName);

	int insertNewCustomers(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, int datasourceId, int defaultMailType);

	int updateFirstExistingCustomersImproved(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId);

	int getNumberOfEntriesForInsert(String temporaryImportTableName, String duplicateIndexColumn);

	int assignNewCustomerToMailingList(int companyId, int datasourceId, int mailingListId, MediaTypes mediatype, UserStatus status);

	int assignExistingCustomerWithoutBindingToMailingList(String temporaryImportTableName, int companyId, int mailingListId, MediaTypes mediatype, UserStatus status);

	int changeStatusInMailingList(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, MediaTypes mediatype, UserStatus currentStatus, UserStatus newStatus, String remark);

	int updateLatestDataSourceId(int dataSourceId, String temporaryImportTableName, List<String> keyColumns, int companyId);
	
	int importInBlackList(String temporaryImportTableName, int companyId);

	int updateAllExistingCustomersByKeyColumnImproved(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId);

	String createTemporaryCustomerErrorTable(int companyId, int datasourceId, List<String> columns, String sessionId);

	void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName);
	
	void addErroneousDataItem(int companyID, String temporaryErrorTableName, Map<String, ColumnMapping> columnMappingByDbColumn, List<String> importedDBColumns, Map<String, Object> dataItem, int dataItemCount, ReasonCode reasonCode, String dataAttributeName);

	Map<ImportErrorType, Integer> getReasonStatistics(String temporaryErrorTableName);

	boolean hasRepairableErrors(String temporaryErrorTableName);

	int dropLeftoverTables(String hostName);

	PaginatedList<Map<String, Object>> getInvalidRecipientList(String temporaryErrorTableName, List<String> columns, String sort, String direction, int page, int rownums, int previousFullListSize);

	List<Integer> updateTemporaryErrors(int companyID, String temporaryErrorTableName, List<String> importedCsvFileColumns, Map<String, String> changedValues);

	Map<String, Object> getErrorLine(String temporaryErrorTableName, int csvIndex);

	int getResultEntriesCount(String selectIntStatement);

	void markErrorLineAsRepaired(int companyID, String temporaryErrorTableName, int csvIndex);

	List<Integer> getImportedCustomerIdsWithoutBindingToMailinglist(String temporaryImportTableName, int companyId, int datasourceId, int mailinglistId);

	int removeBlacklistedEmails(String tempTableName, int companyID);

	CaseInsensitiveMap<String, DbColumnType> getCustomerDbFields(int companyId);

	int getAllRecipientsCount(int companyId);

	int changeStatusInMailingListNotIncludedInTempData(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, MediaTypes mediatype, UserStatus currentStatus, UserStatus newStatus, String remark);

	void removeFromBlackListNotIncludedInTempData(String temporaryImportTableName, int companyId);

	void gatherTableStats(String tableName);

	void changeEmailColumnCollation(String temporaryImportTableName, String collation);

	void updateColumnOfTemporaryCustomerImportTable(String temporaryImportTableName, String columnName, Object value);

	int changeStatusInMailingListNotFoundInData(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, UserStatus currentStatus, UserStatus newStatus, List<UserType> inclusiveUserTypes, List<UserType> exclusiveUserTypes, String remark);

	Map<Integer, Map<MediaTypes, Map<UserStatus, Integer>>> getMailinglistStatusesForImportedRecipients(int companyID, List<Integer> mailinglistIDsToAssign, Set<MediaTypes> mediaTypes, int datasourceID);
}
