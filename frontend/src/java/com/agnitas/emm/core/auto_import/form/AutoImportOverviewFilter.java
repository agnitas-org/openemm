/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.auto_import.form;

import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.emm.core.autoimport.bean.AutoImport;
import org.agnitas.emm.core.autoimport.service.AutoImportBadge;
import org.agnitas.web.forms.PaginationForm;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AutoImportOverviewFilter extends PaginationForm {

    private String name;
    private String description;
    private List<AutoImport.AutoImportType> types;
    private Boolean active;
    private DateRange lastStartDate = new DateRange();
    private Boolean success;
    private List<AutoImportBadge> badges;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AutoImport.AutoImportType> getTypes() {
        return types;
    }

    public void setTypes(List<AutoImport.AutoImportType> types) {
        this.types = types;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public DateRange getLastStartDate() {
        return lastStartDate;
    }

    public void setLastStartDate(DateRange lastStartDate) {
        this.lastStartDate = lastStartDate;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<AutoImportBadge> getBadges() {
        return badges;
    }

    public void setBadges(List<AutoImportBadge> badges) {
        this.badges = badges;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || isNotBlank(description) || lastStartDate.isPresent() || isNotEmpty(types)
                || isNotEmpty(badges) || success != null || active != null;
    }
}
