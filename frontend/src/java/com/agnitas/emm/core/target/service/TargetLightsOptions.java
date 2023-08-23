/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.service;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.agnitas.emm.core.target.AltgMode;

public class TargetLightsOptions {

    public static Builder builder() {
        return new Builder();
    }

    private TargetLightsOptions() {}

    private int adminId;
    private int companyId;
    private boolean includeDeleted;
    private boolean worldDelivery;
    private boolean adminTestDelivery;
    private boolean content;
    private boolean isSearchName;
    private boolean isSearchDescription;
    private String searchText = "";
    private AltgMode altgMode = AltgMode.ALL;
    private int pageNumber;
    private int pageSize;
    private String sorting;
    private String direction;

    public int getAdminId() {
        return adminId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public boolean isIncludeDeleted() {
        return includeDeleted;
    }

    public boolean isWorldDelivery() {
        return worldDelivery;
    }

    public boolean isAdminTestDelivery() {
        return adminTestDelivery;
    }

    public boolean isContent() {
        return content;
    }

    public boolean isSearchName() {
        return isSearchName;
    }

    public boolean isSearchDescription() {
        return isSearchDescription;
    }

    public String getSearchText() {
        return searchText;
    }

    public AltgMode getAltgMode() {
        return altgMode;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getSorting() {
        return sorting;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("adminId", adminId)
                .append("companyId", companyId)
                .append("includeDeleted", includeDeleted)
                .append("worldDelivery", worldDelivery)
                .append("adminTestDelivery", adminTestDelivery)
                .append("content", content)
                .append("isSearchName", isSearchName)
                .append("isSearchDescription", isSearchDescription)
                .append("searchText", searchText)
                .append("altgMode", altgMode)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetLightsOptions that = (TargetLightsOptions) o;
        return adminId == that.adminId &&
                companyId == that.companyId &&
                includeDeleted == that.includeDeleted &&
                worldDelivery == that.worldDelivery &&
                adminTestDelivery == that.adminTestDelivery &&
                content == that.content &&
                isSearchName == that.isSearchName &&
                isSearchDescription == that.isSearchDescription &&
                Objects.equals(searchText, that.searchText) &&
                altgMode == that.altgMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, companyId, includeDeleted, worldDelivery, adminTestDelivery, content, isSearchName, isSearchDescription, searchText, altgMode);
    }

    public static class Builder {
        private TargetLightsOptions options = new TargetLightsOptions();

        public Builder setAdminId(int adminId) {
            options.adminId = adminId;
            return this;
        }

        public Builder setCompanyId(int companyId) {
            options.companyId = companyId;
            return this;
        }

        public Builder setIncludeDeleted(boolean includeDeleted) {
            options.includeDeleted = includeDeleted;
            return this;
        }

        public Builder setWorldDelivery(boolean worldDelivery) {
            options.worldDelivery = worldDelivery;
            return this;
        }

        public Builder setAdminTestDelivery(boolean adminTestDelivery) {
            options.adminTestDelivery = adminTestDelivery;
            return this;
        }

        public Builder setContent(boolean content) {
            options.content = content;
            return this;
        }

        public Builder setSearchName(boolean searchName) {
            options.isSearchName = searchName;
            return this;
        }

        public Builder setSearchDescription(boolean searchDescription) {
            options.isSearchDescription = searchDescription;
            return this;
        }

        public Builder setSearchText(String searchText) {
            options.searchText = searchText;
            return this;
        }

        public Builder setAltgMode(AltgMode altgMode) {
            options.altgMode = altgMode;
            return this;
        }

        public Builder setPageNumber(int pageNumber) {
            options.pageNumber = pageNumber;
            return this;
        }

        public Builder setPageSize(int pageSize) {
            options.pageSize = pageSize;
            return this;
        }

        public Builder setSorting(String sorting) {
            options.sorting = sorting;
            return this;
        }

        public Builder setDirection(String direction) {
            options.direction = direction;
            return this;
        }

        public TargetLightsOptions build() {
            TargetLightsOptions result = this.options;
            this.options = null;
            return  result;
        }
    }
}
