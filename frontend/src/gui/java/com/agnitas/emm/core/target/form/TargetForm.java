/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption;
import com.agnitas.web.forms.PaginationForm;

public class TargetForm extends PaginationForm {

    private String searchDescription;
    private String searchName;
    private TargetComplexityGrade searchComplexity;
    private TargetGroupDeliveryOption searchDeliveryOption;
    private DateRange searchCreationDate = new DateRange();
    private DateRange searchChangeDate = new DateRange();
    private boolean showDeleted;

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    public DateRange getSearchCreationDate() {
        return searchCreationDate;
    }

    public void setSearchCreationDate(DateRange searchCreationDate) {
        this.searchCreationDate = searchCreationDate;
    }

    public DateRange getSearchChangeDate() {
        return searchChangeDate;
    }

    public void setSearchChangeDate(DateRange searchChangeDate) {
        this.searchChangeDate = searchChangeDate;
    }

    public TargetComplexityGrade getSearchComplexity() {
        return searchComplexity;
    }

    public void setSearchComplexity(TargetComplexityGrade searchComplexity) {
        this.searchComplexity = searchComplexity;
    }

    public TargetGroupDeliveryOption getSearchDeliveryOption() {
        return searchDeliveryOption;
    }

    public void setSearchDeliveryOption(TargetGroupDeliveryOption searchDeliveryOption) {
        this.searchDeliveryOption = searchDeliveryOption;
    }

    public boolean isShowDeleted() {
        return showDeleted;
    }

    public void setShowDeleted(boolean showDeleted) {
        this.showDeleted = showDeleted;
    }
}
