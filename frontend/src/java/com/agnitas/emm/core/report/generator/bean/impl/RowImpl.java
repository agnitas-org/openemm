/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean.impl;

import java.util.Map;
import java.util.Objects;

import com.agnitas.emm.core.report.generator.bean.Row;

public class RowImpl implements Row {

    private Map<String, String> columns;

    public RowImpl() {
    }

    public RowImpl(Map<String, String> columns) {
        this.columns = columns;
    }

    @Override
    public Map<String, String> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(Map<String, String> columns) {
        this.columns = columns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RowImpl)) return false;
        RowImpl row = (RowImpl) o;
        return Objects.equals(getColumns(), row.getColumns());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getColumns());
    }
}
