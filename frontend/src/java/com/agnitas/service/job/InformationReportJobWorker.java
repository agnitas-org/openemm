/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.agnitas.beans.impl.CompanyStatus;
import org.agnitas.service.JobWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DataEncryptor;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.FtpHelper;
import org.agnitas.util.RemoteFileHelper;
import org.agnitas.util.SFtpHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.emm.core.report.bean.RecipientEntity;
import com.agnitas.emm.core.report.converter.impl.RecipientDeviceHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientEntityDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientMailingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientRetargetingHistoryDtoConverter;
import com.agnitas.emm.core.report.converter.impl.RecipientStatusHistoryDtoConverter;
import com.agnitas.emm.core.report.dto.RecipientDeviceHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientEntityDto;
import com.agnitas.emm.core.report.dto.RecipientMailingHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientRetargetingHistoryDto;
import com.agnitas.emm.core.report.dto.RecipientStatusHistoryDto;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.emm.core.report.printer.RecipientEntityDtoPrinter;
import com.agnitas.emm.core.report.services.RecipientReportService;


/**
 * This JobWorker creates information report txt files and transfers them via sftp/ftp
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    (SELECT MAX(id) + 1, 'InformationReport', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.InformationReportJobWorker', 0 FROM job_queue_tbl);
 */
public class InformationReportJobWorker extends JobWorker {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(InformationReportJobWorker.class);
	
	private RecipientReportService recipientReportService;
	private RecipientEntityDtoConverter recipientEntityDtoConverter;
	private RecipientEntityDtoPrinter recipientEntityDtoPrinter;
	private RecipientStatusHistoryDtoConverter recipientStatusHistoryConverter;
	private TableGenerator txtTableGenerator;
	private RecipientMailingHistoryDtoConverter recipientMailingHistoryDtoConverter;
	private RecipientRetargetingHistoryDtoConverter recipientRetargetingHistoryDtoConverter;
	private RecipientDeviceHistoryDtoConverter recipientDeviceHistoryDtoConverter;

	@Override
	public String runJob() throws Exception {
		DataSource dataSource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		this.recipientReportService = (RecipientReportService) getApplicationContextForJobWorker().getBeansOfType(RecipientReportService.class).values().toArray()[0];
		this.txtTableGenerator = getApplicationContextForJobWorker().getBean("txtTableGenerator", TableGenerator.class);

		this.recipientEntityDtoConverter = new RecipientEntityDtoConverter();
		this.recipientEntityDtoPrinter = new RecipientEntityDtoPrinter();
		this.recipientStatusHistoryConverter = new RecipientStatusHistoryDtoConverter();
		this.recipientMailingHistoryDtoConverter = new RecipientMailingHistoryDtoConverter();
		this.recipientRetargetingHistoryDtoConverter = new RecipientRetargetingHistoryDtoConverter();
		this.recipientDeviceHistoryDtoConverter = new RecipientDeviceHistoryDtoConverter();
		
		String includedCompanyIdsString = job.getParameters().get("includedCompanyIds");
		List<Integer> includedCompanyIds = null;
		if (StringUtils.isNotBlank(includedCompanyIdsString)) {
			includedCompanyIds = AgnUtils.splitAndTrimList(includedCompanyIdsString).stream().map(Integer::parseInt).collect(Collectors.toList());
		}

		String excludedCompanyIdsString = job.getParameters().get("excludedCompanyIds");
		List<Integer> excludedCompanyIds = null;
		if (StringUtils.isNotBlank(excludedCompanyIdsString)) {
			excludedCompanyIds = AgnUtils.splitAndTrimList(excludedCompanyIdsString).stream().map(Integer::parseInt).collect(Collectors.toList());
		}

		String languageString = job.getParameters().get("language");
		if (StringUtils.isBlank(languageString)) {
			languageString = "DE";
		}
		Locale locale = Locale.forLanguageTag(languageString);
		
		String filenamePattern = job.getParameters().get("filenamepattern");
		if (StringUtils.isBlank(filenamePattern)) {
			filenamePattern = "informationreport-[company_id]-[email].txt";
		}
		
		String specialWhereClause = job.getParameters().get("specialWhereClause");
		
		String sftpServerCredentials = job.getParameters().get("sftpServerCredentials");
		String ftpServerCredentials = job.getParameters().get("ftpServerCredentials");
		
		int counter = 0;
		int errorCounter = 0;
		
		try (RemoteFileHelper remoteFileHelper = openRemoteFileHelper(sftpServerCredentials, ftpServerCredentials)){
			String sql = "SELECT company_id FROM company_tbl WHERE status = '" + CompanyStatus.ACTIVE.getDbValue() + "'"
				+ (includedCompanyIds != null && !includedCompanyIds.isEmpty() ? " AND company_id IN (" + StringUtils.join(includedCompanyIds, ", ") + ")" : "")
				+ (excludedCompanyIds != null && !excludedCompanyIds.isEmpty() ? " AND company_id NOT IN (" + StringUtils.join(excludedCompanyIds, ", ") + ")" : "");
			List<Integer> companyIdList = jdbcTemplate.queryForList(sql, Integer.class);
			
			for (int companyID : companyIdList) {
				List<String> columnNames = DbUtilities.getColumnNames(dataSource, "customer_" + companyID + "_tbl");
				List<Integer> customerIdList = jdbcTemplate.queryForList("SELECT customer_id FROM customer_" + companyID + "_tbl" + (StringUtils.isBlank(specialWhereClause) ? "" : " WHERE " + specialWhereClause) + " ORDER BY customer_id", Integer.class);
				for (int customerID : customerIdList) {
					try {
						String informationReportText = getRecipientReport(companyID, customerID, locale);
						String fileName = filenamePattern.replace("[company_id]", Integer.toString(companyID));
						for (String columnName : columnNames) {
							if (fileName.contains("[" + columnName.toLowerCase() + "]")) {
								Object columnValue = jdbcTemplate.queryForObject("SELECT " + columnName + " FROM customer_" + companyID + "_tbl WHERE customer_id = ?", Object.class, customerID);
								fileName = fileName.replace("[" + columnName.toLowerCase() + "]", columnValue == null ? "" : columnValue.toString());
							}
						}
						
						if (remoteFileHelper != null) {
							remoteFileHelper.put(new ByteArrayInputStream(informationReportText.getBytes("UTF-8")), fileName, true);
						} else {
							File informationReportFile = new File(AgnUtils.getTempDir() + File.separator + fileName);
							if (informationReportFile.exists()) {
								logger.error("Skipped multiple recipient information export in same file: " + informationReportFile.getAbsolutePath());
								break;
							}
							FileUtils.writeStringToFile(informationReportFile, informationReportText, "UTF-8");
						}
					} catch (Exception e) {
						logger.error("Error while running job worker", e);

						errorCounter++;
					}
					
					counter++;
				}
			}
		}
		
		return "Created recipient information reports: " + counter + " errors: " + errorCounter;
	}

	private RemoteFileHelper openRemoteFileHelper(String sftpServerCredentials, String ftpServerCredentials) throws Exception {
		if (StringUtils.isNotBlank(sftpServerCredentials)) {
			DataEncryptor dataEncryptor = getApplicationContextForJobWorker().getBean("DataEncryptor", DataEncryptor.class);
			String sftpCredentialsString = dataEncryptor.decrypt(sftpServerCredentials);
			
			SFtpHelper sftpHelper = new SFtpHelper(sftpCredentialsString);
			String privateSshKeyData = job.getParameters().get("privateSshKeyData");
			if (StringUtils.isNotBlank(privateSshKeyData)) {
				sftpHelper.setPrivateSshKeyData(privateSshKeyData);
			}
			
			sftpHelper.setAllowUnknownHostKeys(true);
			sftpHelper.connect();
			return sftpHelper;
		} else if (StringUtils.isNotBlank(ftpServerCredentials)) {
			DataEncryptor dataEncryptor = getApplicationContextForJobWorker().getBean("DataEncryptor", DataEncryptor.class);
			String ftpCredentialsString = dataEncryptor.decrypt(ftpServerCredentials);
			
			FtpHelper ftpHelper = new FtpHelper(ftpCredentialsString);
			ftpHelper.connect();
			return ftpHelper;
		} else {
			throw new Exception("Missing definition of SFTP or FTP destination host");
		}
	}

	public String getRecipientReport(int companyId, int recipientId, Locale locale) {
		// Recipient Info
		RecipientEntity recipientEntity = recipientReportService.getRecipientInfo(recipientId, companyId);
		RecipientEntityDto recipientEntityDto = recipientEntityDtoConverter.convert(recipientEntity, locale);
		String recipientInfo = recipientEntityDtoPrinter.print(recipientEntityDto, locale);

		// Status History
		List<ComRecipientHistory> statusHistory = recipientReportService.getStatusHistory(recipientId, companyId);
		List<RecipientStatusHistoryDto> statusHistoryDto = recipientStatusHistoryConverter.convert(statusHistory, locale);
		String statusHistoryTable = txtTableGenerator.generate(statusHistoryDto, locale);

		// Mailing History
		List<ComRecipientMailing> mailingHistory = recipientReportService.getMailingHistory(recipientId, companyId);
		List<RecipientMailingHistoryDto> mailingHistoryDto = recipientMailingHistoryDtoConverter.convert(mailingHistory, locale);
		String mailingHistoryTable = txtTableGenerator.generate(mailingHistoryDto, locale);

		// Deep Tracking (Retargeting) History
		List<WebtrackingHistoryEntry> trackingHistory = recipientReportService.getRetargetingHistory(recipientId, companyId);
		List<RecipientRetargetingHistoryDto> trackingHistoryDto = recipientRetargetingHistoryDtoConverter.convert(trackingHistory);
		String trackingHistoryTable = txtTableGenerator.generate(trackingHistoryDto, locale);

		// Device history
		List<ComRecipientReaction> deviceHistory = recipientReportService.getDeviceHistory(recipientId, companyId);
		List<RecipientDeviceHistoryDto> deviceHistoryDto = recipientDeviceHistoryDtoConverter.convert(deviceHistory, locale);
		String deviceHistoryTable = txtTableGenerator.generate(deviceHistoryDto, locale);

		// Union of report parts
		StringBuilder report = new StringBuilder();
		report.append(recipientInfo);
		report.append(statusHistoryTable);
		report.append(mailingHistoryTable);
		report.append(trackingHistoryTable);
		report.append(deviceHistoryTable);

		return report.toString();
	}
}
