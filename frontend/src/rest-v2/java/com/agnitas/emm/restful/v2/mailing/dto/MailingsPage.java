/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.dto;

import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.restful.v2.infrastructure.search.dto.PageForm;

public class MailingsPage extends PageForm {

    private String name;
    private Set<MailingType> types;
    private Set<Integer> mailinglistIds;
    private Set<MailingStatus> statuses;
    private String sort;

    @Override
    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String getSort() {
        return sort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<MailingType> getTypes() {
        return types;
    }

    public void setTypes(Set<MailingType> types) {
        this.types = types;
    }

    public Set<Integer> getMailinglistIds() {
        return mailinglistIds;
    }

    public void setMailinglistIds(Set<Integer> mailinglistIds) {
        this.mailinglistIds = mailinglistIds;
    }

    public Set<MailingStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<MailingStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MailingsPage that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return mailinglistIds == that.mailinglistIds
               && Objects.equals(name, that.name)
               && Objects.equals(types, that.types)
               && Objects.equals(statuses, that.statuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, types, mailinglistIds, statuses);
    }
}
