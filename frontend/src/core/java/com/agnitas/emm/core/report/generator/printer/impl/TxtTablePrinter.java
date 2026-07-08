/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.printer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.bouncycastle.util.Strings;

import com.agnitas.emm.core.report.generator.bean.ColumnDefinition;
import com.agnitas.emm.core.report.generator.bean.Row;
import com.agnitas.emm.core.report.generator.bean.Table;
import com.agnitas.emm.core.report.generator.bean.TableDefinition;
import com.agnitas.emm.core.report.generator.bean.impl.RowImpl;
import com.agnitas.emm.core.report.generator.constants.TableSpecialCharacters;
import com.agnitas.emm.core.report.generator.printer.GenericTablePrinter;
import com.agnitas.messages.I18nString;

public class TxtTablePrinter implements GenericTablePrinter<Table> {

    private static final int HORIZONTAL_MARGIN = 1;

    private List<String> requiredNonBreakable;
    private String lineBreaker;
    private String emptyValue;

    public TxtTablePrinter(String lineBreaker, List<String> makeCharactersNonBreakable, String emptyValue) {
        this.lineBreaker = lineBreaker;
        this.requiredNonBreakable = makeCharactersNonBreakable;
        this.emptyValue = emptyValue;
    }

    @Override
    public String print(Table table, Locale locale) {
        TableDefinition tableDefinition = table.getTableDefinition();

        List<String> order = tableDefinition.getOrder();
        Map<String, ColumnDefinition> columnDefinitions = table.getColumnDefinitions();
        List<Row> rows = table.getRows();

        if (columnDefinitions.isEmpty() || order.isEmpty()) {
            return StringUtils.EMPTY;
        }

        StringBuilder textTable = new StringBuilder();
        textTable.append(getTitleBlock(tableDefinition, columnDefinitions, locale));
        textTable.append(getColumnBlock(order, columnDefinitions, locale));
        for (Row row : rows) {
            textTable.append(getRowsBlock(order, columnDefinitions, row));
        }
        textTable.append(getHorizontalBorder(order, columnDefinitions, HORIZONTAL_MARGIN));

        return getReplacedText(textTable);
    }

    private StringBuilder getTitleBlock(TableDefinition tableDefinition, Map<String, ColumnDefinition> columnDefinitions, Locale locale) {
        List<String> order = tableDefinition.getOrder();
        String translationKey = tableDefinition.getTitleTranslationKey();
        String defaultTitle = tableDefinition.getDefaultTitle();

        String title = I18nString.getLocaleStringOrDefault(translationKey, locale, StringUtils.defaultString(defaultTitle));
        if (StringUtils.isBlank(title)) {
            return new StringBuilder();
        }

        StringBuilder out = new StringBuilder(getHorizontalBorder(order, columnDefinitions, HORIZONTAL_MARGIN));
        int contentLength = out.length() - 4;
        String centeredTitle = StringUtils.center(title, contentLength, TableSpecialCharacters.NON_BREAKING_SPACE);

        return out.append(TableSpecialCharacters.VERTICAL_BORDER)
                .append(centeredTitle.toUpperCase())
                .append(TableSpecialCharacters.VERTICAL_BORDER)
                .append(lineBreaker);
    }

    private String getReplacedText(StringBuilder out) {
        String replacedStr = out.toString();
        for (String replaceFrom : requiredNonBreakable) {
            String replaceTo = TableSpecialCharacters.WORD_JOINER + "$1" + TableSpecialCharacters.WORD_JOINER;
            replacedStr = replacedStr.replaceAll("(" + replaceFrom + ")", replaceTo);
        }

        return replacedStr.replaceAll(" ", TableSpecialCharacters.NON_BREAKING_SPACE);
    }

    private StringBuilder getColumnBlock(List<String> order, Map<String, ColumnDefinition> definitions, Locale locale) {
        Map<String, String> columns = new HashMap<>();
        for (String columnKey : order) {
            if (definitions.containsKey(columnKey)) {
                ColumnDefinition columnDefinition = definitions.get(columnKey);
                String translationKey = columnDefinition.getTranslationKey();
                String defaultColumnName = columnDefinition.getValue();
                String columnName = I18nString.getLocaleStringOrDefault(translationKey, locale, defaultColumnName);
                String capitalizedColumnName = Strings.toUpperCase(columnName);		// TODO Do we really need Bouncy Castle here???
                columns.put(columnKey, capitalizedColumnName);

                // in case of translated column title bigger then column width we need to increase the width
                if (columnName.length() > columnDefinition.getWidth()) {
                    columnDefinition.setWidth(columnName.length());
                }
            }
        }

        return getRowsBlock(order, definitions, new RowImpl(columns));
    }

    // todo: abbreviate (WordUtils#abbreviate) long words (more then max length of column) before wrapping.
    private StringBuilder getRowsBlock(List<String> order, Map<String, ColumnDefinition> columnDefinitions, Row row) {
        List<String[]> textBlocks = new ArrayList<>();
        Map<String, String> columns = row.getColumns();
        int maxHeight = 0;
        for (String columnKey : order) {
            String columnText = columns.get(columnKey);
            String[] blockLines;
            if (StringUtils.isNotBlank(columnText)) {
                int columnWidth = columnDefinitions.get(columnKey).getWidth();
                String columnBlock = WordUtils.wrap(columnText, columnWidth, lineBreaker, true);
                blockLines = columnBlock.split("(" + lineBreaker + "|\r|\n)");
            } else {
                blockLines = new String[]{emptyValue};
            }
            textBlocks.add(blockLines);
            maxHeight = maxHeight < blockLines.length ? blockLines.length : maxHeight;
        }

        return appendTextBlocks(order, columnDefinitions, textBlocks, maxHeight);
    }

    private StringBuilder appendTextBlocks(List<String> order, Map<String, ColumnDefinition> definitions,
                                           List<String[]> textBlock, int maxHeight) {

        StringBuilder row = new StringBuilder(getHorizontalBorder(order, definitions, HORIZONTAL_MARGIN));
        for (int lineIndex = 0; lineIndex < maxHeight; lineIndex++) {
            for (int columnIndex = 0; columnIndex < textBlock.size(); columnIndex++) {
                int columnWidth = definitions.get(order.get(columnIndex)).getWidth();
                String[] linePart = textBlock.get(columnIndex);

                StringBuilder lineFormat = new StringBuilder();
                lineFormat.append(TableSpecialCharacters.VERTICAL_BORDER);
                lineFormat.append(StringUtils.repeat(TableSpecialCharacters.NON_BREAKING_SPACE, HORIZONTAL_MARGIN));
                lineFormat.append("%-").append(columnWidth).append("s");
                lineFormat.append(StringUtils.repeat(TableSpecialCharacters.NON_BREAKING_SPACE, HORIZONTAL_MARGIN));
                if (columnIndex >= textBlock.size() - 1) {
                    lineFormat.append(TableSpecialCharacters.VERTICAL_BORDER);
                }

                String currentLinePart;
                if (linePart.length > lineIndex) {
                    currentLinePart = StringUtils.rightPad(linePart[lineIndex], columnWidth);
                } else {
                    currentLinePart = StringUtils.repeat(TableSpecialCharacters.NON_BREAKING_SPACE, columnWidth);
                }

                row.append(String.format(lineFormat.toString(), currentLinePart));
            }
            row.append(lineBreaker);
        }

        return row;
    }

    private StringBuilder getHorizontalBorder(List<String> order, Map<String, ColumnDefinition> definitions, int horizontalMargin) {
        StringBuilder line = new StringBuilder();
        for (int columnIndex = 0; columnIndex < order.size(); columnIndex++) {
            String columnKey = order.get(columnIndex);
            line.append(TableSpecialCharacters.CORNER);
            if (definitions.containsKey(columnKey)) {
                int horizontalSeparatorWidth = definitions.get(columnKey).getWidth() + 2 * horizontalMargin;
                line.append(StringUtils.repeat(TableSpecialCharacters.HORIZONTAL_BORDER, horizontalSeparatorWidth));
            }
            if (columnIndex >= order.size() - 1) {
                line.append(TableSpecialCharacters.CORNER);
            }
        }
        line.append(lineBreaker);
        return line;
    }
}
