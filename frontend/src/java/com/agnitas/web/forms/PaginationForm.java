/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public class PaginationForm {

    private String sort = "";
    private String order = "";
    private int page = 1;
    private int numberOfRows = -1;

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public String getDir() {
        return order;
    }

    public void setDir(String order) {
        this.order = order;
    }

    public String getSortOrDefault(String defVal) {
        return StringUtils.isBlank(getSort()) ? defVal : getSort();
    }

    public boolean ascending() {
        return AgnUtils.sortingDirectionToBoolean(getOrder(), true);
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("sort", sort);
        map.put("order", order);
        map.put("page", page);
        map.put("numberOfRows", numberOfRows);

        return map;
    }

    public Object[] toArray() {
        return new Object[]{sort, order, page, numberOfRows};
    }
}
