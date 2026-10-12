/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.List;
import java.util.Locale;

import com.agnitas.dao.AdminDao.UserListAdditionalColumn;
import com.agnitas.emm.core.commons.dto.DateRange;

public record UserListOpts(
        int companyId,
        String searchFirstName,
        String searchLastName,
        String searchEmail,
        String searchCompanyName,
        Integer filterCompanyId,
        Integer filterAdminGroupId,
        Integer filterMailinglistId,
        String filterLanguage,
        DateRange creationDate,
        DateRange lastLoginDate,
        String username,
        String sort,
        boolean sortDirectionAsc,
        int pageNumber,
        int pageSize,
        List<UserListAdditionalColumn> additionalColumns,
        Locale locale
) {

    public UserListOpts {
        if (companyId < 0) {
            throw new IllegalArgumentException("companyId must be >= 0");
        }
        if (creationDate == null) {
            creationDate = new DateRange();
        }
        if (lastLoginDate == null) {
            lastLoginDate = new DateRange();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int companyId;
        private String searchFirstName;
        private String searchLastName;
        private String searchEmail;
        private String searchCompanyName;
        private Integer filterCompanyId;
        private Integer filterAdminGroupId;
        private Integer filterMailinglistId;
        private String filterLanguage;
        private DateRange creationDate;
        private DateRange lastLoginDate;
        private String username;
        private String sort;
        private boolean sortDirectionAsc;
        private int pageNumber;
        private int pageSize;
        private List<UserListAdditionalColumn> additionalColumns;
        private Locale locale;

        public Builder companyId(int value) {
            this.companyId = value;
            return this;
        }

        public Builder searchFirstName(String value) {
            this.searchFirstName = value;
            return this;
        }

        public Builder searchLastName(String value) {
            this.searchLastName = value;
            return this;
        }

        public Builder searchEmail(String value) {
            this.searchEmail = value;
            return this;
        }

        public Builder searchCompanyName(String value) {
            this.searchCompanyName = value;
            return this;
        }

        public Builder filterCompanyId(Integer value) {
            this.filterCompanyId = value;
            return this;
        }

        public Builder filterAdminGroupId(Integer value) {
            this.filterAdminGroupId = value;
            return this;
        }

        public Builder filterMailinglistId(Integer value) {
            this.filterMailinglistId = value;
            return this;
        }

        public Builder filterLanguage(String value) {
            this.filterLanguage = value;
            return this;
        }

        public Builder creationDate(DateRange value) {
            this.creationDate = value;
            return this;
        }

        public Builder lastLoginDate(DateRange value) {
            this.lastLoginDate = value;
            return this;
        }

        public Builder username(String value) {
            this.username = value;
            return this;
        }

        public Builder sort(String value) {
            this.sort = value;
            return this;
        }

        public Builder sortDirectionAsc(boolean value) {
            this.sortDirectionAsc = value;
            return this;
        }

        public Builder pageNumber(int value) {
            this.pageNumber = value;
            return this;
        }

        public Builder pageSize(int value) {
            this.pageSize = value;
            return this;
        }

        public Builder additionalColumns(List<UserListAdditionalColumn> value) {
            this.additionalColumns = value;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public UserListOpts build() {
            return new UserListOpts(
                    companyId,
                    searchFirstName,
                    searchLastName,
                    searchEmail,
                    searchCompanyName,
                    filterCompanyId,
                    filterAdminGroupId,
                    filterMailinglistId,
                    filterLanguage,
                    creationDate,
                    lastLoginDate,
                    username,
                    sort,
                    sortDirectionAsc,
                    pageNumber,
                    pageSize,
                    additionalColumns,
                    locale
            );
        }
    }
}
