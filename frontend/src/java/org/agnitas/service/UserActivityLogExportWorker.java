/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import org.agnitas.beans.AdminEntry;
import org.agnitas.util.SqlPreparedStatementManager;

import javax.sql.DataSource;
import java.text.DateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class UserActivityLogExportWorker extends GenericExportWorker {

	private final Date fromDate;
	private final Date toDate;
	private final String filterDescription;
	private final int filterAction;
	private final String filterAdminUserName;
	private final List<AdminEntry> filterAdmins;
	private final UserActivityLogService.UserType userType;
	private final UserActivityLogService userActivityLogService;

	public UserActivityLogExportWorker(Date fromDate, Date toDate, String filterDescription, int filterAction, String filterAdminUserName,
                                       List<AdminEntry> filterAdmins, UserActivityLogService userActivityLogService,
                                       UserActivityLogService.UserType userType) {
		super();
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.filterDescription = filterDescription;
		this.filterAction = filterAction;
		this.filterAdminUserName = filterAdminUserName;
		this.filterAdmins = filterAdmins;
		this.userActivityLogService = userActivityLogService;
		this.userType = userType;
	}

	@Override
	public GenericExportWorker call() throws Exception {
        Objects.requireNonNull(userType, "User type is not defined!");

        try {
			String sortColumn = "logtime";

			if (UserActivityLogService.UserType.SOAP.equals(userType) || UserActivityLogService.UserType.REST.equals(userType)) {
			    sortColumn = "timestamp";
            }

			SqlPreparedStatementManager sqlPreparedStatementManager = userActivityLogService.prepareSqlStatementForDownload(
                filterAdmins,
                filterAdminUserName,
                filterAction,
                fromDate,
                toDate,
                filterDescription,
                userType
            );

	        selectStatement = sqlPreparedStatementManager.getPreparedSqlString() + " ORDER BY " + sortColumn + " ASC";
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
		private UserActivityLogService userActivityLogService;
        private UserActivityLogService.UserType userType;

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

		public Builder setUserActivityLogService(UserActivityLogService userActivityLogService) {
			this.userActivityLogService = userActivityLogService;
			return this;
		}

		public Builder setUserActivityType(UserActivityLogService.UserType userType) {
			this.userType = userType;
			return this;
		}

        public UserActivityLogExportWorker build() {
            UserActivityLogExportWorker userActivityLogExportWorker = new UserActivityLogExportWorker(fromDate,
                    toDate,
                    filterDescription,
                    filterAction,
                    filterAdminUserName,
                    filterAdmins,
                    userActivityLogService,
                    userType
			);

            userActivityLogExportWorker.setExportFile(exportFile);
            userActivityLogExportWorker.setDataSource(dataSource);
            userActivityLogExportWorker.setDateFormat(dateFormat);
            userActivityLogExportWorker.setDateTimeFormat(dateTimeFormat);
            userActivityLogExportWorker.setExportTimezone(exportTimezone);

            return userActivityLogExportWorker;
        }
    }
}
