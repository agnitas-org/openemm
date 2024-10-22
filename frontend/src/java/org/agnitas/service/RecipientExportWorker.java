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

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.export.util.ExportUtils;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.target.service.ComTargetService;

public class RecipientExportWorker extends GenericExportWorker {

	public static final String EXPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientExport";

	public static final int NO_MAILINGLIST = -1;
	public static final int ALL_MAILINGLISTS = 0;
	public static final String ALL_BINDING_TYPES = "E";

	private final ComTargetService targetService;
	private final MailinglistService mailinglistService;
    private final RecipientFieldService recipientFieldService;

	private ExportPredef exportProfile;
	protected Admin admin;
    
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
	
	public final Admin getAdmin() {
		return admin;
	}

	public RecipientExportWorker(ExportPredef exportProfile, Admin admin, final ComTargetService targetService, final RecipientFieldService recipientFieldService, MailinglistService mailinglistService) throws Exception {
		super();
		this.exportProfile = exportProfile;
		this.admin = admin;
		
		setEncoding(exportProfile.getCharset());
		
		if (StringUtils.isNotEmpty(exportProfile.getSeparator())) {
			setDelimiter(exportProfile.getSeparator().toCharArray()[0]);
		}

		setUseDecodedValues(exportProfile.isUseDecodedValues());
		setLocale(exportProfile.getLocale());
		
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
        this.recipientFieldService = recipientFieldService;
		this.mailinglistService = mailinglistService;
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

			// Create basic Select statement
			selectParameters = new ArrayList<>();
			StringBuilder customerTableSql = new StringBuilder("SELECT ");
			boolean selectBounces = false;
			boolean isFirstColumn = true;
            List<ExportColumnMapping> profileFieldsToExport = getProfileFieldsToExport(companyID, admin);
            
    		CaseInsensitiveMap<String, RecipientFieldDescription> profilefields = new CaseInsensitiveMap<>();
            for (RecipientFieldDescription field : recipientFieldService.getRecipientFields(companyID)) {
    			ProfileFieldMode permission;
	    		if (admin != null) {
	    			permission = field.getAdminPermission(admin.getAdminID());
	    		} else {
	    			permission = field.getDefaultPermission();
	    		}
	    		if (permission == ProfileFieldMode.Editable || permission == ProfileFieldMode.ReadOnly) {
    				profilefields.put(field.getColumnName(), field);
    			}
            }
    		
    		for (ExportColumnMapping exportColumnMapping : profileFieldsToExport) {
    			if (profilefields.containsKey(exportColumnMapping.getDbColumn())) {
    				// Allways export customer fields as upper case because clients processes may break otherwise 
    				exportColumnMapping.setDbColumn(exportColumnMapping.getDbColumn().toUpperCase());
    			}
    		}
    		
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
				} else if (!profilefields.containsKey(columnName)) {
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
            columnMappings.addAll(ExportUtils.getCustomColumnMappingsFromExport(exportProfile, companyID, admin, recipientFieldService));
            
			for (int selectedMailinglistID : mailingListIds) {
				String mailinglistStr = "Mailinglist_" + selectedMailinglistID;
				String mailinglistDecodedStr = mailinglistStr;
				if (exportProfile.isUseDecodedValues()) {
					mailinglistDecodedStr = mailinglistService.getMailinglistName(selectedMailinglistID, companyID) + "(" + selectedMailinglistID + ")";
				}

				customerTableSql.append(String.format(", m%d.user_status AS Userstate_%s", selectedMailinglistID, mailinglistStr));
				if (exportProfile.isUseDecodedValues()) {
					columnMappings.add(new ExportColumnMapping("Userstate_" + mailinglistStr,  "Userstate_" + mailinglistDecodedStr, null, false));
				} else {
					columnMappings.add(new ExportColumnMapping("Userstate_" + mailinglistStr,  "Userstate_" + mailinglistStr , null, false));
				}
				customerTableSql.append(String.format(", m%d.timestamp AS %s_Timestamp", selectedMailinglistID, mailinglistStr));
				columnMappings.add(new ExportColumnMapping(mailinglistStr + "_Timestamp", mailinglistDecodedStr + "_Timestamp", null, false));
				customerTableSql.append(", CONCAT(m" + selectedMailinglistID + ".user_remark, CASE WHEN (m" + selectedMailinglistID + ".exit_mailing_id IS NULL OR m" + selectedMailinglistID + ".exit_mailing_id = 0) THEN '' ELSE CONCAT(' ExitMailingID: ', m" + selectedMailinglistID + ".exit_mailing_id) END) AS " + mailinglistStr + "_UserRemark");
				columnMappings.add(new ExportColumnMapping(mailinglistStr + "_UserRemark", mailinglistDecodedStr + "_UserRemark", null, false));
				if (selectBounces) {
					customerTableSql.append(String.format(", m%d.exit_mailing_id AS %s_ExitMailID", selectedMailinglistID, mailinglistStr));
					columnMappings.add(new ExportColumnMapping(mailinglistStr + "_ExitMailID", mailinglistDecodedStr + "_ExitMailID", null, false));
					customerTableSql.append(String.format(", 510 AS %s_Detail", mailinglistStr));
					columnMappings.add(new ExportColumnMapping(mailinglistStr + "_Detail", mailinglistDecodedStr + "_Detail", null, false));
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
			customerTableSql.append(" WHERE ").append(RecipientStandardField.Bounceload.getColumnName() + " = 0");
			if (isNoMailinglistPresent()) {
				customerTableSql.append(" AND NOT EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE cust.customer_id = bind.customer_id)");
			} else {
				StringBuilder existsBindingSqlPart = new StringBuilder();
				
				applyMailingLists(existsBindingSqlPart);
				
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
			
			if (autoExport != null && autoExport.isConsiderLastRun() && autoExport.getLaststart() != null) {
				String changedateLimitationWherePart = "cust.creation_date >= ? OR cust.timestamp >= ?";
				selectParameters.add(autoExport.getLaststart());
				selectParameters.add(autoExport.getLaststart());
				
				for (int selectedMailinglistID : mailingListIds) {
					changedateLimitationWherePart += " OR m" + selectedMailinglistID + ".creation_date >= ? OR m" + selectedMailinglistID + ".timestamp >= ?";
					selectParameters.add(autoExport.getLaststart());
					selectParameters.add(autoExport.getLaststart());
				}
				
				customerTableSql.append(" AND (" + changedateLimitationWherePart + ")");
			}

			// Add date limits for change date to sql statement
			TimeZone timeZone;
			if (admin != null) {
				timeZone = TimeZone.getTimeZone(admin.getAdminTimezone());
			} else {
				timeZone = TimeZone.getDefault();
			}
			
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

	protected void applyMailingLists(StringBuilder sql) {
		if (exportProfile.getMailinglistID() > 0) {
			sql.append("bind.mailinglist_id = ?");
			selectParameters.add(exportProfile.getMailinglistID());
		}
	}

	protected boolean isNoMailinglistPresent() {
        return exportProfile.getMailinglistID() == NO_MAILINGLIST;
    }

    private List<String> getSplittedColumnNames(List<ExportColumnMapping> columnsToExport) {
        return AgnUtils.removeEmptyFromStringlist(AgnUtils.splitAndTrimList(
                columnsToExport.stream().map(ExportColumnMapping::getDbColumn).collect(Collectors.joining(";"))));
    }

    private List<ExportColumnMapping> getProfileFieldsToExport(int companyID, Admin adminParam) throws Exception {
        List<ExportColumnMapping> profileFieldColumns = ExportUtils.getProfileFieldColumnsFromExport(exportProfile, companyID, adminParam, recipientFieldService);
        if (profileFieldColumns.isEmpty()) {
        	List<RecipientFieldDescription> profileFields = new ArrayList<>();
            for (RecipientFieldDescription field : recipientFieldService.getRecipientFields(companyID)) {
    			ProfileFieldMode permission;
	    		if (adminParam != null) {
	    			permission = field.getAdminPermission(adminParam.getAdminID());
	    		} else {
	    			permission = field.getDefaultPermission();
	    		}
	    		if (permission == ProfileFieldMode.Editable || permission == ProfileFieldMode.ReadOnly) {
	    			profileFields.add(field);
    			}
            }
        	
            return profileFields.stream()
	            .map(profileField -> {
	            	ExportColumnMapping columnMapping = new ExportColumnMapping();
	                columnMapping.setDbColumn(profileField.getColumnName());
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

	protected String getTargetSql() {
		if (exportProfile.getTargetID() <= 0) {
			return null;
		} else {
			return targetService.getTargetSQL(exportProfile.getTargetID(), exportProfile.getCompanyID());
		}
	}

	protected ComTargetService getTargetService() {
		return this.targetService;
	}
}
