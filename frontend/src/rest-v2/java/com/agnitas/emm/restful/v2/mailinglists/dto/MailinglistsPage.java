/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailinglists.dto;

import java.util.Objects;

import com.agnitas.emm.restful.v2.infrastructure.search.dto.PageForm;
import com.agnitas.emm.restful.v2.infrastructure.search.validator.ValidSort;
import org.apache.commons.lang3.StringUtils;

public class MailinglistsPage extends PageForm {

    @ValidSort(enumClass = MailinglistsSortKey.class)
    private String sort;

    @Override
    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public String getSort() {
        if (StringUtils.isBlank(sort)) {
            return MailinglistsSortKey.ID.dbColumn();
        }
        return MailinglistsSortKey.valueOf(sort.toUpperCase()).dbColumn();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MailinglistsPage that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sort);
    }
}
