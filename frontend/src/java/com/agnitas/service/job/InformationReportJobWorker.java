/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;

import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.emm.core.report.generator.TableGenerator;
import com.agnitas.emm.core.report.services.RecipientReportService;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DataEncryptor;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.FtpHelper;
import com.agnitas.util.RemoteFileHelper;
import com.agnitas.util.SFtpHelper;
import com.agnitas.util.quartz.JobWorker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * This JobWorker creates information report txt files and transfers them via sftp/ftp
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    (SELECT MAX(id) + 1, 'InformationReport', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.InformationReportJobWorker', 0 FROM job_queue_tbl);
 */
@JobWorker("InformationReport")
public class InformationReportJobWorker extends JobWorkerBase {

	private static final Logger logger = LogManager.getLogger(InformationReportJobWorker.class);

    protected RecipientReportService recipientReportService;
    protected TableGenerator txtTableGenerator;

	@Override
	public String runJob() throws Exception {
		DataSource dataSource = daoLookupFactory.getBeanDataSource();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		this.recipientReportService = (RecipientReportService) getApplicationContextForJobWorker().getBeansOfType(RecipientReportService.class).values().toArray()[0];
		this.txtTableGenerator = getApplicationContextForJobWorker().getBean("txtTableGenerator", TableGenerator.class);

		List<Integer> includedCompanyIds = getIncludedCompanyIdsListParameter();

		List<Integer> excludedCompanyIds = getExcludedCompanyIdsListParameter();

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
						String informationReportText = recipientReportService.getRecipientTxtReport(companyID, customerID, locale);
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
		}

		throw new Exception("Missing definition of SFTP or FTP destination host");
	}
}
