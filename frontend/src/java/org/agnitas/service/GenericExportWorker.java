/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.CsvWriter;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

public class GenericExportWorker implements Callable<GenericExportWorker> {
    private static final transient Logger logger = Logger.getLogger(GenericExportWorker.class);
	
	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	private static Boolean IS_ORACLE_DB = null;

	protected long maximumExportLineLimit = -1;
	
	protected String exportFile = null;
	private boolean overwriteFile = false;
	
	protected DataSource dataSource = null;
	
	protected String selectStatement = null;
	protected List<String> excludedColumns = null;
	protected List<Object> selectParameters = null;

	protected boolean done = true;
	
	private boolean zipped = false;
    private String zipPassword = null;
    private String zippedFileName = null;
	
	private String encoding = "UTF-8";
	private char delimiter = ';';
	private boolean alwaysQuote = false;
	private Character stringQuote = '"';
	private String nullValueText = "";
	
	protected DateFormat dateFormat = null;
	protected DateFormat dateTimeFormat = null;
	protected DateTimeFormatter dateFormatter = null;
	protected DateTimeFormatter dateTimeFormatter = null;
	protected NumberFormat decimalFormat;
	
	protected Date startTime;
	protected Date endTime;

	private long fileSize = 0;
	private long exportedLines = 0;
	
	protected Exception error;
    
	private ZoneId dbTimezone = ZoneId.systemDefault();
	private ZoneId exportTimezone = null;
    
    public GenericExportWorker() {
    	Locale dateAndDecimalLocale = Locale.getDefault();
    	
		// Create the default number format
		decimalFormat = DecimalFormat.getNumberInstance(dateAndDecimalLocale);
		decimalFormat.setGroupingUsed(false);

		// Create the default date format
		dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, dateAndDecimalLocale);
		((SimpleDateFormat) dateFormat).applyPattern(((SimpleDateFormat) dateFormat).toPattern().replaceFirst("y+", "yyyy"));

		// Create the default date and time format
		dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, dateAndDecimalLocale);
		((SimpleDateFormat) dateTimeFormat).applyPattern(((SimpleDateFormat) dateTimeFormat).toPattern().replaceFirst("y+", "yyyy"));
	}

	/**
	 * CSV columnnames to write to the CSV export file.
	 * If empty, we use the selected sql column names.
	 * If set to null, we don't write any csv column names.
	 **/
	private List<String> csvFileHeaders = new ArrayList<>();

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Get the number of exported lines not including the csv header line, so lines within the exported file is "exportedLines + 1"
	 * @return
	 */
	public long getExportedLines() {
		return exportedLines;
	}

	public void setExportFile(String exportFile) {
		this.exportFile = exportFile;
	}

	public String getExportFile() {
		return exportFile;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setSelectStatement(String selectStatement) {
		this.selectStatement = selectStatement;
	}

	public void setSelectParameters(List<Object> selectParameters) {
		this.selectParameters = selectParameters;
	}

	public void setZipped(boolean zipped) {
		this.zipped = zipped;
	}

	public void setZipPassword(String zipPassword) {
		this.zipPassword = zipPassword;
	}

	public void setZippedFileName(String zippedFileName) {
		this.zippedFileName = zippedFileName;
	}

	public String getZippedFileName() {
		return zippedFileName;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}

	public boolean getAlwaysQuote() {
		return alwaysQuote;
	}

	public void setStringQuote(Character stringQuote) {
		this.stringQuote = stringQuote;
	}

	public Character getStringQuote() {
		return stringQuote;
	}

	public void setCsvFileHeaders(List<String> csvFileHeaders) {
		this.csvFileHeaders = csvFileHeaders;
	}

	public boolean isDone() {
		return done;
	}

    public Exception getError() {
		return error;
	}
    
    public void setNullValueText(String nullValueText) {
    	this.nullValueText = nullValueText;
    }

	public void setOverwriteFile(boolean overwriteFile) {
		this.overwriteFile = overwriteFile;
	}

	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		this.dateFormatter = null;
	}

	public void setDateTimeFormat(DateFormat dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
		this.dateTimeFormatter = null;
	}

	public void setDateFormatter(DateTimeFormatter dateFormatter) {
		this.dateFormat = null;
		this.dateFormatter = dateFormatter;
	}

	public void setDateTimeFormatter(DateTimeFormatter dateFormatter) {
		this.dateTimeFormat = null;
		this.dateTimeFormatter = dateFormatter;
	}

	public void setDecimalFormat(NumberFormat decimalFormat) {
		this.decimalFormat = decimalFormat;
	}

	public void setMaximumExportLineLimit(long maximumExportLineLimit) {
		this.maximumExportLineLimit = maximumExportLineLimit;
	}

	public void setDateAndDecimalLocale(Locale dateAndDecimalLocale) {
		dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, dateAndDecimalLocale);
		dateTimeFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, dateAndDecimalLocale);
		dateFormatter = null;
		dateTimeFormatter = null;
		decimalFormat = DecimalFormat.getNumberInstance(dateAndDecimalLocale);
		decimalFormat.setGroupingUsed(false);
	}
	
	/**
	 * By default this is: ZoneId.systemDefault()
	 * @param dbTimezone
	 */
	public void setDbTimezone(ZoneId dbTimezone) {
		this.dbTimezone = dbTimezone;
	}

	public void setExportTimezone(ZoneId exportTimezone) {
		this.exportTimezone = exportTimezone;
	}

	@Override
	public GenericExportWorker call() throws Exception {
		try {
			if (startTime == null) {
				startTime = new Date();
	        }
			done = false;
			
			if (dataSource == null) {
				throw new Exception("DB DataSource is missing");
			}
			
			if (StringUtils.isBlank(selectStatement)) {
				throw new Exception("Select statement is missing");
			}
			
			if (zipped && StringUtils.isEmpty(zipPassword)) {
				if (!exportFile.toLowerCase().endsWith(".zip")) {
					if (!exportFile.toLowerCase().endsWith(".csv")) {
							exportFile = exportFile + ".csv";
					}
					
					exportFile = exportFile + ".zip";
				}
			} else if (!exportFile.toLowerCase().endsWith(".csv")) {
				if (exportFile.toLowerCase().endsWith(".zip")) {
					exportFile = exportFile.substring(0, exportFile.length() - 4);
				}
				exportFile = exportFile + ".csv";
			}

			if (new File(exportFile).exists()) {
				if (overwriteFile) {
					new File(exportFile).delete();
				} else {
					throw new Exception("Outputfile already exists: " + exportFile);
				}
			}
			
			@SuppressWarnings("resource")
			OutputStream outputStream = null;
			try (Connection connection = dataSource.getConnection()) {
				if (maximumExportLineLimit >= 0) {
					try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM (" + selectStatement + (isOracleDB() ? ")" : ") subselect"))) {
						if (selectParameters != null) {
				    		for (int i = 0; i < selectParameters.size(); i++) {
				    			if (selectParameters.get(i) == null) {
					    			preparedStatement.setNull(i + 1, Types.NULL);
				    			} else if (selectParameters.get(i) instanceof String) {
					    			preparedStatement.setString(i + 1, (String) selectParameters.get(i));
				    			} else if (selectParameters.get(i) instanceof Integer) {
					    			preparedStatement.setInt(i + 1, (Integer) selectParameters.get(i));
				    			} else if (selectParameters.get(i) instanceof Date) {
					    			preparedStatement.setDate(i + 1, new java.sql.Date(((Date) selectParameters.get(i)).getTime()));
				    			} else {
					    			preparedStatement.setObject(i + 1, selectParameters.get(i));
				    			}
				    		}
			    		}
			    			
			    		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			    			resultSet.next();
			    			long numberOfLinesForExport = resultSet.getLong(1);
			    			if (numberOfLinesForExport > maximumExportLineLimit) {
			    				throw new Exception("Number of export lines exceeded. Export lines: " + numberOfLinesForExport + " Maximum: " + maximumExportLineLimit);
			    			}
			    		}
					}
				}
				
				if (zipped && StringUtils.isEmpty(zipPassword)) {
					outputStream = ZipUtilities.openNewZipOutputStream(new FileOutputStream(new File(exportFile)));
					if (StringUtils.isBlank(zippedFileName) ) {
						zippedFileName = exportFile;
					}
					if (zippedFileName.contains(File.separator)) {
						zippedFileName = zippedFileName.substring(zippedFileName.lastIndexOf(File.separatorChar) + 1);
					}
					if (zippedFileName.toLowerCase().endsWith(".zip")) {
						zippedFileName = zippedFileName.substring(0, zippedFileName.length() - 4);
					}
					if (!zippedFileName.toLowerCase().endsWith(".csv")) {
						zippedFileName += ".csv";
					}
					ZipEntry entry = new ZipEntry(zippedFileName);
					entry.setTime(new Date().getTime());
					((ZipOutputStream) outputStream).putNextEntry(entry);
				} else {
					outputStream = new FileOutputStream(new File(exportFile));
				}

				try (CsvWriter csvWriter = new CsvWriter(outputStream, encoding, delimiter, stringQuote)) {
					csvWriter.setAlwaysQuote(alwaysQuote);
					exportedLines = 0;
					
			    	try (PreparedStatement preparedStatement = connection.prepareStatement(selectStatement)) {
			    		if (selectParameters != null) {
				    		for (int i = 0; i < selectParameters.size(); i++) {
				    			if (selectParameters.get(i) == null) {
					    			preparedStatement.setNull(i + 1, Types.NULL);
				    			} else if (selectParameters.get(i) instanceof String) {
					    			preparedStatement.setString(i + 1, (String) selectParameters.get(i));
				    			} else if (selectParameters.get(i) instanceof Integer) {
					    			preparedStatement.setInt(i + 1, (Integer) selectParameters.get(i));
				    			} else if (selectParameters.get(i) instanceof Date) {
					    			preparedStatement.setDate(i + 1, new java.sql.Date(((Date) selectParameters.get(i)).getTime()));
				    			} else {
					    			preparedStatement.setObject(i + 1, selectParameters.get(i));
				    			}
				    		}
			    		}
			    			
			    		try (ResultSet resultSet = preparedStatement.executeQuery()) {
							ResultSetMetaData metaData = resultSet.getMetaData();
							
							CaseInsensitiveSet excludedColumnsSet = null;
							if (excludedColumns != null) {
								excludedColumnsSet = new CaseInsensitiveSet(excludedColumns);
							}
							
							// Scan headers
							List<String> columnNames = new ArrayList<>();
							List<String> columnTypes = new ArrayList<>();
							for (int i = 1; i <= metaData.getColumnCount(); i++) {
								String columnName = metaData.getColumnName(i);
								if (excludedColumnsSet == null || !excludedColumnsSet.contains(columnName)) {
									columnNames.add(columnName);
									columnTypes.add(metaData.getColumnTypeName(i));
								}
							}
	
							// Write headers
					    	if (csvFileHeaders != null) {
					    		if (csvFileHeaders.size() > 0) {
					    			csvWriter.writeValues(csvFileHeaders);
						    	} else {
						    		csvFileHeaders = columnNames;
						    		csvWriter.writeValues(csvFileHeaders);
						    	}
					    	}
					    	
					    	// Write values
							while (resultSet.next()) {
								List<String> values = new ArrayList<>();
								
								for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
									String columnName = metaData.getColumnName(columnIndex);
									if (excludedColumnsSet == null || !excludedColumnsSet.contains(columnName)) {
										Object value = convertValue(metaData, resultSet, columnIndex);
										
										if (value == null) {
											values.add(nullValueText);
										} else if (value instanceof String) {
											values.add((String) value);
										} else if (value instanceof Number) {
											values.add(decimalFormat.format(value));
										} else {
											values.add(value.toString());
										}
									}
								}
								
								csvWriter.writeValues(values);
							}
			    		}
			    	}
					exportedLines = csvWriter.getWrittenLines() - 1;
				}
		    } catch (SQLException e) {
		        logger.error("Error during export: " + e.getMessage() + "\nSQL: " + selectStatement, e);
		        throw e;
		    } catch (Exception e) {
		        logger.error("Error during export: " + e.getMessage(), e);
		        throw e;
		    } finally {
		    	if (outputStream != null) {
		    		outputStream.close();
		    	}
		    }
			
			if (zipped && StringUtils.isNotEmpty(zipPassword) ) {
				String encryptedZippedFile = exportFile + ".zip";

				String entryFileName = zippedFileName;
				if (StringUtils.isBlank(entryFileName) ) {
					entryFileName = exportFile;
				}
				if (entryFileName.contains(File.separator)) {
					entryFileName = entryFileName.substring(entryFileName.lastIndexOf(File.separatorChar) + 1);
				}
				if (zippedFileName.toLowerCase().endsWith(".zip")) {
					zippedFileName = zippedFileName.substring(0, zippedFileName.length() - 4);
				}
				if (!entryFileName.toLowerCase().endsWith(".csv")) {
					entryFileName += ".csv";
				}
				
				ZipUtilities.compressToEncryptedZipFile(new File(encryptedZippedFile), new File(exportFile), entryFileName, zipPassword);
				new File(exportFile).delete();
				exportFile = encryptedZippedFile;
			}
		    
		    endTime = new Date();
		} catch (Exception e) {
			if (endTime == null) {
				endTime = new Date();
	        }
			
	        error = e;
	        logger.error("Error during export: " + e.getMessage(), e);
		} finally {
	        done = true;
		}

        return this;
    }
	
	public Object convertValue(ResultSetMetaData metaData, ResultSet resultSet, int columnIndex) throws Exception {
		int columnTypeCode = metaData.getColumnType(columnIndex);
		
		if (isOracleDB()) {
			if (columnTypeCode == DbUtilities.ORACLE_TIMESTAMPTZ_TYPECODE
					|| columnTypeCode == Types.TIMESTAMP) {
				Timestamp timestamp = resultSet.getTimestamp(columnIndex);
				if (resultSet.wasNull()) {
					return null;
				} else {
					if (exportTimezone != null) {
						ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), dbTimezone);
						ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(exportTimezone);
						if (dateTimeFormatter != null)  {
							return dateTimeFormatter.format(exportZonedDateTime);
						} else if (dateTimeFormat != null) {
							return dateTimeFormat.format(Date.from(exportZonedDateTime.toInstant()));
						} else {
							return exportZonedDateTime;
						}
					} else {
						if (dateTimeFormatter != null)  {
							ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), dbTimezone);
							return dateTimeFormatter.format(dbZonedDateTime);
						} else if (dateTimeFormat != null) {
							return dateTimeFormat.format(timestamp);
						} else {
							return timestamp;
						}
					}
				}
			}
		} else {
			if (columnTypeCode == Types.BINARY || columnTypeCode == Types.VARBINARY || columnTypeCode == Types.LONGVARBINARY) {
				byte[] data = (byte[]) resultSet.getObject(columnIndex);
				if (resultSet.wasNull()) {
					return null;
				} else {
					return Base64.getEncoder().encodeToString(data);
				}
			}
		}
		
		if (columnTypeCode == Types.BLOB) {
			Blob blob = resultSet.getBlob(columnIndex);
			if (resultSet.wasNull()) {
				return null;
			} else {
				try (InputStream input = blob.getBinaryStream()) {
					byte[] data = IOUtils.toByteArray(input);
					return Base64.getEncoder().encodeToString(data);
				}
			}
		} else if (columnTypeCode == Types.CLOB) {
			Clob clob = resultSet.getClob(columnIndex);
			if (resultSet.wasNull()) {
				return null;
			} else  {
				try (Reader input = clob.getCharacterStream()) {
					return IOUtils.toString(input);
				}
			}
		} else if (columnTypeCode == Types.DATE) {
			Object value = resultSet.getObject(columnIndex);
			if (resultSet.wasNull() || "0000-00-00 00:00:00".equals(value)) {
				return null;
			} else {
				if (DateUtils.truncate(value, Calendar.DAY_OF_MONTH).equals(value)) {
					// This is a fix date without time like birthday, so we do not export the time, if the user does not explicitly format it
					if (dateFormatter != null)  {
						ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), dbTimezone);
						return dateFormatter.format(dbZonedDateTime);
					} else if (dateFormat != null) {
						return dateFormat.format(((Date) value));
					} else {
						return value;
					}
				} else {
					if (exportTimezone != null) {
						ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), dbTimezone);
						ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(exportTimezone);
						if (dateTimeFormatter != null)  {
							return dateTimeFormatter.format(exportZonedDateTime);
						} else if (dateTimeFormat != null) {
							return dateTimeFormat.format(Date.from(exportZonedDateTime.toInstant()));
						} else {
							return exportZonedDateTime;
						}
					} else {
						if (dateTimeFormatter != null)  {
							ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), dbTimezone);
							return dateTimeFormatter.format(dbZonedDateTime);
						} else if (dateTimeFormat != null) {
							return dateTimeFormat.format(((Date) value));
						} else {
							return value;
						}
					}
				}
			}
		} else if (columnTypeCode == Types.TIMESTAMP) {
			Object value = resultSet.getObject(columnIndex);
			if (resultSet.wasNull() || "0000-00-00 00:00:00".equals(value)) {
				return null;
			} else {
				if (exportTimezone != null) {
					ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), dbTimezone);
					ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(exportTimezone);
					if (dateTimeFormatter != null)  {
						return dateTimeFormatter.format(exportZonedDateTime);
					} else if (dateTimeFormat != null) {
						return dateTimeFormat.format(Date.from(exportZonedDateTime.toInstant()));
					} else {
						return exportZonedDateTime;
					}
				} else {
					if (dateTimeFormatter != null)  {
						ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), dbTimezone);
						return dateTimeFormatter.format(dbZonedDateTime);
					} else if (dateTimeFormat != null) {
						return dateTimeFormat.format(((Date) value));
					} else {
						return value;
					}
				}
			}
		} else {
			Object value = resultSet.getObject(columnIndex);
			if (resultSet.wasNull()) {
				return null;
			} else {
				return value;
			}
		}
	}
	
	/**
	 * Checks the db vendor of the dataSource and caches the result for further usage
	 * @return true if db vendor of dataSource is Oracle, false if any other vendor (e.g. mysql)
	 */
	protected final boolean isOracleDB() {
		if (IS_ORACLE_DB == null) {
			IS_ORACLE_DB = DbUtilities.checkDbVendorIsOracle(dataSource);
		}
		return IS_ORACLE_DB;
	}
}
