/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
    private TargetGroupDeliveryOption deliveryOption;
    private boolean content;
    private DateRange creationDate;
    private DateRange changeDate;
    private IntRange complexity;
    private int recipientCountBasedComplexityAdjustment;
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

    public boolean isContent() {
        return content;
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
                .append("deliveryOption", deliveryOption)
                .append("content", content)
                .append("altgMode", altgMode)
                .append("creationDate", creationDate)
                .append("changeDate", changeDate)
                .append("changeDate", changeDate)
                .append("complexity", complexity)
                .append("recipientCountBasedComplexityAdjustment", recipientCountBasedComplexityAdjustment)
                .append("searchName", searchName)
                .append("searchDescription", searchDescription)
                .append("deleted", deleted)
                .append("pageNumber", pageNumber)
                .append("pageSize", pageSize)
                .append("sorting", sorting)
                .append("direction", direction)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TargetLightsOptions that)) {
            return false;
        }
        return adminId == that.adminId
               && companyId == that.companyId
               && content == that.content
               && recipientCountBasedComplexityAdjustment == that.recipientCountBasedComplexityAdjustment
               && deleted == that.deleted
               && pageNumber == that.pageNumber
               && pageSize == that.pageSize
               && deliveryOption == that.deliveryOption
               && altgMode == that.altgMode
               && Objects.equals(creationDate, that.creationDate)
               && Objects.equals(changeDate, that.changeDate)
               && Objects.equals(complexity, that.complexity)
               && Objects.equals(searchName, that.searchName)
               && Objects.equals(searchDescription, that.searchDescription)
               && Objects.equals(sorting, that.sorting)
               && Objects.equals(direction, that.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adminId, companyId, deliveryOption, content, creationDate, changeDate, complexity,
            recipientCountBasedComplexityAdjustment, searchName, searchDescription, deleted, altgMode, pageNumber,
            pageSize, sorting, direction);
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

        public Builder setContent(boolean content) {
            options.content = content;
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
