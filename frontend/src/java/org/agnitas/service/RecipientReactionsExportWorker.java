/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvReader;
import org.agnitas.util.CsvWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.util.LinkUtils;

/**
 * Event Code 1=Linkklick, 2=Opening, 3=Unsubscribtion, 4=Mail-Delivery, 5=Softbounce, 6=Hardbounce, 7=Blacklist, 8=Revenue
 */
public class RecipientReactionsExportWorker extends GenericExportWorker {
	private static final Logger logger = LogManager.getLogger(RecipientReactionsExportWorker.class);

	protected ComRecipientDao recipientDao;
	protected TrackableLinkDao trackableLinkDao;
	
	protected Date exportDataStartDate;
	protected Date exportDataEndDate;

	/**
	 * Descriptive username for manually executed exports (non-AutoExport)
	 */
	protected String username = null;
		
	protected AutoExport autoExport = null;

	protected RemoteFile remoteFile = null;
	
	protected List<String> additionalCustomerFields = null;
	
	protected boolean exportLinkDescription = false;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public AutoExport getAutoExport() {
		return autoExport;
	}

	public void setAutoExport(AutoExport autoExport) {
		this.autoExport = autoExport;
	}

	public RemoteFile getRemoteFile() {
		return remoteFile;
	}

	public void setRemoteFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}

	public Date getExportDataStartDate() {
		return exportDataStartDate;
	}

	public Date getExportDataEndDate() {
		return exportDataEndDate;
	}

	public RecipientReactionsExportWorker(ComRecipientDao recipientDao, TrackableLinkDao trackableLinkDao, boolean exportLinkDescription, AutoExport autoExport, Date exportDataStartDate, Date exportDataEndDate, List<String> additionalCustomerFields) {
		this.recipientDao = recipientDao;
		this.trackableLinkDao = trackableLinkDao;
		this.exportLinkDescription = exportLinkDescription;
		this.autoExport = autoExport;
		this.exportDataStartDate = exportDataStartDate;
		this.exportDataEndDate = exportDataEndDate;
		this.additionalCustomerFields = additionalCustomerFields;
	}
	
	public GenericExportWorker genericExportCall() throws Exception {
		return super.call();
	}

	@Override
	public GenericExportWorker call() throws Exception {
		try {
			String additionalCustomerFieldsSqlPart = "";
			csvFileHeaders = new ArrayList<>();
			csvFileHeaders.add("MAILING_ID");
			csvFileHeaders.add("MAILING_NAME");
			csvFileHeaders.add("CUSTOMER_ID");
			csvFileHeaders.add("EMAIL");
			if (additionalCustomerFields != null && additionalCustomerFields.size() > 0) {
				for (String additionalField : additionalCustomerFields) {
					if (StringUtils.isNotBlank(additionalField)) {
						csvFileHeaders.add(additionalField.toUpperCase());
						additionalCustomerFieldsSqlPart += ", cust." + additionalField.toLowerCase();
					}
				}
			}
			csvFileHeaders.add("EVENT");
			csvFileHeaders.add("TIMESTAMP");
			csvFileHeaders.add("LINK");
			if (exportLinkDescription) {
				csvFileHeaders.add("LINK_DESCRIPTION");
			}
			csvFileHeaders.add("REVENUE");
			
			StringBuilder sqlSelectStatement = new StringBuilder();
			List<Object> sqlParameter = new ArrayList<>();
			
	//		// Mail-Deliveries
	//		sqlStatement.append("SELECT track.mailing_id, track.customer_id, cust.email, 4, track.timestamp, NULL, NULL"
	//			+ " FROM mailtrack_" + companyId + "_tbl track, customer_" + companyId + "_tbl cust"
	//			+ " WHERE track.customer_id = cust.customer_id"
	//			+ " AND track.timestamp >= ?"
	//			+ " AND track.timestamp < ?");
	//		sqlParameter.add(exportDataStartDate);
	//		sqlParameter.add(exportDataEndDate);
	//
	//		sqlStatement.append("\nUNION ALL\n");
			
			// Mail-Delivery-Successes
			sqlSelectStatement.append("SELECT succ.mailing_id, mail.shortname, succ.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 4, succ.timestamp, NULL" + (exportLinkDescription ? ", NULL" : "") + ", NULL"
				+ " FROM success_" + autoExport.getCompanyId() + "_tbl succ, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE succ.customer_id = cust.customer_id"
				+ " AND succ.mailing_id = mail.mailing_id"
				+ " AND succ.timestamp >= ?"
				+ " AND succ.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Mail-Hard-Bounces
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 6, bounce.timestamp, NULL" + (exportLinkDescription ? ", NULL" : "") + ", NULL"
				+ " FROM bounce_tbl bounce, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE bounce.customer_id = cust.customer_id"
				+ " AND bounce.mailing_id = mail.mailing_id"
				+ " AND bounce.company_id = ?"
				+ " AND bounce.timestamp >= ?"
				+ " AND bounce.timestamp < ?"
				+ " AND bounce.detail >= 500");
			sqlParameter.add(autoExport.getCompanyId());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Mail-Soft-Bounces
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 5, bounce.timestamp, NULL" + (exportLinkDescription ? ", NULL" : "") + ", NULL"
				+ " FROM bounce_tbl bounce, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE bounce.customer_id = cust.customer_id"
				+ " AND bounce.mailing_id = mail.mailing_id"
				+ " AND bounce.company_id = ?"
				+ " AND bounce.timestamp >= ?"
				+ " AND bounce.timestamp < ?"
				+ " AND bounce.detail < 500");
			sqlParameter.add(autoExport.getCompanyId());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Unsubscriptions
			sqlSelectStatement.append("SELECT bind.exit_mailing_id, mail.shortname, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 3, bind.timestamp, NULL" + (exportLinkDescription ?", NULL" : "") + ", NULL"
				+ " FROM customer_" + autoExport.getCompanyId() + "_binding_tbl bind, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE bind.customer_id = cust.customer_id"
				+ " AND bind.exit_mailing_id = mail.mailing_id"
				+ " AND bind.user_status = ?"
				+ " AND bind.timestamp >= ?"
				+ " AND bind.timestamp < ?");
			sqlParameter.add(UserStatus.UserOut.getStatusCode());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Openings
			sqlSelectStatement.append("SELECT opl.mailing_id, mail.shortname, opl.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 2, opl.creation, NULL" + (exportLinkDescription ? ", NULL" : "") + ", NULL"
				+ " FROM onepixellog_device_" + autoExport.getCompanyId() + "_tbl opl, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE opl.customer_id = cust.customer_id"
				+ " AND opl.mailing_id = mail.mailing_id"
				+ " AND opl.creation >= ?"
				+ " AND opl.creation < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Clicks
			sqlSelectStatement.append("SELECT rlog.mailing_id, mail.shortname, rlog.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 1, rlog.timestamp, url.url_id" + (exportLinkDescription ? ", url.shortname" : "") + ", NULL"
				+ " FROM rdirlog_" + autoExport.getCompanyId() + "_tbl rlog, customer_" + autoExport.getCompanyId() + "_tbl cust, rdir_url_tbl url, mailing_tbl mail"
				+ " WHERE rlog.customer_id = cust.customer_id"
				+ " AND rlog.mailing_id = mail.mailing_id"
				+ " AND rlog.url_id = url.url_id"
				+ " AND rlog.timestamp >= ?"
				+ " AND rlog.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Blacklisted
			sqlSelectStatement.append("SELECT NULL, ban.reason, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 7, MIN(bind.timestamp), NULL" + (exportLinkDescription ? ", NULL" : "") + ", NULL"
				+ " FROM customer_" + autoExport.getCompanyId() + "_binding_tbl bind, customer_" + autoExport.getCompanyId() + "_tbl cust"
				+ " LEFT OUTER join cust" + autoExport.getCompanyId() + "_ban_tbl ban ON ban.email = cust.email"
				+ " WHERE bind.customer_id = cust.customer_id"
				+ " AND bind.user_status = ?"
				+ " AND bind.timestamp >= ?"
				+ " AND bind.timestamp < ?"
				+ " GROUP BY ban.reason, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart);
			sqlParameter.add(UserStatus.Blacklisted.getStatusCode());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Revenues
			sqlSelectStatement.append("SELECT valnum.mailing_id, mail.shortname, valnum.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 8, valnum.timestamp, NULL" + (exportLinkDescription ? ", NULL" : "") + ", valnum.num_parameter"
				+ " FROM rdirlog_" + autoExport.getCompanyId() + "_val_num_tbl valnum, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE valnum.customer_id = cust.customer_id"
				+ " AND valnum.mailing_id = mail.mailing_id"
				+ " AND valnum.page_tag >= ?"
				+ " AND valnum.timestamp >= ?"
				+ " AND valnum.timestamp < ?");
			sqlParameter.add("revenue");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			selectStatement = sqlSelectStatement.toString();
			selectParameters = sqlParameter;
			
			// Execute export
			super.call();
			
			// Add link extensions
			File extendedExportFile = File.createTempFile("ReactionsExtendedExportFile", "");
			if (exportFile.length() > 0) {
				try (CsvReader reader = new CsvReader(new FileInputStream(new File(exportFile)), encoding, delimiter, stringQuote);
						CsvWriter writer = new CsvWriter(new FileOutputStream(extendedExportFile), encoding, delimiter, stringQuote)) {
					// skip headers
					writer.writeValues(reader.readNextCsvLine());
					List<String> nextLine;
					while ((nextLine = reader.readNextCsvLine()) != null) {
						if (StringUtils.isNotEmpty(nextLine.get(0))) {
							int mailingID = Integer.parseInt(nextLine.get(0));
							int customerID = Integer.parseInt(nextLine.get(2));
							String urlIdString = nextLine.get(nextLine.size() - (exportLinkDescription ? 3 : 2));
							if (StringUtils.isNotEmpty(urlIdString)) {
								int urlId = Integer.parseInt(urlIdString);
								String fullUrlWithLinkextensions = createDirectLinkWithOptionalExtensions(autoExport.getCompanyId(), mailingID, customerID, urlId);
								nextLine.set(nextLine.size() - (exportLinkDescription ? 3 : 2), fullUrlWithLinkextensions);
							}
						}
						writer.writeValues(nextLine);
					}
				}
			}
			new File(exportFile).delete();
			extendedExportFile.renameTo(new File(exportFile));
			
			if (error != null) {
				throw error;
			}
			
			if (remoteFile != null) {
				remoteFile = new RemoteFile(remoteFile.getRemoteFilePath(), new File(exportFile), remoteFile.getDownloadDurationMillis());
			}
		} catch (Exception e) {
			error = e;
		}
		
		return this;
	}

	protected String createDirectLinkWithOptionalExtensions(int companyID, int mailingID, int customerID, int linkID) throws Exception {
		ComTrackableLink trackableLink = trackableLinkDao.getTrackableLink(linkID, companyID, true);
		if (trackableLink != null) {
			String linkString = trackableLink.getFullUrl();
			Map<String, Object> cachedRecipientData = null;
			for (LinkProperty linkProperty : trackableLink.getProperties()) {
				if (LinkUtils.isExtension(linkProperty)) {
					String propertyValue = linkProperty.getPropertyValue();
					if (propertyValue != null && propertyValue.contains("##")) {
						if (cachedRecipientData == null) {
							try {
								cachedRecipientData = recipientDao.getCustomerDataFromDb(companyID, customerID);
								cachedRecipientData.put("mailing_id", mailingID);
							} catch (Throwable e) {
								logger.error("Error occured: " + e.getMessage(), e);
							}
						}
						
						// Replace customer and form placeholders
						@SuppressWarnings("unchecked")
						String replacedPropertyValue = AgnUtils.replaceHashTags(propertyValue, cachedRecipientData);
						propertyValue = replacedPropertyValue;
					}
					// Extend link properly (watch out for html-anchors etc.)
					linkString = AgnUtils.addUrlParameter(linkString, linkProperty.getPropertyName(), propertyValue == null ? "" : propertyValue, "UTF-8");
				}
			}
			return linkString;
		} else {
			return "Link not found";
		}
	}
}
