/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.io.Closeable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.agnitas.util.Log;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * Database abstraction layer
 */
public class DBase {
	private static final int DB_UNSET = 0;
	private static final int DB_MYSQL = 1;
	private static final int DB_ORACLE = 2;
	// general retry counter
	private static final int RETRY = 5;

	/**
	 * If not set from outside, Backend will try to create its own datasource from emm.properties data
	 */
	public static DataSource DATASOURCE = null;
	/**
	 * The column meassure must be written in different ways for oracle and mysql in sql statements
	 */
	public String measureType = null;
	public String measureRepr = null;

	/**
	 * name for current date in database
	 */
	private int dbType = DB_UNSET;
	private String nullQuery = null;
	/**
	 * Reference to configuration
	 */
	private Data data = null;
	/**
	 * Default jdbc access instance
	 */
	private NamedParameterJdbcTemplate jdbcTmpl = null;

	/**
	 * Wrap backend logging into a log4j appender and just log
	 * non informal messages
	 */
	static class DBaseFilter extends Filter {
		@Override
		public int decide(LoggingEvent e) {
			Level l = e.getLevel();

			if ((l == Level.WARN) || (l == Level.ERROR) || (l == Level.FATAL)) {
				return Filter.ACCEPT;
			}
			return Filter.NEUTRAL;
		}
	}

	static class DBaseAppender extends AppenderSkeleton {
		private Log log;

		public DBaseAppender(Log nLog) {
			super();
			log = nLog;
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		@Override
		public void close() {
			// nothing to do
		}

		@Override
		protected void append(LoggingEvent e) {
			Level l = e.getLevel();
			int lvl = -1;

			if (l == Level.WARN) {
				lvl = Log.WARNING;
			} else if (l == Level.ERROR) {
				lvl = Log.ERROR;
			} else if (l == Level.FATAL) {
				lvl = Log.FATAL;
			}
			if (lvl != -1) {
				String loggerName = e.getLoggerName();

				if ((loggerName == null) || loggerName.startsWith("org.springframework.jdbc")) {
					log.out(lvl, "jdbc", e.getRenderedMessage());
				}
			}
		}
	}

	/**
	 * Wrapper to create a data source and enforce auto commitment
	 */
	static class DBDatasource {
		private Map<String, DataSource> cache;
		private Set<String> seen;
		protected Log log;

		public DBDatasource() {
			cache = new HashMap<>();
			seen = new HashSet<>();
			log = new Log("jdbc", Log.INFO);

			Appender app = new DBaseAppender(log);

			app.addFilter(new DBaseFilter());
			BasicConfigurator.configure(app);
		}

		public DataSource newDataSource(String driver, String connect, String login, String password) {
			Properties p = new Properties();

			p.put("user", login);
			p.put("password", password);
			p.put("autocommit", "true");
			p.put("defaultautocommit", "true");
			p.put("autoreconnect", "true");
			return new DriverManagerDataSource(connect, p);
		}

		public synchronized DataSource request(String driver, String connect, String login, String password) throws ClassNotFoundException {
			DataSource rc = null;
			String key = driver + ";" + connect + ";" + login + ";*";

			if (cache.containsKey(key)) {
				rc = cache.get(key);
				log.out(Log.DEBUG, "rq", "Got exitsing DS for " + key);
			} else if (driver != null) {
				if (!seen.contains(driver)) {
					try {
						Class.forName(driver);
						seen.add(driver);
						log.out(Log.INFO, "rq", "Installed new driver for " + driver);
					} catch (ClassNotFoundException e) {
						log.out(Log.ERROR, "rq", "Failed to install driver " + driver, e);
						throw e;
					}
				}
				rc = newDataSource(driver, connect, login, password);
				cache.put(key, rc);
				log.out(Log.INFO, "rq", "Created new DS for " + key);
			}
			return rc;
		}
	}

	static class DBDatasourcePooled extends DBDatasource {
		private int dsPoolsize = 12;
		private boolean dsPoolgrow = true;

		public void setup(int poolsize, boolean poolgrow) {
			dsPoolsize = poolsize;
			dsPoolgrow = poolgrow;
		}

		@Override
		public DataSource newDataSource(String driver, String connect, String login, String password) {
			ObjectPool<Object> connectionPool = new GenericObjectPool<>(null, dsPoolsize, (dsPoolgrow ? GenericObjectPool.WHEN_EXHAUSTED_GROW : GenericObjectPool.WHEN_EXHAUSTED_BLOCK), 0);
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connect, login, password);

			// THIS IS REQUIRED DUE TO INTERNAL SIDE EFFECTS, DO NOT REMOVE THIS (AGAIN)!
			@SuppressWarnings("unused")
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
			log.out(Log.INFO, "nds", "New data source for " + driver + " using " + connect + " with " + login + " created (poolsize " + dsPoolsize + ", grow is " + dsPoolgrow + ")");
			return new PoolingDataSource(connectionPool);
		}
	}

	/**
	 * Simple base class for logging exceptions
	 */
	static class DBAccess {
		Data data;
		NamedParameterJdbcTemplate jdbc;

		public DBAccess(Data nData, NamedParameterJdbcTemplate nJdbc) {
			data = nData;
			jdbc = nJdbc;
		}

		public SQLException failure(String q, SQLException e) {
			data.logging(Log.ERROR, "dbase", "DB Failed: " + q + ": " + e.toString());
			return e;
		}
	}

	/**
	 * Wrapper class to allow logging of occured expections
	 */
	static class DBAccessSingle<T> extends DBAccess implements ResultSetExtractor<T> {
		public DBAccessSingle(Data nData, NamedParameterJdbcTemplate nJdbc) {
			super(nData, nJdbc);
		}

		@Override
		@SuppressWarnings("unchecked")
		public T extractData(ResultSet rs) throws SQLException, DataAccessException {
			if (rs.next()) {
				return (T) rs.getObject(1);
			}
			return null;
		}

		public T query(String q, Map<String, Object> packed) {
			return jdbc.query(q, packed, this);
		}

		public T query(String q) {
			return query(q, null);
		}
	}

	private static DBDatasourcePooled dsPool = new DBDatasourcePooled();

	/**
	 * Constructor for a database wrapper object
	 *
	 * @param data the global configuration
	 */
	private static Object lock = new Object();

	public DBase(Data data) throws ClassNotFoundException {
		this.data = data;

		synchronized (lock) {
			// Only create a new Datasource from emm.properties-Data, if none has been injected by EMM or OpenEMM
			dsPool.setup(data.dbPoolsize(), data.dbPoolgrow());
			if (DATASOURCE == null) {
				DATASOURCE = dsPool.request(data.dbDriver(), data.dbConnect(), data.dbLogin(), data.dbPassword());
			}
		}
		jdbcTmpl = null;
	}

	/**
	 * Cleanup, close open statements and database connection
	 *
	 * @trhows Exception
	 */
	public DBase done() throws Exception {
		jdbcTmpl = null;
		return null;
	}

	/**
	 * Try to determinate the type of database system
	 * in use and set some variables which depend on
	 * a specific database system
	 *
	 * @throws Exception
	 */
	public void setup() throws Exception {
		String dbms = data.dbMS;

		dbType = DB_MYSQL;
		if (dbms != null) {
			if (dbms.equals("oracle")) {
				dbType = DB_ORACLE;
			} else if (dbms.equals("mysql") || dbms.equals("mariadb")) {
				dbType = DB_MYSQL;
			} else {
				throw new Exception("Unsupported dbms found: " + dbms);
			}
		} else if (DATASOURCE != null) {
			boolean found = false;

			for (int retry = RETRY; (!found) && (retry > 0); --retry) {
				try (Connection c = DATASOURCE.getConnection()) {
					String product = c.getMetaData().getDatabaseProductName();

					if ((product != null) && (product.toLowerCase().indexOf("oracle") != -1)) {
						dbType = DB_ORACLE;
					}
					found = true;
				} catch (Exception e) {
					data.logging(Log.ERROR, "db", "Failed to get connection to determinate type of driver: " + e.toString(), e);
					if (retry > 0) {
						try {
							Thread.sleep(retry * 1000);
						} catch (InterruptedException e2) {
							data.logging(Log.ERROR, "db", "Failed to delay for " + retry + " seconds: " + e2.toString(), e2);
						}
					}
				}
			}
			if (!found) {
				throw new Exception("Failed to determinate database type");
			}
		} else {
			if (data.dbDriver() == null) {
				throw new Exception("No configured database driver found");
			}
			if ((data.dbDriver().toLowerCase().indexOf("mysql") == -1) && (data.dbDriver().toLowerCase().indexOf("mariadb") == -1)) {
				dbType = DB_ORACLE;
			}
		}

		if (dbType == DB_ORACLE) {
			measureType = "usage";
			nullQuery = "SELECT 1 FROM DUAL";
		} else {
			measureType = "`usage`";
			nullQuery = "SELECT 1";
		}
		measureRepr = "usage";
	}

	/**
	 * creates the default jdbc template for all accesses to the
	 * database, which do not colide with another connections
	 *
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		jdbcTmpl = validateJdbc(null);
	}

	/**
	 * wraps logging to be accessed from DAO
	 */
	public void logging(int loglvl, String mid, String msg, Throwable th) {
		data.logging(loglvl, mid + "/db", msg, th);
	}

	public void logging(int loglvl, String mid, String msg) {
		logging(loglvl, mid, msg, null);
	}

	/**
	 * if we are using an oracle database system, this method
	 * returns true, otherwise false
	 *
	 * @return true, if oracle, false otherwise
	 */
	public boolean isOracle() {
		return dbType == DB_ORACLE;
	}

	/**
	 * check if a table (case insensitive) exists in the current
	 * database schema
	 *
	 * @param table the name of the table
	 * @return true, if the table exists, false otherwise
	 */
	public boolean tableExists(String table) throws SQLException {
		boolean rc = false;
		String query = null;

		switch (dbType) {
			case DB_MYSQL:
				query = "SELECT count(*) FROM information_schema.tables WHERE lower(table_name) = lower(:tableName) AND table_schema=(SELECT SCHEMA())";
				break;
			case DB_ORACLE:
				query = "SELECT count(*) FROM user_tables WHERE lower(table_name) = lower(:tableName)";
				break;
			default:
				break;
		}
		if (query != null) {
			try (With with = with()) {
				rc = queryInt(with.jdbc(), query, "tableName", table) > 0;
			}
		}
		return rc;
	}

	/**
	 * check if a view (case insensitive) exists in the current
	 * database schema
	 *
	 * @param view the name of the view
	 * @return true, if the view exists, false otherwise
	 */
	public boolean viewExists(String view) throws SQLException {
		boolean rc = false;
		String query = null;

		switch (dbType) {
			case DB_MYSQL:
				query = "SELECT count(*) FROM information_schema.views WHERE lower(table_name) = lower(:viewName) AND table_schema=(SELECT SCHEMA())";
				break;
			case DB_ORACLE:
				query = "SELECT count(*) FROM user_views WHERE lower(view_name) = lower(:viewName)";
				break;
			default:
				break;
		}

		if (query != null) {
			try (With with = with()) {
				rc = queryInt(with.jdbc(), query, "viewName", view) > 0;
			}
		}
		return rc;
	}

	/**
	 * check if a synonym (case insensitive) exists in the current
	 * database schema
	 *
	 * @param synonym the name of the synonym
	 * @return true, if the synonym exists, false otherwise
	 */
	public boolean synonymExists(String synonym) throws SQLException {
		boolean rc = false;
		String query = null;

		switch (dbType) {
			case DB_ORACLE:
				query = "SELECT count(*) FROM user_synonyms WHERE lower(synonym_name) = lower(:synonymName)";
				break;
			default:
				break;
		}
		if (query != null) {
			try (With with = with()) {
				rc = queryInt(with.jdbc(), query, "synonymName", synonym) > 0;
			}
		}
		return rc;
	}
	
	/**
	 * check if a table name (case insensitive) exists in the current
	 * database schema (as table, view or synonym)
	 *
	 * @param table the name of the table
	 * @return true, if the table exists, false otherwise
	 */
	public boolean exists (String table) throws SQLException {
		return tableExists (table) || viewExists (table) || synonymExists (table);
	}

	/**
	 * returns the global jdbc instance
	 *
	 * @return the jdbc instance
	 */
	public NamedParameterJdbcTemplate jdbc() {
		return jdbcTmpl;
	}

	/**
	 * returns the global jdbc instance
	 * for invoking a specific query. This will log
	 * the query and its parameter (if this is a prepared
	 * statement with parameter).
	 *
	 * @param q     the query for which the connection will be used
	 * @param param the optional parameter for a prepared statement
	 * @return the jdbc instance
	 */
	public NamedParameterJdbcTemplate jdbc(String q, Map<String, Object> param) {
		show("JDB", q, param);
		return jdbc();
	}

	public NamedParameterJdbcTemplate jdbc(String q) {
		return jdbc(q, null);
	}

	static public class With implements Closeable {
		private NamedParameterJdbcTemplate jdbc;
		private DBase dbase;
		private String query;
		private Map<String, Object> param;

		protected With(NamedParameterJdbcTemplate nJdbc, DBase nDbase, String nQuery, Map<String, Object> nParam) {
			jdbc = nJdbc;
			dbase = nDbase;
			query = nQuery;
			param = nParam;
		}

		public NamedParameterJdbcTemplate jdbc() {
			return jdbc;
		}

		@Override
		public void close() {
			if (query != null) {
				dbase.release(jdbc, query, param);
			} else {
				dbase.release(jdbc);
			}
			jdbc = null;
		}
	}

	/**
	 * encapsulate a request within a closeable object
	 * to use it in a try-with construct
	 */
	public With with() throws SQLException {
		return new With(request(), this, null, null);
	}

	public With with(String query, Map<String, Object> param) throws SQLException {
		return new With(request(query, param), this, query, param);
	}

	public With with(String query) throws SQLException {
		return with(query, null);
	}

	/**
	 * requests a new jdbc template instance which will be created.
	 *
	 * @return the new jdbc instance
	 * @throws Exception
	 */
	public NamedParameterJdbcTemplate request() throws SQLException {
		return validateJdbc(null);
	}

	/**
	 * requests a new jdbc template instance which will be created for
	 * using the connection to execute query. This will be logged
	 * including the optional parameter for a prepared statement.
	 *
	 * @param param the parameter for a prepared statement
	 * @return the new jdbc instance
	 * @throws Exception
	 * @paran query the query the jdbc connection will be used
	 */
	public NamedParameterJdbcTemplate request(String query, Map<String, Object> param) throws SQLException {
		show("REQ", query, param);
		return request();
	}

	public NamedParameterJdbcTemplate request(String query) throws SQLException {
		return request(query, null);
	}

	/**
	 * releases a former requested jdbc template
	 *
	 * @param temp the jdbc instance to be released
	 * @return null
	 */
	public NamedParameterJdbcTemplate release(NamedParameterJdbcTemplate temp) {
		if (temp != jdbcTmpl) {
			temp = null;
		}
		return null;
	}

	/**
	 * releases a former requested jdbc template
	 * including the query for which the template had been used for logging.
	 *
	 * @param temp  the jdbc instance to be released
	 * @param query the query for which the template had been used
	 * @param param the optional parameter for a prepared statement
	 * @return null
	 */
	public NamedParameterJdbcTemplate release(NamedParameterJdbcTemplate temp, String query, Map<String, Object> param) {
		show("REL", query, param);
		return release(temp);
	}

	public NamedParameterJdbcTemplate release(NamedParameterJdbcTemplate temp, String query) {
		return release(temp, query, null);
	}

	public int queryInt(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doQueryInt(jdbc, q, pack(param));
	}

	public int queryInt(String q, Object... param) throws SQLException {
		return doQueryInt(jdbc(), q, pack(param));
	}

	public long queryLong(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doQueryLong(jdbc, q, pack(param));
	}

	public long queryLong(String q, Object... param) throws SQLException {
		return doQueryLong(jdbc(), q, pack(param));
	}

	public String queryString(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doQueryString(jdbc, q, pack(param));
	}

	public String queryString(String q, Object... param) throws SQLException {
		return doQueryString(jdbc(), q, pack(param));
	}

	public Map<String, Object> querys(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doQuerys(jdbc, q, pack(param));
	}

	public Map<String, Object> querys(String q, Object... param) throws SQLException {
		return doQuerys(jdbc(), q, pack(param));
	}

	public List<Map<String, Object>> query(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doQuery(jdbc, q, pack(param));
	}

	public List<Map<String, Object>> query(String q, Object... param) throws SQLException {
		return doQuery(jdbc(), q, pack(param));
	}

	public int update(NamedParameterJdbcTemplate jdbc, String q, Object... param) throws SQLException {
		return doUpdate(jdbc, q, pack(param));
	}

	public int update(String q, Object... param) throws SQLException {
		return doUpdate(jdbc(), q, pack(param));
	}

	public int update(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> param) throws SQLException {
		return doUpdate(jdbc, q, param);
	}

	public void execute(NamedParameterJdbcTemplate jdbc, String q) throws Exception {
		doExecute(jdbc, q);
	}

	public void execute(String q) throws Exception {
		doExecute(jdbc(), q);
	}

	/**
	 * Check the string for a minimum length or not all spaces,
	 * otherwise set it to null
	 *
	 * @param s         the string to validate
	 * @param minLength the minimal length required for the string
	 * @return the modified string
	 */
	public String validate(String s, int minLength) {
		if (s != null) {
			int len = s.length();

			if (len < minLength) {
				s = null;
			} else {
				int n;

				for (n = 0; n < len; ++n) {
					if (s.charAt(n) != ' ') {
						break;
					}
				}
				if (n == len) {
					s = null;
				}
			}
		}
		return s;
	}

	public String validate(String s) {
		return validate(s, 1);
	}

	public int asInt(Object o, int ifNull) {
		return o != null ? ((Number) o).intValue() : ifNull;
	}

	public int asInt(Object o) {
		return asInt(o, 0);
	}

	public long asLong(Object o, long ifNull) {
		return o != null ? ((Number) o).longValue() : ifNull;
	}

	public long asLong(Object o) {
		return asLong(o, 0L);
	}

	public String asString(Object o, int minLength, String ifNull, boolean trim) {
		String s = validate((String) o, minLength);

		return s != null ? (trim ? s.trim() : s) : ifNull;
	}

	public String asString(Object o, int minLength, String ifNull) {
		return asString(o, minLength, ifNull, false);
	}

	public String asString(Object o, int minLength) {
		return asString(o, minLength, null, false);
	}

	public String asString(Object o, String ifNull) {
		return asString(o, 1, ifNull, false);
	}

	public String asString(Object o, boolean trim) {
		return asString(o, 1, null, trim);
	}

	public String asString(Object o) {
		return asString(o, 1, null, false);
	}

	public String asClob(Object o) {
		if (o == null) {
			return null;
		} else if (o.getClass() == String.class) {
			return (String) o;
		} else {
			Clob clob = (Clob) o;

			try {
				return clob == null ? null : clob.getSubString(1, (int) clob.length());
			} catch (SQLException e) {
				failure("clob parse", e);
			}
			return null;
		}
	}

	public byte[] asBlob(Object o) {
		if (o == null) {
			return null;
		} else if (o.getClass().getName().equals("[B")) {
			return (byte[]) o;
		} else {
			Blob blob = (Blob) o;

			try {
				return blob == null ? null : blob.getBytes(1, (int) blob.length());
			} catch (SQLException e) {
				failure("blob parse", e);
			}
			return null;
		}
	}

	public Date asDate(Object o, Date ifNull) {
		return o != null ? (Date) o : ifNull;
	}

	public Date asDate(Object o) {
		return asDate(o, null);
	}

	public Timestamp asTimestamp(Object o, Timestamp ifNull) {
		return o != null ? (Timestamp) o : ifNull;
	}

	public Timestamp asTimestamp(Object o) {
		return asTimestamp(o, null);
	}

	/**
	 * packs an array of objects as typically passed via
	 * an argument list with variable number of elements
	 * into a name/object map
	 */
	public Map<String, Object> pack(Object[] param) {
		if ((param.length == 1) && (param[0] instanceof Map)) {
			return asMap(param[0]);
		}

		Map<String, Object> input = new HashMap<>(param.length / 2);

		for (int n = 0; n < param.length; n += 2) {
			input.put((String) param[n], param[n + 1]);
		}
		return input;
	}

	/**
	 * logs a failure and returns the exception passed to the method
	 * to allow something like ``throw failure ("...", e);''
	 */
	public SQLException failure(String q, SQLException e) {
		data.logging(Log.ERROR, "dbase", "DB Failed: " + q + ": " + e.toString());
		return e;
	}

	/**
	 * log a query to logfile on debug level
	 */
	public void show(String what, String query, Map<String, Object> param) {
		if ((query != null) && data.islog(Log.DEBUG)) {
			String m = what + ": " + query;

			if ((param != null) && (param.size() > 0)) {
				String sep = " { ";

				for (Map.Entry<String, Object> kv : param.entrySet()) {
					String key = kv.getKey();
					Object val = kv.getValue();
					String disp;

					if (val == null) {
						disp = "null";
					} else {
						try {
							Class<?> cls = val.getClass();

							if ((cls == String.class) || (cls == StringBuffer.class)) {
								disp = "\"" + val.toString() + "\"";
							} else if (cls == Character.class) {
								disp = "'" + val.toString() + "'";
							} else if (cls == Boolean.class) {
								disp = ((Boolean) val).booleanValue() ? "true" : "false";
							} else {
								disp = val.toString();
							}
						} catch (Exception e) {
							disp = "??? <" + e.toString() + ">";
						}
					}
					m += sep + key + "=" + disp;
					sep = ", ";
				}
				m += " }";
			}
			data.logging(Log.DEBUG, "dbase", m);
		}
	}

	public void showq(String what, String query, Object... param) {
		Map<String, Object> parsedParam = null;

		if (param.length > 0) {
			parsedParam = new HashMap<>();
			for (int n = 0; n < param.length; ++n) {
				parsedParam.put(":" + (n + 1), param[n]);
			}
		}
		show(what, query, parsedParam);
	}

	/**
	 * get a connection from the data source to directly operate on
	 * the connection
	 */
	public Connection getConnection() throws SQLException {
		return DATASOURCE.getConnection();
	}

	public Connection getConnection(String query, Object... param) throws SQLException {
		showq("CONN", query, param);
		return getConnection();
	}

	/**
	 * Class to implement a retry concept for database queries where
	 * recoverable queries are retried several times. To use this
	 * one have to subclass this class (anonymous is okay) and overwrite
	 * the method execute
	 */
	abstract public class Retry<T> {
		public String name;
		public DBase dbase;
		public NamedParameterJdbcTemplate jdbc;
		public T priv;
		public SQLException error;

		public Retry(String nName, DBase nDBase, NamedParameterJdbcTemplate nJdbc, T nPriv) {
			name = nName;
			dbase = nDBase;
			jdbc = nJdbc;
			priv = nPriv;
			error = null;
		}

		public Retry(String nName, DBase nDBase, NamedParameterJdbcTemplate nJdbc) {
			this(nName, nDBase, nJdbc, null);
		}

		public void reset() {
			priv = null;
		}

		public abstract void execute() throws SQLException;
	}

	/**
	 * executes the r.execute() method more than once, if a recoverable
	 * exception had occured ot overcome temporary problems during
	 * execution
	 *
	 * @return true, if execution was successful, false otherwise
	 * @paaram r the retry class to execute
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean retry(Retry r) {
		boolean rc = false;
		boolean rerun = true;
		boolean first = true;
		String logid = "db/" + r.name;

		while ((!rc) && rerun) {
			for (int state = RETRY - 1; (!rc) && (state >= 0); --state) {
				try {
					r.execute();
					if (r.error != null) {
						r.error = null;
						data.logging(Log.INFO, logid, "Execution now succeeded");
					}
					rc = true;
				} catch (Exception e) {
					boolean recoverable = recoverableErrors(e);

					r.error = e instanceof SQLException ? (SQLException) e : new SQLException(e.toString(), e);
					r.reset();
					if (first) {
						data.logging(recoverable ? Log.WARNING : Log.ERROR, logid, "Initial failure (" + (recoverable ? "" : "NOT ") + "recoverable): " + e.toString(), e);
					}
					rc = false;
					if (!recoverable) {
						rerun = false;
						break;
					}
					if (first) {
						first = false;
					} else {
						data.logging(state > 0 ? Log.WARNING : Log.ERROR, logid, "Failed to execute, " + (state > 0 ? "retry in " + state + " seconds" : "failed") + ": " + e.toString());
					}
					if (state > 0) {
						try {
							Thread.sleep(state * 1000);
						} catch (InterruptedException e2) {
							data.logging(Log.WARNING, logid, "Interrupted during delay: " + e2.toString());
						}
						if (r.jdbc != null) {
							NamedParameterJdbcTemplate jdbc = revalidateJdbc(r.jdbc);

							if (jdbc == null) {
								data.logging(Log.ERROR, logid, "Failed to revalidate jdbc connection");
								break;
							}
							data.logging(Log.DEBUG, logid, "Got new jdbc connection");
							if (r.jdbc == jdbcTmpl) {
								jdbcTmpl = jdbc;
							}
							r.jdbc = jdbc;
						}
					}
				}
			}
		}
		return rc;
	}

	/**
	 * checks if a chain of exceptions are all recoverable
	 * errors
	 *
	 * @param t a throwable to check
	 * @return true, if all occured exceptions had been recoverable, false otherwise
	 */
	private boolean recoverableErrors(Throwable t) {
		for (Throwable s : t.getSuppressed()) {
			if (recoverableError(s)) {
				return true;
			}
		}
		return recoverableError(t);
	}

	/**
	 * checks if an exception is considered as recoverable
	 *
	 * @param t the exception to check
	 * @return true, if the exception is considered as recoverable, false otherwise
	 */
	private boolean recoverableError (Throwable t) {
		Class <? extends Throwable>	cls = t.getClass ();
		
		if ((cls == org.springframework.jdbc.BadSqlGrammarException.class) ||
		    (cls == org.springframework.dao.DataIntegrityViolationException.class) ||
		    (cls == org.springframework.dao.DuplicateKeyException.class) ||
		    (cls == org.springframework.dao.PermissionDeniedDataAccessException.class) ||
		    (cls == org.springframework.dao.EmptyResultDataAccessException.class) ||
		    (cls == org.springframework.jdbc.UncategorizedSQLException.class)) {
			return false;
		}
		if ((t instanceof org.springframework.dao.DataAccessException) ||
		    (t instanceof org.springframework.remoting.RemoteAccessException) ||
		    (t instanceof org.springframework.transaction.TransactionException)) {
			return true;
		}
		if (t instanceof RuntimeException) {
			return false;
		}
		return true;
	}
	
	private NamedParameterJdbcTemplate revalidateJdbc (NamedParameterJdbcTemplate jdbc) {
		if (jdbc != null) {
			try {
				NamedParameterJdbcTemplate	newJdbc = request ();
					
				if (jdbc == jdbcTmpl) {
					jdbcTmpl = newJdbc;
					data.logging (Log.DEBUG, "db", "Replaced default jdbc connection");
				} else {
					data.logging (Log.DEBUG, "db", "Replaced local jdbc connection");
				}
				jdbc = newJdbc;
			} catch (Exception e) {
				data.logging (Log.ERROR, "db", "Failed to create replacement jdbc connection", e);
				jdbc = null;
			}
		}
		return jdbc;
	}

	private NamedParameterJdbcTemplate validateJdbc(NamedParameterJdbcTemplate jdbc) throws SQLException {
		Retry<NamedParameterJdbcTemplate> r = new Retry<NamedParameterJdbcTemplate>("jdbc", this, null, jdbc) {
			@Override
			public void execute() throws SQLException {
				int delay = 0;
				NamedParameterJdbcTemplate oldJdbc = null;

				do {
					if (priv == null) {
						priv = new NamedParameterJdbcTemplate(DATASOURCE);
						if (priv == null) {
							data.logging(Log.ERROR, "db", "Failed to get new connection");
							break;
						} else if (priv == oldJdbc) {
							data.logging(Log.ERROR, "db", "Got old, invalid connections while trying to get a new one, abort");
							break;
						}
					}
					try {
						(new DBAccessSingle<Integer>(data, priv)).query(nullQuery);
					} catch (Throwable t) {
						data.logging(Log.ERROR, "db", "connection invalid (" + t.toString() + "), reset " + (delay > 0 ? "after waitung " + delay + " seconds" : "now"));
						oldJdbc = priv;
						priv = null;
						if (delay > 0) {
							try {
								Thread.sleep(delay * 1000);
							} catch (InterruptedException e) {
								// do nothing
							}
						}
						if (delay < 30) {
							++delay;
						}
					}
				} while (priv == null);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw r.error;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object o) {
		return (Map<String, Object>) o;
	}

	private int doQueryInt(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("QYI", q, packed);

		Retry<Object> r = new Retry<Object>("queryInt", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				priv = (new DBAccessSingle<Integer>(data, jdbc)).query(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv != null ? ((Number) r.priv).intValue() : 0;
		}
		throw failure(q, r.error);
	}

	private long doQueryLong(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("QYL", q, packed);

		Retry<Object> r = new Retry<Object>("queryLong", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				priv = (new DBAccessSingle<Long>(data, jdbc)).query(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv != null ? ((Number) r.priv).longValue() : 0L;
		}
		throw failure(q, r.error);
	}

	private String doQueryString(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("QYS", q, packed);

		Retry<String> r = new Retry<String>("queryString", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				priv = (new DBAccessSingle<String>(data, jdbc)).query(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	private Map<String, Object> doQuerys(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("QYM", q, packed);

		Retry<Map<String, Object>> r = new Retry<Map<String, Object>>("queryMap", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				try {
					priv = jdbc.queryForMap(q, packed);
				} catch (org.springframework.dao.EmptyResultDataAccessException e) {
					priv = null;
				}
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	private List<Map<String, Object>> doQuery(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("QLM", q, packed);

		Retry<List<Map<String, Object>>> r = new Retry<List<Map<String, Object>>>("queryList", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				priv = jdbc.queryForList(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	@DaoUpdateReturnValueCheck
	private int doUpdate(NamedParameterJdbcTemplate jdbc, String q, Map<String, Object> packed) throws SQLException {
		show("UPD", q, packed);

		Retry<Integer> r = new Retry<Integer>("update", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				priv = jdbc.update(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	private void doExecute(NamedParameterJdbcTemplate jdbc, String q) throws Exception {
		show("EXE", q, null);

		Retry<Object> r = new Retry<Object>("execute", this, jdbc) {
			@Override
			public void execute() throws SQLException {
				jdbc.getJdbcOperations().execute(q);
			}
		};
		if (retry(r)) {
			return;
		}
		throw failure(q, r.error);
	}
}
