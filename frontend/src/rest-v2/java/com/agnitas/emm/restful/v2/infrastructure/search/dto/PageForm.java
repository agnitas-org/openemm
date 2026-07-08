/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.search.dto;

import java.util.Objects;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class PageForm {

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(200)
    private Integer pageSize = 20;

    @Pattern(regexp = "ASC|asc|DESC|desc", message = "Allowed values: ['asc', 'desc']")
    private String order = "ASC";

    public void setSort(String sort) {
        // overridden in child classes
    }

    public String getSort() {
        return "";
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PageForm pageForm)) {
            return false;
        }
        return Objects.equals(page, pageForm.page)
               && Objects.equals(pageSize, pageForm.pageSize)
               && Objects.equals(order, pageForm.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, pageSize, order);
    }
}
