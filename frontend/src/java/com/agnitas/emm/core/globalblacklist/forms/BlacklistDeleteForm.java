/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.forms;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.constraints.NotEmpty;

public class BlacklistDeleteForm {

    @NotEmpty(message = "error.email.empty")
    private String email;

    private List<Integer> mailingListIds;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getMailingListIds() {
        return mailingListIds;
    }

    public void setMailingListIds(List<Integer> mailingIds) {
        this.mailingListIds = mailingIds;
    }

    public Set<Integer> getMailingListIdSet() {
        if (CollectionUtils.isNotEmpty(mailingListIds)) {
            return new HashSet<>(mailingListIds);
        } else {
            return Collections.emptySet();
        }
    }
}
