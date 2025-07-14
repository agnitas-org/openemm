/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.service.FileCompressionType;
import com.agnitas.service.GenericExportWorker;
import com.agnitas.service.JobWorker;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DataEncryptor;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.SFtpHelper;
import com.agnitas.util.TextTableBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.emm.core.JavaMailAttachment;
import com.jcraft.jsch.ChannelSftp;

/**
 * This JobWorker executes Update, Delete, Insert and Select statements on the database and, if configured, sends an email with the results
 * Allowed JobParameters are:
 *   x stands for an consecutive index of the statements beginning with 1
 *   infoMailAddress = (optional) Emailaddress for information email
 *   infoMailSubject = (optional) Emailsubject for information email
 *   x_statement = SQL statement text
 *   x_label = Label text to be shown in information email
 *   x_continueOnError = continue with next statement if this one has an error
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    (SELECT MAX(id) + 1, 'DBJobWorkerTest', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.DBJobWorker', 0  FROM job_queue_tbl);
 * 
 *   INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value)
 *     VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'PeriodicalDbChange'), '1_statement', 'SELECT * FROM tag_tbl');
 *   INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value)
 *     VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'PeriodicalDbChange'), '1_label', 'Currently available AgnTags');
 *   INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value)
 *     VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'PeriodicalDbChange'), 'infoMailAddress', 'test@example.com');
 *   INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value)
 *     VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'PeriodicalDbChange'), 'infoMailSubject', 'PeriodicalDbChange');
 */
public class DBJobWorker extends JobWorker {
	private static final transient Logger logger = LogManager.getLogger(DBJobWorker.class);

	@Override
	public String runJob() throws Exception {
		DataSource datasource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
		
		int statementIndex = 1;
		boolean errorOccurred = false;
		String errorMessage = null;
		boolean haltExecution = false;
		
		int companyID = 0;
		String companyIdString = job.getParameters().get("companyID");
		if (StringUtils.isNotBlank(companyIdString)) {
			companyID = Integer.parseInt(companyIdString);
		}
		
		String infoMailAddress;
		String infoMailAddressConfigValueName = job.getParameters().get("infoMailAddressConfigValueName");
		if (StringUtils.isNotBlank(infoMailAddressConfigValueName)) {
			infoMailAddress = configService.getValue(ConfigValue.getConfigValueByName(infoMailAddressConfigValueName), companyID);
		} else {
			infoMailAddress = job.getParameters().get("infoMailAddress");
		}
		
		String infoMailSubject = job.getParameters().get("infoMailSubject");
		infoMailSubject = DateUtilities.replaceDatePatternsInFileName(infoMailSubject, 0, null); // May contain Java date pattern parts ("[YYYY]", "[MM]", "[DD]", "[HH]", "[MI]", "[SS]")
		String sftpServerCredentials = job.getParameters().get("sftpServerCredentials");
		
		StringBuilder infoMailContent = new StringBuilder();
		
		List<JavaMailAttachment> mailAttachments = new ArrayList<>();
		
		List<File> createdExportFiles = new ArrayList<>();
		while (!haltExecution && job.getParameters().containsKey(statementIndex + "_statement")) {
			String sqlStatement = job.getParameters().get(statementIndex + "_statement");
			String statementLabel = job.getParameters().get(statementIndex + "_label");
			statementLabel = DateUtilities.replaceDatePatternsInFileName(statementLabel, 0, null); // May contain Java date pattern parts ("[YYYY]", "[MM]", "[DD]", "[HH]", "[MI]", "[SS]")
			String format = job.getParameters().get(statementIndex + "_format");
			String filename = job.getParameters().get(statementIndex + "_filename"); // May contain Java date pattern parts ("[YYYY]", "[MM]", "[DD]", "[HH]", "[MI]", "[SS]")
			String zipFilePassword = job.getParameters().get(statementIndex + "_zipFilePassword");
			String separatorString = job.getParameters().get(statementIndex + "_separator");
			String textDelimiterString = job.getParameters().get(statementIndex + "_textdelimiter");
			boolean alwaysQuote = AgnUtils.interpretAsBoolean(job.getParameters().get(statementIndex + "_alwaysquote"));
			boolean continueOnError = AgnUtils.interpretAsBoolean(job.getParameters().get(statementIndex + "_continueOnError"));

			infoMailContent.append(statementLabel);
			infoMailContent.append("<br />\n");
			
			if (StringUtils.isBlank(sqlStatement)) {
				throw new Exception("SQL statement parameter (" + statementIndex + "_statement) is empty or missing");
			} else {
				sqlStatement = sqlStatement.replace("<company_id>", Integer.toString(companyID));
				if (previousJobStart != null) {
					sqlStatement = sqlStatement.replace("<previous_start>", new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(previousJobStart));
				}
			}
			
			if (StringUtils.isNotEmpty(sftpServerCredentials)) {
				DataEncryptor dataEncryptor = getApplicationContextForJobWorker().getBean("DataEncryptor", DataEncryptor.class);
				String sftpCredentialsString = dataEncryptor.decrypt(sftpServerCredentials);
				
				if (StringUtils.isBlank(filename)) {
					throw new Exception("SFTP file name parameter (" + statementIndex + "_filename) is empty or missing");
				}
			
				String sftpFileName = DateUtilities.replaceDatePatternsInFileName(filename, 0, null);
				
				try {
					if (DbUtilities.isSelectStatement(sqlStatement)) {
						if (StringUtils.isEmpty(format) || "CSV".equalsIgnoreCase(format) || "CSV_ZIP".equalsIgnoreCase(format)) {
							File exportDataFile = createExportDataFile(datasource, sqlStatement, format, separatorString, textDelimiterString, alwaysQuote, statementIndex, zipFilePassword);
							createdExportFiles.add(exportDataFile);
							
							try (SFtpHelper sftpHelper = new SFtpHelper(sftpCredentialsString)) {
								String privateSshKeyData = job.getParameters().get("privateSshKeyData");
								if (StringUtils.isNotBlank(privateSshKeyData)) {
									sftpHelper.setPrivateSshKeyData(privateSshKeyData);
								}
								
								sftpHelper.setAllowUnknownHostKeys(true);
								sftpHelper.connect();
								sftpHelper.put(exportDataFile.getAbsolutePath(), sftpFileName, ChannelSftp.OVERWRITE, true);
							} catch (Exception e) {
								throw new Exception("Cannot upload file to SFTP server ('" + (exportDataFile == null ? "null" : exportDataFile.getAbsolutePath()) + "'): " + e.getMessage(), e);
							}

							infoMailContent.append("Export data file '" + sftpFileName + "' transfered to SFTP server<br />\n");
						}
					}
				} catch (Exception e) {
					infoMailContent.append("Error occurred while executing statement with index " + statementIndex + ": " + e.getMessage());
					logger.error("Error occurred while executing statement with index " + statementIndex, e);
					haltExecution = !continueOnError;
					errorOccurred = true;
					errorMessage = e.getMessage();
				}
			} else {
				try {
					if (DbUtilities.isSelectStatement(sqlStatement)) {
						if (StringUtils.isEmpty(format) || "CSV".equalsIgnoreCase(format) || "CSV_ZIP".equalsIgnoreCase(format)) {
							File exportDataFile = createExportDataFile(datasource, sqlStatement, format, separatorString, textDelimiterString, alwaysQuote, statementIndex, zipFilePassword);
							createdExportFiles.add(exportDataFile);
							
							if (StringUtils.isEmpty(format) || "CSV".equalsIgnoreCase(format)) {
								String mailFilename;
								if (StringUtils.isBlank(filename)) {
									mailFilename = "data_" + new SimpleDateFormat(DateUtilities.YYYYMMDDHHMMSS).format(new Date()) + ".csv";
								} else {
									mailFilename = DateUtilities.replaceDatePatternsInFileName(filename, 0, null);
								}
								mailAttachments.add(new JavaMailAttachment(mailFilename, FileUtils.readFileToByteArray(exportDataFile), "text/comma-separated-values"));
								infoMailContent.append("For data see attached file<br />\n");
							} else if ("CSV_ZIP".equalsIgnoreCase(format)) {
								String mailFilename;
								if (StringUtils.isBlank(filename)) {
									mailFilename = "data_" + new SimpleDateFormat(DateUtilities.YYYYMMDDHHMMSS).format(new Date()) + ".csv.zip";
								} else {
									mailFilename = DateUtilities.replaceDatePatternsInFileName(filename, 0, null);
								}
								mailAttachments.add(new JavaMailAttachment(mailFilename, FileUtils.readFileToByteArray(exportDataFile), "application/zip"));
								infoMailContent.append("For data see attached file<br />\n");
							}
						} else {
							TextTableBuilder resultsTableText = DbUtilities.getResultAsTextTable(datasource, sqlStatement);
							infoMailContent.append(resultsTableText.toHtmlString(""));
							infoMailContent.append("<br />\n");
						}
					} else {
						int touchedLines = jdbcTemplate.update(sqlStatement);
						infoMailContent.append(touchedLines);
						infoMailContent.append("<br />\n");
					}
				} catch (Exception e) {
					infoMailContent.append("Error occurred while executing statement with index " + statementIndex + ": " + e.getMessage());
					logger.error("Error occurred while executing statement with index " + statementIndex, e);
					haltExecution = !continueOnError;
					errorOccurred = true;
					errorMessage = e.getMessage();
				}
				
				infoMailContent.append("<br />\n");
			}
			
			statementIndex++;
		}
		
		if (StringUtils.isNotBlank(infoMailAddress)) {
			serviceLookupFactory.getBeanJavaMailService().sendEmail(companyID, infoMailAddress, infoMailSubject, infoMailContent.toString(), infoMailContent.toString(), mailAttachments.toArray(new JavaMailAttachment[0]));
		}
		
		if (errorOccurred) {
			throw new Exception(errorMessage);
		} else {
			for (File exportDataFile : createdExportFiles) {
				if (exportDataFile != null && exportDataFile.exists()) {
					exportDataFile.delete();
				}
			}
		}
		
		return null;
	}

	private File createExportDataFile(DataSource datasource, String sqlStatement, String format, String separatorString, String textDelimiterString, boolean alwaysQuote, int statementIndex, String zipFilePassword) throws Exception {
		GenericExportWorker exportWorker = new GenericExportWorker();
		exportWorker.setDataSource(datasource);
		exportWorker.setSelectStatement(sqlStatement);
		if (StringUtils.isNotEmpty(separatorString)) {
			exportWorker.setDelimiter(separatorString.charAt(0));
		}
		if (StringUtils.isNotEmpty(textDelimiterString)) {
			exportWorker.setStringQuote(textDelimiterString.charAt(0));
		}
		exportWorker.setAlwaysQuote(alwaysQuote);
		exportWorker.setOverwriteFile(true);
		if (format != null && format.toUpperCase().startsWith("CSV_")) {
			exportWorker.setCompressionType(FileCompressionType.getFromString(format.substring(4)));
			if (StringUtils.isNotBlank(zipFilePassword)) {
				exportWorker.setZipPassword(zipFilePassword);
			}
			exportWorker.setExportFile(File.createTempFile("Export_" + job.getDescription() + "_" + statementIndex + "_", ".csv.zip", AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "Export")).getAbsolutePath());
		} else {
			exportWorker.setExportFile(File.createTempFile("Export_" + job.getDescription() + "_" + statementIndex + "_", ".csv", AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "Export")).getAbsolutePath());
		}
		
		exportWorker.call();
		if (exportWorker.getError() != null) {
			throw exportWorker.getError();
		}
		return new File(exportWorker.getExportFile());
	}
}
