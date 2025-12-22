/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.FormSearchParams;

public class JobQueueFormSearchParams implements FormSearchParams<JobQueueOverviewFilter> {

    private Integer id;
    private Boolean running;
    private String name;
    private DateRange nextStartDate = new DateRange();
    private Boolean successful;

    @Override
    public void storeParams(JobQueueOverviewFilter filter) {
        this.id = filter.getId();
        this.running = filter.getRunning();
        this.name = filter.getName();
        this.nextStartDate = filter.getNextStartDate();
        this.successful = filter.getSuccessful();
    }

    @Override
    public void restoreParams(JobQueueOverviewFilter filter) {
        filter.setId(id);
        filter.setRunning(running);
        filter.setName(name);
        filter.setNextStartDate(nextStartDate);
        filter.setSuccessful(successful);
    }

}
