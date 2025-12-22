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
import com.agnitas.web.forms.FormSearchParams;

public class TargetListFormSearchParams implements FormSearchParams<TargetForm> {

    private String name;
    private String description;
    private TargetGroupDeliveryOption deliveryOption;
    private DateRange creationDate = new DateRange();
    private DateRange changeDate = new DateRange();
    private TargetComplexityGrade complexity;

    @Override
    public void storeParams(TargetForm form) {
        this.name = form.getSearchName();
        this.description = form.getSearchDescription();
        this.deliveryOption = form.getSearchDeliveryOption();
        this.creationDate = form.getSearchCreationDate();
        this.changeDate = form.getSearchChangeDate();
        this.complexity = form.getSearchComplexity();
    }

    @Override
    public void restoreParams(TargetForm form) {
        form.setSearchName(name);
        form.setSearchDescription(description);
        form.setSearchDeliveryOption(deliveryOption);
        form.setSearchCreationDate(creationDate);
        form.setSearchChangeDate(changeDate);
        form.setSearchComplexity(complexity);
    }

}
