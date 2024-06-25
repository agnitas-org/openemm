/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.text.DateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.beans.AdminEntry;
import org.agnitas.util.SqlPreparedStatementManager;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;

public class ActivityLogExportWorker extends GenericExportWorker {

    private final UserActivityLogFilterBase filter;
    private final Admin admin;
    private final UserActivityLogService.UserType userType;
    private final UserActivityLogService userActivityLogService;
    private final List<AdminEntry> filterAdmins;

    public ActivityLogExportWorker(UserActivityLogService.UserType userType, UserActivityLogFilterBase filter, List<AdminEntry> filterAdmins,
                                   Admin admin, UserActivityLogService userActivityLogService) {
        super();
        this.filter = filter;
        this.userType = userType;
        this.filterAdmins = filterAdmins;
        this.admin = admin;
        this.userActivityLogService = userActivityLogService;
    }

    @Override
    public GenericExportWorker call() throws Exception {
        Objects.requireNonNull(userType, "User type is not defined!");

        try {
            String sortColumn = "logtime";

            if (UserActivityLogService.UserType.SOAP.equals(userType) || UserActivityLogService.UserType.REST.equals(userType)) {
                sortColumn = "timestamp";
            }

            SqlPreparedStatementManager sqlPreparedStatementManager = userActivityLogService.prepareSqlStatementForDownload(filter, filterAdmins, userType, admin);

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

        private String exportFile;
        private DataSource dataSource;
        private DateFormat dateFormat;
        private DateFormat dateTimeFormat;
        private ZoneId exportTimezone;
        private UserActivityLogFilterBase filter;
        private UserActivityLogService userActivityLogService;
        private UserActivityLogService.UserType userType;
        private List<AdminEntry> filterAdmins;
        private Admin admin;

        public Builder(DataSource dataSource) {
            this.dataSource = Objects.requireNonNull(dataSource);
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

        public Builder setFilter(UserActivityLogFilterBase filter) {
            this.filter = filter;
            return this;
        }

        public Builder setAdmin(Admin admin) {
            this.admin = admin;
            return this;
        }

        public Builder setFilterAdmins(List<AdminEntry> filterAdmins) {
            this.filterAdmins = filterAdmins;
            return this;
        }

        public ActivityLogExportWorker build() {
            ActivityLogExportWorker worker = new ActivityLogExportWorker(
                    userType,
                    filter,
                    filterAdmins,
                    admin,
                    userActivityLogService
            );

            worker.setExportFile(exportFile);
            worker.setDataSource(dataSource);
            worker.setDateFormat(dateFormat);
            worker.setDateTimeFormat(dateTimeFormat);
            worker.setExportTimezone(exportTimezone);

            return worker;
        }
    }
}
