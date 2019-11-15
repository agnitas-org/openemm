/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.text.DateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.beans.AdminEntry;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.util.UserActivityLogActions;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class UserActivityLogExportWorker extends GenericExportWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(UserActivityLogExportWorker.class);

	private Date fromDate;
	private Date toDate;
	private String filterDescription;
	private int filterAction;
	private String filterAdminUserName;
	private List<AdminEntry> filterAdmins;

	public UserActivityLogExportWorker(Date fromDate, Date toDate, String filterDescription, int filterAction, String filterAdminUserName, List<AdminEntry> filterAdmins) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.filterDescription = filterDescription;
		this.filterAction = filterAction;
		this.filterAdminUserName = filterAdminUserName;
		this.filterAdmins = filterAdmins;
	}

	@Override
	public GenericExportWorker call() throws Exception {
		try {
			String sortColumn = "logtime";
			boolean sortDirectionAscending = true;

			SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT logtime, username, supervisor_name, action, description FROM userlog_tbl");
			sqlPreparedStatementManager.addWhereClause("logtime >= ?", fromDate);
			sqlPreparedStatementManager.addWhereClause("logtime < ?", toDate);

	        //  If set, any of the visible admins must match
	        if (filterAdmins != null && filterAdmins.size() > 0) {
	        	List<String> visibleAdminNameList = new ArrayList<>();
	        	for (AdminEntry visibleAdmin : filterAdmins) {
	        		if (visibleAdmin != null) {
		        		visibleAdminNameList.add(visibleAdmin.getUsername());
	        		}
	        	}
	        	if (visibleAdminNameList.size() > 0) {
	        		sqlPreparedStatementManager.addWhereClause(DbUtilities.makeBulkInClauseWithDelimiter(isOracleDB(), "username", visibleAdminNameList, "'"));
	        	}
	        }

	        // If set, the selected admin must match
	        if (StringUtils.isNotBlank(filterAdminUserName) && !"0".equals(filterAdminUserName)) {
	        	sqlPreparedStatementManager.addWhereClause("username = ?", filterAdminUserName);
	        }

			if (StringUtils.isNotBlank(filterDescription)) {
				if (isOracleDB()) {
					sqlPreparedStatementManager.addWhereClause("description LIKE ('%' || ? || '%')", filterDescription);
				} else {
					sqlPreparedStatementManager.addWhereClause("description LIKE CONCAT('%', ?, '%')", filterDescription);
				}
			}

	        // If set, the selected action must match
	        if (UserActivityLogActions.ANY.getIntValue() != filterAction) {
	        	if (UserActivityLogActions.LOGIN_LOGOUT.getIntValue() == filterAction) {
	            	sqlPreparedStatementManager.addWhereClause("action IN ('do login', 'do logout', 'login', 'logout', ?)", UserActivityLogActions.getLocalValue(filterAction));
	        	} else if (UserActivityLogActions.ANY_WITHOUT_LOGIN.getIntValue() == filterAction) {
	            	sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
	        	} else {
					sqlPreparedStatementManager.addAndClause();
					sqlPreparedStatementManager.appendOpeningParenthesis();
					String[] localValues = UserActivityLogActions.getLocalValues(filterAction);
					for(int i = 0; i < localValues.length; i++){
						if(i != 0){
							sqlPreparedStatementManager.addOrClause();
						}
						sqlPreparedStatementManager.addWhereClauseSimple("(LOWER(action) LIKE ? OR LOWER(action) = ?)", localValues[i].toLowerCase() + " %", localValues[i].toLowerCase());
					}
					sqlPreparedStatementManager.appendClosingParenthesis();
	            	sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
	        	}
	        }

	        selectStatement = sqlPreparedStatementManager.getPreparedSqlString() + " ORDER BY " + sortColumn + " " + (sortDirectionAscending ? "ASC" : "DESC");
	        selectParameters = Arrays.asList(sqlPreparedStatementManager.getPreparedSqlParameters());

			// Execute export
			super.call();

			if (error != null) {
				throw error;
			}
		} catch (Exception e) {
			error = e;
		}

		return this;
	}

    public static Builder getBuilder(DataSource dataSource) {
        return new Builder(dataSource);
    }

    public static class Builder {
        private Date fromDate;
        private Date toDate;
        private String filterDescription;
        private int filterAction;
        private String filterAdminUserName;
        private List<AdminEntry> filterAdmins;
        private String exportFile;
        private DataSource dataSource;
        private DateFormat dateFormat;
        private DateFormat dateTimeFormat;
    	private ZoneId exportTimezone;

        public Builder(DataSource dataSource) {
        	this.dataSource = Objects.requireNonNull(dataSource);
        }

        public Builder setFromDate(Date fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder setToDate(Date toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder setFilterDescription(String filterDescription) {
            this.filterDescription = filterDescription;
            return this;
        }

        public Builder setFilterAction(int filterAction) {
            this.filterAction = filterAction;
            return this;
        }

        public Builder setFilterAdminUserName(String filterAdminUserName) {
            this.filterAdminUserName = filterAdminUserName;
            return this;
        }

        public Builder setFilterAdmins(List<AdminEntry> filterAdmins) {
            this.filterAdmins = filterAdmins;
            return this;
        }

        public Builder setExportFile(String exportFile) {
            this.exportFile = exportFile;
            return this;
        }

        public Builder setDateFormat(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        public Builder setDateTimeFormat(DateFormat dateTimeFormat) {
            this.dateTimeFormat = dateTimeFormat;
            return this;
        }

		public Builder setExportTimezone(ZoneId exportTimezone) {
            this.exportTimezone = exportTimezone;
            return this;
		}

        public UserActivityLogExportWorker build() {
            UserActivityLogExportWorker userActivityLogExportWorker = new UserActivityLogExportWorker(fromDate,
                    toDate,
                    filterDescription,
                    filterAction,
                    filterAdminUserName,
                    filterAdmins);

            userActivityLogExportWorker.setExportFile(exportFile);
            userActivityLogExportWorker.setDataSource(dataSource);
            userActivityLogExportWorker.setDateFormat(dateFormat);
            userActivityLogExportWorker.setDateTimeFormat(dateTimeFormat);
            userActivityLogExportWorker.setExportTimezone(exportTimezone);

            return userActivityLogExportWorker;
        }
    }
}
