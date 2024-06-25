/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class BlacklistOverviewFilter extends PaginationForm {
    private String email;
    private String reason;
    private DateRange creationDate = new DateRange();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DateRange getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(DateRange creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Object[] toArray() {
        return ArrayUtils.addAll(Arrays.asList(email, reason, creationDate).toArray(), super.toArray());
    }
}
