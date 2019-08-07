/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.printer.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.agnitas.emm.core.report.generator.bean.ColumnDefinition;
import com.agnitas.emm.core.report.generator.bean.Row;
import com.agnitas.emm.core.report.generator.bean.Table;
import com.agnitas.emm.core.report.generator.printer.GenericTablePrinter;
import com.agnitas.messages.I18nString;

public class CsvTablePrinter implements GenericTablePrinter<Table> {

    private static final String WORD_SEPARATOR = ";";
    private String lineBreaker;
    private String emptyValue;

    public CsvTablePrinter(String lineBreaker, String emptyValue) {
        this.lineBreaker = lineBreaker;
        this.emptyValue = emptyValue;
    }

    @Override
    public String print(Table table, Locale locale) {
        List<String> order = table.getTableDefinition().getOrder();

        StringBuilder csvTable = new StringBuilder();
        csvTable.append(getColumnBlock(order, table.getColumnDefinitions(), locale));
        csvTable.append(getRowsBlock(order, table.getRows()));

        return csvTable.toString();
    }

    private StringBuilder getColumnBlock(List<String> order, Map<String, ColumnDefinition> columnDefinitions, Locale locale) {
        StringBuilder columnBlock = new StringBuilder();

        for (String columnKey : order) {
            ColumnDefinition columnDefinition = columnDefinitions.get(columnKey);
            String translationKey = columnDefinition.getTranslationKey();
            String defaultColumnName = columnDefinition.getValue();
            String columnName = I18nString.getLocaleStringOrDefault(translationKey, locale, defaultColumnName);
            columnBlock.append(columnName)
                    .append(WORD_SEPARATOR);
        }
        columnBlock.append(lineBreaker);
        return columnBlock;
    }

    private StringBuilder getRowsBlock(List<String> order, List<Row> rows) {
        StringBuilder rowsBlock = new StringBuilder();
        for (Row row : rows) {
            for (String columnKey : order) {
                String columnText = row.getColumns().get(columnKey);
                if (StringUtils.isNotBlank(columnText)) {
                    rowsBlock.append(row.getColumns().get(columnKey));
                } else {
                    rowsBlock.append(emptyValue);
                }
                rowsBlock.append(WORD_SEPARATOR);
            }
            rowsBlock.append(lineBreaker);
        }

        return rowsBlock;
    }
}
