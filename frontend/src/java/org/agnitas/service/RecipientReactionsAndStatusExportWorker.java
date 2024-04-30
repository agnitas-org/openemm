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

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.util.CsvReader;
import org.agnitas.util.CsvWriter;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.TrackableLinkDao;

/**
 * Event Codes<br />
 * 1 = Subscribtion<br />
 * 2 = Unsubscription by Admin<br />
 * 3 = Unsubscription by User<br />
 * 4 = Blocklist<br />
 * 5 = Mail-Delivery<br />
 * 6 = Softbounce<br />
 * 7 = Hardbounce<br />
 * 8 = Opening<br />
 * 9 = Linkklick<br />
 * 10 = Revenue<br />
 */
public class RecipientReactionsAndStatusExportWorker extends RecipientReactionsExportWorker {
	public static int EVENTCODE_SUBSCRIPTION = 1;
	public static int EVENTCODE_UNSUBSCRIPTION_BY_ADMIN = 2;
	public static int EVENTCODE_UNSUBSCRIPTION_BY_USER = 3;
	public static int EVENTCODE_BLOCKLISTED = 4;
	public static int EVENTCODE_MAILDELIVERY = 5;
	public static int EVENTCODE_SOFTBOUNCE = 6;
	public static int EVENTCODE_HARDBOUNCE = 7;
	public static int EVENTCODE_OPENED_MAILING = 8;
	public static int EVENTCODE_CLICKED_MAILING = 9;
	public static int EVENTCODE_REVENUE = 10;

	public RecipientReactionsAndStatusExportWorker(ComRecipientDao recipientDao, TrackableLinkDao trackableLinkDao, AutoExport autoExport, Date exportDataStartDate, Date exportDataEndDate, List<String> additionalCustomerFields) {
		super(recipientDao, trackableLinkDao, false, autoExport, exportDataStartDate, exportDataEndDate, additionalCustomerFields);
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
			csvFileHeaders.add("REVENUE");
			
			StringBuilder sqlSelectStatement = new StringBuilder("SELECT * FROM (");
			List<Object> sqlParameter = new ArrayList<>();
			
	//		// Mail-Deliveries
	//		sqlStatement.append("SELECT track.mailing_id, track.customer_id, cust.email, " + EVENTCODE_MAILDELIVERY + ", track.timestamp, NULL, NULL"
	//			+ " FROM mailtrack_" + companyId + "_tbl track, customer_" + companyId + "_tbl cust"
	//			+ " WHERE track.customer_id = cust.customer_id"
	//			+ " AND track.timestamp >= ?"
	//			+ " AND track.timestamp < ?");
	//		sqlParameter.add(exportDataStartDate);
	//		sqlParameter.add(exportDataEndDate);
	//
	//		sqlStatement.append("\nUNION ALL\n");
			
			// Mail-Delivery-Successes
			sqlSelectStatement.append("SELECT succ.mailing_id, mail.shortname, succ.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_MAILDELIVERY + " AS Event, succ.timestamp AS Timestamp, NULL AS Link, NULL AS Revenue"
				+ " FROM success_" + autoExport.getCompanyId() + "_tbl succ, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE succ.customer_id = cust.customer_id"
				+ " AND succ.mailing_id = mail.mailing_id"
				+ " AND succ.timestamp >= ?"
				+ " AND succ.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Mail-Hard-Bounces
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_HARDBOUNCE + ", bounce.timestamp, NULL, NULL"
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
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_SOFTBOUNCE + ", bounce.timestamp, NULL, NULL"
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
			
			// Subscriptions
			sqlSelectStatement.append("SELECT NULL, NULL, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_SUBSCRIPTION + ", bind.timestamp, NULL, NULL"
				+ " FROM customer_" + autoExport.getCompanyId() + "_binding_tbl bind, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE bind.customer_id = cust.customer_id"
				+ " AND bind.exit_mailing_id = mail.mailing_id"
				+ " AND bind.user_status = ?"
				+ " AND bind.timestamp >= ?"
				+ " AND bind.timestamp < ?");
			sqlParameter.add(UserStatus.Active.getStatusCode());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Unsubscriptions by Admin
			sqlSelectStatement.append("SELECT bind.exit_mailing_id, mail.shortname, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_UNSUBSCRIPTION_BY_ADMIN + ", bind.timestamp, NULL, NULL"
				+ " FROM customer_" + autoExport.getCompanyId() + "_binding_tbl bind, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE bind.customer_id = cust.customer_id"
				+ " AND bind.exit_mailing_id = mail.mailing_id"
				+ " AND bind.user_status = ?"
				+ " AND bind.timestamp >= ?"
				+ " AND bind.timestamp < ?");
			sqlParameter.add(UserStatus.AdminOut.getStatusCode());
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Unsubscriptions by User
			sqlSelectStatement.append("SELECT bind.exit_mailing_id, mail.shortname, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_UNSUBSCRIPTION_BY_USER + ", bind.timestamp, NULL, NULL"
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
			sqlSelectStatement.append("SELECT opl.mailing_id, mail.shortname, opl.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_OPENED_MAILING + ", opl.creation, NULL, NULL"
				+ " FROM onepixellog_device_" + autoExport.getCompanyId() + "_tbl opl, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE opl.customer_id = cust.customer_id"
				+ " AND opl.mailing_id = mail.mailing_id"
				+ " AND opl.creation >= ?"
				+ " AND opl.creation < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Clicks
			sqlSelectStatement.append("SELECT rlog.mailing_id, mail.shortname, rlog.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_CLICKED_MAILING + ", rlog.timestamp, url.url_id, NULL"
				+ " FROM rdirlog_" + autoExport.getCompanyId() + "_tbl rlog, customer_" + autoExport.getCompanyId() + "_tbl cust, rdir_url_tbl url, mailing_tbl mail"
				+ " WHERE rlog.customer_id = cust.customer_id"
				+ " AND rlog.mailing_id = mail.mailing_id"
				+ " AND rlog.url_id = url.url_id"
				+ " AND rlog.timestamp >= ?"
				+ " AND rlog.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Blocklisted
			sqlSelectStatement.append("SELECT NULL, ban.reason, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_BLOCKLISTED + ", MIN(bind.timestamp), NULL, NULL"
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
			sqlSelectStatement.append("SELECT valnum.mailing_id, mail.shortname, valnum.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", " + EVENTCODE_REVENUE + ", valnum.timestamp, NULL, valnum.num_parameter"
				+ " FROM rdirlog_" + autoExport.getCompanyId() + "_val_num_tbl valnum, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE valnum.customer_id = cust.customer_id"
				+ " AND valnum.mailing_id = mail.mailing_id"
				+ " AND valnum.page_tag >= ?"
				+ " AND valnum.timestamp >= ?"
				+ " AND valnum.timestamp < ?");
			sqlParameter.add("revenue");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append(") sub ORDER BY Event, Timestamp");
			
			selectStatement = sqlSelectStatement.toString();
			selectParameters = sqlParameter;
			
			// Execute export
			genericExportCall();
			
			// Add link extensions
			File extendedExportFile = File.createTempFile("ReactionsAndStatusExtendedExportFile", "");
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
							String urlIdString = nextLine.get(nextLine.size() - 2);
							if (StringUtils.isNotEmpty(urlIdString)) {
								int urlId = Integer.parseInt(urlIdString);
								String fullUrlWithLinkextensions = createDirectLinkWithOptionalExtensions(autoExport.getCompanyId(), mailingID, customerID, urlId);
								nextLine.set(nextLine.size() - 2, fullUrlWithLinkextensions);
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
}
