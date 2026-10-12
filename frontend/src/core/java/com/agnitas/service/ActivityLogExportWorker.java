/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.service;

import java.text.DateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.sql.DataSource;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.service.UserActivityLogService.UserType;
import com.agnitas.util.SqlPreparedStatementManager;

public class ActivityLogExportWorker extends GenericExportWorker {

    private final UserActivityLogFilterBase filter;
    private final Admin admin;
    private final UserType userType;
    private final UserActivityLogService userActivityLogService;

    public ActivityLogExportWorker(UserType userType, UserActivityLogFilterBase filter,
                                   Admin admin, UserActivityLogService userActivityLogService) {
        super();
        this.filter = filter;
        this.userType = userType;
        this.admin = admin;
        this.userActivityLogService = userActivityLogService;
    }

    @Override
    public GenericExportWorker call() {
        Objects.requireNonNull(userType, "User type is not defined!");

        try {
            SqlPreparedStatementManager sqlPreparedStatementManager = userActivityLogService.prepareSqlStatementForDownload(filter, userType, admin);

            String sortColumn = UserType.GUI.equals(userType) ? "logtime" : "timestamp";
            selectStatement = sqlPreparedStatementManager.getPreparedSqlString() + " ORDER BY " + sortColumn + " ASC";
            selectParameters = Optional.ofNullable(sqlPreparedStatementManager.getPreparedSqlParameters())
                    .map(Arrays::asList)
                    .orElse(Collections.emptyList());

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
        private UserType userType;
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

        public Builder setUserActivityType(UserType userType) {
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

        public ActivityLogExportWorker build() {
            ActivityLogExportWorker worker = new ActivityLogExportWorker(
                    userType,
                    filter,
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
