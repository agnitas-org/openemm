/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonWriter;

public class DbUtilities {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(DbUtilities.class);

	/** Number format with 2 digits. */
	private static final NumberFormat TWO_DIGIT_FORMAT = new DecimalFormat("00");

	/** Number format with 4 digits. */
	private static final NumberFormat FOUR_DIGIT_FORMAT = new DecimalFormat("0000");

	public static final int ORACLE_TIMESTAMPTZ_TYPECODE = -101;

	// All characters (except digits) allowed in a target expression
	public static final String TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS = "!( )&|";
	
	public static final CaseInsensitiveSet RESERVED_WORDS_ORACLE = new CaseInsensitiveSet(new String[] { "access", "account", "activate", "add", "admin", "advise", "after", "all",
			"all_rows", "allocate", "alter", "analyze", "and", "any", "archive", "archivelog", "array", "as", "asc", "at", "audit", "authenticated", "authorization", "autoextend",
			"automatic", "backup", "become", "before", "begin", "between", "bfile", "bitmap", "blob", "block", "body", "by", "cache", "cache_instances", "cancel", "cascade",
			"cast", "cfile", "chained", "change", "char", "char_cs", "character", "check", "checkpoint", "choose", "chunk", "clear", "clob", "clone", "close",
			"close_cached_open_cursors", "cluster", "coalesce", "column", "columns", "comment", "commit", "committed", "compatibility", "compile", "complete", "composite_limit",
			"compress", "compute", "connect", "connect_time", "constraint", "constraints", "contents", "continue", "controlfile", "convert", "cost", "cpu_per_call",
			"cpu_per_session", "create", "current", "current_schema", "curren_user", "cursor", "cycle", "dangling", "database", "datafile", "datafiles", "dataobjno", "date", "dba",
			"dbhigh", "dblow", "dbmac", "deallocate", "debug", "dec", "decimal", "declare", "default", "deferrable", "deferred", "degree", "delete", "deref", "desc", "directory",
			"disable", "disconnect", "dismount", "distinct", "distributed", "dml", "double", "drop", "dump", "each", "else", "enable", "end", "enforce", "entry", "escape",
			"except", "exceptions", "exchange", "excluding", "exclusive", "execute", "exists", "expire", "explain", "extent", "extents", "externally", "failed_login_attempts",
			"false", "fast", "file", "first_rows", "flagger", "float", "flob", "flush", "for", "force", "foreign", "freelist", "freelists", "from", "full", "function", "global",
			"globally", "global_name", "grant", "group", "groups", "hash", "hashkeys", "having", "header", "heap", "identified", "idgenerators", "idle_time", "if", "immediate",
			"in", "including", "increment", "index", "indexed", "indexes", "indicator", "ind_partition", "initial", "initially", "initrans", "insert", "instance", "instances",
			"instead", "int", "integer", "intermediate", "intersect", "into", "is", "isolation", "isolation_level", "keep", "key", "kill", "label", "layer", "less", "level",
			"library", "like", "limit", "link", "list", "lob", "local", "lock", "locked", "log", "logfile", "logging", "logical_reads_per_call", "logical_reads_per_session",
			"long", "manage", "master", "max", "maxarchlogs", "maxdatafiles", "maxextents", "maxinstances", "maxlogfiles", "maxloghistory", "maxlogmembers", "maxsize", "maxtrans",
			"maxvalue", "min", "member", "minimum", "minextents", "minus", "minvalue", "mlslabel", "mls_label_format", "mode", "modify", "mount", "move", "mts_dispatchers",
			"multiset", "national", "nchar", "nchar_cs", "nclob", "needed", "nested", "network", "new", "next", "noarchivelog", "noaudit", "nocache", "nocompress", "nocycle",
			"noforce", "nologging", "nomaxvalue", "nominvalue", "none", "noorder", "nooverride", "noparallel", "noreverse", "normal", "nosort", "not", "nothing", "nowait", "null",
			"number", "numeric", "nvarchar2", "object", "objno", "objno_reuse", "of", "off", "offline", "oid", "oidindex", "old", "on", "online", "only", "opcode", "open",
			"optimal", "optimizer_goal", "option", "or", "order", "organization", "oslabel", "overflow", "own", "package", "parallel", "partition", "password",
			"password_grace_time", "password_life_time", "password_lock_time", "password_reuse_max", "password_reuse_time", "password_verify_function", "pctfree", "pctincrease",
			"pctthreshold", "pctused", "pctversion", "percent", "permanent", "plan", "plsql_debug", "post_transaction", "precision", "preserve", "primary", "prior", "private",
			"private_sga", "privilege", "privileges", "procedure", "profile", "public", "purge", "queue", "quota", "range", "raw", "rba", "read", "readup", "real", "rebuild",
			"recover", "recoverable", "recovery", "ref", "references", "referencing", "refresh", "rename", "replace", "reset", "resetlogs", "resize", "resource", "restricted",
			"return", "returning", "reuse", "reverse", "revoke", "role", "roles", "rollback", "row", "rowid", "rownum", "rows", "rule", "sample", "savepoint", "sb4",
			"scan_instances", "schema", "scn", "scope", "sd_all", "sd_inhibit", "sd_show", "segment", "seg_block", "seg_file", "select", "sequence", "serializable", "session",
			"session_cached_cursors", "sessions_per_user", "set", "share", "shared", "shared_pool", "shrink", "size", "skip", "skip_unusable_indexes", "smallint", "snapshot",
			"some", "sort", "specification", "split", "sql_trace", "standby", "start", "statement_id", "statistics", "stop", "storage", "store", "structure", "successful",
			"switch", "sys_op_enforce_not_null$", "sys_op_ntcimg$", "synonym", "sysdate", "sysdba", "sysoper", "system", "table", "tables", "tablespace", "tablespace_no", "tabno",
			"temporary", "than", "the", "then", "thread", "timestamp", "time", "to", "toplevel", "trace", "tracing", "transaction", "transitional", "trigger", "triggers", "true",
			"truncate", "tx", "type", "ub2", "uba", "uid", "unarchived", "undo", "union", "unique", "unlimited", "unlock", "unrecoverable", "until", "unusable", "unused",
			"updatable", "update", "usage", "use", "user", "using", "validate", "validation", "value", "values", "varchar", "varchar2", "varying", "view", "when", "whenever",
			"where", "with", "without", "work", "write", "writedown", "writeup", "xid", "year", "zone" });

	public static final CaseInsensitiveSet RESERVED_WORDS_MYSQL_MARIADB = new CaseInsensitiveSet(new String[] { "accessible", "account", "action", "add", "after", "against",
			"aggregate", "algorithm", "all", "alter", "always", "analyse", "analyze", "and", "any", "as", "asc", "ascii", "asensitive", "at", "autoextend_size", "auto_increment",
			"avg", "avg_row_length", "backup", "before", "begin", "between", "bigint", "binary", "binlog", "bit", "blob", "block", "bool", "boolean", "both", "btree", "by", "byte",
			"cache", "call", "cascade", "cascaded", "case", "catalog_name", "chain", "change", "changed", "channel", "char", "character", "charset", "check", "checksum", "cipher",
			"class_origin", "client", "close", "coalesce", "code", "collate", "collation", "column", "columns", "column_format", "column_name", "comment", "commit", "committed",
			"compact", "completion", "compressed", "compression", "concurrent", "condition", "connection", "consistent", "constraint", "constraint_catalog", "constraint_name",
			"constraint_schema", "contains", "context", "continue", "convert", "cpu", "create", "cross", "cube", "current", "current_date", "current_time", "current_timestamp",
			"current_user", "cursor", "cursor_name", "data", "database", "databases", "datafile", "date", "datetime", "day", "day_hour", "day_microsecond", "day_minute",
			"day_second", "deallocate", "dec", "decimal", "declare", "default", "default_auth", "definer", "delayed", "delay_key_write", "delete", "desc", "describe",
			"des_key_file", "deterministic", "diagnostics", "directory", "disable", "discard", "disk", "distinct", "distinctrow", "div", "do", "double", "drop", "dual", "dumpfile",
			"duplicate", "dynamic", "each", "else", "elseif", "enable", "enclosed", "encryption", "end", "ends", "engine", "engines", "enum", "error", "errors", "escape",
			"escaped", "event", "events", "every", "exchange", "execute", "exists", "exit", "expansion", "expire", "explain", "export", "extended", "extent_size", "false", "fast",
			"faults", "fetch", "fields", "file", "file_block_size", "filter", "first", "fixed", "float", "float4", "float8", "flush", "follows", "for", "force", "foreign",
			"format", "found", "from", "full", "fulltext", "function", "general", "generated", "geometry", "geometrycollection", "get", "get_format", "global", "grant", "grants",
			"group", "group_replication", "handler", "hash", "having", "help", "high_priority", "host", "hosts", "hour", "hour_microsecond", "hour_minute", "hour_second",
			"identified", "if", "ignore", "ignore_server_ids", "import", "in", "index", "indexes", "infile", "initial_size", "inner", "inout", "insensitive", "insert",
			"insert_method", "install", "instance", "int", "int1", "int2", "int3", "int4", "int8", "integer", "interval", "into", "invoker", "io", "io_after_gtids",
			"io_before_gtids", "io_thread", "ipc", "is", "isolation", "issuer", "iterate", "join", "json", "key", "keys", "key_block_size", "kill", "language", "last", "leading",
			"leave", "leaves", "left", "less", "level", "like", "limit", "linear", "lines", "linestring", "list", "load", "local", "localtime", "localtimestamp", "lock", "locks",
			"logfile", "logs", "long", "longblob", "longtext", "loop", "low_priority", "master", "master_auto_position", "master_bind", "master_connect_retry", "master_delay",
			"master_heartbeat_period", "master_host", "master_log_file", "master_log_pos", "master_password", "master_port", "master_retry_count", "master_server_id", "master_ssl",
			"master_ssl_ca", "master_ssl_capath", "master_ssl_cert", "master_ssl_cipher", "master_ssl_crl", "master_ssl_crlpath", "master_ssl_key", "master_ssl_verify_server_cert",
			"master_tls_version", "master_user", "match", "maxvalue", "max_connections_per_hour", "max_queries_per_hour", "max_rows", "max_size", "max_statement_time",
			"max_updates_per_hour", "max_user_connections", "medium", "mediumblob", "mediumint", "mediumtext", "memory", "merge", "message_text", "microsecond", "middleint",
			"migrate", "minute", "minute_microsecond", "minute_second", "min_rows", "mod", "mode", "modifies", "modify", "month", "multilinestring", "multipoint", "multipolygon",
			"mutex", "mysql_errno", "name", "names", "national", "natural", "nchar", "ndb", "ndbcluster", "never", "new", "next", "no", "nodegroup", "nonblocking", "none", "not",
			"no_wait", "no_write_to_binlog", "null", "number", "numeric", "nvarchar", "offset", "old_password", "on", "one", "only", "open", "optimize", "optimizer_costs",
			"option", "optionally", "options", "or", "order", "out", "outer", "outfile", "owner", "pack_keys", "page", "parser", "parse_gcol_expr", "partial", "partition",
			"partitioning", "partitions", "password", "phase", "plugin", "plugins", "plugin_dir", "point", "polygon", "port", "precedes", "precision", "prepare", "preserve",
			"prev", "primary", "privileges", "procedure", "processlist", "profile", "profiles", "proxy", "purge", "quarter", "query", "quick", "range", "read", "reads",
			"read_only", "read_write", "real", "rebuild", "recover", "redofile", "redo_buffer_size", "redundant", "references", "regexp", "relay", "relaylog", "relay_log_file",
			"relay_log_pos", "relay_thread", "release", "reload", "remove", "rename", "reorganize", "repair", "repeat", "repeatable", "replace", "replicate_do_db",
			"replicate_do_table", "replicate_ignore_db", "replicate_ignore_table", "replicate_rewrite_db", "replicate_wild_do_table", "replicate_wild_ignore_table", "replication",
			"require", "reset", "resignal", "restore", "restrict", "resume", "return", "returned_sqlstate", "returns", "reverse", "revoke", "right", "rlike", "rollback", "rollup",
			"rotate", "routine", "row", "rows", "row_count", "row_format", "rtree", "savepoint", "schedule", "schema", "schemas", "schema_name", "second", "second_microsecond",
			"security", "select", "sensitive", "separator", "serial", "serializable", "server", "session", "set", "share", "show", "shutdown", "signal", "signed", "simple",
			"slave", "slow", "smallint", "snapshot", "socket", "some", "soname", "sounds", "source", "spatial", "specific", "sql", "sqlexception", "sqlstate", "sqlwarning",
			"sql_after_gtids", "sql_after_mts_gaps", "sql_before_gtids", "sql_big_result", "sql_buffer_result", "sql_cache", "sql_calc_found_rows", "sql_no_cache",
			"sql_small_result", "sql_thread", "sql_tsi_day", "sql_tsi_hour", "sql_tsi_minute", "sql_tsi_month", "sql_tsi_quarter", "sql_tsi_second", "sql_tsi_week", "sql_tsi_year",
			"ssl", "stacked", "start", "starting", "starts", "stats_auto_recalc", "stats_persistent", "stats_sample_pages", "status", "stop", "storage", "stored", "straight_join",
			"string", "subclass_origin", "subject", "subpartition", "subpartitions", "super", "suspend", "swaps", "switches", "table", "tables", "tablespace", "table_checksum",
			"table_name", "temporary", "temptable", "terminated", "text", "than", "then", "time", "timestamp", "timestampadd", "timestampdiff", "tinyblob", "tinyint", "tinytext",
			"to", "trailing", "transaction", "trigger", "triggers", "true", "truncate", "type", "types", "uncommitted", "undefined", "undo", "undofile", "undo_buffer_size",
			"unicode", "uninstall", "union", "unique", "unknown", "unlock", "unsigned", "until", "update", "upgrade", "usage", "use", "user", "user_resources", "use_frm", "using",
			"utc_date", "utc_time", "utc_timestamp", "validation", "value", "values", "varbinary", "varchar", "varcharacter", "variables", "varying", "view", "virtual", "wait",
			"warnings", "week", "weight_string", "when", "where", "while", "with", "without", "work", "wrapper", "write", "x509", "xa", "xid", "xml", "xor", "year", "year_month",
			"zerofill" });

	public static int readoutInOutputStream(JdbcTemplate jdbcTemplate, String statementString, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		return readoutInOutputStream(jdbcTemplate.getDataSource(), statementString, outputStream, encoding, separator, stringQuote);
	}

	public static int readoutInOutputStream(DataSource dataSource, String statementString, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return readoutInOutputStream(connection, statementString, outputStream, encoding, separator, stringQuote);
		}
	}

	public static int readoutInOutputStream(DataSource dataSource, String statementString, List<String> csvColumnNames, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return readoutInOutputStream(connection, statementString, csvColumnNames, outputStream, encoding, separator, stringQuote);
		}
	}

	public static int readoutInOutputStream(DataSource dataSource, String statementString, List<Object> statementParameter, List<String> csvColumnNames, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return readoutInOutputStream(connection, statementString, statementParameter, csvColumnNames, outputStream, encoding, separator, stringQuote);
		}
	}

	public static int readoutInOutputStreamWithDateIntervals(DataSource dataSource, String statementString, OutputStream outputStream, String encoding, char separator, Character stringQuote, Date...dates) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			if(dates == null || dates.length == 0){
				return readoutInOutputStream(connection, statementString, outputStream, encoding, separator, stringQuote);
			}
			try (PreparedStatement statement = connection.prepareStatement(statementString)) {
				for(int i = 0; i < dates.length; i++){
					statement.setTimestamp(i+1, new java.sql.Timestamp(dates[i].getTime()));
				}
				try (ResultSet resultSet = statement.executeQuery()) {
					return readoutInOutputStream(null, resultSet, outputStream, encoding, separator, stringQuote);
				}
			}
		}
	}

	private static int readoutInOutputStream(List<String> csvColumnNames, ResultSet resultSet, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (CsvWriter csvWriter = new CsvWriter(outputStream, encoding, separator, stringQuote)) {
			ResultSetMetaData metaData = resultSet.getMetaData();

			if (csvColumnNames == null || csvColumnNames.isEmpty()) {
				List<String> headers = new ArrayList<>();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					headers.add(metaData.getColumnLabel(i));
				}
				csvWriter.writeValues(headers);
			} else {
				csvWriter.writeValues(csvColumnNames);
			}

			while (resultSet.next()) {
				List<String> values = new ArrayList<>();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					if (metaData.getColumnType(i) == Types.BLOB
							|| metaData.getColumnType(i) == Types.BINARY
							|| metaData.getColumnType(i) == Types.VARBINARY
							|| metaData.getColumnType(i) == Types.LONGVARBINARY) {
						Blob blob = resultSet.getBlob(i);
						if (resultSet.wasNull()) {
							values.add("");
						} else {
							try (InputStream input = blob.getBinaryStream()) {
								byte[] data = IOUtils.toByteArray(input);
								values.add(Base64.getEncoder().encodeToString(data));
							}
						}
					} else if (metaData.getColumnType(i) == Types.DATE || metaData.getColumnType(i) == Types.TIMESTAMP) {
						String value = resultSet.getString(i);
						if ("0000-00-00 00:00:00".equals(value)) {
							value = null;
						}
						values.add(value);
					} else {
						values.add(resultSet.getString(i));
					}
				}
				csvWriter.writeValues(values);
			}

			return csvWriter.getWrittenLines() - 1;
		}
	}

    public static int readoutInOutputStream(Connection connection, String statementString, List<Object> statementParameter, List<String> csvColumnNames, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (PreparedStatement statement = connection.prepareStatement(statementString)) {
			if (statementParameter != null && statementParameter.size() > 0) {
				int parameterIndex = 0;
				for (Object parameter: statementParameter) {
					statement.setObject(++parameterIndex, parameter);
				}
			}
			try (ResultSet resultSet = statement.executeQuery()) {
				return readoutInOutputStream(csvColumnNames, resultSet, outputStream, encoding, separator, stringQuote);
			}
		}
	}

    public static int readoutInOutputStream(Connection connection, String statementString, List<String> csvColumnNames, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery(statementString)) {
				return readoutInOutputStream(csvColumnNames, resultSet, outputStream, encoding, separator, stringQuote);
			}
		}
	}

    public static int readoutInOutputStream(Connection connection, String statementString, OutputStream outputStream, String encoding, char separator, Character stringQuote) throws Exception {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery(statementString)) {
				return readoutInOutputStream(null, resultSet, outputStream, encoding, separator, stringQuote);
			}
		}
	}

	public static String readout(JdbcTemplate jdbcTemplate, String statementString, char separator, Character stringQuote) throws Exception {
		return readout(jdbcTemplate.getDataSource(), statementString, separator, stringQuote);
	}

	public static String readout(DataSource dataSource, String statementString, char separator, Character stringQuote) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return readout(connection, statementString, separator, stringQuote);
		}
	}

    public static String readout(Connection connection, String statementString, char separator, Character stringQuote) throws Exception {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			readoutInOutputStream(connection, statementString, outputStream, "UTF-8", separator, stringQuote);
			return new String(outputStream.toByteArray(), "UTF-8");
		}
	}

    public static String readoutTable(DataSource dataSource, String tableName, char separator, Character stringQuote) throws Exception {
    	return readout(dataSource, "SELECT * FROM " + tableName, separator, stringQuote);
    }

    public static String readoutTable(Connection connection, String tableName, char separator, Character stringQuote) throws Exception {
    	return readout(connection, "SELECT * FROM " + tableName, separator, stringQuote);
    }

    public static String readoutTable(JdbcTemplate jdbcTemplate, String tableName, char separator, Character stringQuote) throws Exception {
    	return readout(jdbcTemplate, "SELECT * FROM " + tableName, separator, stringQuote);
    }

	public static int readoutInJsonOutputStream(DataSource dataSource, String statementString, List<String> dataColumns, OutputStream outputStream) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(statementString)) {
					try (JsonWriter jsonWriter = new JsonWriter(outputStream)) {
						ResultSetMetaData metaData = resultSet.getMetaData();
						int itemCount = 0;
						jsonWriter.openJsonArray();
						while (resultSet.next()) {
							itemCount++;
							JsonObject jsonObject = new JsonObject();
							for (int i = 1; i <= metaData.getColumnCount(); i++) {
								String propertyName = dataColumns.get(i -1);
								if (metaData.getColumnType(i) == Types.BLOB
										|| metaData.getColumnType(i) == Types.BINARY
										|| metaData.getColumnType(i) == Types.VARBINARY
										|| metaData.getColumnType(i) == Types.LONGVARBINARY) {
									Blob blob = resultSet.getBlob(i);
									if (resultSet.wasNull()) {
										jsonObject.add(propertyName, null);
									} else {
										try (InputStream input = blob.getBinaryStream()) {
											byte[] data = IOUtils.toByteArray(input);
											jsonObject.add(propertyName, Base64.getEncoder().encodeToString(data));
										}
									}
								} else if (metaData.getColumnType(i) == Types.DATE || metaData.getColumnType(i) == Types.TIMESTAMP) {
									String value = resultSet.getString(i);
									if ("0000-00-00 00:00:00".equals(value)) {
										value = null;
									}
									jsonObject.add(propertyName, value);
								} else {
									jsonObject.add(propertyName, resultSet.getString(i));
								}
							}
							jsonWriter.add(jsonObject);
						}
						jsonWriter.closeJsonArray();

						return itemCount;
					}
				}
			}
		}
	}

	public static JsonArray readoutInJsonArray(DataSource dataSource, String statementString, List<String> dataColumns) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(statementString)) {
					ResultSetMetaData metaData = resultSet.getMetaData();
					
					if (dataColumns == null) {
						dataColumns = new ArrayList<>();
						for (int i = 1; i <= metaData.getColumnCount(); i++) {
							dataColumns.add(metaData.getColumnName(i));
						}
					}
					
					JsonArray jsonArray = new JsonArray();
					while (resultSet.next()) {
						JsonObject jsonObject = new JsonObject();
						for (int i = 1; i <= metaData.getColumnCount(); i++) {
							String propertyName = dataColumns.get(i -1);
							SimpleDataType simpleDataType = DbColumnType.getSimpleDataType(metaData.getColumnTypeName(i), metaData.getScale(i));
							if (metaData.getColumnType(i) == Types.BLOB
									|| metaData.getColumnType(i) == Types.BINARY
									|| metaData.getColumnType(i) == Types.VARBINARY
									|| metaData.getColumnType(i) == Types.LONGVARBINARY) {
								Blob blob = resultSet.getBlob(i);
								if (resultSet.wasNull()) {
									jsonObject.add(propertyName, null);
								} else {
									try (InputStream input = blob.getBinaryStream()) {
										byte[] data = IOUtils.toByteArray(input);
										jsonObject.add(propertyName, Base64.getEncoder().encodeToString(data));
									}
								}
							} else if (simpleDataType == SimpleDataType.Date) {
								if ("0000-00-00 00:00:00".equals(resultSet.getString(i))) {
									jsonObject.add(propertyName, null);
								} else {
									Date value = resultSet.getTimestamp(i);
									jsonObject.add(propertyName, value);
								}
							} else if (simpleDataType == SimpleDataType.DateTime) {
								if ("0000-00-00 00:00:00".equals(resultSet.getString(i))) {
									jsonObject.add(propertyName, null);
								} else {
									Date value = resultSet.getTimestamp(i);
									jsonObject.add(propertyName, value);
								}
							} else if (simpleDataType == SimpleDataType.Float) {
								jsonObject.add(propertyName, resultSet.getBigDecimal(i).doubleValue());
							} else if (simpleDataType == SimpleDataType.Numeric) {
								jsonObject.add(propertyName, resultSet.getBigDecimal(i).intValue());
							} else {
								jsonObject.add(propertyName, resultSet.getString(i));
							}
						}
						jsonArray.add(jsonObject);
					}

					return jsonArray;
				}
			}
		}
	}

	public static boolean checkDbVendorIsOracle(DataSource dataSource) {
		if (dataSource == null) {
			throw new RuntimeException("Cannot detect db vendor: dataSource is null");
		}

		try (final Connection connection = dataSource.getConnection()) {
			return checkDbVendorIsOracle(connection);
		} catch (Exception e) {
			logger.error("Cannot detect db vendor: " + e.getMessage(), e);
			throw new RuntimeException("Cannot detect db vendor: " + e.getMessage(), e);
		}
	}

	public static boolean checkDbVendorIsMariaDB(DataSource dataSource) {
		if (dataSource == null) {
			throw new RuntimeException("Cannot detect db vendor: dataSource is null");
		}

		try (final Connection connection = dataSource.getConnection()) {
			return checkDbVendorIsMariaDB(connection);
		} catch (Exception e) {
			logger.error("Cannot detect db vendor: " + e.getMessage(), e);
			throw new RuntimeException("Cannot detect db vendor: " + e.getMessage(), e);
		}
	}

	public static boolean checkDbVendorIsOracle(Connection connection) {
		if (connection == null) {
			throw new RuntimeException("Cannot detect db vendor: connection is null");
		}

		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			if (databaseMetaData != null) {
				String productName = databaseMetaData.getDatabaseProductName();
				if ("oracle".equalsIgnoreCase(productName)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Cannot detect db vendor: " + e.getMessage(), e);
			throw new RuntimeException("Cannot detect db vendor: " + e.getMessage(), e);
		}
	}

	public static boolean checkDbVendorIsMariaDB(Connection connection) {
		if (connection == null) {
			throw new RuntimeException("Cannot detect db vendor: connection is null");
		}

		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			if (databaseMetaData != null) {
				String productName = databaseMetaData.getDatabaseProductName();
				if ("maria".equalsIgnoreCase(productName)
						|| "mariadb".equalsIgnoreCase(productName)
						|| databaseMetaData.getURL().toLowerCase().startsWith("jdbc:mariadb:")) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Cannot detect db vendor: " + e.getMessage(), e);
			throw new RuntimeException("Cannot detect db vendor: " + e.getMessage(), e);
		}
	}

	public static String getDbUrl(DataSource dataSource) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return getDbUrl(connection);
		}
	}

	public static String getDbUrl(Connection connection) {
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			if (databaseMetaData != null) {
				return databaseMetaData.getURL();
			} else {
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	public static boolean checkTableAndColumnsExist(DataSource dataSource, String tableName, String... columns) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return checkTableAndColumnsExist(connection, tableName, columns, false);
		}
	}

	public static boolean checkTableAndColumnsExist(Connection connection, String tableName, String... columns) throws Exception {
		return checkTableAndColumnsExist(connection, tableName, columns, false);
	}

	public static boolean checkTableAndColumnsExist(DataSource dataSource, String tableName, String[] columns, boolean throwExceptionOnError) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
    		return checkTableAndColumnsExist(connection, tableName, columns, throwExceptionOnError);
    	}
	}

	public static boolean checkTableAndColumnsExist(Connection connection, String tableName, String[] columns, boolean throwExceptionOnError) throws Exception {
		try (Statement statement = connection.createStatement()){
			try (ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE 1 = 0")) {
				// Check if all needed columns exist
				Set<String> dbTableColumns = new HashSet<>();
				ResultSetMetaData metaData = resultSet.getMetaData();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					dbTableColumns.add(metaData.getColumnLabel(i).toUpperCase());
				}
				for (String column : columns) {
					if (column != null && !dbTableColumns.contains(column.toUpperCase())) {
						if (throwExceptionOnError) {
							throw new Exception("Column '" + column + "' does not exist in table '" + tableName + "'");
						} else {
							return false;
						}
					}
				}
				return true;
			} catch (Exception e) {
				if (throwExceptionOnError) {
					throw new Exception("Table '" + tableName + "' does not exist");
				} else {
					return false;
				}
			}
		}
	}

	public static boolean checkIfTableExists(DataSource dataSource, String tableName) {
		try {
			checkTableExists(dataSource, tableName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean checkIfTableExists(Connection connection, String tableName) {
		try {
			checkTableExists(connection, tableName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void checkTableExists(DataSource dataSource, String tableName) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			checkTableExists(connection, tableName);
		}
	}

	public static void checkTableExists(Connection connection, String tableName) throws Exception {
		if (connection == null) {
			throw new Exception("Connection for checkTableExists is null");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("TableName for checkTableExists is empty");
		} else if (checkDbVendorIsOracle(connection)) {
			try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM user_tables WHERE table_name = ?")) {
				preparedStatement.setString(1, tableName.toUpperCase());
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					resultSet.next();
					if (resultSet.getInt(1) <= 0) {
						throw new Exception("Table '" + tableName + "' does not exist");
					}
				} catch (Exception e) {
					throw new Exception("Table '" + tableName + "' does not exist");
				}
			}
		} else {
			try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = SCHEMA() AND table_name = ?")) {
				preparedStatement.setString(1, tableName.toLowerCase());
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					resultSet.next();
					if (resultSet.getInt(1) <= 0) {
						throw new Exception("Table '" + tableName + "' does not exist");
					}
				} catch (Exception e) {
					throw new Exception("Table '" + tableName + "' does not exist");
				}
			}
		}
	}

	public static void checkTableExists(JdbcTemplate jdbcTemplate, String tableName) throws Exception {
		if (jdbcTemplate == null) {
			throw new Exception("JdbcTemplate for checkTableExists is null");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("TableName for checkTableExists is empty");
		} else if (checkDbVendorIsOracle(jdbcTemplate.getDataSource())) {
			int foundTables = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_tables WHERE table_name = ?", Integer.class, tableName.toUpperCase());
			if (foundTables <= 0) {
				throw new Exception("Table '" + tableName + "' does not exist");
			}
		} else {
			int foundTables = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = SCHEMA() AND table_name = ?", Integer.class, tableName.toLowerCase());
			if (foundTables <= 0) {
				throw new Exception("Table '" + tableName + "' does not exist");
			}
		}
	}

	public static String callStoredProcedureWithDbmsOutput(Connection connection, String procedureName, Object... parameters) throws SQLException {
		try (CallableStatement callableStatement = connection.prepareCall("begin dbms_output.enable(:1); end;")) {
			callableStatement.setLong(1, 10000);
			callableStatement.executeUpdate();
		}

		if (parameters != null) {
			try (CallableStatement callableStatement = connection.prepareCall("{call " + procedureName + "(" + AgnUtils.repeatString("?", parameters.length, ", ") + ")}")) {
				for (int i = 0; i < parameters.length; i++) {
					if (parameters[i] instanceof Date) {
						callableStatement.setTimestamp(i + 1, new java.sql.Timestamp(((Date) parameters[i]).getTime()));
					} else {
						callableStatement.setObject(i + 1, parameters[i]);
					}
				}
				callableStatement.execute();
			}
		} else {
			try (CallableStatement callableStatement = connection.prepareCall("{call " + procedureName + "()}")) {
				callableStatement.execute();
			}
		}

		StringBuffer dbmsOutput = new StringBuffer(1024);
		
		try (CallableStatement callableStatement = connection
			.prepareCall(
				"declare "
				+ "    l_line varchar2(255); "
				+ "    l_done number; "
				+ "    l_buffer long; "
				+ "begin "
				+ "  loop "
				+ "    exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; "
				+ "    dbms_output.get_line( l_line, l_done ); "
				+ "    l_buffer := l_buffer || l_line || chr(10); "
				+ "  end loop; " + " :done := l_done; "
				+ " :buffer := l_buffer; "
				+ "end;")) {
	
			callableStatement.registerOutParameter(2, Types.INTEGER);
			callableStatement.registerOutParameter(3, Types.VARCHAR);
			while (true) {
				callableStatement.setInt(1, 32000);
				callableStatement.executeUpdate();
				dbmsOutput.append(callableStatement.getString(3).trim());
				if (callableStatement.getInt(2) == 1) {
					break;
				}
			}
		}

		try (CallableStatement callableStatement = connection.prepareCall("begin dbms_output.disable; end;")) {
			callableStatement.executeUpdate();
		}

		return dbmsOutput.toString();
	}

	public static TextTable getResultAsTextTable(DataSource datasource, String selectString) throws Exception {
		List<Map<String, Object>> results = new JdbcTemplate(datasource).queryForList(selectString);
		if (results != null && results.size() > 0) {
			TextTable textTable = new TextTable();
			for (String column : results.get(0).keySet()) {
				textTable.addColumn(column);
			}

			if (results != null && results.size() > 0) {
				for (Map<String, Object> row : results) {
					textTable.startNewLine();
					for (Entry<String, Object> entry : row.entrySet()) {
						if (entry.getValue() != null) {
							textTable.addValueToCurrentLine(entry.getValue().toString());
						} else {
							textTable.addValueToCurrentLine("<null>");
						}
					}
				}
			}

			return textTable;
		} else {
			return null;
		}
	}

	/**
	 * Get all column names of a table in lowercase
	 *
	 * @param dataSource
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static List<String> getColumnNames(DataSource dataSource, String tableName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getColumnNames");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getColumnNames");
		} else {
			try (final Connection connection = dataSource.getConnection()) {
				try (Statement stmt = connection.createStatement()) {
			    	String sql = "SELECT * FROM " + SafeString.getSafeDbTableName(tableName) + " WHERE 1 = 0";
			        try (ResultSet rset = stmt.executeQuery(sql)) {
				        List<String> columnNamesList = new ArrayList<>();
				        for (int i = 1; i <= rset.getMetaData().getColumnCount(); i++) {
				        	columnNamesList.add(rset.getMetaData().getColumnLabel(i).toLowerCase());
				        }
				        return columnNamesList;
			        }
				}
			}
		}
	}

	public static DbColumnType getColumnDataType(DataSource dataSource, String tableName, String columnName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getColumnDataType");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getColumnDataType");
		} else if (StringUtils.isBlank(columnName)) {
			throw new Exception("Invalid empty columnName for getColumnDataType");
		} else {
			try (final Connection connection = dataSource.getConnection()) {
				long characterLength;
				int numericPrecision;
				int numericScale;
				boolean isNullable;
				if (checkDbVendorIsOracle(connection)) {
					// Read special constraints for not-nullable fields by user_tab_columns
					String nullableSql = "SELECT search_condition FROM all_constraints WHERE table_name = UPPER(?)";
					boolean isNotNullByConstraintFields = false;
					try (final PreparedStatement preparedStatementNullable = connection.prepareStatement(nullableSql)) {
						preparedStatementNullable.setString(1, tableName);
						try (final ResultSet resultSetNullable = preparedStatementNullable.executeQuery()) {
							while (resultSetNullable.next()) {
								String searchCondition = resultSetNullable.getString("search_condition");
								searchCondition = searchCondition != null ? searchCondition.toLowerCase().replace("'", "").replace("\"", "") : "";
								if (searchCondition.endsWith(" is not null")) {
									if (columnName.equals(searchCondition.substring(0, searchCondition.indexOf(" is not null")).trim())) {
										isNotNullByConstraintFields = true;
									}
								}
							}
						}
					}

					// Watchout: Oracle's timestamp datatype is "TIMESTAMP(6)", so remove the bracket value
					String sql = "SELECT COALESCE(substr(data_type, 1, instr(data_type, '(') - 1), data_type) as data_type, data_length, data_precision, data_scale, nullable FROM user_tab_columns WHERE table_name = ? AND column_name = ?";
					try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
						preparedStatement.setString(1, tableName.toUpperCase());
						preparedStatement.setString(2, columnName.toUpperCase());

						try (final ResultSet resultSet = preparedStatement.executeQuery()) {

							if (resultSet.next()) {
								String dataType = resultSet.getString("data_type");

								if (resultSet.wasNull() || dataType.toUpperCase().contains("DATE") || dataType.toUpperCase().contains("TIMESTAMP") || dataType.toUpperCase().contains("CLOB") || dataType.toUpperCase().contains("BLOB")) {
									characterLength = -1;
								} else {
									characterLength = resultSet.getInt("data_length");
								}

								numericPrecision = resultSet.getInt("data_precision");
								if (resultSet.wasNull()) {
									numericPrecision = -1;
								}
								numericScale = resultSet.getInt("data_scale");
								if (resultSet.wasNull()) {
									numericScale = -1;
								}

								isNullable = resultSet.getString("nullable").equalsIgnoreCase("y");
								// Check for special constraints if nullable by user_tab_columns
								if (isNullable && isNotNullByConstraintFields) {
									isNullable = false;
								}

								return new DbColumnType(dataType, characterLength, numericPrecision, numericScale, isNullable);
							} else {
								return null;
							}
						}
					}
	        	} else {
	        		String sql = "SELECT data_type, character_maximum_length, numeric_precision, numeric_scale, is_nullable FROM information_schema.columns WHERE table_schema = SCHEMA() AND lower(table_name) = lower(?) AND lower(column_name) = lower(?)";

	        		try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
						preparedStatement.setString(1, tableName);
						preparedStatement.setString(2, columnName);

						try (final ResultSet resultSet = preparedStatement.executeQuery()) {

							if (resultSet.next()) {
								String dataType = resultSet.getString("data_type");

								if (resultSet.wasNull() || dataType.toUpperCase().contains("DATE") || dataType.toUpperCase().contains("TIMESTAMP") || dataType.toUpperCase().contains("BLOB")) {
									characterLength = -1;
								} else if (dataType.toUpperCase().contains("TINYTEXT") || dataType.toUpperCase().contains("MEDIUMTEXT") || dataType.toUpperCase().contains("TEXT") || dataType.toUpperCase().contains("LONGTEXT")) {
									characterLength = resultSet.getLong("character_maximum_length");
								} else {
									characterLength = resultSet.getLong("character_maximum_length");
								}

								numericPrecision = resultSet.getInt("numeric_precision");
								if (resultSet.wasNull()) {
									numericPrecision = -1;
								}
								numericScale = resultSet.getInt("numeric_scale");
								if (resultSet.wasNull()) {
									if ("float".equalsIgnoreCase(dataType) || "double".equalsIgnoreCase(dataType)) {
										numericScale = 0;
									} else {
										numericScale = -1;
									}
								}
								isNullable = resultSet.getString("is_nullable").equalsIgnoreCase("yes");

								return new DbColumnType(dataType, characterLength, numericPrecision, numericScale, isNullable);
							} else {
								return null;
							}
						}
	        		}
	        	}
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	public static CaseInsensitiveMap<String, DbColumnType> getColumnDataTypes(DataSource dataSource, String tableName) throws Exception {
		if (dataSource == null) {
			throw new RuntimeException("Cannot getColumnDataTypes: dataSource is null");
		}

		try (final Connection connection = dataSource.getConnection()) {
			return getColumnDataTypes(connection, tableName);
		} catch (Exception e) {
			logger.error("Cannot getColumnDataTypes: " + e.getMessage(), e);
			throw new RuntimeException("Cannot getColumnDataTypes: " + e.getMessage(), e);
		}
	}

	public static CaseInsensitiveMap<String, DbColumnType> getColumnDataTypes(Connection connection, String tableName) throws Exception {
		if (connection == null) {
			throw new Exception("Invalid empty connection for getColumnDataTypes");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getColumnDataTypes");
		} else {
			try {
				CaseInsensitiveMap<String, DbColumnType> returnMap = new CaseInsensitiveMap<>();
				if (checkDbVendorIsOracle(connection)) {
					// Read special constraints for not-nullable fields by user_tab_columns
					String nullableSql = "SELECT search_condition FROM all_constraints WHERE table_name = UPPER(?)";
					Set<String> notNullByConstraintFields = new HashSet<>();
					try (final PreparedStatement preparedStatementNullable = connection.prepareStatement(nullableSql)) {
						preparedStatementNullable.setString(1, tableName);
						try (final ResultSet resultSetNullable = preparedStatementNullable.executeQuery()) {
							while (resultSetNullable.next()) {
								String searchCondition = resultSetNullable.getString("search_condition");
								searchCondition = searchCondition != null ? searchCondition.toLowerCase().replace("'", "").replace("\"", "") : "";
								if (searchCondition.endsWith(" is not null")) {
									notNullByConstraintFields.add(searchCondition.substring(0, searchCondition.indexOf(" is not null")).trim());
								}
							}
						}
					}

					// Watchout: Oracle's timestamp datatype is "TIMESTAMP(6)", so remove the bracket value
					String sql = "SELECT column_name, COALESCE(SUBSTR(data_type, 1, INSTR(data_type, '(') - 1), data_type) AS data_type, data_length, data_precision, data_scale, nullable FROM user_tab_columns WHERE table_name = ? ORDER BY column_name";
					try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
						preparedStatement.setString(1, tableName.toUpperCase());

						try (final ResultSet resultSet = preparedStatement.executeQuery()) {
							while (resultSet.next()) {
								String columnName = resultSet.getString("column_name");

								String dataType = resultSet.getString("data_type");

								int characterLength;
								if (resultSet.wasNull() || dataType.toUpperCase().contains("DATE") || dataType.toUpperCase().contains("TIMESTAMP") || dataType.toUpperCase().contains("CLOB") || dataType.toUpperCase().contains("BLOB")) {
									characterLength = -1;
								} else {
									characterLength = resultSet.getInt("data_length");
								}

								int numericPrecision = resultSet.getInt("data_precision");
								if (resultSet.wasNull()) {
									if ("number".equalsIgnoreCase(dataType)) {
										// maximum precision of oracle number is 38 which is represented by null
										numericPrecision = 38;
									} else {
										numericPrecision = -1;
									}
								}
								int numericScale = resultSet.getInt("data_scale");
								if (resultSet.wasNull()) {
									numericScale = -1;
								}

								boolean isNullable = resultSet.getString("nullable").equalsIgnoreCase("y");
								// Check for special constraints if nullable by user_tab_columns
								if (isNullable && notNullByConstraintFields.contains(columnName.toLowerCase())) {
									isNullable = false;
								}

								returnMap.put(columnName, new DbColumnType(dataType, characterLength, numericPrecision, numericScale, isNullable));
							}
						}
					}
	        	} else {
	        		String sql = "SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale, is_nullable FROM information_schema.columns WHERE table_schema = SCHEMA() AND LOWER(table_name) = LOWER(?) ORDER BY column_name";

	        		try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
						preparedStatement.setString(1, tableName);

						try (final ResultSet resultSet = preparedStatement.executeQuery()) {
							while (resultSet.next()) {
								String dataType = resultSet.getString("data_type");

								long characterLength;
								if (resultSet.wasNull() || dataType.toUpperCase().contains("DATE") || dataType.toUpperCase().contains("TIMESTAMP") || dataType.toUpperCase().contains("BLOB")) {
									characterLength = -1;
								} else if (dataType.toUpperCase().contains("TINYTEXT") || dataType.toUpperCase().contains("MEDIUMTEXT") || dataType.toUpperCase().contains("TEXT") || dataType.toUpperCase().contains("LONGTEXT")) {
									characterLength = resultSet.getLong("character_maximum_length");
								} else {
									characterLength = resultSet.getLong("character_maximum_length");
								}

								int numericPrecision = resultSet.getInt("numeric_precision");
								if (resultSet.wasNull()) {
									numericPrecision = -1;
								}
								int numericScale = resultSet.getInt("numeric_scale");
								if (resultSet.wasNull()) {
									numericScale = -1;
								}
								boolean isNullable = resultSet.getString("is_nullable").equalsIgnoreCase("yes");

								returnMap.put(resultSet.getString("column_name"), new DbColumnType(dataType, characterLength, numericPrecision, numericScale, isNullable));
							}
						}
	        		}
	        	}
		        return returnMap;
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public static CaseInsensitiveMap<String, CaseInsensitiveMap<String, DbColumnType>> getColumnDataTypes(DataSource dataSource, Set<String> tableNames) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getColumnDataTypes");
		} else {
			CaseInsensitiveMap<String, CaseInsensitiveMap<String, DbColumnType>> resultMap = new CaseInsensitiveMap<>();
			// Return empty map for empty set
			if (tableNames.size() > 0) {
				for (String tableName : tableNames) {
					resultMap.put(tableName, getColumnDataTypes(dataSource, tableName));
				}
			}
			return resultMap;
		}
	}

	public static int getColumnCount(DataSource dataSource, String tableName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getColumnCount");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getColumnCount");
		} else {
			try (final Connection connection = dataSource.getConnection()) {
		        return getColumnCount(connection, tableName);
			}
		}
	}

	public static int getColumnCount(Connection connection, String tableName) throws Exception {
		if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getColumnCount");
		} else {
			try (Statement stmt = connection.createStatement()) {
		    	String sql = "SELECT * FROM " + SafeString.getSafeDbTableName(tableName) + " WHERE 1 = 0";

		    	try (ResultSet rset = stmt.executeQuery(sql)) {
			        return rset.getMetaData().getColumnCount();
		    	}
			}
		}
	}

	public static int getTableEntriesCount(DataSource dataSource, String tableName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getTableEntriesCount");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getTableEntriesCount");
		} else {
			try (final Connection connection = dataSource.getConnection()) {
		        return getTableEntriesCount(connection, tableName);
			}
		}
	}

	public static int getTableEntriesCount(Connection connection, String tableName) throws Exception {
		if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getTableEntriesNumber");
		} else {
			try (Statement stmt = connection.createStatement()) {
		    	String sql = "SELECT COUNT(*) FROM " + SafeString.getSafeDbTableName(tableName);
		        try (ResultSet rset = stmt.executeQuery(sql)) {
			        if (rset.next()) {
			        	return rset.getInt(1);
			        } else {
			        	return 0;
			        }
		        }
			}
		}
	}

	public static boolean containsColumnName(DataSource dataSource, String tableName, String columnName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for containsColumnName");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for containsColumnName");
		} else if (StringUtils.isBlank(columnName)) {
			throw new Exception("Invalid empty columnName for containsColumnName");
		} else {
			return getColumnNames(dataSource, tableName).contains(columnName.toLowerCase());
		}
	}

	public static String getColumnDefaultValue(DataSource dataSource, String tableName, String columnName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getDefaultValueOf");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getDefaultValueOf");
		} else if (StringUtils.isBlank(columnName)) {
			throw new Exception("Invalid empty columnName for getDefaultValueOf");
		} else {
			if (checkDbVendorIsOracle(dataSource)) {
				String sql = "SELECT data_default FROM user_tab_columns WHERE table_name = ? AND column_name = ?";
				String value = new JdbcTemplate(dataSource).queryForObject(sql, String.class, tableName.toUpperCase(), columnName.toUpperCase());

				if (StringUtils.isNotBlank(value)) {
					// A trailing whitespace appears in Oracle.
					value = StringUtils.removeEnd(value, " ");
				}

				if (StringUtils.equalsIgnoreCase(value, "null")) {
					return null;
				}

				return extractStringLiteral(value);
			} else {
				String sql = "SELECT column_default FROM information_schema.columns WHERE table_schema = SCHEMA() AND table_name = ? AND column_name = ?";
				return new JdbcTemplate(dataSource).queryForObject(sql, String.class, tableName, columnName);
			}
		}
	}

	private static String extractStringLiteral(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}

		if (value.startsWith("'") && value.endsWith("'")) {
			return value.substring(1, value.length() - 1).replace("''", "'");
		}

		return value;
	}

	public static boolean isNowKeyword(String value) {
		switch (StringUtils.trimToEmpty(value).toUpperCase()) {
			case "NOW":
			case "NOW()":
			case "SYSDATE":
			case "SYSDATE()":
			case "CURRENT_TIMESTAMP":
			case "CURRENT_TIMESTAMP()":
				return true;

			default:
				return false;
		}
	}

	public static String getDateDefaultValue(String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean isOracle) throws Exception {
		if (isNowKeyword(fieldDefault)) {
			return "CURRENT_TIMESTAMP";
		} else {
			if (isOracle) {
				// TODO: A fixed date format is not a good solution, should
				// depend on language setting of the user
				/*
				 * Here raise a problem: The default value is not only used for
				 * the ALTER TABLE statement. It is also stored in
				 * customer_field_tbl.default_value as a string. A problem
				 * occurs, when two users with language settings with different
				 * date formats edit the profile field.
				 */
				return "to_date('" + new SimpleDateFormat(DateUtilities.DD_MM_YYYY).format(fieldDefaultDateFormat.parse(fieldDefault)) + "', 'DD.MM.YYYY')";
			} else {
				return "'" + new SimpleDateFormat(DateUtilities.YYYY_MM_DD).format(fieldDefaultDateFormat.parse(fieldDefault)) + "'";
			}
		}
	}

	public static boolean addColumnToDbTable(DataSource dataSource, String tablename, String fieldname, String fieldType, long length, String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean notNull) throws Exception {
		boolean isOracle = checkDbVendorIsOracle(dataSource);

		if (StringUtils.isBlank(fieldname)) {
			return false;
		} else if (!tablename.equalsIgnoreCase(SafeString.getSafeDbTableName(tablename))) {
			logger.error("Cannot create db column: Invalid tablename " + tablename);
			return false;
		} else if (StringUtils.isBlank(fieldname)) {
			return false;
		}  else if (!fieldname.equalsIgnoreCase(SafeString.getSafeDbColumnName(fieldname))) {
			logger.error("Cannot create db column: Invalid fieldname " + fieldname);
			return false;
		} else if (StringUtils.isBlank(fieldType)) {
			return false;
		} else if (containsColumnName(dataSource, tablename, fieldname)) {
			return false;
		} else {
			if (fieldType != null) {
				fieldType = fieldType.toUpperCase().trim();
				if (fieldType.startsWith("VARCHAR")) {
					fieldType = "VARCHAR";
				}
			}

			String dbType;
			if ("NUMERIC".equalsIgnoreCase(fieldType) || "NUMBER".equalsIgnoreCase(fieldType) || "INTEGER".equalsIgnoreCase(fieldType)) {
				if (isOracle) {
					dbType = "NUMBER(38)";
				} else {
					dbType = "INTEGER";
				}
			} else if ("FLOAT".equalsIgnoreCase(fieldType)) {
				if (isOracle) {
					dbType = "NUMBER";
				} else {
					dbType = "FLOAT";
				}
			} else if ("VARCHAR".equalsIgnoreCase(fieldType) || "VARCHAR2".equalsIgnoreCase(fieldType)) {
				if (isOracle) {
					dbType = "VARCHAR2";
				} else {
					dbType = "VARCHAR";
				}
			} else if ("DATE".equalsIgnoreCase(fieldType)) {
				dbType = "DATE";
			} else if ("DATETIME".equalsIgnoreCase(fieldType) || "TIMESTAMP".equalsIgnoreCase(fieldType)) {
				dbType = "TIMESTAMP";
			} else {
				throw new Exception("Invalid fieldtype");
			}

			String addColumnStatement = "ALTER TABLE " + tablename + " ADD (" + fieldname.toLowerCase() + " " + dbType;
			if (fieldType.equalsIgnoreCase("VARCHAR")) {
				if (length <= 0) {
					length = 100;
				}
				if (length > 4000) {
					length = 4000;
				}
				addColumnStatement += "(" + length + ")";
			}

			// Default Value
			if (StringUtils.isNotEmpty(fieldDefault)) {
				if (fieldType.equalsIgnoreCase("VARCHAR")) {
					addColumnStatement += " DEFAULT '" + fieldDefault + "'";
				} else if (fieldType.equalsIgnoreCase("DATE") || fieldType.equalsIgnoreCase("DATETIME")) {
					addColumnStatement += " DEFAULT " + getDateDefaultValue(fieldDefault, fieldDefaultDateFormat, isOracle);
				} else {
					addColumnStatement += " DEFAULT " + fieldDefault;
				}
			} else {
				if(fieldType.equalsIgnoreCase("DATETIME")) {
					if(!notNull) {
						addColumnStatement += " DEFAULT null";
					}
				}
			}

			// Maybe null
			if (notNull) {
				addColumnStatement += " NOT NULL";
			} else {
				if(fieldType.equalsIgnoreCase("DATETIME")) {
					addColumnStatement += " NULL";
				}
			}

			addColumnStatement += ")";

			try {
				new JdbcTemplate(dataSource).update(addColumnStatement);
				return true;
			} catch (Exception e) {
				logger.error("Cannot create db column: " + addColumnStatement, e);
				return false;
			}
		}
	}

	public static boolean dropTable(DataSource dataSource, String tablename) {
		if (StringUtils.isBlank(tablename)) {
			return false;
		} else {
			try {
				new JdbcTemplate(dataSource).execute("TRUNCATE TABLE " + tablename);
				new JdbcTemplate(dataSource).execute("DROP TABLE " + tablename);
				return true;
			} catch (Exception e) {
				logger.error("Cannot truncate/drop table: " + tablename, e);
				return false;
			}
		}
	}
	
	public static boolean dropColumnFromDbTable(DataSource dataSource, String tablename, String fieldname) throws Exception {
		if (StringUtils.isBlank(fieldname)) {
			return false;
		} else if (!tablename.equalsIgnoreCase(SafeString.getSafeDbTableName(tablename))) {
			logger.error("Cannot drop db column: Invalid tablename " + tablename);
			return false;
		} else if (StringUtils.isBlank(fieldname)) {
			return false;
		}  else if (!fieldname.equalsIgnoreCase(SafeString.getSafeDbTableName(fieldname))) {
			logger.error("Cannot drop db column: Invalid fieldname " + fieldname);
			return false;
		} else if (!containsColumnName(dataSource, tablename, fieldname)) {
			return false;
		} else {
			String dropColumnStatement = "ALTER TABLE " + tablename + " DROP COLUMN " + fieldname.toLowerCase();

			try {
				new JdbcTemplate(dataSource).execute(dropColumnStatement);
				return true;
			} catch (Exception e) {
				logger.error("Cannot drop db column: " + dropColumnStatement, e);
				return false;
			}
		}
	}

	public static boolean alterColumnDefaultValueInDbTable(DataSource dataSource, String tablename, String fieldname, String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean notNull) throws Exception {
		return alterColumnTypeInDbTable(dataSource, tablename, fieldname, null, -1, -1, fieldDefault, fieldDefaultDateFormat, notNull);
	}

	public static boolean alterColumnTypeInDbTable(DataSource dataSource, String tablename, String fieldname, String fieldType, int length, int precision, String fieldDefault, SimpleDateFormat fieldDefaultDateFormat, boolean notNull) throws Exception {
		boolean isOracle = checkDbVendorIsOracle(dataSource);

		if (StringUtils.isBlank(fieldname)) {
			return false;
		} else if (!tablename.equalsIgnoreCase(SafeString.getSafeDbTableName(tablename))) {
			logger.error("Cannot create db column: Invalid tablename " + tablename);
			return false;
		} else if (StringUtils.isBlank(fieldname)) {
			return false;
		}  else if (!fieldname.equalsIgnoreCase(SafeString.getSafeDbTableName(fieldname))) {
			logger.error("Cannot create db column: Invalid fieldname " + fieldname);
			return false;
		} else if (!containsColumnName(dataSource, tablename, fieldname)) {
			return false;
		} else {
			boolean dbChangeIsNeeded = false;
			boolean isDefaultChangeOnly = true;

			// ColumnType
			DbColumnType dbType;
			if (StringUtils.isBlank(fieldType)) {
				dbType = getColumnDataType(dataSource, tablename, fieldname);
			} else {
				String tempFieldType = fieldType.toUpperCase().trim();

				if (tempFieldType.startsWith("VARCHAR")) {
					// Bugfix for Oracle: Oracle dialect returns long for varchar
					// Bugfix for MySQL: The jdbc-Driver for mysql maps VARCHAR to longtext. This might be ok in most cases, but longtext doesn't support length restrictions. So the correct type for mysql should be varchar
					dbType = new DbColumnType("VARCHAR", length, -1, -1, !notNull);
				} else {
					String dbTypeString;
					if ("INTEGER".equalsIgnoreCase(fieldType)) {
						if (isOracle) {
							dbTypeString = "NUMBER";
						} else {
							dbTypeString = "INTEGER";
						}
					} else if ("FLOAT".equalsIgnoreCase(fieldType)) {
						if (isOracle) {
							dbTypeString = "NUMBER";
						} else {
							dbTypeString = "FLOAT";
						}
					} else if (fieldType.equalsIgnoreCase("VARCHAR")) {
						if (isOracle) {
							dbTypeString = "VARCHAR2";
						} else {
							dbTypeString = "VARCHAR";
						}
					} else if (fieldType.equalsIgnoreCase("DATE")) {
						dbTypeString = "DATE";
					} else if ("DATETIME".equalsIgnoreCase(fieldType) || "TIMESTAMP".equalsIgnoreCase(fieldType)) {
						dbTypeString = "TIMESTAMP";
					} else {
						throw new Exception("Invalid fieldtype");
					}

					dbType = new DbColumnType(dbTypeString, length, precision, -1, !notNull);
				}
			}

			String changeColumnStatementPart = fieldname.toLowerCase();

			// Datatype, length (only change when fieldType is set)
			if (StringUtils.isNotEmpty(fieldType)) {
				dbChangeIsNeeded = true;
				isDefaultChangeOnly = false;
				if (dbType.getTypeName().toUpperCase().startsWith("VARCHAR")) {
					// varchar datatype
					changeColumnStatementPart += " " + dbType.getTypeName() + "(" + dbType.getCharacterLength() + ")";
				} else if (dbType.getTypeName().toUpperCase().contains("DATE") || dbType.getTypeName().toUpperCase().contains("TIME")) {
					// date or time type
					changeColumnStatementPart += " " + dbType.getTypeName();
				} else {
					// Numeric datatype
					if (dbType.getNumericScale() > -1) {
						changeColumnStatementPart += " " + dbType.getTypeName() + "(" + dbType.getNumericPrecision() + ", " + dbType.getNumericScale() + ")";
					} else {
						changeColumnStatementPart += " " + dbType.getTypeName() + "(" + dbType.getNumericPrecision() + ")";
					}
				}
			}

			// Default value
			String currentDefaultValue = getColumnDefaultValue(dataSource, tablename, fieldname);
			if ((currentDefaultValue == null && fieldDefault != null) || currentDefaultValue != null && !currentDefaultValue.equals(fieldDefault)) {
				dbChangeIsNeeded = true;
				if (fieldDefault == null || "".equals(fieldDefault)) {
					// null value as default
					changeColumnStatementPart += " DEFAULT NULL";
				} else if (dbType.getTypeName().toUpperCase().startsWith("VARCHAR")) {
					// varchar datatype
					changeColumnStatementPart += " DEFAULT '" + fieldDefault.replace("'", "''") + "'";
				} else if (dbType.getTypeName().toUpperCase().contains("DATE") || dbType.getTypeName().toUpperCase().contains("TIME")) {
					// date or time type
					changeColumnStatementPart += " DEFAULT " + getDateDefaultValue(fieldDefault.replace("'", "''"), fieldDefaultDateFormat, isOracle);
				} else {
					// Numeric datatype
					changeColumnStatementPart += " DEFAULT " + fieldDefault.replace("'", "''");
				}
			}

			// Maybe null
			if (dbType.isNullable() == notNull) {
				dbChangeIsNeeded = true;
				isDefaultChangeOnly = false;
				changeColumnStatementPart += " NOT NULL";
			}

			if (dbChangeIsNeeded) {
				String changeColumnStatement;
				if (isOracle) {
					changeColumnStatement = "ALTER TABLE " + tablename + " MODIFY (" + changeColumnStatementPart + ")";
				} else {
					if (isDefaultChangeOnly) {
						changeColumnStatement = "ALTER TABLE " + tablename + " ALTER " + changeColumnStatementPart.replaceFirst("DEFAULT", "SET DEFAULT");
					} else {
						changeColumnStatement = "ALTER TABLE " + tablename + " MODIFY " + changeColumnStatementPart;
					}
				}

				try {
					new JdbcTemplate(dataSource).update(changeColumnStatement);
					return true;
				} catch (Exception e) {
					logger.error("Cannot change db column: " + changeColumnStatement, e);
					return false;
				}
			} else {
				// No change is needed, but everything is OK
				return true;
			}
		}
	}

	public static boolean checkAllowedDefaultValue(String dataType, String defaultValue, SimpleDateFormat dateFormat) {
		if (defaultValue == null) {
			return false;
		} else if ("".equals(defaultValue)) {
			// default value null
			return true;
		} else if (dataType.toUpperCase().contains("DATE") || dataType.toUpperCase().contains("TIME")) {
			if (isNowKeyword(defaultValue)) {
				return true;
			} else {
				try {
					dateFormat.parse(defaultValue);
					return true;
				} catch (ParseException e) {
					return false;
				}
			}
		} else if (dataType.equalsIgnoreCase("NUMBER") || dataType.equalsIgnoreCase("INTEGER") || dataType.equalsIgnoreCase("FLOAT") || dataType.equalsIgnoreCase("DOUBLE")) {
			return AgnUtils.isNumberValid(defaultValue);
		} else {
			return true;
		}
	}

	public static String getToDateString_Oracle(int day, int month, int year, int hour, int minute, int second) {
		StringBuilder stringBuilder = new StringBuilder("TO_DATE('");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(day));
		stringBuilder.append(".");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(month));
		stringBuilder.append(".");
		stringBuilder.append(FOUR_DIGIT_FORMAT.format(year));
		stringBuilder.append(" ");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(hour));
		stringBuilder.append(":");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(minute));
		stringBuilder.append(":");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(second));
		stringBuilder.append("', 'DD.MM.YYYY HH24:MI:SS')");
		return stringBuilder.toString();
	}

	public static String getToDateString_MySQL(int day, int month, int year, int hour, int minute, int second) {
		StringBuilder stringBuilder = new StringBuilder("STR_TO_DATE('");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(day));
		stringBuilder.append("-");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(month));
		stringBuilder.append("-");
		stringBuilder.append(FOUR_DIGIT_FORMAT.format(year));
		stringBuilder.append(" ");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(hour));
		stringBuilder.append(":");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(minute));
		stringBuilder.append(":");
		stringBuilder.append(TWO_DIGIT_FORMAT.format(second));
		stringBuilder.append("', '%d-%m-%Y %H:%i:%s')");
		return stringBuilder.toString();
	}

	public static String convertSimpleDateFormat(boolean isOracle, String pattern) {
		if (isOracle) {
			return convertSimpleDateFormatToOracleDateFormat(pattern);
		} else {
			return convertSimpleDateFormatToMysqlDateFormat(pattern);
		}
	}

	private static String convertSimpleDateFormatToOracleDateFormat(String pattern) {
		// dd.MM.yyyy HH:mm:ss -> DD.MM.YYYY HH24:MI:SS

		return pattern
			.replace("dd", "DD")
			.replace("yyyy", "YYYY")
			.replace("HH", "HH24")
			.replace("mm", "MI")
			.replace("ss", "SS");
	}

	private static String convertSimpleDateFormatToMysqlDateFormat(String pattern) {
		// dd.MM.yyyy HH:mm:ss -> %d.%m.%Y %H:%i:%s

		return pattern
			.replace("dd", "%d")
			.replace("MM", "%m")
			.replace("yyyy", "%Y")
			.replace("HH", "%H")
			.replace("mm", "%i")
			.replace("ss", "%s");
	}

	public static int[] getSqlTypes(Object[] values) {
		int[] returnTypes = new int[values.length];
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			if (value == null) {
				returnTypes[i] = Types.NULL;
			} else if (value instanceof String) {
				returnTypes[i] = Types.VARCHAR;
			} else if (value instanceof Integer) {
				returnTypes[i] = Types.INTEGER;
			} else if (value instanceof Double) {
				returnTypes[i] = Types.DOUBLE;
			} else if (value instanceof Date || value instanceof Timestamp || value instanceof java.sql.Date) {
				returnTypes[i] = Types.TIMESTAMP;
			} else {
				returnTypes[i] = Types.VARCHAR;
			}
		}
        return returnTypes;
	}

	public static boolean renameTableField(DataSource dataSource, String tableName, String fieldNameOld, String fieldNameNew) throws Exception {
		String changeColumnStatement;
		if (checkDbVendorIsOracle(dataSource)) {
			changeColumnStatement = "ALTER TABLE " + tableName + " RENAME COLUMN " + fieldNameOld + " TO " + fieldNameNew;
		} else {
			DbColumnType columnType = getColumnDataType(dataSource, tableName, fieldNameOld);
			changeColumnStatement = "ALTER TABLE " + tableName + " CHANGE " + fieldNameOld + " " + fieldNameNew + " " + columnType.getTypeName() + (columnType.getSimpleDataType() == SimpleDataType.Characters ? "(" + columnType.getCharacterLength() + ")" : "");
		}

		try {
			new JdbcTemplate(dataSource).update(changeColumnStatement);
			return true;
		} catch (Exception e) {
			logger.error("Cannot rename db column: " + changeColumnStatement, e);
			return false;
		}
	}

	public static boolean checkOracleTablespaceExists(DataSource dataSource, String tablespaceName) {
		if (checkDbVendorIsOracle(dataSource) && tablespaceName != null) {
			String statement = "SELECT COUNT(*) FROM user_tablespaces WHERE LOWER(tablespace_name) = ?";
			return new JdbcTemplate(dataSource).queryForObject(statement, Integer.class, tablespaceName.toLowerCase()) > 0;
		} else {
			return false;
		}
	}

	public static int getMysqlMaxAllowedPacketSize(DataSource dataSource) {
		List<Map<String,Object>> result = new JdbcTemplate(dataSource).queryForList("SHOW VARIABLES like 'max_allowed_packet'");
		if (result != null && result.size() > 0) {
			Map<String, Object> packetMap = result.get(0);
			Object value = packetMap.get("value");
			if (value != null) {
				return Integer.parseInt(value.toString());
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static void checkDatasourceConnection(DataSource dataSource) throws Exception {
		if (dataSource == null) {
			throw new Exception("Cannot acquire datasource");
		} else {
			try (Connection connection = dataSource.getConnection()) {
				// do nothing, just checking
			} catch (Exception e) {
				BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(dataSource.getClass());
				String username = null;
				String password = null;
				String url = null;
				String driverClassName = null;
				for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
					if ("username".equals(propertyDescriptor.getName())) {
						username = (String) propertyDescriptor.getReadMethod().invoke(dataSource);
					} else if ("password".equals(propertyDescriptor.getName())) {
						password = (String) propertyDescriptor.getReadMethod().invoke(dataSource);
					} else if ("url".equals(propertyDescriptor.getName())) {
						url = (String) propertyDescriptor.getReadMethod().invoke(dataSource);
					} else if ("driverClassName".equals(propertyDescriptor.getName())) {
						driverClassName = (String) propertyDescriptor.getReadMethod().invoke(dataSource);
					}
				}

				if (StringUtils.isEmpty(username)){
					throw new Exception("Cannot acquire connection: Missing username");
				} else if (StringUtils.isEmpty(password)){
					throw new Exception("Cannot acquire connection: Missing password");
				} else if (url == null || StringUtils.isBlank(url)) {
					throw new Exception("Cannot acquire connection: Missing Url");
				} else {
					try {
						Class.forName(driverClassName);
					} catch (Exception e1) {
						throw new Exception("Cannot acquire connection, caused by unknown DriverClassName: " + e1.getMessage(), e1);
					}

					try {
						String hostname;
						String portString;
						if (url.toLowerCase().contains("oracle")) {
							String[] urlParts = url.split(":");
							hostname = urlParts[urlParts.length - 3];
							if (hostname.contains("@")) {
								hostname = hostname.substring(hostname.indexOf("@") + 1);
							}
							portString = urlParts[urlParts.length - 2];
						} else {
							String[] urlParts = url.split("/"); // jdbc:mysql://localhost/emm?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8
							hostname = urlParts[urlParts.length - 2];
							if (hostname.contains(":")) {
								portString = hostname.substring(hostname.indexOf(":") + 1);
								hostname = hostname.substring(0, hostname.indexOf(":"));
							} else {
								portString = "3306";
							}
						}
						int port = Integer.parseInt(portString);
						AgnUtils.checkHostConnection(hostname, port);
					} catch (Exception e1) {
						throw new Exception("Cannot acquire connection, caused by Url: " + e1.getMessage(), e1);
					}

					throw new Exception("Cannot acquire connection: " + e.getMessage());
				}
			}
		}
	}

	public static String convertOracleDateFormatToMySqlDateFormat(String oracleDateFormat) {
		String mySqlDateFormat = oracleDateFormat;
		mySqlDateFormat = mySqlDateFormat.replace("YYYY", "%Y");
		mySqlDateFormat = mySqlDateFormat.replace("YY", "%y");
		mySqlDateFormat = mySqlDateFormat.replace("MM", "%m");
		mySqlDateFormat = mySqlDateFormat.replace("DD", "%d");
		mySqlDateFormat = mySqlDateFormat.replace("HH24", "%H");
		mySqlDateFormat = mySqlDateFormat.replace("MI", "%i");
		mySqlDateFormat = mySqlDateFormat.replace("SS", "%s");
		return mySqlDateFormat;
	}

	/**
	 * Generate a search expression (for WHERE clause) using one or more LIKE operator(s).
	 * All the special characters will be escaped to make sure that there's no SQL-injection possible via {@code query} parameter.
	 *
	 * The call makeSimpleLikeClause("name", "abc 123 xyz") will generate an expression as follows:
	 * (name LIKE '%abc%' OR name LIKE '%123%' OR name LIKE '%xyz%')
	 *
	 * @param column a text (char, varchar, clob) column's name to scan.
	 * @param query a search query to be treated as a space-separated list of wildcard-prefixed and wildcard-suffixed words.
	 * @param databaseIsOracle whether ({@code true}) or not ({@code false}) database is Oracle.
	 * @return an SQL-expression or {@code null}.
	 */
	public static String makeSimpleLikeClause(String column, String query, boolean databaseIsOracle) {
		column = StringUtils.trimToNull(column);
		query = StringUtils.trimToNull(query);

		if (column == null || query == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		String escape = databaseIsOracle ? " ESCAPE '\\'" : "";

		String[] words = query
				.replace("'", "\\'")
				.replace("_", "\\_")
				.replace("%", "\\%")
				.split("\\s+");

		if (words.length > 1) {
			sb.append('(');
		}

		for (int i = 0; i < words.length; i++) {
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append(column).append(" LIKE '%").append(words[i]).append("%'").append(escape);
		}

		if (words.length > 1) {
			sb.append(')');
		}

		return sb.toString();
	}

	/**
	 * Escape all special characters for LIKE operator's operand.
	 *
	 * @param query a string to escape.
	 * @param escapeChar a character to be used to escape special characters.
	 * @return an escaped string.
	 */
	public static String escapeLikeExpression(String query, char escapeChar) {
		if (StringUtils.isBlank(query)) {
			return query;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);

			if (c == '_' || c == '%' || c == escapeChar) {
				sb.append(escapeChar);
			}

			sb.append(c);
		}

		return sb.toString();
	}

	public static <T> String joinForIN(Collection<T> elements, Function<T, String> property){
		StringBuilder query = new StringBuilder();
		query.append("(");
		query.append(elements.stream().map(element -> "'" + property.apply(element)+"'").collect(Collectors.joining(",")));
		query.append(")");
		return query.toString();
	}

	public static <T> String joinForIN(T[] elements, Function<T, String> property){
		Function<T, String> propertyWithQuotes = element -> "'" + property.apply(element)+"'";
		return joinForInWithoutQuotes(elements, propertyWithQuotes);
	}

	public static <T> String joinForInWithoutQuotes(T[] elements, Function<T, String> property){
		StringBuilder query = new StringBuilder();
		query.append("(");
		query.append(Arrays.stream(elements).map(property).collect(Collectors.joining(",")));
		query.append(")");
		return query.toString();
	}

	public static <T> String joinForIn(Collection<T> elements) {
		if (CollectionUtils.isEmpty(elements)) {
			return "";
		} else {
			return StringUtils.join(elements, ',');
		}
	}

	public static String asCondition(String format, CompaniesConstraints constraints, String companyIdColumn) {
		Objects.requireNonNull(format);
		Objects.requireNonNull(companyIdColumn);

		if (constraints == null) {
			return "";
		}

		String includedIds = joinForIn(constraints.getIncludedIds());
		String excludedIds = joinForIn(constraints.getExcludedIds());

		if (includedIds.isEmpty() && excludedIds.isEmpty()) {
			return "";
		}

		if (excludedIds.isEmpty()) {
			return String.format(format, companyIdColumn + " IN (" + includedIds + ")");
		}

		if (includedIds.isEmpty()) {
			return String.format(format, companyIdColumn + " NOT IN (" + excludedIds + ")");
		}

		return String.format(format, companyIdColumn + " IN (" + includedIds + ") AND " + companyIdColumn + " NOT IN (" + excludedIds + ")");
	}

	public static String asCondition(String format, CompaniesConstraints constraints) {
		return asCondition(format, constraints, "company_id");
	}

	/**
	 * Add required clauses to add pagination to a selection query {@code sqlSelection}.
	 *
	 * @param sqlSelection an original selection query.
	 * @param page a 1-based number of a page to select.
	 * @param pageSize rows per page.
	 * @param databaseIsOracle whether ({@code true}) or not ({@code false}) an Oracle is target database.
	 * @param sqlParameters a collection to append bound parameters related to pagination.
	 * @return generated selection query.
	 */
	public static String selectRowsPage(String sqlSelection, int page, int pageSize, boolean databaseIsOracle, Collection<Object> sqlParameters) {
		int param1, param2;

		if (page <= 0 || pageSize <= 0) {
			return null;
		}

		if (databaseIsOracle) {
			param1 = (page - 1) * pageSize + 1;
			param2 = param1 + pageSize - 1;
			sqlSelection = "SELECT * FROM (" +
					"SELECT ROWNUM row_index, sorted_selection.* FROM (" +  sqlSelection + ") sorted_selection" +
					") WHERE row_index BETWEEN ? AND ?";
		} else {
			param1 = (page - 1) * pageSize;
			param2 = pageSize;
			sqlSelection += " LIMIT ?, ?";
		}

		sqlParameters.add(param1);
		sqlParameters.add(param2);

		return sqlSelection;
	}

	/**
	 * Validate a search query (see {@link com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator#generateSpecificQuery(String)} ).
	 * @param queryText the end-user typed query text.
	 * @return a list of error messages or an empty list.
     */
	public static List<String> validateFulltextSearchQueryText(String queryText) {
		List<String> errors = new ArrayList<>();

		if (StringUtils.isNotEmpty(queryText)) {
			boolean quote = false;
			int parentheses = 0;

			boolean disorderedParentheses = false;

			for (int i = 0; i < queryText.length(); ++i) {
				char c = queryText.charAt(i);
				switch (c) {
					case '(':
						parentheses++;
						break;

					case ')':
						parentheses--;
						if (parentheses < 0) {  // E.g. ')('
							disorderedParentheses = true;
						}
						break;

					case '"':
						quote = !quote;
						break;
					default:
						// do nothing
				}
			}

			if (disorderedParentheses) {
				errors.add("mailing.search.UnpairedParentheses");
			}

			if (quote) {
				errors.add("mailing.search.UnpairedQuotes");
			}

			if (parentheses != 0) {
				errors.add("mailing.search.UnpairedParentheses");
			}
		}

		return errors;
	}

	public static boolean checkForIndex(DataSource dataSource, String tableName, List<String> keyColumns) {
    	// There is a result line for each keycolumn in both dbtypes. Maybe we should check the field "columnindex" too. But for now it should do this way
        if (checkDbVendorIsOracle(dataSource)) {
        	for (String keyColumn : keyColumns) {
	            String query = "SELECT COUNT(*) FROM user_ind_columns WHERE LOWER(table_name) = ? AND LOWER(column_name) = ?";
	            int totalCount = new JdbcTemplate(dataSource).queryForObject(query, Integer.class, tableName.toLowerCase(), keyColumn.toLowerCase());
	            if (totalCount <= 0) {
	            	return false;
	            }
        	}
            return true;
        } else {
        	for (String keyColumn : keyColumns) {
	            String query = "SHOW INDEX FROM " + tableName.toLowerCase() + " WHERE column_name = ?";

				final List<Map<String, Object>> resultList = new JdbcTemplate(dataSource).queryForList(query, keyColumn.toLowerCase());
	            if (resultList.size() <= 0){
	                return false;
	            }
        	}
            return true;
        }
    }

	public static List<String> getTableIndexNames(DataSource dataSource, String tableName) {
    	if (checkDbVendorIsOracle(dataSource)) {
            String query = "SELECT index_name FROM user_ind_columns WHERE LOWER(table_name) = ?";
            return new JdbcTemplate(dataSource).queryForList(query, String.class, tableName.toLowerCase());
        } else {
            String query = "SHOW INDEX FROM " + tableName.toLowerCase();
			final List<Map<String, Object>> result = new JdbcTemplate(dataSource).queryForList(query);
			List<String> returnList = new ArrayList<>();
            for (Map<String, Object> row : result) {
            	returnList.add((String) row.get("table_name"));
            }
            return returnList;
        }
    }

	public static CaseInsensitiveSet getPrimaryKeyColumns(DataSource dataSource, String tableName) throws SQLException {
		try (final Connection connection = dataSource.getConnection()) {
			return getPrimaryKeyColumns(connection, tableName);
		}
	}

	public static CaseInsensitiveSet getPrimaryKeyColumns(Connection connection, String tableName) throws SQLException {
		if (StringUtils.isBlank(tableName)) {
			return null;
		} else {
			try {
				if (checkDbVendorIsOracle(connection)) {
					tableName = tableName.toUpperCase();
				}

				DatabaseMetaData metaData = connection.getMetaData();
				try (ResultSet resultSet = metaData.getPrimaryKeys(connection.getCatalog(), null, tableName)) {
					CaseInsensitiveSet returnList = new CaseInsensitiveSet();
					while (resultSet.next()) {
						returnList.add(resultSet.getString("COLUMN_NAME"));
					}
					return returnList;
				}
			} catch (Exception e) {
				throw new RuntimeException("Cannot read primarykey columns for table " + tableName + ": " + e.getMessage(), e);
			}
		}
	}

	public static String getDateConstraint(String fieldName, Date dateBegin, Date dateEnd, boolean isOracleDB) {
		StringBuilder dateCondition = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		if (dateBegin == null && dateEnd == null) {
			return "";
		}

		dateCondition.append(fieldName);

		if (isOracleDB) {
			if (dateEnd == null) {
				dateCondition.append(" > TO_DATE('").append(dateFormat.format(dateBegin)).append("', 'yyyy-mm-dd') ");
			} else if (dateBegin == null) {
				dateCondition.append(" < (TO_DATE('").append(dateFormat.format(dateEnd)).append("', 'yyyy-mm-dd') + 1) ");
			} else {
				dateCondition.append(" BETWEEN TO_DATE('").append(dateFormat.format(dateBegin))
						.append("', 'yyyy-mm-dd') AND (TO_DATE('").append(dateFormat.format(dateEnd))
						.append("', 'yyyy-mm-dd') + 1) ");
			}
		} else {
			if (dateEnd == null) {
				dateCondition.append(" > STR_TO_DATE('").append(dateFormat.format(dateBegin)).append("', '%Y-%m-%d') ");
			} else if (dateBegin == null) {
				dateCondition.append(" < STR_TO_DATE('").append(dateFormat.format(dateEnd)).append("', '%Y-%m-%d') + INTERVAL 1 DAY ");
			} else {
				dateCondition.append(" BETWEEN STR_TO_DATE('").append(dateFormat.format(dateBegin))
						.append("', '%Y-%m-%d') AND STR_TO_DATE('").append(dateFormat.format(dateEnd))
						.append("', '%Y-%m-%d') + INTERVAL 1 DAY ");
			}
		}

		return  dateCondition.toString();
	}

	public static PreparedStatement setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
		int counter = 0;
		for (Object parameter : parameters) {
			statement.setObject(++counter, parameter);
		}
		return statement;
	}

	public static <T> String makeBulkInClauseWithDelimiter(boolean isOracle, String columnName, Collection<T> values, String delimiter) {
		final String elementDelimiter = delimiter == null ? "" : delimiter;
		boolean splittingRequired;
		int entriesCountLimit;

		// Remove duplicates from collection
		Set<T> valueSet = new HashSet<>(values);

		if (isOracle) {
			// Oracle allows to use at most 1000 entries in a single IN clause
			entriesCountLimit = 1000;
			splittingRequired = valueSet.size() > entriesCountLimit;
		} else {
			// MySQL: The number of values in the IN list is only limited by the
			// max_allowed_packet value.
			entriesCountLimit = valueSet.size();
			splittingRequired = false;
		}

		List<Set<T>> choppedSets;
		if (splittingRequired) {
			choppedSets = AgnUtils.chopToChunks(valueSet, entriesCountLimit);
		} else {
			choppedSets = new ArrayList<>();
			choppedSets.add(valueSet);
		}

		final StringBuilder clauseBuilder = new StringBuilder();
		boolean firstList = true;
		clauseBuilder.append("(");
		for (Set<T> set : choppedSets) {
			if (!firstList) {
				clauseBuilder.append(" OR ");
			}
			clauseBuilder.append(columnName);
			clauseBuilder.append(" IN (");

			boolean firstElement = true;
			for (T element : set) {
				if (!firstElement) {
					clauseBuilder.append(",");
				}
				clauseBuilder.append(elementDelimiter).append(element).append(elementDelimiter);

				firstElement = false;
			}
			clauseBuilder.append(")");

			firstList = false;
		}
		clauseBuilder.append(")");

		return clauseBuilder.toString();
	}

	public static String makeSelectRangeOfDates(String fieldName, Date fromDate, Date toDate, boolean isOracle) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (fromDate == null || toDate == null) {
			throw new IllegalArgumentException("Could not create SQL without date limits");
		}

		Date dateBegin = fromDate;
		Date dateEnd = toDate;

		// check dateBegin is before dateEnd
		if (dateBegin.after(dateEnd)) {
			dateBegin = toDate;
			dateEnd = fromDate;
		}

		if (isOracle) {
			return String.format("SELECT TRUNC(TO_DATE('%2$s', 'yyyy-mm-dd') + 1 - ROWNUM) " + fieldName +
					" FROM DUAL " +
					" CONNECT BY ROWNUM <= (TO_DATE('%2$s', 'yyyy-mm-dd') + 1 - TO_DATE('%1$s', 'yyyy-mm-dd'))",
					dateFormat.format(dateBegin), dateFormat.format(dateEnd));
		} else {
			return "SELECT * FROM" +
					"(SELECT ADDDATE('1970-01-01',t4*10000 + t3*1000 + t2*100 + t1*10 + t0) " + fieldName + " FROM " +
					" (SELECT 0 t0 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t0, " +
					" (SELECT 0 t1 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1, " +
					" (SELECT 0 t2 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2, " +
					" (SELECT 0 t3 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3, " +
					" (SELECT 0 t4 UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4) v " +
					" WHERE v." + fieldName + " BETWEEN STR_TO_DATE('" + dateFormat.format(dateBegin) + "', '%Y-%m-%d') AND STR_TO_DATE('" + dateFormat.format(dateEnd) + "', '%Y-%m-%d')";
		}
	}

	/**
	 * Checks, if given table name references a view.
	 *
	 * @param tableName table name
	 * @param dataSource datasource
	 *
	 * @return <code>true</code> if table is a view
	 */
	public static final boolean checkTableIsView(final String tableName, final DataSource dataSource) {
		if(checkDbVendorIsOracle(dataSource)) {
			final JdbcTemplate template = new JdbcTemplate(dataSource);
			final int count = template.queryForObject("SELECT count(*) FROM all_views WHERE view_name=?", Integer.class, tableName.toUpperCase());

			return count > 0;
		} else {
			final JdbcTemplate template = new JdbcTemplate(dataSource);
			final int count = template.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_name=? AND table_type='VIEW'", Integer.class, tableName);

			return count > 0;
		}
	}

	public static final boolean dropSequenceIfExists(String sequenceName, final DataSource dataSource) {
		if (checkDbVendorIsOracle(dataSource)) {
			final JdbcTemplate template = new JdbcTemplate(dataSource);
			try {
		    	int foundSequences = template.queryForObject("SELECT COUNT(*) FROM all_sequences WHERE sequence_name = ?", Integer.class, sequenceName.toUpperCase());
		    	if (foundSequences > 0) {
		    		template.execute("DROP SEQUENCE " + sequenceName);
		    		return true;
		    	} else {
		    		return true;
		    	}
			} catch (Exception e) {
				return false;
			}
		} else {
			return true;
		}
	}

	public static final boolean isSelectStatement(String sqlStatement) {
		return StringUtils.startsWithIgnoreCase(sqlStatement, "select ")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "select\t")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "select\n")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "select\r")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "with ")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "with(")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "with\t")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "with\n")
				|| StringUtils.startsWithIgnoreCase(sqlStatement, "with\r");
	}

	public static final boolean resultsetHasColumn(ResultSet resultSet, String columnNameToSearch) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int numberOfColumns = metaData.getColumnCount();
		for (int i = 1; i < numberOfColumns + 1; i++) {
		    String columnName = metaData.getColumnLabel(i);
		    if (StringUtils.equalsIgnoreCase(columnNameToSearch, columnName)) {
		        return true;
		    }
		}
		return false;
	}

	/**
	 * Convert '?' and '*' into SQL LIKE placeholders '_' and '%', but keeps them if they are escaped by '\'
	 *
	 * @param searchText
	 * @return
	 */
	public static String normalizeSqlLikeSearchPlaceholders(String searchText) {
		if (searchText == null) {
			return searchText;
		} else {
			char[] searchTextArray = searchText.toCharArray();
			StringBuilder searchTextBuilder = new StringBuilder();
			boolean escapeNextChar = false;
			for (int i = 0; i < searchTextArray.length; i++) {
				char nextChar = searchTextArray[i];
				if (escapeNextChar) {
					searchTextBuilder.append(nextChar);
					escapeNextChar = false;
				} else if ('\\' == nextChar) {
					searchTextBuilder.append('\\');
					escapeNextChar = true;
				} else if ('?' == nextChar) {
					searchTextBuilder.append('_');
				} else if ('*' == nextChar) {
					searchTextBuilder.append('%');
				} else {
					searchTextBuilder.append(nextChar);
				}
			}
			String returnSearchText = searchTextBuilder.toString();
			returnSearchText = returnSearchText.replace("\\?", "?").replace("\\*", "*");
			return returnSearchText;
		}
	}

	/**
	 * Convert '_' and '%' into FullText search placeholders '?' and '*', but keeps them if they are escaped by '\'
	 *
	 * @param searchText
	 * @return
	 */
	public static String normalizeSqlFullTextSearchPlaceholders(String searchText) {
		if (searchText == null) {
			return searchText;
		} else {
			char[] searchTextArray = searchText.toCharArray();
			StringBuilder searchTextBuilder = new StringBuilder();
			boolean escapeNextChar = false;
			for (int i = 0; i < searchTextArray.length; i++) {
				char nextChar = searchTextArray[i];
				if (escapeNextChar) {
					searchTextBuilder.append(nextChar);
					escapeNextChar = false;
				} else if ('\\' == nextChar) {
					searchTextBuilder.append('\\');
					escapeNextChar = true;
				} else if ('_' == nextChar) {
					searchTextBuilder.append('?');
				} else if ('%' == nextChar) {
					searchTextBuilder.append('*');
				} else {
					searchTextBuilder.append(nextChar);
				}
			}
			String returnSearchText = searchTextBuilder.toString();
			returnSearchText = returnSearchText.replace("\\?", "?").replace("\\*", "*");
			return returnSearchText;
		}
	}

	public static boolean containsSqlLikeSearchPlaceholders(String searchText) {
		if (searchText == null) {
			return false;
		} else {
			char[] searchTextArray = searchText.toCharArray();
			boolean escapeNextChar = false;
			for (int i = 0; i < searchTextArray.length; i++) {
				char nextChar = searchTextArray[i];
				if (escapeNextChar) {
					escapeNextChar = false;
				} else if ('\\' == nextChar) {
					escapeNextChar = true;
				} else if ('_' == nextChar || '%' == nextChar) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Checks if a where-clause only contains rules like "1=1"
	 */
	public static boolean isTautologicWhereClause(String whereClause) {
		if (StringUtils.isBlank(whereClause)) {
			return true;
		} else {
			whereClause = AgnUtils.removeObsoleteEnclosingBrackets(whereClause);
			if ("1=1".equals(whereClause.trim().replace(" ", ""))) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public static final String escapeSinglesQuotes(final String string) {
		final StringBuffer buffer = new StringBuffer();
		String remaining = string;
		
		int index;
		while((index = remaining.indexOf('\'')) != -1) {
			buffer.append(remaining.subSequence(0, index));
			buffer.append("''");
			remaining = remaining.substring(index + 1);
		}
		
		buffer.append(remaining);
		
		return buffer.toString();
	}

	public static boolean checkForeignKeyExists(DataSource dataSource, String tableName, String referencedTableName) throws Exception {
		try (final Connection connection = dataSource.getConnection()) {
			return checkForeignKeyExists(connection, tableName, referencedTableName);
		}
	}

	public static boolean checkForeignKeyExists(Connection connection, String tableName, String referencedTableName) throws Exception {
		if (connection == null) {
			throw new Exception("Connection for checkForeignKeyExists is null");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("TableName for checkForeignKeyExists is empty");
		} else if (StringUtils.isBlank(referencedTableName)) {
			throw new Exception("Referenced tableName for checkForeignKeyExists is empty");
		} else if (checkDbVendorIsOracle(connection)) {
			throw new Exception("checkForeignKeyExists for Oracle databases is not supported yet");
		} else {
			try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.key_column_usage WHERE table_schema = SCHEMA() AND table_name = ? AND referenced_table_name = ?")) {
				preparedStatement.setString(1, tableName.toLowerCase());
				preparedStatement.setString(2, referencedTableName.toLowerCase());
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					resultSet.next();
					return resultSet.getInt(1) > 0;
				} catch (Exception e) {
					throw new Exception("Cannot check foreign keys: " + e.getMessage(), e);
				}
			}
		}
	}
	
	public static boolean checkIfTableOrSynonymExists(DataSource dataSource, String tableOrSynonymName) {
		if (dataSource == null) {
			return false;
		} else if (StringUtils.isBlank(tableOrSynonymName)) {
			return false;
		} else {
			try (final Connection connection = dataSource.getConnection()) {
				return checkIfTableOrSynonymExists(connection, tableOrSynonymName);
			} catch (SQLException e) {
				return false;
			}
		}
	}

	public static boolean checkIfTableOrSynonymExists(Connection connection, String tableOrSynonymName) {
		if (connection == null) {
			return false;
		} else if (StringUtils.isBlank(tableOrSynonymName)) {
			return false;
		} else {
			if (checkIfTableExists(connection, tableOrSynonymName)) {
				return true;
			} else {
				if (checkDbVendorIsOracle(connection)) {
					try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM all_synonyms WHERE synonym_name = ?")) {
						preparedStatement.setString(1, tableOrSynonymName.toUpperCase());
						try (ResultSet resultSet = preparedStatement.executeQuery()) {
							resultSet.next();
							return resultSet.getInt(1) > 0;
						} catch (Exception e) {
							return false;
						}
					} catch (Exception e) {
						return false;
					}
				} else {
					// There are no synonyms in mysql/mariadb
					return false;
				}
			}
		}
	}

	public static boolean checkIfConstraintExists(DataSource dataSource, String constraintName) {
		if (dataSource == null) {
			return false;
		} else if (StringUtils.isBlank(constraintName)) {
			return false;
		} else {
			try (final Connection connection = dataSource.getConnection()) {
				return checkIfConstraintExists(connection, constraintName);
			} catch (SQLException e) {
				return false;
			}
		}
	}

	public static boolean checkIfConstraintExists(Connection connection, String constraintName) {
		if (connection == null) {
			return false;
		} else if (StringUtils.isBlank(constraintName)) {
			return false;
		} else {
			if (checkDbVendorIsOracle(connection)) {
				try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM all_constraints WHERE LOWER(constraint_name) = LOWER(?)")) {
					preparedStatement.setString(1, constraintName);
					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						resultSet.next();
						return resultSet.getInt(1) > 0;
					} catch (Exception e) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			} else {
				try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.key_column_usage WHERE LOWER(constraint_name) = LOWER(?)")) {
					preparedStatement.setString(1, constraintName);
					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						resultSet.next();
						return resultSet.getInt(1) > 0;
					} catch (Exception e) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		}
	}

	public static boolean dropForeignKeyIfExists(DataSource dataSource, String tableName, String foreignKeyName) {
		if (dataSource == null) {
			return false;
		} else if (StringUtils.isBlank(tableName)) {
			return false;
		} else if (StringUtils.isBlank(foreignKeyName)) {
			return false;
		} else {
			try (final Connection connection = dataSource.getConnection()) {
				return dropForeignKeyIfExists(connection, tableName, foreignKeyName);
			} catch (SQLException e) {
				return false;
			}
		}
	}

	public static boolean dropForeignKeyIfExists(Connection connection, String tableName, String foreignKeyName) {
		if (connection == null) {
			return false;
		} else if (StringUtils.isBlank(tableName)) {
			return false;
		} else if (StringUtils.isBlank(foreignKeyName)) {
			return false;
		} else {
			if (checkDbVendorIsOracle(connection)) {
				try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM all_constraints WHERE LOWER(table_name) = LOWER(?) AND LOWER(constraint_name) = LOWER(?)")) {
					preparedStatement.setString(1, tableName);
					preparedStatement.setString(2, foreignKeyName);
					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						resultSet.next();
						if (resultSet.getInt(1) == 0) {
							return false;
						}
					} catch (Exception e) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}

				// Oracle needs the key word "CONSTRAINT"
				try (PreparedStatement preparedStatement = connection.prepareStatement("ALTER TABLE " + tableName + " DROP CONSTRAINT " + foreignKeyName)) {
					preparedStatement.execute();
					return true;
				} catch (Exception e) {
					return false;
				}
			} else {
				try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM information_schema.key_column_usage WHERE LOWER(table_name) = LOWER(?) AND LOWER(constraint_name) = LOWER(?)")) {
					preparedStatement.setString(1, tableName);
					preparedStatement.setString(2, foreignKeyName);
					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						resultSet.next();
						if (resultSet.getInt(1) == 0) {
							return false;
						}
					} catch (Exception e) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}

				// MySQL needs the key word "FOREIGN KEY"
				try (PreparedStatement preparedStatement = connection.prepareStatement("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + foreignKeyName)) {
					preparedStatement.execute();
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		}
	}

	public static String joinColumnsNames(final Collection<String> columns, final boolean addCommaBefore) {
		if(CollectionUtils.isEmpty(columns)) {
			return StringUtils.EMPTY;
		}

		String result = StringUtils.join(columns, ", ") + " ";
		return addCommaBefore ? ", " + result : result;
	}

	public static String joinColumnsNamesForUpdate(final Collection<String> columns, final boolean addCommaBefore) {
		if(CollectionUtils.isEmpty(columns)) {
			return StringUtils.EMPTY;
		}

		String result = StringUtils.join(columns, " = ?, ") + " = ? ";
		return addCommaBefore ? ", " + result : result;
	}

	public static long getTableStorageSize(DataSource dataSource, String tableName) throws Exception {
		if (dataSource == null) {
			throw new Exception("Invalid empty dataSource for getTableStorageSize");
		} else if (StringUtils.isBlank(tableName)) {
			throw new Exception("Invalid empty tableName for getTableStorageSize");
		} else {
			try {
				try (final Connection connection = dataSource.getConnection()) {
					if (checkDbVendorIsOracle(connection)) {
						List<String> indexNames = getTableIndexNames(dataSource, tableName);
						String sql = "SELECT SUM(bytes) FROM user_segments WHERE segment_name in (SELECT segment_name FROM user_lobs WHERE table_name = UPPER(?)) OR segment_name IN (UPPER(?)" + AgnUtils.repeatString(", UPPER(?)", indexNames.size()) + ")";
						try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
							preparedStatement.setString(1, tableName);
							preparedStatement.setString(2, tableName);
							for (int i = 0; i < indexNames.size(); i++) {
								preparedStatement.setString(3 + i, indexNames.get(i));
							}
							try (final ResultSet resultSet = preparedStatement.executeQuery()) {
								if (resultSet.next()) {
									return resultSet.getLong(1);
								} else {
									throw new Exception("Invalid data for: " + tableName);
								}
							}
						}
		        	} else {
		        		String sql = "SELECT data_length + data_free FROM information_schema.tables WHERE LOWER(table_name) = LOWER(?)";
		        		try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
							preparedStatement.setString(1, tableName);
							try (final ResultSet resultSet = preparedStatement.executeQuery()) {
								if (resultSet.next()) {
									return resultSet.getLong(1);
								} else {
									throw new Exception("Invalid data for: " + tableName);
								}
							}
						}
		        	}
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}

	public static String createTargetExpressionRestriction(final boolean isOracle) {
		return createTargetExpressionRestriction(isOracle, null);
	}

	public static String createTargetExpressionRestriction(final boolean isOracle, final String mailingTableName) {
		final StringBuilder targetExpressionRestriction = new StringBuilder();

		final String appendMailingTableName = mailingTableName == null ? "" : mailingTableName + ".";

		if (isOracle) {
			// Works three times faster than an MySQL-incompatible REGEXP_INSTR() version
			targetExpressionRestriction.append("INSTR(':' || ");

			// Replace all non-numeric characters with a colon character
			targetExpressionRestriction.append("TRANSLATE(")
					.append(appendMailingTableName)
					.append("target_expression, '")
					.append(TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS)
					.append("', '")
					.append(StringUtils.repeat(":", TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length()))
					.append("')");

			targetExpressionRestriction.append(" || ':', ':' || ? || ':') <> 0");
		} else {
			// Works three times faster than an MySQL-incompatible REGEXP_INSTR() version
			// MySQL: "||" is NOT concatenation, but an boolean expression. Concatenation is done by "concat()"
			targetExpressionRestriction.append("INSTR(CONCAT(':', ");

			// Replace all non-numeric characters with a colon character
			targetExpressionRestriction.append(StringUtils.repeat("REPLACE(", TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length()))
					.append(appendMailingTableName).append("target_expression");
			for (int i = 0; i < TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length(); i++) {
				targetExpressionRestriction.append(", '")
						.append(TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.charAt(i))
						.append("', ':')");
			}

			targetExpressionRestriction.append(", ':'), CONCAT(':', ?, ':')) <> 0");
		}
		return targetExpressionRestriction.toString();
	}

	public static boolean isAllowedValueForDataType(boolean isOracle, DbColumnType dbColumnType, Object value) {
		switch (dbColumnType.getSimpleDataType()) {
			case Characters:
				if (!(value instanceof String)) {
					return false;
				} else {
					return true;
				}
	
			case Numeric:
			case Float:
				double numericValue;
				if (value instanceof String) {
					try {
						numericValue = Double.parseDouble((String) value);
					} catch (NumberFormatException e) {
						return false;
					}
				} else {
					numericValue = ((Number) value).doubleValue();
				}
					
				if (isOracle) {
					if ("integer".equalsIgnoreCase(dbColumnType.getTypeName())) {
						if (numericValue > 2147483647) {
							return false;
						} else if (numericValue < -2147483648) {
							return false;
						} else {
							return true;
						}
					} else {
						String stringRepresentation = Double.toString(numericValue);
						if (stringRepresentation.endsWith(".0")) {
							stringRepresentation = stringRepresentation.substring(0, stringRepresentation.length() - 2);
						}
						if (dbColumnType.getNumericPrecision() < stringRepresentation.length()) {
							return false;
						} else {
							return true;
						}
					}
				} else {
					if ("smallint".equalsIgnoreCase(dbColumnType.getTypeName())) {
						if (numericValue > 32767) {
							return false;
						} else if (numericValue < -32768) {
							return false;
						} else {
							return true;
						}
					} else if ("int".equalsIgnoreCase(dbColumnType.getTypeName()) || "integer".equalsIgnoreCase(dbColumnType.getTypeName())) {
						if (numericValue > 2147483647) {
							return false;
						} else if (numericValue < -2147483648) {
							return false;
						} else {
							return true;
						}
					} else if ("bigint".equalsIgnoreCase(dbColumnType.getTypeName())) {
						if (numericValue > 9223372036854775807l) {
							return false;
						} else if (numericValue < -9223372036854775808l) {
							return false;
						} else {
							return true;
						}
					} else {
						return true;
					}
				}
	
			case Date:
			case DateTime:
				if (value instanceof String) {
					try {
						DateUtilities.detectSimpleFormat((String) value);
						return true;
					} catch (Exception e) {
						return false;
					}
				} else {
					return true;
				}
	
			case Blob:
			default:
				return false;
		}
	}
}
