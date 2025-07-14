/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.FormSearchParams;

public class MailingComparisonSearchParams
        extends MailingComparisonFilter
        implements FormSearchParams<MailingComparisonFilter> {

    @Override
    public void storeParams(MailingComparisonFilter filter) {
        this.setMailing(filter.getMailing());
        this.setDescription(filter.getDescription());
        this.setSendDate(filter.getSendDate());
    }

    @Override
    public void restoreParams(MailingComparisonFilter filter) {
        filter.setMailing(this.getMailing());
        filter.setDescription(this.getDescription());
        filter.setSendDate(this.getSendDate());
    }

    @Override
    public void resetParams() {
        this.setMailing("");
        this.setDescription("");
        this.setSendDate(new DateRange());
    }
}
