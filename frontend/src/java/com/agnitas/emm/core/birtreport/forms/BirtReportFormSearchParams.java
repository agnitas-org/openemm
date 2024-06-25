/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.web.forms.FormSearchParams;

public class BirtReportFormSearchParams implements FormSearchParams<BirtReportOverviewFilter> {

    private String name;
    private DateRange changeDate = new DateRange();
    private DateRange lastDeliveryDate = new DateRange();

    @Override
    public void storeParams(BirtReportOverviewFilter filter) {
        this.name = filter.getName();
        this.lastDeliveryDate = filter.getLastDeliveryDate();
        this.changeDate = filter.getChangeDate();
    }

    @Override
    public void restoreParams(BirtReportOverviewFilter filter) {
        filter.setName(name);
        filter.setLastDeliveryDate(lastDeliveryDate);
        filter.setChangeDate(changeDate);
    }

    @Override
    public void resetParams() {
        name = "";
        changeDate = new DateRange();
        lastDeliveryDate = new DateRange();
    }
}
