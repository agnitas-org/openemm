/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.agnitas.util.Log;
import com.agnitas.util.Str;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

/**
 * Database abstraction layer
 */
public class DBase {
	static abstract class Cursor implements Closeable {
		abstract public String name ();
		abstract public void commit () throws SQLException;
		abstract public void rollback () throws SQLException;
		abstract public <T> T query (String query, Map <String, Object> packed, ResultSetExtractor <T> extractor) throws SQLException;
		abstract public <T> T querySingle (String query, Map <String, Object> packed) throws SQLException;
		abstract public Map <String, Object> queryForMap (String query, Map <String, Object> packed) throws SQLException;
		abstract public List <Map <String, Object>> queryForList (String query, Map <String, Object> packed) throws SQLException;
		abstract public int update (String query, Map <String, Object> packed) throws SQLException;
		abstract public void execute (String query) throws SQLException;
		public Object get (ResultSet rset, int position) throws SQLException {
			Object	o = rset.getObject (position);

			if (o != null) {
				switch (o.getClass ().getName ()) {
				case "oracle.sql.TIMESTAMP":
					return rset.getTimestamp (position);
				}
			}
			return o;
		}
	}
	static interface DB {
		public void setup (Data data, DBase dbase, DataSource dataSource) throws ClassNotFoundException;
		public void done ();
		public DataSource dataSource ();
		public Cursor cursor () throws SQLException;
	}
	/**
	 * internal datasource representation
	 */
	static class DBDatasource {
		private Map<String, DataSource> cache;
		private Set<String> seen;
		protected Log log;

		public DBDatasource(Data data) {
			cache = new HashMap<>();
			seen = new HashSet<>();
			log = new Log("jdbc", Log.INFO, 0);
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
		private GenericObjectPool<PoolableConnection> connectionPool;
		public DBDatasourcePooled(Data data) {
			super(data);
		}

		@Override
		public DataSource newDataSource(String driver, String connect, String login, String password) {
			ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connect, login, password);
			PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory (connectionFactory, null);

			connectionPool = new GenericObjectPool <> (poolableConnectionFactory);
			connectionPool.setMaxTotal (65536);
			connectionPool.setMaxWait (Duration.ofMinutes (10));
			connectionPool.setTestOnBorrow (true);
			poolableConnectionFactory.setPool (connectionPool);
			poolableConnectionFactory.setDefaultAutoCommit (true);
			log.out(Log.INFO, "nds", "New data source for " + driver + " using " + connect + " with " + login + " created");
			return new PoolingDataSource <> (connectionPool);
		}
	}

	static class DBNative implements DB {
		static class CursorNative extends Cursor {
			static class ParsedSQL {
				private String			originalSQL;
				private String			parsedSQL;
				private List <String>		placeHolder;
				private Set <String>		expectedPlaceHolder;
				private static Pattern  	parseNamedParameter = Pattern.compile ("'[^']*'|:[a-z][a-z0-9_]*", Pattern.CASE_INSENSITIVE);
				public ParsedSQL (String sql) {
					int		pos = 0;
					int		length = sql.length ();
					StringBuffer	parsed = new StringBuffer (length);
					Matcher		m = parseNamedParameter.matcher (sql);
			
					placeHolder = new ArrayList <> ();
					while (pos < length) {
						if (m.find (pos)) {
							int	start = m.start ();
							int	end = m.end ();
							String	match = m.group ();
					
							if (start > pos) {
								parsed.append (sql.substring (pos, start));
							}
							if (match.startsWith (":")) {
								placeHolder.add (sql.substring (start + 1, end));
								parsed.append ("?");
							} else {
								parsed.append (match);
							}
							pos = end;
						} else {
							parsed.append (sql.substring (pos));
							pos = length;
						}
					}
					originalSQL = sql;
					parsedSQL = parsed.toString ();
					expectedPlaceHolder = new HashSet <> (placeHolder);
				}

				public PreparedStatement fill (Connection connection, Map<String, Object> parameters) throws SQLException {
					PreparedStatement preparedStatement = null;
					try {
						preparedStatement = connection.prepareStatement (parsedSQL);
						if (parameters != null) {
							int	pos = 1;
	
							for (String ph : placeHolder) {
								if (parameters.containsKey (ph)) {
									Object	value = parameters.get (ph);
									int	valueType = Types.JAVA_OBJECT;
							
									if (value != null) {
										if (value instanceof java.util.Date) {
											valueType = Types.TIMESTAMP;
										}
									}
									if (valueType == Types.JAVA_OBJECT) {
										preparedStatement.setObject (pos++, value);
									} else {
										preparedStatement.setObject (pos++, value, valueType);
									}
								} else {
									throw new SQLException (originalSQL + ": no value passed for placeholder \"" + ph + "\"");
								}
							}
							String	unexpected = null;
	
							for (String key : parameters.keySet ()) {
								if (! expectedPlaceHolder.contains (key)) {
									if (unexpected == null) {
										unexpected = key;
									} else {
										unexpected += ", " + key;
									}
								}
							}
							if (unexpected != null) {
								throw new SQLException (originalSQL + ": passed unexpected values for these keys: " + unexpected);
							}
						} else if (placeHolder.size () > 0) {
							throw new SQLException (originalSQL + ": no paramater at all passed for expected placeholder");
						}
						return preparedStatement;
					} catch (Exception e) {
						if (preparedStatement != null) {
							preparedStatement.close();
						}
						throw e;
					}
				}
			}
			static private Map <String, ParsedSQL> parsedSQLCache = new HashMap <> ();
			static private int nr = 0;
			private int mynr;
			private DBase dbase;
			private DataSource dataSource;
			private Connection connection;
			
			public CursorNative (DBase dbase, DataSource dataSource) {
				synchronized (this) {
					mynr =++nr;
				}
				this.dbase = dbase;
				this.dataSource = dataSource;
				this.connection = null;
			}
			@Override
			public String name () {
				return "cursor[" + mynr + "]: " + connection;
			}
			@Override
			public void close () {
				if (connection != null) {
					try {
						connection.close ();
					} catch (SQLException e) {
						dbase.logging (Log.INFO, "dbnative", "Failed to close connection, assume its invalid anyway: " + e.toString ());
					}
					connection = null;
				}
			}
			@Override
			public void commit () throws SQLException {
				if ((connection != null) && (! connection.getAutoCommit ())) {
					connection.commit ();
				}
			}
			@Override
			public void rollback () throws SQLException {
				if ((connection != null) && (! connection.getAutoCommit ())) {
					connection.rollback ();
				}
			}

			@Override
			public <T> T query (String query, Map <String, Object> packed, ResultSetExtractor <T> extractor) throws SQLException {
				try (ResultSet rset = prepare (query, packed).executeQuery ()) {
					return extractor.extractData (rset);
				}
			}
			@Override
			@SuppressWarnings("unchecked")
			public <T> T querySingle (String query, Map <String, Object> packed) throws SQLException {
				try (ResultSet rset = prepare (query, packed).executeQuery ()) {
					if (rset.next ()) {
						return (T) get (rset, 1);
					}
				}
				return null;
			}
			@Override
			public Map <String, Object> queryForMap (String query, Map <String, Object> packed) throws SQLException {
				try (ResultSet rset = prepare (query, packed).executeQuery ()) {
					if (rset.next ()) {
						ResultSetMetaData	meta = rset.getMetaData ();
						int			numberOfColumns = meta.getColumnCount ();
						Map <String, Object>	result = new HashMap <> ();
						
						for (int index = 1; index <= numberOfColumns; ++index) {
							result.put (meta.getColumnName (index).toLowerCase (), get (rset, index));
						}
						return result;
					}
				}
				return null;
			}
			@Override
			public List <Map <String, Object>> queryForList (String query, Map <String, Object> packed) throws SQLException {
				List <Map <String, Object>>	result = new ArrayList <> ();
				
				try (ResultSet rset = prepare (query, packed).executeQuery ()) {
					ResultSetMetaData	meta = rset.getMetaData ();
					int			numberOfColumns = meta.getColumnCount ();

					while (rset.next ()) {
						Map <String, Object>	row = new HashMap <> ();
						
						for (int index = 1; index <= numberOfColumns; ++index) {
							row.put (meta.getColumnName (index).toLowerCase (), get (rset, index));
						}
						result.add (row);
					}
				}
				return result;
			}
			@Override
			public int update (String query, Map <String, Object> packed) throws SQLException {
				return prepare (query, packed).executeUpdate ();
			}
			@Override
			public void execute (String query) throws SQLException {
				prepare (query, null).execute ();
			}
			
			private PreparedStatement prepare (String query, Map <String, Object> packed) throws SQLException {
				ParsedSQL	psql;
		
				for (int retry = 0; retry < 2; ++retry) {
					if (connection == null) {
						try {
							connection = dataSource.getConnection ();
							if (connection == null) {
								throw new Exception ("no new connection available");
							}
							if (! connection.getAutoCommit ()) {
								dbase.logging (Log.WARNING, "dbnative", "Got unexpected new connection w/o auto commit enabled, enable it");
								connection.setAutoCommit (true);
							}
						} catch (Exception e) {
							close ();
							throw new SQLException ("failed to prepare query \"" + query + "\": " + e.toString (), e);
						}
					}
					synchronized (parsedSQLCache) {
						psql = parsedSQLCache.get (query);
						if (psql == null) {
							psql = new ParsedSQL (query);
							parsedSQLCache.put (query, psql);
						}
					}
					try {
						return psql.fill (connection, packed);
					} catch (SQLException e) {
						dbase.logging (Log.ERROR, "dbnative", "failed to prepare \"" + query + "\": " + e.toString ());
						close ();
					}
				}
				throw new SQLException ("failed to prepare query " + query);
			}
		}
		private static DBDatasource dbDatasource = null;
		private DBase dbase = null;
		private DataSource dataSource = null;
		
		@Override
		public synchronized void setup (Data data, DBase dbase, DataSource dataSource) throws ClassNotFoundException {
			this.dbase = dbase;
			if (dataSource == null) {
				if (dbDatasource == null) {
					dbDatasource = new DBDatasource (data);
				}
				this.dataSource = dbDatasource.request (data.dbDriver (), data.dbConnect (), data.dbLogin (), data.dbPassword ());
			} else {
				this.dataSource = dataSource;
			}
		}
		
		@Override
		public void done () {
			// nothing to do
		}
		
		@Override
		public DataSource dataSource () {
			return dataSource;
		}
		
		@Override
		public Cursor cursor () {
			return new CursorNative (dbase, dataSource);
		}
	}

	static class DBJDBC implements DB {
		static class CursorJDBC extends Cursor {
			static private int nr = 0;
			private int mynr;
			private DataSource dataSource;
			private NamedParameterJdbcTemplate jdbc;
			
			public CursorJDBC (DataSource dataSource) {
				synchronized (this) {
					mynr = ++nr;
				}
				this.dataSource = dataSource;
				this.jdbc = null;
			}
			@Override
			public String name () {
				return "cursor[" + mynr + "]: " + jdbc;
			}
			@Override
			public void close () {
				jdbc = null;
			}
			@Override
			public void commit () {
			}
			@Override
			public void rollback () {
			}
			@Override
			public <T> T query (String query, Map <String, Object> packed, ResultSetExtractor <T> extractor) {
				check ();
				return jdbc.query (query, packed, extractor);
			}
			private class SingleExtract <T> implements ResultSetExtractor <T> {
				@Override
				@SuppressWarnings("unchecked")
				public T extractData (ResultSet rs) throws SQLException, DataAccessException {
					if (rs.next ()) {
						return (T) get (rs, 1);
					}
					return null;
				}
			}
			@Override
			public <T> T querySingle (String query, Map <String, Object> packed) {
				check ();
				return jdbc.query (query, packed, new SingleExtract<>());
			}
			@Override
			public Map <String, Object> queryForMap (String query, Map <String, Object> packed) {
				check ();
				return jdbc.queryForMap (query, packed);
			}
			@Override
			public List <Map <String, Object>> queryForList (String query, Map <String, Object> packed) {
				check ();
				return jdbc.queryForList (query, packed);
			}
			@Override
			public int update (String query, Map <String, Object> packed) {
				check ();
				return jdbc.update (query, packed);
			}
			@Override
			public void execute (String query) {
				check ();
				jdbc.getJdbcOperations ().execute (query);
			}

			private void check () {
				if (jdbc == null) {
					jdbc = new NamedParameterJdbcTemplate (dataSource);
				}
			}
		}
		private static DBDatasourcePooled dsPool = null;
		private DataSource dataSource = null;

		@Override
		public synchronized void setup (Data data, DBase dbase, DataSource dataSource) throws ClassNotFoundException {
			if (dataSource == null) {
				if (dsPool == null) {
					dsPool = new DBDatasourcePooled (data);
				}
				this.dataSource = dsPool.request(data.dbDriver(), data.dbConnect(), data.dbLogin(), data.dbPassword());
			} else {
				this.dataSource = dataSource;
			}
		}
		
		@Override
		public void done () {
			// nothing to do
		}
		
		@Override
		public DataSource dataSource () {
			return dataSource;
		}
		
		@Override
		public Cursor cursor () {
			return new CursorJDBC (dataSource);
		}
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
		public Cursor cursor;
		public T priv;
		public SQLException error;

		public Retry(String nName, DBase nDBase, Cursor nCursor, T nPriv) {
			name = nName;
			dbase = nDBase;
			cursor = nCursor;
			priv = nPriv;
			error = null;
		}

		public Retry(String nName, DBase nDBase, Cursor nCursor) {
			this(nName, nDBase,nCursor, null);
		}

		public void reset() {
			priv = null;
			try {
				cursor.close ();
			} catch (java.io.IOException e) {
				dbase.logging (Log.WARNING, name, "reset cursor");
			}
		}

		public abstract void execute() throws SQLException;
	}
	
	private static final int DB_UNSET = 0;
	private static final int DB_MYSQL = 1;
	private static final int DB_ORACLE = 2;
	// general retry counter
	private static final int RETRY = 5;

	/**
	 * If not set from outside, Backend will try to create its own datasource from dbcfg data
	 */
	public static DataSource DATASOURCE = null;

	/**
	 * name for current date in database
	 */
	private int dbType = DB_UNSET;
	private int dbVersion = 0;
	private String dbImplementation = "unset";
	private boolean cursorTrace = false;
	private boolean retryUncategorizedSQLException = false;
	/**
	 * Reference to configuration
	 */
	private Data data = null;
	/**
	 * Default database access instance
	 */
	private DB db = null;
	private Cursor defaultCursor = null;
	private List <Cursor> cursorCache = null;
	private static String schema = null;
	private static Object schemaLock = new Object ();
	private static Map<String, Map<String, Boolean>> existingCache = new HashMap <> ();

	public DBase(Data data) throws ClassNotFoundException {
		this.data = data;
		dbImplementation = Data.syscfg.get ("db-implementation", "default");
		cursorTrace = Data.syscfg.get ("db-cursor-trace", cursorTrace);
		retryUncategorizedSQLException = Data.syscfg.get ("db-retry-on-uncategorized-error", true);
		switch (dbImplementation) {
		case "native":
			db = new DBNative ();
			break;
		default:
		case "jdbc":
			if (! "jdbc".equals (dbImplementation)) {
				dbImplementation = "jdbc (" + dbImplementation + ")";
			}
			db = new DBJDBC ();
			break;
		}
		db.setup (data, this, DATASOURCE);
		cursorCache = new ArrayList <> ();
		cursorTrace ("created");
	}

	/**
	 * Cleanup, close open statements and database connection
	 *
	 * @trhows Exception
	 */
	public DBase done() {
		cursorTrace ("releasing");
		while (! cursorCache.isEmpty ()) {
			@SuppressWarnings("resource")
			Cursor	cursor = cursorCache.remove (0);
			
			try {
				cursor.close ();
				cursorTrace ("close " + cursor.name ());
			} catch (IOException e) {
				cursorTrace ("close " + cursor.name () + " failed: " + e.toString ());
			}
		}
		cursorTrace ("released");
		if (defaultCursor != null) {
			try {
				defaultCursor.close ();
			} catch (IOException e) {
				logging (Log.WARNING, "failed to close default cursor: " + e.toString ());
			}
		}
		db.done ();
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

		if (dbms != null) {
			if (dbms.equals("oracle")) {
				dbType = DB_ORACLE;
			} else if (dbms.equals("mysql") || dbms.equals("mariadb")) {
				dbType = DB_MYSQL;
			} else {
				throw new Exception("Unsupported dbms found: " + dbms);
			}
		} else {
			if (data.dbDriver() == null) {
				throw new Exception("No configured database driver found");
			}
			if ((data.dbDriver().toLowerCase().indexOf("mysql") == -1) && (data.dbDriver().toLowerCase().indexOf("mariadb") == -1)) {
				dbType = DB_ORACLE;
			} else {
				dbType = DB_MYSQL;
			}
		}
	}

	/**
	 * creates the default cursor for all accesses to the
	 * database, which do not colide with another connections
	 *
	 * @throws Exception
	 */
	public void initialize() throws SQLException {
		defaultCursor = db.cursor ();
		logging (Log.DEBUG, "Using " + dbImplementation  + " database interface implementation");
		if (isOracle ()) {
			Pattern	versionPattern = Pattern.compile ("Oracle Database.*Release ([0-9]+)");
			
			for (Map <String, Object> row : query ("SELECT banner FROM v$version")) {
				String	banner = asString (row.get ("banner"));

				if (banner != null) {
					Matcher	m = versionPattern.matcher (banner);

					if (m.find ()) {
						dbVersion = Str.atoi (m.group (1));
						logging (Log.DEBUG, "Found Oracle major version " + dbVersion + " in banner " + banner);
						break;
					}
				}
			}
		}
	}

	/**
	 * wraps logging to be accessed from DAO
	 */
	public void logging(int loglvl, String mid, String msg, Throwable th) {
		data.logging(loglvl, (mid != null ? mid + "/db" : "db"), msg, th);
	}
	public void logging(int loglvl, String mid, String msg) {
		logging(loglvl, mid, msg, null);
	}
	public void logging(int loglvl, String msg) {
		logging(loglvl, null, msg, null);
	}
	public Log getLogger () {
		return data.getLogger ();
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

	private boolean existing (String object, String name, String oracleStatement, String mysqlStatement) throws SQLException {
		synchronized (existingCache) {
			Map <String, Boolean>	objectCache = existingCache.get (object);
			
			if (objectCache == null) {
				objectCache = new HashMap <> ();
				existingCache.put (object, objectCache);
			}
			name = name.toLowerCase ();
			Boolean exists = objectCache.get (name);
			if (exists == null) {
				String	statement;
			
				exists = false;
				switch (dbType) {
					case DB_ORACLE:
						statement = oracleStatement;
						break;
					case DB_MYSQL:
						statement = mysqlStatement;
						break;
					default:
						statement = null;
						break;
				}
				if (statement != null) {
					try (With with = with()) {
						exists = queryInt(with.cursor(), statement, object + "Name", name) > 0;
						objectCache.put (name, exists);
					}
				}
			}
			return exists;
		}
	}
				
	/**
	 * check if a table (case insensitive) exists in the current
	 * database schema
	 *
	 * @param table the name of the table
	 * @return true, if the table exists, false otherwise
	 */
	public boolean tableExists(String table) throws SQLException {
		return existing (
				 "table",
				 table,
				 "SELECT count(*) FROM user_tables WHERE lower(table_name) = lower(:tableName)",
				 "SELECT count(*) FROM information_schema.tables WHERE lower(table_name) = lower(:tableName) AND table_schema=(SELECT SCHEMA())"
		);
	}

	/**
	 * check if a view (case insensitive) exists in the current
	 * database schema
	 *
	 * @param view the name of the view
	 * @return true, if the view exists, false otherwise
	 */
	public boolean viewExists(String view) throws SQLException {
		return existing (
				 "view",
				 view,
				 "SELECT count(*) FROM user_views WHERE lower(view_name) = lower(:viewName)",
				 "SELECT count(*) FROM information_schema.views WHERE lower(table_name) = lower(:viewName) AND table_schema=(SELECT SCHEMA())"
		);
	}

	/**
	 * check if a synonym (case insensitive) exists in the current
	 * database schema
	 *
	 * @param synonym the name of the synonym
	 * @return true, if the synonym exists, false otherwise
	 */
	public boolean synonymExists(String synonym) throws SQLException {
		return existing (
				 "synonym",
				 synonym,
				 "SELECT count(*) FROM user_synonyms WHERE lower(synonym_name) = lower(:synonymName)",
				 null
		);
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
	 * called after creation/filling of a temp. table to ensure
	 * proper operation of the database
	 * 
	 * @param table name of the table
	 * @param estimatePercent oracle specific value
	 */
	public void setupTableOptimizer (String table, int estimatePercent) {
		try {
			if (isOracle () && (dbVersion >= 18)) {
				try (With with = with ()) {
					synchronized (schemaLock) {
						if (schema == null) {
							schema = queryString (with.cursor (), "SELECT user FROM DUAL");
						}
					}
					if (schema != null) {
						execute (with.cursor (),
							 "begin\n" +
							 "    dbms_stats.gather_table_stats(\n" +
							 "        ownname => '" + schema + "',\n" +
							 "        tabname => '" + table + "',\n" +
							 "        estimate_percent => " + estimatePercent + ",\n" +
							 "        method_opt => 'for all columns size 254',\n" +
							 "        cascade => true,\n" +
							 "        no_invalidate => FALSE\n" +
							 "    );\n" +
							 "end;");
					}
				}
			}
		} catch (SQLException e) {
			logging (Log.ERROR, "Failed to setup newly create table \"" + table + "\": " + e);
		}
	}
	public void setupTableOptimizer (String table) {
		setupTableOptimizer (table, 30);
	}

	/**
	 * returns the global cursor
	 *
	 * @return the cursor instance
	 */
	public Cursor cursor () {
		return defaultCursor;
	}

	/**
	 * returns the global cursor
	 * for invoking a specific query. This will log
	 * the query and its parameter (if this is a prepared
	 * statement with parameter).
	 *
	 * @param q     the query for which the connection will be used
	 * @param param the optional parameter for a prepared statement
	 * @return the cursor instance
	 */
	public Cursor cursor(String q, Map<String, Object> param) {
		show("JDB", q, param);
		return cursor();
	}

	public Cursor cursor(String q) {
		return cursor(q, null);
	}

	static public class With implements Closeable {
		private Cursor cursor;
		private DBase dbase;
		private String query;
		private Map<String, Object> param;

		protected With(Cursor nCursor, DBase nDbase, String nQuery, Map<String, Object> nParam) {
			cursor = nCursor;
			dbase = nDbase;
			query = nQuery;
			param = nParam;
		}

		public Cursor cursor () {
			return cursor;
		}

		@Override
		public void close() {
			if (query != null) {
				dbase.release(cursor, query, param);
			} else {
				dbase.release(cursor);
			}
			cursor = null;
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
	 * requests a new cursor instance which will be created.
	 *
	 * @return the new cursor instance
	 * @throws Exception
	 */
	public Cursor request() throws SQLException {
		if (cursorCache.isEmpty ()) {
			Cursor c = db.cursor ();
			cursorTrace ("request: create new " + c.name ());
			return c;
		} else {
			Cursor c = cursorCache.remove (cursorCache.size () - 1);
			cursorTrace ("request: pop from cache " + c.name ());
			return c;
		}
	}

	/**
	 * requests a new cursor template instance which will be created for
	 * using the connection to execute query. This will be logged
	 * including the optional parameter for a prepared statement.
	 *
	 * @param param the parameter for a prepared statement
	 * @return the new cursor instance
	 * @throws Exception
	 * @paran query the query the cursor connection will be used
	 */
	public Cursor request(String query, Map<String, Object> param) throws SQLException {
		show("REQ", query, param);
		return request();
	}

	public Cursor  request(String query) throws SQLException {
		return request(query, null);
	}

	/**
	 * releases a former requested cursor template
	 *
	 * @param temp the cursor instance to be released
	 * @return null
	 */
	public void release(Cursor cursor) {
		flush (cursor);
		if (cursor != defaultCursor) {
			cursorCache.add (cursor);
			cursorTrace ("release: added cursor " + cursor.name ());
		} else {
			cursorTrace ("release: found default cursor " + cursor.name ());
		}
	}
	private void cursorTrace (String message) {
		if (cursorTrace) {
			logging(Log.NOTICE, "cursortrace[" + cursorCache.size () + "]", message);
		}
	}

	public void flush (Cursor cursor, boolean commit) {
		try {
			if (commit) {
				cursor.commit ();
			} else {
				cursor.rollback ();
			}
		} catch (SQLException e) {
			logging(Log.DEBUG, "flush", (commit ? "Commit" : "Rolback") + " failed: " + e.toString ());
		}
	}
	public void flush (Cursor cursor) {
		flush (cursor, true);
	}
	public void flush (boolean commit) {
		flush (defaultCursor, commit);
	}
	public void flush () {
		flush (defaultCursor, true);
	}
	
	/**
	 * releases a former requested cursor
	 * including the query for which the template had been used for logging.
	 *
	 * @param temp  the cursor instance to be released
	 * @param query the query for which the template had been used
	 * @param param the optional parameter for a prepared statement
	 * @return null
	 */
	public void release(Cursor cursor, String query, Map<String, Object> param) {
		show("REL", query, param);
		release(cursor);
	}

	public void release(Cursor cursor, String query) {
		release(cursor, query, null);
	}

	public int queryInt(Cursor cursor, String q, Object... param) throws SQLException {
		return doQueryInt(cursor, q, pack(param));
	}

	public int queryInt(String q, Object... param) throws SQLException {
		return doQueryInt(cursor(), q, pack(param));
	}

	public long queryLong(Cursor cursor, String q, Object... param) throws SQLException {
		return doQueryLong(cursor, q, pack(param));
	}

	public long queryLong(String q, Object... param) throws SQLException {
		return doQueryLong(cursor(), q, pack(param));
	}

	public String queryString(Cursor cursor, String q, Object... param) throws SQLException {
		return doQueryString(cursor, q, pack(param));
	}

	public String queryString(String q, Object... param) throws SQLException {
		return doQueryString(cursor(), q, pack(param));
	}

	public Map<String, Object> querys(Cursor cursor, String q, Object... param) throws SQLException {
		return doQuerys(cursor, q, pack(param));
	}

	public Map<String, Object> querys(String q, Object... param) throws SQLException {
		return doQuerys(cursor(), q, pack(param));
	}

	public List<Map<String, Object>> query(Cursor cursor, String q, Object... param) throws SQLException {
		return doQuery(cursor, q, pack(param));
	}

	public List<Map<String, Object>> query(String q, Object... param) throws SQLException {
		return doQuery(cursor(), q, pack(param));
	}

	public int update(Cursor cursor, String q, Object... param) throws SQLException {
		return doUpdate(cursor, q, pack(param));
	}

	public int update(String q, Object... param) throws SQLException {
		return doUpdate(cursor(), q, pack(param));
	}

	public int update(Cursor cursor, String q, Map<String, Object> param) throws SQLException {
		return doUpdate(cursor, q, param);
	}

	public void execute(Cursor cursor, String q) throws SQLException {
		doExecute(cursor, q);
	}

	public void execute(String q) throws SQLException {
		doExecute(cursor(), q);
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
		String	s;
		
		if (o instanceof Clob) {
			Clob	clob = (Clob) o;
			
			try {
				s = clob.getSubString (1, (int) clob.length ());
			} catch (SQLException e) {
				failure ("clob parse", e);
				s = null;
			} finally {
				try {
					clob.free ();
				} catch (SQLException e) {
					failure("clob free", e);
				}
			}
		} else {
			s = (String) o;
		}
		s = validate (s, minLength);

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
		} else if (o instanceof String) {
			return (String) o;
		} else if (o instanceof Clob) {
			Clob clob = (Clob) o;

			try {
				return clob.getSubString(1, (int) clob.length());
			} catch (SQLException e) {
				failure("clob parse", e);
			} finally {
				try {
					clob.free ();
				} catch (SQLException e) {
					failure("clob free", e);
				}
			}
		} else {
			return o.toString ();
		}
		return null;
	}

	public byte[] asBlob(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof byte[]) {
			return (byte[]) o;
		} else if (o instanceof Blob) {
			Blob blob = (Blob) o;

			try {
				return blob.getBytes(1, (int) blob.length());
			} catch (SQLException e) {
				failure("blob parse", e);
			} finally {
				try {
					blob.free ();
				} catch (SQLException e) {
					failure("blob free", e);
				}
			}
		}
		return null;
	}

	public Date asDate(Object o, Date ifNull) {
		return o != null ? (Date) o : ifNull;
	}

	public Date asDate(Object o) {
		return asDate(o, null);
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
		logging(Log.ERROR, "DB Failed: " + q + ": " + e.toString());
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
							if (val instanceof String || val instanceof StringBuffer) {
								disp = "\"" + val.toString() + "\"";
							} else if (val instanceof Character) {
								disp = "'" + val.toString() + "'";
							} else if (val instanceof Boolean) {
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
			logging(Log.DEBUG, m);
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
		Connection connection = null;
		try {
			connection = db.dataSource().getConnection();
			try {
				if (connection.getAutoCommit()) {
					connection.setAutoCommit(false);
				}
				connection.commit();
			} catch (SQLException e) {
				logging(Log.WARNING, "new connections: commit fails even auto commit had been turned off: " + e.toString());
			} finally {
				connection.setAutoCommit(true);
			}
			return connection;
		} catch (Exception e) {
			if (connection != null) {
				connection.close();
				connection = null;
			}
			throw e;
		}
	}

	public Connection getConnection(String query, Object... param) throws SQLException {
		showq("CONN", query, param);
		return getConnection();
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

		while ((!rc) && rerun) {
			for (int state = RETRY - 1; (!rc) && (state >= 0); --state) {
				try {
					r.execute();
					if (r.error != null) {
						r.error = null;
						logging(Log.INFO, r.name, "Execution now succeeded");
					}
					rc = true;
				} catch (Exception e) {
					boolean recoverable = recoverableErrors(e, first);

					r.error = e instanceof SQLException ? (SQLException) e : new SQLException(e.toString(), e);
					r.reset();
					if (first) {
						logging(recoverable ? Log.WARNING : Log.ERROR, r.name, "Initial failure (" + (recoverable ? "" : "NOT ") + "recoverable): " + e.toString(), e);
					}
					if (!recoverable) {
						rerun = false;
						break;
					}
					if (first) {
						first = false;
					} else {
						logging(state > 0 ? Log.WARNING : Log.ERROR, r.name, "Failed to execute, " + (state > 0 ? "retry in " + state + " seconds" : "failed") + ": " + e.toString());
					}
					if (state > 0) {
						try {
							Thread.sleep(state * 1000);
						} catch (InterruptedException e2) {
							logging(Log.WARNING, r.name, "Interrupted during delay: " + e2.toString());
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
	private boolean recoverableErrors(Throwable t, boolean first) {
		for (Throwable s : t.getSuppressed()) {
			if (recoverableError(s, first)) {
				return true;
			}
		}
		return recoverableError(t, first);
	}

	/**
	 * checks if an exception is considered as recoverable
	 *
	 * @param t the exception to check
	 * @return true, if the exception is considered as recoverable, false otherwise
	 */
	private boolean recoverableError (Throwable t, boolean first) {
		Class <? extends Throwable>	cls = t.getClass ();
		
		if (cls == org.springframework.jdbc.UncategorizedSQLException.class) {
			return retryUncategorizedSQLException ? true : first;
		}
		if ((cls == org.springframework.jdbc.BadSqlGrammarException.class) ||
		    (cls == org.springframework.dao.DataIntegrityViolationException.class) ||
		    (cls == org.springframework.dao.DuplicateKeyException.class) ||
		    (cls == org.springframework.dao.PermissionDeniedDataAccessException.class) ||
		    (cls == org.springframework.dao.EmptyResultDataAccessException.class)) {
			return false;
		}
		if ((t instanceof org.springframework.dao.DataAccessException) ||
		    (t instanceof org.springframework.transaction.TransactionException)) {
			return true;
		}
		if ((t instanceof java.sql.SQLIntegrityConstraintViolationException) ||
		    (t instanceof java.sql.SQLSyntaxErrorException) ||
		    (t instanceof java.sql.SQLDataException)) {
			return false;
		}
		if ((t instanceof java.sql.SQLRecoverableException) ||
		    (t instanceof java.sql.SQLWarning)) {
			return true;
		}
		if (t instanceof RuntimeException) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object o) {
		return (Map<String, Object>) o;
	}

	private int doQueryInt(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("QYI", q, packed);

		Retry<Object> r = new Retry<>("queryInt", this, cursor) {
			@Override
			public void execute() throws SQLException {
				priv = cursor.querySingle (q, packed);
			}
		};
		if (retry(r)) {
			return r.priv != null ? ((Number) r.priv).intValue() : 0;
		}
		throw failure(q, r.error);
	}

	private long doQueryLong(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("QYL", q, packed);

		Retry<Object> r = new Retry<>("queryLong", this, cursor) {
			@Override
			public void execute() throws SQLException {
				priv = cursor.querySingle (q, packed);
			}
		};
		if (retry(r)) {
			return r.priv != null ? ((Number) r.priv).longValue() : 0L;
		}
		throw failure(q, r.error);
	}

	private String doQueryString(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("QYS", q, packed);

		Retry<String> r = new Retry<>("queryString", this, cursor) {
			@Override
			public void execute() throws SQLException {
				priv = cursor.querySingle (q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	private Map<String, Object> doQuerys(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("QYM", q, packed);

		Retry<Map<String, Object>> r = new Retry<>("queryMap", this, cursor) {
			@Override
			public void execute() throws SQLException {
				try {
					priv = cursor.queryForMap(q, packed);
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

	private List<Map<String, Object>> doQuery(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("QLM", q, packed);

		Retry<List<Map<String, Object>>> r = new Retry<>("queryList", this, cursor) {
			@Override
			public void execute() throws SQLException {
				priv = cursor.queryForList(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	@DaoUpdateReturnValueCheck
	private int doUpdate(Cursor cursor, String q, Map<String, Object> packed) throws SQLException {
		show("UPD", q, packed);

		Retry<Integer> r = new Retry<>("update", this, cursor) {
			@Override
			public void execute() throws SQLException {
				priv = cursor.update(q, packed);
			}
		};
		if (retry(r)) {
			return r.priv;
		}
		throw failure(q, r.error);
	}

	private void doExecute(Cursor cursor, String q) throws SQLException {
		show("EXE", q, null);

		Retry<Object> r = new Retry<>("execute", this, cursor) {
			@Override
			public void execute() throws SQLException {
				cursor.execute(q);
			}
		};
		if (retry(r)) {
			return;
		}
		throw failure(q, r.error);
	}
}
