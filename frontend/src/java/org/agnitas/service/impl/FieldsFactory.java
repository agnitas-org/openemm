/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportRecipientsDao;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.Form;
import org.apache.log4j.Logger;

public class FieldsFactory {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(FieldsFactory.class);
	
    public static final String CUSTOM_FIELD = "customField";
    public static final Map<String, String> mTypeColums = new LinkedHashMap <>();

    public CSVColumnState[] createColumnHeader(List<String> columnHeaderList, ImportProfile profile) {
        final CSVColumnState[] states = new CSVColumnState[columnHeaderList.size()];
        for (int i = 0; i < columnHeaderList.size(); i++) {
            String headerName = columnHeaderList.get(i);
            final String columnNameByCvsFileName = getDBColumnNameByCsvFileName(headerName, profile);
            if (columnNameByCvsFileName != null) {
                states[i] = new CSVColumnState();
                states[i].setColName(columnNameByCvsFileName);
                states[i].setImportedColumn(true);
            } else {
                states[i] = new CSVColumnState();
                states[i].setColName(headerName);
                states[i].setImportedColumn(false);
            }
        }
        return states;
    }

    public String getDBColumnNameByCsvFileName(String headerName, ImportProfile profile) {
        if (headerName == null) {
            return null;
        }

        for (ColumnMapping columnMapping : profile.getColumnMapping()) {
            if (headerName.equals(columnMapping.getFileColumn()) && !columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                return columnMapping.getDatabaseColumn();
            }
        }

        return null;
    }

    public void createRulesForCustomFields(CSVColumnState[] columns, Form form, ImportRecipientsDao importRecipientsDao, ImportProfile importProfile) {
        for (CSVColumnState column : columns) {
            if (!column.getImportedColumn()) {
                continue;
            }

            final String colName = column.getColName();
			Map<String, Object> columnInfo = importRecipientsDao.getColumnInfoByColumnName(importProfile.getCompanyId(), colName).get(colName);
			if (columnInfo == null) {
				logger.error("Invalid column '" + colName + "' for company " + importProfile.getCompanyId());
				throw new RuntimeException("Invalid column '" + colName + "' for company " + importProfile.getCompanyId());
			}
            final String typeOfCustomColumn = (String) columnInfo.get(ImportRecipientsDao.TYPE);

            final Field field = new Field();
            field.setProperty(colName);
            mTypeColums.put(colName,typeOfCustomColumn);
            if ("email".equals(colName)) {
                column.setType(CSVColumnState.TYPE_CHAR);
                field.setDepends("mandatory,checkRange,email");
                field.addVar("maxLength", "100", null);
            } else if ("gender".equals(colName)) {
                column.setType(CSVColumnState.TYPE_NUMERIC);
                field.setDepends("mandatory,gender");
            } else if ("mailtype".equals(colName)) {
                column.setType(CSVColumnState.TYPE_NUMERIC);
                field.setDepends("mandatory,mailType");
            } else if ("firstname".equals(colName)) {
                column.setType(CSVColumnState.TYPE_CHAR);
                field.setDepends("mandatory,checkRange");
                field.addVar("maxLength", "100", null);
            } else if ("lastname".equals(colName)) {
                column.setType(CSVColumnState.TYPE_CHAR);
                field.setDepends("mandatory,checkRange");
                field.addVar("maxLength", "100", null);
            } else if ("title".equals(colName)) {
                column.setType(CSVColumnState.TYPE_CHAR);
                field.setDepends("mandatory,checkRange");
                field.addVar("maxLength", "100", null);
            } else if (typeOfCustomColumn.equals(DataType.INTEGER)) {
                field.setDepends("mandatory,int");
                markFieldAsCustom(field);
                column.setType(CSVColumnState.TYPE_NUMERIC);
            } else if (typeOfCustomColumn.equals(DataType.DOUBLE)) {
                field.setDepends("mandatory,validateDouble");
                markFieldAsCustom(field);
                column.setType(CSVColumnState.TYPE_NUMERIC);
            } else if (typeOfCustomColumn.equals(DataType.CHAR)) {
                field.setDepends("mandatory,checkRange");
                field.addVar("maxLength", "1", null);
                markFieldAsCustom(field);
                column.setType(CSVColumnState.TYPE_CHAR);
            } else if (typeOfCustomColumn.equals(DataType.VARCHAR)) {
                field.setDepends("mandatory,checkRange");
                final Integer maxLength = (Integer) columnInfo.get("length");
                field.addVar("maxLength", String.valueOf(maxLength), null);
                markFieldAsCustom(field);
                column.setType(CSVColumnState.TYPE_CHAR);
            } else if (typeOfCustomColumn.equals(DataType.DATE)) {
                field.setDepends("mandatory,date");
                markFieldAsCustom(field);
                column.setType(CSVColumnState.TYPE_DATE);
            }

            form.addField(field);
        }
    }

    private void markFieldAsCustom(Field field) {
        field.addVar(CUSTOM_FIELD, Boolean.TRUE.toString(), null);
    }
}
