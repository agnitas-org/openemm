/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.database.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.agnitas.util.TimeoutLRUMap;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.database.DatabaseInformation;
import com.agnitas.emm.core.commons.database.DatabaseInformationException;
import com.agnitas.emm.core.commons.database.TooMuchColumnsException;
import com.agnitas.emm.core.commons.database.UnknownTableOrColumnException;

public class DatabaseInformationImpl implements DatabaseInformation {
	
	private final TimeoutLRUMap<TableAndColumn, Integer> columnLengthMap;

	/** JDBC data source. */
	private DataSource datasource;
	
	public DatabaseInformationImpl(final int capacity, final int timeoutInMillis) {
		this.columnLengthMap = new TimeoutLRUMap<>(capacity, timeoutInMillis);
	}
	
	@Override
	public int getColumnStringLength(String tableName, String columnName) throws DatabaseInformationException {
		columnName = columnName.toUpperCase();
		
		TableAndColumn tableAndColumn = new TableAndColumn(tableName, columnName);
		Integer columnLength = columnLengthMap.get(tableAndColumn);
		
		if(columnLength != null) {
			return columnLength;
		} else {
			try(Connection connection = datasource.getConnection()) {
				DatabaseMetaData metaData = connection.getMetaData();

				if (metaData.storesLowerCaseIdentifiers()) {
					tableName = tableName.toLowerCase();
				} else if (metaData.storesUpperCaseIdentifiers()) {
					tableName = tableName.toUpperCase();
				}

				try(ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
					if(!resultSet.next()) {
						throw new UnknownTableOrColumnException(tableName, columnName);
					}
					
					int length = resultSet.getInt("COLUMN_SIZE");		// According to JDBC API, "COLUMN_SIZE" has type "int"
					
					if(resultSet.next()) {
						throw new TooMuchColumnsException(tableName, columnName);
					}
					
					columnLengthMap.put(tableAndColumn, length);
					
					return length;
				}
			} catch(SQLException e) {
				throw new DatabaseInformationException(String.format("Error reading column information (table: %s, column: %s)", tableName, columnName), e);
			}
		}
	}

	
	/**
	 * Set JDBC data source.
	 * 
	 * @param dataSource JDBC data source
	 */
	@Required
	public void setDataSource(final DataSource dataSource) {
		this.datasource = dataSource;
	}
}
