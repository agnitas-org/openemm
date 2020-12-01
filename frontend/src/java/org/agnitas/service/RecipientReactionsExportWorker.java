/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.apache.log4j.Logger;

/**
 * Event Code 1=Linkklick, 2=Opening, 3=Unsubscribtion, 4=Mail-Delivery, 5=Softbounce, 6=Hardbounce, 7=Blacklist 8=Revenue
 */
public class RecipientReactionsExportWorker extends GenericExportWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(RecipientReactionsExportWorker.class);
	
	public static final int MAILING_RECIPIENTS_ALL = 0;
	public static final int MAILING_RECIPIENTS_OPENED = 1;
	public static final int MAILING_RECIPIENTS_CLICKED = 2;
	public static final int MAILING_RECIPIENTS_BOUNCED = 3;
	public static final int MAILING_RECIPIENTS_UNSUBSCRIBED = 4;
	
	private Date exportDataStartDate;
	private Date exportDataEndDate;

	/**
	 * Descriptive username for manually executed exports (non-AutoExport)
	 */
	private String username = null;
		
	private AutoExport autoExport = null;

	private RemoteFile remoteFile = null;
	
	private List<String> additionalCustomerFields = null;

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

	public RecipientReactionsExportWorker(AutoExport autoExport, Date exportDataStartDate, Date exportDataEndDate, List<String> additionalCustomerFields) {
		super();
		
		this.autoExport = autoExport;
		this.exportDataStartDate = exportDataStartDate;
		this.exportDataEndDate = exportDataEndDate;
		this.additionalCustomerFields = additionalCustomerFields;
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
			if (additionalCustomerFields != null) {
				for (String additionalField : additionalCustomerFields) {
					csvFileHeaders.add(additionalField.toUpperCase());
					additionalCustomerFieldsSqlPart += ", cust." + additionalField.toLowerCase();
				}
			}
			csvFileHeaders.add("EVENT");
			csvFileHeaders.add("TIMESTAMP");
			csvFileHeaders.add("LINK");
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
			sqlSelectStatement.append("SELECT succ.mailing_id, mail.shortname, succ.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 4, succ.timestamp, NULL, NULL"
				+ " FROM success_" + autoExport.getCompanyId() + "_tbl succ, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE succ.customer_id = cust.customer_id"
				+ " AND succ.mailing_id = mail.mailing_id"
				+ " AND succ.timestamp >= ?"
				+ " AND succ.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Mail-Hard-Bounces
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 6, bounce.timestamp, NULL, NULL"
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
			sqlSelectStatement.append("SELECT bounce.mailing_id, mail.shortname, bounce.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 5, bounce.timestamp, NULL, NULL"
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
			sqlSelectStatement.append("SELECT bind.exit_mailing_id, mail.shortname, bind.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 3, bind.timestamp, NULL, NULL"
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
			sqlSelectStatement.append("SELECT opl.mailing_id, mail.shortname, opl.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 2, opl.creation, NULL, NULL"
				+ " FROM onepixellog_device_" + autoExport.getCompanyId() + "_tbl opl, customer_" + autoExport.getCompanyId() + "_tbl cust, mailing_tbl mail"
				+ " WHERE opl.customer_id = cust.customer_id"
				+ " AND opl.mailing_id = mail.mailing_id"
				+ " AND opl.creation >= ?"
				+ " AND opl.creation < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Clicks
			sqlSelectStatement.append("SELECT rlog.mailing_id, mail.shortname, rlog.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 1, rlog.timestamp, url.full_url, NULL"
				+ " FROM rdirlog_" + autoExport.getCompanyId() + "_tbl rlog, customer_" + autoExport.getCompanyId() + "_tbl cust, rdir_url_tbl url, mailing_tbl mail"
				+ " WHERE rlog.customer_id = cust.customer_id"
				+ " AND rlog.mailing_id = mail.mailing_id"
				+ " AND rlog.url_id = url.url_id"
				+ " AND rlog.timestamp >= ?"
				+ " AND rlog.timestamp < ?");
			sqlParameter.add(exportDataStartDate);
			sqlParameter.add(exportDataEndDate);
			
			sqlSelectStatement.append("\nUNION ALL\n");
			
			// Revenues
			sqlSelectStatement.append("SELECT valnum.mailing_id, mail.shortname, valnum.customer_id, cust.email" + additionalCustomerFieldsSqlPart + ", 8, valnum.timestamp, NULL, valnum.num_parameter"
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
