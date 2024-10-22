/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms;

import com.agnitas.emm.core.commons.dto.DateRange;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class JobQueueOverviewFilter {

    private Integer id;
    private Boolean running;
    private String name;
    private DateRange startDate = new DateRange();
    private Boolean successful;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateRange getStartDate() {
        return startDate;
    }

    public void setStartDate(DateRange startDate) {
        this.startDate = startDate;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || startDate.isPresent() || id != null || running != null || successful != null;
    }
}
