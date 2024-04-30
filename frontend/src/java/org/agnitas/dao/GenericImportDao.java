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

import org.agnitas.service.GenericImportException.ReasonCode;
import org.agnitas.service.UpdateMethod;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.Triple;

public interface GenericImportDao {
	String createTemporaryImportTable(int companyID, String destinationTableName, int adminID, int datasourceID, List<String> keyColumns, String sessionId, String description) throws Exception;

	String addIndexedIntegerColumn(int companyID, String tableName, String baseColumnName, String indexName) throws Exception;

	void dropTemporaryImportTable(int companyID, String tempTableName);

	DataSource getDataSource();

	int markDuplicatesEntriesCrossTable(int companyID, String destinationTableName, String sourceTableName, List<String> keyColumns, String duplicateSignColumn);

	int markDuplicatesEntriesSingleTable(int companyID, String temporaryImportTableName, List<String> keyColumns, String importIndexColumn, String duplicateIndexColumn);
	
	int removeNewEntriesWithInvalidNullValues(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateInDestinationTableColumn) throws Exception;

	int insertNewEntries(int companyID, String temporaryImportTableName, String destinationTableName, List<String> keyColumns, List<String> importDbColumns, String duplicateIndexColumn, String duplicateInDestinationTableColumn);

	int updateAllExistingEntriesByKeyColumn(String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, String duplicateIndexColumn, String duplicateInDestinationTableColumn, UpdateMethod updateMethod, int datasourceId, int companyId) throws Exception;

	String createTemporaryImportErrorTable(int companyId, int adminId, int datasourceId, List<String> columns, String sessionId) throws Exception;

	void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<Integer> importedCsvFileColumnIndexes, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName);

	Map<ImportErrorType, Integer> getReasonStatistics(String temporaryErrorTableName);

	boolean hasRepairableErrors(String temporaryErrorTableName);

	List<Integer> updateTemporaryErrors(int companyID, String temporaryErrorTableName, List<String> importedCsvFileColumns, Map<String, String> changedValues);

	Map<String, Object> getErrorLine(String temporaryErrorTableName, int csvIndex);

	void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName);

	int getResultEntriesCount(String selectIntStatement);

	void markErrorLineAsRepaired(int companyID, String temporaryErrorTableName, int csvIndex);

	boolean isKeyColumnIndexed(String tablename, List<String> keyColumns);

	int clearTable(int companyID, String destinationTableName);

	boolean checkIfTableExists(String destinationTableName);

	List<Triple<Integer, ReasonCode, String>> getFirstErrorLines(String temporaryErrorTableName, int limit) throws Exception;

	CaseInsensitiveSet getPrimaryKeyColumns(String dbTableName) throws Exception;

	void gatherTableStats(String tableName);
}
