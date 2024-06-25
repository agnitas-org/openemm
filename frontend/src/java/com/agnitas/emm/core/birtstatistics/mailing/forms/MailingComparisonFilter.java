/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.PaginationForm;

public class MailingComparisonFilter extends PaginationForm {

    private String mailing;
    private String description;
    private DateRange sendDate = new DateRange();

    public String getMailing() {
        return mailing;
    }

    public void setMailing(String mailing) {
        this.mailing = mailing;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateRange getSendDate() {
        return sendDate;
    }

    public void setSendDate(DateRange sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    public boolean ascending() {
        return AgnUtils.sortingDirectionToBoolean(getOrder(), false);
    }
}
