/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.agnitas.beans.ExportColumnMapping;
import org.agnitas.beans.ExportPredef;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoimport.service.RemoteFile;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.web.ExportException;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.export.util.ExportWizardUtils;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComColumnInfoService;

public class RecipientExportWorker extends GenericExportWorker {

	public static final String EXPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientExport";

	public static final int NO_MAILINGLIST = -1;
	public static final String ALL_BINDING_TYPES = "E";

	private final ComTargetService targetService;
    private final ComColumnInfoService columnInfoService;

	private ExportPredef exportProfile;
	private ComAdmin admin;
    
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

	public RecipientExportWorker(ExportPredef exportProfile, ComAdmin admin, final ComTargetService targetService, final ComColumnInfoService columnInfoService) throws Exception {
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

		setDateFormat(new SimpleDateFormat(DateFormat.getDateFormatById(exportProfile.getDateFormat()).getValue()));
		setDateTimeFormat(new SimpleDateFormat(DateFormat.getDateFormatById(exportProfile.getDateTimeFormat()).getValue()));
		setExportTimezone(ZoneId.of(exportProfile.getTimezone()));
		
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		if (",".equals(exportProfile.getDecimalSeparator())) {
			decimalFormatSymbols.setDecimalSeparator(',');
			decimalFormatSymbols.setGroupingSeparator('.');
		} else {
			decimalFormatSymbols.setDecimalSeparator('.');
			decimalFormatSymbols.setGroupingSeparator(',');
		}
		setDecimalFormat(new DecimalFormat("###0.###", decimalFormatSymbols));

		this.targetService = targetService;
        this.columnInfoService = columnInfoService;
	}

	@Override
	public GenericExportWorker call() throws Exception {
		try {
			if (startTime == null) {
				startTime = new Date();
	        }
			done = false;
			
			int companyID = exportProfile.getCompanyID();
            int adminId = admin.getAdminID();
			List<Integer> mailingListIds = getMailingListIds();

			// Create basic Select statement
			selectParameters = new ArrayList<>();
			StringBuilder customerTableSql = new StringBuilder("SELECT ");
			boolean selectBounces = false;
			boolean isFirstColumn = true;
            List<ExportColumnMapping> profileFieldsToExport = getProfileFieldsToExport(companyID, adminId);

            CaseInsensitiveMap<String, ProfileField> profilefields = columnInfoService.getColumnInfoMap(companyID, admin.getAdminID());
            
            for (String columnName : getSplittedColumnNames(profileFieldsToExport)) {
				if ("mailing_bounce".equalsIgnoreCase(columnName)) {
					if (exportProfile.getUserStatus() == UserStatus.Bounce.getStatusCode()) {
						//show bounces only if user checked checkbox with bounces and Recipient status of the export is "Bounced"
						selectBounces = true;
						if (mailingListIds.size() == 0) {
							if (isFirstColumn) {
								isFirstColumn = false;
							} else {
								customerTableSql.append(", ");
							}
							customerTableSql.append("bounce.exit_mailing_id AS ExitMailingID, bounce.mailinglist_id AS Mailinglist, bounce.detail AS BounceDetail");
						}
					}
				} else if (profilefields.get(columnName).getModeEdit() != ProfileField.MODE_EDIT_EDITABLE && profilefields.get(columnName).getModeEdit() != ProfileField.MODE_EDIT_READONLY) {
					throw new ExportException(false, ExportException.Reason.ColumnNotExportableError, columnName);
				} else {
					if (isFirstColumn) {
						isFirstColumn = false;
					} else {
						customerTableSql.append(", ");
					}
					customerTableSql.append("cust.").append(columnName).append(" ").append(columnName);
				}
			}
            columnMappings = new ArrayList<>(profileFieldsToExport);
            columnMappings.addAll(ExportWizardUtils.getCustomColumnMappingsFromExport(exportProfile, companyID, adminId, columnInfoService));
            
			for (int selectedMailinglistID : mailingListIds) {
				customerTableSql.append(", m" + selectedMailinglistID + ".user_status AS Userstate_Mailinglist_" + selectedMailinglistID);
				columnMappings.add(new ExportColumnMapping("Userstate_Mailinglist_" + selectedMailinglistID, "Userstate_Mailinglist_" + selectedMailinglistID, null, false));
				customerTableSql.append(", m" + selectedMailinglistID + ".timestamp AS Mailinglist_" + selectedMailinglistID + "_Timestamp");
				columnMappings.add(new ExportColumnMapping("Mailinglist_" + selectedMailinglistID + "_Timestamp", "Mailinglist_" + selectedMailinglistID + "_Timestamp", null, false));
				customerTableSql.append(", CONCAT(m" + selectedMailinglistID + ".user_remark, CASE WHEN (m" + selectedMailinglistID + ".exit_mailing_id IS NULL OR m" + selectedMailinglistID + ".exit_mailing_id = 0) THEN '' ELSE CONCAT(' ExitMailingID: ', m" + selectedMailinglistID + ".exit_mailing_id) END) AS Mailinglist_" + selectedMailinglistID + "_UserRemark");
				columnMappings.add(new ExportColumnMapping("Mailinglist_" + selectedMailinglistID + "_UserRemark", "Mailinglist_" + selectedMailinglistID + "_UserRemark", null, false));
				if (selectBounces) {
					customerTableSql.append(", m" + selectedMailinglistID + ".exit_mailing_id AS Mailinglist_" + selectedMailinglistID + "_ExitMailID");
					columnMappings.add(new ExportColumnMapping("Mailinglist_" + selectedMailinglistID + "_ExitMailID", "Mailinglist_" + selectedMailinglistID + "_ExitMailID", null, false));
					customerTableSql.append(", 510 AS Mailinglist_" + selectedMailinglistID + "_Detail");
					columnMappings.add(new ExportColumnMapping("Mailinglist_" + selectedMailinglistID + "_Detail", "Mailinglist_" + selectedMailinglistID + "_Detail", null, false));
				}
			}

			customerTableSql.append(" FROM customer_").append(companyID).append("_tbl cust");
			if (selectBounces && mailingListIds.size() == 0) {
				customerTableSql.append(" LEFT OUTER JOIN ("
					+ "SELECT customer_id, mailinglist_id, exit_mailing_id, 510 AS detail"
					+ " FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_status = " + UserStatus.Bounce.getStatusCode()
					+ ") bounce ON cust.customer_id = bounce.customer_id");
			}

			final String targetSql = getTargetSql();
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
			TimeZone timeZone = TimeZone.getTimeZone(admin.getAdminTimezone());
			
			String timestampLimitPart = "";
			if (exportProfile.getTimestampStart() != null) {
				timestampLimitPart += "cust.timestamp >= ?";
				selectParameters.add(exportProfile.getTimestampStart());
			}
			if (exportProfile.getTimestampEnd() != null) {
				if (timestampLimitPart.length() > 0) {
					timestampLimitPart += " AND ";
				}
				timestampLimitPart += "cust.timestamp < ?";
				Calendar endGregDate = new GregorianCalendar(timeZone);
				endGregDate.setTime(exportProfile.getTimestampEnd());
				endGregDate.add(Calendar.DAY_OF_MONTH, 1);
				endGregDate = DateUtilities.removeTime(endGregDate);
				selectParameters.add(endGregDate.getTime());
			}
			if (exportProfile.getTimestampLastDays() > 0) {
				if (timestampLimitPart.length() > 0) {
					timestampLimitPart += " AND ";
				}
				timestampLimitPart += "cust.timestamp >= ?";
				selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getTimestampLastDays()), timeZone));
				
				if (!exportProfile.isTimestampIncludeCurrentDay()) {
					// Exclude data of current day
					timestampLimitPart += " AND cust.timestamp < ?";
					selectParameters.add(DateUtilities.midnight(timeZone));
				}
			}

			String creationDateLimitPart = "";
			// Add date limits for create date to sql statement
			if (exportProfile.getCreationDateStart() != null) {
				creationDateLimitPart += "cust.creation_date >= ?";
				selectParameters.add(exportProfile.getCreationDateStart());
			}
			if (exportProfile.getCreationDateEnd() != null) {
				if (creationDateLimitPart.length() > 0) {
					creationDateLimitPart += " AND ";
				}
				creationDateLimitPart += "cust.creation_date < ?";
				Calendar endGregDate = new GregorianCalendar(timeZone);
				endGregDate.setTime(exportProfile.getCreationDateEnd());
				endGregDate.add(Calendar.DAY_OF_MONTH, 1);
				endGregDate = DateUtilities.removeTime(endGregDate);
				selectParameters.add(endGregDate.getTime());
			}
			if (exportProfile.getCreationDateLastDays() > 0) {
				if (creationDateLimitPart.length() > 0) {
					creationDateLimitPart += " AND ";
				}
				creationDateLimitPart += "cust.creation_date >= ?";
				selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getCreationDateLastDays()), timeZone));
				
				if (!exportProfile.isCreationDateIncludeCurrentDay()) {
					// Exclude data of current day
					creationDateLimitPart += " AND cust.creation_date < ?";
					selectParameters.add(DateUtilities.midnight(timeZone));
				}
			}

			// Add date limits for mailing list binding change date to sql statement
			List<String> mailinglistChangeClauses = new ArrayList<>();
			for (int selectedMailinglistID : mailingListIds) {
				String mailinglistChangeClause = "";
				
				if (exportProfile.getMailinglistBindStart() != null) {
					mailinglistChangeClause = "m" + selectedMailinglistID + ".timestamp >= ?";
					selectParameters.add(exportProfile.getMailinglistBindStart());
				}
				if (exportProfile.getMailinglistBindEnd() != null) {
					if (mailinglistChangeClause.length() > 0) {
						mailinglistChangeClause += " AND ";
					}
					mailinglistChangeClause += "m" + selectedMailinglistID + ".timestamp < ?";
					Calendar endGregDate = new GregorianCalendar(timeZone);
					endGregDate.setTime(exportProfile.getMailinglistBindEnd());
					endGregDate.add(Calendar.DAY_OF_MONTH, 1);
					endGregDate = DateUtilities.removeTime(endGregDate);
					selectParameters.add(endGregDate.getTime());
				}
				if (exportProfile.getMailinglistBindLastDays() > 0) {
					if (mailinglistChangeClause.length() > 0) {
						mailinglistChangeClause += " AND ";
					}
					mailinglistChangeClause += "m" + selectedMailinglistID + ".timestamp >= ?";
					selectParameters.add(DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(exportProfile.getMailinglistBindLastDays()), timeZone));

					if (!exportProfile.isMailinglistBindIncludeCurrentDay()) {
						// Exclude data of current day
						mailinglistChangeClause += " AND m" + selectedMailinglistID + ".timestamp < ?";
						selectParameters.add(DateUtilities.midnight(timeZone));
					}
				}
				
				if (mailinglistChangeClause.length() > 0) {
					mailinglistChangeClauses.add(mailinglistChangeClause);
				}
			}
			
			String timeLimitLinkOperator = "AND";
			if (!exportProfile.isTimeLimitsLinkedByAnd()) {
				timeLimitLinkOperator = "OR";
			}
			
			String timeLimitPart = "";
			if (timestampLimitPart.length() > 0) {
				timeLimitPart += "(" + timestampLimitPart + ")";
			}
			if (creationDateLimitPart.length() > 0) {
				if (timeLimitPart.length() > 0) {
					timeLimitPart += " " + timeLimitLinkOperator + " ";
				}
				timeLimitPart += "(" + creationDateLimitPart + ")";
			}
			if (mailinglistChangeClauses.size() > 0) {
				if (timeLimitPart.length() > 0) {
					timeLimitPart += " " + timeLimitLinkOperator + " ";
				}
				if (mailinglistChangeClauses.size() == 1) {
					timeLimitPart += "(" + mailinglistChangeClauses.get(0) + ")";
				} else {
					timeLimitPart += "((" + StringUtils.join(mailinglistChangeClauses, ") OR (") + "))";
				}
			}
			if (timeLimitPart.length() > 0) {
				customerTableSql.append(" AND (" + timeLimitPart + ")");
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

    private List<String> getSplittedColumnNames(List<ExportColumnMapping> columnsToExport) {
        return AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(
                columnsToExport.stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.joining(";"))));
    }

    private List<ExportColumnMapping> getProfileFieldsToExport(int companyID, int adminId) throws Exception {
        List<ExportColumnMapping> profileFieldColumns = ExportWizardUtils
                .getProfileFieldColumnsFromExport(exportProfile, companyID, adminId, columnInfoService);
        if (profileFieldColumns.isEmpty()) {
            return columnInfoService.getComColumnInfos(companyID, adminId).stream()
	            .filter(new Predicate<ProfileField>() {
					@Override
					public boolean test(ProfileField profileField) {
						return profileField.getModeEdit() != ProfileField.MODE_EDIT_NOT_VISIBLE;
					}
				})
	            .map(profileField -> {
	            	ExportColumnMapping columnMapping = new ExportColumnMapping();
	                columnMapping.setDbColumn(profileField.getColumn());
	                return columnMapping;
	            })
	            .collect(Collectors.toList());
        } else {
            return profileFieldColumns.stream().map(profileField -> {
            	ExportColumnMapping columnMapping = new ExportColumnMapping();
                columnMapping.setDbColumn(profileField.getDbColumn());
                return columnMapping;
            }).collect(Collectors.toList());
        }
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

	protected String getTargetSql() throws Exception {
		if (exportProfile.getTargetID() <= 0) {
			return null;
		}

		return targetService.getTargetSQL(exportProfile.getTargetID(), exportProfile.getCompanyID());
	}

	protected ComTargetService getTargetService() {
		return this.targetService;
	}
}
