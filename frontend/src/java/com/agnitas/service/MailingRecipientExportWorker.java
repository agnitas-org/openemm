/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.messages.I18nString;

// TODO: EMMGUI-714: remove when old design will be removed
@Deprecated
public class MailingRecipientExportWorker extends GenericExportWorker {
	
	public static final int MAILING_RECIPIENTS_ALL = 0;
	public static final int MAILING_RECIPIENTS_OPENED = 1;
	public static final int MAILING_RECIPIENTS_CLICKED = 2;
	public static final int MAILING_RECIPIENTS_BOUNCED = 3;
	public static final int MAILING_RECIPIENTS_UNSUBSCRIBED = 4;
	
	private int companyID;
	private int mailingID;
	private int filterType;
	private List<String> columns;
	private String sortCriterion;
	private boolean sortAscending;

	/**
	 * Descriptive username for manually executed exports (non-AutoExport)
	 */
	private String username = null;
		
	private AutoExport autoExport = null;
	
	private RemoteFile remoteFile = null;

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

	public int getCompanyID() {
		return companyID;
	}

	public int getMailingID() {
		return mailingID;
	}

	public int getFilterType() {
		return filterType;
	}

	public String getSortCriterion() {
		return sortCriterion;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setRemoteFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}

	public MailingRecipientExportWorker(int companyID, int mailingID, int filterType, List<String> columns, String sortCriterion, boolean sortAscending, Locale locale) {
		super();
		
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.filterType = filterType;
		this.columns = columns;
		this.sortCriterion = sortCriterion;
		this.sortAscending = sortAscending;
		this.locale = locale;
	}

	@Override
	public GenericExportWorker call() {
		try {
			Map<String, String> sortableColumns = new CaseInsensitiveMap<>();
			
			if (columns != null && !columns.isEmpty()) {
				for (String column : columns) {
					sortableColumns.put(column, "cust." + column);
				}
			} else {
				columns = new ArrayList<>();
				columns.add("title");
				columns.add("firstname");
				columns.add("lastname");
				columns.add("email");
			}
			
			if (!columns.contains("title")) {
				columns.add("title");
			}
			if (!columns.contains("firstname")) {
				columns.add("firstname");
			}
			if (!columns.contains("lastname")) {
				columns.add("lastname");
			}
			if (!columns.contains("email")) {
				columns.add("email");
			}
			
			sortableColumns.put("title", "cust.title");
			sortableColumns.put("firstname", "cust.firstname");
			sortableColumns.put("lastname", "cust.lastname");
			sortableColumns.put("email", "cust.email");
			sortableColumns.put("receive_time", "receive_time");
			sortableColumns.put("open_time", "open_time");
			sortableColumns.put("openings", "openings");
			sortableColumns.put("click_time", "click_time");
			sortableColumns.put("clicks", "clicks");
			sortableColumns.put("bounce_time", "bounce_time");
			sortableColumns.put("optout_time", "optout_time");

			if ("customer_id".equalsIgnoreCase(sortCriterion)) {
				sortCriterion = "cust.customer_id";
			} else if (StringUtils.isBlank(sortCriterion) || !sortableColumns.containsKey(sortCriterion)) {
				sortCriterion = "receive_time";
			} else if (sortableColumns.containsKey(sortCriterion)) {
				sortCriterion = sortableColumns.get(sortCriterion);
			}
			
			final int mailingListId = new JdbcTemplate(dataSource).queryForObject("SELECT mailinglist_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", IntegerRowMapper.INSTANCE, companyID, mailingID);
			
			// Keep the order of requested columns
			List<String> sqlColumns = new ArrayList<>();
			for (String column : columns) {
				if ("customer_id".equalsIgnoreCase(column)) {
					// exclude customer_id
				} else if (sortableColumns.get(column).startsWith("cust.")) {
					sqlColumns.add(sortableColumns.get(column));
				}
			}
			
			String selectSql =
				"SELECT cust.customer_id,"
					+ " " + StringUtils.join(sqlColumns, ", ") + ","
					+ " MAX(succ.timestamp) AS receive_time,"
					+ " MIN(opl.first_open) AS open_time,"
					+ " COALESCE(MAX(opl.open_count), 0) AS openings,"
					+ " MIN(rlog.timestamp) AS click_time,"
					+ " COUNT(DISTINCT rlog.timestamp) AS clicks,"
					+ " MAX(bind1.timestamp) AS bounce_time,"
					+ " MAX(bind2.timestamp) AS optout_time"
				+ " FROM customer_" + companyID + "_tbl cust"
					+ " JOIN mailtrack_" + companyID + "_tbl track ON track.customer_id = cust.customer_id AND track.mailing_id = ?"
					+ " LEFT OUTER JOIN success_" + companyID + "_tbl succ ON succ.customer_id = cust.customer_id AND succ.mailing_id = ?"
					+ " LEFT OUTER JOIN onepixellog_" + companyID + "_tbl opl ON opl.customer_id = cust.customer_id AND opl.mailing_id = ?"
					+ " LEFT OUTER JOIN rdirlog_" + companyID + "_tbl rlog ON rlog.customer_id = cust.customer_id AND rlog.mailing_id = ?"
					+ " LEFT OUTER JOIN customer_" + companyID + "_binding_tbl bind1 ON bind1.customer_id = cust.customer_id AND bind1.exit_mailing_id = ? AND bind1.user_status = ? AND bind1.user_type NOT IN (?, ?, ?)"
					+ " LEFT OUTER JOIN customer_" + companyID + "_binding_tbl bind2 ON bind2.customer_id = cust.customer_id AND bind2.exit_mailing_id = ? AND bind2.user_status IN (?, ?) AND bind2.user_type IN (?, ?)"
				+ " WHERE EXISTS"
					+ " (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? AND bind.user_type NOT IN (?, ?, ?))"
				+ " GROUP BY cust.customer_id, " + StringUtils.join(sqlColumns, ", ");

			switch (filterType) {
				case MAILING_RECIPIENTS_OPENED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE open_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MAILING_RECIPIENTS_CLICKED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE click_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MAILING_RECIPIENTS_BOUNCED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE bounce_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MAILING_RECIPIENTS_UNSUBSCRIBED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE optout_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;
				default:
					// no filtering
			}
			
			String sortClause = " ORDER BY ";
			if (isOracleDB()) {
				sortClause += sortCriterion + " " + (sortAscending ? "ASC" : "DESC") + " NULLS LAST";
			} else {
				// Force MySQL sort null values the same way that Oracle does
				if ("receive_time".equals(sortCriterion)
						|| "open_time".equals(sortCriterion)
						|| "openings".equals(sortCriterion)
						|| "click_time".equals(sortCriterion)
						|| "clicks".equals(sortCriterion)
						|| "bounce_time".equals(sortCriterion)
						|| "optout_time".equals(sortCriterion)) {
					if (filterType == MAILING_RECIPIENTS_ALL) {
						selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel");
					}
				}
				sortClause += "ISNULL(" + sortCriterion + "), " + sortCriterion + " " + (sortAscending ? "ASC" : "DESC");
			}
			sortClause += ", customer_id " + (sortAscending ? "ASC" : "DESC");
			
			List<String> csvheaders = new ArrayList<>();
			for (String sqlColumn : sqlColumns) {
				if ("cust.title".equals(sqlColumn)) {
					csvheaders.add(I18nString.getLocaleString("Title", locale));
				} else if ("cust.firstname".equals(sqlColumn)) {
					csvheaders.add(I18nString.getLocaleString("Firstname", locale));
				} else if ("cust.lastname".equals(sqlColumn)) {
					csvheaders.add(I18nString.getLocaleString("Lastname", locale));
				} else if ("cust.email".equals(sqlColumn)) {
					csvheaders.add(I18nString.getLocaleString("mailing.MediaType.0", locale));
				} else {
					csvheaders.add(sqlColumn);
				}
			}
			csvheaders.add(I18nString.getLocaleString("target.rule.mailingReceived", locale));
			csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_opened", locale));
			csvheaders.add(I18nString.getLocaleString("statistic.openings", locale));
			csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_clicked", locale));
			csvheaders.add(I18nString.getLocaleString("statistic.Clicks", locale));
			csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_bounced", locale));
			csvheaders.add(I18nString.getLocaleString("mailing.recipients.mailing_unsubscribed", locale));
			
			selectStatement = selectSql + sortClause;
			
			selectParameters = new ArrayList<>();
			selectParameters.add(mailingID);
			selectParameters.add(mailingID);
			selectParameters.add(mailingID);
			selectParameters.add(mailingID);
			selectParameters.add(mailingID);
			selectParameters.add(UserStatus.Bounce.getStatusCode());
			selectParameters.add(UserType.Admin.getTypeCode());
			selectParameters.add(UserType.TestUser.getTypeCode());
			selectParameters.add(UserType.TestVIP.getTypeCode());
			selectParameters.add(mailingID);
			selectParameters.add(UserStatus.UserOut.getStatusCode());
			selectParameters.add(UserStatus.AdminOut.getStatusCode());
			selectParameters.add(UserType.World.getTypeCode());
			selectParameters.add(UserType.WorldVIP.getTypeCode());
			selectParameters.add(mailingListId);
			selectParameters.add(UserType.Admin.getTypeCode());
			selectParameters.add(UserType.TestUser.getTypeCode());
			selectParameters.add(UserType.TestVIP.getTypeCode());
			
			setCsvFileHeaders(csvheaders);
			
			excludedColumns = new ArrayList<>();
			excludedColumns.add("customer_id");
			
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
