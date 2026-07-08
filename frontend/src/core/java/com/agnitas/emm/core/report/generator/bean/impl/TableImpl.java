/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.bean.impl;

import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.report.generator.bean.ColumnDefinition;
import com.agnitas.emm.core.report.generator.bean.Row;
import com.agnitas.emm.core.report.generator.bean.Table;
import com.agnitas.emm.core.report.generator.bean.TableDefinition;

public class TableImpl implements Table {

    private TableDefinition tableDefinition;

    private Map<String, ColumnDefinition> columnDefinitions;

    private List<Row> rows;

    public TableImpl(TableDefinition tableDefinition, Map<String, ColumnDefinition> columnDefinitions, List<Row> rows) {
        this.tableDefinition = tableDefinition;
        this.columnDefinitions = columnDefinitions;
        this.rows = rows;
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void setTableDefinition(TableDefinition tableDefinition) {
        this.tableDefinition = tableDefinition;
    }

    @Override
    public Map<String, ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    @Override
    public void setColumnDefinitions(Map<String, ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    @Override
    public List<Row> getRows() {
        return rows;
    }

    @Override
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}
