/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.generator.parser.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import com.agnitas.emm.core.report.generator.TextColumn;
import com.agnitas.emm.core.report.generator.TextTable;
import com.agnitas.emm.core.report.generator.bean.ColumnDefinition;
import com.agnitas.emm.core.report.generator.bean.Row;
import com.agnitas.emm.core.report.generator.bean.Table;
import com.agnitas.emm.core.report.generator.bean.TableDefinition;
import com.agnitas.emm.core.report.generator.bean.impl.ColumnDefinitionImpl;
import com.agnitas.emm.core.report.generator.bean.impl.RowImpl;
import com.agnitas.emm.core.report.generator.bean.impl.TableDefinitionImpl;
import com.agnitas.emm.core.report.generator.bean.impl.TableImpl;
import com.agnitas.emm.core.report.generator.parser.GenericTableParser;

public class ReflectionTableParser implements GenericTableParser<Table> {

    private Class<? extends TextTable> textTableClass;
    private Class<? extends TextColumn> textColumnClass;

    public ReflectionTableParser(Class<? extends TextTable> textTableClass, Class<? extends TextColumn> textColumnClass) {
        this.textTableClass = textTableClass;
        this.textColumnClass = textColumnClass;
    }

    @Override
    public Table parse(Collection<?> collection) {
        TableDefinition tableDefinition = new TableDefinitionImpl();
        Map<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();
        List<Row> rows = getRows(collection);

        List<Object> uniqueMappedObjects = getUniqueMappedObjects(collection);
        for (Object uniqueMappedObject : uniqueMappedObjects) {
            updateTableDefinition(tableDefinition, uniqueMappedObject);
            columnDefinitions.putAll(getColumnDefinitions(uniqueMappedObject));
        }

        updateOrder(tableDefinition, columnDefinitions);
        updateWidths(columnDefinitions, rows, tableDefinition.getOrder());

        return new TableImpl(tableDefinition, columnDefinitions, rows);
    }

    private List<Object> getUniqueMappedObjects(Collection<?> objects) {
        List<Object> uniqueMappedObjects = new ArrayList<>();
        Set<String> alreadyProcessedClasses = new HashSet<>();

        for (Object object : objects) {
            String canonicalName = object.getClass().getCanonicalName();
            if (!alreadyProcessedClasses.contains(canonicalName)) {
                alreadyProcessedClasses.add(object.getClass().getCanonicalName());
                Class<?> clazz = object.getClass();
                TextTable textTable = AnnotationUtils.findAnnotation(clazz, textTableClass);
                if (Objects.nonNull(textTable)) {
                    uniqueMappedObjects.add(object);
                }
            }
        }

        return uniqueMappedObjects;
    }

    private void updateTableDefinition(TableDefinition originalTableDefinition, Object object) {
        Class<?> clazz = object.getClass();
        TextTable textTable = AnnotationUtils.findAnnotation(clazz, textTableClass);

        String currentTitleTranslationKey = StringUtils.defaultString(textTable.translationKey());
        String currentDefaultTitle = StringUtils.defaultString(textTable.defaultTitle());
        Set<String> currentOrder = new LinkedHashSet<>(Arrays.asList(textTable.order()));

        // updates translation key
        String originalTitleTranslationKey = originalTableDefinition.getTitleTranslationKey();
        if (StringUtils.isBlank(originalTitleTranslationKey) && StringUtils.isNotBlank(currentTitleTranslationKey)) {
            originalTableDefinition.setTitleTranslationKey(currentTitleTranslationKey);
        }

        // updates default title
        String originalDefaultTitle = originalTableDefinition.getDefaultTitle();
        if (StringUtils.isBlank(originalDefaultTitle) && StringUtils.isNotBlank(currentDefaultTitle)) {
            originalTableDefinition.setDefaultTitle(currentDefaultTitle);
        }

        // updates update order
        LinkedHashSet<String> originalOrder = Objects.nonNull(originalTableDefinition.getOrder()) ?
                new LinkedHashSet<>(originalTableDefinition.getOrder()) :
                new LinkedHashSet<>();

        if (!originalOrder.containsAll(currentOrder)) {
            originalOrder.addAll(currentOrder);
            originalTableDefinition.setOrder(new ArrayList<>(originalOrder));
        }
    }

    private Map<String, ColumnDefinition> getColumnDefinitions(Object object) {
        LinkedHashMap<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();
        columnDefinitions.putAll(getColumnDefinitionsFromMethods(object));
        columnDefinitions.putAll(getColumnDefinitionsFromFields(object));

        return columnDefinitions;
    }

    private List<Row> getRows(Collection<?> objects) {
        List<Row> rows = new ArrayList<>(objects.size());
        for (Object object : objects) {
            // get columns of row from methods
            Map<String, String> rowColumnsFromObjectMethods = getRowColumnsFromObjectMethods(object);
            if (MapUtils.isNotEmpty(rowColumnsFromObjectMethods)) {
                rows.add(new RowImpl(rowColumnsFromObjectMethods));
            }
            // get columns of row from fields
            Map<String, String> rowColumnsFromObjectFields = getRowColumnsFromObjectFields(object);
            if (MapUtils.isNotEmpty(rowColumnsFromObjectFields)) {
                rows.add(new RowImpl(rowColumnsFromObjectFields));
            }
        }

        return rows;
    }

    /**
     * Updates width of each column in ColumnDefinitions in case of width wasn't setted up.
     *
     * @param columnDefinitions description of each column in table
     * @param rows              rows in table
     * @param order             list of column's keys
     */
    private void updateWidths(Map<String, ColumnDefinition> columnDefinitions, List<Row> rows, List<String> order) {
        Set<String> keysForCalculation = new HashSet<>();
        for (String key : order) {
            ColumnDefinition columnDefinition = columnDefinitions.get(key);
            if (columnDefinition.getWidth() <= 0) {
                keysForCalculation.add(key);
            }
        }

        for (Row row : rows) {
            for (String key : keysForCalculation) {
                int lengthOfColumnContent = row.getColumns().get(key).length();
                if (columnDefinitions.get(key).getWidth() < lengthOfColumnContent) {
                    columnDefinitions.get(key).setWidth(lengthOfColumnContent);
                }
            }
        }
    }

    private Map<String, ColumnDefinition> getColumnDefinitionsFromMethods(Object object) {
        Class<?> clazz = object.getClass();
        if (Objects.isNull(AnnotationUtils.findAnnotation(clazz, textTableClass))) {
            return Collections.emptyMap();
        }

        Map<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();
        for (Method method : object.getClass().getDeclaredMethods()) {
            TextColumn textColumn = AnnotationUtils.findAnnotation(method, textColumnClass);
            if (Objects.nonNull(textColumn) && (method.getParameterCount() == 0)) {
                String columnKey = StringUtils.defaultIfEmpty(textColumn.key(), method.getName());
                columnDefinitions.put(columnKey, getColumnDefinition(textColumn, method));
            }
        }

        return columnDefinitions;
    }

    private Map<String, ColumnDefinition> getColumnDefinitionsFromFields(Object object) {
        Class<?> clazz = object.getClass();
        if (Objects.isNull(AnnotationUtils.findAnnotation(clazz, textTableClass))) {
            return Collections.emptyMap();
        }

        Map<String, ColumnDefinition> columnDefinitions = new LinkedHashMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(textColumnClass)) {
                TextColumn textColumn = field.getAnnotation(textColumnClass);
                String columnKey = StringUtils.defaultIfEmpty(textColumn.key(), field.getName());
                columnDefinitions.put(columnKey, getColumnDefinition(textColumn, field));
            }
        }

        return columnDefinitions;
    }

    private ColumnDefinition getColumnDefinition(TextColumn textColumn, Member member) {
        String translationKey = textColumn.translationKey();
        String defaultName = StringUtils.defaultIfEmpty(textColumn.defaultValue(), member.getName());
        int width = textColumn.width();

        return new ColumnDefinitionImpl(defaultName, translationKey, width);
    }

    private Map<String, String> getRowColumnsFromObjectMethods(Object object) {
        Class<?> clazz = object.getClass();
        if (Objects.isNull(AnnotationUtils.findAnnotation(clazz, textTableClass))) {
            return Collections.emptyMap();
        }

        Map<String, String> rowColumns = new LinkedHashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            TextColumn textColumn = AnnotationUtils.findAnnotation(method, textColumnClass);
            if (Objects.nonNull(textColumn) && (method.getParameterCount() == 0)) {
                String columnKey = StringUtils.defaultIfEmpty(textColumn.key(), method.getName());
                rowColumns.put(columnKey, getColumnValueFromMethod(method, object));
            }
        }

        return rowColumns;
    }

    private String getColumnValueFromMethod(Method method, Object object) {
        Object columnValue = null;
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
                if (method.getParameterCount() == 0) {
                    columnValue = method.invoke(object);
                }
                method.setAccessible(false);
            } else {
                if (method.getParameterCount() == 0) {
                    columnValue = method.invoke(object);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        	// do nothing
        }

        return columnValue != null ? columnValue.toString() : StringUtils.EMPTY;
    }

    private Map<String, String> getRowColumnsFromObjectFields(Object object) {
        Class<?> clazz = object.getClass();
        if (Objects.isNull(AnnotationUtils.findAnnotation(clazz, textTableClass))) {
            return Collections.emptyMap();
        }

        Map<String, String> columns = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(textColumnClass)) {
                TextColumn textColumn = field.getAnnotation(textColumnClass);
                String columnKey = StringUtils.defaultIfEmpty(textColumn.key(), field.getName());
                columns.put(columnKey, getColumnValueFromField(field, object));
            }
        }

        return columns;
    }

    private String getColumnValueFromField(Field field, Object object) {
        Object columnValue = null;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
                columnValue = field.get(object);
                field.setAccessible(false);
            } else {
                columnValue = field.get(object);
            }
        } catch (IllegalAccessException ignored) {
        	// do nothing
        }

        return columnValue != null ? columnValue.toString() : StringUtils.EMPTY;
    }

    private void updateOrder(TableDefinition tableDefinition, Map<String, ColumnDefinition> definitions) {
        List<String> order = tableDefinition.getOrder();
        if (CollectionUtils.isEmpty(order)) {
            tableDefinition.setOrder(new ArrayList<>(definitions.keySet()));
        }
    }
}
