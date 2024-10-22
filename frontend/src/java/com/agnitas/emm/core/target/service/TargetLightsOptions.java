/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.service;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.dto.IntRange;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TargetLightsOptions {

    public static Builder builder() {
        return new Builder();
    }

    private TargetLightsOptions() {}

    private int adminId;
    private int companyId;
    private boolean worldDelivery; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean adminTestDelivery; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private TargetGroupDeliveryOption deliveryOption;
    private boolean content;
    private DateRange creationDate;
    private DateRange changeDate;
    private IntRange complexity;
    private int recipientCountBasedComplexityAdjustment;
    private boolean isSearchName; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean isSearchDescription; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private String searchText = ""; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private boolean isRedesignedUiUsed; // TODO: remove after EMMGUI-714 will be finished and old design will removed
    private String searchName;
    private String searchDescription;
    private boolean deleted;
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

    public String getSearchName() {
        return searchName;
    }

    public DateRange getCreationDate() {
        return creationDate;
    }

    public DateRange getChangeDate() {
        return changeDate;
    }

    public IntRange getComplexity() {
        return complexity;
    }

    public int getRecipientCountBasedComplexityAdjustment() {
        return recipientCountBasedComplexityAdjustment;
    }

    public void setRecipientCountBasedComplexityAdjustment(int recipientCountBasedComplexityAdjustment) {
        this.recipientCountBasedComplexityAdjustment = recipientCountBasedComplexityAdjustment;
    }

    public void setComplexity(IntRange complexity) {
        this.complexity = complexity;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchName(boolean searchName) {
        isSearchName = searchName;
    }

    public void setSearchDescription(boolean searchDescription) {
        isSearchDescription = searchDescription;
    }

    public boolean isRedesignedUiUsed() {
        return isRedesignedUiUsed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public TargetGroupDeliveryOption getDeliveryOption() {
        return deliveryOption;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(searchName) || isNotBlank(searchDescription) || (complexity != null && complexity.isPresent())
                || deliveryOption != null || creationDate.isPresent() || changeDate.isPresent();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("adminId", adminId)
                .append("companyId", companyId)
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
        return Objects.hash(adminId, companyId, worldDelivery, adminTestDelivery, content, isSearchName, isSearchDescription, searchText, altgMode);
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
        
        public Builder setSearchName(String name) {
            options.searchName = name;
            return this;
        }

        public Builder setDeleted(boolean deleted) {
            options.deleted = deleted;
            return this;
        }

        public Builder setSearchDescription(String description) {
            options.searchDescription = description;
            return this;
        }

        public Builder setRedesignedUiUsed(boolean value) {
            options.isRedesignedUiUsed = value;
            return this;
        }

        public Builder setCreationDate(DateRange creationDate) {
            options.creationDate = creationDate;
            return this;
        }

        public Builder setChangeDate(DateRange changeDate) {
            options.changeDate = changeDate;
            return this;
        }

        public Builder setDeliveryOption(TargetGroupDeliveryOption deliveryOption) {
            options.deliveryOption = deliveryOption;
            return this;
        }

        public TargetLightsOptions build() {
            TargetLightsOptions result = this.options;
            this.options = null;
            return  result;
        }
    }
}
