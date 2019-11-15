/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.util.beanshell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import bsh.Interpreter;
import bsh.NameSpace;

/**
 * Factory for BeanShell interpreters.
 */
public final class BeanShellInterpreterFactory {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(BeanShellInterpreterFactory.class);
	
	/** JDBC DataSource. */
	private DataSource dataSource;

	/**
	 * Creates a BeanShell interpreter and populates profile fields of given recipient.
	 * 
	 * @param companyID company ID of recipient
	 * @param customerID ID of recipient
	 * 
	 * @return BeanShell interpreter
	 * 
	 * @throws Exception on errors preparing interpreter
	 */
	public final Interpreter createBeanShellInterpreter(final int companyID, final int customerID) throws Exception {
		final Interpreter interpreter = new Interpreter();
		final NameSpace beanShellNameSpace = interpreter.getNameSpace();

		if (companyID <= 0) {
			throw new IllegalArgumentException("Company ID <= 0");
		}
		
		try {
			populateProfileFields(beanShellNameSpace, companyID, customerID);

			// Register additional components
			beanShellNameSpace.importClass("org.agnitas.util.AgnUtils");
			beanShellNameSpace.importClass(BeanShellRuntimeUtils.class.getCanonicalName());
			beanShellNameSpace.importClass(Date.class.getCanonicalName());
			
			// add virtual column "sysdate"
			beanShellNameSpace.setTypedVariable("CURRENT_TIMESTAMP", Date.class, new Date(), null);

			return interpreter;
		} catch (final Exception e) {
			logger.error("Error in getBshInterpreter(): " + e.getMessage(), e);
		
			throw e;
		}
	}

	/**
	 * Populates profile fields in namespace of BeanShell interpreter.
	 * 
	 * @param nameSpace namespace
	 * @param companyID company ID of recipient
	 * @param customerID ID of recpient
	 *
	 * @throws Exception on errors populating profile fields
	 */
	private final void populateProfileFields(final NameSpace nameSpace, final int companyID, final int customerID) throws Exception {
		final String sqlStatement = String.format("SELECT * FROM customer_%d_tbl cust WHERE cust.customer_id = ?", companyID);

		try (final Connection connection = dataSource.getConnection()) {
			try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
				preparedStatement.setInt(1, customerID);

				try (final ResultSet resultSet = preparedStatement.executeQuery()) {
					final ResultSetMetaData resultMetaData = resultSet.getMetaData();

					if (resultSet.next()) {
						for (int i = 1; i <= resultMetaData.getColumnCount(); i++) {
							final String columnName = resultMetaData.getColumnName(i).toLowerCase();

							try {
								switch (resultMetaData.getColumnType(i)) {
								case java.sql.Types.BIGINT:
								case java.sql.Types.INTEGER:
								case java.sql.Types.NUMERIC:
								case java.sql.Types.SMALLINT:
								case java.sql.Types.TINYINT:
									if (resultSet.getObject(i) != null) {
										nameSpace.setTypedVariable(columnName, Integer.class, resultSet.getInt(i), null);
									} else {
										nameSpace.setTypedVariable(columnName, Integer.class, null, null);
									}
									break;
	
								case java.sql.Types.DECIMAL:
								case java.sql.Types.DOUBLE:
								case java.sql.Types.FLOAT:
									if (resultSet.getObject(i) != null) {
										nameSpace.setTypedVariable(columnName, Double.class, resultSet.getDouble(i), null);
									} else {
										nameSpace.setTypedVariable(columnName, Double.class, null, null);
									}
									break;
	
								case java.sql.Types.CHAR:
								case java.sql.Types.LONGVARCHAR:
								case java.sql.Types.VARCHAR:
									nameSpace.setTypedVariable(columnName, String.class, resultSet.getString(i), null);
									break;
	
								case java.sql.Types.DATE:
								case java.sql.Types.TIME:
								case java.sql.Types.TIMESTAMP:
									nameSpace.setTypedVariable(columnName, Date.class, resultSet.getTimestamp(i), null);
									break;
								default:
									logger.error("Ignoring: " + columnName);
								}
							} catch(final Exception e) {
								throw new Exception(String.format("Error accessing column %d ('%s')", i, columnName), e);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sets JDBC DataSource.
	 * 
	 * @param dataSource JDBC DataSource
	 */
	@Required
	public final void setDataSource(final DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource, "DataSource is null");
	}
}
