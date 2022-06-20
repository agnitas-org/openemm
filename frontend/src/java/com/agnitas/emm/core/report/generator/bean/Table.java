/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.report.generator.TextColumn;

public interface Table {

    TableDefinition getTableDefinition();

    void setTableDefinition(TableDefinition tableDefinition);

    /**
     * Keys ether names of annotated fields/methods or specified {@link TextColumn#key()}.
     *
     * @return map of column with unique keys in default order.
     */
    Map<String, ColumnDefinition> getColumnDefinitions();

    void setColumnDefinitions(Map<String, ColumnDefinition> columnDefinitions);

    /**
     * @return list of rows which contains map of columns by unique key.
     */
    List<Row> getRows();

    void setRows(List<Row> rows);

}
