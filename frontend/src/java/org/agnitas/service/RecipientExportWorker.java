/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;

public class RecipientExportWorker extends GenericExportWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(RecipientExportWorker.class);

	public static final String EXPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientExport";

	public static final int NO_MAILINGLIST = -1;
	public static final String ALL_BINDING_TYPES = "E";
	
	private ExportPredef exportProfile;
	private ComAdmin admin;
	private ComMailinglistService mailinglistService;
    private MailinglistApprovalService mailinglistApprovalService;
    
	/**
	 * Descriptive username for manually executed exports (non-AutoExport)
	 */
	private String username = null;
		
	private AutoExport autoExport = null;
	
	private RemoteFile remoteFile = null;
	
	public ExportPredef getExportProfile() {
		return exportProfile;
	}

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
	
	public final ComAdmin getAdmin() {
		return this.admin;
	}

	public RecipientExportWorker(ExportPredef exportProfile, ComAdmin admin) {
		super();
		this.exportProfile = exportProfile;
		this.admin = admin;
		
		setEncoding(exportProfile.getCharset());
		
		if (StringUtils.isNotEmpty(exportProfile.getSeparator())) {
			setDelimiter(exportProfile.getSeparator().toCharArray()[0]);
		}
		
		if (StringUtils.isNotEmpty(exportProfile.getDelimiter())) {
			setStringQuote(exportProfile.getDelimiter().toCharArray()[0]);
			setAlwaysQuote(exportProfile.isAlwaysQuote());
		}
	}

	@Override
	public GenericExportWorker call() throws Exception {
		try {
			if (startTime == null) {
				startTime = new Date();
	        }
			done = false;
			
			int companyID = exportProfile.getCompanyID();
			List<Integer> mailingListIds = getMailingListIds();

			String targetSql = null;
			if (exportProfile.getTargetID() != 0) {
				try {
					targetSql = new JdbcTemplate(dataSource).queryForObject("SELECT target_sql FROM dyn_target_tbl WHERE company_id = ? AND target_id = ?", new StringRowMapper(), companyID, exportProfile.getTargetID());
				} catch (Exception e) {
					throw new Exception("Cannot read data for targetid " + exportProfile.getTargetID() + ": " + e.getMessage(), e);
				}
			}
			
			// Create basic Select statement
			selectParameters = new ArrayList<>();
			StringBuilder customerTableSql = new StringBuilder("SELECT ");
			boolean selectBounces = false;
			boolean isFirstColumn = true;
			if (StringUtils.isBlank(exportProfile.getColumns())) {
				throw new Exception("No export columns selected");
			}
			for (String columnName : AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(exportProfile.getColumns()))) {
				if ("mailing_bounce".equalsIgnoreCase(columnName)) {
					if (exportProfile.getUserStatus() == UserStatus.Bounce.getStatusCode()) {
						//show bounces only if user checked checkbox with bounces and Recipient status of the export is "Bounced"
						selectBounces = true;
						if (isFirstColumn) {
							isFirstColumn = false;
						} else {
							customerTableSql.append(", ");
						}
						customerTableSql.append("bounce.mailing_id mailing_id, bounce.detail bounce_detail");
					}
				} else {
					if (isFirstColumn) {
						isFirstColumn = false;
					} else {
						customerTableSql.append(", ");
					}
					customerTableSql.append("cust.").append(columnName).append(" ").append(columnName);
				}
			}

			for (int selectedMailinglistID : mailingListIds) {
				customerTableSql.append(", m" + selectedMailinglistID + ".user_status Userstate_Mailinglist_" + selectedMailinglistID);
				customerTableSql.append(", m" + selectedMailinglistID + ".timestamp Mailinglist_" + selectedMailinglistID + "_Timestamp");
				customerTableSql.append(", m" + selectedMailinglistID + ".user_remark Mailinglist_" + selectedMailinglistID + "_UserRemark");
			}

			customerTableSql.append(" FROM customer_").append(companyID).append("_tbl cust");
			if (selectBounces) {
				customerTableSql.append(" LEFT OUTER JOIN ("
					+ "SELECT customer_id, exit_mailing_id AS mailing_id, 510 AS detail"
					+ " FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_status = " + UserStatus.Bounce.getStatusCode()
					+ ") bounce ON cust.customer_id = bounce.customer_id");
			}
			if (StringUtils.containsIgnoreCase(targetSql, "bind.")) {
				customerTableSql.append(" JOIN customer_").append(companyID).append("_binding_tbl bind");
				customerTableSql.append(" ON cust.customer_id = bind.customer_id");
			}

			for (int selectedMailinglistID : mailingListIds) {
				customerTableSql.append(" LEFT OUTER JOIN customer_").append(companyID).append("_binding_tbl m" + selectedMailinglistID
						+ " ON m" + selectedMailinglistID + ".customer_id = cust.customer_id"
						+ " AND m" + selectedMailinglistID + ".mailinglist_id = " + selectedMailinglistID);
			}

			// Add where clauses to basic Select statement
			customerTableSql.append(" WHERE ").append(ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD + " = 0");
			if (exportProfile.getMailinglistID() == NO_MAILINGLIST) {
				customerTableSql.append(" AND NOT EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE cust.customer_id = bind.customer_id)");
			} else {
				StringBuilder existsBindingSqlPart = new StringBuilder();
				if (exportProfile.getMailinglistID() > 0) {
					existsBindingSqlPart.append("bind.mailinglist_id = ?");
					selectParameters.add(exportProfile.getMailinglistID());
				}
				
				if (!ALL_BINDING_TYPES.equals(exportProfile.getUserType())) {
					if (existsBindingSqlPart.length() > 0) {
						existsBindingSqlPart.append(" AND ");
					}
					existsBindingSqlPart.append("bind.user_type = ?");
					selectParameters.add(exportProfile.getUserType());
				}

				if (exportProfile.getUserStatus() != 0) {
					if (existsBindingSqlPart.length() > 0) {
						existsBindingSqlPart.append(" AND ");
					}
					existsBindingSqlPart.append("bind.user_status = ?");
					selectParameters.add(exportProfile.getUserStatus());
				}
				
				if (existsBindingSqlPart.length() > 0) {
					customerTableSql.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND " + existsBindingSqlPart.toString() + ")");
				}
			}

			if (StringUtils.isNotBlank(targetSql) ) {
				customerTableSql.append(" AND (").append(targetSql).append(")");
			}

			// Add date limits for change date to sql statement
			if (exportProfile.getTimestampStart() != null) {
				customerTableSql.append(" AND cust.timestamp >= ?");
				selectParameters.add(exportProfile.getTimestampStart());
			}
			if (exportProfile.getTimestampEnd() != null) {
				customerTableSql.append(" AND cust.timestamp < ?");
				Calendar endGregDate = new GregorianCalendar();
				endGregDate.setTime(exportProfile.getTimestampEnd());
				endGregDate.add(Calendar.DAY_OF_MONTH, 1);
				endGregDate = DateUtilities.removeTime(endGregDate);
				selectParameters.add(endGregDate.getTime());
			}
			if (exportProfile.getTimestampLastDays() > 0) {
				customerTableSql.append(" AND cust.timestamp >= ?");
				selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getTimestampLastDays())));
				customerTableSql.append(" AND cust.timestamp < ?");
				selectParameters.add(DateUtilities.midnight());
			}

			// Add date limits for create date to sql statement
			if (exportProfile.getCreationDateStart() != null) {
				customerTableSql.append(" AND cust.creation_date >= ?");
				selectParameters.add(exportProfile.getCreationDateStart());
			}
			if (exportProfile.getCreationDateEnd() != null) {
				customerTableSql.append(" AND cust.creation_date < ?");
				Calendar endGregDate = new GregorianCalendar();
				endGregDate.setTime(exportProfile.getCreationDateEnd());
				endGregDate.add(Calendar.DAY_OF_MONTH, 1);
				endGregDate = DateUtilities.removeTime(endGregDate);
				selectParameters.add(endGregDate.getTime());
			}
			if (exportProfile.getCreationDateLastDays() > 0) {
				customerTableSql.append(" AND cust.creation_date >= ?");
				selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getCreationDateLastDays())));
				customerTableSql.append(" AND cust.creation_date < ?");
				selectParameters.add(DateUtilities.midnight());
			}

			// Add date limits for mailing list binding change date to sql statement
			for (int selectedMailinglistID : mailingListIds) {
				if (exportProfile.getMailinglistBindStart() != null) {
					customerTableSql.append(" AND m" + selectedMailinglistID + ".timestamp >= ?");
					selectParameters.add(exportProfile.getMailinglistBindStart());
				}
				if (exportProfile.getMailinglistBindEnd() != null) {
					customerTableSql.append(" AND m" + selectedMailinglistID + ".timestamp < ?");
					Calendar endGregDate = new GregorianCalendar();
					endGregDate.setTime(exportProfile.getMailinglistBindEnd());
					endGregDate.add(Calendar.DAY_OF_MONTH, 1);
					endGregDate = DateUtilities.removeTime(endGregDate);
					selectParameters.add(endGregDate.getTime());
				}
				if (exportProfile.getMailinglistBindLastDays() > 0) {
					customerTableSql.append(" AND m" + selectedMailinglistID + ".timestamp >= ?");
					selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getMailinglistBindLastDays())));
					customerTableSql.append(" AND m" + selectedMailinglistID + ".timestamp < ?");
					selectParameters.add(DateUtilities.midnight());
				}
			}

			selectStatement = customerTableSql.toString();
			
			// Execute export
			super.call();
			
			if (error != null) {
				throw error;
			}
			
			if (remoteFile != null) {
				remoteFile = new RemoteFile(remoteFile.getRemoteFilePath(), new File(exportFile), remoteFile.getDownloadDurationMillis());
			}
		} catch (Exception e) {
			if (endTime == null) {
				endTime = new Date();
	        }
			
			error = e;
		}
		
		return this;
	}

	protected List<Integer> getMailingListIds() {
		String separatedIds = exportProfile.getMailinglists();

		if (StringUtils.isNotBlank(separatedIds)) {
			return AgnUtils.splitAndTrimList(separatedIds).stream()
					.map(NumberUtils::toInt)
					.filter(id -> id > 0)
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	@Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}
}
