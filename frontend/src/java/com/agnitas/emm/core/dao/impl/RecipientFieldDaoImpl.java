/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.dao.RecipientFieldDao;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class RecipientFieldDaoImpl extends BaseDaoImpl implements RecipientFieldDao {

	private static final int MAX_SORT_INDEX = 1000;

	private final ConfigService configService;

	public RecipientFieldDaoImpl(@Qualifier("dataSource") DataSource dataSource, @Autowired(required = false) JavaMailService javaMailService,
								 ConfigService configService) {
		super(dataSource, javaMailService);
		this.configService = configService;
	}

	@Override
	public List<String> getFieldNames(int companyId) {
		return select("SELECT col_name FROM customer_field_tbl WHERE company_id = ?", StringRowMapper.INSTANCE, companyId);
	}

	@Override
	public List<RecipientFieldDescription> getRecipientFields(int companyID) {
		// Get all existing recipient fields from database
		Map<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>();
		Map<String, String> defaultValues = DbUtilities.getColumnDefaultValues(dataSource, "customer_" + companyID + "_tbl");
		for (Entry<String, DbColumnType> fieldEntry : DbUtilities.getColumnDataTypes(dataSource, "customer_" + companyID + "_tbl").entrySet()) {
			RecipientFieldDescription recipientFieldDescription = new RecipientFieldDescription();
			
			recipientFieldDescription.setColumnName(fieldEntry.getKey().toLowerCase());
			recipientFieldDescription.setShortName(fieldEntry.getKey());
			
			SimpleDataType simpleDataType = fieldEntry.getValue().getSimpleDataType();
			String databaseDataType = fieldEntry.getValue().getTypeName();
			if (isOracleDB()) {
				// Some Oracle DATE fields should be displayed with time
				if (fieldEntry.getKey().equalsIgnoreCase("creation_date")
						|| fieldEntry.getKey().equalsIgnoreCase("timestamp")
						|| fieldEntry.getKey().equalsIgnoreCase("lastclick_date")
						|| fieldEntry.getKey().equalsIgnoreCase("lastopen_date")
						|| fieldEntry.getKey().equalsIgnoreCase("lastsend_date")) {
					databaseDataType = "TIMESTAMP";
					simpleDataType = SimpleDataType.DateTime;
				}
			}
			recipientFieldDescription.setDatabaseDataType(databaseDataType);
			recipientFieldDescription.setSimpleDataType(simpleDataType);
			
			recipientFieldDescription.setCharacterLength(fieldEntry.getValue().getCharacterLength());
			recipientFieldDescription.setNumericPrecision(fieldEntry.getValue().getNumericPrecision());
			recipientFieldDescription.setNumericScale(fieldEntry.getValue().getNumericScale());
			recipientFieldDescription.setNullable(fieldEntry.getValue().isNullable());
			
			recipientFieldDescription.setDefaultValue(defaultValues.get(fieldEntry.getKey()));
			
			// Some fields are read only
			if (RecipientStandardField.getReadOnlyRecipientStandardFieldColumnNames().contains(recipientFieldDescription.getColumnName())) {
				recipientFieldDescription.getPermissions().put(0, ProfileFieldMode.ReadOnly);
			}
			
			recipientFieldsMap.put(fieldEntry.getKey(), recipientFieldDescription);
		}
		
		// Enrich database recipient fields by optionally defined description data
		select("SELECT col_name, shortname, description, mode_edit, field_sort, line, isinterest, historize, allowed_values, creation_date, change_date"
			+ " FROM customer_field_tbl"
			+ " WHERE company_id = ?"
			+ " ORDER BY field_sort, LOWER(shortname), LOWER(col_name)",
			new RecipientField_RowMapper(recipientFieldsMap), companyID);
		
		// Enrich database recipient fields by optionally defined admin permissions
		select("SELECT column_name, admin_id, mode_edit"
			+ " FROM customer_field_permission_tbl"
			+ " WHERE company_id = ?",
			new RecipientFieldPermission_RowMapper(recipientFieldsMap), companyID);
		
		return recipientFieldsMap
			.values()
			.stream()
			.sorted(Comparator.comparing(RecipientFieldDescription::isStandardField).reversed()
				.thenComparing(e -> e.getSortOrder())
				.thenComparing(e -> e.getShortName() == null ? "" : e.getShortName())
				.thenComparing(RecipientFieldDescription::getColumnName))
			.collect(Collectors.toList());
	}

    private class RecipientField_RowMapper implements RowMapper<RecipientFieldDescription> {
    	private Map<String, RecipientFieldDescription> recipientFieldsMap;
    	
    	public RecipientField_RowMapper(Map<String, RecipientFieldDescription> recipientFieldsMap) {
    		this.recipientFieldsMap = recipientFieldsMap;
    	}
    	
		@Override
		public RecipientFieldDescription mapRow(ResultSet resultSet, int row) throws SQLException {
			String columnName = resultSet.getString("col_name").toLowerCase();
			RecipientFieldDescription readRecipientFieldDescription = recipientFieldsMap.get(columnName);
			if (readRecipientFieldDescription == null) {
				throw new SQLException("RecipientField '" + columnName + "', which is defined in customer_field_tbl, does not exit in database");
			}
			
			if (StringUtils.isNotEmpty(resultSet.getString("shortname"))) {
				readRecipientFieldDescription.setShortName(resultSet.getString("shortname"));
			}
			readRecipientFieldDescription.setDescription(resultSet.getString("description"));
			
			try {
				ProfileFieldMode defaultPermission = ProfileFieldMode.getProfileFieldModeForStorageCode(resultSet.getInt("mode_edit"));

				// Some fields are read only
				if (defaultPermission != ProfileFieldMode.NotVisible && RecipientStandardField.getReadOnlyRecipientStandardFieldColumnNames().contains(columnName)) {
					defaultPermission = ProfileFieldMode.ReadOnly;
				}

				readRecipientFieldDescription.getPermissions().put(0, defaultPermission);
			} catch (Exception e) {
				throw new SQLException(e.getMessage(), e);
			}

			readRecipientFieldDescription.setHistorized(resultSet.getBoolean("historize"));

			Object sortObject = resultSet.getObject("field_sort");
			if (sortObject != null) {
				readRecipientFieldDescription.setSortOrder(((Number)sortObject).intValue());
			} else {
				readRecipientFieldDescription.setSortOrder(MAX_SORT_INDEX);
			}

			Object lineObject = resultSet.getObject("line");
			if (lineObject != null) {
				readRecipientFieldDescription.setLine(((Number)lineObject).intValue());
			} else {
				readRecipientFieldDescription.setLine(0);
			}

			Object interestObject = resultSet.getObject("isinterest");
			if (interestObject != null) {
				readRecipientFieldDescription.setInterest(((Number)interestObject).intValue());
			} else {
				readRecipientFieldDescription.setInterest(0);
			}

			String allowedValuesJson = resultSet.getString("allowed_values");
			List<String> allowedValues = null;
			if (StringUtils.isNotBlank(allowedValuesJson)) {
				allowedValues = new ArrayList<>();
				try {
					JSONArray array = new JSONArray(allowedValuesJson);
					for (int i = 0; i < array.length(); i++) {
						allowedValues.add(array.getString(i));
					}
				} catch (JSONException e) {
					logger.error("Error occurred while parsing JSON: " + e.getMessage(), e);
				}
			}
			readRecipientFieldDescription.setAllowedValues(allowedValues);

			readRecipientFieldDescription.setCreationDate(resultSet.getTimestamp("creation_date"));
			readRecipientFieldDescription.setChangeDate(resultSet.getTimestamp("change_date"));

			return readRecipientFieldDescription;
		}
	}

    private static class RecipientFieldPermission_RowMapper implements RowMapper<RecipientFieldDescription> {
    	private Map<String, RecipientFieldDescription> recipientFieldsMap;
    	
    	public RecipientFieldPermission_RowMapper(Map<String, RecipientFieldDescription> recipientFieldsMap) {
    		this.recipientFieldsMap = recipientFieldsMap;
    	}
    	
		@Override
		public RecipientFieldDescription mapRow(ResultSet resultSet, int row) throws SQLException {
			String columnName = resultSet.getString("column_name").toLowerCase();
			RecipientFieldDescription readRecipientFieldDescription = recipientFieldsMap.get(columnName);

			int adminID = ((Number) resultSet.getObject("admin_id")).intValue();
			ProfileFieldMode profileFieldMode;
			try {
				profileFieldMode = ProfileFieldMode.getProfileFieldModeForStorageCode(resultSet.getInt("mode_edit"));
			} catch (Exception e) {
				throw new SQLException("Invalid profilemode value for adminid " + adminID + " for column '" + columnName + "'", e);
			}

			readRecipientFieldDescription.getPermissions().put(adminID, profileFieldMode);
			
			return readRecipientFieldDescription;
		}
    }

	@Override
	public void saveRecipientField(int companyID, RecipientFieldDescription recipientFieldDescription) throws Exception {
		String columnName = selectWithDefaultValue("SELECT col_name FROM customer_field_tbl WHERE company_id = ? AND (LOWER(shortname) = ? OR LOWER(col_name) = ?)",
			String.class, null, companyID, recipientFieldDescription.getShortName().toLowerCase(), recipientFieldDescription.getColumnName().toLowerCase());
		boolean hasDescription;
		if (columnName == null) {
			hasDescription = false;
			columnName = recipientFieldDescription.getColumnName().toLowerCase();
		} else {
			hasDescription = true;
			columnName = columnName.toLowerCase();
		}
		
		DbColumnType dbColumnType = DbUtilities.getColumnDataType(dataSource, "customer_" + companyID + "_tbl", columnName);

		boolean allowSafeHtmlTags = configService.allowHtmlInReferenceAndProfileFields(companyID);
		HtmlChecker.checkTags("fieldDefault", recipientFieldDescription.getDefaultValue(), allowSafeHtmlTags);
		HtmlChecker.checkTags(getSerializedAllowedValues(recipientFieldDescription), allowSafeHtmlTags);

		if (dbColumnType == null) {
			// Create column in database table
			boolean success = DbUtilities.addColumnToDbTable(dataSource, "customer_" + companyID + "_tbl",
				recipientFieldDescription.getColumnName(),
				recipientFieldDescription.getDatabaseDataType(),
				recipientFieldDescription.getCharacterLength(),
				StringUtils.isEmpty(recipientFieldDescription.getDefaultValue()) ? null : recipientFieldDescription.getDefaultValue(),
				new SimpleDateFormat(DateUtilities.YYYY_MM_DD),
				!recipientFieldDescription.isNullable());
			
			if (!success) {
				throw new Exception("Creation of new database profilefield failed");
			}

			// Create description data entry
			ProfileFieldMode fallbackProfileFieldMode = (recipientFieldDescription.getPermissions() == null || recipientFieldDescription.getPermissions().get(0) == null) ? ProfileFieldMode.Editable : recipientFieldDescription.getPermissions().get(0);
			update("INSERT INTO customer_field_tbl (company_id, col_name, shortname, description, mode_edit, field_sort, line, isinterest, historize, allowed_values, creation_date, change_date)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				companyID,
				recipientFieldDescription.getColumnName().toLowerCase(),
				recipientFieldDescription.getShortName(),
				recipientFieldDescription.getDescription(),
				fallbackProfileFieldMode.getStorageCode(),
				recipientFieldDescription.getSortOrder(),
				recipientFieldDescription.getLine(),
				recipientFieldDescription.getInterest(),
				recipientFieldDescription.isHistorized(),
				getSerializedAllowedValues(recipientFieldDescription),
				new Date(),
				new Date()
			);
			
			// Create recipient field admin permissions
			updateRecipientFieldPermissions(companyID, recipientFieldDescription, columnName, fallbackProfileFieldMode);
		} else {
			// Update column in database table
			if (dbColumnType.getSimpleDataType() != recipientFieldDescription.getSimpleDataType()) {
				throw new Exception("Modification of recipient field type is not supported");
			} else if (dbColumnType.getSimpleDataType() == SimpleDataType.Characters && dbColumnType.getCharacterLength() > recipientFieldDescription.getCharacterLength()) {
				throw new Exception("Decrease of recipient field type length is not supported");
			}
			
			String defaultValue = DbUtilities.getColumnDefaultValue(dataSource, "customer_" + companyID + "_tbl", recipientFieldDescription.getColumnName());
			if ((defaultValue == null && defaultValue != null)
				|| (defaultValue != null && !defaultValue.equals(recipientFieldDescription.getDefaultValue()))
				|| dbColumnType.isNullable() != recipientFieldDescription.isNullable()) {
				DbUtilities.alterColumnDefaultValueInDbTable(dataSource, "customer_" + companyID + "_tbl", recipientFieldDescription.getColumnName(), recipientFieldDescription.getDefaultValue(), new SimpleDateFormat(DateUtilities.YYYY_MM_DD), !recipientFieldDescription.isNullable());
			}
			
			if (dbColumnType.getSimpleDataType() == SimpleDataType.Characters && dbColumnType.getCharacterLength() < recipientFieldDescription.getCharacterLength()) {
				DbUtilities.alterColumnTypeInDbTable(dataSource, "customer_" + companyID + "_tbl", recipientFieldDescription.getColumnName(), "VARCHAR", (int) recipientFieldDescription.getCharacterLength(), 0, defaultValue, null, !dbColumnType.isNullable());
			}
			
			if (hasDescription) {
				// Create or update description data
				ProfileFieldMode fallbackProfileFieldMode = (recipientFieldDescription.getPermissions() == null || recipientFieldDescription.getPermissions().get(0) == null) ? ProfileFieldMode.Editable : recipientFieldDescription.getPermissions().get(0);
				update("UPDATE customer_field_tbl SET shortname = ?, description = ?,"
					+ " mode_edit = ?, field_sort = ?, line = ?, isinterest = ?,"
					+ " historize = ?, allowed_values = ?, change_date = ?"
					+ " WHERE company_id = ? AND LOWER(col_name) = ?",
					recipientFieldDescription.getShortName(),
					recipientFieldDescription.getDescription(),
					fallbackProfileFieldMode.getStorageCode(),
					recipientFieldDescription.getSortOrder(),
					recipientFieldDescription.getLine(),
					recipientFieldDescription.getInterest(),
					recipientFieldDescription.isHistorized(),
					getSerializedAllowedValues(recipientFieldDescription),
					new Date(),
					companyID,
					recipientFieldDescription.getColumnName().toLowerCase());

				// Create or update recipient field admin permissions
				updateRecipientFieldPermissions(companyID, recipientFieldDescription, columnName, fallbackProfileFieldMode);
			} else {
				// Update description data
				ProfileFieldMode fallbackProfileFieldMode = (recipientFieldDescription.getPermissions() == null || recipientFieldDescription.getPermissions().get(0) == null) ? ProfileFieldMode.Editable : recipientFieldDescription.getPermissions().get(0);
				update("INSERT INTO customer_field_tbl (company_id, col_name, shortname, description, mode_edit, field_sort, line, isinterest, historize, allowed_values, creation_date, change_date)"
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
					companyID,
					recipientFieldDescription.getColumnName().toLowerCase(),
					recipientFieldDescription.getShortName(),
					recipientFieldDescription.getDescription(),
					fallbackProfileFieldMode.getStorageCode(),
					recipientFieldDescription.getSortOrder(),
					recipientFieldDescription.getLine(),
					recipientFieldDescription.getInterest(),
					recipientFieldDescription.isHistorized(),
					getSerializedAllowedValues(recipientFieldDescription),
					new Date(),
					new Date()
				);

				// Create or update recipient field admin permissions
				updateRecipientFieldPermissions(companyID, recipientFieldDescription, columnName, fallbackProfileFieldMode);
			}
		}
	}

	private void updateRecipientFieldPermissions(int companyID, RecipientFieldDescription recipientFieldDescription, String columnName, ProfileFieldMode fallbackProfileFieldMode) {
		List<Object[]> parameterList = new ArrayList<>();
		if (recipientFieldDescription.getPermissions() != null) {
			// Some old cases of profilefield col_name may be stored in uppercse and cannot be changed because of FK relationships. Therefore use the stored name's letter case.
			String storedColumnName = select("SELECT col_name FROM customer_field_tbl WHERE company_id = ? AND LOWER(col_name) = ?", String.class, companyID, columnName.toLowerCase());
			
			update("DELETE FROM customer_field_permission_tbl WHERE company_id = ? AND LOWER(column_name) = ?", companyID, columnName.toLowerCase());
			
			for (Entry<Integer, ProfileFieldMode> permissionEntry : recipientFieldDescription.getPermissions().entrySet()) {
				if (permissionEntry.getKey() != 0 && fallbackProfileFieldMode != permissionEntry.getValue()) {
					parameterList.add(new Object[] { companyID, storedColumnName, permissionEntry.getKey(), permissionEntry.getValue().getStorageCode() });
				}
		    }
			if (!parameterList.isEmpty()) {
		        batchupdate("INSERT INTO customer_field_permission_tbl (company_id, column_name, admin_id, mode_edit) VALUES (?, ?, ?, ?)", parameterList);
			}
		}
	}

	private String getSerializedAllowedValues(RecipientFieldDescription recipientFieldDescription) {
		if (recipientFieldDescription.getAllowedValues() != null) {
            return new JSONArray(recipientFieldDescription.getAllowedValues()).toString();
		}

		return null;
	}

	@Override
	public void deleteRecipientField(int companyID, String recipientFieldName) {
		String columnName = selectWithDefaultValue("SELECT col_name FROM customer_field_tbl WHERE company_id = ? AND (LOWER(shortname) = ? OR LOWER(col_name) = ?)",
				String.class, null, companyID, recipientFieldName.toLowerCase(), recipientFieldName.toLowerCase());
		if (columnName != null) {
			update("DELETE FROM customer_field_permission_tbl WHERE company_id = ? AND LOWER(column_name) = ?", companyID, columnName.toLowerCase());
			update("DELETE FROM customer_field_tbl WHERE company_id = ? AND LOWER(col_name) = ?", companyID, columnName.toLowerCase());
			DbUtilities.dropColumnFromDbTable(dataSource, "customer_" + companyID + "_tbl", columnName.toLowerCase());
		} else {
			update("DELETE FROM customer_field_permission_tbl WHERE company_id = ? AND LOWER(column_name) = ?", companyID, recipientFieldName.toLowerCase());
			DbUtilities.dropColumnFromDbTable(dataSource, "customer_" + companyID + "_tbl", recipientFieldName.toLowerCase());
		}
	}

	@Override
	public boolean isReservedKeyWord(String fieldname) {
		if (isOracleDB()) {
			return DbUtilities.RESERVED_WORDS_ORACLE.contains(fieldname);
		} else {
			return DbUtilities.RESERVED_WORDS_MYSQL_MARIADB.contains(fieldname);
		}
	}

	@Override
	public boolean hasRecipients(int companyID) {
		return selectInt("SELECT COUNT(*) FROM customer_" + companyID + "_tbl") > 0;
	}

	@Override
	public boolean hasRecipientsWithNullValue(int companyID, String columnName) {
		return selectInt("SELECT COUNT(*) FROM customer_" + companyID + "_tbl WHERE columnName IS NULL") > 0;
	}
	
	@Override
	public final int countCustomerEntries(final int companyID) {
		return selectInt("SELECT COUNT(*) FROM customer_" + companyID + "_tbl");
	}
}
