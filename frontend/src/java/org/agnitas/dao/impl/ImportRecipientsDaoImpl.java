/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ProfileRecipientFields;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.ProfileRecipientFieldsImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.ImportException;
import org.agnitas.service.ProfileImportCsvException.ReasonCode;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.NullValuesAction;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.json.JsonObject;

public class ImportRecipientsDaoImpl extends RetryUpdateBaseDaoImpl implements ImportRecipientsDao {
	/**
	 * The logger.
	 */
	private static final transient Logger logger = LogManager.getLogger(ImportRecipientsDaoImpl.class);
	
    public static final String MYSQL_COMPARE_DATE_FORMAT = "%Y-%m-%d %H:%i:%s";
    public static final String JAVA_COMPARE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final int MAX_WRITE_PROGRESS = 90;
    public static final int MAX_WRITE_PROGRESS_HALF = MAX_WRITE_PROGRESS / 2;

    @Override
    public List<String> getTemporaryTableNamesBySessionId(String sessionId) {
        List<String> result = new ArrayList<>();
        String query = "SELECT temporary_table_name FROM import_temporary_tables WHERE session_id = ?";

		List<Map<String, Object>> resultList = select(logger, query, sessionId);
        for (Map<String, Object> row : resultList) {
            final String temporaryTableName = (String) row.get("temporary_table_name");
            result.add(temporaryTableName);
        }
        return result;
    }

    @Override
	public boolean isKeyColumnIndexed( @VelocityCheck int companyId, List<String> keyColumns) {
    	return DbUtilities.checkForIndex(getDataSource(), "customer_" + companyId + "_tbl", keyColumns);
    }

	@Override
	public String createTemporaryCustomerImportTable(int companyID, String destinationTableName, int adminID, int datasourceID, List<String> keyColumns, String sessionId, String description) throws Exception {
		List<String> alreadyRunningImports = select(logger, "SELECT description FROM import_temporary_tables WHERE import_table_name = ?", StringRowMapper.INSTANCE, destinationTableName.toLowerCase());
		if (alreadyRunningImports.size() > 0) {
			throw new ImportException(false, "error.import.AlreadyRunning", alreadyRunningImports.get(0));
		}
		
		String tempTableName = "tmp_imp" + companyID + "_" + datasourceID;
		
		retryableUpdate(companyID, logger, "INSERT INTO import_temporary_tables (session_id, temporary_table_name, import_table_name, host, description) VALUES(?, ?, ?, ?, ?)", sessionId, tempTableName, destinationTableName.toLowerCase(), AgnUtils.getHostName(), description);
		
		if (DbUtilities.checkIfTableExists(getDataSource(), tempTableName)) {
			logger.error("Import table " + tempTableName + " already existed. Dropped it to continue.");
			DbUtilities.dropTable(getDataSource(), tempTableName);
		}
		if (isOracleDB()) {
			String tablespacePart = "";
			if (DbUtilities.checkOracleTablespaceExists(getDataSource(), "data_temp")) {
				tablespacePart = " TABLESPACE data_temp";
			}
			
			execute(logger, "CREATE TABLE " + tempTableName + tablespacePart + " AS SELECT * FROM customer_" + companyID + "_tbl WHERE 1 = 0");
		} else {
			execute(logger, "CREATE TABLE " + tempTableName + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AS SELECT * FROM customer_" + companyID + "_tbl WHERE 1 = 0");
			execute(logger, "ALTER TABLE " + tempTableName + " MODIFY firstname VARCHAR(100) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci");
			execute(logger, "ALTER TABLE " + tempTableName + " MODIFY lastname VARCHAR(100) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci");
		}
		
		// Make all columns nullable
		CaseInsensitiveMap<String, DbColumnType> columnDataTypes = DbUtilities.getColumnDataTypes(getDataSource(), tempTableName);
		for (Entry<String, DbColumnType> columnDataType : columnDataTypes.entrySet()) {
			if (!columnDataType.getValue().isNullable()) {
				String typeString = columnDataType.getValue().getTypeName();
				if (columnDataType.getValue().getSimpleDataType() == SimpleDataType.Characters) {
					typeString += "(" + (Long.toString(columnDataType.getValue().getCharacterLength())) + ")";
				} else if (columnDataType.getValue().getSimpleDataType() == SimpleDataType.Numeric || columnDataType.getValue().getSimpleDataType() == SimpleDataType.Float) {
					typeString += "(" + (Integer.toString(columnDataType.getValue().getNumericPrecision())) + ")";
				}
				execute(logger, "ALTER TABLE " + tempTableName + " MODIFY " + columnDataType.getKey() + " " + typeString + " NULL");
			}
		}
		
		// Always create customer_id index on temp table
		execute(logger, "CREATE INDEX tmp_" + datasourceID + "_cust_idx ON " + tempTableName + " (customer_id)");
		
		// Always create email index on temp table for
		// blacklist actions
		execute(logger, "CREATE INDEX tmp_" + datasourceID + "_email_idx ON " + tempTableName + " (email)");
		
		if (keyColumns != null && !keyColumns.isEmpty() && (keyColumns.size() != 1 || (!keyColumns.get(0).equalsIgnoreCase("customer_id") && !keyColumns.get(0).equalsIgnoreCase("email")))) {
			// Only create keyColumns index on temp table, if customer_id is not the only keycolumn (otherwise we would have twice the same index)
			execute(logger, "CREATE INDEX tmp_" + datasourceID + "_idx ON " + tempTableName + " (" + StringUtils.join(keyColumns, ", ") + ")");
		}
		
		return tempTableName;
	}

	@Override
	public String createTemporaryCustomerErrorTable(int companyID, int adminID, int datasourceID, List<String> csvColumns, String sessionId) throws Exception {
		String tempTableName = "tmp_err" + companyID + "_" + adminID + "_" + datasourceID;
		
		retryableUpdate(companyID, logger, "INSERT INTO import_temporary_tables (session_id, temporary_table_name, host) VALUES(?, ?, ?)", sessionId, tempTableName, AgnUtils.getHostName());
		
		if (DbUtilities.checkIfTableExists(getDataSource(), tempTableName)) {
			logger.error("Import table " + tempTableName + " already existed. Dropped it to continue.");
			DbUtilities.dropTable(getDataSource(), tempTableName);
		}
		
		if (isOracleDB()) {
			String tablespacePart = "";
			if (DbUtilities.checkOracleTablespaceExists(getDataSource(), "data_temp")) {
				tablespacePart = " TABLESPACE data_temp";
			}
			
			execute(logger, "CREATE TABLE " + tempTableName + "("
				+ " csvindex INTEGER,"
				+ " reason VARCHAR2(50),"
				+ " errorfield VARCHAR2(50),"
				+ " errorfixed INTEGER DEFAULT 0"
				+ ")" + tablespacePart);
		} else {
			execute(logger, "CREATE TABLE " + tempTableName + "("
				+ " csvindex INTEGER,"
				+ " reason VARCHAR(50),"
				+ " errorfield VARCHAR(50),"
				+ " errorfixed INTEGER DEFAULT 0"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
		}
		
		// Create index on temp error table
		execute(logger, "CREATE INDEX tmperr_" + datasourceID + "_csv_idx ON " + tempTableName + " (csvindex)");
		execute(logger, "CREATE INDEX tmperr_" + datasourceID + "_rsn_idx ON " + tempTableName + " (reason)");
		
		for (String column : csvColumns) {
			execute(logger, "ALTER TABLE " + tempTableName + " ADD " + column + " " + (isOracleDB() ? "CLOB" : "TEXT"));
		}
		
		return tempTableName;
	}

	@Override
	public String addIndexedIntegerColumn(int companyID, String tableName, String baseColumnName, String indexName) throws Exception {
		String importIndexColumn = baseColumnName;
		int testIndex = 0;
		int testIndexMaximum = 10;
		while (DbUtilities.checkTableAndColumnsExist(getDataSource(), tableName, importIndexColumn) && testIndex < testIndexMaximum) {
			testIndex++;
			importIndexColumn = baseColumnName + testIndex;
		}
		if (testIndex >= testIndexMaximum) {
			throw new Exception("Cannot create column with basename " + baseColumnName + " in table " + tableName);
		}
		retryableUpdate(companyID, logger, "ALTER TABLE " + tableName + " ADD " + importIndexColumn + " INTEGER");
		retryableUpdate(companyID, logger, "CREATE INDEX " + indexName + " ON " + tableName + " (" + importIndexColumn + ")");
		return importIndexColumn;
	}

	@Override
	public String addIndexedStringColumn(int companyID, String tableName, String baseColumnName, String indexName) throws Exception {
		String importIndexColumn = baseColumnName;
		int testIndex = 0;
		int testIndexMaximum = 10;
		while (DbUtilities.checkTableAndColumnsExist(getDataSource(), tableName, importIndexColumn) && testIndex < testIndexMaximum) {
			testIndex++;
			importIndexColumn = baseColumnName + testIndex;
		}
		if (testIndex >= testIndexMaximum) {
			throw new Exception("Cannot create column with basename " + baseColumnName + " in table " + tableName);
		}
		retryableUpdate(companyID, logger, "ALTER TABLE " + tableName + " ADD " + importIndexColumn + " VARCHAR" + (isOracleDB() ? "2" : "") + "(4000)");
		retryableUpdate(companyID, logger, "CREATE INDEX " + indexName + " ON " + tableName + " (" + importIndexColumn + ")");
		return importIndexColumn;
	}

	@Override
	public void dropTemporaryCustomerImportTable(int companyID, String tempTableName) {
		if (StringUtils.isNotBlank(tempTableName)) {
			if (DbUtilities.checkIfTableExists(getDataSource(), tempTableName)) {
				DbUtilities.dropTable(getDataSource(), tempTableName);
			}
			retryableUpdate(companyID, logger, "DELETE FROM import_temporary_tables WHERE temporary_table_name = ?", tempTableName);
		}
	}

	@Override
	public DataSource getDataSource() {
		return super.getDataSource();
	}

	@Override
	public int markDuplicatesEntriesCrossTable(int companyID, String destinationTableName, String sourceTableName, List<String> keyColumns, String duplicateSignColumn) {
		if (keyColumns != null && !keyColumns.isEmpty()) {
			List<String> keycolumnParts = new ArrayList<>();
			for (String keyColumn : keyColumns) {
				keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
			}
			String updateStatement = "UPDATE " + destinationTableName + " dst SET dst." + duplicateSignColumn + " = COALESCE((SELECT MIN(src." + duplicateSignColumn + ") FROM " + sourceTableName + " src WHERE " + StringUtils.join(keycolumnParts, " AND ") + "), 0)";
			retryableUpdate(companyID, logger, updateStatement);
			return select(logger, "SELECT COUNT(*) FROM " + destinationTableName + " WHERE " + duplicateSignColumn + " > 0", Integer.class);
		} else {
			return 0;
		}
	}

	@Override
	public int markDuplicatesEntriesSingleTable(int companyID, String tempTableName, List<String> keyColumns, String itemIndexColumn, String duplicateIndexColumn) {
		if (keyColumns != null && !keyColumns.isEmpty()) {
			List<String> keycolumnParts = new ArrayList<>();
			for (String keyColumn : keyColumns) {
				keycolumnParts.add(tempTableName + "." + keyColumn + " = subselect." + keyColumn + " AND " + tempTableName + "." + keyColumn + " IS NOT NULL");
			}
			String updateStatement = "UPDATE " + tempTableName + " SET " + duplicateIndexColumn + " = (SELECT subselect." + itemIndexColumn + " FROM"
				+ " (SELECT " + StringUtils.join(keyColumns, ", ") + ", MIN(" + itemIndexColumn + ") AS " + itemIndexColumn + " FROM " + tempTableName + " GROUP BY " + StringUtils.join(keyColumns, ", ") + ") subselect"
				+ " WHERE " + StringUtils.join(keycolumnParts, " AND ") + ")";
			int possibleDuplicatesCount = retryableUpdate(companyID, logger, updateStatement);
			int revertedDuplicatesCount = retryableUpdate(companyID, logger, "UPDATE " + tempTableName + " SET " + duplicateIndexColumn + " = NULL WHERE " + itemIndexColumn + " = " + duplicateIndexColumn);
			return possibleDuplicatesCount - revertedDuplicatesCount;
		} else {
			return 0;
		}
	}

	@Override
	public int insertNewCustomers(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> columnsToInsert, String duplicateIndexColumn, int datasourceId, int defaultMailType, List<ColumnMapping> columnMappingForDefaultValues, int companyId) {
		String additionalColumns = "";
		String additionalValues = "";
		if (!columnsToInsert.contains("mailtype")) {
			additionalColumns += ", mailtype";
			additionalValues += ", " + defaultMailType;
		}
		if (!columnsToInsert.contains("gender")) {
			additionalColumns += ", gender";
			additionalValues += ", 2";
		}
		
		// Import of new customers with a given customer_id is not supported, because it will corrupt the customer_id sequencing
		columnsToInsert = new ArrayList<>(columnsToInsert);
		columnsToInsert.remove("customer_id");
		
		int insertedItems;

		if (isOracleDB()) {
			String customerIdSequenceName = destinationTableName + "_seq";
			insertedItems = retryableUpdate(companyID, logger, "INSERT INTO " + destinationTableName + " (customer_id, creation_date, timestamp, datasource_id, latest_datasource_id" + additionalColumns + ", " + StringUtils.join(columnsToInsert, ", ") + ") (SELECT " + customerIdSequenceName + ".NEXTVAL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + datasourceId + ", " + datasourceId + additionalValues + ", " + StringUtils.join(columnsToInsert, ", ") + " FROM " + tempTableName + " WHERE (customer_id = 0 OR customer_id IS NULL) AND " + duplicateIndexColumn + " IS NULL)");
		} else {
			insertedItems = retryableUpdate(companyID, logger, "INSERT INTO " + destinationTableName + " (creation_date, timestamp, datasource_id, latest_datasource_id" + additionalColumns + ", " + StringUtils.join(columnsToInsert, ", ") + ") (SELECT CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, " + datasourceId + ", " + datasourceId + additionalValues + ", " + StringUtils.join(columnsToInsert, ", ") + " FROM " + tempTableName + " WHERE (customer_id = 0 OR customer_id IS NULL) AND " + duplicateIndexColumn + " IS NULL)");
		}
		
		if (keyColumns != null && !keyColumns.isEmpty()) {
			List<String> keycolumnParts = new ArrayList<>();
			for (String keyColumn : keyColumns) {
				keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
			}
			// Update remaining csv data items in temp table which might be used for updates later
			retryableUpdate(companyID, logger, "UPDATE " + tempTableName + " src SET customer_id = (SELECT MAX(customer_id) FROM " + destinationTableName + " dst WHERE " + StringUtils.join(keycolumnParts, " AND ") + ") WHERE (customer_id IS NULL OR customer_id = 0) AND " + duplicateIndexColumn + " IS NOT NULL");
		}
		
		return insertedItems;
	}

	/**
	 * Only update the first customer with the suitable customer_id
	 * @throws Exception
	 */
	@Override
	public int updateFirstExistingCustomers(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception {
		if (keyColumns == null || keyColumns.isEmpty()) {
			throw new Exception("Missing keycolumns");
		}
		
		// Do not update the field customer_id
		updateColumns = new ArrayList<>(updateColumns);
		updateColumns.remove("customer_id");
		
		// Do not update the keycolumns
		updateColumns = new ArrayList<>(updateColumns);
		updateColumns.removeAll(keyColumns);
		
		if (!updateColumns.isEmpty()) {
			if (nullValuesAction == NullValuesAction.OVERWRITE.getIntValue()) {
				if (isOracleDB()) {
					// Oracle supports multi-column updates like "UPDATE table SET (a, b, c) = (SELECT a, b, c FROM othertable ...)",
					// which are more performant than "UPDATE table SET a = (SELECT ...), b = (SELECT ...), c = (SELECT ...)"
					String updateAllAtOnce = "UPDATE " + destinationTableName + " dst"
						+ " SET (" + StringUtils.join(updateColumns, ", ") + ") = (SELECT " + StringUtils.join(updateColumns, ", ") + " FROM " + tempTableName
							+ " WHERE " + importIndexColumn + " = (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE dst.customer_id = src.customer_id))"
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE dst.customer_id = src.customer_id)";
					retryableUpdate(companyID, logger, updateAllAtOnce);
				} else {
					// MySQL and MariaDB do not support multi-column updates like "UPDATE table SET (a, b, c) = (SELECT a, b, c FROM othertable ...)"
					String updateSetPart = "";
					for (String updateColumn : updateColumns) {
						if (updateSetPart.length() > 0) {
							updateSetPart += ", ";
						}
						updateSetPart += updateColumn + " = (SELECT " + updateColumn + " FROM " + tempTableName + " WHERE " + importIndexColumn + " ="
							+ " (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE dst.customer_id = src.customer_id))";
					}
					String updateAllAtOnce = "UPDATE " + destinationTableName + " dst SET " + updateSetPart
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE dst.customer_id = src.customer_id)";
					retryableUpdate(companyID, logger, updateAllAtOnce);
				}
			} else {
				for (String updateColumn : updateColumns) {
					String updateSingleColumn = "UPDATE " + destinationTableName + " dst"
						+ " SET " + updateColumn + " = (SELECT " + updateColumn + " FROM " + tempTableName + " WHERE " + importIndexColumn + " ="
							+ " (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE " + updateColumn + " IS NOT NULL AND dst.customer_id = src.customer_id))"
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE " + updateColumn + " IS NOT NULL AND dst.customer_id = src.customer_id)";
					retryableUpdate(companyID, logger, updateSingleColumn);
				}
			}
		}

		int updatedItems;
		// Set change date and latest datasource id for updated items
		boolean hasCleanedDateField = DbUtilities.checkTableAndColumnsExist(getDataSource(), destinationTableName, ComCompanyDaoImpl.STANDARD_FIELD_CLEANED_DATE);
		updatedItems = retryableUpdate(companyID, logger, "UPDATE " + destinationTableName + " SET timestamp = CURRENT_TIMESTAMP" + (hasCleanedDateField ? ", " + ComCompanyDaoImpl.STANDARD_FIELD_CLEANED_DATE + " = NULL" : "") + " , latest_datasource_id = ? WHERE customer_id IN (SELECT DISTINCT customer_id FROM " + tempTableName + " WHERE customer_id != 0 AND customer_id IS NOT NULL)", datasourceId);
		
		return updatedItems;
	}
	/**
	 * Update all customers with the suitable keycolumn value combination
	 */
	@Override
	public int updateAllExistingCustomersByKeyColumn(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> updateColumns, String importIndexColumn, int nullValuesAction, int datasourceId, int companyId) throws Exception {
		if (keyColumns == null || keyColumns.isEmpty()) {
			throw new Exception("Missing keycolumns");
		}
		
		// Do not update the field customer_id
		updateColumns = new ArrayList<>(updateColumns);
		updateColumns.remove("customer_id");
		
		// Do not update the keycolumns
		updateColumns = new ArrayList<>(updateColumns);
		updateColumns.removeAll(keyColumns);
		
		if (!updateColumns.isEmpty()) {
			List<String> keycolumnParts = new ArrayList<>();
			for (String keyColumn : keyColumns) {
				keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
			}
			if (nullValuesAction == NullValuesAction.OVERWRITE.getIntValue()) {
				if (isOracleDB()) {
					// Oracle supports multi-column updates like "UPDATE table SET (a, b, c) = (SELECT a, b, c FROM othertable ...)",
					// which are more performant than "UPDATE table SET a = (SELECT ...), b = (SELECT ...), c = (SELECT ...)"
					String updateAllAtOnce = "UPDATE " + destinationTableName + " dst"
						+ " SET (" + StringUtils.join(updateColumns, ", ") + ") = (SELECT " + StringUtils.join(updateColumns, ", ") + " FROM " + tempTableName
							+ " WHERE " + importIndexColumn + " = (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0))"
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0)";
					retryableUpdate(companyID, logger, updateAllAtOnce);
				} else {
					// MySQL and MariaDB do not support multi-column updates like "UPDATE table SET (a, b, c) = (SELECT a, b, c FROM othertable ...)"
					String updateSetPart = "";
					for (String updateColumn : updateColumns) {
						if (updateSetPart.length() > 0) {
							updateSetPart += ", ";
						}
						updateSetPart += updateColumn + " = (SELECT " + updateColumn + " FROM " + tempTableName + " WHERE " + importIndexColumn + " ="
							+ " (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0))";
					}
					String updateAllAtOnce = "UPDATE " + destinationTableName + " dst SET " + updateSetPart
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0)";
					retryableUpdate(companyID, logger, updateAllAtOnce);
				}
			} else {
				for (String updateColumn : updateColumns) {
					String updateSingleColumn = "UPDATE " + destinationTableName + " dst"
						+ " SET " + updateColumn + " = (SELECT " + updateColumn + " FROM " + tempTableName + " WHERE " + importIndexColumn + " ="
							+ " (SELECT MAX(" + importIndexColumn + ") FROM " + tempTableName + " src WHERE " + updateColumn + " IS NOT NULL AND " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0))"
						+ " WHERE EXISTS (SELECT 1 FROM " + tempTableName + " src WHERE " + updateColumn + " IS NOT NULL AND " + StringUtils.join(keycolumnParts, " AND ") + " AND customer_id > 0)";
					retryableUpdate(companyID, logger, updateSingleColumn);
				}
			}
		}
		
		int updatedItems;
		// Set change date and latest datasource id for updated items
		boolean hasCleanedDateField = DbUtilities.checkTableAndColumnsExist(getDataSource(), destinationTableName, ComCompanyDaoImpl.STANDARD_FIELD_CLEANED_DATE);
		updatedItems = retryableUpdate(companyID, logger, "UPDATE " + destinationTableName + " SET timestamp = CURRENT_TIMESTAMP" + (hasCleanedDateField ? ", " + ComCompanyDaoImpl.STANDARD_FIELD_CLEANED_DATE + " = NULL" : "") + " , latest_datasource_id = ? WHERE (" + StringUtils.join(keyColumns, ", ") + ") IN (SELECT DISTINCT " + StringUtils.join(keyColumns, ", ") + " FROM " + tempTableName + " WHERE customer_id != 0 AND customer_id IS NOT NULL)", datasourceId);
		
		return updatedItems;
	}

	@Override
	public int getNumberOfEntriesForInsert(String temporaryImportTableName, String duplicateIndexColumn) {
		return select(logger, "SELECT COUNT(*) FROM " + temporaryImportTableName + " WHERE (customer_id IS NULL OR customer_id = 0) AND " + duplicateIndexColumn + " IS NULL", Integer.class);
	}

	@Override
	public int assignNewCustomerToMailingList(int companyID, int datasourceId, int mailingListId, MediaTypes mediatype, UserStatus status) {
		String insertBindingsStatement = "INSERT INTO customer_" + companyID + "_binding_tbl (customer_id, user_type, user_status, user_remark, timestamp, creation_date, exit_mailing_id, mailinglist_id, mediatype)"
        	+ " (SELECT customer_id, '" + UserType.World.getTypeCode() + "', ?, 'CSV File Upload', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, ?, ? FROM customer_" + companyID + "_tbl cust WHERE datasource_id = ?"
        	+ " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND ? = bind.mailinglist_id AND ? = bind.mediatype))";
		return retryableUpdate(companyID, logger, insertBindingsStatement, status.getStatusCode(), mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode(), datasourceId, mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode());
	}

	@Override
	public int assignExistingCustomerWithoutBindingToMailingList(String temporaryImportTableName, int companyID, int mailingListId, MediaTypes mediatype, UserStatus status) {
		String insertBindingsStatement = "INSERT INTO customer_" + companyID + "_binding_tbl (customer_id, user_type, user_status, user_remark, timestamp, creation_date, exit_mailing_id, mailinglist_id, mediatype)"
        	+ " (SELECT DISTINCT customer_id, '" + UserType.World.getTypeCode() + "', ?, 'CSV File Upload', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, ?, ? FROM " + temporaryImportTableName + " temp WHERE (customer_id != 0 AND customer_id IS NOT NULL)"
        	+ " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE temp.customer_id = bind.customer_id AND ? = bind.mailinglist_id AND ? = bind.mediatype))";
		return retryableUpdate(companyID, logger, insertBindingsStatement, status.getStatusCode(), mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode(), mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode());
	}

	@Override
	public int changeStatusInMailingList(String temporaryImportTableName, List<String> keyColumns, int companyID, int mailingListId, MediaTypes mediatype, int currentStatus, int updateStatus, String remark) throws Exception {
		// Check for valid UserStatus code
		UserStatus.getUserStatusByID(updateStatus);
		
		List<String> keycolumnParts = new ArrayList<>();
		for (String keyColumn : keyColumns) {
			keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
		}
		
		return retryableUpdate(companyID, logger, "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP WHERE user_status = ? AND mailinglist_id = ? AND mediatype = ? AND customer_id IN ("
			+ "SELECT dst.customer_id FROM " + temporaryImportTableName + " src, customer_" + companyID + "_tbl dst WHERE " + StringUtils.join(keycolumnParts, " AND ") + ")", updateStatus, remark, currentStatus, mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode());
	}

    @Override
	@DaoUpdateReturnValueCheck
    public int importInBlackList(String temporaryImportTableName, @VelocityCheck final int companyID) {
        String updateBlacklist = "INSERT INTO cust" + companyID + "_ban_tbl (email) (SELECT DISTINCT email FROM " + temporaryImportTableName + " temp WHERE email IS NOT NULL"
        	+ " AND NOT EXISTS (SELECT email FROM cust" + companyID + "_ban_tbl ban WHERE ban.email = temp.email))";
        return retryableUpdate(companyID, logger, updateBlacklist);
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void removeFromBlackListNotIncludedInTempData(String temporaryImportTableName, @VelocityCheck final int companyID) {
        String updateBlacklist = "DELETE FROM cust" + companyID + "_ban_tbl WHERE email NOT IN (SELECT DISTINCT email FROM " + temporaryImportTableName + " temp WHERE email IS NOT NULL)";
        retryableUpdate(companyID, logger, updateBlacklist);
    }

	@Override
	public void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<Integer> importedCsvFileColumnIndexes, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName) {
		List<String> columnNames = new ArrayList<>();
		Object[] parameters = new Object[importedCsvFileColumnIndexes.size() + 3];
		for (int i = 0; i < importedCsvFileColumnIndexes.size(); i++) {
			columnNames.add("data_" + (i + 1));
			parameters[i] = csvDataLine.get(importedCsvFileColumnIndexes.get(i));
		}
		parameters[importedCsvFileColumnIndexes.size()] = csvLineIndex;
		parameters[importedCsvFileColumnIndexes.size() + 1] = reasonCode == null ? "Unknown" : reasonCode.toString();
		parameters[importedCsvFileColumnIndexes.size() + 2] = erroneousFieldName;
		
		retryableUpdate(companyID, logger, "DELETE FROM " + temporaryErrorTableName + " WHERE csvindex = ?", csvLineIndex);
		retryableUpdate(companyID, logger, "INSERT INTO " + temporaryErrorTableName + " (" + StringUtils.join(columnNames, ", ") + ", csvindex, reason, errorfield, errorfixed) VALUES (" + AgnUtils.repeatString("?", columnNames.size(), ", ") + ", ?, ?, ?, 0)", parameters);
	}
	
	@Override
	public void addErroneousCsvEntry(int companyID, String temporaryErrorTableName, List<String> csvDataLine, int csvLineIndex, ReasonCode reasonCode, String erroneousFieldName) {
		List<String> columnNames = new ArrayList<>();
		Object[] parameters = new Object[csvDataLine.size() + 3];
		for (int i = 0; i < csvDataLine.size(); i++) {
			columnNames.add("data_" + (i + 1));
			parameters[i] = csvDataLine.get(i);
		}
		parameters[csvDataLine.size()] = csvLineIndex;
		parameters[csvDataLine.size() + 1] = reasonCode == null ? "Unknown" : reasonCode.toString();
		parameters[csvDataLine.size() + 2] = erroneousFieldName;

		retryableUpdate(companyID, logger, "DELETE FROM " + temporaryErrorTableName + " WHERE csvindex = ?", csvLineIndex);
		retryableUpdate(companyID, logger, "INSERT INTO " + temporaryErrorTableName + " (" + StringUtils.join(columnNames, ", ") + ", csvindex, reason, errorfield, errorfixed) VALUES (" + AgnUtils.repeatString("?", columnNames.size(), ", ") + ", ?, ?, ?, 0)", parameters);
	}

	@Override
	public void addErroneousJsonObject(int companyID, String temporaryErrorTableName, Map<String, ColumnMapping> columnMappingByDbColumn, List<String> importedDBColumns, JsonObject jsonDataObject, int jsonObjectCount, ReasonCode reasonCode, String jsonAttributeName) {
		List<String> columnNames = new ArrayList<>();
		Object[] parameters = new Object[importedDBColumns.size() + 3];
		for (int i = 0; i < importedDBColumns.size(); i++) {
			columnNames.add("data_" + (i + 1));
			parameters[i] = jsonDataObject.get(columnMappingByDbColumn.get(importedDBColumns.get(i)).getFileColumn());
		}
		parameters[columnNames.size()] = jsonObjectCount;
		parameters[columnNames.size() + 1] = reasonCode == null ? "Unknown" : reasonCode.toString();
		parameters[columnNames.size() + 2] = jsonAttributeName;

		retryableUpdate(companyID, logger, "DELETE FROM " + temporaryErrorTableName + " WHERE csvindex = ?", jsonObjectCount);
		retryableUpdate(companyID, logger, "INSERT INTO " + temporaryErrorTableName + " (" + StringUtils.join(columnNames, ", ") + ", csvindex, reason, errorfield, errorfixed) VALUES (" + AgnUtils.repeatString("?", columnNames.size(), ", ") + ", ?, ?, ?, 0)", parameters);
	}

	@Override
	public Map<ImportErrorType, Integer> getReasonStatistics(String temporaryErrorTableName) {
		List<Map<String, Object>> result = select(logger, "SELECT reason, COUNT(*) amount FROM " + temporaryErrorTableName + " WHERE errorfixed = 0 GROUP BY reason");
		Map<ImportErrorType, Integer> resultMap = new HashMap<>();
		for (Map<String, Object> row : result) {
			int count = ((Number) row.get("amount")).intValue();
			String reason = (String) row.get("reason");
			ReasonCode reasonCode = ReasonCode.getFromString(reason);
			if (reasonCode == null) {
				// String reason 'Unknown' returns ReasonCode null
				resultMap.put(ImportErrorType.DBINSERT_ERROR, count);
			} else {
				switch (reasonCode) {
					case InvalidEmail:
						resultMap.put(ImportErrorType.EMAIL_ERROR, count);
						break;
					case InvalidMailtype:
						resultMap.put(ImportErrorType.MAILTYPE_ERROR, count);
						break;
					case InvalidGender:
						resultMap.put(ImportErrorType.GENDER_ERROR, count);
						break;
					case InvalidDate:
						resultMap.put(ImportErrorType.DATE_ERROR, count);
						break;
					case InvalidNumber:
						resultMap.put(ImportErrorType.NUMERIC_ERROR, count);
						break;
					case InvalidEncryption:
						resultMap.put(ImportErrorType.ENCRYPTION_ERROR, count);
						break;
					case ValueTooLarge:
						resultMap.put(ImportErrorType.VALUE_TOO_LARGE_ERROR, count);
						break;
					case NumberTooLarge:
						resultMap.put(ImportErrorType.NUMBER_TOO_LARGE_ERROR, count);
						break;
					default:
						resultMap.put(ImportErrorType.DBINSERT_ERROR, count);
						break;
				}
			}
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> getErrorLine(String temporaryErrorTableName, int csvIndex) {
		Map<String, Object> returnValue = selectSingleRow(logger, "SELECT * FROM " + temporaryErrorTableName + " WHERE csvindex = ?", csvIndex);
		return returnValue;
	}

	@Override
	public void markErrorLineAsRepaired(int companyID, String temporaryErrorTableName, int csvIndex) {
		retryableUpdate(companyID, logger, "UPDATE " + temporaryErrorTableName + " SET errorfixed = 1 WHERE csvindex = ?", csvIndex);
	}

	@Override
	public boolean hasRepairableErrors(String temporaryErrorTableName) {
		int repairableItems = selectInt(logger, "SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE errorfixed = 0");
		return repairableItems > 0;
	}

	@Override
	public int dropLeftoverTables(int companyID, String hostName) {
		List<String> tableNames = select(logger, "SELECT temporary_table_name FROM import_temporary_tables WHERE LOWER(host) = ?", StringRowMapper.INSTANCE, hostName.toLowerCase());
		int droppedTables = 0;
		for (String tableName : tableNames) {
			if (DbUtilities.checkIfTableExists(getDataSource(), tableName)) {
				DbUtilities.dropTable(getDataSource(), tableName);
				droppedTables++;
			}
			retryableUpdate(companyID, logger, "DELETE FROM import_temporary_tables WHERE temporary_table_name = ?", tableName);
		}
		return droppedTables;
	}

	@Override
	public int dropLeftoverTables(String hostName) {
		List<String> tableNames = select(logger, "SELECT temporary_table_name FROM import_temporary_tables WHERE LOWER(host) = ?", StringRowMapper.INSTANCE, hostName.toLowerCase());
		int droppedTables = 0;
		for (String tableName : tableNames) {
			if (DbUtilities.checkIfTableExists(getDataSource(), tableName)) {
				DbUtilities.dropTable(getDataSource(), tableName);
				droppedTables++;
			}
			update(logger, "DELETE FROM import_temporary_tables WHERE temporary_table_name = ?", tableName);
		}
		return droppedTables;
	}
	
    @Override
    public PaginatedListImpl<Map<String, Object>> getInvalidRecipientList(String temporaryErrorTableName, List<String> columns, String sort, String direction, int page, int rownums, int previousFullListSize) throws Exception {
        int totalRows = selectInt(logger, "SELECT COUNT(*) FROM " + temporaryErrorTableName + " WHERE errorfixed = 0");
        if (previousFullListSize == 0 || previousFullListSize != totalRows) {
            page = 1;
        }

        int offset = (page - 1) * rownums;

        String sqlStatement;
        if (isOracleDB()) {
            sqlStatement = "SELECT * FROM (SELECT tmp.*, ROWNUM r FROM " + temporaryErrorTableName + " tmp WHERE errorfixed = 0) WHERE r BETWEEN " + (offset + 1) + " AND " + (offset + rownums);
        } else {
            sqlStatement = "SELECT * FROM " + temporaryErrorTableName + " WHERE errorfixed = 0 LIMIT " + offset + " , " + rownums;
        }

		List<Map<String, Object>> result = select(logger, sqlStatement);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map<String, Object> row : result) {
        	// This map contains all the csv values and a temporaryId, but also has an extra entry for the validator results and an extra entry for a ProfileRecipientFields object
        	// The ProfileRecipientFields object also has a special data entry "temporaryId" for referencing the csv data line
            Map<String, Object> newBean = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
            	String value = (String) row.get("data_" + (i + 1));
                newBean.put(columns.get(i), value);
            }
            
            final int temporaryID = ((Number) row.get("csvindex")).intValue();
            
            final ProfileRecipientFields recipient = new ProfileRecipientFieldsImpl();
            recipient.setTemporaryId(Integer.toString(temporaryID));
            Map<String, String> customFields = new HashMap<>();
            for (Entry<String, Object> newBeanEntry : newBean.entrySet()) {
            	String fieldName = newBeanEntry.getKey();
            	if ("email".equals(fieldName)) {
            		recipient.setEmail((String) newBean.get("email"));
            	} else if ("gender".equals(fieldName)) {
            		recipient.setGender((String) newBean.get("gender"));
            	} else if ("mailtype".equals(fieldName)) {
            		recipient.setMailtype((String) newBean.get("mailtype"));
            	} else if ("firstname".equals(fieldName)) {
            		recipient.setFirstname((String) newBean.get("firstname"));
            	} else if ("lastname".equals(fieldName)) {
            		recipient.setLastname((String) newBean.get("lastname"));
            	} else if ("creation_date".equals(fieldName)) {
            		recipient.setCreation_date((String) newBean.get("creation_date"));
            	} else if ("change_date".equals(fieldName)) {
            		recipient.setChange_date((String) newBean.get("change_date"));
            	} else if ("title".equals(fieldName)) {
            		recipient.setTitle((String) newBean.get("title"));
            	} else if ("isMailtypeDefined".equals(fieldName)) {
            		recipient.setMailtypeDefined((String) newBean.get("isMailtypeDefined"));
            	} else {
	            	customFields.put(fieldName, (String) newBean.get(fieldName));
            	}
            }
            recipient.setCustomFields(customFields);
            newBean.put(ERROR_EDIT_RECIPIENT_EDIT_RESERVED, recipient);
            
            newBean.put("temporaryId", temporaryID);
            
            newBean.put(VALIDATOR_RESULT_RESERVED, row.get("errorfield"));
            
            newBean.put(ERROR_EDIT_REASON_KEY_RESERVED, ReasonCode.getFromString((String) row.get("reason")).getMessageKey());
            
            resultList.add(newBean);
        }

        return new PaginatedListImpl<>(resultList, totalRows, rownums, page, sort, direction);
    }

	@Override
	public List<Integer> updateTemporaryErrors(int companyID, String temporaryErrorTableName, List<String> importedCsvFileColumns, Map<String, String> changedValues) {
		List<Integer> updatedCsvIndexes = new ArrayList<>();
		for (Entry<String, String> changeEntry : changedValues.entrySet()) {
			int csvIndex = Integer.parseInt(changeEntry.getKey().substring(0, changeEntry.getKey().indexOf("/")));
			String changedFieldName = changeEntry.getKey().substring(changeEntry.getKey().indexOf("/") + 1);
			String newValue = changeEntry.getValue();
			String columnToUpdate = "data_" + (importedCsvFileColumns.indexOf(changedFieldName) + 1);
			retryableUpdate(companyID, logger, "UPDATE " + temporaryErrorTableName + " SET " + columnToUpdate + " = ? WHERE csvindex = ?", newValue, csvIndex);
			updatedCsvIndexes.add(csvIndex);
		}
		return updatedCsvIndexes;
	}

	@Override
	public int getResultEntriesCount(String selectIntStatement) {
		return select(logger, selectIntStatement, Integer.class);
	}

	@Override
	public List<Integer> getImportedCustomerIdsWithoutBindingToMailinglist(String temporaryImportTableName, int companyId, int datasourceId, int mailinglistId) {
		// First union part = newly created customers, second union part = already existing customers without binding on the specified mailinglist
		String selectCustomerIdsStatement =
			"SELECT customer_id FROM customer_" + companyId + "_tbl cust WHERE datasource_id = ?"
        		+ " AND NOT EXISTS (SELECT 1 FROM customer_" + companyId + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND bind.mailinglist_id = ?)"
        	+ " UNION ALL"
        	+ " SELECT DISTINCT customer_id FROM " + temporaryImportTableName + " temp WHERE (customer_id != 0 AND customer_id IS NOT NULL)"
        		+ " AND NOT EXISTS (SELECT 1 FROM customer_" + companyId + "_binding_tbl bind WHERE temp.customer_id = bind.customer_id AND bind.mailinglist_id = ?)";
			return select(logger, selectCustomerIdsStatement, IntegerRowMapper.INSTANCE, datasourceId, mailinglistId, mailinglistId);
	}

	@Override
	public int removeNewCustomersWithInvalidNullValues(int companyID, String tempTableName, String destinationTableName, List<String> keyColumns, List<String> columnsToInsert, String duplicateIndexColumn, List<ColumnMapping> columnMappingForDefaultValues) throws Exception {
		String notNullableDestinationColumnsPart = "";
		for (Entry<String, DbColumnType> entry : DbUtilities.getColumnDataTypes(getDataSource(), destinationTableName).entrySet()) {
			if (!entry.getValue().isNullable()
					&& DbUtilities.getColumnDefaultValue(getDataSource(), "customer_" + companyID + "_tbl", entry.getKey()) == null
					&& !"customer_id".equalsIgnoreCase(entry.getKey())
					&& !"gender".equalsIgnoreCase(entry.getKey())
					&& !"mailtype".equalsIgnoreCase(entry.getKey())) {
				String defaultValue = DbUtilities.getColumnDefaultValue(getDataSource(), destinationTableName, entry.getKey());
				if (StringUtils.isEmpty(defaultValue)) {
					if (notNullableDestinationColumnsPart.length() > 0) {
						notNullableDestinationColumnsPart += " OR ";
					}
					if (entry.getValue().getSimpleDataType() == SimpleDataType.Characters) {
						notNullableDestinationColumnsPart += "(" + entry.getKey() + " IS NULL OR " + entry.getKey() + " = '')";
					} else {
						notNullableDestinationColumnsPart += entry.getKey() + " IS NULL";
					}
				}
			}
		}
		
		int removedItems = 0;
		if (notNullableDestinationColumnsPart.length() > 0) {
			removedItems = retryableUpdate(companyID, logger, "DELETE FROM " + tempTableName + " WHERE (customer_id = 0 OR customer_id IS NULL) AND " + notNullableDestinationColumnsPart);
		}
		return removedItems;
	}

	/**
	 * Remove blacklisted entries (global blacklist is ignored to allow import of global blacklisted entries to companies blacklist)
	 **/
	@Override
	public int removeBlacklistedEmails(String tempTableName, int companyID) {
		// Remove emails by exact match (excludes blacklist entries with "%" and "*")
		int blacklistedEntries = retryableUpdate(companyID, logger, "DELETE FROM " + tempTableName + " WHERE EXISTS (SELECT 1 FROM cust" + companyID + "_ban_tbl ban WHERE " + tempTableName + ".email = ban.email AND NOT ban.email LIKE '%*%' AND NOT ban.email LIKE '%|%%' ESCAPE '|')");
		
		// Remove emails by wildcard match
		List<String> blacklistPatterns = select(logger, "SELECT email FROM cust" + companyID + "_ban_tbl WHERE email LIKE '%|%%' escape '|' OR email LIKE '%*%'", StringRowMapper.INSTANCE);
		for (String blacklistPattern : blacklistPatterns) {
			// Preserves "?" as regular character (non-wilcard), escapes "_" not to be a wildcard, replaces "*" by "%", escape the escape char
			final String likePattern = blacklistPattern.replace("_", "\\_").replace('*', '%').replace("|", "||");
			
			final String sql = String.format("DELETE FROM %s WHERE email LIKE ? ESCAPE '|'", tempTableName);
			
			blacklistedEntries += retryableUpdate(companyID, logger, sql, likePattern);
		}
		
		return blacklistedEntries;
	}

	@Override
	public CaseInsensitiveMap<String, DbColumnType> getCustomerDbFields(int companyId) throws Exception {
		return DbUtilities.getColumnDataTypes(getDataSource(), "customer_" + companyId + "_tbl");
	}

	@Override
	public boolean checkUnboundCustomersExist(int companyID) {
		return selectInt(logger, "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust WHERE NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.customer_id = cust.customer_id AND " + ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD + " = 0)") > 0;
	}

	@Override
	public int getAllRecipientsCount(int companyID) {
		if (DbUtilities.checkIfTableExists(getDataSource(), "customer_" + companyID + "_tbl")) {
			return selectInt(logger, String.format("SELECT COUNT(*) FROM customer_" + companyID + "_tbl WHERE " + ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD + " = 0"));
		} else {
			return 0;
		}
	}

	@Override
	public int changeStatusInMailingListNotIncludedInTempData(String temporaryImportTableName, List<String> keyColumns, int companyID, int mailingListId, MediaTypes mediatype, int currentStatus, int updateStatus, String remark) throws Exception {
		// Check for valid UserStatus codes
		UserStatus.getUserStatusByID(currentStatus);
		UserStatus.getUserStatusByID(updateStatus);
		
		List<String> keycolumnParts = new ArrayList<>();
		for (String keyColumn : keyColumns) {
			keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
		}
		
		return retryableUpdate(companyID, logger, "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP WHERE user_status = ? AND mailinglist_id = ? AND mediatype = ? AND customer_id NOT IN ("
			+ "SELECT dst.customer_id FROM " + temporaryImportTableName + " src, customer_" + companyID + "_tbl dst WHERE " + StringUtils.join(keycolumnParts, " AND ") + ")", updateStatus, remark, currentStatus, mailingListId, mediatype == null ? MediaTypes.EMAIL.getMediaCode() : mediatype.getMediaCode());
	}

	@Override
	public void gatherTableStats(String tableName) {
		if (isOracleDB()) {
			String username = select(logger, "SELECT USER FROM DUAL", String.class);
			execute(logger,
				"begin\n"
				+ " dbms_stats.gather_table_stats(\n"
					+ " ownname => '" + username.toUpperCase() + "',\n"
					+ " tabname => '" + tableName.toUpperCase() + "',\n"
					+ " estimate_percent => 30,\n"
					+ " method_opt => 'for all columns size 254',\n"
					+ " cascade => true,\n"
					+ " no_invalidate => FALSE\n"
				+ " );\n"
				+ " end;"
			);
		}
	}

	@Override
	public void changeEmailColumnCollation(String temporaryImportTableName, String collation) {
		execute(logger, "ALTER TABLE " + temporaryImportTableName + " MODIFY email VARCHAR(150) COLLATE " + collation + " NOT NULL");
	}

	@Override
	public void updateColumnOfTemporaryCustomerImportTable(String temporaryImportTableName, String columnName, Object value) {
		update(logger, "UPDATE " + temporaryImportTableName + " SET " + columnName + "= ?", value);
	}

	@Override
	public int changeStatusInMailingListNotFoundInData(String temporaryImportTableName, List<String> keyColumns, int companyId, int mailingListId, int currentStatus, int updateStatus, List<UserType> inclusiveUserTypes, List<UserType> exclusiveUserTypes, String remark) throws Exception {
		// Check for valid UserStatus code
		UserStatus.getUserStatusByID(updateStatus);
		
		List<String> keycolumnParts = new ArrayList<>();
		for (String keyColumn : keyColumns) {
			keycolumnParts.add("src." + keyColumn + " = dst." + keyColumn + " AND src." + keyColumn + " IS NOT NULL");
		}
		
		String inclusiveUserTypesPart = "";
		if (inclusiveUserTypes != null && inclusiveUserTypes.size() > 0) {
			for (UserType userType : inclusiveUserTypes) {
				if (inclusiveUserTypesPart.length() > 0) {
					inclusiveUserTypesPart += ", ";
				}
				inclusiveUserTypesPart += "'" + userType.getTypeCode() + "'";
			}
			inclusiveUserTypesPart = " AND user_type IN (" + inclusiveUserTypesPart + ")";
		}
		
		String exclusiveUserTypesPart = "";
		if (exclusiveUserTypes != null && exclusiveUserTypes.size() > 0) {
			for (UserType userType : exclusiveUserTypes) {
				if (exclusiveUserTypesPart.length() > 0) {
					exclusiveUserTypesPart += ", ";
				}
				exclusiveUserTypesPart += "'" + userType.getTypeCode() + "'";
			}
			exclusiveUserTypesPart = " AND user_type NOT IN (" + exclusiveUserTypesPart + ")";
		}
		
		return retryableUpdate(companyId, logger, "UPDATE customer_" + companyId + "_binding_tbl SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP WHERE user_status = ?" + inclusiveUserTypesPart + exclusiveUserTypesPart + " AND mailinglist_id = ? AND customer_id NOT IN ("
			+ "SELECT dst.customer_id FROM " + temporaryImportTableName + " src, customer_" + companyId + "_tbl dst WHERE " + StringUtils.join(keycolumnParts, " AND ") + ")", updateStatus, remark, currentStatus, mailingListId);
	}
}
