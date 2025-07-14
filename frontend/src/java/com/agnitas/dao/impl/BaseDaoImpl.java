/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.sql.DataSource;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;

/**
 * Helper class which hides the dependency injection variables and eases some select and update actions and logging.
 * But still the datasource or the JdbcTemplate can be used directly if needed.
 * 
 * The logger of this class is not used for db actions to log, because it would hide the calling from the derived classes.
 * Therefore every simplified update and select method demands an logger delivered as parameter.
 */
public abstract class BaseDaoImpl {

	private static Integer MYSQL_MAXPACKETSIZE = null;
	
	protected static final String DISABLED_MAILINGLIST_QUERY = " (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?) ";

	protected final Logger logger = LogManager.getLogger(this.getClass());
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	/**
	 * Datasource to be used in this dao. JdbcTemplate uses this, too.
	 * This member variable should be set by dependency injectors like spring via the setDataSource method
	 */
	protected DataSource dataSource;
	
	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	private static String dbVendor = null;
	
	protected JavaMailService javaMailService;

	public BaseDaoImpl(DataSource dataSource, JavaMailService javaMailService) {
		this.dataSource = dataSource;
		this.javaMailService = javaMailService;
	}
	
	public BaseDaoImpl() {
		// Nothing to do
	}

	/**
	 * Dependency injection method
	 * @param dataSource to be used by this dao object
	 */
	public final void setDataSource(DataSource dataSource) {
		/*
		 * Keep this method final to avoid problems with
		 * overriding in subclasses!
		 */

		this.dataSource = dataSource;
	}
	
	/**
	 * Auxiliary method to execute some actions on the dataSource without using JdbcTemplate
	 * @return dataSource used by this dao object
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	public final void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	/**
	 * Getter method for the JdbcTemplate, which uses the cache of this object
	 * @return the cached JdbcTemplate used by this object
	 */
	protected final JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(getDataSource());
	}
	
	/**
	 * Checks the db vendor of the dataSource and caches the result for further usage
	 * @return true if db vendor of dataSource is Oracle, false if any other vendor (e.g. mysql)
	 */
	public final boolean isOracleDB() {
		/*
		 * Keep this method final to avoid problems with
		 * overriding in subclasses!
		 */
		
		return "oracle".equalsIgnoreCase(getDbVendorName());
	}
	
	protected final boolean isMariaDB() {
		/*
		 * Keep this method final to avoid problems with
		 * overriding in subclasses!
		 */
		
		return "mariadb".equalsIgnoreCase(getDbVendorName());
	}
	
	private String getDbVendorName() {
		if (dbVendor == null) {
			if (DbUtilities.checkDbVendorIsOracle(getDataSource())) {
				dbVendor = "oracle";
			} else if (DbUtilities.checkDbVendorIsMariaDB(getDataSource())) {
				dbVendor = "mariadb";
			} else {
				dbVendor = "mysql";
			}
		}
		
		return dbVendor;
	}

	/**
	 * Logs the sql statement. This is typically used before db executions.
	 * This method is also included in select and update methods of this class, so they don't have to be called explicitly
	 */
	protected void logSqlStatement(String statement, Object... parameter) {
		if (logger.isDebugEnabled()) {
			if (parameter != null && parameter.length > 0) {
				logger.debug(MessageFormat.format("SQL: {0}\nParameter: {1}", statement, getParameterStringList(parameter)));
			} else {
				logger.debug(MessageFormat.format("SQL: {0}", statement));
			}
		}
	}

	/**
	 * Logs the sql statement and an occurred error. This is typically used after db executions.
	 * This method is also included in select and update methods of this class, so they don't have to be called explicitly
	 */
	protected void logSqlError(Exception e, String statement, Object... parameter) {
		if (parameter != null && parameter.length > 0) {
			logger.error(MessageFormat.format("Error: {0}\nSQL: {1}\nParameter: {2}", e.getMessage(), statement, getParameterStringList(parameter)), e);
		} else {
			logger.error(MessageFormat.format("Error: {0}\nSQL: {1}", e.getMessage(), statement), e);
		}
		if (javaMailService != null) {
			javaMailService.sendExceptionMail(0, "SQL: " + statement + "\nParameter: " + getParameterStringList(parameter), e);
		} else {
			logger.error("Missing javaMailService. So no erroremail was sent.");
		}
	}
	
	protected String getParameterStringList(Object[] parameterArray) {
		if (parameterArray == null) {
			return null;
		} else {
			StringBuilder parameterStringList = new StringBuilder();
			for (Object parameter : parameterArray) {
				if (parameterStringList.length() > 0) {
					parameterStringList.append(", ");
				}
				if (parameter == null) {
					parameterStringList.append("NULL");
				} else if (parameter instanceof String) {
					parameterStringList.append("'");
					parameterStringList.append(parameter);
					parameterStringList.append("'");
				} else if (parameter instanceof Date) {
					parameterStringList.append(parameter.getClass().getName());
					parameterStringList.append(":'");
					parameterStringList.append(new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_MS).format(parameter));
					parameterStringList.append("'");
				} else {
					parameterStringList.append(parameter.toString());
				}
			}
			return parameterStringList.toString();
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 *
	 * @return List of db entries represented as caseinsensitive maps
	 */
	protected List<Map<String, Object>> select(String statement, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().queryForList(statement, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * 
	 * @return List of db entries represented as objects
	 */
	protected <T> List<T> select(String statement, RowMapper<T> rowMapper, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().query(statement, rowMapper, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 *
	 * @param statement an sql-query to execute.
	 * @param rowHandler a callback object for results processing.
	 * @param parameter bound parameters for an sql-query.
	 */
	protected void query(String statement, RowCallbackHandler rowHandler, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			getJdbcTemplate().query(statement, rowHandler, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * 
	 * @return List of db entries represented as caseinsensitive maps
	 */
	protected Map<String, Object> selectSingleRow(String statement, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().queryForMap(statement, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * 
	 * @return single db entry as object
	 */
	protected <T> T select(String statement, Class<T> requiredType, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().queryForObject(statement, requiredType, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * If the searched entry does not exist an DataAccessException is thrown.
	 * 
	 * @return single db entry as object
	 */
	protected <T> T selectObject(String statement, RowMapper<T> rowMapper, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().queryForObject(statement, rowMapper, parameter);
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	protected <K, V> Map<K, V> selectMap(String statement, RowMapper<Tuple<K, V>> rowMapper, Object ... params) {
		List<Tuple<K, V>> resultList = select(statement, rowMapper, params);

		Map<K, V> map = new HashMap<>();
		for (int index = 0; index < resultList.size(); index++) {
			Tuple<K, V> tuple = resultList.get(index);
			map.put(tuple.getFirst(), tuple.getSecond());
		}
		return map;
	}

	protected String selectStringDefaultNull(String statement, Object ... parameters) {
		return selectObjectDefaultNull(statement, StringRowMapper.INSTANCE, parameters);
	}

	protected Integer selectIntDefaultNull(String statement, Object ... parameters) {
		return selectObjectDefaultNull(statement, IntegerRowMapper.INSTANCE, parameters);
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select for an object and logs error.
	 * If the searched entry does not exist, then null is returned as default value.
	 * If more than one item is found it throws an error saying so.
	 * 
	 * @return single db entry as object, null if no entry was found
	 */
	protected <T> T selectObjectDefaultNull(String statement, RowMapper<T> rowMapper, Object... parameter) {
		List<T> list = select(statement, rowMapper, parameter);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.isEmpty()) {
			return null;
		} else {
			throw new RuntimeException("Found invalid number of items: " + list.size());
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * 
	 * @return single db entry as int value or 0 in case of nothing was found
	 */
	protected int selectInt(String statement, Object... parameter) {
		return selectIntWithDefaultValue(statement, 0, parameter);
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 *
	 * @return single db entry as int value or 0 in case of nothing was found
	 */
	protected long selectLong(String statement, Object... parameter) {
		return selectLongWithDefaultValue(statement, 0L, parameter);
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * The given default value is returned, if the statement return an EmptyResultDataAccessException,
	 * which indicates that the selected value is missing and no rows are returned by DB.
	 * All other Exceptions are not touched and will be thrown in the usual way.
	 * 
	 * @return single db entry as int value, default value if not found
	 */
	protected int selectIntWithDefaultValue(String statement, int defaultValue, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			Integer value = getJdbcTemplate().queryForObject(statement, parameter, Integer.class);

			return value != null ? value : defaultValue;
		} catch (EmptyResultDataAccessException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("Empty result, using default value: {0}", defaultValue));
			}
			return defaultValue;
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * The given default value is returned, if the statement return an EmptyResultDataAccessException,
	 * which indicates that the selected value is missing and no rows are returned by DB.
	 * All other Exceptions are not touched and will be thrown in the usual way.
	 *
	 * @return single db entry as int value, default value if not found
	 */
	protected long selectLongWithDefaultValue(String statement, long defaultValue, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			Long value = getJdbcTemplate().queryForObject(statement, parameter, Long.class);
			
			return value != null ? value : defaultValue;
		} catch (EmptyResultDataAccessException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("Empty result, using default value: {0}", defaultValue));
			}
			return defaultValue;
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes select and logs error.
	 * 
	 * @return single db entry as object
	 */
	protected <T> T selectWithDefaultValue(String statement, Class<T> requiredType, T defaultValue, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			return getJdbcTemplate().queryForObject(statement, requiredType, parameter);
		} catch (EmptyResultDataAccessException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("Empty result, using default value: {0}", defaultValue));
			}
			return defaultValue;
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes update and logs error.
	 * 
	 * @return number of touched lines in db
	 */
	protected int update(String statement, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			int touchedLines = getJdbcTemplate().update(statement, parameter);
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("lines changed by update: {0}", touchedLines));
			}
			return touchedLines;
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}
	
	/**
	 * Logs the statement and parameter in debug-level, executes a DDL SQL Statement.
	 */
	protected void execute(String statement) {
		try {
			validateStatement(statement);
			logSqlStatement(statement);
			getJdbcTemplate().execute(statement);
		} catch (RuntimeException e) {
			logSqlError(e, statement);
			throw e;
		}
	}

	/**
	 * Execute DDL statements with retry on "busy resource",
	 * 
	 * @param timeoutSeconds (0 = EMM System default)
	 */
	protected void executeWithRetry(int timeoutSeconds, int maxNumberOfAttempts, int waitTimeSeconds, String statement) {
		try {
			validateStatement(statement);
			logSqlStatement(statement);
			JdbcTemplate jdbcTemplateWithTimeout = new JdbcTemplate(getDataSource());
			int defaultTimeout = jdbcTemplateWithTimeout.getQueryTimeout();
			
			int tryCount = 0;
			while (tryCount < maxNumberOfAttempts) {
				try {
					tryCount++;
					jdbcTemplateWithTimeout.setQueryTimeout(timeoutSeconds);
					jdbcTemplateWithTimeout.execute(statement);
					return;
				} catch (QueryTimeoutException qe) {
					if (tryCount < maxNumberOfAttempts) {
						logger.warn(MessageFormat.format("Execute statement reached timeout of {0} seconds. Attempt {1} of maximum {2}.\n{3}", timeoutSeconds, tryCount, maxNumberOfAttempts, statement));
					} else {
						String errorText = "Execute statement reached final timeout of " + timeoutSeconds + " seconds after " + tryCount + " attempts.\n" + statement;
						logger.error(errorText);
						if (javaMailService != null) {
							javaMailService.sendExceptionMail(0, "Execute statement reached final timeout of " + timeoutSeconds + " seconds after " + tryCount + " attempts.\nSQL: " + statement, new Exception());
						} else {
							logger.error("Missing javaMailService. So no erroremail was sent.");
						}
						throw qe;
					}
				} finally {
					jdbcTemplateWithTimeout.setQueryTimeout(defaultTimeout);
				}
				
				try {
					Thread.sleep(waitTimeSeconds * 1000L);
				} catch (InterruptedException e) {
					// Do nothing, just continue
				}
			}
		} catch (RuntimeException e) {
			logSqlError(e, statement);
			throw e;
		}
	}

	/**
	 * Logs the statement and parameter in debug-level, executes update, retrieves generated keys and logs error.
	 *
	 * @param statement an sql-query to execute.
	 * @param keys a holder for retrieved generated keys.
	 * @param keyColumns columns to retrieve as a generated keys.
	 * @param parameter bound parameters for an sql-query.
	 * @return number of touched lines in db.
	 */
	protected int update(String statement, KeyHolder keys, String[] keyColumns, Object... parameter) {
		try {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement, parameter);
			int touchedLines = getJdbcTemplate().update(connection -> {
				PreparedStatement ps = connection.prepareStatement(statement, keyColumns);
				for (int i = 0; i < parameter.length; i++) {
					ps.setObject(i + 1, parameter[i]);
				}
				return ps;
			}, keys);
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("lines changed by update: {0}", touchedLines));
			}
			return touchedLines;
		} catch (RuntimeException e) {
			logSqlError(e, statement, parameter);
			throw e;
		}
	}

	protected void checkMaximumDataSize(long datasize) {
		if (!isOracleDB() && getMysqlMaxPacketSize() < datasize) {
			// If MariaDB/MySQL's MaxPacketSize is to low for uploading this blob
			throw new IllegalArgumentException("Data is to big for storage in your database. Please rise db configuration value 'max_allowed_packet'. Current value: " + getMysqlMaxPacketSize() + " bytes Datasize: " + datasize + " bytes");
		}
	}

	protected void updateClob(String statement, String clobData, Object... parameter) {
		if (clobData == null) {
			try {
				validateStatement(statement);
				validateParameters(parameter);
				logSqlStatement(statement + " clobDataLength: NULL-Clob", parameter);
				Object[] parameterInclNull = new Object[parameter.length + 1];
				parameterInclNull[0] = null;
				for (int i = 0; i < parameter.length; i++) {
					parameterInclNull[i + 1] = parameter[i];
				}
				new JdbcTemplate(dataSource).update(statement, parameterInclNull);
			} catch(Exception e) {
				logSqlError(e, statement, "clobDataLength: NULL-Clob", parameter);
				throw e;
			}
		} else {
			validateStatement(statement);
			validateParameters(parameter);
			logSqlStatement(statement + " clobDataLength:" + clobData.length(), parameter);
			
			checkMaximumDataSize(clobData.getBytes(StandardCharsets.UTF_8).length);
			
			try (Reader clobDataReader = new StringReader(clobData)) {
				new JdbcTemplate(dataSource).execute(statement, new AbstractLobCreatingPreparedStatementCallback(new DefaultLobHandler()) {
					@Override
					protected void setValues(PreparedStatement preparedStatement, LobCreator lobCreator) throws SQLException {
						lobCreator.setClobAsCharacterStream(preparedStatement, 1, clobDataReader, clobData.length());
						int parameterIndex = 2;
						for (Object parameterObject : parameter) {
							preparedStatement.setObject(parameterIndex++, parameterObject);
						}
					}
				});
			} catch(Exception e) {
				logSqlError(e, statement, "clobDataLength:" + clobData.length(), parameter);
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Method to update the data of an blob.
	 * This method should be DB-Vendor independent.
	 * The update statement must contain at least one parameter for the blob data and this must be the first parameter within the statement.
	 * 
	 * Example: updateBlob(logger, "UPDATE tableName SET blobField = ? WHERE idField1 = ? AND idField2 = ?", blobDataArray, id1Object, id2Object);
	 * 
	 */
	protected void updateBlob(String statement, byte[] blobData, Object... parameter) {
		if (blobData == null) {
			updateBlob(statement, (InputStream) null, parameter);
		} else {
			updateBlob(statement, new ByteArrayInputStream(blobData), parameter);
		}
	}

	/**
	 * Method to update the data of an blob.
	 * This method should be DB-Vendor independent.
	 * The update statement must contain at least one parameter for the blob data and this must be the first parameter within the statement.
	 * 
	 * Example: updateBlob("UPDATE tableName SET blobField = ? WHERE idField1 = ? AND idField2 = ?", blobDataInputSTream, id1Object, id2Object);
	 *
	 */
	protected void updateBlob(String statement, InputStream blobDataInputStream, Object... parameter) {
		if (blobDataInputStream == null) {
			try {
				validateStatement(statement);
				validateParameters(parameter);
				logSqlStatement(statement + " blobDataLength: NULL-Blob", parameter);
				Object[] parameterInclNull = new Object[parameter.length + 1];
				parameterInclNull[0] = null;
				for (int i = 0; i < parameter.length; i++) {
					parameterInclNull[i + 1] = parameter[i];
				}
				new JdbcTemplate(dataSource).update(statement, parameterInclNull);
			} catch(Exception e) {
				logSqlError(e, statement, "blobDataLength: NULL-Blob", parameter);
				throw e;
			}
		} else {
			validateStatement(statement);
			validateParameters(parameter);
            try {
                logSqlStatement(statement + " blobDataLength:" + blobDataInputStream.available(), parameter);
				checkMaximumDataSize(blobDataInputStream.available());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

			try {
				new JdbcTemplate(dataSource).execute(statement, new AbstractLobCreatingPreparedStatementCallback(new DefaultLobHandler()) {
					@Override
					protected void setValues(PreparedStatement preparedStatement, LobCreator lobCreator) throws SQLException {
						try {
							lobCreator.setBlobAsBinaryStream(preparedStatement, 1, blobDataInputStream, blobDataInputStream.available());
						} catch (IOException e) {
							throw new SQLException("Cannot update blob data: " + e.getMessage(), e);
						}
						int parameterIndex = 2;
						for (Object parameterObject : parameter) {
							preparedStatement.setObject(parameterIndex++, parameterObject);
						}
					}
				});
			} finally {
				IOUtils.closeQuietly(blobDataInputStream);
			}
		}
	}

	/**
	 * Write the data of a blob into an outputstream.
	 * The selectBlobStatement must return a single column of type Blob.
	 * 
	 */
	protected void writeBlobInStream(String selectBlobStatement, OutputStream outputStream, Object... parameter) throws Exception {
		try {
			if (outputStream == null) {
				throw new IllegalArgumentException("outputStream is null");
			}

			validateStatement(selectBlobStatement);
			validateParameters(parameter);
			logSqlStatement(selectBlobStatement, parameter);
			Blob blob = getJdbcTemplate().queryForObject(selectBlobStatement, Blob.class, parameter);
			if (blob != null) {
				try (InputStream inputStream = blob.getBinaryStream()) {
					IOUtils.copy(inputStream, outputStream);
				}
			}
		} catch (RuntimeException e) {
			logSqlError(e, selectBlobStatement, parameter);
			throw e;
		}
	}

	/**
	 * Method to update multiple data entries at once.<br />
	 * Logs the statement and parameter in debug-level, executes update and logs error.<br />
	 * Watch out: Oracle returns value -2 (= Statement.SUCCESS_NO_INFO) per line for success with no "lines touched" info<br />
	 * 
	 */
	protected int[] batchupdate(String statement, List<Object[]> values) {
		try {
			validateStatement(statement);
			logSqlStatement(statement, "BatchUpdateParameterList(Size: " + values.size() + ")");
			int[] touchedLines = getJdbcTemplate().batchUpdate(statement, values);
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("lines changed by update: {0}", Arrays.toString(touchedLines)));
			}
			return touchedLines;
		} catch (RuntimeException e) {
			logSqlError(e, statement, "BatchUpdateParameterList(Size: " + values.size() + ")");

			if (!values.isEmpty() && values.get(0) != null) {
				logger.error(MessageFormat.format("Error: {0}\nSQL: {1}\nFirstBatchParameterList: {2}", e.getMessage(), statement, getParameterStringList(values.get(0))), e);
			}
			throw e;
		}
	}
	
	protected int[] batchInsertIntoAutoincrementMysqlTable(String insertStatement, List<Object[]> listOfValueArrays) throws Exception {
		try {
			validateStatement(insertStatement);
			logSqlStatement(insertStatement, "BatchInsertParameterList(Size: " + listOfValueArrays.size() + ")");
			
			List<Integer> generatedKeys = new ArrayList<>();

			try (final Connection connection = getDataSource().getConnection()) {
				try (final PreparedStatement preparedStatement = connection.prepareStatement(insertStatement, Statement.RETURN_GENERATED_KEYS)) {
					for (Object[] valueArray : listOfValueArrays) {
						int parameterIndex = 1;
						for (Object value : valueArray) {
							preparedStatement.setObject(parameterIndex++, value);
						}
						preparedStatement.addBatch();
					}
					preparedStatement.executeBatch();
					
					try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
						while (resultSet.next()) {
							generatedKeys.add(resultSet.getInt(1));
						}
					}
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug(MessageFormat.format("keys inserted by batch insert: {0}", StringUtils.join(generatedKeys, ", ")));
			}
			return generatedKeys.stream().mapToInt(i->i).toArray();
		} catch (RuntimeException e) {
			logSqlError(e, insertStatement, "BatchInsertParameterList(Size: " + listOfValueArrays.size() + ")");
			throw e;
		}
	}
	
	protected int[] batchInsertIntoAutoincrementMysqlTable(String autoincrementColumn, String insertStatement, List<Object[]> parametersList) {
		try {
			validateStatement(insertStatement);
			logSqlStatement(insertStatement, "BatchInsertParameterList(Size: " + parametersList.size() + ")");
			
			Object[] parameter = parametersList.isEmpty() ? new Object[0] : parametersList.get(0);
			int[] insertParameterTypes = new int[parameter.length];
			for (int i = 0; i < parameter.length; i++) {
				if (parameter[i] == null) {
					insertParameterTypes[i] = Types.NULL;
				} else if (parameter[i] instanceof Integer) {
					insertParameterTypes[i] = Types.INTEGER;
				} else if (parameter[i] instanceof Long) {
					insertParameterTypes[i] = Types.BIGINT;
				} else if (parameter[i] instanceof Boolean boolParam) {
					insertParameterTypes[i] = Types.INTEGER;
					parameter[i] = boolParam ? 1: 0;
				} else if (parameter[i] instanceof Double) {
					insertParameterTypes[i] = Types.DOUBLE;
				} else if (parameter[i] instanceof Float) {
					insertParameterTypes[i] = Types.FLOAT;
				} else if (parameter[i] instanceof String) {
					insertParameterTypes[i] = Types.VARCHAR;
				} else if (parameter[i] instanceof Date) {
					insertParameterTypes[i] = Types.TIMESTAMP;
				} else if (parameter[i] instanceof byte[]) {
					if (isMariaDB()) {
						insertParameterTypes[i] = Types.BINARY;
					} else {
						insertParameterTypes[i] = Types.BLOB;
					}
				} else {
					throw new RuntimeException("Invalid parameter type for insert in autoincrement table (Value: \"" + parameter[i] + "\", " + parameter[i].getClass().getSimpleName() + ")");
				}
			}
			
			BatchSqlUpdate batchSqlUpdate = new BatchSqlUpdate(getDataSource(), insertStatement, insertParameterTypes);
			batchSqlUpdate.setGeneratedKeysColumnNames(autoincrementColumn);
			batchSqlUpdate.setReturnGeneratedKeys(true);
			batchSqlUpdate.compile();
			GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
			int touchedLines = 0;
			int size = parametersList.size();
			batchSqlUpdate.setBatchSize(size);
			int[] generatedKeys = new int[size];
			for (int i = 0; i < size; i++) {
				int touchedLine = batchSqlUpdate.update(parametersList.get(i), generatedKeyHolder);
				touchedLines += touchedLine;
				generatedKeys[i] = generatedKeyHolder.getKey().intValue();
			}
			batchSqlUpdate.flush();
			
			if (touchedLines != parametersList.size()) {
				throw new RuntimeException("Illegal insert result");
			}
			
			return generatedKeys;
		} catch (RuntimeException e) {
			logSqlError(e, insertStatement, "BatchInsertParameterList(Size: " + parametersList.size() + ")");
			throw e;
		}
	}

	public static int getValueOrDefaultFromNumberField(Number numberField, int defaultValue) {
		if (numberField == null) {
			return defaultValue;
		} else {
			return numberField.intValue();
		}
	}

	/**
	 * This method makes an insert into a mysql table with an autoincrement column.
	 * Logs the statement and parameter in debug-level, executes insert and logs error.
	 * 
	 * @return inserted id
	 */
	@DaoUpdateReturnValueCheck
	protected int insertIntoAutoincrementMysqlTable(String autoincrementColumn, String insertStatement, Object... parameter) {
		try {
			validateStatement(insertStatement);
			validateParameters(parameter);
			logSqlStatement(insertStatement + " autoincrement: " + autoincrementColumn, parameter);

			int[] insertParameterTypes = new int[parameter.length];
			for (int i = 0; i < parameter.length; i++) {
				if (parameter[i] == null) {
					insertParameterTypes[i] = Types.NULL;
				} else if (parameter[i] instanceof Integer) {
					insertParameterTypes[i] = Types.INTEGER;
				} else if (parameter[i] instanceof Long) {
					insertParameterTypes[i] = Types.BIGINT;
				} else if (parameter[i] instanceof Boolean) {
					insertParameterTypes[i] = Types.INTEGER;
					parameter[i] = ((Boolean) parameter[i]) ? 1: 0;
				} else if (parameter[i] instanceof Double) {
					insertParameterTypes[i] = Types.DOUBLE;
				} else if (parameter[i] instanceof Float) {
					insertParameterTypes[i] = Types.FLOAT;
				} else if (parameter[i] instanceof String) {
					insertParameterTypes[i] = Types.VARCHAR;
				} else if (parameter[i] instanceof Date) {
					insertParameterTypes[i] = Types.TIMESTAMP;
				} else if (parameter[i] instanceof byte[]) {
					if (isMariaDB()) {
						insertParameterTypes[i] = Types.BINARY;
					} else {
						insertParameterTypes[i] = Types.BLOB;
					}
				} else {
					throw new RuntimeException("Invalid parameter type for insert in autoincrement table (Value: \"" + parameter[i] + "\", " + parameter[i].getClass().getSimpleName() + ")");
				}
			}

			SqlUpdate sqlUpdate = new SqlUpdate(getDataSource(), insertStatement, insertParameterTypes);
			sqlUpdate.setReturnGeneratedKeys(true);
			sqlUpdate.setGeneratedKeysColumnNames(autoincrementColumn);
			sqlUpdate.compile();
			GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

			int touchedLines = sqlUpdate.update(parameter, generatedKeyHolder);
			int autoincrementedValue = generatedKeyHolder.getKey().intValue();

			if (touchedLines != 1) {
				throw new RuntimeException("Illegal insert result");
			}

			return autoincrementedValue;
		} catch (RuntimeException e) {
			logSqlError(e, insertStatement + " autoincrement: " + autoincrementColumn, parameter);
			throw e;
		}
	}

	/**
	 * This method makes multiple inserts into a mysql table with an autoincrement column.
	 * Logs the statement and parameter in debug-level, executes insert and logs error.
	 * 
	 * @return number of touched lines in db
	 */
	@DaoUpdateReturnValueCheck
	protected int insertMultipleIntoAutoincrementMysqlTable(String autoincrementColumn, String insertStatement, Object... parameter) {
		try {
			validateStatement(insertStatement);
			validateParameters(parameter);
			logSqlStatement(insertStatement + " autoincrement: " + autoincrementColumn, parameter);

			int[] insertParameterTypes = new int[parameter.length];
			for (int i = 0; i < parameter.length; i++) {
				if (parameter[i] == null) {
					insertParameterTypes[i] = Types.NULL;
				} else if (parameter[i] instanceof Integer) {
					insertParameterTypes[i] = Types.INTEGER;
				} else if (parameter[i] instanceof Long) {
					insertParameterTypes[i] = Types.BIGINT;
				} else if (parameter[i] instanceof Boolean) {
					insertParameterTypes[i] = Types.INTEGER;
					parameter[i] = ((Boolean) parameter[i]) ? 1: 0;
				} else if (parameter[i] instanceof Double) {
					insertParameterTypes[i] = Types.DOUBLE;
				} else if (parameter[i] instanceof Float) {
					insertParameterTypes[i] = Types.FLOAT;
				} else if (parameter[i] instanceof String) {
					insertParameterTypes[i] = Types.VARCHAR;
				} else if (parameter[i] instanceof Date) {
					insertParameterTypes[i] = Types.TIMESTAMP;
				} else if (parameter[i] instanceof byte[]) {
					if (isMariaDB()) {
						insertParameterTypes[i] = Types.BINARY;
					} else {
						insertParameterTypes[i] = Types.BLOB;
					}
				} else {
					throw new RuntimeException("Invalid parameter type for insert in autoincrement table (Value: \"" + parameter[i] + "\", " + parameter[i].getClass().getSimpleName() + ")");
				}
			}

			SqlUpdate sqlUpdate = new SqlUpdate(getDataSource(), insertStatement, insertParameterTypes);
			sqlUpdate.setReturnGeneratedKeys(true);
			sqlUpdate.setGeneratedKeysColumnNames(autoincrementColumn);
			sqlUpdate.compile();
			GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
			
			return sqlUpdate.update(parameter, generatedKeyHolder);
		} catch (RuntimeException e) {
			logSqlError(e, insertStatement + " autoincrement: " + autoincrementColumn, parameter);
			throw e;
		}
	}

	protected String makeTimestampTruncateClause(String timestampExpression) {
		return (isOracleDB() ? "TRUNC(" : "DATE(") + timestampExpression + ")";
	}

	protected boolean checkIndicesAvailable(String... indicesToCheck) {
		if (indicesToCheck == null || indicesToCheck.length == 0) {
			return false;
		} else {
			String sqlCheckIndices;

			if (isOracleDB()) {
				sqlCheckIndices = "SELECT COUNT(*) FROM user_indexes WHERE LOWER(index_name) IN ('" + StringUtils.join(indicesToCheck, "','") + "')";
			} else {
				sqlCheckIndices = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE LOWER(index_name) IN ('" + StringUtils.join(indicesToCheck, "','") + "') AND table_schema = (SELECT DATABASE())";
			}

			int existingIndicesCount = selectInt(sqlCheckIndices);

			// All indices are required
			return existingIndicesCount == indicesToCheck.length;
		}
	}
	
	protected String makeBulkInClauseForString(final String columnName, Collection<String> values) {
		return makeBulkInClauseWithDelimiter(columnName, values, "'");
	}

	protected String makeBulkNotInClauseForInteger(final String columnName, Collection<Integer> values) {
		return DbUtilities.makeBulkInClauseWithDelimiter(isOracleDB(), columnName, values, "", false);
	}
	
	protected String makeBulkInClauseForInteger(final String columnName, Collection<Integer> values) {
		return makeBulkInClauseWithDelimiter(columnName, values, "");
	}
	
	private <T> String makeBulkInClauseWithDelimiter(String columnName, Collection<T> values, String delimiter) {
		return DbUtilities.makeBulkInClauseWithDelimiter(isOracleDB(), columnName, values, delimiter, true);
	}

	protected String getIsEmpty(String column) {
		return getIsEmpty(column, true);
	}

	protected String getIsNotEmpty(String column) {
		return getIsEmpty(column, false);
	}

	protected String getIsEmpty(String column, boolean positive) {
		if (isOracleDB()) {
			if (positive) {
				return column + " IS NULL";
			} else {
				return column + " IS NOT NULL";
			}
		} else {
			if (positive) {
				return "(" + column + " IS NULL OR " + column + " = '')";
			} else {
				return column + " > ''";
			}
		}
	}

	/**
	 * Check existence on rows with good performance
	 * @param query like "SELECT 1 FROM ..."
	 */
	protected boolean existsAtLeastOneRow(String query, Object... params){
		String existsQuery;
		if(isOracleDB()){
			existsQuery =
					"SELECT CASE" +
					" WHEN EXISTS (" + query + ")" +
					"  THEN 1" +
					" ELSE 0" +
					" END AS any_row_exists " +
					"FROM dual";
		} else {
			existsQuery = "SELECT EXISTS(" + query + ")";
		}
		return selectObject(existsQuery, (resultSet, i) -> resultSet.getBoolean(1), params);
	}
	
	private int getMysqlMaxPacketSize() {
		if (MYSQL_MAXPACKETSIZE == null) {
			MYSQL_MAXPACKETSIZE = DbUtilities.getMysqlMaxAllowedPacketSize(getDataSource());
		}
		
		return MYSQL_MAXPACKETSIZE;
	}

	protected void validateStatement(String statement) {
		if (StringUtils.isBlank(statement)) {
			throw new IllegalArgumentException("SQL statement is missing, empty or blank");
		}
	}

	protected void validateParameters(Object[] parameter) {
		if (parameter != null) {
			for (Object parameterObject : parameter) {
				if (parameterObject != null && parameterObject.getClass().isArray()) {
					throw new RuntimeException("Invalid usage of array as sql parameter");
				} else if (parameterObject != null && parameterObject.getClass().isEnum()) {
					throw new RuntimeException("Invalid usage of enum as sql parameter");
				}
			}
		}
	}

	protected String replaceWildCardCharacters(String searchQuery) {
		return searchQuery.replaceAll("\\*", "%").replaceAll("\\?", "_");
	}

	/**
	 * Replaces some sql control characters to escape sequence.
	 *
	 * @param value some string which should be escaped for sql
	 * @return escaped string
	 */
	protected String getEscapedValue(String value) {
		final Pattern regex = Pattern.compile("(?<filteredCharacter>[!%_])");
		final String replacement = "!${filteredCharacter}";

		return value.replaceAll(regex.pattern(), replacement);
	}

	protected boolean deleteByCompany(String tableName, int companyId) {
		String selectByCompanyQuery = String.format("SELECT COUNT(*) FROM %s WHERE company_id = ?", tableName);
		String deleteByCompanyQuery = String.format("DELETE FROM %s WHERE company_id = ?", tableName);
		update(deleteByCompanyQuery, companyId);
		return selectInt(selectByCompanyQuery, companyId) == 0;
	}
	
	protected String getUniqueCloneNameByNamePrefix(String tableName, String fieldName, String namePrefix, String condition, Object... parameters) {
		String prefix = DbUtilities.escapeLikeExpression(namePrefix, '\\');
	    String sqlGetMaxIndex;

	    if (StringUtils.isBlank(condition)) {
	    	condition = "";
		} else {
	    	condition = "(" + condition + ") AND ";
		}

        sqlGetMaxIndex = String.format("SELECT %s FROM %s WHERE %s %s like ?", fieldName, tableName, condition, fieldName);
		
		List<String> nameList = select(sqlGetMaxIndex, StringRowMapper.INSTANCE, ArrayUtils.add(parameters, prefix + "%"));
		
		return AgnUtils.findUniqueCloneName(nameList, prefix);
	}

	protected String getPartialSearchFilterWithAnd(String searchIn) {
		return getPartialSearchFilterWithAnd(searchIn, null, null);
	}

	protected String getPartialSearchFilterWithAnd(String searchIn, String text, List<Object> params) {
		return getPartialSearchFilter(searchIn, text, params, true);
	}

    protected String getPartialSearchFilterWithAnd(String searchIn, int number, List<Object> params) {
        params.add("%" + number + "%");
   		return " AND (" + searchIn + " IS NOT NULL AND CAST(" + searchIn + " AS CHAR(15)) LIKE ?)";
   	}

	protected String getPartialSearchFilter(String searchIn) {
		return getPartialSearchFilter(searchIn, null, null, false);
	}

	protected String getPartialSearchFilter(String searchIn, String text, List<Object> params) {
		return getPartialSearchFilter(searchIn, text, params, false);
	}

	private String getPartialSearchFilter(String searchIn, String text, List<Object> params, boolean prependAnd) {
		if (params != null) {
			params.add(DbUtilities.normalizeSqlLikeSearchPlaceholders(text));
		}
		String filter = isOracleDB()
				? "LOWER(" + searchIn + ") LIKE ('%' || LOWER(?) || '%')"
				: searchIn + " LIKE CONCAT('%', ?, '%')";
		return (prependAnd ? " AND " : " ") + filter;
	}

	protected String getDateRangeFilterWithAnd(String columnName, DateRange dateRange, List<Object> params) {
        return getDateRangeFilter(columnName, dateRange, params)
				.map(f -> " AND " + f)
				.orElse("");
	}

	protected Optional<String> getDateRangeFilter(String columnName, DateRange dateRange, List<Object> params) {
		if (StringUtils.isBlank(columnName)) {
			throw new IllegalArgumentException("Column name is missing!");
		}

		if (dateRange == null || !dateRange.isPresent()) {
			return Optional.empty();
		}

		List<String> conditions = new ArrayList<>(2);

		if (dateRange.getFrom() != null) {
			conditions.add(columnName + " >= ?");
			params.add(dateRange.getFrom());
		}
		if (dateRange.getTo() != null) {
			conditions.add(columnName + " < ?");
			params.add(DateUtilities.addDaysToDate(dateRange.getTo(), 1));
		}

		return Optional.of("(" + String.join(" AND ", conditions) + ")");
	}

	protected String addRowLimit(String query, Integer rowsCount) {
		if (rowsCount == null) {
			return query;
		}

		if (rowsCount < 0) {
			throw new IllegalArgumentException("Rows count is invalid!");
		}

		if (isOracleDB()) {
			return "SELECT * FROM (%s) WHERE ROWNUM <= %d".formatted(query, rowsCount);
		}

		return query + " LIMIT " + rowsCount;
	}
}
